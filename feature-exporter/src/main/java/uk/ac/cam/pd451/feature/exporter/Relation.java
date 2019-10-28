package uk.ac.cam.pd451.feature.exporter;

import javafx.util.Pair;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Relation {

    private String name;
    private Set<Pair<String, String>> pairs;

    public Relation(String name) {
        this.name = name;
        this.pairs = new HashSet<>();
    }

    public void addPair(Pair<String, String> p) {
        this.pairs.add(p);
    }

    public void addAll(Relation r) {
        this.pairs.addAll(r.getPairs());
    }

    public void addAllFromSet(Set<Pair<String, String>> pairs) {
        this.pairs.addAll(pairs);
    }

    private Set<Pair<String, String>> getPairs() {
        return this.pairs;
    }

    public void writeToCSV(File dir) throws IOException {
        if(!dir.exists()) dir.mkdir();
        File csvOutputFile = new File(dir.getAbsolutePath() + File.separator + this.name + ".csv");
        FileWriter writer = new FileWriter(csvOutputFile, true);
        try (PrintWriter pw = new PrintWriter(writer)) {
            this.pairs.forEach(pair -> pw.println(pair.getKey() + "," + pair.getValue()));
        }
    }
}
