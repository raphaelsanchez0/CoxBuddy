package com.example.coxbuddy;

public class LatLng {
    double Lat;
    double Lng;
    String time;
    boolean trackingEnabled;
    public LatLng(double Lat, double Lng, String time, boolean trackingEnabled){
        this.Lat = Lat;
        this.Lng = Lng;
        this.time = time;
        this.trackingEnabled = trackingEnabled;
    }
    public double getLat(){
        return Lat;
    }

    public double getLng(){
        return Lng;

    }

    public boolean getIfTrackingEnabled(){
        return trackingEnabled;
    }

    public String getTimeStamp(){
        return time;
    }
//HH:mm:ss
    public String toString(){
        return ""+Lat+","+Lng+","+time;
    }

    public int getTimeAsTotalInSeconds(){
        int hours = Integer.parseInt(time.substring(0,2));
        int minutes = Integer.parseInt(time.substring(3,5));
        int seconds = Integer.parseInt(time.substring(6,8));
        return hours*3600 + minutes*60 + seconds;

    }
}


