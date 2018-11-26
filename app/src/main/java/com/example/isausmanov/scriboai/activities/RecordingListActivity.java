package com.example.isausmanov.scriboai.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.RecordingListAdapter;

import java.io.File;
import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {
    // RecordsList data
    private ArrayList<RecordingDataModel> dataModels;
    private ListView listView;
    private RecordingListAdapter adapter;
    private TextView textViewNoRecordings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        listView = findViewById(R.id.recording_list);
        textViewNoRecordings = findViewById(R.id.textViewNoRecordings);
        dataModels = new ArrayList<>();

        /* Populate dummy data
        dataModels.add(new RecordingDataModel("Work_rec1", "14.11.18", 523, "location", false));
        dataModels.add(new RecordingDataModel("Work_rec2", "15.11.18",332,"location", false));
        dataModels.add(new RecordingDataModel("Work_rec3", "16.11.18",113,"location", false));
        dataModels.add(new RecordingDataModel("Hotel_booking_rec1", "18.11.18",76,"location", false));
        dataModels.add(new RecordingDataModel("Interview_rec1", "19.11.18",725,"location", false));
        dataModels.add(new RecordingDataModel("Interview_rec2", "20.11.18",888,"location", false));
        dataModels.add(new RecordingDataModel("Interview_rec3", "25.11.18",937,"location", false));
        dataModels.add(new RecordingDataModel("Interview_rec4", "26.11.18",1005,"location", false));
        */

        fetchRecordings();
        // Setup List view of recordings. Use custom adapter.
        // adapter = new RecordingListAdapter(dataModels, getApplicationContext());
        // listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecordingDataModel dataModel = dataModels.get(position);
                // To be forwarded to a new View.
                Toast.makeText(getBaseContext(),
                        dataModel.getName() + "\nDuration: " + dataModel.getDuration(),
                        Toast.LENGTH_SHORT).show();

                Intent i = new Intent(RecordingListActivity.this, RecordingDetailsActivity.class);
                startActivity(i);
            }
        });
    }

    private void fetchRecordings() {

        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/ScriboAI/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        if( files!=null ){

            for (int i = 0; i < files.length; i++) {

                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/ScriboAI/Audios/" + fileName;

                // Some info is hard-coded. To be fixed.
                // A local Room Persistance database shall be created and info about recordings must be stored there.

                RecordingDataModel recording = new RecordingDataModel(fileName, "11.11.11", 90, recordingUri, false);

                dataModels.add(recording);
            }

            textViewNoRecordings.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            setAdaptertoRecyclerView();

        }else{
            textViewNoRecordings.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

    }

    private void setAdaptertoRecyclerView() {
        adapter = new RecordingListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);
    }
}
