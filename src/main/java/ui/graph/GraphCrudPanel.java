package ui.graph;

import dao.graph.GraphCrudDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * GraphCrudPanel - UI Tab for executing CRUD operations directly on the Neo4j Graph.
 * Supports:
 * - Adding/Updating Case nodes
 * - Updating Case Status
 * - Deleting Case with DETACH DELETE
 * - Creating & Deleting CITES, INVOKES, PARTY_TO, REPRESENTS relationships
 * - Creating Court, Judge, Lawyer, Party, and Statute nodes
 */
public class GraphCrudPanel extends JPanel {

    private final GraphCrudDAO crudDAO = new GraphCrudDAO();

    // Case Fields
    private JTextField txtCaseNo;
    private JTextField txtTitle;
    private JTextField txtFilingDate;
    private JTextField txtCaseType;
    private JComboBox<String> comboStatus;
    private JTextField txtCourtId;

    // Citation Fields
    private JTextField txtCitingCase;
    private JTextField txtCitedCase;
    private JTextField txtCitationDate;

    // Other Entity Fields
    private JTextField txtCourtName, txtCourtLevel, txtCourtLoc;
    private JTextField txtJudgeName, txtJudgeDesig, txtJudgeCourtId;
    private JTextField txtLawyerName, txtLawyerBar, txtLawyerSpec;

    private JLabel lblStatus;

    public GraphCrudPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane crudTabs = new JTabbedPane();
        crudTabs.setFont(new Font("SansSerif", Font.PLAIN, 12));

        crudTabs.addTab("Manage Case Nodes", createCaseCrudPanel());
        crudTabs.addTab("Manage Citations & Edges", createCitationCrudPanel());
        crudTabs.addTab("Manage Other Nodes (Court, Judge, Lawyer)", createOtherNodesPanel());

        add(crudTabs, BorderLayout.CENTER);

