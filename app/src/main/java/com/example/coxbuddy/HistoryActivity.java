    package com.example.coxbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

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

        sessionsArrayList = getSessionsNamesFromDir();

        sessionNameListView = (ListView) findViewById(R.id.history_ListVIew);
        arrayAdapter = new ArrayAdapter<String> (this, R.layout.activity_list_view, R.id.textView,sessionsArrayList);
        sessionNameListView.setAdapter(arrayAdapter);


    }

    public ArrayList<String> getSessionsNamesFromDir(){
        ArrayList <String> sessionNames = new ArrayList<>();
        File[] files = getApplicationContext().getFilesDir().listFiles();
        for(int i=0;i<files.length;i++){
            sessionNames.add(files[i].getName());
        }
        return sessionNames;
    }


        public void updateListview(){
            sessionsArrayList.clear();
            sessionsArrayList.addAll(getSessionsNamesFromDir());
            arrayAdapter.notifyDataSetChanged();
        }


    }
