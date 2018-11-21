package com.example.isausmanov.scriboai;

public class RecordingDataModel {
    private String name;
    private String date;
    private long duration;
    private String duration_s;

    public RecordingDataModel(String name, String date, long duration) {
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.duration_s = formatSeconds(duration);
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getDuration() {
        return duration_s;
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
