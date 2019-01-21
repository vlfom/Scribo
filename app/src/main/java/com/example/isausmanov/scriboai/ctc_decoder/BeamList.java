package com.example.isausmanov.scriboai.ctc_decoder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeamList {
    public Map<String, Beam> beams;

    public BeamList() {
        this.beams = new HashMap<>();
    }

    public void addBeam(Beam beam) {
        if (!beams.containsKey(beam.text)) {
            this.beams.put(beam.text, beam);
        }
        else {
            this.beams.get(beam.text).mergeBeam(beam);
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

    public void completeBeams(LanguageModel languageModel) {
        for (Map.Entry<String, Beam> entry : this.beams.entrySet()) {
            String lastPrefix = entry.getValue().wordDev;
            if (lastPrefix.equals("") || languageModel.isWord(lastPrefix)) {
                continue;
            }

            // TODO: this is bullshit, he just selects first word. Need to at least use unigrams
            List<String> words = languageModel.getNextWords(lastPrefix);
            if (words.size() == 1) {
                String word = words.get(0);
                entry.getValue().text = entry.getValue().text + word.substring(lastPrefix.length());
            }
        }
    }

    public void dump() {
        for (String key : beams.keySet()) {
            System.out.println(beams.get(key));
        }
    }
}
