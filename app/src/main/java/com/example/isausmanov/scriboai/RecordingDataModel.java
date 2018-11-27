package com.example.isausmanov.scriboai;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class RecordingDataModel {

    @PrimaryKey(autoGenerate = true)
    private int id;
    // r_name = recording name
    @ColumnInfo(name = "r_name")
    private String name;

    @ColumnInfo(name = "r_date")
    private String date;

    @ColumnInfo(name = "r_duration")
    private long duration;

    private String duration_s;

    @ColumnInfo(name = "r_uri")
    private String uri;

    @ColumnInfo(name = "r_playing")
    boolean isPlaying = false;

    public RecordingDataModel(String name, String date, long duration, String uri, boolean isPlaying) {
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.duration_s = formatSeconds(duration);
        this.uri = uri;
        this.isPlaying = isPlaying;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration_s(String duration) {
        this.duration_s = duration;
    }

    public String getDuration_s() {
        return duration_s;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setPlaying(boolean playing){
        this.isPlaying = playing;
    }

    public boolean getPlaying() {
        return isPlaying;
    }

    private static String formatSeconds(long timeInSeconds){
        int seconds = (int)(timeInSeconds % 3600 % 60);
        int minutes = (int)(timeInSeconds % 3600 / 60);
        int hours = (int)(timeInSeconds / 3600);

        String HH = hours < 10 ? "0" + hours : String.valueOf(hours);
        String MM = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String SS = seconds < 10 ? "0" + seconds : String.valueOf(seconds);

        return HH + ":" + MM + ":" + SS;
    }

}
