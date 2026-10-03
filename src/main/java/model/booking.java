package model;
  
import java.time.LocalDateTime;

public class booking {
    private int bookingId;
    private int customerAccountId;
    private int packageId;
    private String status;
    private LocalDateTime bookingDate;
    private LocalDateTime travelDate;

    public booking(int bookingId, int customerAccountId, int packageId, String status, LocalDateTime bookingDate, LocalDateTime travelDate) {
        this.bookingId = bookingId;
        this.customerAccountId = customerAccountId;
        this.packageId = packageId;
        this.status = status;
        this.bookingDate = bookingDate;
        this.travelDate = travelDate;
    }



    public int getbookingId() {
        return bookingId;
    }

    public void setbookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getcustomerAccountId() {
        return customerAccountId;
    }

    public void setcustomerAccountId(int customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public int getpackageId() {
        return packageId;
    }

    public void setpackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    public LocalDateTime getbookingDate() {
        return bookingDate;
    }

    public void setbookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime gettravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDateTime travelDate) {
        this.travelDate = travelDate;
    }
}