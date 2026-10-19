package dao;

import db.DBConnection;
import model.CaseModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CaseDAO - Data Access Object for COURT_CASE table.
 * Demonstrates CallableStatement for PL/SQL procedure execution (add_case, update_case_status, delete_case).
 */
public class CaseDAO {

    /**
     * Retrieves all cases joined with Court Name for user display.
     */
    public List<CaseModel> getAllCases() throws SQLException {
        List<CaseModel> list = new ArrayList<>();
        String sql = "SELECT C.Case_No, C.Case_Title, C.Filing_Date, C.Case_Type, C.Status, C.Court_ID, CO.Court_Name " +
                     "FROM COURT_CASE C LEFT JOIN COURT CO ON C.Court_ID = CO.Court_ID ORDER BY C.Filing_Date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CaseModel cm = new CaseModel(
                    rs.getString("Case_No"),
                    rs.getString("Case_Title"),
                    rs.getDate("Filing_Date"),
                    rs.getString("Case_Type"),
                    rs.getString("Status"),
                    rs.getInt("Court_ID")
                );
                cm.setCourtName(rs.getString("Court_Name"));
                list.add(cm);
            }
        }
        return list;
    }

    /**
     * Inserts a case by invoking PL/SQL Procedure: add_case
     */
    public void addCase(CaseModel caseModel) throws SQLException {
        String callSql = "{call add_case(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {

            cstmt.setString(1, caseModel.getCaseNo());
            cstmt.setString(2, caseModel.getCaseTitle());
            cstmt.setDate(3, caseModel.getFilingDate());
            cstmt.setString(4, caseModel.getCaseType());
            cstmt.setString(5, caseModel.getStatus());
            if (caseModel.getCourtId() > 0) {
                cstmt.setInt(6, caseModel.getCourtId());
            } else {
                cstmt.setNull(6, Types.INTEGER);
            }
            cstmt.execute();
        }
    }

    /**
     * Updates case status by invoking PL/SQL Procedure: update_case_status
     */
    public void updateCaseStatus(String caseNo, String newStatus) throws SQLException {
        String callSql = "{call update_case_status(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {

            cstmt.setString(1, caseNo);
            cstmt.setString(2, newStatus);
            cstmt.execute();
        }
    }

    /**
     * Deletes a case and dependent records by invoking PL/SQL Procedure: delete_case
     */
    public void deleteCase(String caseNo) throws SQLException {
        String callSql = "{call delete_case(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {

            cstmt.setString(1, caseNo);
            cstmt.execute();
        }
    }

    /**
     * Dynamic search for cases by title, status, and court ID filter.
     */
    public List<CaseModel> searchCases(String title, String status, int courtId) throws SQLException {
        List<CaseModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT C.Case_No, C.Case_Title, C.Filing_Date, C.Case_Type, C.Status, C.Court_ID, CO.Court_Name " +
            "FROM COURT_CASE C LEFT JOIN COURT CO ON C.Court_ID = CO.Court_ID WHERE 1=1 "
        );

        if (title != null && !title.trim().isEmpty()) {
            sql.append("AND LOWER(C.Case_Title) LIKE LOWER(?) ");
        }
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            sql.append("AND C.Status = ? ");
        }
        if (courtId > 0) {
            sql.append("AND C.Court_ID = ? ");
        }
        sql.append("ORDER BY C.Filing_Date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (title != null && !title.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + title.trim() + "%");
            }
            if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
                stmt.setString(paramIndex++, status.trim());
            }
            if (courtId > 0) {
                stmt.setInt(paramIndex++, courtId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CaseModel cm = new CaseModel(
                        rs.getString("Case_No"),
                        rs.getString("Case_Title"),
                        rs.getDate("Filing_Date"),
                        rs.getString("Case_Type"),
                        rs.getString("Status"),
                        rs.getInt("Court_ID")
                    );
                    cm.setCourtName(rs.getString("Court_Name"));
                    list.add(cm);
                }
            }
        }
        return list;
    }
}
