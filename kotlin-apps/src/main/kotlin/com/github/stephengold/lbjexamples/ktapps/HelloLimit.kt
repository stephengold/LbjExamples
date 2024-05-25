/*
 Copyright (c) 2024 Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.lbjexamples.ktapps

import com.github.stephengold.sport.Constants
import com.github.stephengold.sport.Projection
import com.github.stephengold.sport.input.RotateMode
import com.github.stephengold.sport.physics.BasePhysicsApp
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.RotationOrder
import com.jme3.bullet.collision.shapes.Box2dShape
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.joints.New6Dof
import com.jme3.bullet.joints.motors.MotorParam
import com.jme3.bullet.objects.PhysicsBody
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.FastMath
import com.jme3.math.Matrix3f
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import jme3utilities.math.MyVector3f

/*
 * A simple example of a PhysicsJoint with limits.
 *
 * Builds upon HelloJoint.
 *
 * author:  Stephen Gold sgold@sonic.net
 */

/*
 * Main entry point for the HelloLimit application.
 */
fun main() {
    val application = HelloLimit()
    application.start()
}

/*
 * physics-space Y coordinate of the ground plane
 */
private val groundY = -2f
/*
 * half the height of the paddle (in physics-space units)
 */
private val paddleHalfHeight = 1f
/*
 * mouse-controlled kinematic paddle
 */
private var paddleBody: PhysicsRigidBody? = null
/*
 * latest ground location indicated by the mouse cursor
 */
private val mouseLocation = Vector3f()

class HelloLimit : BasePhysicsApp<PhysicsSpace>(), PhysicsTickListener {
    // *************************************************************************
    // BasePhysicsApp override functions

    /*
     * Create the PhysicsSpace. Invoked once during initialization.
     */
    override fun createSpace(): PhysicsSpace {
        val result = PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT)

        // To enable the callbacks, register the application as a tick listener.
        result.addTickListener(this)

        // Reduce the time step for better accuracy.
        result.setAccuracy(0.005f)

