package model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int bookingId;
    private LocalDateTime date;
    private double amount;
    private String method;
    private String source;

    public Payment(int paymentId, int bookingId, LocalDateTime date, double amount, String method, String source) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.date = date;
        this.amount = amount;
        this.method = method;
        this.source = source;
    }

 
   
    public int getpaymentId() {
        return paymentId;
    }

    public void setpaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getbookingId() {
        return bookingId;
    }

    public void setbookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getdate() {
        return date;
    }

    public void setdate(LocalDateTime date) {
        this.date = date;
    }

    public double getamount() {
        return amount;
    }

    public void setamount(double amount) {
        this.amount = amount;
    }

    public String getmethod() {
        return method;
    }

    public void setmethod(String method) {
        this.method = method;
    }

    public String getsource() {
        return source;
    }

    public void setsource(String source) {
        this.source = source;
    }
}