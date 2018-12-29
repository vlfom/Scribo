package com.example.isausmanov.scriboai.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.RecordingListAdapter;
import com.example.isausmanov.scriboai.ctc_decoder.LanguageModel;
import com.example.isausmanov.scriboai.ctc_decoder.WordBeamSearch;
import com.example.isausmanov.scriboai.database.AppDatabase;
import com.example.isausmanov.scriboai.mfcc.MFCC;
import com.example.isausmanov.scriboai.model.SpeechrecognitionModel;
import com.example.isausmanov.scriboai.wav_reader.WavFile;
import com.example.isausmanov.scriboai.wav_reader.WavFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import android.arch.persistence.room.Room;

public class RecordingListActivity extends AppCompatActivity {
    // RecordsList data
    private ArrayList<RecordingDataModel> dataModels;
    private List<RecordingDataModel> dataInList;
    private ListView listView;
    private RecordingListAdapter adapter;
    private TextView textViewNoRecordings;
    private AppDatabase db;

    private static LanguageModel languageModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        listView = findViewById(R.id.recording_list);
        textViewNoRecordings = findViewById(R.id.textViewNoRecordings);
        dataModels = new ArrayList<>();


        fetchRecordings();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            RecordingDataModel dataModel = dataModels.get(position);

            float[] input = load_wav_data(dataModel.getUri(), false);
            //float[] input = load_wav_data("example.wav", true);

            Log.d("CoolModel", dataModel.getUri());
            SpeechrecognitionModel.load(getAssets());

            Log.d("CoolStuff", String.valueOf(input[1000]));
            Log.d("CoolStuff", String.valueOf(input[10000]));
            Log.d("CoolStuff", String.valueOf(input[14239]));
            Log.d("CoolStuff", String.valueOf(input[14240]));
            float[] model_result = SpeechrecognitionModel.predict(input);

            SpeechrecognitionModel.unload();

            String text = decodeCTCMatrix(model_result);

