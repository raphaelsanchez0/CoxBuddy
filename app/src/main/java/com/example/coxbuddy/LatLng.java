package com.example.coxbuddy;

public class LatLng {
    double Lat;
    double Lng;
    String time;
    public LatLng(double Lat, double Lng, String time){
        this.Lat = Lat;
        this.Lng = Lng;
        this.time = time;
    }
    public double getLat(){
        return Lat;
    }

    public double getLng(){
        return Lng;

    }

    public String toString(){
        return ""+Lat+","+Lng+","+time;
    }

}


