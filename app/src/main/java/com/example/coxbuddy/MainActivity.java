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
import java.io.FileNotFoundException;
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


    private Viewport graphViewport;

    //creates series object associated with graph
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {});
    private LineGraphSeries<DataPoint> graphSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {});
    private LineGraphSeries<DataPoint>currentSeries;

    private Session currentSession;

    //creates a generalized list for acceleration values
    private ArrayList<Double> accelerationValues = new ArrayList<Double>();

    private FileWriter currentWriter;
    private FileOutputStream currentFileStream;

    //list of all filenames that store memory of data
    private ArrayList<String> historyIntentMemory = new ArrayList<String>();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);


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

                    currentWriter = createFile();
                    //historyIntentMemory.add(currentFileName);

                    currentSeries = new LineGraphSeries<DataPoint>(new DataPoint[] {});


                    currentSession = new Session();





                    startOnTimer();

                }else if(startStopButton.getText().equals("Stop")){ //stops timer and distance tracking
                    startStopButton.setText(R.string.start);
                    startStopButton.setBackgroundColor(getResources().getColor(R.color.go_green));
                    resetButton.setEnabled(true);
                    closeFile(currentWriter);


                    //adds all points in graphview to a list

                    /*
                    for(int i = 0;i<accelerationValues.size();i++){
                        writeToFile();
                        Log.d("accelerometerValues", accelerationValues.get(i) +"");
                    }
                    */






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


        //adds point to accelerationValues 2d array







        //updates graph

        pointsPlotted+=1;
        //adds point to graph
        graphSeries.appendData(new DataPoint(pointsPlotted,changeInAcceleration),true,500);


        graphViewport.setMaxX(pointsPlotted);
        graphViewport.setMinX(pointsPlotted-500);



        //breaks code, only for debugging
        if (onTimerToggle){
            accelerationValues.add(changeInAcceleration);
            currentSession.series.appendData(new DataPoint(pointsPlotted,changeInAcceleration),true,500);


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
//        String [] files = getApplicationContext().fileList();
//        for(int i =0; i<files.length;i++){
//            Log.d("filelist",files[i]);
//        }



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

//    private void writeToFile (String fileName, String x,String y){
//        String completeString = x +","+y;
//        File filePath = getApplicationContext().getFilesDir();
//        try{
//            FileOutputStream fileWriter = new FileOutputStream(new File(filePath,fileName),true);
//            fileWriter.write(completeString.getBytes());
//            fileWriter.close();
//        }catch (Exception e){
//            Toast.makeText(getApplicationContext(),"File Write Failed",Toast.LENGTH_SHORT);
//            e.printStackTrace();
//        }
//    }

    private void launchHistoryActivity(View v){
        Intent intent = new Intent(this,HistoryActivity.class);
        intent.putExtra("accelerometerValues",accelerationValues);
        intent.putExtra("memory", historyIntentMemory);
        startActivity(intent);

    }

    private String getCurrentDateTime(){
        Calendar calender = Calendar.getInstance();
        String dateString=
                (calender.get(Calendar.HOUR_OF_DAY)) +":"+
                (calender.get(Calendar.MINUTE))+"_"+
                (calender.get(Calendar.MONTH)+1)+"/"+
                calender.get(Calendar.DAY_OF_MONTH)+"/"+
                calender.get(Calendar.YEAR);

        return dateString;

    }

//    public void save(String fileName){
//        FileOutputStream fos = null;
//
//        try {
//            fos = openFileOutput(fileName,MODE_PRIVATE);
//            fos.write("test".getBytes());
//            Toast.makeText(this, "Saved to " + getFilesDir()+ "/" + fileName, Toast.LENGTH_LONG).show();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(fos != null){
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }

    public FileWriter createFile(){
        File file = Environment.getExternalStorageDirectory();
        String strFilePath = file.getAbsolutePath()+getCurrentDateTime();
//        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
//        fileName += "/"+getCurrentDateTime()+".txt";

        try{
            currentFileStream = new FileOutputStream(strFilePath);
            FileWriter writer = new FileWriter(currentFileStream.getFD());
            return writer;
        }catch (IOException e) {
            e.printStackTrace();
            Log.d("createFileException","createFileException");
            return null;
        }

    }

    public void writeToFile(FileWriter writer, String x,String y){
        try{
            writer.write(x +","+y);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void closeFile(FileWriter writer){
        try{
            writer.close();
            currentFileStream.getFD().sync();
            currentFileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

//    public void saveSeries(LineGraphSeries series){
//        File dir = new File();
//        String fileName = getCurrentDateTime();
//        try{
//            FileOutputStream file =openFileOutput(getCurrentDateTime(),MODE_PRIVATE);
//            OutputStreamWriter outputStreamWriter =  new OutputStreamWriter(file);
//            for(int i =0;i<currentSeries.)
//
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


}