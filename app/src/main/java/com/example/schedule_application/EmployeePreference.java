package com.example.schedule_application;

import java.util.List;

public class EmployeePreference {
    private String email;
    private List<String> possibleDays;
    private List<String> preferredDays;

    public EmployeePreference(String email, List<String> possibleDays, List<String> preferredDays) {
        this.email = email;
        this.possibleDays = possibleDays;
        this.preferredDays = preferredDays;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public List<String> getPossibleDays() {
        return possibleDays;
    }

    public List<String> getPreferredDays() {
        return preferredDays;
    }

    @Override
    public String toString() {
        return "EmployeePreference{" +
                "email='" + email + '\'' +
                ", possibleDays=" + possibleDays +
                ", preferredDays=" + preferredDays +
                '}';
    }
}
