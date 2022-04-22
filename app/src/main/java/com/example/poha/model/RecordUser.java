package com.example.poha.model;

public class RecordUser {
    private String username;
    private double distance;
    private double speed;
    private Time time;

    public RecordUser() {
    }

    public RecordUser(String username, double distance, Time time, double speed) {
        this.username = username;
        this.distance = distance;
        this.time = time;
        this.speed = speed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
