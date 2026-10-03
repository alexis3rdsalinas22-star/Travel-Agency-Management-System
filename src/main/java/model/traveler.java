package model;
 
import java.time.LocalDate;

public class traveler {
    private int travelerId;
    private int bookingId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String contactNumber;

    public traveler(int travelerId, int bookingId, String firstName, String lastName, LocalDate dateOfBirth, String contactNumber) {
        this.travelerId = travelerId;
        this.bookingId = bookingId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.contactNumber = contactNumber;
    }



    public int gettravelerId() {
        return travelerId;
    }

    public void settravelerId(int travelerId) {
        this.travelerId = travelerId;
    }

    public int getbookingId() {
        return bookingId;
    }

    public void setbookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getfirstName() {
        return firstName;
    }

    public void setfirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getlastName() {
        return lastName;
    }

    public void setlastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getdateOfBirth() {
        return dateOfBirth;
    }

    public void setdateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getcontactNumber() {
        return contactNumber;
    }

    public void setcontactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}