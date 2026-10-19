package ui;

import dao.CourtDAO;
import dao.JudgeDAO;
import model.Court;
import model.Judge;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * JudgesPanel - UI Tab for managing Judges.
 */
public class JudgesPanel extends JPanel {

    private final JudgeDAO judgeDAO = new JudgeDAO();
    private final CourtDAO courtDAO = new CourtDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtDesignation;
    private JComboBox<CourtItem> comboCourt;

    public JudgesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);

        refreshCourtsCombo();
        loadData();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Judges List"));

        String[] columns = {"Judge ID", "Judge Name", "Designation", "Court ID"};
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
        mainPanel.setBorder(BorderFactory.createTitledBorder("Manage Judge (PreparedStatement)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 4, 8, 8));

        fieldsPanel.add(new JLabel("Judge ID (Auto):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        fieldsPanel.add(txtId);

        fieldsPanel.add(new JLabel("Judge Name:"));
        txtName = new JTextField();
        fieldsPanel.add(txtName);

        fieldsPanel.add(new JLabel("Designation:"));
        txtDesignation = new JTextField();
        fieldsPanel.add(txtDesignation);

        fieldsPanel.add(new JLabel("Assigned Court:"));
        comboCourt = new JComboBox<>();
        fieldsPanel.add(comboCourt);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnAdd = new JButton("Insert Judge");
        btnAdd.addActionListener(e -> insertRecord());

        JButton btnUpdate = new JButton("Update Judge");
        btnUpdate.addActionListener(e -> updateRecord());

        JButton btnDelete = new JButton("Delete Judge");
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

    private void refreshCourtsCombo() {
        comboCourt.removeAllItems();
        comboCourt.addItem(new CourtItem(0, "-- None --"));
        try {
            List<Court> courts = courtDAO.getAllCourts();
            for (Court c : courts) {
                comboCourt.addItem(new CourtItem(c.getCourtId(), c.getCourtName()));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Judge> judges = judgeDAO.getAllJudges();
            for (Judge j : judges) {
                tableModel.addRow(new Object[]{
                    j.getJudgeId(), j.getJudgeName(), j.getDesignation(), j.getCourtId()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertRecord() {
        String name = txtName.getText().trim();
        String desig = txtDesignation.getText().trim();
        CourtItem item = (CourtItem) comboCourt.getSelectedItem();
        int courtId = item != null ? item.id : 0;

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judge Name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            judgeDAO.insertJudge(new Judge(0, name, desig, courtId));
            JOptionPane.showMessageDialog(this, "Judge added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a judge to update!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = txtName.getText().trim();
            String desig = txtDesignation.getText().trim();
            CourtItem item = (CourtItem) comboCourt.getSelectedItem();
            int courtId = item != null ? item.id : 0;

            judgeDAO.updateJudge(new Judge(id, name, desig, courtId));
            JOptionPane.showMessageDialog(this, "Judge updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a judge to delete!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete Judge ID " + idStr + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                judgeDAO.deleteJudge(Integer.parseInt(idStr));
                JOptionPane.showMessageDialog(this, "Judge deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
            txtDesignation.setText(tableModel.getValueAt(selectedRow, 2) != null ? tableModel.getValueAt(selectedRow, 2).toString() : "");

            Object cObj = tableModel.getValueAt(selectedRow, 3);
            if (cObj != null) {
                int cId = Integer.parseInt(cObj.toString());
                for (int i = 0; i < comboCourt.getItemCount(); i++) {
                    if (comboCourt.getItemAt(i).id == cId) {
                        comboCourt.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtDesignation.setText("");
        if (comboCourt.getItemCount() > 0) comboCourt.setSelectedIndex(0);
        table.clearSelection();
    }

    private static class CourtItem {
        int id;
        String name;
        CourtItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + (id > 0 ? " (" + id + ")" : ""); }
    }
}
