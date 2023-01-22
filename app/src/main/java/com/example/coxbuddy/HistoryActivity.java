    package com.example.coxbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

    public class HistoryActivity extends AppCompatActivity {
        ListView sessionNameListView;
        ArrayAdapter<String> arrayAdapter;

        ArrayList<String> sessionsArrayList;
        boolean firstHistoryLaunch = true;


        @Override
        protected void onResume() {
            super.onResume();
            //updateSessionsListview();
            Toast.makeText(getApplicationContext(),"resumed",Toast.LENGTH_SHORT).show();
            updateListview();
        }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sessionsArrayList = getSessionsFromDir();

        sessionNameListView = (ListView) findViewById(R.id.history_ListVIew);
        arrayAdapter = new ArrayAdapter<String> (this, R.layout.activity_list_view, R.id.textView,sessionsArrayList);
        sessionNameListView.setAdapter(arrayAdapter);


    }

    public ArrayList<String> getSessionsFromDir(){
        ArrayList <String> sessionNames = new ArrayList<>();
        File[] files = getApplicationContext().getFilesDir().listFiles();
        for(int i=0;i<files.length;i++){
            sessionNames.add(files[i].getName());
        }
        return sessionNames;
    }


        public void updateListview(){
            sessionsArrayList.clear();
            File[] files = getApplicationContext().getFilesDir().listFiles();
            for(int i=0;i<files.length;i++){
                sessionsArrayList.add(files[i].getName());
            }
            arrayAdapter.notifyDataSetChanged();
        }


    }
