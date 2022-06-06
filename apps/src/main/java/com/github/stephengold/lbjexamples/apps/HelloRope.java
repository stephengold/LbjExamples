package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.sport.*;
import com.github.stephengold.sport.mesh.BoxMesh;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.joints.Point2PointJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import jme3utilities.math.MyVector3f;
import org.joml.Matrix2f;
import org.joml.Vector2fc;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import java.util.ArrayList;
import java.util.List;

public class HelloRope extends BasePhysicsApp<PhysicsSpace> implements PhysicsTickListener {
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloCcd application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        Platform platform = JmeSystem.getPlatform();
        if (platform.getOs() == Platform.Os.MacOS) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        HelloRope application = new HelloRope();
        application.start();
    }
    // *************************************************************************
    // BasePhysicsApp methods

    boolean turn;

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

        result.setAccuracy(0.005f);
        //result.getSolverInfo().setGlobalCfm(0.1f);
        // result.getSolverInfo().setJointErp(0.1f);
        result.getSolverInfo().setNumIterations(250);
        //result.getSolverInfo().setContactErp(0.8f);

        // Increase gravity to make the balls fall faster.

        return result;
    }

    Point2PointJoint lastJoint;

    @Override
    protected void initialize() {
        super.initialize();
        configureCamera();

        getInputManager().add(new InputProcessor() {
            @Override
            public void onMouseScroll(double xOffset, double yOffset) {
                super.onMouseScroll(xOffset, yOffset);
                zPlane -= yOffset;
            }

            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                super.onKeyboard(keyId, isPressed);
                switch (keyId) {
                    case GLFW.GLFW_KEY_SPACE:
                        if (mass != null)
                            mass.applyCentralImpulse(new Vector3f(0, 1000, 0));
                        break;
                    case GLFW.GLFW_KEY_ENTER:
                        if (isPressed) {
                            if (mass == null && lastJoint == null) {
                                attachMassToRope(1000);
                            } else {
                                if (lastJoint != null) {
                                    physicsSpace.removeJoint(lastJoint);
                                    lastJoint = null;
                                }
                                if (mass != null) {
                                    physicsSpace.removeCollisionObject(mass);
                                    mass = null;
                                }
                            }
                        }
                        break;
                    case GLFW.GLFW_KEY_UP:
                        if (isPressed) {
                            turn = !turn;

                        }
                        break;
                }
            }
        });
    }

    PhysicsRigidBody paddleBody;
    PhysicsRigidBody mass;
    List<RopeElement> ropeCapsules = new ArrayList<>();
    BoxMesh boxMesh;

    @Override
    public void populateSpace() {

        boxMesh = new BoxMesh(0.1f);
        boxMesh.generateFacetNormals();
        boxMesh.makeImmutable();

        CylinderCollisionShape cylinderCollisionShape = new CylinderCollisionShape(3,10,0);
        PhysicsRigidBody cylinder = new PhysicsRigidBody(cylinderCollisionShape,100);
        cylinder.setKinematic(true);
        physicsSpace.addCollisionObject(cylinder);
        visualizeShape(cylinder);

        BoxCollisionShape boxCollisionShape
                = new BoxCollisionShape(0.3f, 0.3f, 10f);
        paddleBody = new PhysicsRigidBody(boxCollisionShape, 100);
        paddleBody.setKinematic(true);

        physicsSpace.addCollisionObject(paddleBody);
        visualizeShape(paddleBody);
        //box.setWireframe(true);

        SphereCollisionShape capsuleCollisionShape = new SphereCollisionShape(1);
        PhysicsRigidBody firstCapsule = new PhysicsRigidBody(capsuleCollisionShape, 100);
        Vector3f capsuleLocation = new Vector3f(0, 5, 0);
        firstCapsule.setPhysicsLocation(capsuleLocation);
        physicsSpace.addCollisionObject(firstCapsule);
        BoxMesh boxMesh = new BoxMesh(0.3f);
        boxMesh.generateFacetNormals();
        boxMesh.makeImmutable();
        Geometry geometry = new Geometry(boxMesh);
        geometry.setColor(Constants.RED);
        geometry.setLocation(capsuleLocation);

        Point2PointJoint joint = new Point2PointJoint(firstCapsule, new Vector3f(), capsuleLocation);
        joint.setTau(1.2f);
        physicsSpace.addJoint(joint);

        visualizeShape(firstCapsule);
        ropeCapsules.add(new RopeElement(joint, firstCapsule));


        for (int i = 1; i < 30; ++i) {
            RopeElement currentElement = ropeCapsules.get(i - 1);
            addCapsuleToRope(capsuleCollisionShape, currentElement.capsule);
        }

    }

    private void addCapsuleToRope(SphereCollisionShape capsuleCollisionShape, PhysicsRigidBody lastCapsule) {
        PhysicsRigidBody currentCapsule = new PhysicsRigidBody(capsuleCollisionShape, 100);
        Vector3f capNLocation = lastCapsule.getPhysicsLocation(null).add(new Vector3f(0, -capsuleCollisionShape.getRadius(), 0));
        currentCapsule.setPhysicsLocation(capNLocation);
        physicsSpace.addCollisionObject(currentCapsule);

        Point2PointJoint jointN = new Point2PointJoint(lastCapsule, currentCapsule, new Vector3f(0, -capsuleCollisionShape.getRadius(), 0), new Vector3f(0, capsuleCollisionShape.getRadius(), 0));
        physicsSpace.addJoint(jointN);
        ropeCapsules.add(new RopeElement(jointN, currentCapsule));
        visualizeShape(currentCapsule);
    }

    int zPlane = 0;

    private final Vector3f mouseLocation = new Vector3f();

    @Override
    protected void render() {
        Vector2fc clipXy = getInputManager().locateCursor();
        if (clipXy != null) {
            float nearZ = -1f;
            Vector3f nearLocation = cam.clipToWorld(clipXy, nearZ, null);
            float farZ = +1f;
            Vector3f farLocation = cam.clipToWorld(clipXy, farZ, null);
            if (nearLocation.z > zPlane && farLocation.z < zPlane) {
                float dy = nearLocation.z - farLocation.z;
                float t = (nearLocation.z - zPlane) / dy;
                MyVector3f.lerp(t, nearLocation, farLocation, mouseLocation);
            }
        }
        super.render();
    }


    @Override
    public void updatePhysics(float intervalSeconds) {
        physicsSpace.update(intervalSeconds);
    }

    private static void configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.None);

        cam.setLocation(new Vector3f(0f, 0f, 40f));
    }

    private void attachMassToRope(int size) {
        RopeElement lastElement = ropeCapsules.get(ropeCapsules.size()-1);
        SphereCollisionShape sphere = new SphereCollisionShape(0.3f);
        mass = new PhysicsRigidBody(sphere, size);
        mass.setPhysicsLocation(lastElement.capsule.getPhysicsLocation(null).add(0, -1, 0));

        lastJoint = new Point2PointJoint(lastElement.capsule, mass, new Vector3f(0, -1, 0), new Vector3f(0, 1, 0));
        physicsSpace.addCollisionObject(mass);
        physicsSpace.addJoint(lastJoint);
        visualizeShape(mass);
    }


    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        Vector3f bodyLocation = mouseLocation.add(0f, 1, 0f);
        paddleBody.setPhysicsLocation(bodyLocation);

        if (turn) {

            RopeElement ropeElement = ropeCapsules.get(0);
            Vector3f capLoc = ropeElement.capsule.getPhysicsLocation(null);

            float angle = 1*timeStep;
            org.joml.Vector2f vec = new org.joml.Vector2f(capLoc.y, capLoc.z);
            Matrix2f rot = new Matrix2f(FastMath.cos(angle), FastMath.sin(angle), -FastMath.sin(angle), FastMath.cos(angle));
            vec.mul(rot);
            Vector3f finalPos = new Vector3f(capLoc.x, vec.x, vec.y);
            ropeElement.joint.setPivotInB(finalPos);

            Geometry geometry = new Geometry(boxMesh);
            geometry.setLocation(finalPos);
        }

    }

    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
    }

    public static class RopeElement {
        public Point2PointJoint joint;
        public PhysicsRigidBody capsule;

        public RopeElement(Point2PointJoint joint, PhysicsRigidBody capsule) {
            this.joint = joint;
            this.capsule = capsule;
        }
    }
}
