package com.example.vlfom.emptymfccapplication;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.vlfom.emptymfccapplication.ctc_decoder.DummyData;
import com.example.vlfom.emptymfccapplication.ctc_decoder.LanguageModel;
import com.example.vlfom.emptymfccapplication.ctc_decoder.WordBeamSearch;
import com.example.vlfom.emptymfccapplication.mfcc.MFCC;
import com.example.vlfom.emptymfccapplication.wav_reader.WavFile;
import com.example.vlfom.emptymfccapplication.wav_reader.WavFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read file from assets
        FileInputStream iStream;
        try {
            iStream = getApplicationContext().getAssets().openFd("example.wav").createInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Get MFCC features from example file
        double[][] mfcc_data;
        try {
            mfcc_data = getMFCC(iStream);
        } catch (IOException | WavFileException e) {
            e.printStackTrace();
            return;
        }

        // Log for fun
        Log.d("MFCC_LOG",
                "MFCC shape: " + mfcc_data.length + ", " + mfcc_data[0].length + "; element[0][0]: " + mfcc_data[0][0]
        );

        // Here your model should take this mfcc_data stuff
        // TODO: insert Huawei model
        // TODO: input is mfcc_data which is of shape (320, 16)
        // TODO: output is textMat which is of shape (155, 29(
        // TODO: example values provided here already for testing

        // double[][] prediction = Model.predict(mfcc_data)

        //

        // Use dummy data for now. Its shape must be (155, 29) as in the notebook
        double[][] textMat = DummyData.dummyMatrix;

        // Create a language model using target output
        LanguageModel lm = new LanguageModel("he went into the scheme with his whole heart", "' abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");

        // Do BeamSearch
        String res = WordBeamSearch.search(textMat, 25, lm, true);

        // Log result
        Log.d("CTC_LOG", res);
    }

    public static double[][] getMFCC(FileInputStream iStream) throws IOException, WavFileException {
        WavFile wavFile = WavFile.openWavFile(iStream);

        // Max buffer size; currently set to 5.11 secs
        int buffer_size = 81760;

        double[] temp_buffer = new double[buffer_size];

        // Read file into temp buffer
        int wavFramesCount = wavFile.readFrames(temp_buffer, buffer_size);

        wavFile.close();

        // Write file to specific buffer for MFCC
        // TODO: try remove this buffer copy and use previous buffer
        double[] buffer = new double[wavFramesCount];
        for (int s = 0 ; s < wavFramesCount ; s++)
        {
            buffer[s] = 32768 * temp_buffer[s];
        }

        // Get MFCC
        MFCC mfcc = new MFCC();
        double[][] mfcc_data = mfcc.process(buffer);

        // Pad MFCC to fixed shape (320, 16)
        double[][] mfcc_padded = new double[320][16];
        for (int i = 0; i < mfcc_data[0].length; ++i) {
            for (int j = 0; j < 16; ++j) {
                mfcc_padded[i][j] = mfcc_data[j][i];
            }
        }

        return mfcc_padded;
    }
}