            Intent i = new Intent(RecordingListActivity.this, RecordingDetailsActivity.class);
            i.putExtra("AUDIO_TRANSCRIPTION", text);
            startActivity(i);
        });

        initializeLM();
    }

    private void initializeLM() {
//        languageModel = new LanguageModel(
//                "he went into the scheme with his whole heart",
//                "' abcdefghijklmnopqrstuvwxyz",
//                "abcdefghijklmnopqrstuvwxyz"
//        );

//        StringBuilder contentBuilder = new StringBuilder();
//
//        try (Stream<String> stream = new BufferedReader(
//                new InputStreamReader(getAssets().open("cleaned_words_10k.txt"))
//        ).lines())
//        {
//            stream.forEach(s -> contentBuilder.append(s).append("\n"));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        languageModel = new LanguageModel(
//                contentBuilder.toString(),
//                "' abcdefghijklmnopqrstuvwxyz",
//                "abcdefghijklmnopqrstuvwxyz"
//        );


        // Read file from assets
        FileInputStream iStream;
        try {
            iStream = getApplicationContext().getAssets().openFd("cleaned_words_10k_freq.txt").createInputStream();
            Log.d("CoolModel", "Working!");
        } catch (IOException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            Log.d("CoolModel", sStackTrace);
            return;
        }
        Log.d("CoolModel", "Working!");
        languageModel = new LanguageModel(
                iStream,
                "' abcdefghijklmnopqrstuvwxyz",
                "abcdefghijklmnopqrstuvwxyz"
        );
    }

    private void fetchRecordings() {

        /*
        new Thread(new Runnable() {
            public void run() {

            }
        }).start();
        */

        // TODO: MUST PUT THIS INTO A SEPARATE THREAD! For a time being on a main thread.
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                .allowMainThreadQueries()
                .build();

       // db.recordingDao().insertAll(new RecordingDataModel("dummyName", "20.20.20", 90, "sas", false));
        // Put recordings from database into List
        dataInList = db.recordingDao().getAllRecordings();
        db.close();
        // Cast List into ArrayList
        dataModels = new ArrayList<>(dataInList);

        textViewNoRecordings.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        setAdaptertoRecyclerView();
    }

    private void setAdaptertoRecyclerView() {
        adapter = new RecordingListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);
    }

    public static double[][] getMFCC(FileInputStream iStream) throws IOException, WavFileException {
        WavFile wavFile = WavFile.openWavFile(iStream);

        // Max buffer size; currently set to 5.11 secs
        int buffer_size = 245280;

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

        // Normalization stuff
//        double[] means = {7.17412761e+02,  8.09448977e+01, -3.22094204e+00,  2.67395995e+01,
//                -2.95364379e+00, -1.68174362e+00, -7.54160070e+00, -6.30995971e+00,
//                -2.19392249e+00, -2.69355131e+00, -3.47557558e+00, -1.61287202e+00,
//                -6.99222589e-01, -1.01427192e-01, -3.19738535e+00, -1.41767440e+00};
//        double[] std = {132.31567292,  62.67935034,  38.41606962,  31.46123938,
//                24.54817898,  20.31396172,  18.21243215,  17.26334167,
//                14.77640526,  13.54350092,  11.62770688,  11.31913029,
//                10.59833541,  10.04452268,   9.63493706,   9.03885952};

        double[] means = new double [16];
        double[] std = new double [16];

        for (int j = 0; j < 16; ++j) {
            for (int i = 0; i < mfcc_data[0].length; ++i) {
                means[j] += mfcc_data[j][i];
            }
            means[j] /= mfcc_data[0].length;
        }

        for (int j = 0; j < 16; ++j) {
            for (int i = 0; i < mfcc_data[0].length; ++i) {
                std[j] += (mfcc_data[j][i] - means[j]) * (mfcc_data[j][i] - means[j]);
            }
            std[j] /= mfcc_data[0].length;
            std[j] = Math.sqrt(std[j]);
        }

        for (int j = 0; j < 16; ++j) {
            for (int i = 0; i < mfcc_data[0].length; ++i) {
                mfcc_data[j][i] = (mfcc_data[j][i] - means[j]) / std[j];
            }
        }

        // Pad MFCC to fixed shape (960, 16)
//        double[][] mfcc_padded = new double[960][16];
//        for (int i = 0; i < mfcc_data[0].length; ++i) {
//            for (int j = 0; j < 16; ++j) {
//                mfcc_padded[i][j] = (mfcc_data[j][i] - means[j]) / std[j];
//            }
//        }

        return mfcc_data;
    }

    private double[][] cnn_process(double[][] data, int kernel_w, int stride_w) {
        double[][] res = new double[480][160];

        int start = 0;
        int iter_num = 0;
        while (start + kernel_w < data[0].length) {
            for (int i = start; i < start + kernel_w; ++i) {
                for (int j = 0 ; j < 16; ++j) {
                    res[iter_num][(i-start) * 16 + j] = data[j][i];
                }
            }
            start += stride_w;
            iter_num += 1;
        }

        return res;
    }

    private float[] load_wav_data(String fileURI, boolean fromAssets) {
        // Read file from assets
        FileInputStream iStream;
        try {
            if (fromAssets) {
                iStream = getApplicationContext().getAssets().openFd(fileURI).createInputStream();
            }
            else {
                iStream = new FileInputStream(new File(fileURI));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Get MFCC features from example file
        double[][] mfcc_data;
        try {
            mfcc_data = getMFCC(iStream);
        } catch (IOException | WavFileException e) {
            e.printStackTrace();
            return null;
        }

        // Apply CNN preprocessing
        double[][] data_cnn;
        data_cnn = cnn_process(mfcc_data, 10, 2);

//        Log.d("CoolStuff", String.valueOf(data_cnn[50][0]));
//        Log.d("CoolStuff", String.valueOf(data_cnn[88][0]));
//        Log.d("CoolStuff", String.valueOf(data_cnn[89][0]));
//        Log.d("CoolStuff", String.valueOf(data_cnn[400][0]));

        // Unravel input
        float[] input = new float[77056];
        for (int i = 0; i < 76800; ++i) {
            input[i] = (float) data_cnn[i / 160][i % 160];
        }
//        for (int i = 256; i < 77056; ++i) {
//            input[i] = (float) data_cnn[(i-256) / 160][(i-256) % 160];
//        }
        return input;
    }

    private String decodeCTCMatrix(float[] prediction) {
        double[][] matrix = new double[480][32];
        Log.d("ModelOutput", String.valueOf(matrix[10][0]));
        for (int i = 0; i < 480; ++i) {
            double sum = 0;
            for (int j = 0; j < 29; ++j) {
                if (i * 32 + j > prediction.length) {
                    break;
                }
                matrix[i][j] = Math.exp(prediction[i * 32 + j]);
                sum += matrix[i][j];
            }
            for (int j = 0; j < 29; ++j) {
                matrix[i][j] /= sum;
            }
            if (i * 32 > prediction.length) {
                break;
            }
        }

        // Do BeamSearch
        return WordBeamSearch.search(matrix, 25, languageModel, true);
    }
}
