package com.internshala.hms.gui;

import com.internshala.hms.dao.RoomDAO;
import com.internshala.hms.model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel providing Add / Edit / Delete / View functionality for Rooms.
 */
public class RoomPanel extends JPanel {

    private final RoomDAO roomDAO = new RoomDAO();

    private final JTextField idField = new JTextField(5);
    private final JTextField hotelIdField = new JTextField(5);
    private final JTextField roomNumberField = new JTextField(10);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"SINGLE", "DOUBLE", "DELUXE", "SUITE"});
    private final JTextField priceField = new JTextField(10);
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED"});

    private final DefaultTableModel tableModel;
    private final JTable table;

    public RoomPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Room Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField.setEditable(false);

        addRow(formPanel, gbc, 0, "Room ID:", idField);
        addRow(formPanel, gbc, 1, "Hotel ID:", hotelIdField);
        addRow(formPanel, gbc, 2, "Room Number:", roomNumberField);
        addRow(formPanel, gbc, 3, "Type:", typeCombo);
        addRow(formPanel, gbc, 4, "Price/Night:", priceField);
        addRow(formPanel, gbc, 5, "Status:", statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Room");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Hotel ID", "Room No.", "Type", "Price", "Status"}, 0) {
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

        addBtn.addActionListener(e -> addRoom());
        updateBtn.addActionListener(e -> updateRoom());
        deleteBtn.addActionListener(e -> deleteRoom());
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

    private void addRoom() {
        Integer hotelId = parseInt(hotelIdField.getText());
        Double price = parseDouble(priceField.getText());
        if (hotelId == null || roomNumberField.getText().trim().isEmpty() || price == null) {
            JOptionPane.showMessageDialog(this, "Hotel ID, Room Number and Price are required (numeric fields must be valid).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Room room = new Room(hotelId, roomNumberField.getText().trim(), (String) typeCombo.getSelectedItem(),
                price, (String) statusCombo.getSelectedItem());
        if (roomDAO.addRoom(room)) {
            JOptionPane.showMessageDialog(this, "Room added successfully. ID: " + room.getRoomId());
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add room. Check that the Hotel ID exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoom() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room from the table first.");
            return;
        }
        Integer hotelId = parseInt(hotelIdField.getText());
        Double price = parseDouble(priceField.getText());
        if (hotelId == null || price == null) {
            JOptionPane.showMessageDialog(this, "Hotel ID and Price must be valid numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Room room = new Room(Integer.parseInt(idField.getText()), hotelId, roomNumberField.getText().trim(),
                (String) typeCombo.getSelectedItem(), price, (String) statusCombo.getSelectedItem());
        if (roomDAO.updateRoom(room)) {
            JOptionPane.showMessageDialog(this, "Room updated successfully.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update room.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoom() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this room?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (roomDAO.deleteRoom(Integer.parseInt(idField.getText()))) {
                JOptionPane.showMessageDialog(this, "Room deleted.");
                clearForm();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete room (it may have reservations linked to it).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        idField.setText(tableModel.getValueAt(row, 0).toString());
        hotelIdField.setText(tableModel.getValueAt(row, 1).toString());
        roomNumberField.setText(tableModel.getValueAt(row, 2).toString());
        typeCombo.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        priceField.setText(tableModel.getValueAt(row, 4).toString());
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 5).toString());
    }

    private void clearForm() {
        idField.setText("");
        hotelIdField.setText("");
        roomNumberField.setText("");
        typeCombo.setSelectedIndex(0);
        priceField.setText("");
        statusCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomDAO.getAllRooms();
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{r.getRoomId(), r.getHotelId(), r.getRoomNumber(), r.getType(), r.getPrice(), r.getStatus()});
        }
    }
}
