package com.example.coxbuddy;

public class SplitFormater {

    public static double getSplit(float speed) {
        final int splitDistance = 500;
        double splitInSeconds = splitDistance/speed;
        return splitInSeconds;

    }

    public static String FormatToSplitString(double splitInSeconds){
        int splitInSecondsInt = (int)Math.round(splitInSeconds);
        int minutes = splitInSecondsInt/60;
        int seconds = splitInSecondsInt % 60;
        if(seconds<10){
            return minutes + ":"+seconds+"0";
        }else{
            return minutes + ":"+seconds;
        }

    }
}