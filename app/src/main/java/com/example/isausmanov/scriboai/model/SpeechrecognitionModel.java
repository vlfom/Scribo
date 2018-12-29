package com.example.isausmanov.scriboai.model;

import android.content.res.AssetManager;
import android.util.Log;


public class SpeechrecognitionModel {


    /**** user load model manager sync interfaces ****/
    public static int load(AssetManager mgr){
            return ModelManager.loadModelSync("Speechrecognition", mgr);
    }

    public static float[] predict(float[] buf){
        return ModelManager.runModelSync("Speechrecognition",buf);
    }

    public static int unload(){
        return ModelManager.unloadModelSync();
    }


    /**** load user model async interfaces ****/
    public static int registerListenerJNI(ModelManagerListener listener){
        return ModelManager.registerListenerJNI(listener);
    }

    public static void loadAsync(AssetManager mgr){
        ModelManager.loadModelAsync("Speechrecognition", mgr);
    }

    public static void predictAsync(float[] buf) {
        ModelManager.runModelAsync("Speechrecognition",buf);
    }

    public static void unloadAsync(){
        ModelManager.unloadModelAsync();
    }
}
