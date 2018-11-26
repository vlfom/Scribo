package com.example.isausmanov.scriboai.activities;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.isausmanov.scriboai.R;

import java.util.concurrent.TimeUnit;

public class RecordingDetailsActivity extends AppCompatActivity {

    private TextView textTranscription;
    private TextView textDuration;
    private TextView textTime;
    private Button buttonPause;
    private Button buttonPlay;
    private SeekBar seekBar;
    private Handler threadHandler = new Handler();
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_details);

        // Configure top toolbar to navigate back
        Toolbar toolbar = findViewById(R.id.recording_details_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.textTranscription = findViewById(R.id.recording_details_transcription);
        this.textTranscription.setMovementMethod(new ScrollingMovementMethod());
        this.textDuration = findViewById(R.id.recording_details_duration);
        this.textTime = findViewById(R.id.recording_details_time);
        this.buttonPlay = findViewById(R.id.recording_details_play_button);
        this.buttonPause = findViewById(R.id.recording_details_pause_button);
        this.buttonPause.setEnabled(false);

        this.seekBar = this.findViewById(R.id.recording_details_seek_bar);
        this.seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        // ID of template song
        int songId = this.getRawResIdByName("template_song");

        // Create MediaPlayer.
        this.mediaPlayer = MediaPlayer.create(this, songId);
    }

    // Find ID of resource in 'raw' folder.
    public int getRawResIdByName(String resName) {
        String pkgName = this.getPackageName();
        return this.getResources().getIdentifier(resName, "raw", pkgName);
    }

    // Converts milliseconds to string
    // TODO: handle situations when hours > 100
    private String millisecondsToString(int milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours((long) milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes((long) milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds((long) milliseconds) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Configure SeekBar interaction
    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }

            mediaPlayer.seekTo((int) (progress * 1.0 / seekBar.getMax() * mediaPlayer.getDuration()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    // Starts playback
    public void startPlayback(View view) {
        int duration = this.mediaPlayer.getDuration();

        int currentPosition = this.mediaPlayer.getCurrentPosition();
        if (currentPosition == 0) {
            this.seekBar.setMax(duration);
            String maxTimeString = this.millisecondsToString(duration);
            this.textDuration.setText(maxTimeString);
        } else if (currentPosition == duration) {
            // Resets the MediaPlayer to its uninitialized state.
            this.mediaPlayer.reset();
        }
        this.mediaPlayer.start();
        // Create a thread to update position of SeekBar.
        UpdateSeekBarThread updateSeekBarThread = new UpdateSeekBarThread();
        threadHandler.postDelayed(updateSeekBarThread, 50);

        this.buttonPlay.setEnabled(false);
        this.buttonPause.setEnabled(true);
    }

    // Pauses playback
    public void pausePlayback(View view) {
        this.mediaPlayer.pause();
        this.buttonPlay.setEnabled(true);
        this.buttonPause.setEnabled(false);
    }

    // Thread to update position for the SeekBar
    class UpdateSeekBarThread implements Runnable {

        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            String currentPositionStr = millisecondsToString(currentPosition);
            textTime.setText(currentPositionStr);
            seekBar.setProgress(currentPosition);

            // Delay thread by 50 milliseconds
            threadHandler.postDelayed(this, 50);
        }
    }
}
