package com.example.isausmanov.scriboai.model;

import android.content.res.AssetManager;
import android.util.Log;


public class SpeakerchangedetectionModel {


    /**** user load model manager sync interfaces ****/
    public static int load(AssetManager mgr){
            return ModelManager.loadModelSync("Speakerchangedetection", mgr);
    }

    public static float[] predict(float[] buf){
        return ModelManager.runModelSync("Speakerchangedetection",buf);
    }

    public static int unload(){
        return ModelManager.unloadModelSync();
    }


    /**** load user model async interfaces ****/
    public static int registerListenerJNI(ModelManagerListener listener){
        return ModelManager.registerListenerJNI(listener);
    }

    public static void loadAsync(AssetManager mgr){
        ModelManager.loadModelAsync("Speakerchangedetection", mgr);
    }

    public static void predictAsync(float[] buf) {
        ModelManager.runModelAsync("Speakerchangedetection",buf);
    }

    public static void unloadAsync(){
        ModelManager.unloadModelAsync();
    }
}
