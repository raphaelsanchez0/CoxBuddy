package com.example.coxbuddy;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Session {
    LineGraphSeries<DataPoint> series;
    int totalPoints;

    public Session (){
        this.series = new LineGraphSeries<>();
        this.totalPoints=0;
    }


    public LineGraphSeries<DataPoint> getSeries() {
        return series;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setSeries(LineGraphSeries<DataPoint> series) {
        this.series = series;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void incrementTotalPoints(){
        totalPoints++;
    }
}
