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

    public void dump() {
        for (String key : beams.keySet()) {
            System.out.println(beams.get(key));
        }
    }
}
