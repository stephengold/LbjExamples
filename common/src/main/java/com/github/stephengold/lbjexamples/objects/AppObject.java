package com.github.stephengold.lbjexamples.objects;

import com.github.stephengold.lbjexamples.BasePhysicsApp;
import com.github.stephengold.lbjexamples.Utils;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class AppObject {

    private Mesh mesh;
    private PhysicsRigidBody rigidBody;
    private Vector3f position = new Vector3f();
    private Vector3f rotation = new Vector3f();
    private Vector3f scale = new Vector3f(1, 1, 1);
    private Vector4f color = new Vector4f(1);

    public AppObject(Mesh mesh) {
        this.mesh = mesh;
        BasePhysicsApp.APP_OBJECTS.add(this);
    }

    public AppObject(PhysicsRigidBody rigidBody, Mesh mesh) {
        this(mesh);
        this.rigidBody = rigidBody;
    }

    public AppObject(PhysicsRigidBody rigidBody) {
        this(rigidBody, new Mesh(rigidBody.getCollisionShape()));
    }

    public AppObject(float[] positions, int drawMode) {
        this(new Mesh(positions, drawMode));
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public PhysicsRigidBody getRigidBody() {
        return rigidBody;
    }

    public void setRigidBody(PhysicsRigidBody rigidBody) {
        this.rigidBody = rigidBody;
    }

    public void syncWithPhysics() {
        rigidBody.setPhysicsLocation(position);
        //rigidBody.setPhysicsRotation(rotation);
    }

    public void syncWithRender() {
        if (rigidBody == null) {
            return;
        }
        position = rigidBody.getMotionState().getLocation(null);
        Quaternion physicsRotation = rigidBody.getMotionState().getOrientation(new Quaternion());
        Quaternionf quat = new Quaternionf(physicsRotation.getX(), physicsRotation.getY(), physicsRotation.getZ(), physicsRotation.getW());
        rotation = Utils.toLibjmeVector(quat.getEulerAnglesXYZ(new org.joml.Vector3f()));
    }


    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f color) {
        this.color = color;
    }

    public void setColor(Vector4fc color) {
        this.color = new Vector4f(color);
    }

    public void removeFromWorld() {
        if (rigidBody != null) {
            rigidBody.getCollisionSpace().remove(rigidBody);
        }
    }

    public void destroy(){
        BasePhysicsApp.APP_OBJECTS.remove(this);
        mesh.cleanUp();
    }

    public Matrix4f getTransformMatrix() {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity().translate(Utils.toLwjglVector(position)).
                rotateX(rotation.x).
                rotateY(rotation.y).
                rotateZ(rotation.z).
                scale(Utils.toLwjglVector(scale));
        return modelMatrix;
    }
}
