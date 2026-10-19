package ui;

import dao.CaseDAO;
import dao.CitationDAO;
import model.CaseModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * CitationsPanel - UI Tab for legal citations and precedent network analysis.
 * Demonstrates:
 * 1. Stored Function: get_citation_count(p_case_no)
 * 2. Oracle Trigger: trg_no_self_citation (prevents Citing_Case_No = Cited_Case_No)
 * 3. Citation graph querying: Cases cited BY X vs. Cases citing X
 */
public class CitationsPanel extends JPanel {

    private final CitationDAO citationDAO = new CitationDAO();
    private final CaseDAO caseDAO = new CaseDAO();

    private JTable citationsTable;
    private DefaultTableModel tableModel;

    // Form fields
    private JComboBox<CaseItem> comboCitingCase;
    private JComboBox<CaseItem> comboCitedCase;
    private JTextField txtCitationDate; // YYYY-MM-DD

    // Lookup controls
    private JComboBox<CaseItem> comboLookupCase;
    private JLabel lblCitationCount;

    public CitationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // North: Query / Function Inspection
        add(createLookupPanel(), BorderLayout.NORTH);

        // Center: JTable
        add(createTablePanel(), BorderLayout.CENTER);

        // South: Add Citation Form
        add(createAddCitationPanel(), BorderLayout.SOUTH);

