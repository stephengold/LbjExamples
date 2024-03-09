/*
 Copyright (c) 2022-2023, Stephen Gold and Yanis Boudiaf

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
package com.github.stephengold.sport.physics;

import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.Geometry;
import com.github.stephengold.sport.mesh.WheelMesh;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;

/**
 * Visualize one of the wheels of a vehicle.
 */
public class WheelGeometry extends Geometry {
    // *************************************************************************
    // fields

    /**
     * index of the wheel to visualize (&ge;0)
     */
    final private int wheelIndex;
    /**
     * vehicle to visualize
     */
    final private PhysicsVehicle vehicle;
    /**
     * reusable mesh, allocated lazily
     */
    private static WheelMesh mesh;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Geometry to visualize the specified wheel of the specified
     * vehicle.
     *
     * @param vehicle the vehicle (not null, alias created)
     * @param wheelIndex which wheel (&ge;0)
     */
    public WheelGeometry(PhysicsVehicle vehicle, int wheelIndex) {
        super();
        Validate.nonNull(vehicle, "vehicle");
        Validate.nonNegative(wheelIndex, "wheel index");

        this.vehicle = vehicle;
        this.wheelIndex = wheelIndex;

        if (mesh == null) {
            mesh = new WheelMesh();
        }
        super.setMesh(mesh);

        super.setProgram("Unshaded/Monochrome");
        BaseApplication.makeVisible(this);
    }
    // *************************************************************************
    // Geometry methods

    /**
     * Update properties based on the PhysicsWheel and then render.
     */
    @Override
    public void updateAndRender() {
        updateTransform();
        super.updateAndRender();
    }

    /**
     * Test whether the PhysicsWheel has been removed from the specified
     * PhysicsSpace.
     *
     * @param space the space to test (not null, unaffected)
     * @return true if removed, otherwise false
     */
    @Override
    public boolean wasRemovedFrom(CollisionSpace space) {
        if (space.contains(vehicle)) {
            int numWheels = vehicle.getNumWheels();
            if (wheelIndex < numWheels) {
                return false;
            }
        }

        return true;
    }
    // *************************************************************************
    // private methods

    /**
     * Update the mesh-to-world transform.
     */
    private void updateTransform() {
        VehicleWheel vw = vehicle.getWheel(wheelIndex);
        vw.updatePhysicsState();

        Vector3f location = vw.getWheelWorldLocation(null);
        setLocation(location);

        Quaternion orientation = vw.getWheelWorldRotation(null);
        setOrientation(orientation);

        float radius = vw.getRadius();
        setScale(radius);
    }
}
