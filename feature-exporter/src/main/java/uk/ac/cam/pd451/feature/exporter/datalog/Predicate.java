package uk.ac.cam.pd451.feature.exporter.datalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Represents p("A", "B", "C") - predicate with terms "A", "B", "C", and arity 3.
 * Must be immutable.
 */
public class Predicate {

    private String name;
    private int arity;
    private List<String> terms;

    public Predicate(String name, String...terms) {
        this.name = name;
        this.arity = terms.length;
        this.terms = Arrays.asList(terms);
    }

    public Predicate(String name, List<String> terms) {
        this.name = name;
        this.arity = terms.size();
        this.terms = new ArrayList<>(terms);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Predicate)) return false;
        return  this.name.equals(((Predicate) other).name) &&
                this.terms.equals(((Predicate) other).terms);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.terms.hashCode();
    }

    public String getName() {
        return this.name;
    }
}
