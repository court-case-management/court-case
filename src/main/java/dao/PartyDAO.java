package dao;

import db.DBConnection;
import model.Party;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PartyDAO - Data Access Object for PARTY table.
 */
public class PartyDAO {

    public List<Party> getAllParties() throws SQLException {
        List<Party> list = new ArrayList<>();
        String sql = "SELECT Party_ID, Party_Name, Contact_No FROM PARTY ORDER BY Party_ID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Party(
                    rs.getInt("Party_ID"),
                    rs.getString("Party_Name"),
                    rs.getString("Contact_No")
                ));
            }
        }
        return list;
    }

    public void insertParty(Party party) throws SQLException {
        String sql = "INSERT INTO PARTY (Party_Name, Contact_No) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, party.getPartyName());
            stmt.setString(2, party.getContactNo());
            stmt.executeUpdate();
        }
    }

    public void updateParty(Party party) throws SQLException {
        String sql = "UPDATE PARTY SET Party_Name = ?, Contact_No = ? WHERE Party_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, party.getPartyName());
            stmt.setString(2, party.getContactNo());
            stmt.setInt(3, party.getPartyId());
            stmt.executeUpdate();
        }
    }

    public void deleteParty(int partyId) throws SQLException {
        String sql = "DELETE FROM PARTY WHERE Party_ID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, partyId);
            stmt.executeUpdate();
        }
    }
}
