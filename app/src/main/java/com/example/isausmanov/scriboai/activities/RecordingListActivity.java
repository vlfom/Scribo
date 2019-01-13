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
import com.example.isausmanov.scriboai.database.AppDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.arch.persistence.room.Room;

public class RecordingListActivity extends AppCompatActivity {
    // RecordsList data
    private ArrayList<RecordingDataModel> dataModels;
    private List<RecordingDataModel> dataInList;
    private ListView listView;
    private RecordingListAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        listView = findViewById(R.id.recording_list);
        dataModels = new ArrayList<>();


        fetchRecordings();
        setAdaptertoRecyclerView();
        //listView.setDivider(getDrawable(R.drawable.divider));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecordingDataModel dataModel = dataModels.get(position);
                // To be forwarded to a new View.
                Log.d("ITEM", dataModel.getUri());

                Intent i = new Intent(RecordingListActivity.this, RecordingDetailsActivity.class);
                startActivity(i);
            }
        });

    }

    private void fetchRecordings() {

        /*
        new Thread(new Runnable() {
            public void run() {

            }
        }).start();
        */

        // TODO: MUST PUT THIS INTO A SEPARATE THREAD! For a time being on a main thread.
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .build();

       // db.recordingDao().insertAll(new RecordingDataModel("dummyName", "20.20.20", 90, "sas", false));
        // Put recordings from database into List
        dataInList = db.recordingDao().getAllRecordings();
        db.close();
        // Cast List into ArrayList
        dataModels = new ArrayList<>(dataInList);
        listView.setVisibility(View.VISIBLE);
    }

    private void setAdaptertoRecyclerView() {
        adapter = new RecordingListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);
    }
}
