package com.example.coxbuddy;

public class SessionSecond {
    String split;
    String distance;
    String speed;
    String chrono;
    int second;

    public SessionSecond(String split, String distance, String speed, String chrono, int second) {
        this.split = split;
        this.distance = distance;
        this.speed = speed;
        this.chrono = chrono;
        this.second = second;
    }




    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getChrono() {
        return chrono;
    }

    public void setChrono(String chrono) {
        this.chrono = chrono;
    }
}
