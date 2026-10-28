package com.internshala.hms.model;

/**
 * Entity class representing a Room belonging to a Hotel.
 */
public class Room {
    private int roomId;
    private int hotelId;
    private String roomNumber;
    private String type;       // e.g. SINGLE, DOUBLE, SUITE
    private double price;      // price per night
    private String status;     // AVAILABLE, BOOKED

    public Room() {
    }

    public Room(int hotelId, String roomNumber, String type, double price, String status) {
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.status = status;
    }

    public Room(int roomId, int hotelId, String roomNumber, String type, double price, String status) {
        this.roomId = roomId;
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return roomId + " - Room " + roomNumber + " (" + type + ")";
    }
}
