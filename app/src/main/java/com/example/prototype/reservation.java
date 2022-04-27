package com.example.prototype;

import android.content.res.ColorStateList;

public class reservation {

    private String userID;
    private String SeatID;
    private String Date;
    private String Time;
    private String Floor;
    private Boolean CheckIn;
    private Boolean CheckOut;
    private int color;

    public reservation(){ }

    public reservation(String UserID,String SeatID,String Date,String Time,String Floor,Boolean CheckIn,Boolean CheckOut,int color)
    {
        this.userID = UserID;
        this.SeatID = SeatID;
        this.Date = Date;
        this.Time = Time;
        this.Floor = Floor;
        this.CheckIn = CheckIn;
        this.CheckOut = CheckOut;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSeatID() {
        return SeatID;
    }

    public void setSeatID(String seatID) {
        SeatID = seatID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getFloor() {
        return Floor;
    }

    public void setFloor(String floor) {
        Floor = floor;
    }

    public Boolean getCheckIn() {
        return CheckIn;
    }

    public void setCheckIn(Boolean checkIn) {
        CheckIn = checkIn;
    }

    public Boolean getCheckOut() {
        return CheckOut;
    }

    public void setCheckOut(Boolean checkOut) {
        CheckOut = checkOut;
    }
}
