package com.example.prototype;

public class Reservation {

    private Boolean checkIn;
    private Boolean checkOut;
    private String userId;
    private String date;
    private String time;
    private String floor;
    private String seatId;

    public Reservation(Boolean checkIn, Boolean checkOut, String userId, String date, String time, String floor, String seatId) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.floor = floor;
        this.seatId = seatId;
    }

    public Boolean getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Boolean checkIn) {
        this.checkIn = checkIn;
    }

    public Boolean getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Boolean checkOut) {
        this.checkOut = checkOut;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }
}
