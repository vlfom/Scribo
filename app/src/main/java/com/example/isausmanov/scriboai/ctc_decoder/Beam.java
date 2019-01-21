package com.example.isausmanov.scriboai.ctc_decoder;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Beam {
    // Time
    public ArrayList<Integer> time;

    // Optical score
    public double prBlank;
    public double prNonBlank;

    // Textual score
    public String text;
    public List<String> wordHist;
    public String wordDev;
    public double prUnnormalized;
    public double prTotal;

    public LanguageModel languageModel;

    public Beam(LanguageModel languageModel) {
        this.prBlank = 1;
        this.prNonBlank = 0;

        this.text = "";
        this.wordHist = new LinkedList<>();
        this.wordDev = "";
        this.prUnnormalized = 1.0;
        this.prTotal = 1.0;

        this.languageModel = languageModel;

        this.time = new ArrayList<>();
    }

    public boolean mergeBeam(Beam beam) {
        if (!this.text.equals(beam.text)) {
            return false;
        }

        this.prNonBlank += beam.prNonBlank;
        this.prBlank += beam.prBlank;

        return true;
    }

    public List<Character> getNextChars() {
        return this.languageModel.getNextChars(this.wordDev);
    }

    public Beam createChildBeam(Character newChar, double prBlank, double prNonBlank, int time) {
        Beam beam = new Beam(this.languageModel);

        beam.text = this.text;
        beam.wordHist = new LinkedList<>(this.wordHist);
        beam.wordDev = this.wordDev;
        beam.prUnnormalized = this.prUnnormalized;
        beam.prTotal = this.prTotal;

        beam.prBlank = prBlank;
        beam.prNonBlank = prNonBlank;

        beam.time = new ArrayList<>(this.time);

        if (newChar != null) {
            if (!beam.languageModel.nonWordChars.contains(newChar) && beam.wordDev.equals("")) {
                beam.time.add(time);
            }

            beam.text = beam.text + newChar;

            if (beam.languageModel.ngram_usage != LanguageModel.NGRAM_NONE) {
                if (beam.languageModel.wordChars.contains(newChar)) {
                    beam.wordDev += newChar;

                    int numWords = beam.wordHist.size();
                    double prSum = 0;

                    if (numWords == 0 || beam.languageModel.ngram_usage == LanguageModel.NGRAM_UNIGRAM) {
                        prSum += beam.languageModel.getNextWordsUnigramProb(beam.wordDev);
                    }
                    else {
                        String lastWord = beam.wordHist.get(beam.wordHist.size() - 1);
                        prSum += beam.languageModel.getNextWordsBigramProb(lastWord, beam.wordDev);
                    }

                    beam.prTotal = beam.prUnnormalized * prSum;
                    if (numWords >= 1) {
                        beam.prTotal = Math.pow(beam.prTotal, 1.0 / (numWords + 1));
                    }
                }
                else {
                    if (!beam.wordDev.equals("")) {
                        beam.wordHist.add(beam.wordDev);
                        beam.wordDev = "";

                        int numWords = beam.wordHist.size();

                        if (numWords == 1 || beam.languageModel.ngram_usage == LanguageModel.NGRAM_UNIGRAM) {
                            beam.prUnnormalized *= beam.languageModel.getUnigramProb(beam.wordHist.get(beam.wordHist.size() - 1));
                            beam.prTotal = beam.prUnnormalized;
                        }
                        else if (numWords >= 2) {
                            beam.prUnnormalized *= beam.languageModel.getBigramProb(beam.wordHist.get(beam.wordHist.size() - 2), beam.wordHist.get(beam.wordHist.size() - 1));
                            beam.prTotal = Math.pow(beam.prUnnormalized, 1.0 / numWords);
                        }
                    }
                }
            }
            else {
                if (beam.languageModel.wordChars.contains(newChar)) {
                    beam.wordDev += newChar;
                }
                else {
                    beam.wordDev = "";
                }
            }
        }

        return beam;
    }

    public void completeBeam(LanguageModel languageModel) {
        String lastPrefix = this.wordDev;
        if (lastPrefix.equals("") || languageModel.isWord(lastPrefix)) {
            return;
        }

        Log.d("Coolest", lastPrefix);

        // TODO: this is bullshit, he just selects first word. Need to at least use unigrams
        List<String> words = languageModel.getNextWords(lastPrefix);

        String word = words.get(0);
        this.text = this.text + word.substring(lastPrefix.length());
    }

    public String toString() {
        return text + " " + wordDev + " " + prBlank + " " + prNonBlank + " " + prUnnormalized + " " + prTotal + " " + ((prBlank + prNonBlank) * prTotal);
    }
}
