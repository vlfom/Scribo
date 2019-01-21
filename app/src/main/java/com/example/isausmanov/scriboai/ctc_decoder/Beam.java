package com.example.isausmanov.scriboai.ctc_decoder;

import android.util.Log;
import android.util.Pair;

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
    public StringBuilder text;
    public StringBuilder wordDev;
    public double prUnnormalized;
    public double prTotal;

    public String preLastString;
    public String lastString;
    public int wordHistSize;

    public LanguageModel languageModel;

    public Beam(LanguageModel languageModel) {
        this.prBlank = 1;
        this.prNonBlank = 0;

        this.text = new StringBuilder("");
        this.wordDev = new StringBuilder("");
        this.prUnnormalized = 1.0;
        this.prTotal = 1.0;

        this.languageModel = languageModel;

        this.time = new ArrayList<>();

        this.wordHistSize = 0;
    }

    public void mergeBeam(Beam beam) {
        if (!this.text.toString().equals(beam.text.toString())) {
            return;
        }

        this.prNonBlank += beam.prNonBlank;
        this.prBlank += beam.prBlank;
    }

    public List<Character> getNextChars() {
        return this.languageModel.getNextChars(this.wordDev.toString());
    }

    public Double getChildBeamProbability(Character newChar, double prBlank_, double prNonBlank_) {
        double prTotal = this.prTotal;

        if (newChar != null) {
            if (this.languageModel.ngram_usage != LanguageModel.NGRAM_NONE) {
                if (this.languageModel.wordChars.contains(newChar)) {
                    wordDev.append(newChar);

                    double prSum;

                    if (wordHistSize == 0 || languageModel.ngram_usage == LanguageModel.NGRAM_UNIGRAM) {
                        prSum = languageModel.getNextWordsUnigramProb(wordDev.toString());
                    }
                    else {
                        prSum = languageModel.getNextWordsBigramProb(lastString, wordDev.toString());
                    }

                    prTotal = prUnnormalized * prSum;
                    if (wordHistSize >= 1) {
                        prTotal = Math.pow(prTotal, 1.0 / (wordHistSize + 1));
                    }

                    wordDev.deleteCharAt(wordDev.length() - 1);
                }
                else {
                    if (wordDev.length() > 0) {

                        if (wordHistSize + 1 == 1 || languageModel.ngram_usage == LanguageModel.NGRAM_UNIGRAM) {
                            prUnnormalized *= languageModel.getUnigramProb(wordDev.toString());
                            prTotal = prUnnormalized;
                        }
                        else if (wordHistSize + 1 >= 2) {
                            prUnnormalized *= languageModel.getBigramProb(lastString, wordDev.toString());
                            prTotal = Math.pow(prUnnormalized, 1.0 / (wordHistSize + 1));
                        }
                    }
                }
            }
        }

        return (prBlank_ + prNonBlank_) * prTotal;
    }

    public Beam createChildBeam(Character newChar, double prBlank, double prNonBlank, int time) {
        Beam beam = new Beam(this.languageModel);

        beam.text = new StringBuilder(this.text);
        beam.wordDev = new StringBuilder(this.wordDev);

        beam.wordHistSize = this.wordHistSize;
        beam.preLastString = this.preLastString;
        beam.lastString = this.lastString;

        beam.prUnnormalized = this.prUnnormalized;
        beam.prTotal = this.prTotal;

        beam.prBlank = prBlank;
        beam.prNonBlank = prNonBlank;

        beam.time = new ArrayList<>(this.time);

        if (newChar != null) {
            if (!beam.languageModel.nonWordChars.contains(newChar) && beam.wordDev.length() == 0) {
                beam.time.add(time);
            }

            beam.text.append(newChar);

            if (beam.languageModel.ngram_usage != LanguageModel.NGRAM_NONE) {
                if (beam.languageModel.wordChars.contains(newChar)) {
                    beam.wordDev.append(newChar);

                    double prSum;

                    if (beam.wordHistSize == 0 || beam.languageModel.ngram_usage == LanguageModel.NGRAM_UNIGRAM) {
                        prSum = beam.languageModel.getNextWordsUnigramProb(beam.wordDev.toString());
                    }
                    else {
                        prSum = beam.languageModel.getNextWordsBigramProb(beam.lastString, beam.wordDev.toString());
                    }

                    beam.prTotal = beam.prUnnormalized * prSum;
                    if (beam.wordHistSize >= 1) {
                        beam.prTotal = Math.pow(beam.prTotal, 1.0 / (beam.wordHistSize + 1));
                    }
                }
                else {
                    if (beam.wordDev.length() > 0) {
                        beam.wordHistSize += 1;
                        beam.preLastString = beam.lastString;
                        beam.lastString = beam.wordDev.toString();

                        beam.wordDev.setLength(0);

                        int numWords = beam.wordHistSize;

                        if (numWords == 1 || beam.languageModel.ngram_usage == LanguageModel.NGRAM_UNIGRAM) {
                            beam.prUnnormalized *= beam.languageModel.getUnigramProb(beam.lastString);
                            beam.prTotal = beam.prUnnormalized;
                        }
                        else if (numWords >= 2) {
                            beam.prUnnormalized *= beam.languageModel.getBigramProb(beam.preLastString, beam.lastString);
                            beam.prTotal = Math.pow(beam.prUnnormalized, 1.0 / numWords);
                        }
                    }
                }
            }
            else {
                if (beam.languageModel.wordChars.contains(newChar)) {
                    beam.wordDev.append(newChar);
                }
                else {
                    beam.wordDev.setLength(0);
                }
            }
        }

        return beam;
    }

    public void completeBeam(LanguageModel languageModel) {
        String lastPrefix = this.wordDev.toString();
        if (lastPrefix.equals("") || languageModel.isWord(lastPrefix)) {
            return;
        }

        // TODO: this is bullshit, he just selects first word. Need to at least use unigrams
        List<String> words = languageModel.getNextWords(lastPrefix);

        String word = words.get(0);
        this.text.append(word.substring(lastPrefix.length()));
    }
}