        refreshCasesDropdowns();
        loadAllCitations();
    }

    private JPanel createLookupPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Inspect Citation Network & Call Stored Function get_citation_count()"));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Target Case:"));
        comboLookupCase = new JComboBox<>();
        topPanel.add(comboLookupCase);

        JButton btnCitedBy = new JButton("View Cases Cited BY Target");
        btnCitedBy.addActionListener(e -> loadCasesCitedBy());
        topPanel.add(btnCitedBy);

        JButton btnCitingThis = new JButton("View Cases Citing Target");
        btnCitingThis.addActionListener(e -> loadCasesCitingThis());
        topPanel.add(btnCitingThis);

        JButton btnGetCount = new JButton("Get Citation Count (get_citation_count)");
        btnGetCount.setToolTipText("Invokes PL/SQL Function: get_citation_count");
        btnGetCount.addActionListener(e -> checkCitationCountFunction());
        topPanel.add(btnGetCount);

        JButton btnShowAll = new JButton("Show All Citations");
        btnShowAll.addActionListener(e -> loadAllCitations());
        topPanel.add(btnShowAll);

        mainPanel.add(topPanel, BorderLayout.CENTER);

        // Status Label for Stored Function Result
        lblCitationCount = new JLabel("Stored Function Result: Select a case and press 'Get Citation Count'");
        lblCitationCount.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblCitationCount.setForeground(new Color(0, 102, 204));
        lblCitationCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        mainPanel.add(lblCitationCount, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Citation Network Records"));

        String[] columns = {"Citing Case No", "Cited Case No", "Citation Date / Detail"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        citationsTable = new JTable(tableModel);
        panel.add(new JScrollPane(citationsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAddCitationPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Add Citation Precedent (Trigger Test)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 6, 8, 8));

        fieldsPanel.add(new JLabel("Citing Case (Newer):"));
        comboCitingCase = new JComboBox<>();
        fieldsPanel.add(comboCitingCase);

        fieldsPanel.add(new JLabel("Cited Case (Precedent):"));
        comboCitedCase = new JComboBox<>();
        fieldsPanel.add(comboCitedCase);

        fieldsPanel.add(new JLabel("Citation Date:"));
        txtCitationDate = new JTextField();
        txtCitationDate.setToolTipText("YYYY-MM-DD or leave blank for SYSDATE");
        fieldsPanel.add(txtCitationDate);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton btnAdd = new JButton("Add Citation Entry");
        btnAdd.addActionListener(e -> addCitation());

        JButton btnSelfCitationDemo = new JButton("Test Self-Citation Trigger (ORA-20002)");
        btnSelfCitationDemo.setToolTipText("Attempts to cite same case (Citing = Cited) to demonstrate trigger trg_no_self_citation");
        btnSelfCitationDemo.addActionListener(e -> demonstrateSelfCitationTrigger());

        btnPanel.add(btnAdd);
        btnPanel.add(btnSelfCitationDemo);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void refreshCasesDropdowns() {
        comboCitingCase.removeAllItems();
        comboCitedCase.removeAllItems();
        comboLookupCase.removeAllItems();

        comboCitingCase.addItem(new CaseItem("", "-- Select Citing Case --"));
        comboCitedCase.addItem(new CaseItem("", "-- Select Cited Case --"));
        comboLookupCase.addItem(new CaseItem("", "-- Select Case --"));

        try {
            List<CaseModel> cases = caseDAO.getAllCases();
            for (CaseModel c : cases) {
                CaseItem item = new CaseItem(c.getCaseNo(), c.getCaseNo() + " - " + c.getCaseTitle());
                comboCitingCase.addItem(item);
                comboCitedCase.addItem(item);
                comboLookupCase.addItem(item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error populating cases dropdown: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllCitations() {
        tableModel.setColumnIdentifiers(new String[]{"Citing Case No", "Cited Case No", "Citation Date"});
        tableModel.setRowCount(0);
        try {
            List<String[]> list = citationDAO.getAllCitations();
            for (String[] row : list) {
                tableModel.addRow(row);
            }
            lblCitationCount.setText("Displaying all " + list.size() + " citation precedents in database.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCasesCitedBy() {
        CaseItem target = (CaseItem) comboLookupCase.getSelectedItem();
        if (target == null || target.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a target case to view precedents it cited!", "Select Case", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setColumnIdentifiers(new String[]{"Cited Precedent Case No", "Cited Case Title", "Citation Date"});
        tableModel.setRowCount(0);
        try {
            List<String[]> list = citationDAO.getCasesCitedBy(target.caseNo);
            for (String[] row : list) {
                tableModel.addRow(row);
            }
            lblCitationCount.setText("Case (" + target.caseNo + ") cited " + list.size() + " prior precedent cases.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCasesCitingThis() {
        CaseItem target = (CaseItem) comboLookupCase.getSelectedItem();
        if (target == null || target.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a target case to view cases citing it!", "Select Case", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setColumnIdentifiers(new String[]{"Citing Case No", "Citing Case Title", "Citation Date"});
        tableModel.setRowCount(0);
        try {
            List<String[]> list = citationDAO.getCasesCiting(target.caseNo);
            for (String[] row : list) {
                tableModel.addRow(row);
            }
            lblCitationCount.setText("Case (" + target.caseNo + ") is cited as a precedent by " + list.size() + " subsequent cases.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkCitationCountFunction() {
        CaseItem target = (CaseItem) comboLookupCase.getSelectedItem();
        if (target == null || target.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a target case first!", "Select Case", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Invokes PL/SQL Stored Function: get_citation_count
            int count = citationDAO.getCitationCount(target.caseNo);
            lblCitationCount.setText("PL/SQL Function get_citation_count('" + target.caseNo + "') RETURNED: " + count + " citation(s)");
            JOptionPane.showMessageDialog(this,
                "PL/SQL Function Output:\nget_citation_count('" + target.caseNo + "') = " + count,
                "Stored Function Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Function Invocation Error: " + e.getMessage(), "Oracle Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCitation() {
        CaseItem citing = (CaseItem) comboCitingCase.getSelectedItem();
        CaseItem cited = (CaseItem) comboCitedCase.getSelectedItem();
        String dateStr = txtCitationDate.getText().trim();

        if (citing == null || citing.caseNo.isEmpty() || cited == null || cited.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both Citing Case and Cited Case must be selected!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date cDate = dateStr.isEmpty() ? new Date(System.currentTimeMillis()) : Date.valueOf(dateStr);
            citationDAO.addCitation(citing.caseNo, cited.caseNo, cDate);

            JOptionPane.showMessageDialog(this, "Citation entry added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtCitationDate.setText("");
            loadAllCitations();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format! Use YYYY-MM-DD or leave blank for SYSDATE", "Date Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            // Catches Oracle Trigger ORA-20002 or PK duplicate errors
            JOptionPane.showMessageDialog(this, "Oracle Exception:\n" + ex.getMessage(), "PL/SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void demonstrateSelfCitationTrigger() {
        if (comboCitingCase.getItemCount() > 1) {
            comboCitingCase.setSelectedIndex(1);
            comboCitedCase.setSelectedIndex(1); // Select same case for both!
        }

        CaseItem item = (CaseItem) comboCitingCase.getSelectedItem();
        if (item == null || item.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a case first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Attempting to add citation where Citing Case = Cited Case (" + item.caseNo + ").\n" +
            "This will fire Oracle Trigger 'trg_no_self_citation' and raise ORA-20002 exception.",
            "Demonstrating Self-Citation Trigger", JOptionPane.INFORMATION_MESSAGE);

        addCitation();
    }

    private static class CaseItem {
        String caseNo;
        String display;
        CaseItem(String caseNo, String display) { this.caseNo = caseNo; this.display = display; }
        @Override public String toString() { return display; }
    }
}
