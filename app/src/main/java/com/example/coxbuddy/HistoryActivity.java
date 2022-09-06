    package com.example.coxbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

    public class HistoryActivity extends AppCompatActivity {

    private ArrayList<Double> accelerationValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        try{
            accelerationValues = (ArrayList<Double>)getIntent().getSerializableExtra("accelerometerValues");
            for(int i = 0;i<accelerationValues.size();i++){
                Log.d("accelerometerValues", accelerationValues.get(i) +"");
            }

        }catch(Exception e){
            ;
            //Do nothing
        }



    }

}