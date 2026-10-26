package ui.graph;

import util.BenchmarkRunner;
import util.SyntheticDataGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * BenchmarkPanel - Comparative Performance Analysis Dashboard.
 * Executes equivalent recursive queries in Oracle SQL (CONNECT BY) vs Neo4j Cypher ([:CITES*1..N])
 * over increasing chain depths (2, 4, 6, 8, 10 hops) to demonstrate Index-Free Adjacency advantages.
 */
public class BenchmarkPanel extends JPanel {

    private JTextField txtRootCase;
    private JSpinner spinReps;
    private JTable benchmarkTable;
    private DefaultTableModel tableModel;
    private JTextArea txtSummary;
    private JLabel lblStatus;
    private JButton btnRunBenchmark;
    private JButton btnGenSynthetic;

    public BenchmarkPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Controls Panel
        add(createControlPanel(), BorderLayout.NORTH);

        // Center SplitPane (Table on Top, Technical Analysis Summary at Bottom)
        add(createResultsPanel(), BorderLayout.CENTER);

        // Bottom Status
        lblStatus = new JLabel("Status: Ready to benchmark. Click 'Run Depth Benchmark'.");
        lblStatus.setBorder(BorderFactory.createEtchedBorder());
        add(lblStatus, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createTitledBorder("Benchmark Parameters (Oracle SQL vs Neo4j Cypher)"));

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        inputs.add(new JLabel("Root Case No:"));
        txtRootCase = new JTextField("SC-2024-02", 12);
        inputs.add(txtRootCase);

        inputs.add(new JLabel("Repetitions per Depth:"));
        spinReps = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        inputs.add(spinReps);

        btnRunBenchmark = new JButton("Run Depth Benchmark (2, 4, 6, 8, 10 Hops)");
        btnRunBenchmark.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRunBenchmark.setBackground(new Color(234, 88, 12));
        btnRunBenchmark.setForeground(Color.WHITE);
        btnRunBenchmark.addActionListener(e -> runBenchmark());
        inputs.add(btnRunBenchmark);

        btnGenSynthetic = new JButton("Generate Large Synthetic Dataset (10k Cases)");
        btnGenSynthetic.addActionListener(e -> generateSyntheticData());
        inputs.add(btnGenSynthetic);

