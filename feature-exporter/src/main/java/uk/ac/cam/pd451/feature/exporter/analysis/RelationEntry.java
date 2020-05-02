package uk.ac.cam.pd451.feature.exporter.analysis;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a relation entry such as alloc("variable", "object").
 * The name and arity are not represented in the class and are assigned
 * by inserting the entry into a Relation.
 */
public class RelationEntry {

    private String[] elements;

    public RelationEntry(String...elements) {
        this.elements = elements;
    }

    public int getArity() {
        return elements.length;
    }

    /**
     * @return The elements of the relation entry separated by commas.
     */
    public String getCSVString() {
        return Arrays.stream(elements).map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof RelationEntry)) {
            return false;
        } else {
            RelationEntry otherEntry = (RelationEntry) obj;
            if(this.getArity() != otherEntry.getArity()) return false;
            for(int i = 0; i < this.getArity(); i++) {
                if(!this.elements[i].equals(otherEntry.elements[i])) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return String.join("", elements).hashCode();
    }
}
