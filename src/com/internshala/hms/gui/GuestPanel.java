package com.internshala.hms.gui;

import com.internshala.hms.dao.GuestDAO;
import com.internshala.hms.model.Guest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel providing Add / Edit / Delete / View functionality for Guests.
 */
public class GuestPanel extends JPanel {

    private final GuestDAO guestDAO = new GuestDAO();

    private final JTextField idField = new JTextField(5);
    private final JTextField nameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(12);

    private final DefaultTableModel tableModel;
    private final JTable table;

    public GuestPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Guest Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField.setEditable(false);

        addRow(formPanel, gbc, 0, "Guest ID:", idField);
        addRow(formPanel, gbc, 1, "Name:", nameField);
        addRow(formPanel, gbc, 2, "Email:", emailField);
        addRow(formPanel, gbc, 3, "Phone:", phoneField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Guest");
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

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone"}, 0) {
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

        addBtn.addActionListener(e -> addGuest());
        updateBtn.addActionListener(e -> updateGuest());
        deleteBtn.addActionListener(e -> deleteGuest());
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

    private void addGuest() {
        if (!validateFields()) return;
        Guest guest = new Guest(nameField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim());
        if (guestDAO.addGuest(guest)) {
            JOptionPane.showMessageDialog(this, "Guest added successfully. ID: " + guest.getGuestId());
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add guest. Check DB connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGuest() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a guest from the table first.");
            return;
        }
        if (!validateFields()) return;
        Guest guest = new Guest(Integer.parseInt(idField.getText()), nameField.getText().trim(),
                emailField.getText().trim(), phoneField.getText().trim());
        if (guestDAO.updateGuest(guest)) {
            JOptionPane.showMessageDialog(this, "Guest updated successfully.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update guest.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteGuest() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a guest from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this guest?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (guestDAO.deleteGuest(Integer.parseInt(idField.getText()))) {
                JOptionPane.showMessageDialog(this, "Guest deleted.");
                clearForm();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete guest (they may have reservations linked to them).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        idField.setText(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        emailField.setText(tableModel.getValueAt(row, 2).toString());
        phoneField.setText(tableModel.getValueAt(row, 3).toString());
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        table.clearSelection();
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Guest> guests = guestDAO.getAllGuests();
        for (Guest g : guests) {
            tableModel.addRow(new Object[]{g.getGuestId(), g.getName(), g.getEmail(), g.getPhone()});
        }
    }
}
