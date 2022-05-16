package com.example.coxbuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;


    private Button startStopButton;
    private Button resetButton;
    private TextView splitText;
    private TextView totalDistanceTraveledText;

    private LocationRequest locationRequest;

    private ArrayList<LatLng> locationLog = new ArrayList<>();
    private int totalDistanceTraveled = 0;

    private final int locationRefreshDelay = 5;

    private boolean trackingToggled = false;
    private int locationLogLenAtPause;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //assigns button variables IDs
        statusText = findViewById(R.id.status_Text);
        splitText = findViewById(R.id.split_text);
        totalDistanceTraveledText = findViewById(R.id.totalDistance_text);

        startStopButton = findViewById(R.id.start_stop_button);
        resetButton = findViewById(R.id.reset_button);



        //creates location request objeccts and sets values to them.
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(8000)
            .setFastestInterval(locationRefreshDelay*1000);


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalDistanceTraveled = 0;
                totalDistanceTraveledText.setText(String.valueOf(totalDistanceTraveled));

            }
        });

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(startStopButton.getText().equals("Start")){
                    startStopButton.setText("Stop");

                    startStopButton.setBackgroundColor(getResources().getColor(R.color.stop_red));


                    trackingToggled = true;
                    startLocationUpdates();
                    getCurrentLocation();
                    locationLogLenAtPause = locationLog.size();

                    //turn on tracking


                }else if(startStopButton.getText().equals("Stop")){
                    startStopButton.setText("Start");
                    trackingToggled = false;
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.go_green));

                    stopLocationUpdates();

                }
                Log.d("onOrOff",trackingToggled+"");
            }
        });


        //runs central location function
        //getCurrentLocation();



    }

    private void startLocationUpdates() {
        statusText.setText("Location is being tracked");
    }
    private void stopLocationUpdates(){
        statusText.setText("Location is not being tracked");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

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

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    //LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            //.removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){


                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        Log.d("onOrOff",trackingToggled+"");
                                        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                                        locationLog.add(new LatLng(latitude,longitude,currentTime));

                                            if (locationLog.size()-2>=locationLogLenAtPause) {
                                                double lat1 = locationLog.get(locationLog.size() - 2).getLat();
                                                double lng1 = locationLog.get(locationLog.size() - 2).getLng();
                                                double lat2 = locationLog.get(locationLog.size() - 1).getLat();
                                                double lng2 = locationLog.get(locationLog.size() - 1).getLng();
                                                int totalTime1 = locationLog.get(locationLog.size() - 2).getTimeAsTotalInSeconds();
                                                int totalTime2 = locationLog.get(locationLog.size() - 1).getTimeAsTotalInSeconds();
                                                int totalTimeDiff = totalTime2 - totalTime1;
                                                double split = SplitCalcualtor.getSplit(lat1, lng1, lat2, lng2, totalTimeDiff);

                                                if (trackingToggled == true) {
                                                    totalDistanceTraveled += getDistanceFromCordinates.gpsDistance(lat1, lng1, lat2, lng2);
                                                }
                                                Log.d("LocationGrabber", split + "");
                                                Log.d("totalDistanceTraveled", totalDistanceTraveled + "");
                                                Log.d("totalDistanceTraveled", totalDistanceTraveled + "");
                                                
                                                splitText.setText(SplitCalcualtor.FormatToSplitString(split));
                                                totalDistanceTraveledText.setText(String.valueOf(totalDistanceTraveled));
                                                if(trackingToggled == false){
                                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                                    .removeLocationUpdates(this);

                                                }
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