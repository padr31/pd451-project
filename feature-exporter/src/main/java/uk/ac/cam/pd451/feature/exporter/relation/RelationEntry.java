package uk.ac.cam.pd451.feature.exporter.relation;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RelationEntry {

    private final int arity;
    private String[] elements;

    public RelationEntry(int arity, String...elements) {
        this.arity = arity;
        if(elements.length != arity) throw new ArrayIndexOutOfBoundsException("Number of elements does not match arity.");
        this.elements = elements;
    }

    public int getArity() {
        return arity;
    }

    public String getCSVString() {
        return String.join(",", Arrays.stream(elements).map(s -> "\"" + s + "\"").collect(Collectors.toList()));
    }
}
