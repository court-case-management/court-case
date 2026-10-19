package ui;

import dao.PartyDAO;
import model.Party;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * PartiesPanel - UI Tab for managing Litigants and Parties.
 */
public class PartiesPanel extends JPanel {

    private final PartyDAO partyDAO = new PartyDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtContact;

    public PartiesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Litigants & Parties List"));

        String[] columns = {"Party ID", "Party Name", "Contact No"};
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
        mainPanel.setBorder(BorderFactory.createTitledBorder("Manage Party (PreparedStatement)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 6, 8, 8));

        fieldsPanel.add(new JLabel("Party ID (Auto):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        fieldsPanel.add(txtId);

        fieldsPanel.add(new JLabel("Party Name:"));
        txtName = new JTextField();
        fieldsPanel.add(txtName);

        fieldsPanel.add(new JLabel("Contact No:"));
        txtContact = new JTextField();
        fieldsPanel.add(txtContact);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnAdd = new JButton("Insert Party");
        btnAdd.addActionListener(e -> insertRecord());

        JButton btnUpdate = new JButton("Update Party");
        btnUpdate.addActionListener(e -> updateRecord());

        JButton btnDelete = new JButton("Delete Party");
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
            List<Party> list = partyDAO.getAllParties();
            for (Party p : list) {
                tableModel.addRow(new Object[]{
                    p.getPartyId(), p.getPartyName(), p.getContactNo()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertRecord() {
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Party Name is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            partyDAO.insertParty(new Party(0, name, contact));
            JOptionPane.showMessageDialog(this, "Party added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a party to update!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();

            partyDAO.updateParty(new Party(id, name, contact));
            JOptionPane.showMessageDialog(this, "Party updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a party to delete!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete Party ID " + idStr + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                partyDAO.deleteParty(Integer.parseInt(idStr));
                JOptionPane.showMessageDialog(this, "Party deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
            txtContact.setText(tableModel.getValueAt(selectedRow, 2) != null ? tableModel.getValueAt(selectedRow, 2).toString() : "");
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtContact.setText("");
        table.clearSelection();
    }
}
