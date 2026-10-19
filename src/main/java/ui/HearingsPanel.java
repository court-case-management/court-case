package ui;

import dao.CaseDAO;
import dao.HearingDAO;
import dao.JudgeDAO;
import model.CaseModel;
import model.Hearing;
import model.Judge;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * HearingsPanel - UI Tab for scheduling and viewing Case Hearings.
 * Demonstrates:
 * 1. PL/SQL Procedure execution: schedule_hearing
 * 2. PL/SQL Trigger validation: trg_hearing_date_check (prevents date before filing date)
 * 3. PL/SQL Cursor execution: get_case_hearings (returns SYS_REFCURSOR)
 */
public class HearingsPanel extends JPanel {

    private final HearingDAO hearingDAO = new HearingDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final JudgeDAO judgeDAO = new JudgeDAO();

    private JTable hearingsTable;
    private DefaultTableModel tableModel;

    // Form controls
    private JComboBox<CaseItem> comboCases;
    private JComboBox<JudgeItem> comboJudges;
    private JTextField txtHearingDate; // YYYY-MM-DD
    private JTextField txtRemarks;

    // Filter controls
    private JComboBox<CaseItem> comboFilterCase;

    public HearingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Filter Panel
        add(createFilterPanel(), BorderLayout.NORTH);

        // Center Table Panel
        add(createTablePanel(), BorderLayout.CENTER);

        // South Schedule Hearing Form
        add(createScheduleFormPanel(), BorderLayout.SOUTH);

        refreshDropdowns();
        loadAllHearings();
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filter Hearings by Case (Ref Cursor Procedure Test)"));

        panel.add(new JLabel("Select Case:"));
        comboFilterCase = new JComboBox<>();
        panel.add(comboFilterCase);

        JButton btnFilterCursor = new JButton("View Hearings via SYS_REFCURSOR");
        btnFilterCursor.setToolTipText("Calls PL/SQL procedure get_case_hearings with SYS_REFCURSOR OUT parameter");
        btnFilterCursor.addActionListener(e -> loadHearingsBySelectedCaseCursor());
        panel.add(btnFilterCursor);

        JButton btnShowAll = new JButton("Show All Hearings");
        btnShowAll.addActionListener(e -> loadAllHearings());
        panel.add(btnShowAll);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Scheduled Hearings"));

