package com.internshala.hms.gui;

import com.internshala.hms.dao.HotelDAO;
import com.internshala.hms.model.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel providing Add / Edit / Delete / View functionality for Hotels.
 */
public class HotelPanel extends JPanel {

    private final HotelDAO hotelDAO = new HotelDAO();

    private final JTextField idField = new JTextField(5);
    private final JTextField nameField = new JTextField(15);
    private final JTextField locationField = new JTextField(15);
    private final JTextField amenitiesField = new JTextField(20);

    private final DefaultTableModel tableModel;
    private final JTable table;

    public HotelPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- form ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Hotel Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField.setEditable(false);

        addRow(formPanel, gbc, 0, "Hotel ID:", idField);
        addRow(formPanel, gbc, 1, "Name:", nameField);
        addRow(formPanel, gbc, 2, "Location:", locationField);
        addRow(formPanel, gbc, 3, "Amenities:", amenitiesField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Hotel");
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

        // ---- table ----
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Location", "Amenities"}, 0) {
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

        addBtn.addActionListener(e -> addHotel());
        updateBtn.addActionListener(e -> updateHotel());
        deleteBtn.addActionListener(e -> deleteHotel());
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

    private void addHotel() {
        if (!validateFields()) return;
        Hotel hotel = new Hotel(nameField.getText().trim(), locationField.getText().trim(), amenitiesField.getText().trim());
        if (hotelDAO.addHotel(hotel)) {
            JOptionPane.showMessageDialog(this, "Hotel added successfully. ID: " + hotel.getHotelId());
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add hotel. Check DB connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateHotel() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a hotel from the table first.");
            return;
        }
        if (!validateFields()) return;
        Hotel hotel = new Hotel(Integer.parseInt(idField.getText()), nameField.getText().trim(),
                locationField.getText().trim(), amenitiesField.getText().trim());
        if (hotelDAO.updateHotel(hotel)) {
            JOptionPane.showMessageDialog(this, "Hotel updated successfully.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update hotel.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteHotel() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a hotel from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this hotel?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (hotelDAO.deleteHotel(Integer.parseInt(idField.getText()))) {
                JOptionPane.showMessageDialog(this, "Hotel deleted.");
                clearForm();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete hotel (it may have rooms linked to it).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        idField.setText(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        locationField.setText(tableModel.getValueAt(row, 2).toString());
        amenitiesField.setText(tableModel.getValueAt(row, 3).toString());
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        locationField.setText("");
        amenitiesField.setText("");
        table.clearSelection();
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Location are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Hotel> hotels = hotelDAO.getAllHotels();
        for (Hotel h : hotels) {
            tableModel.addRow(new Object[]{h.getHotelId(), h.getName(), h.getLocation(), h.getAmenities()});
        }
    }
}
