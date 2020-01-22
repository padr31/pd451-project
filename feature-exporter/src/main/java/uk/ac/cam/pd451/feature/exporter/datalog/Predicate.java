package uk.ac.cam.pd451.feature.exporter.datalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Represents p("A", "B", "C") - predicate with terms "A", "B", "C", and arity 3.
 * Must be immutable.
 */
public class Predicate {

    private int arity;
    private List<String> terms;
    private String name;

    public Predicate(String name, String...terms) {
        this.name = name;
        this.terms = Arrays.asList(terms);
    }

    public Predicate(String name, List<String> terms) {
        this.name = name;
        this.terms = new ArrayList<>(terms);
    }
    
}
