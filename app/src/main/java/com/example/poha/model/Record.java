package com.example.poha.model;

public class Record {
    private double distance;
    private double speed;
    private Time time;

    public Record() {
    }

    public Record(double distance, Time time, double speed) {
        this.distance = distance;
        this.time = time;
        this.speed = speed;
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
