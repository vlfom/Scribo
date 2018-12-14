package com.example.vlfom.emptymfccapplication.ctc_decoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WordBeamSearch {

    public static String search(double[][] mat, int beamWidth, LanguageModel languageModel, boolean useNGrams) {
        List<Character> chars = languageModel.allChars;
        int blankIdx = chars.size();
        int maxT = mat.length;

        Beam genesisBeam = new Beam(languageModel, useNGrams);
        BeamList last = new BeamList();
        last.addBeam(genesisBeam);

        for (int t = 0; t < maxT; ++t) {
            BeamList curr = new BeamList();

            List<Beam> bestBeams = last.getBestBeams(beamWidth);
            for (Beam beam : bestBeams) {
                double prNonBlank = 0;
                if (!beam.text.equals("")) {
                    int labelIdx = chars.indexOf(beam.text.charAt(beam.text.length() - 1));
                    prNonBlank = beam.prNonBlank * mat[t][labelIdx];
                }

                double prBlank = (beam.prBlank + beam.prNonBlank) * mat[t][blankIdx];

                curr.addBeam(beam.createChildBeam(null, prBlank, prNonBlank));

                List<Character> nextChars = beam.getNextChars();
                for (Character c : nextChars) {
                    int labelIdx = chars.indexOf(c);
                    if (!beam.text.equals("") && beam.text.charAt(beam.text.length() - 1) == c) {
                        prNonBlank = mat[t][labelIdx] * beam.prBlank;
                    }
                    else {
                        prNonBlank = mat[t][labelIdx] * (beam.prBlank + beam.prNonBlank);
                    }
                    curr.addBeam(beam.createChildBeam(c, 0, prNonBlank));
                }
            }
            last = curr;
        }

        last.completeBeams(languageModel);

        List<Beam> bestBeams = last.getBestBeams(1);

        return bestBeams.get(0).text;
    }
}
