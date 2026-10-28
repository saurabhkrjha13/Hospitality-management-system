package com.internshala.hms.dao;

import com.internshala.hms.db.DatabaseConnector;
import com.internshala.hms.model.Room;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Room entity. Handles CRUD operations against
 * the "rooms" table plus business logic for availability checks.
 */
public class RoomDAO {

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (hotel_id, room_number, type, price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, room.getHotelId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getType());
            ps.setDouble(4, room.getPrice());
            ps.setString(5, room.getStatus());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        room.setRoomId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET hotel_id = ?, room_number = ?, type = ?, price = ?, status = ? WHERE room_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, room.getHotelId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getType());
            ps.setDouble(4, room.getPrice());
            ps.setString(5, room.getStatus());
            ps.setInt(6, room.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
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

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_id";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    /**
     * Business logic: checks whether a room is free for the given date
     * range by making sure no existing reservation for that room overlaps
     * with the requested check-in/check-out dates.
     */
    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut) {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE room_id = ? AND NOT (check_out_date <= ? OR check_in_date >= ?)";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, checkIn);
            ps.setDate(3, checkOut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getInt("hotel_id"),
                rs.getString("room_number"),
                rs.getString("type"),
                rs.getDouble("price"),
                rs.getString("status")
        );
    }
}
