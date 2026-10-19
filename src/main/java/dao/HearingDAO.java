package dao;

import db.DBConnection;
import model.Hearing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HearingDAO - Data Access Object for HEARING table.
 * Demonstrates procedure call with IN/OUT cursor parameters.
 */
public class HearingDAO {

    /**
     * Retrieves all hearings with judge names.
     */
    public List<Hearing> getAllHearings() throws SQLException {
        List<Hearing> list = new ArrayList<>();
        String sql = "SELECT H.Hearing_ID, H.Case_No, H.Judge_ID, H.Hearing_Date, H.Remarks, J.Judge_Name " +
                     "FROM HEARING H LEFT JOIN JUDGE J ON H.Judge_ID = J.Judge_ID ORDER BY H.Hearing_Date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Hearing h = new Hearing(
                    rs.getInt("Hearing_ID"),
                    rs.getString("Case_No"),
                    rs.getInt("Judge_ID"),
                    rs.getDate("Hearing_Date"),
                    rs.getString("Remarks")
                );
                h.setJudgeName(rs.getString("Judge_Name"));
                list.add(h);
            }
        }
        return list;
    }

    /**
     * Schedules a hearing by calling PL/SQL procedure: schedule_hearing
     */
    public void scheduleHearing(Hearing hearing) throws SQLException {
        String callSql = "{call schedule_hearing(?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {

            cstmt.setString(1, hearing.getCaseNo());
            if (hearing.getJudgeId() > 0) {
                cstmt.setInt(2, hearing.getJudgeId());
            } else {
                cstmt.setNull(2, Types.INTEGER);
            }
            cstmt.setDate(3, hearing.getHearingDate());
            cstmt.setString(4, hearing.getRemarks());
            cstmt.execute();
        }
    }

    /**
     * Retrieves hearings for a case using PL/SQL Procedure with REF CURSOR output parameter.
     */
    public List<Hearing> getHearingsByCaseCursor(String caseNo) throws SQLException {
        List<Hearing> list = new ArrayList<>();
        String callSql = "{call get_case_hearings(?, ?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {

            cstmt.setString(1, caseNo);
            // Register Oracle REF CURSOR output parameter
            cstmt.registerOutParameter(2, Types.REF_CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(2)) {
                if (rs != null) {
                    while (rs.next()) {
                        Hearing h = new Hearing();
                        h.setHearingId(rs.getInt("Hearing_ID"));
                        h.setCaseNo(rs.getString("Case_No"));
                        h.setHearingDate(rs.getDate("Hearing_Date"));
                        h.setRemarks(rs.getString("Remarks"));
                        h.setJudgeName(rs.getString("Judge_Name"));
                        list.add(h);
                    }
                }
            }
        }
        return list;
    }
}