        return result
    }

    /*
     * Initialize the application. Invoked once.
     */
    override fun initialize() {
        super.initialize()

        configureCamera()
        configureLighting()

        // Disable VSync for more frequent mouse-position updates.
        setVsync(false)
    }

    /*
     * Populate the PhysicsSpace. Invoked once during initialization.
     */
    override fun populateSpace() {
        // Add a static, green square to represent the ground.
        val halfExtent = 3f
        val ground = addSquare(halfExtent, groundY)

        // Add a mouse-controlled kinematic paddle.
        addPaddle()

        // Add a dynamic ball.
        val ballBody = addBall()

        // Add a single-ended physics joint to constrain the ball's center.
        val pivotInBall = Vector3f(0f, 0f, 0f)
        val pivotInWorld = Vector3f(0f, 0f, 0f)
        val rotInBall = Matrix3f.IDENTITY
        val rotInPaddle = Matrix3f.IDENTITY
        val joint = New6Dof(ballBody, pivotInBall, pivotInWorld,
                rotInBall, rotInPaddle, RotationOrder.XYZ)
        physicsSpace.addJoint(joint)

        // Limit the X and Z translation DOFs.
        joint.set(MotorParam.LowerLimit, PhysicsSpace.AXIS_X, -halfExtent)
        joint.set(MotorParam.LowerLimit, PhysicsSpace.AXIS_Z, -halfExtent)
        joint.set(MotorParam.UpperLimit, PhysicsSpace.AXIS_X, +halfExtent)
        joint.set(MotorParam.UpperLimit, PhysicsSpace.AXIS_Z, +halfExtent)

        // Lock the Y translation at paddle height.
        val paddleY = groundY + paddleHalfHeight
        joint.set(MotorParam.LowerLimit, PhysicsSpace.AXIS_Y, paddleY)
        joint.set(MotorParam.UpperLimit, PhysicsSpace.AXIS_Y, paddleY)

        // Visualize the ground.
        visualizeShape(ground)
                .setColor(Constants.GREEN)
                .setSpecularColor(Constants.DARK_GRAY)
    }

    /*
     * Callback invoked during each iteration of the main update loop.
     */
    override fun render() {
        // Calculate the ground location (if any) indicated by the mouse cursor.
        val screenXy = getInputManager().locateCursor()
        if (screenXy != null) {
            val nearLocation
                    = cam.clipToWorld(screenXy, Projection.nearClipZ, null)
            val farLocation
                    = cam.clipToWorld(screenXy, Projection.farClipZ, null)
            if (nearLocation.y > groundY && farLocation.y < groundY) {
                val dy = nearLocation.y - farLocation.y
                val t = (nearLocation.y - groundY) / dy
                MyVector3f.lerp(t, nearLocation, farLocation, mouseLocation)
            }
        }
        super.render()
    }
    // *************************************************************************
    // PhysicsTickListener override methods

    /*
     * Callback from Bullet, invoked just before each simulation step.
     *
     * space:  the space that's about to be stepped (not null)
     * timeStep:  the duration of the simulation step (in seconds, &ge;0)
     */
    override fun prePhysicsTick(space: PhysicsSpace, timeStep: Float) {
        // Reposition the paddle based on the mouse location.
        val bodyLocation = mouseLocation.add(0f, paddleHalfHeight, 0f)
        paddleBody!!.setPhysicsLocation(bodyLocation)
    }

    /*
     * Callback from Bullet, invoked just after each simulation step.
     *
     * space:  the space that was just stepped (not null)
     * timeStep:  the duration of the simulation step (in seconds, >=0)
     */
    override fun physicsTick(space: PhysicsSpace, timeStep: Float) {
        // do nothing
    }
    // *************************************************************************
    // private functions

    /*
     * Create a dynamic rigid body with a sphere shape and add it to the space.
     */
    private fun addBall(): PhysicsRigidBody {
        val radius = 0.4f
        val shape = SphereCollisionShape(radius)

        val mass = 0.2f
        val result = PhysicsRigidBody(shape, mass)
        physicsSpace.addCollisionObject(result)

        // Apply angular damping to reduce the ball's tendency to spin.
        result.setAngularDamping(0.6f)

        // Disable sleep (deactivation).
        result.setEnableSleep(false)

        visualizeShape(result)

        return result
    }

    /*
     * Create a kinematic body with a box shape and add it to the space.
     */
    private fun addPaddle() {
        val shape = BoxCollisionShape(0.3f, paddleHalfHeight, 1f)
        paddleBody = PhysicsRigidBody(shape)
        paddleBody!!.setKinematic(true)

        physicsSpace.addCollisionObject(paddleBody)
        visualizeShape(paddleBody)
    }

    /*
     * Add a horizontal square body to the space.
     *
     * halfExtent:  half of the desired side length
     * y:  the desired elevation (in physics-space coordinates)
     */
    private fun addSquare(halfExtent: Float, y: Float): PhysicsRigidBody {
        // Construct a static rigid body with a square shape.
        val shape = Box2dShape(halfExtent)
        val result = PhysicsRigidBody(shape, PhysicsBody.massForStatic)

        physicsSpace.addCollisionObject(result)

        // Rotate it 90 degrees to a horizontal orientation.
        val rotate90 = Quaternion().fromAngles(-FastMath.HALF_PI, 0f, 0f)
        result.setPhysicsRotation(rotate90)

        // Translate it to the desired elevation.
        result.setPhysicsLocation(Vector3f(0f, y, 0f))

        return result
    }

    /*
     * Configure the Camera and CIP during startup.
     */
    private fun configureCamera() {
        getCameraInputProcessor().setRotationMode(RotateMode.None)

        cam.setLocation(Vector3f(0f, 5f, 10f))
        cam.setUpAngle(-0.6f)
        cam.setAzimuth(-1.6f)
    }

    /*
     * Configure lighting and the background color.
     */
    private fun configureLighting() {
        setLightDirection(7f, 3f, 5f)

        // Set the background color to light blue.
        setBackgroundColor(Constants.SKY_BLUE)
    }
}
