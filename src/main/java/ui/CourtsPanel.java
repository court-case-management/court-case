package ui;

import dao.CourtDAO;
import model.Court;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * CourtsPanel - UI Tab for managing Judicial Courts.
 * Simple CRUD panel using PreparedStatement.
 */
public class CourtsPanel extends JPanel {

    private final CourtDAO courtDAO = new CourtDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> comboLevel;
    private JTextField txtLocation;

    public CourtsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Center Table
        add(createTablePanel(), BorderLayout.CENTER);

        // South Form
        add(createFormPanel(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Judicial Courts List"));

        String[] columns = {"Court ID", "Court Name", "Court Level", "Location"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> populateForm());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Manage Court (PreparedStatement)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 4, 8, 8));

        fieldsPanel.add(new JLabel("Court ID (Auto):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        fieldsPanel.add(txtId);

        fieldsPanel.add(new JLabel("Court Name:"));
        txtName = new JTextField();
        fieldsPanel.add(txtName);

        fieldsPanel.add(new JLabel("Court Level:"));
        comboLevel = new JComboBox<>(new String[]{"District", "High", "Supreme"});
        fieldsPanel.add(comboLevel);

        fieldsPanel.add(new JLabel("Location:"));
        txtLocation = new JTextField();
        fieldsPanel.add(txtLocation);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnAdd = new JButton("Insert Court");
        btnAdd.addActionListener(e -> insertRecord());

        JButton btnUpdate = new JButton("Update Court");
        btnUpdate.addActionListener(e -> updateRecord());

        JButton btnDelete = new JButton("Delete Court");
        btnDelete.addActionListener(e -> deleteRecord());

        JButton btnClear = new JButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Court> courts = courtDAO.getAllCourts();
            for (Court c : courts) {
                tableModel.addRow(new Object[]{
                    c.getCourtId(), c.getCourtName(), c.getCourtLevel(), c.getLocation()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertRecord() {
        String name = txtName.getText().trim();
        String level = (String) comboLevel.getSelectedItem();
        String location = txtLocation.getText().trim();

        if (name.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Court Name and Location are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            courtDAO.insertCourt(new Court(0, name, level, location));
            JOptionPane.showMessageDialog(this, "Court added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a court to update!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = txtName.getText().trim();
            String level = (String) comboLevel.getSelectedItem();
            String location = txtLocation.getText().trim();

            courtDAO.updateCourt(new Court(id, name, level, location));
            JOptionPane.showMessageDialog(this, "Court updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a court to delete!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete Court ID " + idStr + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                courtDAO.deleteCourt(Integer.parseInt(idStr));
                JOptionPane.showMessageDialog(this, "Court deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            comboLevel.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            txtLocation.setText(tableModel.getValueAt(selectedRow, 3).toString());
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtLocation.setText("");
        comboLevel.setSelectedIndex(0);
        table.clearSelection();
    }
}
