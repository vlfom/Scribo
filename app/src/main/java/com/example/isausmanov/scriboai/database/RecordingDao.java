package com.example.isausmanov.scriboai.database;

import com.example.isausmanov.scriboai.RecordingDataModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverter;

@Dao
public interface RecordingDao {

    @Query("SELECT * FROM recordings_data")
    List<RecordingDataModel> getAllRecordings();

    // When transcribed audio
    @Query("UPDATE recordings_data SET r_transcribed =:isTranscribed, r_transcription_words = :transcription_words, r_transcription_word_times = :transcription_word_times, r_transcription_speaker = :transcription_speaker WHERE r_id =:id")
    void update(boolean isTranscribed, ArrayList<String> transcription_words, ArrayList<Integer> transcription_word_times, ArrayList<Integer> transcription_speaker, int id);

    @Insert
    void insertAll(RecordingDataModel... recordingDataModels);

    //void update(boolean b, ArrayList<String> transcriptionWords, ArrayList<Integer> transcriptionWordTimes);
}

class Converters {
    @TypeConverter
    public static ArrayList<String> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static ArrayList<Integer> fromInteger(String value) {
        Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayIntList(ArrayList<Integer> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
    /*@TypeConverter
    public static ArrayList<Integer> fromInteger(Integer value) {
        Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
        return new Gson().fromJson(value.toString(), listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Integer> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }*/
}
