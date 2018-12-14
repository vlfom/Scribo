package com.example.vlfom.emptymfccapplication.ctc_decoder;

import java.util.*;

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

        return bestBeams.subList(0, Math.min(num, bestBeams.size()));
    }

    public void deletePartialBeams(LanguageModel languageModel) {
        for (Map.Entry<String, Beam> entry : this.beams.entrySet()) {
            String lastWord = entry.getValue().wordDev;
            if (!lastWord.equals("") && !languageModel.isWord(lastWord)) {
                this.beams.remove(lastWord);
            }
        }
    }

    public void completeBeams(LanguageModel languageModel) {
        for (Map.Entry<String, Beam> entry : this.beams.entrySet()) {
            String lastPrefix = entry.getValue().wordDev;
            if (lastPrefix.equals("") || languageModel.isWord(lastPrefix)) {
                continue;
            }

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
