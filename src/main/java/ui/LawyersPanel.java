package ui;

import dao.LawyerDAO;
import model.Lawyer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * LawyersPanel - UI Tab for managing Advocates and Lawyers.
 */
public class LawyersPanel extends JPanel {

    private final LawyerDAO lawyerDAO = new LawyerDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtBarRegNo;
    private JTextField txtSpecialization;

    public LawyersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Advocates & Lawyers List"));

        String[] columns = {"Lawyer ID", "Lawyer Name", "Bar Reg No", "Specialization"};
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
        mainPanel.setBorder(BorderFactory.createTitledBorder("Manage Lawyer (PreparedStatement)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 4, 8, 8));

        fieldsPanel.add(new JLabel("Lawyer ID (Auto):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        fieldsPanel.add(txtId);

        fieldsPanel.add(new JLabel("Lawyer Name:"));
        txtName = new JTextField();
        fieldsPanel.add(txtName);

        fieldsPanel.add(new JLabel("Bar Reg No (Unique):"));
        txtBarRegNo = new JTextField();
        fieldsPanel.add(txtBarRegNo);

        fieldsPanel.add(new JLabel("Specialization:"));
        txtSpecialization = new JTextField();
        fieldsPanel.add(txtSpecialization);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnAdd = new JButton("Insert Lawyer");
        btnAdd.addActionListener(e -> insertRecord());

        JButton btnUpdate = new JButton("Update Lawyer");
        btnUpdate.addActionListener(e -> updateRecord());

        JButton btnDelete = new JButton("Delete Lawyer");
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
            List<Lawyer> list = lawyerDAO.getAllLawyers();
            for (Lawyer l : list) {
                tableModel.addRow(new Object[]{
                    l.getLawyerId(), l.getLawyerName(), l.getBarRegNo(), l.getSpecialization()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertRecord() {
        String name = txtName.getText().trim();
        String barNo = txtBarRegNo.getText().trim();
        String spec = txtSpecialization.getText().trim();

        if (name.isEmpty() || barNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lawyer Name and Bar Reg No are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            lawyerDAO.insertLawyer(new Lawyer(0, name, barNo, spec));
            JOptionPane.showMessageDialog(this, "Lawyer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a lawyer to update!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = txtName.getText().trim();
            String barNo = txtBarRegNo.getText().trim();
            String spec = txtSpecialization.getText().trim();

            lawyerDAO.updateLawyer(new Lawyer(id, name, barNo, spec));
            JOptionPane.showMessageDialog(this, "Lawyer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a lawyer to delete!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete Lawyer ID " + idStr + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                lawyerDAO.deleteLawyer(Integer.parseInt(idStr));
                JOptionPane.showMessageDialog(this, "Lawyer deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
            txtBarRegNo.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtSpecialization.setText(tableModel.getValueAt(selectedRow, 3) != null ? tableModel.getValueAt(selectedRow, 3).toString() : "");
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtBarRegNo.setText("");
        txtSpecialization.setText("");
        table.clearSelection();
    }
}
