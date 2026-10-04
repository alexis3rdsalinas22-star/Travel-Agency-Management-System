package model;
   
public class AdminAccount {
    private int adminAccountId;
    private String username;
    private String password;
    private String role;

    public AdminAccount(int adminAccountId, String username, String password, String role) {
        this.adminAccountId = adminAccountId;
        this.username = username;
        this.password = password;
        this.role = role;
    }



    public int getadminAccountId() {
        return adminAccountId;
    }

    public void setadminAccountId(int adminAccountId) {
        this.adminAccountId = adminAccountId;
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

    public String getrole() {
        return role;
    }

    public void setrole(String role) {
        this.role = role;
    }
}