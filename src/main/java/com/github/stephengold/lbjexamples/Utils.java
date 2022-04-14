package com.github.stephengold.lbjexamples;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.nio.FloatBuffer;

public class Utils {

    public static float[] toArray(FloatBuffer buffer) {
        float[] array = new float[buffer.limit()];
        for (int i = 0; i < buffer.limit(); i++) {
            array[i] = buffer.get(i);
        }
        return array;
    }

    public static Vector3f toLibjmeVector(org.joml.Vector3f vector3f) {
        return new Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    public static org.joml.Vector3f toLwjglVector(Vector3f vector3f) {
        return new org.joml.Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    public static Quaternionf toLwjglQuat(Quaternion quat) {
        return new Quaternionf(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }
}