        lblStatus = new JLabel("Status: Ready to perform Graph CRUD operations.");
        lblStatus.setBorder(BorderFactory.createEtchedBorder());
        add(lblStatus, BorderLayout.SOUTH);
    }

    private JPanel createCaseCrudPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Case Node Properties (:Case)"));

        form.add(new JLabel("Case No (Unique):"));
        txtCaseNo = new JTextField("SC-2024-999");
        form.add(txtCaseNo);

        form.add(new JLabel("Case Title:"));
        txtTitle = new JTextField("Graph Demo Case v. State");
        form.add(txtTitle);

        form.add(new JLabel("Filing Date (YYYY-MM-DD):"));
        txtFilingDate = new JTextField(LocalDate.now().toString());
        form.add(txtFilingDate);

        form.add(new JLabel("Case Type:"));
        txtCaseType = new JTextField("Constitutional Law");
        form.add(txtCaseType);

        form.add(new JLabel("Status:"));
        comboStatus = new JComboBox<>(new String[]{"Open", "Closed", "Under Appeal"});
        form.add(comboStatus);

        form.add(new JLabel("Court ID:"));
        txtCourtId = new JTextField("1");
        form.add(txtCourtId);

        panel.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnSaveCase = new JButton("Create / Update Case Node (MERGE)");
        btnSaveCase.addActionListener(e -> saveCaseNode());
        btnPanel.add(btnSaveCase);

        JButton btnUpdateStatus = new JButton("Update Status");
        btnUpdateStatus.addActionListener(e -> updateStatus());
        btnPanel.add(btnUpdateStatus);

        JButton btnDetachDelete = new JButton("Delete Case (DETACH DELETE)");
        btnDetachDelete.setForeground(new Color(185, 28, 28));
        btnDetachDelete.addActionListener(e -> detachDelete());
        btnPanel.add(btnDetachDelete);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCitationCrudPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Manage Precedent Citation Relationship [:CITES]"));

        form.add(new JLabel("Citing Case No (Subsequent):"));
        txtCitingCase = new JTextField("SC-2024-02");
        form.add(txtCitingCase);

        form.add(new JLabel("Cited Precedent Case No:"));
        txtCitedCase = new JTextField("SC-1978-02");
        form.add(txtCitedCase);

        form.add(new JLabel("Citation Date (YYYY-MM-DD):"));
        txtCitationDate = new JTextField(LocalDate.now().toString());
        form.add(txtCitationDate);

        panel.add(form, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAddCitation = new JButton("Create [:CITES] Edge");
        btnAddCitation.addActionListener(e -> createCitation());
        btnPanel.add(btnAddCitation);

        JButton btnDelCitation = new JButton("Delete [:CITES] Edge");
        btnDelCitation.setForeground(new Color(185, 28, 28));
        btnDelCitation.addActionListener(e -> deleteCitation());
        btnPanel.add(btnDelCitation);

        panel.add(btnPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOtherNodesPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Court Subpanel
        JPanel courtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        courtPanel.setBorder(BorderFactory.createTitledBorder("Add Court Node (:Court)"));
        txtCourtName = new JTextField("High Court of Telangana", 14);
        txtCourtLevel = new JTextField("High", 6);
        txtCourtLoc = new JTextField("Hyderabad", 10);
        JButton btnAddCourt = new JButton("Add Court");
        btnAddCourt.addActionListener(e -> addCourtNode());
        courtPanel.add(new JLabel("Name:")); courtPanel.add(txtCourtName);
        courtPanel.add(new JLabel("Level:")); courtPanel.add(txtCourtLevel);
        courtPanel.add(new JLabel("Location:")); courtPanel.add(txtCourtLoc);
        courtPanel.add(btnAddCourt);
        panel.add(courtPanel);

        // 2. Judge Subpanel
        JPanel judgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        judgePanel.setBorder(BorderFactory.createTitledBorder("Add Judge Node (:Judge)-[:SITS_IN]->(:Court)"));
        txtJudgeName = new JTextField("Hon. Justice K. Rao", 14);
        txtJudgeDesig = new JTextField("Senior Judge", 10);
        txtJudgeCourtId = new JTextField("1", 4);
        JButton btnAddJudge = new JButton("Add Judge");
        btnAddJudge.addActionListener(e -> addJudgeNode());
        judgePanel.add(new JLabel("Name:")); judgePanel.add(txtJudgeName);
        judgePanel.add(new JLabel("Designation:")); judgePanel.add(txtJudgeDesig);
        judgePanel.add(new JLabel("Court ID:")); judgePanel.add(txtJudgeCourtId);
        judgePanel.add(btnAddJudge);
        panel.add(judgePanel);

        // 3. Lawyer Subpanel
        JPanel lawyerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        lawyerPanel.setBorder(BorderFactory.createTitledBorder("Add Lawyer Node (:Lawyer)"));
        txtLawyerName = new JTextField("Adv. Ramesh Chandra", 14);
        txtLawyerBar = new JTextField("BAR/DEL/2010/888", 12);
        txtLawyerSpec = new JTextField("Cyber Forensics", 10);
        JButton btnAddLawyer = new JButton("Add Lawyer");
        btnAddLawyer.addActionListener(e -> addLawyerNode());
        lawyerPanel.add(new JLabel("Name:")); lawyerPanel.add(txtLawyerName);
        lawyerPanel.add(new JLabel("Bar Reg:")); lawyerPanel.add(txtLawyerBar);
        lawyerPanel.add(new JLabel("Spec:")); lawyerPanel.add(txtLawyerSpec);
        lawyerPanel.add(btnAddLawyer);
        panel.add(lawyerPanel);

        return panel;
    }

    private void saveCaseNode() {
        try {
            String caseNo = txtCaseNo.getText().trim();
            String title = txtTitle.getText().trim();
            LocalDate fDate = LocalDate.parse(txtFilingDate.getText().trim());
            String type = txtCaseType.getText().trim();
            String status = (String) comboStatus.getSelectedItem();
            int courtId = Integer.parseInt(txtCourtId.getText().trim());

            crudDAO.createOrUpdateCase(caseNo, title, fDate, type, status, courtId);
            lblStatus.setText("SUCCESS: Case node (" + caseNo + ") merged into Neo4j graph.");
            JOptionPane.showMessageDialog(this, "Case node successfully created/merged into Neo4j!",
                "Neo4j CRUD Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Error saving Case node: " + ex.getMessage());
        }
    }

    private void updateStatus() {
        try {
            String caseNo = txtCaseNo.getText().trim();
            String status = (String) comboStatus.getSelectedItem();
            boolean ok = crudDAO.updateCaseStatus(caseNo, status);
            if (ok) {
                lblStatus.setText("SUCCESS: Case (" + caseNo + ") status updated to '" + status + "'.");
                JOptionPane.showMessageDialog(this, "Case status updated in Neo4j!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("Case (" + caseNo + ") not found in Neo4j.");
            }
        } catch (Exception ex) {
            showError("Error updating status: " + ex.getMessage());
        }
    }

    private void detachDelete() {
        String caseNo = txtCaseNo.getText().trim();
        int opt = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to DETACH DELETE Case " + caseNo + "?\n" +
            "This will permanently delete the node and all its incoming/outgoing citation edges.",
            "Confirm Detach Delete", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                boolean ok = crudDAO.detachDeleteCase(caseNo);
                if (ok) {
                    lblStatus.setText("SUCCESS: Case (" + caseNo + ") DETACH DELETED from Neo4j.");
                    JOptionPane.showMessageDialog(this, "Case node and connected edges deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showError("Case (" + caseNo + ") not found in Neo4j.");
                }
            } catch (Exception ex) {
                showError("Error deleting case: " + ex.getMessage());
            }
        }
    }

    private void createCitation() {
        try {
            String citing = txtCitingCase.getText().trim();
            String cited = txtCitedCase.getText().trim();
            LocalDate cDate = LocalDate.parse(txtCitationDate.getText().trim());

            crudDAO.createCitation(citing, cited, cDate);
            lblStatus.setText("SUCCESS: Citation (" + citing + ")-[:CITES]->(" + cited + ") created.");
            JOptionPane.showMessageDialog(this, "Precedent Citation edge created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Error creating citation: " + ex.getMessage());
        }
    }

    private void deleteCitation() {
        try {
            String citing = txtCitingCase.getText().trim();
            String cited = txtCitedCase.getText().trim();

            boolean ok = crudDAO.deleteCitation(citing, cited);
            if (ok) {
                lblStatus.setText("SUCCESS: Citation (" + citing + ")-[:CITES]->(" + cited + ") deleted.");
                JOptionPane.showMessageDialog(this, "Citation edge removed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("No citation found between " + citing + " and " + cited);
            }
        } catch (Exception ex) {
            showError("Error deleting citation: " + ex.getMessage());
        }
    }

    private void addCourtNode() {
        try {
            int id = (int) (System.currentTimeMillis() % 100000);
            crudDAO.createCourt(id, txtCourtName.getText().trim(), txtCourtLevel.getText().trim(), txtCourtLoc.getText().trim());
            lblStatus.setText("SUCCESS: Court node added (ID: " + id + ").");
            JOptionPane.showMessageDialog(this, "Court node added!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void addJudgeNode() {
        try {
            int id = (int) (System.currentTimeMillis() % 100000);
            int courtId = Integer.parseInt(txtJudgeCourtId.getText().trim());
            crudDAO.createJudge(id, txtJudgeName.getText().trim(), txtJudgeDesig.getText().trim(), courtId);
            lblStatus.setText("SUCCESS: Judge node added (ID: " + id + ").");
            JOptionPane.showMessageDialog(this, "Judge node added!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void addLawyerNode() {
        try {
            int id = (int) (System.currentTimeMillis() % 100000);
            crudDAO.createLawyer(id, txtLawyerName.getText().trim(), txtLawyerBar.getText().trim(), txtLawyerSpec.getText().trim());
            lblStatus.setText("SUCCESS: Lawyer node added (ID: " + id + ").");
            JOptionPane.showMessageDialog(this, "Lawyer node added!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        lblStatus.setText("ERROR: " + msg);
        JOptionPane.showMessageDialog(this, msg, "Neo4j Error", JOptionPane.ERROR_MESSAGE);
    }
}
