package util;

import db.DBConnection;
import db.neo4j.Neo4jConnection;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * BenchmarkRunner - Compares execution performance between Oracle SQL (Recursive CONNECT BY)
 * and Neo4j Cypher (Variable-length path traversal [:CITES*1..N]) for increasing precedent chain depths.
 * Measures latency across multiple repetitions and produces empirical comparative metrics.
 */
public class BenchmarkRunner {

    public static class BenchmarkRow {
        public int maxDepth;
        public long oracleTimeMs;
        public int oracleResultCount;
        public long neo4jTimeMs;
        public int neo4jResultCount;
        public double speedupRatio; // oracle / neo4j
        public String statusNotes;

        public Object[] toTableRow() {
            return new Object[]{
                maxDepth + " hops",
                oracleTimeMs >= 0 ? oracleTimeMs + " ms" : "N/A",
                oracleResultCount,
                neo4jTimeMs >= 0 ? neo4jTimeMs + " ms" : "N/A",
                neo4jResultCount,
                speedupRatio > 0 ? String.format("%.2fx faster", speedupRatio) : "N/A",
                statusNotes
            };
        }
    }

    /**
     * Executes comparative benchmark for chain depths (2, 4, 6, 8, 10 hops).
     */
    public static List<BenchmarkRow> runDepthBenchmark(String rootCaseNo, int[] depths, int repetitions) {
        List<BenchmarkRow> rows = new ArrayList<>();
        boolean oracleAvailable = DBConnection.testConnection();
        boolean neo4jAvailable = Neo4jConnection.testConnection();

        for (int depth : depths) {
            BenchmarkRow row = new BenchmarkRow();
            row.maxDepth = depth;

            // 1. Benchmark Oracle SQL (CONNECT BY PRIOR with LEVEL filter)
            if (oracleAvailable) {
                try (Connection oConn = DBConnection.getConnection()) {
                    String oracleSql =
                        "SELECT LEVEL, Citing_Case_No, Cited_Case_No " +
                        "FROM CITATION " +
                        "WHERE LEVEL <= ? " +
                        "START WITH Citing_Case_No = ? " +
                        "CONNECT BY NOCYCLE PRIOR Cited_Case_No = Citing_Case_No";

                    // Warm-up run
                    try (PreparedStatement stmt = oConn.prepareStatement(oracleSql)) {
                        stmt.setInt(1, depth);
                        stmt.setString(2, rootCaseNo);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {}
                        }
                    }

                    // Timed repetitions
                    long totalTime = 0;
                    int count = 0;
                    for (int r = 0; r < repetitions; r++) {
                        long start = System.nanoTime();
                        try (PreparedStatement stmt = oConn.prepareStatement(oracleSql)) {
                            stmt.setInt(1, depth);
                            stmt.setString(2, rootCaseNo);
                            try (ResultSet rs = stmt.executeQuery()) {
                                count = 0;
                                while (rs.next()) { count++; }
                            }
                        }
                        totalTime += (System.nanoTime() - start);
                    }
                    row.oracleTimeMs = (totalTime / repetitions) / 1_000_000;
                    row.oracleResultCount = count;
                } catch (Exception e) {
                    row.oracleTimeMs = -1;
                    row.statusNotes = "Oracle error: " + e.getMessage();
                }
            } else {
                row.oracleTimeMs = -1;
                row.statusNotes = "Oracle DB not connected. ";
            }

            // 2. Benchmark Neo4j Cypher (Variable-length path)
            if (neo4jAvailable) {
                try (Session session = Neo4jConnection.getSession()) {
                    String cypher =
                        "MATCH path = (c:Case {case_no: $root})-[:CITES*1.." + depth + "]->(target:Case) " +
                        "RETURN length(path) AS depth, target.case_no AS cited_case";

                    // Warm-up run
                    Result warm = session.run(cypher, Values.parameters("root", rootCaseNo));
                    while (warm.hasNext()) { warm.next(); }

                    // Timed repetitions
                    long totalTime = 0;
                    int count = 0;
                    for (int r = 0; r < repetitions; r++) {
                        long start = System.nanoTime();
                        Result res = session.run(cypher, Values.parameters("root", rootCaseNo));
                        count = 0;
                        while (res.hasNext()) {
                            res.next();
                            count++;
                        }
                        totalTime += (System.nanoTime() - start);
                    }
                    row.neo4jTimeMs = (totalTime / repetitions) / 1_000_000;
                    row.neo4jResultCount = count;
                } catch (Exception e) {
                    row.neo4jTimeMs = -1;
                    row.statusNotes = (row.statusNotes != null ? row.statusNotes : "") + "Neo4j error: " + e.getMessage();
                }
            } else {
                row.neo4jTimeMs = -1;
                row.statusNotes = (row.statusNotes != null ? row.statusNotes : "") + "Neo4j not connected.";
            }

            // Calculate speedup
            if (row.oracleTimeMs > 0 && row.neo4jTimeMs > 0) {
                row.speedupRatio = (double) row.oracleTimeMs / Math.max(1, row.neo4jTimeMs);
                row.statusNotes = "Completed successfully.";
            }

            rows.add(row);
        }

        return rows;
    }

    public static void main(String[] args) {
        System.out.println("Running Relational SQL vs Neo4j Cypher Benchmark...");
        int[] depths = {2, 4, 6, 8, 10};
        List<BenchmarkRow> rows = runDepthBenchmark("SC-2024-02", depths, 3);
        System.out.println(String.format("%-10s | %-12s | %-12s | %-15s", "Depth", "Oracle Time", "Neo4j Time", "Speedup"));
        System.out.println("------------------------------------------------------------");
        for (BenchmarkRow r : rows) {
            System.out.println(String.format("%-10s | %-12s | %-12s | %-15s",
                r.maxDepth + " hops",
                r.oracleTimeMs + " ms",
                r.neo4jTimeMs + " ms",
                r.speedupRatio > 0 ? String.format("%.2fx", r.speedupRatio) : "N/A"
            ));
        }
    }
}
