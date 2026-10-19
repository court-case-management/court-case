package dao;

import db.DBConnection;

import java.sql.*;

/**
 * ProcedureDAO - Provides explicit methods to test all PL/SQL stored procedures and functions directly.
 */
public class ProcedureDAO {

    /**
     * Test add_case procedure.
     */
    public String callAddCase(String caseNo, String title, Date filingDate, String type, String status, int courtId) throws SQLException {
        String call = "{call add_case(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.setString(1, caseNo);
            cstmt.setString(2, title);
            cstmt.setDate(3, filingDate);
            cstmt.setString(4, type);
            cstmt.setString(5, status);
            if (courtId > 0) cstmt.setInt(6, courtId); else cstmt.setNull(6, Types.INTEGER);
            cstmt.execute();
            return "SUCCESS: Procedure add_case executed for Case No: " + caseNo;
        }
    }

    /**
     * Test schedule_hearing procedure.
     */
    public String callScheduleHearing(String caseNo, int judgeId, Date date, String remarks) throws SQLException {
        String call = "{call schedule_hearing(?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.setString(1, caseNo);
            if (judgeId > 0) cstmt.setInt(2, judgeId); else cstmt.setNull(2, Types.INTEGER);
            cstmt.setDate(3, date);
            cstmt.setString(4, remarks);
            cstmt.execute();
            return "SUCCESS: Procedure schedule_hearing executed for Case No: " + caseNo;
        }
    }

    /**
     * Test update_case_status procedure.
     */
    public String callUpdateCaseStatus(String caseNo, String newStatus) throws SQLException {
        String call = "{call update_case_status(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.setString(1, caseNo);
            cstmt.setString(2, newStatus);
            cstmt.execute();
            return "SUCCESS: Procedure update_case_status executed. New Status: " + newStatus;
        }
    }

    /**
     * Test delete_case procedure.
     */
    public String callDeleteCase(String caseNo) throws SQLException {
        String call = "{call delete_case(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.setString(1, caseNo);
            cstmt.execute();
            return "SUCCESS: Procedure delete_case executed for Case No: " + caseNo;
        }
    }

    /**
     * Test get_citation_count function.
     */
    public int callGetCitationCount(String caseNo) throws SQLException {
        String call = "{? = call get_citation_count(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setString(2, caseNo);
            cstmt.execute();
            return cstmt.getInt(1);
        }
    }

    /**
     * Test get_hearing_count function.
     */
    public int callGetHearingCount(String caseNo) throws SQLException {
        String call = "{? = call get_hearing_count(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setString(2, caseNo);
            cstmt.execute();
            return cstmt.getInt(1);
        }
    }
}
