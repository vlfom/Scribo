package com.example.isausmanov.scriboai;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.isausmanov.scriboai.activities.RecordingListActivity;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordingListAdapter extends ArrayAdapter<RecordingDataModel> implements View.OnClickListener {

    private ArrayList<RecordingDataModel> data;
    Context context;
    int progressStatus = 1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView date;
        TextView duration;
        ImageView icon;
        Button transcribe_btn;
        ProgressBar progressBar;

    }

    public RecordingListAdapter(ArrayList<RecordingDataModel> data, Context context) {
        super(context, R.layout.recording_list_item, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        //RecordingDataModel reco = getItem(position);
        RecordingDataModel dataModel = (RecordingDataModel) object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RecordingDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag
        final Handler handler = new Handler();

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.recording_list_item, parent, false);
            viewHolder.name = convertView.findViewById(R.id.recording_name);
            viewHolder.date = convertView.findViewById(R.id.recording_date);
            viewHolder.duration = convertView.findViewById(R.id.recording_duration);
            viewHolder.icon = convertView.findViewById(R.id.list_item_image);
            viewHolder.transcribe_btn = convertView.findViewById(R.id.transcribe_button);
            viewHolder.progressBar = convertView.findViewById(R.id.progressBar);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }




//        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
        lastPosition = position;

        if (dataModel.getTranscribed()) {
            updateButton(viewHolder);
        }
        else {
            viewHolder.transcribe_btn.setOnClickListener(v -> {
                RecordingListActivity recordingListActivity = (RecordingListActivity) parent.getContext();

                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.transcribe_btn.setVisibility(View.GONE);

                // Synchronization lock for ProgressBar and Speech model
                AtomicInteger transcriptionDone = new AtomicInteger(0);

                new Thread(() -> {
                    int maxSpeed = 10;
                    int updateSpeed = maxSpeed;
                    int sleepDurationMs = 20;
                    int expectedProcessDurationSteps = (int) (dataModel.getDuration() / (1.5 * sleepDurationMs));

                    viewHolder.progressBar.setMax(expectedProcessDurationSteps * maxSpeed);

                    while (transcriptionDone.get() == 0) {
                        try {
                            Thread.sleep(sleepDurationMs);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (progressStatus * 1.0 / (expectedProcessDurationSteps * maxSpeed) > 0.6
                                && updateSpeed == maxSpeed) {
                            updateSpeed /= 2;
                        }

                        if (progressStatus * 1.0 / (expectedProcessDurationSteps * maxSpeed) > 0.8
                                && updateSpeed > maxSpeed / 3) {
                            updateSpeed /= 2;
                        }

                        if (progressStatus * 1.0 / (expectedProcessDurationSteps * maxSpeed) > 0.9
                                && updateSpeed > maxSpeed / 6) {
                            updateSpeed /= 2;
                        }

                        progressStatus += updateSpeed;

                        handler.post(() -> viewHolder.progressBar.setProgress(progressStatus));
                    }
                    recordingListActivity.runOnUiThread(() -> {
                        updateButton(viewHolder);
                    });
                }).start();

                new Thread(() -> {
                    recordingListActivity.transcribeAudioFromDB(position);
                    transcriptionDone.set(1);
                }).start();
            });
        }


        //viewHolder.progressBar.setVisibility(View.GONE);

        viewHolder.name.setText(dataModel.getName());
        viewHolder.duration.setText(dataModel.getDuration_s());
        viewHolder.date.setText(dataModel.getDate());
        viewHolder.icon.setOnClickListener(this);
        viewHolder.icon.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }

    private void updateButton(ViewHolder v){
        v.progressBar.setVisibility(View.GONE);
        v.transcribe_btn.setVisibility(View.VISIBLE);
        v.transcribe_btn.setText("PROCESSED");
        v.transcribe_btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGray));
        v.transcribe_btn.setEnabled(false);
    }
}
