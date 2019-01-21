package com.example.isausmanov.scriboai.ctc_decoder;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WordBeamSearch {

    public static Pair<String, ArrayList> search(double[][] mat, int beamWidth, LanguageModel languageModel) {
        List<Character> chars = languageModel.allChars;
        int blankIdx = chars.size();

        Beam genesisBeam = new Beam(languageModel);
        BeamList last = new BeamList();
        last.addBeam(genesisBeam);

        for (int t = 0; t < mat.length; ++t) {
            BeamList curr = new BeamList();

            List<Beam> bestBeams = last.getBestBeams(beamWidth);

            for (Beam beam : bestBeams) {
                double prNonBlank = 0;
                if (!beam.text.equals("")) {
                    int labelIdx = chars.indexOf(beam.text.charAt(beam.text.length() - 1));
                    prNonBlank = beam.prNonBlank * mat[t][labelIdx];
                }
                double prBlank = (beam.prBlank + beam.prNonBlank) * mat[t][blankIdx];
                curr.addBeam(beam.createChildBeam(null, prBlank, prNonBlank, t));

                List<Character> nextChars = beam.getNextChars();
                for (Character c : nextChars) {
                    int labelIdx = chars.indexOf(c);
                    if (!beam.text.equals("") && beam.text.charAt(beam.text.length() - 1) == c) {
                        prNonBlank = mat[t][labelIdx] * beam.prBlank;
                    }
                    else {
                        prNonBlank = mat[t][labelIdx] * (beam.prBlank + beam.prNonBlank);
                    }
                    curr.addBeam(beam.createChildBeam(c, 0, prNonBlank, t));
                }
            }
            last = curr;
        }

        Beam bestBeam = last.getBestBeams(1).get(0);
        bestBeam.completeBeam(languageModel);

        return new Pair<>(bestBeam.text, bestBeam.time);
    }
}