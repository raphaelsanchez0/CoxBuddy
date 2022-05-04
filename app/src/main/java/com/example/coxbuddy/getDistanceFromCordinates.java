package com.example.coxbuddy;

public class getDistanceFromCordinates {
    public static Double gpsDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371;
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c*1000;
        return distance;
    }

    public static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
