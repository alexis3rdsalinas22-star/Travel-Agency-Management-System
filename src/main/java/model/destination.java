package model;

public class destination {
    private int destinationId;
    private String name;
    private String country;
    private String description;
    private String status;

    public destination(int destinationId, String name, String country,String description, String status) {                     
        this.destinationId = destinationId;
        this.name = name;
        this.country = country;
        this.description = description;
        this.status = status;
    }
   
   public int getdestinationId() {
        return destinationId;
    }

    public void setdestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getcountry() {
        return country;
    }

    public void setcountry(String country) {
        this.country = country;
    }

    public String getdescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }
}  