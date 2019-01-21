package com.example.isausmanov.scriboai.ctc_decoder;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class BeamList {
    public Map<String, Beam> beams;

    public BeamList() {
        this.beams = new HashMap<>();
    }

    public void addBeam(Beam beam) {
        if (!beams.containsKey(beam.text.toString())) {
            this.beams.put(beam.text.toString(), beam);
        }
        else {
            this.beams.get(beam.text.toString()).mergeBeam(beam);
        }
    }

    public List<Beam> getBestBeams(int num) {
        List<Beam> bestBeams = new LinkedList<>(beams.values());

        bestBeams.sort(Comparator.comparingDouble(o -> -(o.prBlank + o.prNonBlank) * o.prTotal));

        List<Beam> result = bestBeams.subList(0, Math.min(num, bestBeams.size()));
        int maxPowTotal = 0;
        for (Beam beam : result) {
            maxPowTotal = Math.max(maxPowTotal, -(int)(Math.log10(beam.prBlank)));
            maxPowTotal = Math.max(maxPowTotal, -(int)(Math.log10(beam.prNonBlank)));
            maxPowTotal = Math.max(maxPowTotal, -(int)(Math.log10(beam.prUnnormalized)));
        }
        int canDo = 1;
        int maxPowPr = 0;
        for (Beam beam : result) {
            if (beam.prBlank >= 0.1 || beam.prNonBlank >= 0.1) {
                canDo = 0;
                break;
            }
            maxPowPr = Math.max(maxPowPr, -(int)(Math.log10(beam.prUnnormalized)) - 2);
        }
        for (Beam beam : result) {
            if (canDo == 1) {
                beam.prBlank *= 10;
                beam.prNonBlank *= 10;
            }
            beam.prUnnormalized *= Math.pow(10, maxPowPr);
        }
        return result;
    }
}

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
                if (beam.text.length() > 0) {
                    int labelIdx = chars.indexOf(beam.text.charAt(beam.text.length() - 1));
                    prNonBlank = beam.prNonBlank * mat[t][labelIdx];
                }
                double prBlank = (beam.prBlank + beam.prNonBlank) * mat[t][blankIdx];
                curr.addBeam(beam.createChildBeam(null, prBlank, prNonBlank, t));

                List<Character> nextChars = beam.getNextChars();
                for (Character c : nextChars) {
                    int labelIdx = chars.indexOf(c);
                    if (beam.text.length() > 0 && beam.text.charAt(beam.text.length() - 1) == c) {
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

        return new Pair<>(bestBeam.text.toString(), bestBeam.time);
    }
}