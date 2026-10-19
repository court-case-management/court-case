package dao;

import db.DBConnection;
import model.Lawyer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LawyerDAO - Data Access Object for LAWYER table.
 */
public class LawyerDAO {

    public List<Lawyer> getAllLawyers() throws SQLException {
        List<Lawyer> list = new ArrayList<>();
        String sql = "SELECT Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization FROM LAWYER ORDER BY Lawyer_ID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Lawyer(
                    rs.getInt("Lawyer_ID"),
                    rs.getString("Lawyer_Name"),
                    rs.getString("Bar_Reg_No"),
                    rs.getString("Specialization")
                ));
            }
        }
        return list;
    }

    public void insertLawyer(Lawyer lawyer) throws SQLException {
        String sql = "INSERT INTO LAWYER (Lawyer_Name, Bar_Reg_No, Specialization) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lawyer.getLawyerName());
            stmt.setString(2, lawyer.getBarRegNo());
            stmt.setString(3, lawyer.getSpecialization());
            stmt.executeUpdate();
        }
    }

    public void updateLawyer(Lawyer lawyer) throws SQLException {
        String sql = "UPDATE LAWYER SET Lawyer_Name = ?, Bar_Reg_No = ?, Specialization = ? WHERE Lawyer_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lawyer.getLawyerName());
            stmt.setString(2, lawyer.getBarRegNo());
            stmt.setString(3, lawyer.getSpecialization());
            stmt.setInt(4, lawyer.getLawyerId());
            stmt.executeUpdate();
        }
    }

    public void deleteLawyer(int lawyerId) throws SQLException {
        String sql = "DELETE FROM LAWYER WHERE Lawyer_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lawyerId);
            stmt.executeUpdate();
        }
    }
}
