package ui.graph;

import dao.graph.GraphQueryDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * CypherConsolePanel - Interactive Cypher Query Execution Terminal.
 * Allows executing arbitrary MATCH / CREATE / MERGE / DELETE queries against Neo4j.
 * Displays results in a tabular grid, execution timing, and instructions for Neo4j Browser.
 */
public class CypherConsolePanel extends JPanel {

    private final GraphQueryDAO queryDAO = new GraphQueryDAO();

    private JTextArea txtCypher;
    private JTable resultTable;
    private JLabel lblStatus;
    private JComboBox<String> comboTemplates;

    public CypherConsolePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Editor Panel
        add(createEditorPanel(), BorderLayout.NORTH);

        // Center Table Panel
        add(createResultPanel(), BorderLayout.CENTER);

        // Bottom Status Panel
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    private JPanel createEditorPanel() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createTitledBorder("Cypher Query Editor (Neo4j 5.x)"));

        // Template Selector Bar
        JPanel templateBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        templateBar.add(new JLabel("Sample Cypher Templates:"));

        comboTemplates = new JComboBox<>(new String[]{
            "-- Select Sample Query --",
            "1. Count nodes by label",
            "2. Sample 25 citation relationships",
            "3. Top 10 most-cited precedents",
            "4. Multi-hop: Lawyers citing Puttaswamy",
            "5. Variable-length precedent path for Electoral Bonds case",
            "6. Find all isolated cases (no citations)",
            "7. Shortest path between SC-2024-02 and SC-1950-08"
        });
        comboTemplates.addActionListener(e -> applyTemplate());
        templateBar.add(comboTemplates);

        main.add(templateBar, BorderLayout.NORTH);

        // Editor Area
        txtCypher = new JTextArea("MATCH (c:Case)-[r:CITES]->(prec:Case)\nRETURN c.case_no AS Citing, c.title AS Title, prec.case_no AS Precedent\nLIMIT 25", 5, 70);
        txtCypher.setFont(new Font("Monospaced", Font.PLAIN, 13));
        main.add(new JScrollPane(txtCypher), BorderLayout.CENTER);

        // Action Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnRun = new JButton("Execute Cypher");
        btnRun.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRun.setBackground(new Color(37, 99, 235));
        btnRun.setForeground(Color.WHITE);
        btnRun.addActionListener(e -> executeCypher());
        btnBar.add(btnRun);

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> txtCypher.setText(""));
        btnBar.add(btnClear);

        JButton btnBrowserInfo = new JButton("Open in Neo4j Browser (Guide)");
        btnBrowserInfo.addActionListener(e -> showBrowserGuide());
        btnBar.add(btnBrowserInfo);

        main.add(btnBar, BorderLayout.SOUTH);
        return main;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Cypher Execution Results Grid"));

        resultTable = new JTable();
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        lblStatus = new JLabel("Status: Ready to execute Cypher queries on bolt://localhost:7687");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lblStatus);

        return panel;
    }

    private void applyTemplate() {
        int idx = comboTemplates.getSelectedIndex();
        switch (idx) {
            case 1:
                txtCypher.setText("MATCH (n)\nRETURN head(labels(n)) AS Label, count(n) AS Total_Nodes\nORDER BY Total_Nodes DESC");
                break;
            case 2:
                txtCypher.setText("MATCH (c1:Case)-[r:CITES]->(c2:Case)\nRETURN c1.case_no AS Citing_Case, c2.case_no AS Precedent, r.citation_date AS Date\nLIMIT 25");
                break;
            case 3:
                txtCypher.setText("MATCH (c:Case)\nOPTIONAL MATCH (citing:Case)-[:CITES]->(c)\nRETURN c.case_no AS Case_No, c.title AS Title, count(citing) AS In_Degree\nORDER BY In_Degree DESC\nLIMIT 10");
                break;
            case 4:
                txtCypher.setText("MATCH (l:Lawyer)-[r:REPRESENTS]->(p:Party)-[:PARTY_TO]->(c:Case)-[:CITES]->(landmark:Case {case_no: 'SC-2018-05'})\nRETURN l.lawyer_name, l.specialization, p.party_name, c.case_no, c.title\nLIMIT 15");
                break;
            case 5:
                txtCypher.setText("MATCH path = (c:Case {case_no: 'SC-2024-02'})-[:CITES*1..5]->(precedent:Case)\nRETURN length(path) AS Depth, precedent.case_no, precedent.title, [x IN nodes(path) | x.case_no] AS Trajectory\nORDER BY Depth ASC");
                break;
            case 6:
                txtCypher.setText("MATCH (c:Case)\nWHERE NOT (c)-[:CITES]->() AND NOT ()-[:CITES]->(c)\nRETURN c.case_no, c.title, c.case_type, c.status");
                break;
            case 7:
                txtCypher.setText("MATCH (c1:Case {case_no: 'SC-2024-02'}), (c2:Case {case_no: 'SC-1950-08'})\nMATCH path = shortestPath((c1)-[:CITES*]-(c2))\nRETURN length(path) AS Hops, [n IN nodes(path) | n.case_no] AS Precedent_Path");
                break;
        }
    }

    private void executeCypher() {
        String cypher = txtCypher.getText().trim();
        if (cypher.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Cypher query to execute!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long start = System.currentTimeMillis();
        try {
            DefaultTableModel model = queryDAO.executeArbitraryCypher(cypher);
            resultTable.setModel(model);

            long elapsed = System.currentTimeMillis() - start;
            lblStatus.setText(String.format("SUCCESS: Query executed in %d ms. Returned %d row(s).", elapsed, model.getRowCount()));
        } catch (Exception ex) {
            lblStatus.setText("ERROR: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Cypher Execution Error:\n" + ex.getMessage(), "Neo4j Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBrowserGuide() {
        String msg =
            "Explore the Interactive 3D/2D Graph Visually in Neo4j Browser:\n\n" +
            "1. Open your web browser at: http://localhost:7474\n" +
            "2. Connect with Username: neo4j, Password: courtpass123\n" +
            "3. Try this visual query:\n\n" +
            "   MATCH (c:Case)-[r:CITES]->(p:Case)\n" +
            "   RETURN c, r, p LIMIT 50;\n\n" +
            "4. You can drag and zoom case nodes, inspect relationships, and style node colors!";
        JOptionPane.showMessageDialog(this, msg, "Neo4j Browser Visualizer Guide", JOptionPane.INFORMATION_MESSAGE);
    }
}
