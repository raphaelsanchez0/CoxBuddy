    package com.example.coxbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

    public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);




    }

    public void getSessionsFromDir(){
        File[] files = getApplicationContext().getFilesDir().listFiles();
        for(int i=0;i<files.length;i++){

        }
    }

}