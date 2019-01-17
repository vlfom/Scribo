package com.example.isausmanov.scriboai;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "recordings_data")
public class RecordingDataModel {

    // temporary fields
    // public static ArrayList<String> transcription_words = new ArrayList<>(Arrays.asList("Fix", "me", "a", "drink", ".", "Make", "it", "a", "strong", "one", ".", "Hey", "comrade,", "a", "drink", ".", "Make", "it", "a", "long", "one", ".", "My", "hands", "are", "shaking", ".", "And", "my", "feet", "are", "numb", ".", "My", "head", "is", "aching", ".", "And", "the", "bar's", "going", "round", ".", "And", "I'm", "so", "down", ".", "In", "this", "foreign", "town", ".", "Tonight", "there's", "a", "band", ".", "It", "ain't", "such", "a", "bad", "one", ".", "Play", "me", "a", "song", ".", "Don'T", "make", "it", "a", "sad", "one", ".", "I", "can't", "even", "talk", ".", "To", "these", "Russian", "girls", ".", "The", "beer", "is", "lousy", ".", "And", "the", "food", "is", "worse", ".", "And", "it's", "so", ".", "Damn", ".", "Cold", ".", "Yes", "it's", "so", ".", "Damn", ".", "Cold", ".", "I", "know", "it's", "hard", "to", "believe", ".", "But", "I", "haven't", "been", "warm", ".", "For", "a", "week", ".", "Moonlight", "and", ".", "Vodka", ".", "Takes", "me", "away", ".", "Midnight", "in", "Moscow", ".", "Is", "lunchtime", ".", "In", "L.A", ".", "Ooh", "play", "boys,", "play", ".", "Espionage", ".", "Is", "a", "serious", "business", ".", "Well", "I've", "had", "enough", ".", "Of", "this", "serious", "business", ".", "That", "dancing", "girl", ".", "Is", "making", "eyes", "at", "me", ".", "I'M", "sure", "she's", "working", ".", "For", "the", "K.G.B", ".", "In", "this", "paradise", ".", "Ah", "cold", "as", "ice", ".", "Moonlight", "and", "vodka", ".", "Takes", "me", "away", ".", "Midnight", "in", "Moscow", ".", "Is", "sunshine", "in", "L.A", ".", "Yes,", "in", "the", "good", "old", "U.S.A", "."));
    // public static ArrayList<Integer> transcription_word_times = new ArrayList<>(Arrays.asList(11470, 11680, 11890, 12100, 12100, 13460, 13760, 14060, 14360, 14660, 14660, 15440, 15760, 16080, 16400, 16400, 18080, 18280, 18480, 18680, 18880, 18880, 19830, 20122, 20414, 20706, 20706, 21790, 22020, 22250, 22480, 22710, 22710, 24450, 24728, 25006, 25284, 25284, 26150, 26398, 26646, 26895, 27143, 27143, 28270, 28698, 29126, 29554, 29554, 32600, 33004, 33408, 33812, 33812, 37440, 37716, 37992, 38268, 38268, 39550, 39788, 40027, 40265, 40504, 40742, 40742, 41980, 42178, 42376, 42574, 42574, 43990, 44210, 44430, 44650, 44870, 45090, 45090, 46370, 46594, 46818, 47042, 47042, 47830, 48232, 48634, 49036, 49036, 50500, 50798, 51096, 51394, 51394, 52380, 52650, 52920, 53190, 53460, 53460, 54470, 54672, 54875, 54875, 56060, 56060, 57160, 57160, 58730, 58938, 59146, 59354, 60400, 60400, 61390, 61390, 63060, 63337, 63614, 63891, 64168, 64445, 64445, 65360, 65665, 65970, 66275, 66580, 66580, 67530, 67907, 68285, 68285, 72550, 73070, 73070, 74660, 74660, 77380, 77870, 78360, 78360, 81320, 82026, 82732, 83438, 85480, 85700, 85920, 87230, 87613, 87613, 89890, 90418, 90946, 91474, 91474, 122750, 123126, 124350, 124730, 125110, 125490, 125490, 126720, 126988, 127256, 127524, 127524, 128550, 128964, 129378, 129792, 129792, 131430, 131664, 131898, 132132, 132860, 133178, 133496, 133815, 134133, 134133, 135580, 135878, 136176, 136474, 136474, 137490, 137867, 138245, 138245, 139680, 140020, 140360, 140360, 143960, 144378, 144796, 145214, 145214, 148950, 149792, 150635, 150635, 153370, 153945, 154520, 154520, 157650, 158346, 159042, 159738, 161780, 162376, 162972, 163568, 163568, 166350, 166715, 167081, 167447, 167812, 168178, 168178));

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "r_id")
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

    @ColumnInfo(name = "r_transcribed")
    boolean isTranscribed = false;

    @ColumnInfo(name = "r_transcription_words")
    private ArrayList<String> transcription_words;

    @ColumnInfo(name = "r_transcription_word_times")
    private ArrayList<Integer> transcription_word_times;

    @ColumnInfo(name = "r_transcription_speaker")
    private ArrayList<Integer> transcription_speaker;

    public RecordingDataModel(String name, long duration, String uri, boolean isTranscribed) {
        this.name = name;
        this.date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());;
        this.duration = duration;
        this.duration_s = formatSeconds(duration);
        this.uri = uri;
        this.isTranscribed = isTranscribed;
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

    public void setTranscribed(boolean transcribed){
        this.isTranscribed = transcribed;
    }

    public boolean getTranscribed() {
        return isTranscribed;
    }

    public ArrayList<String> getTranscription_words() {
        return transcription_words;
    }

    public void setTranscription_words(ArrayList<String> transcription_words) {
        this.transcription_words = transcription_words;
    }

    public ArrayList<Integer> getTranscription_word_times() {
        return transcription_word_times;
    }

    public void setTranscription_word_times(ArrayList<Integer> transcription_word_times) {
        this.transcription_word_times = transcription_word_times;
    }

    public ArrayList<Integer> getTranscription_speaker() {
        return transcription_speaker;
    }

    public void setTranscription_speaker(ArrayList<Integer> transcription_speaker) {
        this.transcription_speaker = transcription_speaker;
    }

    private static String formatSeconds(long durationInMillis){

        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d", hour, minute, second);

        return time;
    }

}
