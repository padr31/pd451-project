package uk.ac.cam.pd451.feature.exporter.analysis;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Relation {

    public String getName() {
        return name;
    }

    public int getSize() {
        if(entries != null) return entries.size();
        else return 0;
    }

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
        FileWriter writer = new FileWriter(csvOutputFile, true);
        try (PrintWriter pw = new PrintWriter(writer)) {
            this.entries.forEach(entry -> pw.println(entry.getCSVString()));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof  Relation)) return false;
        return this.arity == ((Relation) obj).arity && this.name.equals(((Relation) obj).name);
    }

    public void appendTo(Relation relation) {
        if(!this.equals(relation)) throw new RuntimeException("Cannot append to a different relation.");
        this.entries.forEach(relation::addEntry);
    }

    public boolean contains(RelationEntry relationEntry) {
        return this.entries.contains(relationEntry);
    }
}
