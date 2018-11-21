package com.example.isausmanov.scriboai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    // Constant values
    private static final int RECORD_AUDIO_REQUEST_CODE = 123;

    // UI elements declaration
    Button recBtn, toListBtn, doneBtn;
    Chronometer chronometer;
    MediaRecorder mRecorder;
    String fileName = null;
    int recordFlag = 0; // 0-not recording 1-recording
    boolean isPlaying = false;
    private MediaPlayer mPlayer;
    //private int lastProgress = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        // Check the permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }
        initViews();

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
                // If recording is NOT started
                if (recordFlag == 0) {
                    // Start recording.
                    prepareforRecording();
                    startRecording();
                    recordFlag = 1;
                // If recording is on going
                } else if (recordFlag == 1) {
                    prepareforStop();
                    stopRecording();
                    recordFlag = 0;
                }

            }
        });


        // Done button clicked. Create a pop-up to save the recording. Convert to .wav
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if( !isPlaying && fileName != null ){
                    isPlaying = true;
                    startPlaying();
                }else{
                    isPlaying = false;
                    stopPlaying();
                } */
                startPlaying();
            }
        });

    }

    private void prepareforRecording() {
        recBtn.setText("Pause");
        doneBtn.setEnabled(false);
        toListBtn.setEnabled(false);

        //linearLayoutPlay.setVisibility(View.GONE);
    }

    private void prepareforStop() {
        recBtn.setText("Record");
        doneBtn.setEnabled(true);
        toListBtn.setEnabled(true);

        //linearLayoutPlay.setVisibility(View.GONE);
    }

    private void startRecording() {
        //we use the MediaRecorder class to record
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
       // mRecorder.setAudioEncodingBitRate(96000); // s
       // mRecorder.setAudioSamplingRate(44100); // s
        /**In the lines below, we create a directory ScriboAI/Audios in the phone storage
         * and the audios are being stored in the Audios folder **/
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/ScriboAI/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }

        fileName =  root.getAbsolutePath() + "/ScriboAI/Audios/" +
                String.valueOf(System.currentTimeMillis() + ".3gp");
        Log.d("filename",fileName);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopPlaying();
        //starting the chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void stopRecording() {

        try{
            mRecorder.stop();
            mRecorder.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mRecorder = null;
        //starting the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        //showing the play button
        Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
        //fileName is global string. it contains the Uri to the recently recorded audio.
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        /** once the audio is complete, timer is stopped here**/
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Dp something with button
                isPlaying = false;
                chronometer.stop();
            }
        });

    }

    private void stopPlaying() {
        try{
            mPlayer.stop();
            mPlayer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mPlayer = null;
        //showing the play button
        //imageViewPlay.setImageResource(R.drawable.ic_play);// Play enable
        chronometer.stop();
    }


    // Initialize UI elements here
    private void initViews() {
        // Initialize UI elements
        chronometer = findViewById(R.id.chronometerTimer);
        recBtn = findViewById(R.id.rec_btn);
        toListBtn = findViewById(R.id.list_btn);
        doneBtn = findViewById(R.id.done_btn);

    }





    // Permission request
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        // Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);

        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){

                //Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }

}
