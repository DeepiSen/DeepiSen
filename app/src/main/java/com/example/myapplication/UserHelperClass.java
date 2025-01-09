package com.example.myapplication;

public class UserHelperClass {
    String username, email,phone,password;


    public UserHelperClass( String username, String email, String phone,String password) {
        this.username = username;
        this.email = email;
        this.phone= phone;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserPhone() {
        return phone;
    }

    public void setUserPhone(String phone) {
        this.phone = phone;
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
