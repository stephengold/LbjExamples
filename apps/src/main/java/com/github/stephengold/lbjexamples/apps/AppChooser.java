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

        List<BaseApplication> apps = new ArrayList<>();
        apps.add(new HelloKinematics());
        apps.add(new HelloRigidBody());
        apps.add(new HelloStaticBody());
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
