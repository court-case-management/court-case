package dao;

import db.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

/**
 * ReportDAO - Generates analytical reports and complex SQL query results.
 * Demonstrates JOIN, GROUP BY, HAVING, Subqueries, Aggregate functions, and CONNECT BY PRIOR.
 */
public class ReportDAO {

    /**
     * Executes a PreparedStatement and converts its ResultSet into a Swing DefaultTableModel.
     */
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
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only tables for reports
            }
        };
    }

    /**
     * Report 1: Cases with Court and Assigned Judge (JOIN)
     */
    public DefaultTableModel getCasesWithCourtAndJudge() throws SQLException {
        String sql = "SELECT C.Case_No, C.Case_Title, C.Case_Type, C.Status, " +
                     "CO.Court_Name, CO.Court_Level, " +
                     "NVL(J.Judge_Name, 'Unassigned') AS Assigned_Judge " +
                     "FROM COURT_CASE C " +
                     "LEFT JOIN COURT CO ON C.Court_ID = CO.Court_ID " +
                     "LEFT JOIN HEARING H ON C.Case_No = H.Case_No " +
                     "LEFT JOIN JUDGE J ON H.Judge_ID = J.Judge_ID " +
                     "GROUP BY C.Case_No, C.Case_Title, C.Case_Type, C.Status, CO.Court_Name, CO.Court_Level, J.Judge_Name " +
                     "ORDER BY C.Case_No";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return buildTableModel(rs);
        }
    }

    /**
     * Report 2: Number of cases per court (GROUP BY)
     */
    public DefaultTableModel getCasesPerCourt() throws SQLException {
        String sql = "SELECT CO.Court_ID, CO.Court_Name, CO.Court_Level, COUNT(C.Case_No) AS Total_Cases " +
                     "FROM COURT CO " +
                     "LEFT JOIN COURT_CASE C ON CO.Court_ID = C.Court_ID " +
                     "GROUP BY CO.Court_ID, CO.Court_Name, CO.Court_Level " +
                     "ORDER BY Total_Cases DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return buildTableModel(rs);
        }
    }

    /**
     * Report 3: Hearings per judge (GROUP BY + HAVING)
     */
    public DefaultTableModel getHearingsPerJudge() throws SQLException {
        String sql = "SELECT J.Judge_ID, J.Judge_Name, J.Designation, COUNT(H.Hearing_ID) AS Total_Hearings " +
                     "FROM JUDGE J " +
                     "JOIN HEARING H ON J.Judge_ID = H.Judge_ID " +
                     "GROUP BY J.Judge_ID, J.Judge_Name, J.Designation " +
                     "HAVING COUNT(H.Hearing_ID) >= 1 " +
                     "ORDER BY Total_Hearings DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return buildTableModel(rs);
        }
    }

    /**
     * Report 4: Most-cited cases (Aggregate + ORDER BY)
     */
    public DefaultTableModel getMostCitedCases() throws SQLException {
        String sql = "SELECT C.Case_No, C.Case_Title, C.Case_Type, C.Status, COUNT(CIT.Citing_Case_No) AS Times_Cited " +
                     "FROM COURT_CASE C " +
                     "JOIN CITATION CIT ON C.Case_No = CIT.Cited_Case_No " +
                     "GROUP BY C.Case_No, C.Case_Title, C.Case_Type, C.Status " +
                     "ORDER BY Times_Cited DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return buildTableModel(rs);
        }
    }

    /**
     * Report 5: Lawyers and Parties represented in cases (Ternary Table REPRESENTS)
     */
    public DefaultTableModel getLawyerPartyRepresentations(String caseNoFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT R.Case_No, CC.Case_Title, L.Lawyer_Name, L.Specialization, P.Party_Name, R.Role " +
            "FROM REPRESENTS R " +
            "JOIN LAWYER L ON R.Lawyer_ID = L.Lawyer_ID " +
            "JOIN PARTY P ON R.Party_ID = P.Party_ID " +
            "JOIN COURT_CASE CC ON R.Case_No = CC.Case_No "
        );
        if (caseNoFilter != null && !caseNoFilter.trim().isEmpty()) {
            sql.append("WHERE R.Case_No = ? ");
        }
        sql.append("ORDER BY R.Case_No, L.Lawyer_Name");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (caseNoFilter != null && !caseNoFilter.trim().isEmpty()) {
                stmt.setString(1, caseNoFilter.trim());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return buildTableModel(rs);
            }
        }
    }

    /**
     * Report 6: Cases invoking a given statute (Subquery)
     */
    public DefaultTableModel getCasesByStatute(String actNamePattern) throws SQLException {
        String sql = "SELECT Case_No, Case_Title, TO_CHAR(Filing_Date, 'YYYY-MM-DD') AS Filing_Date, Case_Type, Status " +
                     "FROM COURT_CASE " +
                     "WHERE Case_No IN ( " +
                     "   SELECT CS.Case_No " +
                     "   FROM CASE_STATUTE CS " +
                     "   JOIN STATUTE S ON CS.Statute_ID = S.Statute_ID " +
                     "   WHERE LOWER(S.Act_Name) LIKE LOWER(?) OR LOWER(S.Section_No) LIKE LOWER(?) " +
                     ") ORDER BY Filing_Date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (actNamePattern != null ? actNamePattern.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                return buildTableModel(rs);
            }
        }
    }

    /**
     * Report 7: Citation chain from a given case using Oracle CONNECT BY PRIOR
     */
    public DefaultTableModel getCitationChain(String startCaseNo) throws SQLException {
        String sql = "SELECT LEVEL AS Chain_Depth, " +
                     "Citing_Case_No, C1.Case_Title AS Citing_Title, " +
                     "Cited_Case_No, C2.Case_Title AS Cited_Title, " +
                     "SYS_CONNECT_BY_PATH(Citing_Case_No, ' -> ') || ' -> ' || Cited_Case_No AS Citation_Path " +
                     "FROM CITATION CIT " +
                     "JOIN COURT_CASE C1 ON CIT.Citing_Case_No = C1.Case_No " +
                     "JOIN COURT_CASE C2 ON CIT.Cited_Case_No = C2.Case_No " +
                     "START WITH Citing_Case_No = ? " +
                     "CONNECT BY PRIOR Cited_Case_No = Citing_Case_No";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startCaseNo != null ? startCaseNo.trim() : "SC-2018-05");
            try (ResultSet rs = stmt.executeQuery()) {
                return buildTableModel(rs);
            }
        }
    }
}
