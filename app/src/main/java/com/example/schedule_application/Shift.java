// src/com/example/schedule_application/Shift.java
package com.example.schedule_application;

public class Shift {
    private String email;
    private String date;
    private String option;

    public Shift() {
        // Required empty constructor
    }

    public Shift(String email, String date, String option) {
        this.email = email;
        this.date = date;
        this.option = option;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
