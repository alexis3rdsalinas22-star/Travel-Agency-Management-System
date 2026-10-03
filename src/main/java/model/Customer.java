package model;

public class Customer {
    private int customerId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String sex;
    private int age;

    public Customer(int customerId, String firstName, String middleName, String lastName, String email, String sex, int age){
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.email = email;
        this.sex = sex;
        this.age = age;
    }
}


/*

CREATE TABLE Customer (
  customerID INT NOT NULL AUTO_INCREMENT,
  firstName  VARCHAR(45) NOT NULL,
  lastName   VARCHAR(45) NOT NULL,
  middleName VARCHAR(45) NULL,
  email      VARCHAR(45) NOT NULL,
  sex        ENUM('Male','Female') NULL,
  age        INT NULL,
  PRIMARY KEY (customerID),
  UNIQUE KEY uq_customer_email (email)
) ENGINE=InnoDB;

*/