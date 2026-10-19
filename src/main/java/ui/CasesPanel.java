package ui;

import dao.CaseDAO;
import dao.CourtDAO;
import model.CaseModel;
import model.Court;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * CasesPanel - UI Tab for managing Court Cases.
 * Demonstrates PL/SQL procedure execution (add_case, update_case_status, delete_case)
 * and error handling with dialog popups.
 */
public class CasesPanel extends JPanel {

    private final CaseDAO caseDAO = new CaseDAO();
    private final CourtDAO courtDAO = new CourtDAO();

    private JTable casesTable;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField txtCaseNo;
    private JTextField txtCaseTitle;
    private JTextField txtFilingDate; // YYYY-MM-DD
    private JTextField txtCaseType;
    private JComboBox<String> comboStatus;
    private JComboBox<CourtItem> comboCourt;

    // Search fields
    private JTextField txtSearchTitle;
    private JComboBox<String> comboSearchStatus;
    private JComboBox<CourtItem> comboSearchCourt;

    public CasesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel: Search & Filter
        add(createSearchPanel(), BorderLayout.NORTH);

        // Center Panel: JTable listing cases
        add(createTablePanel(), BorderLayout.CENTER);

        // South Panel: Form fields and Action Buttons
        add(createFormPanel(), BorderLayout.SOUTH);

        // Initial Data Load
        refreshCourtsCombo();
        loadCasesData();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Search / Filter Cases"));

        panel.add(new JLabel("Title:"));
        txtSearchTitle = new JTextField(12);
        panel.add(txtSearchTitle);

        panel.add(new JLabel("Status:"));
        comboSearchStatus = new JComboBox<>(new String[]{"All", "Open", "Closed", "Under Appeal"});
        panel.add(comboSearchStatus);

