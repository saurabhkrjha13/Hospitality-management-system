package com.internshala.hms;

import com.internshala.hms.gui.MainFrame;

import javax.swing.*;

/**
 * Application entry point. Launches the Swing GUI on the Event Dispatch
 * Thread, as recommended for all Swing applications.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
