package com.example.poha.model;

public class Time {
    private int min, sec, millisec;

    public Time(int min, int sec, int millisec) {
        this.min = min;
        this.sec = sec;
        this.millisec = millisec;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public int getMillisec() {
        return millisec;
    }

    public void setMillisec(int millisec) {
        this.millisec = millisec;
    }
}
