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

import com.github.stephengold.lbjexamples.apps.HelloCcd;
import com.github.stephengold.lbjexamples.apps.HelloCharacter;
import com.github.stephengold.lbjexamples.apps.HelloCloth;
import com.github.stephengold.lbjexamples.apps.HelloContactResponse;
import com.github.stephengold.lbjexamples.apps.HelloDamping;
import com.github.stephengold.lbjexamples.apps.HelloDeactivation;
import com.github.stephengold.lbjexamples.apps.HelloDoubleEnded;
import com.github.stephengold.lbjexamples.apps.HelloGhost;
import com.github.stephengold.lbjexamples.apps.HelloJoint;
import com.github.stephengold.lbjexamples.apps.HelloKinematics;
import com.github.stephengold.lbjexamples.apps.HelloLimit;
import com.github.stephengold.lbjexamples.apps.HelloMadMallet;
import com.github.stephengold.lbjexamples.apps.HelloMassDistribution;
import com.github.stephengold.lbjexamples.apps.HelloMotor;
import com.github.stephengold.lbjexamples.apps.HelloNonUniformGravity;
import com.github.stephengold.lbjexamples.apps.HelloPin;
import com.github.stephengold.lbjexamples.apps.HelloRigidBody;
import com.github.stephengold.lbjexamples.apps.HelloServo;
import com.github.stephengold.lbjexamples.apps.HelloSoftBody;
import com.github.stephengold.lbjexamples.apps.HelloSoftRope;
import com.github.stephengold.lbjexamples.apps.HelloSoftSoft;
import com.github.stephengold.lbjexamples.apps.HelloSport;
import com.github.stephengold.lbjexamples.apps.HelloSpring;
import com.github.stephengold.lbjexamples.apps.HelloStaticBody;
import com.github.stephengold.lbjexamples.apps.HelloVehicle;
import com.github.stephengold.lbjexamples.apps.HelloWalk;
import com.github.stephengold.lbjexamples.apps.HelloWind;
import com.github.stephengold.sport.BaseApplication;
import com.github.stephengold.sport.test.CheckerboardTest;
import com.github.stephengold.sport.test.ClipspaceTest;
import com.github.stephengold.sport.test.DynamicMeshTest;
import com.github.stephengold.sport.test.IcosphereTest;
import com.github.stephengold.sport.test.MouseTest;
import com.github.stephengold.sport.test.MouseTest2;
import com.github.stephengold.sport.test.OctasphereTest;
import com.github.stephengold.sport.test.RainbowTest;
import com.github.stephengold.sport.test.SpriteTest;
import com.github.stephengold.sport.test.TextureTest;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Choose an LbjExamples application to run.
 */
final class AppChooser extends JFrame {

    /**
     * Main entry point for the AppChooser application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        List<BaseApplication> apps = new ArrayList<>(43);

        apps.add(new CheckerboardTest());
        apps.add(new ClipspaceTest());
        apps.add(new ConveyorDemo());
        apps.add(new DynamicMeshTest());
        apps.add(new HelloCcd());

        apps.add(new HelloCharacter());
        apps.add(new HelloCloth());
        apps.add(new HelloContactResponse());
        apps.add(new HelloDamping());
        apps.add(new HelloDeactivation());

        apps.add(new HelloDoubleEnded());
        apps.add(new HelloGhost());
        apps.add(new HelloJoint());
        apps.add(new HelloKinematics());
        apps.add(new HelloLimit());

        apps.add(new HelloMadMallet());
        apps.add(new HelloMassDistribution());
        apps.add(new HelloMotor());
        apps.add(new HelloNonUniformGravity());
        apps.add(new HelloPin());

        apps.add(new HelloRigidBody());
        apps.add(new HelloServo());
        apps.add(new HelloSoftBody());
        apps.add(new HelloSoftRope());
        apps.add(new HelloSoftSoft());

        apps.add(new HelloSport());
        apps.add(new HelloSpring());
        apps.add(new HelloStaticBody());
        apps.add(new HelloVehicle());
        apps.add(new HelloWalk());

        apps.add(new HelloWind());
        apps.add(new IcosphereTest());
        apps.add(new MouseTest());
        apps.add(new MouseTest2());
        apps.add(new NewtonsCradle());

        apps.add(new OctasphereTest());
        apps.add(new Pachinko());
        apps.add(new RainbowTest());
        apps.add(new SpriteTest());
        apps.add(new TestGearJoint());

        apps.add(new TextureTest());
        apps.add(new ThousandCubes());
        apps.add(new Windlass());

        new AppChooser(apps);
    }

    private AppChooser(List<BaseApplication> apps) {
        setTitle("LbjExamples AppChooser");
        setSize(500, 100);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        Container contentPane = getContentPane();
        /*
         * Add a ComboBox to select one app.
         */
        JComboBox<String> comboBox = new JComboBox<>();
        for (BaseApplication app : apps) {
            String appName = app.getClass().getSimpleName();
            comboBox.addItem(appName);
        }
        contentPane.add(BorderLayout.CENTER, comboBox);
        /*
         * Add a JButton to start the selected app.
         */
        JButton startButton = new JButton("Start the selected app");
        startButton.addActionListener(actionEvent -> {
            setVisible(false);
            int selectedIndex = comboBox.getSelectedIndex();
            apps.get(selectedIndex).start();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        contentPane.add(BorderLayout.EAST, startButton);

        setVisible(true);
    }
}
