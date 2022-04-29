package com.example.coxbuddy;

public class split {
    public static Double getSplit(double lat1, double lon1, double lat2, double lon2, double time) {
        final int splitDistance = 500;
        double distance = getDistanceFromCordinates.gpsDistance(lat1, lon1, lat2, lon2);
        Double splitInSeconds = ((double) time / distance) * splitDistance;
        return splitInSeconds;

    }
}