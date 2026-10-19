package dao;

import db.DBConnection;
import model.Statute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StatuteDAO - Data Access Object for STATUTE table.
 */
public class StatuteDAO {

    public List<Statute> getAllStatutes() throws SQLException {
        List<Statute> list = new ArrayList<>();
        String sql = "SELECT Statute_ID, Act_Name, Section_No, Description FROM STATUTE ORDER BY Statute_ID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Statute(
                    rs.getInt("Statute_ID"),
                    rs.getString("Act_Name"),
                    rs.getString("Section_No"),
                    rs.getString("Description")
                ));
            }
        }
        return list;
    }

    public void insertStatute(Statute statute) throws SQLException {
        String sql = "INSERT INTO STATUTE (Act_Name, Section_No, Description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statute.getActName());
            stmt.setString(2, statute.getSectionNo());
            stmt.setString(3, statute.getDescription());
            stmt.executeUpdate();
        }
    }

    public void updateStatute(Statute statute) throws SQLException {
        String sql = "UPDATE STATUTE SET Act_Name = ?, Section_No = ?, Description = ? WHERE Statute_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statute.getActName());
            stmt.setString(2, statute.getSectionNo());
            stmt.setString(3, statute.getDescription());
            stmt.setInt(4, statute.getStatuteId());
            stmt.executeUpdate();
        }
    }

    public void deleteStatute(int statuteId) throws SQLException {
        String sql = "DELETE FROM STATUTE WHERE Statute_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, statuteId);
            stmt.executeUpdate();
        }
    }
}
