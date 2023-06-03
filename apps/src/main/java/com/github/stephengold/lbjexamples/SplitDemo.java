/*
 Copyright (c) 2020-2023, Stephen Gold and Yanis Boudiaf
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
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.InputProcessor;
import com.github.stephengold.sport.RotateMode;
import com.github.stephengold.sport.mesh.LineMesh;
import com.github.stephengold.sport.physics.BasePhysicsApp;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.EmptyShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import jme3utilities.minie.test.shape.ShapeGenerator;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

/**
 * Test/demonstrate splitting of rigid bodies.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SplitDemo extends BasePhysicsApp<PhysicsSpace> {
    // *************************************************************************
    // constants and loggers

    /**
     * simulation speed when "paused"
     */
    final private static float PAUSED_SPEED = 1e-9f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(SplitDemo.class.getName());
    /**
     * list of names known to ShapeGenerator
     */
    final private static String[] shapeNames = {
        "box", "capsule", "cone", "cylinder", "football", "frame",
        "halfPipe", "hull", "iBeam", "lidlessBox", "multiSphere", "sphere",
        "tetrahedron", "torus", "triangularFrame", "trident", "washer"
    };
    // *************************************************************************
    // fields

    /**
     * simulation speed (simulated seconds per wall-clock second)
     */
    private static float physicsSpeed = 1f;
    /**
     * angle between the normal of the splitting plane and default camera's "up"
     * vector (in radians, &ge;0, &lt;Pi)
     */
    private static float splitAngle = 0f;
    /**
     * visualize the splitting plane
     */
    private static Geometry splitterGeometry;
    /**
     * input signal: 1&rarr;rotate the splitting plane counter-clockwise
     */
    private static int signalCcw;
    /**
     * input signal: 1&rarr;rotate the splitting plane clockwise
     */
    private static int signalCw;
    /**
     * how many times render() has been invoked
     */
    private static int renderCount;
    /**
     * timestamp of the previous render() if renderCount > 0
     */
    private static long lastSplitterUpdate;
    /**
     * temporary storage for a Quaternion
     */
    final private static Quaternion tmpRotation = new Quaternion();
    /**
     * pseudo-random generator
     */
    final private static ShapeGenerator random = new ShapeGenerator();
    /**
     * first clip location used to define the splitting plane
     */
    final private static Vector2f clip1 = new Vector2f();
    /**
     * 2nd clip location used to define the splitting plane
     */
    final private static Vector2f clip2 = new Vector2f();
    /**
     * temporary storage for a vector
     */
    final private static Vector3f tmpLocation = new Vector3f();
    /**
     * first world location used to define the splitting plane
     */
    final private static Vector3f world1 = new Vector3f();
    /**
     * 2nd world location used to define the splitting plane
     */
    final private static Vector3f world2 = new Vector3f();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the SplitDemo application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        SplitDemo application = new SplitDemo();
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
        PhysicsBody.setDeactivationEnabled(false); // avoid a distraction
        PhysicsSpace result
                = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        result.setGravity(Vector3f.ZERO);

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

        LineMesh lineMesh = new LineMesh(Vector3f.ZERO, Vector3f.ZERO);
        splitterGeometry = new Geometry(lineMesh)
                .setProgram("Unshaded/Monochrome");
    }

    /**
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    @Override
    public void populateSpace() {
        restartScenario();
    }

    /**
     * Callback invoked once per frame.
     */
    @Override
    protected void render() {
        ++renderCount;

        // Advance the physics, but not during the first render().
        long nanoTime = System.nanoTime();
        if (renderCount > 1) {
            long nanoseconds = nanoTime - lastSplitterUpdate;
            float seconds = 1e-9f * nanoseconds;
            updateSplitter(seconds);
        }
        lastSplitterUpdate = nanoTime;

        super.render();
    }

    /**
     * Advance the physics simulation by the specified amount. Invoked during
     * each update.
     *
     * @param wallClockSeconds the elapsed wall-clock time since the previous
     * invocation of {@code updatePhysics} (in seconds, &ge;0)
     */
    @Override
    public void updatePhysics(float wallClockSeconds) {
        float simulateSeconds = physicsSpeed * wallClockSeconds;
        physicsSpace.update(simulateSeconds);
    }
    // *************************************************************************
    // private methods

    /**
     * Create a rigid body with the specified shape and debug normals and add it
     * to the PhysicsSpace at the origin, with random rotation.
     *
     * @param shape the collision shape to use (not null)
     * @param mass the desired mass (0 or 1)
     */
    private void addRigidBody(CollisionShape shape, float mass) {
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        visualizeShape(body);

        random.nextVector3f(tmpLocation);
        body.setPhysicsLocation(tmpLocation);
        random.nextQuaternion(tmpRotation);
        body.setPhysicsRotation(tmpRotation);

        physicsSpace.addCollisionObject(body);
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        CameraInputProcessor cip = getCameraInputProcessor();
        cip.setMoveSpeed(2f);
        cip.setRotationMode(RotateMode.DragLMB);

        cam.setLocation(new Vector3f(0f, 0f, 6.8f));
    }

    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        getInputManager().add(new InputProcessor() {
            @Override
            public void onKeyboard(int keyId, boolean isPressed) {
                switch (keyId) {
                    case GLFW.GLFW_KEY_TAB:
                        if (isPressed) {
                            makeSplittableAll();
                        }
                        return;

                    case GLFW.GLFW_KEY_HOME:
                    case GLFW.GLFW_KEY_KP_5:
                        if (isPressed) {
                            restartScenario();
                        }
                        return;

                    case GLFW.GLFW_KEY_ENTER:
                    case GLFW.GLFW_KEY_KP_0:
                    case GLFW.GLFW_KEY_SPACE:
                        if (isPressed) {
                            splitAll();
                        }
                        return;

                    case GLFW.GLFW_KEY_LEFT_BRACKET:
                        signalCw = isPressed ? 1 : 0;
                        return;

                    case GLFW.GLFW_KEY_RIGHT_BRACKET:
                        signalCcw = isPressed ? 1 : 0;
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
     * Ensure that all rigid bodies in the PhysicsSpace have splittable shapes.
     */
    private void makeSplittableAll() {
        Collection<PhysicsCollisionObject> allPcos = physicsSpace.getPcoList();
        for (PhysicsCollisionObject pco : allPcos) {
            PhysicsRigidBody body = (PhysicsRigidBody) pco;
            makeSplittable(body);
        }
    }

    /**
     * Ensure that the specified rigid body has a splittable shape.
     *
     * @param body (not null)
     */
    private void makeSplittable(PhysicsRigidBody body) {
        CollisionShape oldShape = body.getCollisionShape();
        CollisionShape splittableShape = oldShape.toSplittableShape();
        assert splittableShape.canSplit();
        body.setCollisionShape(splittableShape);
    }

    /**
     * Restart the current scenario.
     */
    private void restartScenario() {
        physicsSpace.destroy();
        assert physicsSpace.isEmpty();

        String shapeName = random.pick(shapeNames);
        CollisionShape shape = random.nextShape(shapeName);
        float randomMass = random.nextInt(2);
        addRigidBody(shape, randomMass);
    }

    /**
     * Split all rigid bodies in the PhysicsSpace.
     */
    private void splitAll() {
        Vector3f world3 = cam.getLocation(); // alias
        Triangle triangle = new Triangle(world1, world2, world3);

        Collection<PhysicsCollisionObject> allPcos = physicsSpace.getPcoList();
        for (PhysicsCollisionObject pco : allPcos) {
            PhysicsRigidBody body = (PhysicsRigidBody) pco;
            splitBody(body, triangle);
        }
    }

    /**
     * Attempt to split the specified rigid body using the plane of the
     * specified triangle.
     *
     * @param oldBody (not null, added to the PhysicsSpace)
     * @param worldTriangle a triangle that defines the splitting plane (in
     * world coordinates, not null, unaffected)
     */
    private void splitBody(PhysicsRigidBody oldBody, Triangle worldTriangle) {
        CollisionShape originalShape = oldBody.getCollisionShape();
        CollisionShape splittableShape = originalShape.toSplittableShape();
        assert splittableShape.canSplit();
        if (splittableShape instanceof EmptyShape) {
            return; // Splitting an empty shape has no effect.
        }

        // Transform the triangle to the shape coordinate system.
        Transform shapeToWorld = oldBody.getTransform(null);
        if (splittableShape instanceof CompoundCollisionShape) {
            shapeToWorld.setScale(1f);
        } else {
            splittableShape.getScale(shapeToWorld.getScale());
        }
        Triangle shapeTriangle
                = MyMath.transformInverse(shapeToWorld, worldTriangle, null);

        CollisionShape[] shapes;
        float[] volumes = new float[2];
        Vector3f[] locations = new Vector3f[2];
        Vector3f worldNormal = worldTriangle.getNormal(); // alias

        if (splittableShape instanceof HullCollisionShape) {
            HullCollisionShape hullShape = (HullCollisionShape) splittableShape;
            ChildCollisionShape[] children = hullShape.split(shapeTriangle);
            assert children.length == 2 : children.length;
            if (children[0] == null || children[1] == null) {
                return; // The split plane didn't intersect the hull.
            }

            shapes = new CollisionShape[2];
            for (int i = 0; i < 2; ++i) {
                shapes[i] = children[i].getShape();
                volumes[i] = shapes[i].scaledVolume();

                locations[i] = children[i].copyOffset(null);
                MyMath.transform(shapeToWorld, locations[i], locations[i]);
            }

        } else if (splittableShape instanceof CompoundCollisionShape) {
            CompoundCollisionShape compound
                    = (CompoundCollisionShape) splittableShape;
            shapes = compound.split(shapeTriangle);
            assert shapes.length == 2 : shapes.length;
            if (shapes[0] == null || shapes[1] == null) {
                return; // The split plane didn't intersect the compound shape.
            }
            // TODO deal with disconnected fragments, if any

            for (int i = 0; i < 2; ++i) {
                volumes[i] = shapes[i].scaledVolume();
                /*
                 * Translate each compound so its AABB is centered at (0,0,0)
                 * in its shape coordinates.
                 */
                locations[i] = shapes[i].aabbCenter(null);
                Vector3f offset = locations[i].negate();
                ((CompoundCollisionShape) shapes[i]).translate(offset);
                shapeToWorld.setScale(1f);
                MyMath.transform(shapeToWorld, locations[i], locations[i]);
            }

        } else if (splittableShape instanceof GImpactCollisionShape) {
            GImpactCollisionShape gi = (GImpactCollisionShape) splittableShape;
            ChildCollisionShape[] children = gi.split(shapeTriangle);
            assert children.length == 2 : children.length;
            if (children[0] == null || children[1] == null) {
                return; // The split plane didn't intersect the GImpact shape.
            }

            shapes = new CollisionShape[2];
            for (int i = 0; i < 2; ++i) {
                shapes[i] = children[i].getShape();
                volumes[i] = 1f; // TODO calculate area

                locations[i] = children[i].copyOffset(null);
                MyMath.transform(shapeToWorld, locations[i], locations[i]);
            }

        } else if (splittableShape instanceof MeshCollisionShape) {
            assert oldBody.isStatic();
            MeshCollisionShape mesh = (MeshCollisionShape) splittableShape;
            shapes = mesh.split(shapeTriangle);
            assert shapes.length == 2 : shapes.length;
            if (shapes[0] == null || shapes[1] == null) {
                return; // The split plane didn't intersect the mesh shape.
            }

            for (int i = 0; i < 2; ++i) {
                volumes[i] = 0f; // unused
                locations[i] = oldBody.getPhysicsLocation(null);
            }

        } else { // TODO handle simplex n<=2
            logger.log(Level.WARNING, "Shape not split:  {0}", originalShape);
            return;
        }

        splitBody(oldBody, worldNormal, shapes, volumes, locations);
    }

    /**
     * Split the specified rigid body into 2 using the specified shapes.
     *
     * @param oldBody (not null, added to the PhysicsSpace)
     * @param worldNormal the normal of the splitting plane (in world
     * coordinates, not null, unaffected)
     * @param shapes the shapes to use (length=2, both not null)
     * @param volumes the estimated volumes of the shapes (length=2, both &gt;0)
     * @param locations the center locations (in physics-space coordinates,
     * length=2, both not null)
     */
    private void splitBody(PhysicsRigidBody oldBody, Vector3f worldNormal,
            CollisionShape[] shapes, float[] volumes, Vector3f[] locations) {
        assert shapes.length == 2 : shapes.length;
        assert volumes.length == 2 : volumes.length;
        assert locations.length == 2 : locations.length;

        // Tweak the locations to create some separation.
        boolean isDynamic = oldBody.isDynamic();
        float deltaX = isDynamic ? 0.04f : 0.1f;
        MyVector3f.accumulateScaled(locations[0], worldNormal, -deltaX);
        MyVector3f.accumulateScaled(locations[1], worldNormal, +deltaX);

        float[] masses = new float[2];
        Vector3f w;
        Vector3f[] velocities = new Vector3f[2];
        if (isDynamic) {
            velocities[0] = oldBody.getLinearVelocity(null);
            velocities[1] = velocities[0].clone();

            // Tweak the linear velocities to enhance the separation.
            float deltaV = 0.04f;
            MyVector3f.accumulateScaled(velocities[0], worldNormal, -deltaV);
            MyVector3f.accumulateScaled(velocities[1], worldNormal, +deltaV);

            float totalVolume = volumes[0] + volumes[1];
            assert totalVolume > 0f : totalVolume;
            float totalMass = oldBody.getMass();
            for (int i = 0; i < 2; ++i) {
                masses[i] = totalMass * volumes[i] / totalVolume;
                assert masses[i] > 0f : masses[i];
            }

            w = oldBody.getAngularVelocity(null);

        } else {
            masses[0] = PhysicsBody.massForStatic;
            masses[1] = PhysicsBody.massForStatic;
            w = null;
        }
        Quaternion orientation = oldBody.getPhysicsRotation(null);
        physicsSpace.removeCollisionObject(oldBody);

        for (int i = 0; i < 2; ++i) {
            PhysicsRigidBody body = new PhysicsRigidBody(shapes[i], masses[i]);
            body.setPhysicsLocation(locations[i]);
            body.setPhysicsRotation(orientation);
            if (isDynamic) {
                body.setAngularVelocity(w);
                body.setLinearVelocity(velocities[i]);
            }
            physicsSpace.addCollisionObject(body);
            visualizeShape(body);
        }
    }

    private void togglePause() {
        physicsSpeed = (physicsSpeed <= PAUSED_SPEED) ? 1f : PAUSED_SPEED;
    }

    private void updateSplitter(float tpf) {
        splitAngle += tpf * (signalCcw - signalCw);
        splitAngle = MyMath.modulo(splitAngle, FastMath.PI);

        float ar = aspectRatio();
        if (Math.abs(splitAngle - FastMath.HALF_PI) < FastMath.QUARTER_PI) {
            // The plane is more vertical than horizontal.
            float cotangent = FastMath.tan(FastMath.HALF_PI - splitAngle);
            clip1.x = cotangent / ar;
            clip1.y = +1f;
        } else { // The plane is more horizontal than vertical.
            float tangent = FastMath.tan(splitAngle);
            clip1.x = +1f;
            clip1.y = ar * tangent;
        }
        clip1.negate(clip2);
        cam.clipToWorld(clip1, 0.1f, world1);
        cam.clipToWorld(clip2, 0.1f, world2);

        LineMesh lineMesh = new LineMesh(world1, world2);
        splitterGeometry.setMesh(lineMesh);

        super.render();
    }
}
