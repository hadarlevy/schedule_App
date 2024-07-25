package com.example.schedule_application;

import java.util.Date;

public class Shift {
    private String email;
    private Date date;
    private String option;

    public Shift() {
        // Required empty constructor
    }

    public Shift(String email, Date date, String option) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
