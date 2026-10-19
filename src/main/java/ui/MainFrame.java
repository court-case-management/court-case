package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame - Primary Window of the Court Case Management Application.
 * Contains a clean JTabbedPane wrapping all feature modules and a connection status bar.
 */
public class MainFrame extends JFrame {

    private JLabel lblConnStatus;

    public MainFrame() {
        setTitle("Court Case Management System - Oracle DBMS Mini-Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null); // Center window on screen

        setLayout(new BorderLayout());

        // Top Banner / Status Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabbedPane.addTab("Cases", new CasesPanel());
        tabbedPane.addTab("Courts", new CourtsPanel());
        tabbedPane.addTab("Judges", new JudgesPanel());
        tabbedPane.addTab("Lawyers", new LawyersPanel());
        tabbedPane.addTab("Parties", new PartiesPanel());
        tabbedPane.addTab("Hearings", new HearingsPanel());
        tabbedPane.addTab("Citations Network", new CitationsPanel());
        tabbedPane.addTab("Queries & Reports", new ReportsPanel());
        tabbedPane.addTab("SQL Console", new SqlConsolePanel());
        tabbedPane.addTab("Functions & Procedures", new ProceduresPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Initial Connection Health Check
        checkDbConnection();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(240, 243, 246));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JLabel titleLabel = new JLabel("Court Case Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(20, 30, 55));
        header.add(titleLabel, BorderLayout.WEST);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusPanel.setOpaque(false);

        lblConnStatus = new JLabel("Checking Oracle Connection...");
        lblConnStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusPanel.add(lblConnStatus);

        JButton btnTestConn = new JButton("Test / Refresh DB");
        btnTestConn.addActionListener(e -> checkDbConnection());
        statusPanel.add(btnTestConn);

        header.add(statusPanel, BorderLayout.EAST);
        return header;
    }

    private void checkDbConnection() {
        boolean connected = DBConnection.testConnection();
        if (connected) {
            lblConnStatus.setText("● Oracle DB Connected (" + DBConnection.getDbUser() + ")");
            lblConnStatus.setForeground(new Color(0, 128, 0));
        } else {
            lblConnStatus.setText("● Oracle DB Disconnected!");
            lblConnStatus.setForeground(Color.RED);
        }
    }
}
