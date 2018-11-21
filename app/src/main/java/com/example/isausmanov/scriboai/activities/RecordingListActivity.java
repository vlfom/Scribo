package com.example.isausmanov.scriboai.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.RecordingListAdapter;

import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {
    // RecordsList data
    private ArrayList<RecordingDataModel> dataModels;
    private ListView listView;
    private RecordingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        listView = findViewById(R.id.recording_list);

        // Populate dummy data
        dataModels = new ArrayList<>();
        dataModels.add(new RecordingDataModel("Work_rec1", "14.11.18", 523));
        dataModels.add(new RecordingDataModel("Work_rec2", "15.11.18",332));
        dataModels.add(new RecordingDataModel("Work_rec3", "16.11.18",113));
        dataModels.add(new RecordingDataModel("Hotel_booking_rec1", "18.11.18",76));
        dataModels.add(new RecordingDataModel("Interview_rec1", "19.11.18",725));
        dataModels.add(new RecordingDataModel("Interview_rec2", "20.11.18",888));
        dataModels.add(new RecordingDataModel("Interview_rec3", "25.11.18",937));
        dataModels.add(new RecordingDataModel("Interview_rec4", "26.11.18",1005));

        // Setup List view of recordings. Use custom adapter.
        adapter = new RecordingListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);

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
}
