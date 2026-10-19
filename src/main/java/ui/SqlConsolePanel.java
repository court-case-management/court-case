package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * SqlConsolePanel - Interactive SQL Command Execution Console.
 * Allows running any raw SQL query/DDL/DML statement (SELECT, INSERT, UPDATE, DELETE, ALTER, CREATE, DROP).
 * Includes "Demo ALTER" button as required for viva demonstration.
 */
public class SqlConsolePanel extends JPanel {

    private JTextArea txtSql;
    private JTable resultTable;
    private JLabel lblStatus;

    public SqlConsolePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top SQL Editor Panel
        add(createEditorPanel(), BorderLayout.NORTH);

        // Center Table Result Panel
        add(createResultPanel(), BorderLayout.CENTER);

        // South Status Bar
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("SQL Query Editor (Enter SELECT / DML / DDL Statement)"));

        txtSql = new JTextArea("SELECT * FROM AUDIT_LOG ORDER BY Changed_On DESC", 4, 60);
        txtSql.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(txtSql), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnExecute = new JButton("Execute SQL");
        btnExecute.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExecute.addActionListener(e -> executeSql());
        btnPanel.add(btnExecute);

        JButton btnDemoAlter = new JButton("Demo ALTER (Add Email to LAWYER)");
        btnDemoAlter.setToolTipText("Executes: ALTER TABLE LAWYER ADD Email VARCHAR2(100)");
        btnDemoAlter.addActionListener(e -> executeDemoAlter());
        btnPanel.add(btnDemoAlter);

        JButton btnClear = new JButton("Clear Console");
        btnClear.addActionListener(e -> {
            txtSql.setText("");
            lblStatus.setText("Console cleared.");
        });
        btnPanel.add(btnClear);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Execution Results Grid"));

        resultTable = new JTable();
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        lblStatus = new JLabel("Status: Ready to execute SQL statements.");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lblStatus);

        return panel;
    }

    private void executeSql() {
        String sql = txtSql.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a SQL statement to execute!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Remove trailing semicolon if present
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean isResultSet = stmt.execute(sql);

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    DefaultTableModel model = buildTableModel(rs);
                    resultTable.setModel(model);
                    lblStatus.setText("SUCCESS: Query returned " + model.getRowCount() + " row(s).");
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                resultTable.setModel(new DefaultTableModel());
                lblStatus.setText("SUCCESS: Statement executed successfully. Rows affected / modified: " + updateCount);
                JOptionPane.showMessageDialog(this, "Statement executed successfully!\nRows affected: " + updateCount, "Execution Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            lblStatus.setText("ERROR: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "SQL Execution Error:\n" + e.getMessage(), "Oracle Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDemoAlter() {
        String alterSql = "ALTER TABLE LAWYER ADD Email VARCHAR2(100)";
        txtSql.setText(alterSql);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Execute demonstration DDL statement?\n" + alterSql,
            "Confirm DDL Alter", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute(alterSql);
                resultTable.setModel(new DefaultTableModel());
                lblStatus.setText("SUCCESS: Column 'Email' added to LAWYER table via ALTER TABLE.");
                JOptionPane.showMessageDialog(this, "DDL Execution Successful!\nColumn 'Email' added to LAWYER table.", "Demo ALTER Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                lblStatus.setText("ERROR / NOTICE: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Oracle Notice / Error:\n" + e.getMessage(), "ALTER Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                vector.add(rs.getObject(i));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}
