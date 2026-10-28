package com.internshala.hms.gui;

import com.internshala.hms.dao.ReservationDAO;
import com.internshala.hms.dao.RoomDAO;
import com.internshala.hms.model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel providing reservation creation, availability checking, and
 * cost calculation, plus a table listing all existing reservations.
 */
public class ReservationPanel extends JPanel {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JTextField idField = new JTextField(5);
    private final JTextField guestIdField = new JTextField(6);
    private final JTextField roomIdField = new JTextField(6);
    private final JTextField checkInField = new JTextField(10);
    private final JTextField checkOutField = new JTextField(10);
    private final JTextField costField = new JTextField(10);

    private final DefaultTableModel tableModel;
    private final JTable table;

    public ReservationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Reservation Details (dates as yyyy-MM-dd)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField.setEditable(false);
        costField.setEditable(false);

        addRow(formPanel, gbc, 0, "Reservation ID:", idField);
        addRow(formPanel, gbc, 1, "Guest ID:", guestIdField);
        addRow(formPanel, gbc, 2, "Room ID:", roomIdField);
        addRow(formPanel, gbc, 3, "Check-in Date:", checkInField);
        addRow(formPanel, gbc, 4, "Check-out Date:", checkOutField);
        addRow(formPanel, gbc, 5, "Total Cost:", costField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton checkBtn = new JButton("Check Availability");
        JButton calcBtn = new JButton("Calculate Cost");
        JButton addBtn = new JButton("Make Reservation");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        buttonPanel.add(checkBtn);
        buttonPanel.add(calcBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Guest ID", "Room ID", "Check-in", "Check-out", "Total Cost"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> loadSelectedRowIntoForm());
        JScrollPane scrollPane = new JScrollPane(table);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        checkBtn.addActionListener(e -> checkAvailability());
        calcBtn.addActionListener(e -> calculateCost());
        addBtn.addActionListener(e -> makeReservation());
        deleteBtn.addActionListener(e -> deleteReservation());
        clearBtn.addActionListener(e -> clearForm());

        refreshTable();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void checkAvailability() {
        Integer roomId = parseInt(roomIdField.getText());
        Date checkIn = parseDate(checkInField.getText());
        Date checkOut = parseDate(checkOutField.getText());
        if (roomId == null || checkIn == null || checkOut == null) {
            JOptionPane.showMessageDialog(this, "Enter a valid Room ID and dates (yyyy-MM-dd) first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean available = roomDAO.isRoomAvailable(roomId, checkIn, checkOut);
        JOptionPane.showMessageDialog(this, available
                ? "Room " + roomId + " is AVAILABLE for the selected dates."
                : "Room " + roomId + " is NOT available for the selected dates.");
    }

    private void calculateCost() {
        Integer roomId = parseInt(roomIdField.getText());
        Date checkIn = parseDate(checkInField.getText());
        Date checkOut = parseDate(checkOutField.getText());
        if (roomId == null || checkIn == null || checkOut == null) {
            JOptionPane.showMessageDialog(this, "Enter a valid Room ID and dates (yyyy-MM-dd) first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double cost = reservationDAO.calculateReservationCost(roomId, checkIn, checkOut);
        costField.setText(String.valueOf(cost));
    }

    private void makeReservation() {
        Integer guestId = parseInt(guestIdField.getText());
        Integer roomId = parseInt(roomIdField.getText());
        Date checkIn = parseDate(checkInField.getText());
        Date checkOut = parseDate(checkOutField.getText());

        if (guestId == null || roomId == null || checkIn == null || checkOut == null) {
            JOptionPane.showMessageDialog(this, "Guest ID, Room ID and valid dates (yyyy-MM-dd) are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!checkOut.after(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!roomDAO.isRoomAvailable(roomId, checkIn, checkOut)) {
            JOptionPane.showMessageDialog(this, "Room is not available for the selected dates.", "Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double cost = reservationDAO.calculateReservationCost(roomId, checkIn, checkOut);
        Reservation reservation = new Reservation(guestId, roomId, checkIn, checkOut, cost);

        if (reservationDAO.addReservation(reservation)) {
            JOptionPane.showMessageDialog(this, "Reservation created successfully. ID: " + reservation.getReservationId()
                    + "\nTotal Cost: " + cost);
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create reservation. Check Guest ID / Room ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReservation() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a reservation from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this reservation?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (reservationDAO.deleteReservation(Integer.parseInt(idField.getText()))) {
                JOptionPane.showMessageDialog(this, "Reservation deleted.");
                clearForm();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete reservation.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        idField.setText(tableModel.getValueAt(row, 0).toString());
        guestIdField.setText(tableModel.getValueAt(row, 1).toString());
        roomIdField.setText(tableModel.getValueAt(row, 2).toString());
        checkInField.setText(tableModel.getValueAt(row, 3).toString());
        checkOutField.setText(tableModel.getValueAt(row, 4).toString());
        costField.setText(tableModel.getValueAt(row, 5).toString());
    }

    private void clearForm() {
        idField.setText("");
        guestIdField.setText("");
        roomIdField.setText("");
        checkInField.setText("");
        checkOutField.setText("");
        costField.setText("");
        table.clearSelection();
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseDate(String s) {
        try {
            LocalDate localDate = LocalDate.parse(s.trim(), DATE_FORMAT);
            return Date.valueOf(localDate);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Reservation> reservations = reservationDAO.getAllReservations();
        for (Reservation r : reservations) {
            tableModel.addRow(new Object[]{r.getReservationId(), r.getGuestId(), r.getRoomId(),
                    r.getCheckInDate(), r.getCheckOutDate(), r.getTotalCost()});
        }
    }
}
