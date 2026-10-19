package ui;

import dao.ProcedureDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;

/**
 * ProceduresPanel - Dedicated testing interface to invoke PL/SQL Stored Functions & Procedures.
 * Designed specifically to help students explain PL/SQL concepts during viva examination.
 */
public class ProceduresPanel extends JPanel {

    private final ProcedureDAO procedureDAO = new ProcedureDAO();

    private JComboBox<String> comboRoutine;
    private JPanel dynamicInputsPanel;
    private JTextArea txtOutput;

    // Field components
    private JTextField txtP1, txtP2, txtP3, txtP4, txtP5, txtP6;
    private JLabel lblP1, lblP2, lblP3, lblP4, lblP5, lblP6;

    public ProceduresPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Selection & Input Form
        add(createSelectionPanel(), BorderLayout.NORTH);

        // Center Output Display Area
        add(createOutputPanel(), BorderLayout.CENTER);

        updateInputFields();
    }

    private JPanel createSelectionPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Select Stored Routine to Execute"));

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectPanel.add(new JLabel("Routine Name:"));

        comboRoutine = new JComboBox<>(new String[]{
            "FUNCTION: get_citation_count(p_case_no)",
            "FUNCTION: get_hearing_count(p_case_no)",
            "PROCEDURE: add_case(no, title, filing_date, type, status, court_id)",
            "PROCEDURE: schedule_hearing(case_no, judge_id, date, remarks)",
            "PROCEDURE: update_case_status(case_no, new_status)",
            "PROCEDURE: delete_case(case_no)"
        });
        comboRoutine.addActionListener(e -> updateInputFields());
        selectPanel.add(comboRoutine);

        JButton btnExecute = new JButton("Invoke Routine");
        btnExecute.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExecute.addActionListener(e -> invokeSelectedRoutine());
        selectPanel.add(btnExecute);

        mainPanel.add(selectPanel, BorderLayout.NORTH);

        // Dynamic Parameter Input Fields Grid
        dynamicInputsPanel = new JPanel(new GridLayout(3, 4, 8, 8));
        dynamicInputsPanel.setBorder(BorderFactory.createTitledBorder("Input Parameters"));

        lblP1 = new JLabel("Param 1:"); txtP1 = new JTextField();
        lblP2 = new JLabel("Param 2:"); txtP2 = new JTextField();
        lblP3 = new JLabel("Param 3:"); txtP3 = new JTextField();
        lblP4 = new JLabel("Param 4:"); txtP4 = new JTextField();
        lblP5 = new JLabel("Param 5:"); txtP5 = new JTextField();
        lblP6 = new JLabel("Param 6:"); txtP6 = new JTextField();

        dynamicInputsPanel.add(lblP1); dynamicInputsPanel.add(txtP1);
        dynamicInputsPanel.add(lblP2); dynamicInputsPanel.add(txtP2);
        dynamicInputsPanel.add(lblP3); dynamicInputsPanel.add(txtP3);
        dynamicInputsPanel.add(lblP4); dynamicInputsPanel.add(txtP4);
        dynamicInputsPanel.add(lblP5); dynamicInputsPanel.add(txtP5);
        dynamicInputsPanel.add(lblP6); dynamicInputsPanel.add(txtP6);

        mainPanel.add(dynamicInputsPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Console Output & Execution Logs"));

        txtOutput = new JTextArea(10, 60);
        txtOutput.setEditable(false);
        txtOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(txtOutput), BorderLayout.CENTER);

        return panel;
    }

    private void updateInputFields() {
        int selected = comboRoutine.getSelectedIndex();

        // Reset text
        txtP1.setText(""); txtP2.setText(""); txtP3.setText("");
        txtP4.setText(""); txtP5.setText(""); txtP6.setText("");

        switch (selected) {
            case 0: // get_citation_count
                lblP1.setText("Case No:"); txtP1.setText("SC-1978-02"); setFieldsVisible(1); break;
            case 1: // get_hearing_count
                lblP1.setText("Case No:"); txtP1.setText("SC-2018-05"); setFieldsVisible(1); break;
            case 2: // add_case
                lblP1.setText("Case No:"); txtP1.setText("SC-2024-999");
                lblP2.setText("Title:"); txtP2.setText("Test Case Title");
                lblP3.setText("Filing Date:"); txtP3.setText("2024-06-01");
                lblP4.setText("Type:"); txtP4.setText("Civil");
                lblP5.setText("Status:"); txtP5.setText("Open");
                lblP6.setText("Court ID:"); txtP6.setText("1");
                setFieldsVisible(6); break;
            case 3: // schedule_hearing
                lblP1.setText("Case No:"); txtP1.setText("SC-2018-05");
                lblP2.setText("Judge ID:"); txtP2.setText("102");
                lblP3.setText("Hearing Date:"); txtP3.setText("2024-09-15");
                lblP4.setText("Remarks:"); txtP4.setText("Viva demo hearing");
                setFieldsVisible(4); break;
            case 4: // update_case_status
                lblP1.setText("Case No:"); txtP1.setText("SC-2018-05");
                lblP2.setText("New Status:"); txtP2.setText("Closed");
                setFieldsVisible(2); break;
            case 5: // delete_case
                lblP1.setText("Case No:"); txtP1.setText("SC-2024-999");
                setFieldsVisible(1); break;
        }
    }

    private void setFieldsVisible(int count) {
        lblP1.setVisible(count >= 1); txtP1.setVisible(count >= 1);
        lblP2.setVisible(count >= 2); txtP2.setVisible(count >= 2);
        lblP3.setVisible(count >= 3); txtP3.setVisible(count >= 3);
        lblP4.setVisible(count >= 4); txtP4.setVisible(count >= 4);
        lblP5.setVisible(count >= 5); txtP5.setVisible(count >= 5);
        lblP6.setVisible(count >= 6); txtP6.setVisible(count >= 6);
    }

    private void invokeSelectedRoutine() {
        int selected = comboRoutine.getSelectedIndex();
        try {
            switch (selected) {
                case 0: // get_citation_count
                    int citations = procedureDAO.callGetCitationCount(txtP1.getText().trim());
                    log("SUCCESS: get_citation_count('" + txtP1.getText().trim() + "') returned " + citations);
                    break;
                case 1: // get_hearing_count
                    int hearings = procedureDAO.callGetHearingCount(txtP1.getText().trim());
                    log("SUCCESS: get_hearing_count('" + txtP1.getText().trim() + "') returned " + hearings);
                    break;
                case 2: // add_case
                    String resAdd = procedureDAO.callAddCase(
                        txtP1.getText().trim(),
                        txtP2.getText().trim(),
                        Date.valueOf(txtP3.getText().trim()),
                        txtP4.getText().trim(),
                        txtP5.getText().trim(),
                        Integer.parseInt(txtP6.getText().trim())
                    );
                    log(resAdd);
                    break;
                case 3: // schedule_hearing
                    String resSched = procedureDAO.callScheduleHearing(
                        txtP1.getText().trim(),
                        Integer.parseInt(txtP2.getText().trim()),
                        Date.valueOf(txtP3.getText().trim()),
                        txtP4.getText().trim()
                    );
                    log(resSched);
                    break;
                case 4: // update_case_status
                    String resUpdate = procedureDAO.callUpdateCaseStatus(
                        txtP1.getText().trim(),
                        txtP2.getText().trim()
                    );
                    log(resUpdate);
                    break;
                case 5: // delete_case
                    String resDel = procedureDAO.callDeleteCase(txtP1.getText().trim());
                    log(resDel);
                    break;
            }
        } catch (Exception ex) {
            log("ERROR Executing Routine: " + ex.getMessage());
        }
    }

    private void log(String message) {
        txtOutput.append("[" + new java.util.Date() + "] " + message + "\n");
    }
}
