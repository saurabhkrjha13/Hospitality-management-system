package com.internshala.hms.dao;

import com.internshala.hms.db.DatabaseConnector;
import com.internshala.hms.model.Hotel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Hotel entity. Handles all CRUD operations
 * against the "hotels" table.
 */
public class HotelDAO {

    public boolean addHotel(Hotel hotel) {
        String sql = "INSERT INTO hotels (name, location, amenities) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, hotel.getName());
            ps.setString(2, hotel.getLocation());
            ps.setString(3, hotel.getAmenities());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        hotel.setHotelId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateHotel(Hotel hotel) {
        String sql = "UPDATE hotels SET name = ?, location = ?, amenities = ? WHERE hotel_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hotel.getName());
            ps.setString(2, hotel.getLocation());
            ps.setString(3, hotel.getAmenities());
            ps.setInt(4, hotel.getHotelId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteHotel(int hotelId) {
        String sql = "DELETE FROM hotels WHERE hotel_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Hotel getHotelById(int hotelId) {
        String sql = "SELECT * FROM hotels WHERE hotel_id = ?";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
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

    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels ORDER BY hotel_id";
        try (Connection con = DatabaseConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hotels.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hotels;
    }

    private Hotel mapRow(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getInt("hotel_id"),
                rs.getString("name"),
                rs.getString("location"),
                rs.getString("amenities")
        );
    }
}
