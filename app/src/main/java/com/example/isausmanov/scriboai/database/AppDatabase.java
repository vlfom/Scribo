package com.example.isausmanov.scriboai.database;

import com.example.isausmanov.scriboai.RecordingDataModel;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {RecordingDataModel.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecordingDao recordingDao();
}
