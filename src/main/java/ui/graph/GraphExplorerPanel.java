package ui.graph;

import dao.graph.GraphCrudDAO;
import dao.graph.GraphQueryDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * GraphExplorerPanel - UI Tab for interactive precedent graph analysis in Neo4j.
 * Allows entering a Case_No and executing graph operations:
 * - Full precedent chain ([:CITES*1..])
 * - Downstream dependent cases
 * - Shortest path (shortestPath)
 * - Similar cases based on graph overlap
 * - Multi-hop judicial & lawyer investigations
 * Displays results in Table, Indented Text Tree, and Interactive Visual Graph Canvas.
 */
public class GraphExplorerPanel extends JPanel {

    private final GraphQueryDAO queryDAO = new GraphQueryDAO();
    private final GraphCrudDAO crudDAO = new GraphCrudDAO();

    // Input controls
    private JComboBox<String> comboCase;
    private JTextField txtTargetCase2; // for shortest path
    private JLabel lblTargetCase2;
    private JComboBox<String> comboOperation;
    private JButton btnExecute;

    // Presentation views (Tabbed inside Explorer)
    private JTable resultTable;
    private JTextArea txtTreeView;
    private GraphVisualizerPanel visualizerPanel;
    private JLabel lblStatus;

    public GraphExplorerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Control Panel
        add(createControlPanel(), BorderLayout.NORTH);

        // Center Views (TabbedPane: Table, Indented Tree, Visual Canvas)
        add(createDisplayPanel(), BorderLayout.CENTER);

        // Bottom Status Bar
        lblStatus = new JLabel("Status: Select a case and operation, then click 'Execute Graph Query'.");
        lblStatus.setBorder(BorderFactory.createEtchedBorder());
        add(lblStatus, BorderLayout.SOUTH);

