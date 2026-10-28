package com.internshala.hms.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window. Hosts a tabbed pane with one tab per feature
 * area: Hotels, Rooms, Guests and Reservations, matching the project's
 * required feature list.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Hotel Reservation System - Hospitality Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hotels", new HotelPanel());
        tabbedPane.addTab("Rooms", new RoomPanel());
        tabbedPane.addTab("Guests", new GuestPanel());
        tabbedPane.addTab("Reservations", new ReservationPanel());

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }
}
