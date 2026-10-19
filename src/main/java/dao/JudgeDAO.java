package dao;

import db.DBConnection;
import model.Judge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JudgeDAO - Data Access Object for JUDGE table.
 * Manages judge records using PreparedStatement.
 */
public class JudgeDAO {

    /**
     * Retrieves all judges from the JUDGE table.
     */
    public List<Judge> getAllJudges() throws SQLException {
        List<Judge> list = new ArrayList<>();
        String sql = "SELECT Judge_ID, Judge_Name, Designation, Court_ID FROM JUDGE ORDER BY Judge_ID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Judge(
                    rs.getInt("Judge_ID"),
                    rs.getString("Judge_Name"),
                    rs.getString("Designation"),
                    rs.getInt("Court_ID")
                ));
            }
        }
        return list;
    }

    /**
     * Inserts a new judge using PreparedStatement.
     */
    public void insertJudge(Judge judge) throws SQLException {
        String sql = "INSERT INTO JUDGE (Judge_Name, Designation, Court_ID) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, judge.getJudgeName());
            stmt.setString(2, judge.getDesignation());
            if (judge.getCourtId() > 0) {
                stmt.setInt(3, judge.getCourtId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Updates an existing judge.
     */
    public void updateJudge(Judge judge) throws SQLException {
        String sql = "UPDATE JUDGE SET Judge_Name = ?, Designation = ?, Court_ID = ? WHERE Judge_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, judge.getJudgeName());
            stmt.setString(2, judge.getDesignation());
            if (judge.getCourtId() > 0) {
                stmt.setInt(3, judge.getCourtId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, judge.getJudgeId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a judge by Judge_ID.
     */
    public void deleteJudge(int judgeId) throws SQLException {
        String sql = "DELETE FROM JUDGE WHERE Judge_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, judgeId);
            stmt.executeUpdate();
        }
    }
}
