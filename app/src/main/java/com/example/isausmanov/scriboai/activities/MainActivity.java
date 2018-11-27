package com.example.isausmanov.scriboai.activities;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.database.AppDatabase;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Constant values
    private static final int RECORD_AUDIO_REQUEST_CODE = 123;

    // UI elements declaration
    Button recBtn, toListBtn, doneBtn;
    Chronometer chronometer;
    MediaRecorder mRecorder;
    MediaPlayer mPlayer;
    AlertDialog.Builder builder;
    EditText input;

    String fileName = null;
    int recordFlag = 0; // 0-not recording 1-recording 2-was paused
    boolean isPlaying = false;
    private long mLastStopTime;
    private AppDatabase db;

   /* private String rec_name = "";
    private String rec_date = "";
    private long rec_duration = 0;
    private String rec_uri = ""; */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check the permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }


        initViews();

        // Go to list activity when clicked
        toListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RecordingListActivity.class);
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
                    prepareUIforRecording();
                    startRecording();
                    recordFlag = 1;
                    db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                            .allowMainThreadQueries()
                            .build();
                    // If recording is on going
                } else if (recordFlag == 1) {
                    prepareUIforPause();
                    pauseRecording();
                    recordFlag = 2;
                    // if recording if recording was paused
                } else if (recordFlag == 2) {
                    // resume recording
                    prepareUIforRecording();
                    resumeRecording();
                    recordFlag = 1;
                }

            }
        });


        // Done button clicked. Create a pop-up to save the recording. Convert to .wav
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if( !isPlaying && fileName != null ){
//                    isPlaying = true;
//                    startPlaying();
//                }else{
//                    isPlaying = false;
//                    stopPlaying();
//                }
                //startPlaying();
                // Stop recording, then save the recording

                showAlert();

                // JUST FOR TIME BEING.
                // TODO: STOP RECORDING, SHOW A POP-UP, SAVE TO DATABASE, UPDATE UI.
                prepareUIforStop();
                stopRecording();
                //startPlaying();
                db.close();
                recordFlag = 0;
                prepareUIforStop();
            }
        });


    }

    private void prepareUIforRecording() {
        recBtn.setText("Pause");
        doneBtn.setEnabled(false);
        toListBtn.setEnabled(false);
    }

    private void prepareUIforPause() {
        recBtn.setText("Resume");
        doneBtn.setEnabled(true);
        toListBtn.setEnabled(false);
    }

    private void prepareUIforStop() {
        recBtn.setText("Record");
        doneBtn.setEnabled(true);
        toListBtn.setEnabled(true);
    }


    private void startRecording() {
        //we use the MediaRecorder class to record
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //mRecorder.setAudioEncodingBitRate(96000); // s
        //mRecorder.setAudioSamplingRate(44100); // s
        /**In the lines below, we create a directory ScriboAI/Audios in the phone storage
         * and the audios are being stored in the Audios folder **/
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/ScriboAI/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }

        // Give a specific title for the recording
        fileName =  root.getAbsolutePath() + "/ScriboAI/Audios/" +
                String.valueOf(System.currentTimeMillis() + ".3gp");
        Log.d("filename",fileName);
        mRecorder.setOutputFile(fileName);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        chronoStart();
    }

    private void pauseRecording() {

        try{
            mRecorder.pause();
            //mRecorder.release();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("MediaRecorder", "Exception at pauseRecording");
        }

        chronoPause();

        Toast.makeText(this, "Recording paused", Toast.LENGTH_SHORT).show();
    }

    private void resumeRecording() {

        try{
            mRecorder.resume();
            //mRecorder.release();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("MediaRecorder", "Exception at resumeRecording");
        }

        chronoResume();

        //showing the play button
        Toast.makeText(this, "Recording", Toast.LENGTH_SHORT).show();
    }


    private void stopRecording() {

        try{
            mRecorder.stop();
            mRecorder.release();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("MediaRecorder", "Exception at stopRecording");
        }
        mRecorder = null;
        //stop, reset the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());

    }



    private void startPlaying() {
        if (mPlayer.isPlaying()) {
            stopPlaying();
        }

        mPlayer = new MediaPlayer();
        try {
            //fileName is global string. it contains the Uri to the recently recorded audio.
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
            Log.d("filename",fileName);
        } catch (IOException e) {
            Log.d("MediaRecorder", "Exception at startPlaying");
        }
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Do something with button
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
            Log.d("MediaRecorder", "Exception at stopPlaying");
        }
        mPlayer = null;
        chronometer.stop();
    }


    // Initialize UI elements here
    private void initViews() {
        // Initialize UI elements
        chronometer = findViewById(R.id.chronometerTimer);
        recBtn = findViewById(R.id.rec_btn);
        toListBtn = findViewById(R.id.list_btn);
        doneBtn = findViewById(R.id.done_btn);
        doneBtn.setEnabled(false);
        builder = new AlertDialog.Builder(MainActivity.this);
    }

    // Chronometer actions
    private void chronoStart() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void chronoPause() {
        chronometer.stop();
        mLastStopTime = SystemClock.elapsedRealtime();
    }

    private void chronoResume() {
        long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
        chronometer.setBase( chronometer.getBase() + intervalOnPause );
        chronometer.start();
    }

   /* private void clearRecValues(){
        rec_name = "";
        rec_date = "";
        rec_duration = 0;
        rec_uri = "";
    } */

    private void showAlert(){

        builder.setTitle("Name");

        // Set up the input
        input = new EditText(MainActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveRecording(input.getText().toString());
                Toast.makeText(MainActivity.this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void saveRecording(String rec_name) {
        Uri uri = Uri.parse(fileName);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(MainActivity.this, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String dateStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
       // int millSecond = Integer.parseInt(durationStr);
        Log.d("SAVE", rec_name);
        //Log.d("SAVE", durationStr);
        Log.d("SAVE", dateStr);
        Log.d("SAVE", fileName);

        db.recordingDao().insertAll(new RecordingDataModel(rec_name, dateStr.substring(4,12), Integer.parseInt(durationStr), fileName, false));
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
