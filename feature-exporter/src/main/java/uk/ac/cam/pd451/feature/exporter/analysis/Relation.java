package uk.ac.cam.pd451.feature.exporter.analysis;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Relation {

    private String name;
    private final int arity;
    private Set<RelationEntry> entries;

    public Relation(String name, int arity) {
        this.name = name;
        this.arity = arity;
        this.entries = new HashSet<>();
    }

    public void addEntry(RelationEntry entry) {
        if (entry.getArity() != this.arity) throw new ArrayIndexOutOfBoundsException("Entry arity does not match Relation arity.");
        this.entries.add(entry);
    }

    public void writeToCSV(File dir) throws IOException {
        if(!dir.exists()) dir.mkdir();
        File csvOutputFile = new File(dir.getAbsolutePath() + File.separator + this.name + ".csv");
        FileWriter writer = new FileWriter(csvOutputFile, false);
        try (PrintWriter pw = new PrintWriter(writer)) {
            this.entries.forEach(entry -> pw.println(entry.getCSVString()));
        }
    }
}
