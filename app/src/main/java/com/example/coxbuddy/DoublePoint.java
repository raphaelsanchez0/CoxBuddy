package com.example.coxbuddy;

import java.sql.Array;
import java.util.AbstractQueue;
import java.util.ArrayList;

public class DoublePoint {
    double x;
    double y;

    public DoublePoint(double x,double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public ArrayList<Double> getPoint(){
        ArrayList<Double> point = new ArrayList<Double>();
        point.add(x);
        point.add(y);
        return (point);
    }

    public String getPointString(){
        return x +", "+y;
    }
}
