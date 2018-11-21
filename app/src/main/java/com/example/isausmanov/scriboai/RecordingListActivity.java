package com.example.isausmanov.scriboai;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {
    // RecordsList data
    private ArrayList<RecordingDataModel> dataModels;
    private ListView listView;
    private RecordingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records_list);

        listView = findViewById(R.id.recording_list);

        // Populate dummy data
        dataModels = new ArrayList<>();
        dataModels.add(new RecordingDataModel("Work_rec1", 523));
        dataModels.add(new RecordingDataModel("Work_rec2", 332));
        dataModels.add(new RecordingDataModel("Work_rec3", 113));
        dataModels.add(new RecordingDataModel("Hotel_booking_rec1", 76));
        dataModels.add(new RecordingDataModel("Interview_rec1", 725));
        dataModels.add(new RecordingDataModel("Interview_rec2", 888));
        dataModels.add(new RecordingDataModel("Interview_rec3", 937));
        dataModels.add(new RecordingDataModel("Interview_rec4", 1005));

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
            }
        });
    }
}
