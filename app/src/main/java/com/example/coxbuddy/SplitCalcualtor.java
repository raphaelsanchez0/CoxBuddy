package com.example.coxbuddy;

import android.util.Log;

public class SplitCalcualtor {
    public static double getSplit(double lat1, double lon1, double lat2, double lon2, int time) {
        final int splitDistance = 500;
        double distance = getDistanceFromCordinates.gpsDistance(lat1, lon1, lat2, lon2);
        double splitInSeconds = (((double) time / distance) * splitDistance);
        Log.d("currentSplit","Split: "+splitInSeconds);
        Log.d("distance","Distance: "+distance);
        return splitInSeconds;

    }

    public static String FormatToSplitString(double splitInSeconds){
        int splitInSecondsInt = (int)Math.round(splitInSeconds);
        int minutes = splitInSecondsInt/60;
        int seconds = splitInSecondsInt % 60;
        return minutes + ":"+seconds;
    }
}