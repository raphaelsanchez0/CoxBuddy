package com.example.coxbuddy;
//https://jdsp.dev/peaks.html
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //declares Button and Textview objects
    private Button startStopButton;
    private Button resetButton;
    private Button historyButton;

    private TextView splitText;
    private TextView totalDistanceTraveledText;
    private TextView strokesPerMinuteText;

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

    private Sensor accelerometer;
    private SensorManager sensorManager;
    private TextView accelerationText;

    private double accelerationCurrentValue;
    private double accelerationPreviousValue;

    private double pointsPlotted = 0.0;

    //Viewport is how the graph is displayed, not the data itself
    private Viewport graphViewport;

    //series are containers for the data itself which work with the Viewports
    private LineGraphSeries<DataPoint> graphSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {});
    //current Series is a pointed used to hold the series currently being created while the timer is enabled



    private Session currentSession;

    private ArrayList<Session> sessions = new ArrayList<Session>();

    private FileWriter currentWriter;
    private FileOutputStream currentFileStream;

    //list of all filenames that store memory of data
    private ArrayList<String> historyIntentMemory = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assigns button, textview and chronometer objects to appropriate IDs
        splitText = findViewById(R.id.split_text);
        strokesPerMinuteText = findViewById(R.id.strokersPerMinute_text);
        totalDistanceTraveledText = findViewById(R.id.totalDistance_text);
        startStopButton = findViewById(R.id.start_stop_button);
        resetButton = findViewById(R.id.reset_button);
        historyButton = findViewById(R.id.history_button);
        chronometer = findViewById(R.id.chronometer_text);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        accelerationText = (TextView) findViewById(R.id.acceleration_text);

        //sets rawGraph as target and configures viewport
        GraphView graph = (GraphView) findViewById(R.id.rawGraph);
        graphViewport = graph.getViewport();
        graphViewport.setScrollable(true);
        graphViewport.setXAxisBoundsManual(true);
        graph.addSeries(graphSeries);

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

                    //creates a new session object to store datapoints created while timer is toggled
                    currentSession = new Session();

                    startOnTimer();

                }else if(startStopButton.getText().equals("Stop")){ //stops timer and distance tracking
                    startStopButton.setText(R.string.start);
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.go_green));
                    resetButton.setEnabled(true);

                    //Adds the current to an Arraylist of sessions
                    sessions.add(currentSession);
                    writeSessionToFile(currentSession);

                    stopOnTimer();
                }
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchHistoryActivity(view);
            }
        });



    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        accelerationCurrentValue = Math.sqrt(x*x + y*y + z*z);
        //may need to wrap changeInAcceleration in Math.abs to avoid negative values
        double changeInAcceleration = accelerationCurrentValue-accelerationPreviousValue;
        accelerationPreviousValue = accelerationCurrentValue;


        //Main-graph pointsplotted increases by one
        pointsPlotted+=1;

        //adds point to graph
        graphSeries.appendData(new DataPoint(pointsPlotted,changeInAcceleration),true,500);

        graphViewport.setMaxX(pointsPlotted);
        graphViewport.setMinX(pointsPlotted-500);

        if (onTimerToggle){

            currentSession.series.appendData(new DataPoint(pointsPlotted,changeInAcceleration),true,500);
            currentSession.totalPoints+=1;


        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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

                                                strokesPerMinuteText.setText(String.valueOf(speed));
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

    private void launchHistoryActivity(View v){
        Intent intent = new Intent(this,HistoryActivity.class);
        intent.putExtra("memory", historyIntentMemory);
        startActivity(intent);

    }

    private String getCurrentDateTime(){
        Calendar calender = Calendar.getInstance();
        String dateString=
                (calender.get(Calendar.HOUR_OF_DAY)) +":"+
                (calender.get(Calendar.MINUTE))+"_"+
                (calender.get(Calendar.MONTH)+1)+"-"+
                calender.get(Calendar.DAY_OF_MONTH)+"-"+
                calender.get(Calendar.YEAR);

        return dateString;

    }

    private void writeSessionToFile(Session session){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path,getCurrentDateTime());

        try{
            FileOutputStream fis = openFileOutput(getCurrentDateTime(),MODE_PRIVATE);
            for(int i =1;i<session.totalPoints;i++){
                double currentDataPoints = session.series.findDataPointAtX(i).getY();
                fis.write((currentDataPoints+"").getBytes());
            }

            fis.close();
            Toast.makeText(getApplicationContext(),"Wrote to file"+getCurrentDateTime(),Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}