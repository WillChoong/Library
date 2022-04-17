package com.example.prototype;

public class BookData {

    public String BookName;
    public String CallNumber;
    public String Floor;
    public String Rak;
    public String View;

    public String getBookName() {
        return BookName;
    }

    public String getCallNumber() {
        return CallNumber;
    }

    public String getFloor() {
        return Floor;
    }

    public String getRak() {
        return Rak;
    }

    public String getView() {
        return View;
    }

    public BookData(String BookName,String CallNumber,String Floor,String Rak,String View)
    {
        this.BookName=BookName;
        this.CallNumber=CallNumber;
        this.Floor=Floor;
        this.Rak=Rak;
        this.View=View;
    }
}