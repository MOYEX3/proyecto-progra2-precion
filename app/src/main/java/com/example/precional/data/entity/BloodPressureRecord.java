package com.example.precional.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blood_pressure_records")
public class BloodPressureRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private int age;
    private String gender; // "male", "female", "other"
    private int systolic;
    private int diastolic;
    private long timestamp;
    private String observations;

    // Constructor
    public BloodPressureRecord() {}

    public BloodPressureRecord(String name, int age, String gender, int systolic, int diastolic, long timestamp, String observations) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.timestamp = timestamp;
        this.observations = observations;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }

    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    // Utility methods
    public String getPressureStatus() {
        if (systolic < 120 && diastolic < 80) {
            return "normal";
        } else if (systolic < 140 && diastolic < 90) {
            return "elevated";
        } else {
            return "high";
        }
    }

    public int getPressureStatusColor() {
        String status = getPressureStatus();
        switch (status) {
            case "normal":
                return android.R.color.holo_green_light;
            case "elevated":
                return android.R.color.holo_orange_light;
            case "high":
                return android.R.color.holo_red_light;
            default:
                return android.R.color.darker_gray;
        }
    }
}
