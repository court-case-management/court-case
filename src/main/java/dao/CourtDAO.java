package dao;

import db.DBConnection;
import model.Court;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CourtDAO - Data Access Object for COURT table.
 * Demonstrates CRUD operations using PreparedStatement and try-with-resources.
 */
public class CourtDAO {

    /**
     * Retrieves all court records.
     * @return List of Court objects
     * @throws SQLException on database query failure
     */
    public List<Court> getAllCourts() throws SQLException {
        List<Court> list = new ArrayList<>();
        String sql = "SELECT Court_ID, Court_Name, Court_Level, Location FROM COURT ORDER BY Court_ID";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Court(
                    rs.getInt("Court_ID"),
                    rs.getString("Court_Name"),
                    rs.getString("Court_Level"),
                    rs.getString("Location")
                ));
            }
        }
        return list;
    }

    /**
     * Inserts a new court into COURT table using PreparedStatement.
     */
    public void insertCourt(Court court) throws SQLException {
        String sql = "INSERT INTO COURT (Court_Name, Court_Level, Location) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, court.getCourtName());
            stmt.setString(2, court.getCourtLevel());
            stmt.setString(3, court.getLocation());
            stmt.executeUpdate();
        }
    }

    /**
     * Updates existing court details by Court_ID.
     */
    public void updateCourt(Court court) throws SQLException {
        String sql = "UPDATE COURT SET Court_Name = ?, Court_Level = ?, Location = ? WHERE Court_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, court.getCourtName());
            stmt.setString(2, court.getCourtLevel());
            stmt.setString(3, court.getLocation());
            stmt.setInt(4, court.getCourtId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a court by Court_ID.
     */
    public void deleteCourt(int courtId) throws SQLException {
        String sql = "DELETE FROM COURT WHERE Court_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courtId);
            stmt.executeUpdate();
        }
    }
}
