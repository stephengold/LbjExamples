/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.stephengold.lbjexamples;

import com.github.stephengold.sport.CameraInputProcessor;
import com.github.stephengold.sport.Constants;
import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.collision.AfMode;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.MultiSphere;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import org.lwjgl.glfw.GLFW;

/**
 * A physics demo that simulates a cable coiled around a horizontal barrel. The
 * cable is composed of capsule-shaped rigid segments. A hook is attached to the
 * free end of the cable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Windlass
        extends BasePhysicsApp<PhysicsSpace>
        implements PhysicsTickListener {
    // *************************************************************************
    // constants

    /**
     * simulation speed when "paused"
     */
    final private static float PAUSED_SPEED = 1e-9f;
    /**
     * simulation time step (in seconds)
     */
    final private static float timeStep = 0.0025f;
    // *************************************************************************
    // fields

    /**
     * All cable segments have exactly the same shape.
     */
    private CollisionShape segmentShape;
    /**
     * rotation of the barrel (in radians)
     */
    private float barrelXRotation;
    /**
     * simulation speed (simulated seconds per wall-clock second)
     */
    private float physicsSpeed = 1f;
    /**
     * input signal: 1&rarr;turn counter-clockwise (initially lowers the hook)
     */
    private int signalCcw;
    /**
     * input signal: 1&rarr;turn clockwise (initially raises the hook)
     */
    private int signalCw;
    /**
     * body that represents the barrel
     */
    private PhysicsRigidBody barrel;
    /**
     * orientation of the barrel
     */
    final private Quaternion barrelOrientation = new Quaternion();
    /**
     * location of the forward pivot in a segment's local coordinates
     */
    final private Vector3f localPivot = new Vector3f();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the Windlass application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Windlass application = new Windlass();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    /**
     * Create the PhysicsSpace. Invoked once during initialization.
     *
     * @return a new instance
     */
    @Override
    public PhysicsSpace createSpace() {
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        result.addTickListener(this);
        result.setAccuracy(timeStep);
        result.setGravity(new Vector3f(0f, -981f, 0f)); // 1 psu = 1 cm
        result.setMaxSubSteps(99); // default=4

        return result;
    }

    /**
     * Initialize this application.
     */
    @Override
    public void initialize() {
        super.initialize();

        configureCamera();
        configureInput();
        setBackgroundColor(Constants.SKY_BLUE);
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        float cableRadius = 1f; // should be much larger than collision margin
        Vector3f attachPoint = addBarrel(cableRadius);
        /*
         * Determine the segment length, which is also the distance between
         * successive pivots.
         */
        int numSegmentsPerCoil = 12;
        float deltaPhi = FastMath.TWO_PI / numSegmentsPerCoil;
        float z0 = attachPoint.z;
        float deltaX = 2.1f * cableRadius / numSegmentsPerCoil;
        float deltaY = 2f * z0 * (float) Math.tan(deltaPhi / 2f); // TODO FastMath
        float segmentLength = MyMath.hypotenuse(deltaX, deltaY);

        // The segment shape is a Z-axis capsule.
        assert segmentLength > 2f * cableRadius; // alternate segments collide!
        segmentShape = new CapsuleCollisionShape(
                cableRadius, segmentLength, PhysicsSpace.AXIS_Z);
        localPivot.set(0f, 0f, segmentLength / 2f);
        /*
         * Make the first cable segment tangent to the +Z side of the barrel
         * and attach it with a fixed joint (all DOFs locked).
         */
        float zRotation = FastMath.atan2(deltaX, deltaY);
        Quaternion orientation = new Quaternion().fromAngles(0f, zRotation, 0f);
        new Quaternion().fromAngles(FastMath.HALF_PI, 0f, 0f)
                .mult(orientation, orientation);

        PhysicsRigidBody segment = addCableSegment(attachPoint, orientation);
        New6Dof fixed = New6Dof.newInstance(
                segment, barrel, attachPoint, orientation, RotationOrder.XYZ);
        for (int axisIndex = 0; axisIndex < MyVector3f.numAxes; ++axisIndex) {
            RotationMotor motor = fixed.getRotationMotor(axisIndex);
            motor.set(MotorParam.LowerLimit, 0f);
            motor.set(MotorParam.UpperLimit, 0f);
        }
        physicsSpace.addJoint(fixed);

        Quaternion rotatePhi = new Quaternion().fromAngles(deltaPhi, 0f, 0f);
        int numCoils = 4;
        int numCoiledSegments = numCoils * numSegmentsPerCoil;

        // Attach successive segments a spiral coiling around the barrel.
        float phi = FastMath.HALF_PI;
        PhysicsRigidBody endSegment = segment;
        Vector3f center = attachPoint.clone();
        for (int segmentI = 0; segmentI < numCoiledSegments; ++segmentI) {
            // Calculate the position of the next segment.
            center.x += deltaX;
            phi += deltaPhi;
            center.y = z0 * FastMath.cos(phi);
            center.z = z0 * FastMath.sin(phi);
            rotatePhi.mult(orientation, orientation);

            // Create a new segment and splice it to the existing cable.
            PhysicsRigidBody newSegment = addCableSegment(center, orientation);
            spliceCableSegments(newSegment, endSegment);

            endSegment = newSegment;
        }

        orientation.fromAngles(FastMath.HALF_PI, 0f, 0f);
        int numPendantSegments = 4;

        // Attach successive segments in vertical drop.
        for (int segmentI = 0; segmentI < numPendantSegments; ++segmentI) {
            // Calculate the location of the next segment.
            center.y -= segmentLength;

            // Create a new segment and splice it to the existing cable.
            PhysicsRigidBody newSegment = addCableSegment(center, orientation);
            spliceCableSegments(newSegment, endSegment);

            endSegment = newSegment;
        }

        addHook(endSegment, cableRadius);
    }

    /**
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * @param intervalSeconds the elapsed (real) time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float intervalSeconds) {
        float simSeconds = physicsSpeed * intervalSeconds;
        physicsSpace.update(simSeconds);
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just after each simulation step.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per simulation step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }

    /**
     * Callback from Bullet, invoked just before each simulation step.
     *
     * @param space the space that's about to be stepped (not null)
     * @param timeStep the time per simulation step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // Turn the barrel based on user-input signals.
        float turnRate = 4f; // radians per second
        barrelXRotation += (signalCcw - signalCw) * turnRate * timeStep;
        barrelOrientation.fromAngles(barrelXRotation, 0f, 0f);
        barrel.setPhysicsRotation(barrelOrientation);
    }
    // *************************************************************************
    // private methods

    /**
     * Add the barrel, which is a kinematic rigid body shaped like a horizontal
     * cylinder, with flanges and handles at both ends.
     *
     * @param cableRadius the radius of the cable (in physics-space units,
     * &gt;0)
     * @return the an attachment point for the cable (a location vector in
     * physics space)
     */
    private Vector3f addBarrel(float cableRadius) {
        int axis = PhysicsSpace.AXIS_X;

        float drumLength = 12f * cableRadius;
        float drumRadius = 0.6f * drumLength;
        CollisionShape cylinderShape = new CylinderCollisionShape(
                drumRadius, drumLength, axis);

        float flangeRadius = drumRadius + 3.5f * cableRadius;
        float flangeWidth = 0.1f * drumLength;
        CollisionShape flangeShape = new CylinderCollisionShape(
                flangeRadius, flangeWidth, axis);

        float handleRadius = 0.8f * cableRadius;
        float handleLength = 8f * cableRadius;
        CollisionShape handleShape = new CylinderCollisionShape(
                handleRadius, handleLength, axis);

        CompoundCollisionShape barrelShape = new CompoundCollisionShape(5);
        barrelShape.addChildShape(cylinderShape);

        float flangeX = (drumLength + flangeWidth) / 2f;
        barrelShape.addChildShape(flangeShape, +flangeX, 0f, 0f);
        barrelShape.addChildShape(flangeShape, -flangeX, 0f, 0f);

        float handleX = drumLength / 2f + flangeWidth + handleLength / 2f;
        float handleY = flangeRadius - handleRadius;
        barrelShape.addChildShape(handleShape, +handleX, +handleY, 0f);
        barrelShape.addChildShape(handleShape, -handleX, -handleY, 0f);

        float barrelMass = 100f;
        barrel = new PhysicsRigidBody(barrelShape, barrelMass);
        barrel.setKinematic(true);
        barrel.setAnisotropicFriction(new Vector3f(900f, 10f, 10f),
                AfMode.basic);
        barrel.setFriction(0f); // disable normal friction

        physicsSpace.addCollisionObject(barrel);
        visualizeShape(barrel);

        // Calculate an attachment point on the +Z side of the drum;
        float x0 = -0.49f * drumLength + cableRadius;
        float z0 = drumRadius + cableRadius;
        Vector3f result = new Vector3f(x0, 0f, z0);

        return result;
    }

    /**
     * Add a single segment of cable.
     *
     * @param center the desired center location in physics space (not null,
     * unaffected)
     * @param orientation the desired orientation in physics space (not null,
     * unaffected)
     * @return a new instance
     */
    private PhysicsRigidBody addCableSegment(
            Vector3f center, Quaternion orientation) {
        float mass = 0.2f;
        PhysicsRigidBody result = new PhysicsRigidBody(segmentShape, mass);

        result.setPhysicsLocation(center);
        result.setPhysicsRotation(orientation);

        physicsSpace.addCollisionObject(result);
        visualizeShape(result);

        return result;
    }

    /**
     * Attach a hook to the end of the cable.
     *
     * @param endSegment the final segment of the cable (not null)
     * @param cableRadius the radius of the cable (&gt;0)
     */
    private void addHook(PhysicsRigidBody endSegment, float cableRadius) {
        // Locate the final pivot.
        Transform endTransform = endSegment.getTransform(null);
        Vector3f pivotLocation = endTransform.transformVector(localPivot, null);
        /*
         * Collision shape is composed of 11 overlapping 2-sphere shapes,
         * arranged in a circular arc.
         */
        int numChildren = 11;
        int numSpheres = numChildren + 1;
        float hookRadius = 4f * cableRadius;
        float maxThick = 2.1f * cableRadius; // max thickness
        float minThick = 0.5f * cableRadius; // min thickness

        float[] radius = new float[numSpheres];
        float[] y = new float[numSpheres];
        float[] z = new float[numSpheres];
        float xAngle = 0f; // in radians
        for (int sphereI = 0; sphereI < numSpheres; ++sphereI) {
            float p = sphereI / (float) (numSpheres - 1); // goes from 0 to 1
            float p3 = FastMath.pow(p, 3f);
            float thickness = maxThick - p3 * (maxThick - minThick);
            radius[sphereI] = thickness / 2f;
            if (sphereI > 0) {
                xAngle += radius[sphereI] / hookRadius;
            }
            y[sphereI] = hookRadius * FastMath.cos(xAngle);
            z[sphereI] = -hookRadius * FastMath.sin(xAngle);
            xAngle += radius[sphereI] / hookRadius;
        }

        List<Vector3f> centers = new ArrayList<>(2);
        centers.add(new Vector3f());
        centers.add(new Vector3f());

        List<Float> radii = new ArrayList<>(2);
        radii.add(0f);
        radii.add(0f);

        CompoundCollisionShape shape = new CompoundCollisionShape(numChildren);
        for (int childI = 0; childI < numChildren; ++childI) {
            centers.get(0).set(0f, y[childI], z[childI]);
            radii.set(0, radius[childI]);

            int nextI = childI + 1;
            centers.get(1).set(0f, y[nextI], z[nextI]);
            radii.set(1, radius[nextI]);

            MultiSphere twoSphere = new MultiSphere(centers, radii);
            shape.addChildShape(twoSphere);
        }

        float hookMass = 3f;
        PhysicsRigidBody hook = new PhysicsRigidBody(shape, hookMass);
        hook.setAngularDamping(0.7f);
        hook.setLinearDamping(0.4f);

        float pivotY = hookRadius + maxThick / 2f;
        Vector3f center = pivotLocation.subtract(0f, pivotY, 0f);
        hook.setPhysicsLocation(center);
        physicsSpace.addCollisionObject(hook);
        visualizeShape(hook);

        Quaternion orientation = endTransform.getRotation(); // alias
        New6Dof joint = New6Dof.newInstance(hook, endSegment,
                pivotLocation, orientation, RotationOrder.XYZ);
        joint.setCollisionBetweenLinkedBodies(false);
        physicsSpace.addJoint(joint);
    }

    /**
     * Configure the Camera and CIP during startup.
     */
    private static void configureCamera() {
        cam.setLocation(new Vector3f(30f, 25f, 135f));
        cam.setAzimuth(-1.78f);
        cam.setUpAngle(-0.28f);

        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setMoveSpeed(50f);
        cip.setRotationMode(RotateMode.Immediate);
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_DOWN:
                        signalCcw = isPressed ? 1 : 0;
                        return;

                    case GLFW.GLFW_KEY_UP:
                        signalCw = isPressed ? 1 : 0;
                        return;

                    case GLFW.GLFW_KEY_PAUSE:
                    case GLFW.GLFW_KEY_PERIOD:
                        if (isPressed) {
                            togglePause();
                        }
                        return;
                }
                super.onKeyboard(keyId, isPressed);
            }
        });
    }

    /**
     * Connect the specified cable segments with a New6Dof joint.
     *
     * @param newSegment the new cable segment (not null)
     * @param endSegment the final segment of the cable so far (not null)
     */
    private void spliceCableSegments(
            PhysicsRigidBody newSegment, PhysicsRigidBody endSegment) {
        // Position the pivot.
        Transform endTransform = endSegment.getTransform(null);
        Vector3f pivotLocation = endTransform.transformVector(localPivot, null);

        Quaternion pivotOrientation = endSegment.getPhysicsRotation(null);
        New6Dof joint = New6Dof.newInstance(newSegment, endSegment,
                pivotLocation, pivotOrientation, RotationOrder.XYZ);
        joint.setCollisionBetweenLinkedBodies(false);

        RotationMotor zrMotor = joint.getRotationMotor(PhysicsSpace.AXIS_Z);
        zrMotor.set(MotorParam.Damping, 0.25f / timeStep);
        zrMotor.set(MotorParam.LowerLimit, 0f);
        zrMotor.set(MotorParam.UpperLimit, 0f);
        zrMotor.setSpringEnabled(true);

        physicsSpace.addJoint(joint);
    }

    private void togglePause() {
        physicsSpeed = (physicsSpeed <= PAUSED_SPEED) ? 1f : PAUSED_SPEED;
    }
}
