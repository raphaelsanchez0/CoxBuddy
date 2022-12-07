    package com.example.coxbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.util.ArrayList;

    public class HistoryActivity extends AppCompatActivity {

    private ArrayList<Double> accelerationValues;
    private ArrayList<String> memory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        try{
            accelerationValues = (ArrayList<Double>)getIntent().getSerializableExtra("accelerometerValues");

            memory = (ArrayList<String>) getIntent().getSerializableExtra("memory");

            //delete later
//            for(int i = 0;i<accelerationValues.size();i++){
//                Log.d("accelerometerValues", accelerationValues.get(i) +"");
//            }

            FileInputStream fis = getApplicationContext().openFileInput(memory.get(0));

        }catch(Exception e) {
            Log.d("historyException","Could not generate history from event.");
        }







    }

}