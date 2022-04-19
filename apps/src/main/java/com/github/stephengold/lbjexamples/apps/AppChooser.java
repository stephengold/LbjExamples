package com.github.stephengold.lbjexamples.apps;

import com.github.stephengold.lbjexamples.BaseApplication;
import org.lwjgl.system.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class AppChooser extends JFrame {

    private final JComboBox<String> appChooser = new JComboBox<>();
    public static List<BaseApplication> apps = new ArrayList<>();

    public static void main(String[] args) {
        if (System.getProperty("os.name").startsWith("Mac"))
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        apps.add(new HelloRigidBody());
        new AppChooser();
    }

    public AppChooser(){
        setTitle("LBJ Examples App Chooser");
        setSize(400, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        apps.forEach(baseApplication -> appChooser.addItem(baseApplication.getName()));

        JButton launchApp = new JButton("Start");
        launchApp.addActionListener(actionEvent -> {
            setVisible(false);
            apps.get(appChooser.getSelectedIndex()).run();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        getContentPane().add(BorderLayout.CENTER, appChooser);
        getContentPane().add(BorderLayout.SOUTH, launchApp);

        setVisible(true);
    }
}
