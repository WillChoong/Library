package com.example.prototype;

public class Reservation {

    Boolean checkIn;
    Boolean checkOut;
    String UserID;
    String Date;
    String Time;
    String Floor;
    String SeatID;

    public Reservation()
    {

    }

    public Reservation(Boolean checkIn, Boolean checkOut, String userId, String date, String time, String floor, String seatId) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.UserID = userId;
        this.Date = date;
        this.Time = time;
        this.Floor = floor;
        this.SeatID = seatId;
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

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        this.UserID = userID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        this.Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        this.Time = time;
    }

    public String getFloor() {
        return Floor;
    }

    public void setFloor(String floor) {
        this.Floor = floor;
    }

    public String getSeatID() {
        return SeatID;
    }

    public void setSeatID(String seatID) {
        this.SeatID = seatID;
    }
}
