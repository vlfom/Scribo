package com.example.isausmanov.scriboai.activities;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.ctc_decoder.DummyData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecordingDetailsActivity extends AppCompatActivity {

    private WebView textTranscription;
    private TextView textDuration;
    private TextView textTime;
    private Button buttonPause;
    private Button buttonPlay;
    private SeekBar seekBar;
    private Handler threadHandler = new Handler();
    private MediaPlayer mediaPlayer;
    private Button buttonShare;

    private ArrayList<String> transcriptionWords;
    private ArrayList<Integer> transcriptionTimes;
    private ArrayList<Integer> transcriptionSpeakerChanged;
    private String audioName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_details);

        // Configure top toolbar to navigate back
        Toolbar toolbar = findViewById(R.id.recording_details_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        this.textTranscription = findViewById(R.id.recording_details_transcription);
        this.textTranscription.setVerticalScrollBarEnabled(true);
        this.textTranscription.setBackgroundColor(Color.TRANSPARENT);
        this.textDuration = findViewById(R.id.recording_details_duration);
        this.textTime = findViewById(R.id.recording_details_time);
        this.buttonPlay = findViewById(R.id.recording_details_play_button);
        this.buttonPause = findViewById(R.id.recording_details_pause_button);
        this.buttonPause.setEnabled(false);
        this.buttonShare = findViewById(R.id.share_btn);

        this.seekBar = this.findViewById(R.id.recording_details_seek_bar);
        this.seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        // ID of template song
       // int songId = this.getRawResIdByName("template_song");

        // Create MediaPlayer.
        //this.mediaPlayer = MediaPlayer.create(this, songId);
        String songURI = getIntent().getStringExtra("AUDIO_URI");
        File songFile = new File(songURI);
        this.mediaPlayer = MediaPlayer.create(this, Uri.fromFile(songFile));
        this.mediaPlayer.setOnCompletionListener(mp -> {
            this.buttonPlay.setEnabled(true);
            this.buttonPause.setEnabled(false);
        });

        this.transcriptionTimes = (ArrayList<Integer>) getIntent().getSerializableExtra("AUDIO_WORD_TIMES");

        this.transcriptionSpeakerChanged = (ArrayList<Integer>) getIntent().getSerializableExtra("AUDIO_SPEAKER_CHANGED");

        this.transcriptionWords = (ArrayList<String>) getIntent().getSerializableExtra("AUDIO_TRANSCRIPTION");

        this.audioName = (String) getIntent().getSerializableExtra("AUDIO_NAME");

        setTranscriptionContent(this.transcriptionWords, null);

        buttonShare.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{""});
            i.putExtra(Intent.EXTRA_SUBJECT, audioName);
            i.putExtra(Intent.EXTRA_TEXT   , getPureText(transcriptionWords));
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(RecordingDetailsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // ["makes", "this"] => "makes this"
    private String getPureText(ArrayList<String> transcriptionWords) {
        String transcriptionText = "";
        for(int i = 0; i < transcriptionWords.size(); i++) transcriptionText = transcriptionText + transcriptionWords.get(i) + " ";
        return transcriptionText;
    }

    private void setTranscriptionContent(List<String> words, Integer index) {
        StringBuilder text = new StringBuilder();
        String header = "<html>\n" +
                "<head></head>\n" +
                "<body style=\"text-align:justify;color:rgb(100, 100, 100);font-size:16px;background:transparent;\">\n";
        String footer = "</body>\n" +
                "</html>";

        text.append(header);

        String word;
        if (index == null) {
            for (int i = 0; i < words.size(); ++i) {
                if (transcriptionSpeakerChanged.get(i) == 1) {
                    text.append("<br>Speaker<br>");
                }
                word = words.get(i);
                if (i > 0 && !word.equals(".")) {
                    text.append(" ");
                }
                text.append(word);
            }
        }
        else {
            for (int i = 0; i < index; ++i) {
                if (transcriptionSpeakerChanged.get(i) == 1) {
                    text.append("<br>Speaker<br>");
                }
                word = words.get(i);
                if (i > 0 && !word.equals(".")) {
                    text.append(" ");
                }
                text.append(word);
            }
            if (transcriptionSpeakerChanged.get(index) == 1) {
                text.append("<br>Speaker<br>");
            }
            text.append("<span style=\"background-color: #ddebff;\">");
            word = words.get(index);
            if (word.equals(".")) {
                text.append(".");
            }
            else {
                if (index > 0) {
                    text.append(" ");
                }

                text.append(word);

                if (index + 1 < words.size() && words.get(index + 1).equals(".")) {
                    text.append(".");
                }
                else {
                    text.append(" ");
                }
            }
            text.append("</span>");
            for (int i = index + 1; i < words.size(); ++i) {
                if (transcriptionSpeakerChanged.get(i) == 1) {
                    text.append("<br>Speaker<br>");
                }
                word = words.get(i);
                if (i > 0 && !word.equals(".") && i != index + 1) {
                    text.append(" ");
                }
                if (!(i == index + 1 && word.equals("."))) {
                    text.append(word);
                }
            }
        }

        text.append(footer);

        textTranscription.loadData(text.toString(), "text/html; charset=utf-8", "utf-8");
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
        private int previousIndex = -1;

        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            String currentPositionStr = millisecondsToString(currentPosition);
            textTime.setText(currentPositionStr);
            seekBar.setProgress(currentPosition);

            int index = Collections.binarySearch(transcriptionTimes, currentPosition);
            if (index != previousIndex) {
                previousIndex = index;

                if (index == -1) {
                    setTranscriptionContent(transcriptionWords, null);
                }
                else {
                    if (index < 0) {
                        index = -index - 2;
                    }

                    if (transcriptionWords.get(index).equals(".")) {
                        index -= 1;
                    }

                    setTranscriptionContent(transcriptionWords, index);
                }
            }

            // Delay thread by 50 milliseconds
            threadHandler.postDelayed(this, 50);
        }
    }
}
