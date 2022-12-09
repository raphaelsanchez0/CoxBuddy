    package com.example.coxbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

    public class HistoryActivity extends AppCompatActivity {
        ListView sessionNameListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        sessionNameListView = (ListView) findViewById(R.id.history_ListVIew);
        ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(this,R.layout.activity_history,R.id.history_ListVIew,getSessionsFromDir());
        sessionNameListView.setAdapter(arrayAdapter);
    }

    public List<String> getSessionsFromDir(){
        List <String> sessionNames = new ArrayList<>();
        File[] files = getApplicationContext().getFilesDir().listFiles();
        for(int i=0;i<files.length;i++){
            sessionNames.add(files[i].getName());
        }
        return sessionNames;
    }

}