        panel.add(new JLabel("Court:"));
        comboSearchCourt = new JComboBox<>();
        panel.add(comboSearchCourt);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> performSearch());
        panel.add(btnSearch);

        JButton btnRefresh = new JButton("Reset / Refresh");
        btnRefresh.addActionListener(e -> {
            txtSearchTitle.setText("");
            comboSearchStatus.setSelectedIndex(0);
            comboSearchCourt.setSelectedIndex(0);
            loadCasesData();
        });
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("All Court Cases"));

        String[] columns = {"Case No", "Case Title", "Filing Date", "Case Type", "Status", "Court ID", "Court Name"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        casesTable = new JTable(tableModel);
        casesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        casesTable.getSelectionModel().addListSelectionListener(e -> populateFormFromSelectedRow());

        panel.add(new JScrollPane(casesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Manage Case (PL/SQL Procedure Invocation)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 6, 8, 8));

        fieldsPanel.add(new JLabel("Case No (PK):"));
        txtCaseNo = new JTextField();
        fieldsPanel.add(txtCaseNo);

        fieldsPanel.add(new JLabel("Case Title:"));
        txtCaseTitle = new JTextField();
        fieldsPanel.add(txtCaseTitle);

        fieldsPanel.add(new JLabel("Filing Date (YYYY-MM-DD):"));
        txtFilingDate = new JTextField();
        fieldsPanel.add(txtFilingDate);

        fieldsPanel.add(new JLabel("Case Type:"));
        txtCaseType = new JTextField();
        fieldsPanel.add(txtCaseType);

        fieldsPanel.add(new JLabel("Status:"));
        comboStatus = new JComboBox<>(new String[]{"Open", "Closed", "Under Appeal"});
        fieldsPanel.add(comboStatus);

        fieldsPanel.add(new JLabel("Court:"));
        comboCourt = new JComboBox<>();
        fieldsPanel.add(comboCourt);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton btnInsert = new JButton("Insert Case (add_case)");
        btnInsert.setToolTipText("Calls PL/SQL Procedure: add_case");
        btnInsert.addActionListener(e -> insertCase());

        JButton btnUpdate = new JButton("Update Status (update_case_status)");
        btnUpdate.setToolTipText("Calls PL/SQL Procedure: update_case_status");
        btnUpdate.addActionListener(e -> updateStatus());

        JButton btnDelete = new JButton("Delete Case (delete_case)");
        btnDelete.setToolTipText("Calls PL/SQL Procedure: delete_case");
        btnDelete.addActionListener(e -> deleteCase());

        JButton btnClear = new JButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnInsert);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void refreshCourtsCombo() {
        comboCourt.removeAllItems();
        comboSearchCourt.removeAllItems();

        comboSearchCourt.addItem(new CourtItem(0, "All Courts"));
        comboCourt.addItem(new CourtItem(0, "-- Select Court --"));

        try {
            List<Court> courts = courtDAO.getAllCourts();
            for (Court c : courts) {
                CourtItem item = new CourtItem(c.getCourtId(), c.getCourtName());
                comboCourt.addItem(item);
                comboSearchCourt.addItem(item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courts list: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCasesData() {
        tableModel.setRowCount(0);
        try {
            List<CaseModel> cases = caseDAO.getAllCases();
            for (CaseModel c : cases) {
                tableModel.addRow(new Object[]{
                    c.getCaseNo(),
                    c.getCaseTitle(),
                    c.getFilingDate(),
                    c.getCaseType(),
                    c.getStatus(),
                    c.getCourtId(),
                    c.getCourtName()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading cases: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        tableModel.setRowCount(0);
        String title = txtSearchTitle.getText();
        String status = (String) comboSearchStatus.getSelectedItem();
        CourtItem courtItem = (CourtItem) comboSearchCourt.getSelectedItem();
        int courtId = courtItem != null ? courtItem.id : 0;

        try {
            List<CaseModel> cases = caseDAO.searchCases(title, status, courtId);
            for (CaseModel c : cases) {
                tableModel.addRow(new Object[]{
                    c.getCaseNo(),
                    c.getCaseTitle(),
                    c.getFilingDate(),
                    c.getCaseType(),
                    c.getStatus(),
                    c.getCourtId(),
                    c.getCourtName()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching cases: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertCase() {
        String caseNo = txtCaseNo.getText().trim();
        String title = txtCaseTitle.getText().trim();
        String filingDateStr = txtFilingDate.getText().trim();
        String caseType = txtCaseType.getText().trim();
        String status = (String) comboStatus.getSelectedItem();
        CourtItem selectedCourt = (CourtItem) comboCourt.getSelectedItem();

        if (caseNo.isEmpty() || title.isEmpty() || filingDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Case No, Title, and Filing Date are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date filingDate = Date.valueOf(filingDateStr);
            int courtId = selectedCourt != null ? selectedCourt.id : 0;

            CaseModel c = new CaseModel(caseNo, title, filingDate, caseType, status, courtId);
            // Invokes PL/SQL Procedure: add_case
            caseDAO.addCase(c);

            JOptionPane.showMessageDialog(this, "Case inserted successfully via PL/SQL add_case!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadCasesData();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format! Use YYYY-MM-DD (e.g. 2024-05-15)", "Date Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            // Displays PL/SQL exception or trigger ORA error message directly to the user
            JOptionPane.showMessageDialog(this, "Oracle Error: " + ex.getMessage(), "PL/SQL Procedure Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus() {
        String caseNo = txtCaseNo.getText().trim();
        String status = (String) comboStatus.getSelectedItem();

        if (caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a case or enter Case No to update status!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Invokes PL/SQL Procedure: update_case_status
            caseDAO.updateCaseStatus(caseNo, status);

            JOptionPane.showMessageDialog(this, "Case status updated via PL/SQL update_case_status!\n(Check AUDIT_LOG table for logged trigger entry)", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCasesData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Oracle Error: " + ex.getMessage(), "PL/SQL Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCase() {
        String caseNo = txtCaseNo.getText().trim();
        if (caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a case or enter Case No to delete!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Case " + caseNo + "?\nThis will invoke PL/SQL delete_case to clean up child hearings, citations, and junction records.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Invokes PL/SQL Procedure: delete_case
                caseDAO.deleteCase(caseNo);

                JOptionPane.showMessageDialog(this, "Case deleted successfully via PL/SQL delete_case!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadCasesData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Oracle Error: " + ex.getMessage(), "PL/SQL Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = casesTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtCaseNo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtCaseTitle.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtFilingDate.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtCaseType.setText(tableModel.getValueAt(selectedRow, 3) != null ? tableModel.getValueAt(selectedRow, 3).toString() : "");
            comboStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());

            Object courtIdObj = tableModel.getValueAt(selectedRow, 5);
            if (courtIdObj != null) {
                int cId = Integer.parseInt(courtIdObj.toString());
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
        txtCaseNo.setText("");
        txtCaseTitle.setText("");
        txtFilingDate.setText("");
        txtCaseType.setText("");
        comboStatus.setSelectedIndex(0);
        if (comboCourt.getItemCount() > 0) comboCourt.setSelectedIndex(0);
        casesTable.clearSelection();
    }

    // Helper item for JComboBox
    private static class CourtItem {
        int id;
        String name;

        CourtItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + (id > 0 ? " (ID: " + id + ")" : "");
        }
    }
}