        String[] columns = {"Hearing ID", "Case No", "Hearing Date", "Assigned Judge", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        hearingsTable = new JTable(tableModel);
        panel.add(new JScrollPane(hearingsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createScheduleFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Schedule New Hearing (PL/SQL Procedure + Trigger Test)"));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 4, 8, 8));

        fieldsPanel.add(new JLabel("Target Case:"));
        comboCases = new JComboBox<>();
        fieldsPanel.add(comboCases);

        fieldsPanel.add(new JLabel("Assigned Judge:"));
        comboJudges = new JComboBox<>();
        fieldsPanel.add(comboJudges);

        fieldsPanel.add(new JLabel("Hearing Date (YYYY-MM-DD):"));
        txtHearingDate = new JTextField();
        fieldsPanel.add(txtHearingDate);

        fieldsPanel.add(new JLabel("Remarks / Agenda:"));
        txtRemarks = new JTextField();
        fieldsPanel.add(txtRemarks);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnSchedule = new JButton("Schedule Hearing (schedule_hearing)");
        btnSchedule.setToolTipText("Invokes schedule_hearing. Triggers ORA-20001 if date is before filing date.");
        btnSchedule.addActionListener(e -> scheduleHearing());

        JButton btnTriggerDemo = new JButton("Test Trigger Error (Invalid Date Demo)");
        btnTriggerDemo.setToolTipText("Intentionally inputs an invalid early date to demonstrate trigger trg_hearing_date_check");
        btnTriggerDemo.addActionListener(e -> demonstrateTriggerError());

        btnPanel.add(btnSchedule);
        btnPanel.add(btnTriggerDemo);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void refreshDropdowns() {
        comboCases.removeAllItems();
        comboFilterCase.removeAllItems();
        comboJudges.removeAllItems();

        comboFilterCase.addItem(new CaseItem("", "-- All Cases --"));
        comboCases.addItem(new CaseItem("", "-- Select Case --"));
        comboJudges.addItem(new JudgeItem(0, "-- Select Judge --"));

        try {
            List<CaseModel> cases = caseDAO.getAllCases();
            for (CaseModel c : cases) {
                CaseItem item = new CaseItem(c.getCaseNo(), c.getCaseNo() + " - " + c.getCaseTitle() + " (Filed: " + c.getFilingDate() + ")");
                comboCases.addItem(item);
                comboFilterCase.addItem(item);
            }

            List<Judge> judges = judgeDAO.getAllJudges();
            for (Judge j : judges) {
                comboJudges.addItem(new JudgeItem(j.getJudgeId(), j.getJudgeName() + " (" + j.getDesignation() + ")"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error populating dropdowns: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllHearings() {
        tableModel.setRowCount(0);
        try {
            List<Hearing> hearings = hearingDAO.getAllHearings();
            for (Hearing h : hearings) {
                tableModel.addRow(new Object[]{
                    h.getHearingId(),
                    h.getCaseNo(),
                    h.getHearingDate(),
                    h.getJudgeName() != null ? h.getJudgeName() : "Unassigned",
                    h.getRemarks()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHearingsBySelectedCaseCursor() {
        CaseItem item = (CaseItem) comboFilterCase.getSelectedItem();
        if (item == null || item.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a specific case from dropdown to test SYS_REFCURSOR!", "Select Case", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        try {
            // Invokes get_case_hearings procedure using REF_CURSOR
            List<Hearing> hearings = hearingDAO.getHearingsByCaseCursor(item.caseNo);
            for (Hearing h : hearings) {
                tableModel.addRow(new Object[]{
                    h.getHearingId(),
                    h.getCaseNo(),
                    h.getHearingDate(),
                    h.getJudgeName() != null ? h.getJudgeName() : "Unassigned",
                    h.getRemarks()
                });
            }
            JOptionPane.showMessageDialog(this, "Retrieved " + hearings.size() + " hearings via PL/SQL SYS_REFCURSOR for Case " + item.caseNo, "Cursor Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ref Cursor Error: " + e.getMessage(), "Oracle PL/SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void scheduleHearing() {
        CaseItem selectedCase = (CaseItem) comboCases.getSelectedItem();
        JudgeItem selectedJudge = (JudgeItem) comboJudges.getSelectedItem();
        String dateStr = txtHearingDate.getText().trim();
        String remarks = txtRemarks.getText().trim();

        if (selectedCase == null || selectedCase.caseNo.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Target Case and Hearing Date are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date hDate = Date.valueOf(dateStr);
            int judgeId = selectedJudge != null ? selectedJudge.id : 0;

            Hearing h = new Hearing(0, selectedCase.caseNo, judgeId, hDate, remarks);
            // Calls PL/SQL procedure: schedule_hearing
            hearingDAO.scheduleHearing(h);

            JOptionPane.showMessageDialog(this, "Hearing scheduled successfully via PL/SQL schedule_hearing!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtHearingDate.setText("");
            txtRemarks.setText("");
            loadAllHearings();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format! Use YYYY-MM-DD (e.g. 2024-06-15)", "Date Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            // Catches Oracle ORA-20001 triggered by trg_hearing_date_check or procedure error
            JOptionPane.showMessageDialog(this, "Oracle Trigger Exception:\n" + ex.getMessage(), "PL/SQL Trigger Error (ORA-20001)", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void demonstrateTriggerError() {
        CaseItem selectedCase = (CaseItem) comboCases.getSelectedItem();
        if (selectedCase == null || selectedCase.caseNo.isEmpty()) {
            if (comboCases.getItemCount() > 1) {
                comboCases.setSelectedIndex(1);
                selectedCase = (CaseItem) comboCases.getSelectedItem();
            }
        }

        if (selectedCase == null || selectedCase.caseNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or create a case first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        txtHearingDate.setText("1940-01-01"); // Intentionally way before any case filing date
        txtRemarks.setText("Trigger Test: Pre-filing date attempt");

        JOptionPane.showMessageDialog(this,
            "Attempting to schedule a hearing dated '1940-01-01' for Case " + selectedCase.caseNo + ".\n" +
            "This will fire Oracle Trigger 'trg_hearing_date_check' and raise ORA-20001 exception.",
            "Demonstrating Trigger Validation", JOptionPane.INFORMATION_MESSAGE);

        scheduleHearing();
    }

    private static class CaseItem {
        String caseNo;
        String display;
        CaseItem(String caseNo, String display) { this.caseNo = caseNo; this.display = display; }
        @Override public String toString() { return display; }
    }

    private static class JudgeItem {
        int id;
        String display;
        JudgeItem(int id, String display) { this.id = id; this.display = display; }
        @Override public String toString() { return display; }
    }
}
