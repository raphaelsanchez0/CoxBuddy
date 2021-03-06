package com.example.coxbuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //declares Button and Textview objects
    private Button startStopButton;
    private Button resetButton;

    private TextView splitText;
    private TextView totalDistanceTraveledText;
    private TextView strokerPerMinuteText;

    private LocationRequest locationRequest;

    //location log is where location data is stored chronologically
    private ArrayList<LatLng> locationLog = new ArrayList<>();
    private int totalDistanceTraveled = 0;

    //declares standard and fastest location refresh intervals in seconds
    private final int fastestInterval = 1;
    private final int standardInterval =2;

    private boolean onTimerToggle = false; //when true, distance traveled is tracked and timer is started.
    private int locationLogLenAtPause;

    private Chronometer chronometer;
    private long lastPause;
    private final float[] locationResults = new float[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assigns button, textview and chronometer objects to appropriate IDs
        splitText = findViewById(R.id.split_text);
        strokerPerMinuteText = findViewById(R.id.strokersPerMinute_text);
        totalDistanceTraveledText = findViewById(R.id.totalDistance_text);
        startStopButton = findViewById(R.id.start_stop_button);
        resetButton = findViewById(R.id.reset_button);
        chronometer = findViewById(R.id.chronometer_text);

        //creates location request objects and sets values to them.
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(standardInterval * 1000)
            .setFastestInterval(fastestInterval *1000);
        //after location request has been created, location data is called to start tracking user location
        getLocationData();



        //reset button only enabled when timer is stopped. Eventually make reset button hold to reset
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalDistanceTraveled = 0;
                totalDistanceTraveledText.setText(String.valueOf(totalDistanceTraveled));
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                lastPause = 0;

            }
        });

        //a toggle button for the timer and distance. Switches toggles between start and stop
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startStopButton.getText().equals("Start")){  //starts timer and distance tracking
                    startStopButton.setText(R.string.stop);
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.stop_red));
                    resetButton.setEnabled(false);

                    startOnTimer();

                }else if(startStopButton.getText().equals("Stop")){ //stops timer and distance tracking
                    startStopButton.setText(R.string.start);
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.go_green));
                    resetButton.setEnabled(true);

                    stopOnTimer();
                }
            }
        });

    }

    private void startOnTimer() {
        onTimerToggle = true;
        locationLogLenAtPause = locationLog.size();

        //either resumes or resets time based on last pause
        if (lastPause != 0){
            chronometer.setBase(chronometer.getBase()+SystemClock.elapsedRealtime()-lastPause);
        }
        else{
            chronometer.setBase(SystemClock.elapsedRealtime());
        }
        chronometer.start();
    }
    private void stopOnTimer(){
        onTimerToggle = false;
        chronometer.stop();
        lastPause = SystemClock.elapsedRealtime();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getLocationData();

                }else {

                    turnOnGPS();
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getLocationData();
            }
        }
    }

    private void getLocationData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        List<Location> locationData = locationResult.getLocations();
                                        int index = locationResult.getLocations().size() - 1;
                                        Location currentLocation = locationResult.getLocations().get(index);




                                        //LocationResult = locationResult.getLocations()
                                        double latitude = currentLocation.getLatitude();
                                        double longitude = currentLocation.getLongitude();




                                        float speed = currentLocation.getSpeed();
                                        double split = SplitFormater.getSplit(speed);

                                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                                        locationLog.add(new LatLng(latitude,longitude,currentTime, onTimerToggle));

                                            if (locationLog.size()-2>=locationLogLenAtPause) { //gets two most recent locations points from location log
                                                double lat1 = locationLog.get(locationLog.size() - 2).getLat();
                                                double lng1 = locationLog.get(locationLog.size() - 2).getLng();
                                                double lat2 = locationLog.get(locationLog.size() - 1).getLat();
                                                double lng2 = locationLog.get(locationLog.size() - 1).getLng();

                                                if (onTimerToggle) { //if onTimerToggled enabled, distance between two point will be calculated and added to totalDistanceTraveled
                                                    Location.distanceBetween(lat1,lng1,lat2,lng2, locationResults);
                                                    totalDistanceTraveled += locationResults[0];

                                                }

                                                strokerPerMinuteText.setText(String.valueOf(speed));
                                                splitText.setText(SplitFormater.FormatToSplitString(split));
                                                totalDistanceTraveledText.setText(String.valueOf(totalDistanceTraveled));
                                            }

                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }


}