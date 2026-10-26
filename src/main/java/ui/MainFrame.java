package ui;

import db.DBConnection;
import db.neo4j.Neo4jConnection;
import ui.graph.BenchmarkPanel;
import ui.graph.CypherConsolePanel;
import ui.graph.GraphCrudPanel;
import ui.graph.GraphExplorerPanel;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame - Primary Window of the Court Case Management Application.
 * Contains a clean JTabbedPane wrapping all feature modules and a dual database connection status bar
 * for Oracle RDBMS (DA2) and Neo4j Graph Database (DA3).
 */
public class MainFrame extends JFrame {

    private JLabel lblOracleStatus;
    private JLabel lblNeo4jStatus;

    public MainFrame() {
        setTitle("Court Case Management System with Graph-Based Precedent Analysis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null); // Center window on screen

        setLayout(new BorderLayout());

        // Top Banner / Status Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Relational Modules (DA2 - Oracle RDBMS)
        tabbedPane.addTab("Cases (RDBMS)", new CasesPanel());
        tabbedPane.addTab("Courts", new CourtsPanel());
        tabbedPane.addTab("Judges", new JudgesPanel());
        tabbedPane.addTab("Lawyers", new LawyersPanel());
        tabbedPane.addTab("Parties", new PartiesPanel());
        tabbedPane.addTab("Hearings", new HearingsPanel());
        tabbedPane.addTab("Citations (Relational)", new CitationsPanel());
        tabbedPane.addTab("Queries & Reports", new ReportsPanel());
        tabbedPane.addTab("SQL Console", new SqlConsolePanel());
        tabbedPane.addTab("Functions & Procedures", new ProceduresPanel());

        // Graph Database Modules (DA3 - Neo4j Property Graph)
        tabbedPane.addTab("★ Graph Explorer", new GraphExplorerPanel());
        tabbedPane.addTab("★ Graph CRUD", new GraphCrudPanel());
        tabbedPane.addTab("★ Cypher Console", new CypherConsolePanel());
        tabbedPane.addTab("★ Benchmark (SQL vs Cypher)", new BenchmarkPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Initial Connection Health Check
        checkDbConnections();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(240, 243, 246));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JLabel titleLabel = new JLabel("Court Case Management System (DA3: Oracle + Neo4j)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setForeground(new Color(20, 30, 55));
        header.add(titleLabel, BorderLayout.WEST);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusPanel.setOpaque(false);

        lblOracleStatus = new JLabel("Oracle: Checking...");
        lblOracleStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusPanel.add(lblOracleStatus);

        lblNeo4jStatus = new JLabel("Neo4j: Checking...");
        lblNeo4jStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusPanel.add(lblNeo4jStatus);

        JButton btnTestConn = new JButton("Test / Refresh Connections");
        btnTestConn.addActionListener(e -> checkDbConnections());
        statusPanel.add(btnTestConn);

        header.add(statusPanel, BorderLayout.EAST);
        return header;
    }

    private void checkDbConnections() {
        // Oracle Health Check
        boolean oConnected = DBConnection.testConnection();
        if (oConnected) {
            lblOracleStatus.setText("● Oracle: Connected (" + DBConnection.getDbUser() + ")");
            lblOracleStatus.setForeground(new Color(0, 128, 0));
        } else {
            lblOracleStatus.setText("● Oracle: Offline");
            lblOracleStatus.setForeground(Color.RED);
        }

        // Neo4j Health Check
        boolean nConnected = Neo4jConnection.testConnection();
        if (nConnected) {
            lblNeo4jStatus.setText("● Neo4j: Connected (bolt)");
            lblNeo4jStatus.setForeground(new Color(0, 128, 0));
        } else {
            lblNeo4jStatus.setText("● Neo4j: Offline (bolt:7687)");
            lblNeo4jStatus.setForeground(new Color(220, 38, 38));
        }
    }
}
