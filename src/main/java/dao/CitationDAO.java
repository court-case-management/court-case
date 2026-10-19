package dao;

import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CitationDAO - Data Access Object for CITATION table.
 * Demonstrates calling stored function get_citation_count.
 */
public class CitationDAO {

    /**
     * Inserts a citation entry between two cases.
     */
    public void addCitation(String citingCaseNo, String citedCaseNo, Date citationDate) throws SQLException {
        String sql = "INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, citingCaseNo);
            stmt.setString(2, citedCaseNo);
            stmt.setDate(3, citationDate != null ? citationDate : new Date(System.currentTimeMillis()));
            stmt.executeUpdate();
        }
    }

    /**
     * Gets all citation pairs.
     */
    public List<String[]> getAllCitations() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT Citing_Case_No, Cited_Case_No, TO_CHAR(Citation_Date, 'YYYY-MM-DD') AS CDate FROM CITATION ORDER BY Citation_Date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("Citing_Case_No"),
                    rs.getString("Cited_Case_No"),
                    rs.getString("CDate")
                });
            }
        }
        return list;
    }

    /**
     * Gets cases that are cited BY the specified case.
     */
    public List<String[]> getCasesCitedBy(String caseNo) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT CIT.Cited_Case_No, C.Case_Title, TO_CHAR(CIT.Citation_Date, 'YYYY-MM-DD') AS CDate " +
                     "FROM CITATION CIT JOIN COURT_CASE C ON CIT.Cited_Case_No = C.Case_No " +
                     "WHERE CIT.Citing_Case_No = ? ORDER BY CIT.Citation_Date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, caseNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("Cited_Case_No"),
                        rs.getString("Case_Title"),
                        rs.getString("CDate")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Gets cases that cite the specified case.
     */
    public List<String[]> getCasesCiting(String caseNo) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT CIT.Citing_Case_No, C.Case_Title, TO_CHAR(CIT.Citation_Date, 'YYYY-MM-DD') AS CDate " +
                     "FROM CITATION CIT JOIN COURT_CASE C ON CIT.Citing_Case_No = C.Case_No " +
                     "WHERE CIT.Cited_Case_No = ? ORDER BY CIT.Citation_Date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, caseNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("Citing_Case_No"),
                        rs.getString("Case_Title"),
                        rs.getString("CDate")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Invokes PL/SQL Stored Function: get_citation_count
     */
    public int getCitationCount(String caseNo) throws SQLException {
        String funcSql = "{? = call get_citation_count(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(funcSql)) {

            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setString(2, caseNo);
            cstmt.execute();

            return cstmt.getInt(1);
        }
    }
}
