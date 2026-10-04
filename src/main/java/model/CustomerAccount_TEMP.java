package model;

import java.time.LocalDateTime;

public class CustomerAccount_TEMP {
    private int customerAccountId;
    private int customerId;          
    private String username;
    private String password;
    private String status;           
    private LocalDateTime createdAt;

    public CustomerAccount_TEMP(int customerAccountId, int customerId, String username, String password, String status, LocalDateTime createdAt) {
        this.customerAccountId = customerAccountId;
        this.customerId = customerId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    public int getcustomerAccountId() {
        return customerAccountId;
    }

    public void setcustomerAccountId(int customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public int getcustomerId() {
        return customerId;
    }

    public void setcustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getpassword() {
        return password;
    }

    public void setpassword(String password) {
        this.password = password;
    }

    public String getstatus() {
        return status;
    }
 
    public void setstatus(String status) {
        this.status = status;
    }

    public LocalDateTime getcreatedAt() {
        return createdAt;
    }

    public void setcreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}  
