package com.example.vlfom.emptymfccapplication.ctc_decoder;

import java.util.LinkedList;
import java.util.List;

public class Beam {

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

    public boolean useNGrams;

    public Beam(LanguageModel languageModel, boolean useNGrams) {
        this.prBlank = 1;
        this.prNonBlank = 0;

        this.text = "";
        this.wordHist = new LinkedList<>();
        this.wordDev = "";
        this.prUnnormalized = 1.0;
        this.prTotal = 1.0;

        this.languageModel = languageModel;

        this.useNGrams = useNGrams;
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

    public Beam createChildBeam(Character newChar, double prBlank, double prNonBlank) {
        Beam beam = new Beam(this.languageModel, this.useNGrams);

        beam.text = this.text;
        beam.wordHist = new LinkedList<>(this.wordHist);
        beam.wordDev = this.wordDev;
        beam.prUnnormalized = this.prUnnormalized;
        beam.prTotal = this.prTotal;

        beam.prBlank = prBlank;
        beam.prNonBlank = prNonBlank;

        if (newChar != null) {
            beam.text = beam.text + newChar;

            if (this.useNGrams) {
                if (beam.languageModel.wordChars.contains(newChar)) {
                    beam.wordDev += newChar;
                    List<String> nextWords = beam.languageModel.getNextWords(beam.wordDev);

                    int numWords = beam.wordHist.size();
                    double prSum = 0;
                    if (numWords == 0) {
                        for (String word : nextWords) {
                            prSum += beam.languageModel.getUnigramProb(word);
                        }
                    }
                    else {
                        String lastWord = beam.wordHist.get(beam.wordHist.size() - 1);
                        for (String word : nextWords) {
                            prSum += beam.languageModel.getBigramProb(lastWord, word);
                        }
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

                        if (numWords == 1) {
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

    public String toString() {
        return text + " " + wordDev + " " + prBlank + " " + prNonBlank + " " + prUnnormalized + " " + prTotal + " " + ((prBlank + prNonBlank) * prTotal);
    }
}