        main.add(inputs, BorderLayout.CENTER);
        return main;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Benchmark Table
        String[] cols = {"Chain Depth", "Oracle SQL Time", "Oracle Rows", "Neo4j Cypher Time", "Neo4j Rows", "Speedup", "Status Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        benchmarkTable = new JTable(tableModel);
        benchmarkTable.setRowHeight(24);
        benchmarkTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane tableScroll = new JScrollPane(benchmarkTable);
        tableScroll.setPreferredSize(new Dimension(800, 220));
        tableScroll.setBorder(BorderFactory.createTitledBorder("Empirical Execution Results Grid"));

        // Technical Analysis Summary Area
        txtSummary = new JTextArea();
        txtSummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSummary.setEditable(false);
        txtSummary.setText(
            "========================================================================================\n" +
            "WHY GRAPH DATABASE (NEO4J) SUITS LEGAL PRECEDENT ANALYSIS BETTER THAN RELATIONAL SQL:\n" +
            "========================================================================================\n" +
            "1. INDEX-FREE ADJACENCY:\n" +
            "   In Neo4j, relationships are direct double-linked pointers on disk/RAM. Traversal from\n" +
            "   a case to its cited precedent takes O(1) pointer dereferences, regardless of total graph\n" +
            "   size. In Oracle SQL, recursive CONNECT BY or self-joins on CITATION must repeatedly\n" +
            "   search B-Tree indexes with O(log N) cost per hop, causing exponential latency growth.\n\n" +
            "2. VARIABLE-LENGTH ARBITRARY-DEPTH TRAVERSALS:\n" +
            "   Cypher expresses multi-depth precedent searches naturally via '[:CITES*1..N]'. Relational\n" +
            "   SQL requires complex recursive Common Table Expressions (WITH RECURSIVE) or vendor-locked\n" +
            "   CONNECT BY syntax that suffers performance degradation on deep hierarchies.\n\n" +
            "3. GRAPH ALGORITHMS (SHORTEST PATH & CENTRALITY):\n" +
            "   Neo4j natively provides shortestPath(), allShortestPaths(), and in-degree aggregation\n" +
            "   out-of-the-box. Implementing bidirectional shortest path in SQL requires hundreds of lines\n" +
            "   of procedural PL/SQL code and temporary tables.\n\n" +
            "WHERE RELATIONAL (ORACLE) REMAINS SUPERIOR:\n" +
            "   - Strict ACID transactions across independent business entities (e.g. Court fees, payment audits).\n" +
            "   - Tabular financial aggregation and reporting (SUM, AVG) over static structured columns.\n" +
            "========================================================================================"
        );
        JScrollPane summaryScroll = new JScrollPane(txtSummary);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Architectural & Performance Analysis (Viva Explanation)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, summaryScroll);
        splitPane.setResizeWeight(0.45);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void runBenchmark() {
        String rootCase = txtRootCase.getText().trim();
        if (rootCase.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a valid Root Case No!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reps = (Integer) spinReps.getValue();
        btnRunBenchmark.setEnabled(false);
        lblStatus.setText("Running benchmark over depths 2, 4, 6, 8, 10 hops... please wait...");

        SwingWorker<List<BenchmarkRunner.BenchmarkRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BenchmarkRunner.BenchmarkRow> doInBackground() {
                int[] depths = {2, 4, 6, 8, 10};
                return BenchmarkRunner.runDepthBenchmark(rootCase, depths, reps);
            }

            @Override
            protected void done() {
                btnRunBenchmark.setEnabled(true);
                try {
                    List<BenchmarkRunner.BenchmarkRow> results = get();
                    tableModel.setRowCount(0);
                    for (BenchmarkRunner.BenchmarkRow r : results) {
                        tableModel.addRow(r.toTableRow());
                    }
                    lblStatus.setText("SUCCESS: Benchmark completed across 5 depth levels.");
                } catch (Exception ex) {
                    lblStatus.setText("ERROR: " + ex.getMessage());
                    JOptionPane.showMessageDialog(BenchmarkPanel.this, "Benchmark Error: " + ex.getMessage(),
                        "Benchmark Failure", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void generateSyntheticData() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Generate large synthetic dataset (10,000 cases, 50,000 citations)?\n" +
            "Files will be saved into folder 'synthetic_data/'.",
            "Generate Synthetic Benchmark Data", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            btnGenSynthetic.setEnabled(false);
            lblStatus.setText("Generating 10,000 synthetic cases and 50,000 citations... please wait...");

            SwingWorker<SyntheticDataGenerator.GenerationResult, Void> worker = new SwingWorker<>() {
                @Override
                protected SyntheticDataGenerator.GenerationResult doInBackground() throws Exception {
                    File dir = new File("synthetic_data");
                    return SyntheticDataGenerator.generateDataset(dir, 10000, 50000);
                }

                @Override
                protected void done() {
                    btnGenSynthetic.setEnabled(true);
                    try {
                        SyntheticDataGenerator.GenerationResult res = get();
                        lblStatus.setText("SUCCESS: Generated " + res.totalCases + " cases and " + res.totalCitations + " citations in " + res.durationMs + " ms.");
                        JOptionPane.showMessageDialog(BenchmarkPanel.this,
                            res.toString() + "\n\nFiles saved in: " + res.outputDir,
                            "Synthetic Data Ready", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        lblStatus.setText("ERROR: " + ex.getMessage());
                        JOptionPane.showMessageDialog(BenchmarkPanel.this, "Data Generation Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}
