package com.example.isausmanov.scriboai.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.isausmanov.scriboai.R;
import com.example.isausmanov.scriboai.RecordingDataModel;
import com.example.isausmanov.scriboai.RecordingListAdapter;
import com.example.isausmanov.scriboai.ctc_decoder.LanguageModel;
import com.example.isausmanov.scriboai.ctc_decoder.WordBeamSearch;
import com.example.isausmanov.scriboai.database.AppDatabase;
import com.example.isausmanov.scriboai.mfcc.MFCC;
import com.example.isausmanov.scriboai.model.SpeakerchangedetectionModel;
import com.example.isausmanov.scriboai.model.SpeechrecognitionModel;
import com.example.isausmanov.scriboai.wav_reader.WavFile;
import com.example.isausmanov.scriboai.wav_reader.WavFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.arch.persistence.room.Room;
import android.widget.Toast;

public class RecordingListActivity extends AppCompatActivity {
    // RecordsList data
    private ArrayList<RecordingDataModel> dataModels;
    private List<RecordingDataModel> dataInList;
    private ListView listView;
    private RecordingListAdapter adapter;
    private AppDatabase db;
    private RecordingDataModel dataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        listView = findViewById(R.id.recording_list);

        dataModels = new ArrayList<>();

        fetchRecordings();

        listView.setOnItemClickListener(new MyItemClickListener());

