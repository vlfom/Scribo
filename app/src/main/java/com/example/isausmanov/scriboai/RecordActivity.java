package com.example.isausmanov.scriboai;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RecordActivity extends AppCompatActivity {

    //UI elements declaration
    Button recBtn, toListBtn, doneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Initialize UI elements
        recBtn = findViewById(R.id.rec_btn);
        toListBtn = findViewById(R.id.list_btn);
        doneBtn = findViewById(R.id.done_btn);

        // Go to list activity when clicked
        toListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecordActivity.this, ListActivity.class);
                startActivity(i);
            }
        });


        // Start/Pause Audio recording
        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TO DO:
            }
        });


        // Done button clicked. Create a pop-up to save the recording. Convert to .wav
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TO DO:
            }
        });



    }
}
