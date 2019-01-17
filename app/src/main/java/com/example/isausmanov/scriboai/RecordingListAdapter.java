package com.example.isausmanov.scriboai;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecordingListAdapter extends ArrayAdapter<RecordingDataModel> implements View.OnClickListener {

    private ArrayList<RecordingDataModel> data;
    Context context;
    int progressStatus = 1;
    int progress = 1;

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

        viewHolder.transcribe_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.transcribe_btn.setVisibility(View.GONE);



                new Thread(new Runnable() {
                    public void run() {
                        while (progressStatus < 100) {
                            progressStatus = doSomeWork();
                            handler.post(new Runnable() {
                                public void run() {


                                    viewHolder.progressBar.setProgress(progressStatus);
                                }
                            });
                        }
                        handler.post(new Runnable() {
                            public void run() {
                                // ---0 - VISIBLE; 4 - INVISIBLE; 8 - GONE---
                                updateButton(viewHolder);
                                progress = 1;
                                progressStatus = 1;
                            }
                        });
                    }

                    private int doSomeWork() {
                        try {
                            // ---simulate doing some work---
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return ++progress;
                    }
                }).start();

            }
        });


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
        v.transcribe_btn.setText("FINISHED");
        v.transcribe_btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGray));
        v.transcribe_btn.setEnabled(false);
    }
}
