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
package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.lbjexamples.BaseApplication;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import org.lwjgl.system.Configuration;

/**
 * Choose an LbjExamples application to run.
 */
public class AppChooser extends JFrame {

    public static void main(String[] args) {
        if (System.getProperty("os.name").startsWith("Mac")) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        List<BaseApplication> apps = new ArrayList<>(13);
        apps.add(new HelloCcd());
        apps.add(new HelloCharacter());
        apps.add(new HelloContactResponse());
        apps.add(new HelloDamping());
        apps.add(new HelloDeactivation());
        apps.add(new HelloKinematics());
        apps.add(new HelloMadMallet());
        apps.add(new HelloMassDistribution());
        apps.add(new HelloNonUniformGravity());
        apps.add(new HelloRigidBody());
        apps.add(new HelloStaticBody());
        apps.add(new HelloVehicle());
        apps.add(new HelloWalk());
        apps.add(new NewtonsCradle());
        apps.add(new ThousandCubes());
        new AppChooser(apps);
    }

    private AppChooser(List<BaseApplication> apps) {
        setTitle("LbjExamples AppChooser");
        setSize(500, 60);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
