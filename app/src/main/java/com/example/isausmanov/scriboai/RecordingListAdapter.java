package com.example.isausmanov.scriboai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecordingListAdapter extends ArrayAdapter<RecordingDataModel> implements View.OnClickListener {

    private ArrayList<RecordingDataModel> data;
    Context context;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView date;
        TextView duration;
        ImageView icon;
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
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.recording_list_item, parent, false);
            viewHolder.name = convertView.findViewById(R.id.recording_name);
            viewHolder.date = convertView.findViewById(R.id.recording_date);
            viewHolder.duration = convertView.findViewById(R.id.recording_duration);
            viewHolder.icon = convertView.findViewById(R.id.list_item_image);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
        lastPosition = position;



        viewHolder.name.setText(dataModel.getName());
        viewHolder.duration.setText(dataModel.getDuration_s());
        viewHolder.date.setText(dataModel.getDate());
        viewHolder.icon.setOnClickListener(this);
        viewHolder.icon.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}
