package com.example.coxbuddy;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Session {

    ArrayList<Double> period;
    int totalPoints;

    public Session (){
        this.period = new ArrayList<>();
        this.totalPoints=0;
    }

    public void addPoint(Double point){
        period.add(point);
        totalPoints++;
    }

    public Double getPointAtX(int x){
        return period.get(x);
    }



    public ArrayList<Double> getPeriod() {
        return period;
    }

    public void setPeriod(ArrayList<Double> period) {
        this.period = period;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
