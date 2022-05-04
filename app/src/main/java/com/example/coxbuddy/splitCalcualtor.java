package com.example.coxbuddy;

import android.util.Log;

public class splitCalcualtor {
    public static double getSplit(double lat1, double lon1, double lat2, double lon2, int time) {
        final int splitDistance = 500;
        double distance = getDistanceFromCordinates.gpsDistance(lat1, lon1, lat2, lon2);
        double splitInSeconds = (((double) time / distance) * splitDistance);
        Log.d("currentSplit","Split: "+splitInSeconds);
        Log.d("distance","Distance: "+distance);
        return splitInSeconds;

    }
}