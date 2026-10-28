package com.internshala.hms.dao;

import com.internshala.hms.db.DatabaseConnector;
import com.internshala.hms.model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Guest entity. Handles all CRUD operations
 * against the "guests" table.
 */
public class GuestDAO {

    public boolean addGuest(Guest guest) {
        String sql = "INSERT INTO guests (name, email, phone) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guest.getName());
            ps.setString(2, guest.getEmail());
            ps.setString(3, guest.getPhone());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        guest.setGuestId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateGuest(Guest guest) {
        String sql = "UPDATE guests SET name = ?, email = ?, phone = ? WHERE guest_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, guest.getName());
            ps.setString(2, guest.getEmail());
            ps.setString(3, guest.getPhone());
            ps.setInt(4, guest.getGuestId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteGuest(int guestId) {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, guestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Guest getGuestById(int guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guests ORDER BY guest_id";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                guests.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guests;
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        return new Guest(
                rs.getInt("guest_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}
