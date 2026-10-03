package model;
 

public class travelPackage {
    private int packageId;
    private int destinationId;
    private String name;  
    private String description;
    private double pricePerTraveller;
    private String status;
    private int minTraveller;
    private int maxTraveller;

    public travelPackage(int packageId, int destinationId, String name, String description, double pricePerTraveller, String status, int minTraveller, int maxTraveller) {
        this.packageId = packageId;
        this.destinationId = destinationId;
        this.name = name;
        this.description = description;
        this.pricePerTraveller = pricePerTraveller;
        this.status = status;
        this.minTraveller = minTraveller;
        this.maxTraveller = maxTraveller;
    }

 

    public int getpackageId() {
        return packageId;
    }

    public void setpackageId(int packageId) {
        this.packageId = packageId;
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

    public String getdescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public double getpricePerTraveller() {
        return pricePerTraveller;
    }

    public void setpricePerTraveller(double pricePerTraveller) {
        this.pricePerTraveller = pricePerTraveller;
    }

    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    public int getminTraveller() {
        return minTraveller;
    }

    public void setminTraveller(int minTraveller) {
        this.minTraveller = minTraveller;
    }

    public int getmaxTraveller() {
        return maxTraveller;
    }

    public void setmaxTraveller(int maxTraveller) {
        this.maxTraveller = maxTraveller;
    }
}