        refreshCaseDropdown();
    }

    private JPanel createControlPanel() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createTitledBorder("Graph Precedent Explorer (Cypher Powered)"));

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        inputs.add(new JLabel("Case No:"));
        comboCase = new JComboBox<>();
        comboCase.setEditable(true);
        comboCase.setPreferredSize(new Dimension(150, 26));
        inputs.add(comboCase);

        inputs.add(new JLabel("Operation:"));
        comboOperation = new JComboBox<>(new String[]{
            "1. Full Precedent Chain (Variable-length [:CITES*1..])",
            "2. Downstream Dependent Cases (Reverse Transitive)",
            "3. Shortest Citation Path (shortestPath)",
            "4. Similar Cases (Shared Statutes & Precedents)",
            "5. Most Influential Cases (In-Degree Centrality)",
            "6. Co-Cited Cases (Frequently Cited Together)",
            "7. Judges Presiding Over Article 21 Cases",
            "8. Lawyers Citing Maneka Gandhi Landmark",
            "9. Citation Depth & Node Classification",
            "10. Cross-Court Influence (High/District -> Supreme)"
        });
        comboOperation.addActionListener(e -> updateOperationInputs());
        inputs.add(comboOperation);

        lblTargetCase2 = new JLabel("To Case:");
        lblTargetCase2.setVisible(false);
        inputs.add(lblTargetCase2);

        txtTargetCase2 = new JTextField("SC-1950-08", 10);
        txtTargetCase2.setVisible(false);
        inputs.add(txtTargetCase2);

        btnExecute = new JButton("Execute Graph Query");
        btnExecute.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExecute.addActionListener(e -> executeSelectedOperation());
        inputs.add(btnExecute);

        JButton btnRefreshCases = new JButton("Refresh Cases");
        btnRefreshCases.addActionListener(e -> refreshCaseDropdown());
        inputs.add(btnRefreshCases);

        main.add(inputs, BorderLayout.CENTER);
        return main;
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane displayTabs = new JTabbedPane();
        displayTabs.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Tab 1: Table View
        resultTable = new JTable();
        displayTabs.addTab("Grid Table View", new JScrollPane(resultTable));

        // Tab 2: Indented Text Tree View
        txtTreeView = new JTextArea();
        txtTreeView.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtTreeView.setEditable(false);
        displayTabs.addTab("Indented Precedent Tree", new JScrollPane(txtTreeView));

        // Tab 3: Visual Canvas
        visualizerPanel = new GraphVisualizerPanel();
        displayTabs.addTab("Visual Graph View", visualizerPanel);

        panel.add(displayTabs, BorderLayout.CENTER);
        return panel;
    }

    private void updateOperationInputs() {
        int idx = comboOperation.getSelectedIndex();
        boolean isShortestPath = (idx == 2);
        lblTargetCase2.setVisible(isShortestPath);
        txtTargetCase2.setVisible(isShortestPath);
    }

    private void refreshCaseDropdown() {
        comboCase.removeAllItems();
        List<Map<String, String>> cases = crudDAO.getAllCasesSummary();
        if (cases.isEmpty()) {
            // Default sample entries if Neo4j not yet populated
            comboCase.addItem("SC-2024-02");
            comboCase.addItem("SC-2018-05");
            comboCase.addItem("SC-2015-06");
            comboCase.addItem("SC-1978-02");
            comboCase.addItem("SC-1973-01");
            comboCase.addItem("HC-2023-101");
        } else {
            for (Map<String, String> m : cases) {
                comboCase.addItem(m.get("case_no"));
            }
        }
    }

    private void executeSelectedOperation() {
        String caseNo = (String) comboCase.getSelectedItem();
        if (caseNo == null) caseNo = "";
        caseNo = caseNo.trim();

        int op = comboOperation.getSelectedIndex();
        long start = System.currentTimeMillis();

        try {
            DefaultTableModel model = null;
            StringBuilder treeBuilder = new StringBuilder();

            switch (op) {
                case 0: // Full Precedent Chain
                    model = queryDAO.getFullPrecedentChain(caseNo);
                    treeBuilder.append("Root Case: ").append(caseNo).append("\n");
                    treeBuilder.append("Transitive Precedent Dependency Tree:\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        int depth = Integer.parseInt(model.getValueAt(i, 3).toString());
                        String indent = "  ".repeat(depth) + "|--> [Hop " + depth + "] ";
                        treeBuilder.append(indent)
                            .append(model.getValueAt(i, 0)).append(": ")
                            .append(model.getValueAt(i, 1)).append(" (Path: ")
                            .append(model.getValueAt(i, 4)).append(")\n");
                    }
                    break;

                case 1: // Downstream Dependent Cases
                    model = queryDAO.getDownstreamDependentCases(caseNo);
                    treeBuilder.append("Landmark Precedent: ").append(caseNo).append("\n");
                    treeBuilder.append("Subsequent Downstream Cases Depending on this Decision:\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        int depth = Integer.parseInt(model.getValueAt(i, 3).toString());
                        String indent = "  ".repeat(depth) + "<-- [Depth " + depth + "] ";
                        treeBuilder.append(indent)
                            .append(model.getValueAt(i, 0)).append(": ")
                            .append(model.getValueAt(i, 1)).append("\n");
                    }
                    break;

                case 2: // Shortest Path
                    String caseTo = txtTargetCase2.getText().trim();
                    model = queryDAO.getShortestCitationPath(caseNo, caseTo);
                    treeBuilder.append("Shortest Citation Path between ").append(caseNo).append(" and ").append(caseTo).append(":\n\n");
                    if (model.getRowCount() > 0) {
                        treeBuilder.append("Hop Length: ").append(model.getValueAt(0, 0)).append("\n");
                        treeBuilder.append("Precedent Trail:\n").append(model.getValueAt(0, 1)).append("\n");
                    } else {
                        treeBuilder.append("No citation path connects these two cases.\n");
                    }
                    break;

                case 3: // Similar Cases
                    model = queryDAO.getSimilarCases(caseNo);
                    treeBuilder.append("Cases Semantically Similar to ").append(caseNo).append(" based on Shared Authorities:\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  * %s - %s [Score: %s | Shared Statutes: %s | Shared Precedents: %s]\n",
                            model.getValueAt(i, 0), model.getValueAt(i, 1), model.getValueAt(i, 5), model.getValueAt(i, 3), model.getValueAt(i, 4)));
                    }
                    break;

                case 4: // Most Influential Cases
                    model = queryDAO.getMostInfluentialCases();
                    treeBuilder.append("Top 20 Most Influential Cases by In-Degree Citation Count:\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  [%2d] %-12s | Citations: %-3s | %s\n",
                            (i + 1), model.getValueAt(i, 0), model.getValueAt(i, 4), model.getValueAt(i, 1)));
                    }
                    break;

                case 5: // Co-Cited Cases
                    model = queryDAO.getCoCitedCases();
                    treeBuilder.append("Precedent Pairs Frequently Co-Cited Together by Later Courts:\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  * (%s) & (%s) -> Co-cited %s times\n",
                            model.getValueAt(i, 0), model.getValueAt(i, 2), model.getValueAt(i, 4)));
                    }
                    break;

                case 6: // Judges Presiding over Statute
                    model = queryDAO.getJudgesByStatute("Constitution of India", "Article 21");
                    treeBuilder.append("Judges Presiding Over Article 21 (Personal Liberty) Cases:\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  * %s (%s) - Heard %s cases: %s\n",
                            model.getValueAt(i, 1), model.getValueAt(i, 2), model.getValueAt(i, 3), model.getValueAt(i, 4)));
                    }
                    break;

                case 7: // Lawyers Citing Landmark
                    model = queryDAO.getLawyersInvokingLandmark("SC-1978-02");
                    treeBuilder.append("Lawyers Invoking Landmark Precedent (Maneka Gandhi - SC-1978-02):\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  * %s (%s) representing '%s' in Case %s\n",
                            model.getValueAt(i, 0), model.getValueAt(i, 2), model.getValueAt(i, 3), model.getValueAt(i, 5)));
                    }
                    break;

                case 8: // Citation Depth & Classification
                    model = queryDAO.getCitationDepthAndClassification();
                    treeBuilder.append("Graph Topology: Precedent Depth & Isolated vs Root Classification:\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  * %-12s | Depth: %-2s | In-Citations: %-2s | Type: %s\n",
                            model.getValueAt(i, 0), model.getValueAt(i, 2), model.getValueAt(i, 3), model.getValueAt(i, 4)));
                    }
                    break;

                case 9: // Cross-Court Influence
                    model = queryDAO.getCrossCourtInfluence();
                    treeBuilder.append("Cross-Court Jurisprudential Influence (Subordinate -> Supreme Court):\n\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                        treeBuilder.append(String.format("  * Court: %s (%s) -> Cited Supreme Court in %s cases: %s\n",
                            model.getValueAt(i, 1), model.getValueAt(i, 0), model.getValueAt(i, 2), model.getValueAt(i, 3)));
                    }
                    break;
            }

            // Update Views
            if (model != null) {
                resultTable.setModel(model);
            }
            txtTreeView.setText(treeBuilder.toString());
            txtTreeView.setCaretPosition(0);

            // Update Visual Canvas with neighborhood graph around target case
            Map<String, Object> gData = queryDAO.getNeighborhoodGraph(caseNo, 2);
            List<Map<String, String>> nList = (List<Map<String, String>>) gData.get("nodes");
            List<Map<String, String>> eList = (List<Map<String, String>>) gData.get("edges");
            visualizerPanel.setGraphData(caseNo, nList, eList);

            long elapsed = System.currentTimeMillis() - start;
            lblStatus.setText(String.format("SUCCESS: Operation executed in %d ms. Returned %d row(s).",
                elapsed, model != null ? model.getRowCount() : 0));

        } catch (Exception ex) {
            lblStatus.setText("ERROR: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Graph Query Error:\n" + ex.getMessage(),
                "Neo4j Cypher Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
