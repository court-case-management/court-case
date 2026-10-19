package ui;

import dao.ReportDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/**
 * ReportsPanel - UI Tab for running predefined SQL analytical queries and reports.
 * Demonstrates JOIN, GROUP BY, HAVING, Subqueries, Aggregates, and Oracle CONNECT BY PRIOR.
 */
public class ReportsPanel extends JPanel {

    private final ReportDAO reportDAO = new ReportDAO();

    private JTable reportTable;
    private JLabel lblReportTitle;
    private JTextField txtStatuteInput;
    private JTextField txtCaseChainInput;

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Control Panel with Buttons
        add(createButtonsPanel(), BorderLayout.NORTH);

        // Center Table Panel
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createButtonsPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Analytical Reports & Complex SQL Queries"));

        // Row 1: General Aggregations & Joins
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        JButton btn1 = new JButton("1. Cases with Court & Judge (JOIN)");
        btn1.addActionListener(e -> runReport1());
        row1.add(btn1);

        JButton btn2 = new JButton("2. Cases per Court (GROUP BY)");
        btn2.addActionListener(e -> runReport2());
        row1.add(btn2);

        JButton btn3 = new JButton("3. Hearings per Judge (GROUP BY + HAVING)");
        btn3.addActionListener(e -> runReport3());
        row1.add(btn3);

        JButton btn4 = new JButton("4. Most-Cited Cases (ORDER BY)");
        btn4.addActionListener(e -> runReport4());
        row1.add(btn4);

        JButton btn5 = new JButton("5. Lawyer-Party Representation (Ternary)");
        btn5.addActionListener(e -> runReport5());
        row1.add(btn5);

        mainPanel.add(row1);

        // Row 2: Subqueries and Oracle Hierarchical Queries
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        row2.add(new JLabel("Statute Filter:"));
        txtStatuteInput = new JTextField("Article 21", 8);
        row2.add(txtStatuteInput);

        JButton btn6 = new JButton("6. Cases by Statute (Subquery)");
        btn6.addActionListener(e -> runReport6());
        row2.add(btn6);

        row2.add(new JLabel("Root Case:"));
        txtCaseChainInput = new JTextField("SC-2018-05", 8);
        row2.add(txtCaseChainInput);

        JButton btn7 = new JButton("7. Citation Chain (CONNECT BY PRIOR)");
        btn7.setToolTipText("Executes Oracle Hierarchical Query starting from root case");
        btn7.addActionListener(e -> runReport7());
        row2.add(btn7);

        mainPanel.add(row2);
        return mainPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Report Output Grid"));

        lblReportTitle = new JLabel("Click any button above to generate report results.");
        lblReportTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblReportTitle.setForeground(new Color(0, 51, 153));
        lblReportTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lblReportTitle, BorderLayout.NORTH);

        reportTable = new JTable();
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    private void runReport1() {
        try {
            lblReportTitle.setText("Report 1: Cases with Court and Assigned Judge (Inner/Left JOIN)");
            DefaultTableModel model = reportDAO.getCasesWithCourtAndJudge();
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void runReport2() {
        try {
            lblReportTitle.setText("Report 2: Number of Cases per Court (GROUP BY)");
            DefaultTableModel model = reportDAO.getCasesPerCourt();
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void runReport3() {
        try {
            lblReportTitle.setText("Report 3: Hearings per Judge (GROUP BY + HAVING Total_Hearings >= 1)");
            DefaultTableModel model = reportDAO.getHearingsPerJudge();
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void runReport4() {
        try {
            lblReportTitle.setText("Report 4: Most-Cited Cases (Aggregate COUNT + ORDER BY)");
            DefaultTableModel model = reportDAO.getMostCitedCases();
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void runReport5() {
        try {
            lblReportTitle.setText("Report 5: Lawyers and Parties Represented in Cases (Ternary REPRESENTS Join)");
            DefaultTableModel model = reportDAO.getLawyerPartyRepresentations("");
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void runReport6() {
        String filter = txtStatuteInput.getText().trim();
        try {
            lblReportTitle.setText("Report 6: Cases Invoking Statute / Act matching '" + filter + "' (Subquery)");
            DefaultTableModel model = reportDAO.getCasesByStatute(filter);
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void runReport7() {
        String rootCase = txtCaseChainInput.getText().trim();
        try {
            lblReportTitle.setText("Report 7: Citation Chain Starting From Case '" + rootCase + "' (Oracle CONNECT BY PRIOR)");
            DefaultTableModel model = reportDAO.getCitationChain(rootCase);
            reportTable.setModel(model);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "Query Execution Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
