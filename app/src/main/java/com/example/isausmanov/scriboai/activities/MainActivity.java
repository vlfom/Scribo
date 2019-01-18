package com.example.isausmanov.scriboai.activities;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.ScreenUtils;
import com.example.isausmanov.scriboai.VoiceView;
import com.example.isausmanov.scriboai.WavRecorder;
import com.example.isausmanov.scriboai.ctc_decoder.LanguageModel;
import com.example.isausmanov.scriboai.database.AppDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements VoiceView.OnIClickedListener {

    // Constant values
    private static final int RECORD_AUDIO_REQUEST_CODE = 123;

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static Future<LanguageModel> languageModel;

    //VoiceView Stuff
    private VoiceView mVoiceView;
    private Handler mHandler;

    private boolean mIsRecording = false;

    // UI elements declaration
    public Button toListBtn, doneBtn;
    public Chronometer chronometer;
    public AlertDialog.Builder builder;
    public EditText input;
    public WavRecorder wavRecorder;

    String fileName = null;
    public long totalRecDuration = 0;
    public long recStartTime = 0;
    public long recStopTime = 0;
    public int recordFlag = 0; // 0-not recording 1-recording 2-paused
    public long mLastStopTime;
    public AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check the permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }

        initViews();

        // VoiceView Stuff
        mVoiceView = (VoiceView) findViewById(R.id.voiceview);
        mVoiceView.setOnClickListener(this);
        mHandler = new Handler(Looper.getMainLooper());

        // Go to list activity when clicked
        toListBtn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RecordingListActivity.class);
            startActivity(i);

            /* For sharing
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"isa.usmanov@tum.de"});
            i.putExtra(Intent.EXTRA_SUBJECT, "EMAIL FROM SCRIBO");
            i.putExtra(Intent.EXTRA_TEXT   , "Text text text text text");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
            */
        });

        // Done button clicked. Show a pop-up to save the recording.
        doneBtn.setOnClickListener(v -> {
            prepareUIforStop();
            showAlert();
            stopRecording();
            recordFlag = 0;
        });

        Boolean isFirstRun = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getBoolean("isFirstRun", true);

        Log.d("CoolestModel", String.valueOf(isFirstRun));
        if (isFirstRun) {
            // Initialize LanguageModel for SpeechRecognition model in background
            languageModel = executor.submit(initializeLM);
        }

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply();
    }

    private void prepareUIforRecording() {
        //recBtn.setText("Pause");
        doneBtn.setEnabled(false);
        toListBtn.setEnabled(false);
    }

    private void prepareUIforPause() {
        //recBtn.setText("Resume");
        doneBtn.setEnabled(true);
        toListBtn.setEnabled(false);
    }

    private void prepareUIforStop() {
        //recBtn.setText("Record");
        doneBtn.setEnabled(true);
        toListBtn.setEnabled(true);
    }


    private void startRecording() {
        // Place where the .wav recordings stored on the phone
        File root = android.os.Environment.getExternalStorageDirectory();
        fileName =  root.getAbsolutePath() + "/ScriboAI/Audios/" +
                String.valueOf(System.currentTimeMillis() + ".wav");
        wavRecorder = new WavRecorder(fileName);
        wavRecorder.startRecording();

        chronoStart();
    }

    private void pauseRecording() {
        try{
            wavRecorder.pauseRecording();
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG_REC, "Exception at pauseRecording");
        }

        chronoPause();

        // TODO: UI1 - make a label above timer that changes string value instead
        Toast.makeText(this, "Recording paused", Toast.LENGTH_SHORT).show();
    }

    private void resumeRecording() {

        try{
            wavRecorder.resumeRecording();
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG_REC, "Exception at resumeRecording");
        }

        chronoResume();
        //showing the play button
        // TODO: UI1
        Toast.makeText(this, "Recording", Toast.LENGTH_SHORT).show();
    }


    private void stopRecording() {

        try{
            wavRecorder.stopRecording();
        }catch (Exception e){
            e.printStackTrace();
            // TODO: UI1
            Log.d(TAG_REC, "Exception at stopRecording");
        }

        //stop, reset the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());

    }

    // Initialize UI elements here
    private void initViews() {
        // Initialize UI elements
        chronometer = findViewById(R.id.chronometerTimer);

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

    private void showAlert(){
        builder.setTitle("Save recording");

        // Set up the input
        input = new EditText(MainActivity.this);
        input.setSelected(true);
        input.setSelectAllOnFocus(true);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
        input.setText("Recording " + df.format(new Date()));
        input.selectAll();
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input, 60, 0, 60, 0);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            saveRecording(input.getText().toString());
            Toast.makeText(MainActivity.this, "Successfully saved.", Toast.LENGTH_SHORT).show();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();

            Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        });

        builder.show();

        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void saveRecording(String rec_name) {
        Log.d(TAG_DB, fileName);
        Log.d(TAG_DB, "total rec duration: " + totalRecDuration);

        db.recordingDao().insertAll(new RecordingDataModel(rec_name, totalRecDuration, fileName, false));
        db.close();

        // TODO: Put separately somewhere later
        totalRecDuration = 0;
        recStartTime = 0;
        recStopTime = 0;
    }

    // Logging tags
    String TAG_REC = "recording";
    String TAG_DB = "db_saving";
    String TAG_Voice = "voice";



    // Start/Pause Audio recording
    @Override
    public void onHandleRecording() {
        // If recording is NOT started

        Log.d("FLAG", "flagvalue: " + recordFlag);
        if (recordFlag == 0) {
            // Start recording.
            prepareUIforRecording();
            startRecording();
            recStartTime = System.currentTimeMillis();
            recordFlag = 1;
            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                    .allowMainThreadQueries()
                    .build();

            // If recording is on going
        } else if (recordFlag == 1) {
            prepareUIforPause();
            pauseRecording();

            recStopTime = System.currentTimeMillis();
            long timeElapsed = recStopTime - recStartTime;
            totalRecDuration = totalRecDuration + timeElapsed;

            recordFlag = 2;

            // If recording was paused
        } else if (recordFlag == 2) {
            // resume recording
            prepareUIforRecording();
            recStartTime = System.currentTimeMillis();
            resumeRecording();
            recordFlag = 1;
        }
    }

    // interface methods called from VoiceView.java
    @Override
    public void onAnimationStart() {
        Log.d(TAG_Voice, "onRecordStart");
        mIsRecording = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // get amplitude from wavRecorder
                double amplit = wavRecorder.getAmplitude();

                float radius = (float) scale(amplit, 1, 6500, 50, 520);
                mVoiceView.animateRadius(radius);
                if (mIsRecording) {
                    mHandler.postDelayed(this, 50);
                }
            }
        });
    }

    // math helper - scales range of numbers
    public double scale(final double valueIn, final double baseMin, final double baseMax, final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }

    @Override
    public void onAnimationFinish() {
        Log.d(TAG_Voice, "onRecordFinish");
        mIsRecording = false;
    }

    private Callable<LanguageModel> initializeLM = (Callable<LanguageModel>) () -> {
        // Read file from assets
        FileInputStream iStreamCorpus;
        FileInputStream iStreamPostag;
        try {
            iStreamCorpus = getApplicationContext().getAssets().openFd("data_10kwords_1mlines_postag.txt").createInputStream();
            iStreamPostag = getApplicationContext().getAssets().openFd("postag_dot_data.txt").createInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Log.d("CoolModel", "Working!");
        LanguageModel languageModel = new LanguageModel(
                iStreamCorpus,
                iStreamPostag,
                "' abcdefghijklmnopqrstuvwxyz",
                "abcdefghijklmnopqrstuvwxyz",
                LanguageModel.NGRAM_BIGRAM
        );
        Log.d("CoolModel", "Finished!");

        return languageModel;
    };

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
