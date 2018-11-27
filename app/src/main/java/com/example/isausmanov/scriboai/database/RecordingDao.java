package com.example.isausmanov.scriboai.database;

import com.example.isausmanov.scriboai.RecordingDataModel;

import java.util.ArrayList;
import java.util.List;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface RecordingDao {

    @Query("SELECT * FROM recordingdatamodel")
    List<RecordingDataModel> getAllRecordings();

    @Insert
    void insertAll(RecordingDataModel... recordingDataModels);
}
