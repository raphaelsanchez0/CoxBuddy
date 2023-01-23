package com.example.coxbuddy;

import android.os.Parcel;
import android.os.Parcelable;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Session {

    ArrayList<Double> period;
    ArrayList<SessionSecond> sessionData; //arraylist of split, speed, etc. at every second in session
    int totalPoints;
    LineGraphSeries<DataPoint> graphSeries;

    public Session (){
        this.period = new ArrayList<>();
        this.sessionData = new ArrayList<>();
        this.totalPoints=0;
        this.graphSeries = null;
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

    public LineGraphSeries<DataPoint> getGraphSeries() {
        return graphSeries;
    }

    public void setGraphSeries(LineGraphSeries<DataPoint> graphSeries) {
        this.graphSeries = graphSeries;
    }

    public void addData(SessionSecond sessionSecond){
        this.sessionData.add(sessionSecond);
    }


}