        setAdaptertoRecyclerView();
    }

    private double detectSpeakerChange(double[][] data, int si1, int ei1, int si2, int ei2) {
        float[] embedding1, embedding2;
        float[] input = new float[3584];

        for (int i = si1; i < ei1; ++i) {
            for (int j = 0 ; j < 16; ++j) {
                input[(i - si1) * 16 + j] = (float) data[i][j];
            }
        }
        embedding1 = SpeakerchangedetectionModel.predict(input);

        for (int i = si2; i < ei2; ++i) {
            for (int j = 0 ; j < 16; ++j) {
                input[(i - si2) * 16 + j] = (float) data[i][j];
            }
        }
        embedding2 = SpeakerchangedetectionModel.predict(input);

        double similarity = 0;
        for (int i = 0; i < 32; ++i) {
            similarity += Math.pow(embedding1[i] - embedding2[i], 2);
        }
        similarity = Math.sqrt(similarity);

        return similarity;
    }


    private void fetchRecordings() {
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
        listView.setVisibility(View.VISIBLE);
    }

    private void setAdaptertoRecyclerView() {
        adapter = new RecordingListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);
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

    private Pair<String, ArrayList> decodeCTCMatrix(float[] prediction) {
        double[][] matrix = new double[prediction.length / 32][32];
        for (int i = 0; i < matrix.length; ++i) {
            double sum = 0;
            for (int j = 1; j < 29; ++j) {
                matrix[i][j] = Math.exp(prediction[i * 32 + j]);
                sum += matrix[i][j];
            }
            for (int j = 1; j < 29; ++j) {
                matrix[i][j] /= sum;
            }
        }

        LanguageModel languageModel = null;
        try {
            languageModel = MainActivity.languageModel.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // Do BeamSearch
        return WordBeamSearch.search(matrix, 50, languageModel);
    }

    private Pair<float[], Pair<double[][], Double>> getTranscriptionProbabilities(String fileURI) {
        float[] stackedOutputs;
        double[][] fullMFCC;
        double audioLength = 0;

        try {
            SpeechrecognitionModel.load(getAssets());
            MFCC mfcc = new MFCC();
            LinkedList<double[][]> modelInputs = new LinkedList<>();
            LinkedList<float[]> modelOutputs = new LinkedList<>();

            // Read file
            FileInputStream iStream = new FileInputStream(new File(fileURI));
            WavFile wavFile = WavFile.openWavFile(iStream);

            while(true) {
                // Max buffer size; currently set to 15.35 secs
                int buffer_size = 245600;

                double[] temp_buffer = new double[buffer_size];

                // Read file into temp buffer
                int wavFramesCount = wavFile.readFrames(temp_buffer, buffer_size);

                audioLength += wavFramesCount / 16000.;

                // Write file to specific buffer for MFCC
                double[] buffer = new double[wavFramesCount];
                for (int s = 0; s < wavFramesCount; s++) {
                    buffer[s] = 32768 * temp_buffer[s];
                }

                // Get MFCC
                double[][] mfcc_data = mfcc.process(buffer);

                modelInputs.add(mfcc_data);

                double[] means = new double[16];
                double[] std = new double[16];

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

                // Apply CNN preprocessing
                double[][] data_cnn;
                data_cnn = cnn_process(mfcc_data, 10, 2);

                // Unravel input
                float[] input = new float[76800 + 256];
                for (int i = 0; i < 76800; ++i) {
                    input[i] = (float) data_cnn[i / 160][i % 160];
                }

                float[] model_result = SpeechrecognitionModel.predict(input);
                modelOutputs.add(model_result);

                if (wavFramesCount < buffer_size) {
                    wavFile.close();
                    break;
                }
            }

            fullMFCC = new double[960 * (modelInputs.size() - 1) + modelInputs.getLast()[0].length][16];
            for (int i = 0; i < modelInputs.size(); ++i) {
                for (int j = 0; j < modelInputs.get(i)[0].length; ++j) {
                    for (int k = 0; k < 16; ++k) {
                        fullMFCC[i * 960 + j][k] = modelInputs.get(i)[k][j];
                    }
                }
            }

            stackedOutputs = new float[480 * 32 * modelOutputs.size()];
            for (int i = 0; i < modelOutputs.size(); ++i) {
                for (int j = 0; j < 480 * 32; ++j) {
                    stackedOutputs[480 * 32 * i + j] = modelOutputs.get(i)[j];
                }
            }

            SpeechrecognitionModel.unload();

        } catch (IOException | WavFileException e) {
            e.printStackTrace();
            return null;
        }

        return  new Pair<>(stackedOutputs, new Pair<>(fullMFCC, audioLength));
    }

    public class MyItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            dataModel = dataModels.get(position);

            ArrayList<String> transcriptionWords = null;
            ArrayList<Integer> transcriptionWordTimes = null;
            ArrayList<Integer> speakerChanged = null;

            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "production")
                    .allowMainThreadQueries()
                    .build();

            boolean rec_transcribed = dataModel.getTranscribed();

            if (rec_transcribed == true){
                // if the recording is transcribed already, do...

                //Log.d("DB_TEST", dataModel.getTranscription_words().get(5) + "  " + dataModel.getTranscription_word_times().get(5).toString());
                Intent i = new Intent(RecordingListActivity.this, RecordingDetailsActivity.class);
                i.putExtra("AUDIO_URI", dataModel.getUri());
                i.putExtra("AUDIO_TRANSCRIPTION", dataModel.getTranscription_words());
                i.putExtra("AUDIO_WORD_TIMES", dataModel.getTranscription_word_times());
                i.putExtra("AUDIO_SPEAKER_CHANGED", dataModel.getTranscription_speaker());
                startActivity(i);
            }else {
                // if recording has no transcription, do...
                Toast.makeText(getApplicationContext(), "Recording must be transcribed first", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void transcribeAudioFromDB(int position) {
        RecordingDataModel dataModel = dataModels.get(position);

        ArrayList<String> transcriptionWords = null;
        ArrayList<Integer> transcriptionWordTimes = null;
        ArrayList<Integer> speakerChanged = null;

        Pair<float[], Pair<double[][], Double>> transcriptionData = getTranscriptionProbabilities(dataModel.getUri());
        double[][] fullMFCC = transcriptionData.second.first;
        double audioDuration = transcriptionData.second.second;

        Pair<String, ArrayList> res = decodeCTCMatrix(transcriptionData.first);

        transcriptionWordTimes = res.second;
        for (int i = 0; i < transcriptionWordTimes.size(); ++i) {
            if (transcriptionWordTimes.get(i) * 2 * 255.8 / 16 >= audioDuration * 1000) {
                transcriptionWordTimes = new ArrayList<>(transcriptionWordTimes.subList(0, i));
                break;
            }
            transcriptionWordTimes.set(i, (int)(transcriptionWordTimes.get(i) * 2 * 255.8 / 16));
        }

        transcriptionWords = new ArrayList<>();
        String[] wordCandidates = res.first.split(" ");
        for (String wordCandidate : wordCandidates) {
            if (wordCandidate.equals("")){
                continue;
            }
            transcriptionWords.add(wordCandidate);

            if (transcriptionWords.size() == transcriptionWordTimes.size()) {
                break;
            }
        }

        for (int i = 0; i < fullMFCC.length; ++i) {
            double max = Double.MIN_VALUE;
            for (int j = 0; j < 16; ++j) {
                max = Math.max(max, fullMFCC[i][j]);
            }
            for (int j = 0; j < 16; ++j) {
                fullMFCC[i][j] = fullMFCC[i][j] / max;
            }
        }

        double mfcc_mean = 0;
        double mfcc_std = 0;
        for (int i = 0; i < fullMFCC.length; ++i) {
            for (int j = 0; j < 16; ++j) {
                mfcc_mean += fullMFCC[i][j];
            }
        }

        mfcc_mean /= fullMFCC.length * 16;

        for (int i = 0; i < fullMFCC.length; ++i) {
            for (int j = 0; j < 16; ++j) {
                mfcc_std += Math.pow(fullMFCC[i][j] - mfcc_mean, 2);
            }
        }

        mfcc_std = Math.sqrt(mfcc_std / (fullMFCC.length * 16));

        for (int i = 0; i < fullMFCC.length; ++i) {
            for (int j = 0; j < 16; ++j) {
                fullMFCC[i][j] = (fullMFCC[i][j] - mfcc_mean) / mfcc_std;
            }
        }

        speakerChanged = new ArrayList<>(
                Collections.nCopies(transcriptionWords.size(), 0)
        );
        speakerChanged.set(0, 1);

        int wordToBegin = Integer.MAX_VALUE, wordToEnd = -1;
        for (int i = 0; i < transcriptionWordTimes.size(); ++i) {
            int frameNum = (int)(transcriptionWordTimes.get(i) / 1000 * 960 / 15.35);

            if (frameNum - 1 >= 224 && wordToBegin == Integer.MAX_VALUE) {
                wordToBegin = i;
            }

            if (fullMFCC.length - frameNum >= 224) {
                wordToEnd = i;
            }
        }

        double[] differences = new double[Math.max(0, wordToEnd - wordToBegin + 1)];
        SpeakerchangedetectionModel.load(getAssets());
        for (int i = wordToBegin; i <= wordToEnd; ++i) {
            int middleFrame = (int) (transcriptionWordTimes.get(i) / 1000 * 960 / 15.35);
            differences[i - wordToBegin] = detectSpeakerChange(
                    fullMFCC,
                    middleFrame - 1 - 224, middleFrame - 1,
                    middleFrame, middleFrame + 224
            );
        }
        SpeakerchangedetectionModel.unload();

        double maxDifference = Double.MIN_VALUE;
        for (double difference : differences) {
            maxDifference = Math.max(maxDifference, difference);
        }
        for (int i = 0; i < differences.length; ++i) {
            differences[i] /= maxDifference;
            if (differences[i] >= 5) {
                speakerChanged.set(i + wordToBegin, 1);
            }
        }
        Log.d("Coolest", Arrays.toString(new ArrayList[]{transcriptionWords}));

        LanguageModel languageModel = null;
        try {
            languageModel = MainActivity.languageModel.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // Add dots
        int[] needDotAfter = new int[transcriptionWords.size()];
        for (int i = 5; i < transcriptionWords.size(); ++i) {
            long hash = 0;
            for (int j = i-5; j <= i; ++j) {
//                    Log.d("Coolest", String.valueOf(languageModel));
//                    Log.d("Coolest", String.valueOf(languageModel.wordToPostag));
//                    Log.d("Coolest", transcriptionWords.get(j));
                hash = hash * 35 + languageModel.wordToPostag.get(transcriptionWords.get(j));
            }
            Log.d("Coolest", String.valueOf(hash));
            if (languageModel.posTagSequenceDotHash.contains(hash)) {
                needDotAfter[i - 3] = 1;
            }
        }
        Log.d("Coolest", String.valueOf(languageModel.posTagSequenceDotHash.contains(1385650630L)));

        // Capitalize 1st word
        if (transcriptionWords.size() > 0)
            transcriptionWords.set(0,
                    transcriptionWords.get(0).substring(0, 1).toUpperCase() + transcriptionWords.get(0).substring(1));

        for (int i = 0; i < transcriptionWords.size(); ++i) {
            if (needDotAfter[i] == 1) {
                transcriptionWords.set(i, transcriptionWords.get(i) + ".");
                transcriptionWords.set(i + 1,
                        transcriptionWords.get(i + 1).substring(0, 1).toUpperCase() + transcriptionWords.get(i + 1).substring(1));
            }
        }

        // Add dot in the end
        if (transcriptionWords.size() > 0)
            transcriptionWords.set(transcriptionWords.size() - 1,
                    transcriptionWords.get(transcriptionWords.size() - 1) + ".");

        Log.d("Coolest", Arrays.toString(differences));
        Log.d("Coolest", Arrays.toString(new ArrayList[]{speakerChanged}));

        // Update the recording in the DB
        db.recordingDao().update(true, transcriptionWords, transcriptionWordTimes, speakerChanged, dataModel.getId());
        db.close();

        dataModel.setTranscription_word_times(transcriptionWordTimes);
        dataModel.setTranscribed(true);
        dataModel.setTranscription_words(transcriptionWords);
        dataModel.setTranscription_speaker(speakerChanged);
    }
}
