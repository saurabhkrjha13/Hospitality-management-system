package com.internshala.hms.dao;

import com.internshala.hms.db.DatabaseConnector;
import com.internshala.hms.model.Reservation;
import com.internshala.hms.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Data Access Object for the Reservation entity. Handles CRUD operations
 * against the "reservations" table plus the reservation-cost business logic.
 */
public class ReservationDAO {

    private final RoomDAO roomDAO = new RoomDAO();

    public boolean addReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, total_cost) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reservation.getGuestId());
            ps.setInt(2, reservation.getRoomId());
            ps.setDate(3, reservation.getCheckInDate());
            ps.setDate(4, reservation.getCheckOutDate());
            ps.setDouble(5, reservation.getTotalCost());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        reservation.setReservationId(keys.getInt(1));
                    }
                }
                // mark the room as booked once reserved
                Room room = roomDAO.getRoomById(reservation.getRoomId());
                if (room != null) {
                    room.setStatus("BOOKED");
                    roomDAO.updateRoom(room);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET guest_id = ?, room_id = ?, check_in_date = ?, " +
                "check_out_date = ?, total_cost = ? WHERE reservation_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservation.getGuestId());
            ps.setInt(2, reservation.getRoomId());
            ps.setDate(3, reservation.getCheckInDate());
            ps.setDate(4, reservation.getCheckOutDate());
            ps.setDouble(5, reservation.getTotalCost());
            ps.setInt(6, reservation.getReservationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Reservation getReservationById(int reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
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

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY reservation_id";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * Business logic: number of nights * the room's price per night.
     */
    public double calculateReservationCost(int roomId, java.sql.Date checkIn, java.sql.Date checkOut) {
        Room room = roomDAO.getRoomById(roomId);
        if (room == null) {
            return 0.0;
        }
        long nights = TimeUnit.MILLISECONDS.toDays(checkOut.getTime() - checkIn.getTime());
        if (nights <= 0) {
            nights = 1; // minimum one night
        }
        return nights * room.getPrice();
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("guest_id"),
                rs.getInt("room_id"),
                rs.getDate("check_in_date"),
                rs.getDate("check_out_date"),
                rs.getDouble("total_cost")
        );
    }
}
