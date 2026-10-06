package model;

public class Customer_TEMP1 {
    private int customerId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String sex;
    private int age;

    public Customer_TEMP1(int customerId, String firstName, String lastName,String middleName, String email, String sex, int age) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.email = email;
        this.sex = sex;
        this.age = age;
    }
     
     public int getcustomerId() {
        return customerId;
    }

    public void setcustomerId(int customerId) {
        this.customerId = customerId;
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

    public String getmiddleName() {
        return middleName;
    }

    public void setmiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getemail() {
        return email;
    }
  
    public void setemail(String email) {
        this.email = email;
    }

    public String getsex() {
        return sex;
    }

    public void setsex(String sex) {
        this.sex = sex;
    }

    public int getage() {
        return age;
    }

    public void setage(int age) {
        this.age = age;
    }
}