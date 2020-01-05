package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.Set;

public class Variable implements Comparable{

    private String name;
    private Set<Integer> domain;

    public Variable(String name, Set<Integer> domain) {
        this.name = name;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Variable)) return false;
        return ((Variable) other).getName().equals(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String getName() {
        return this.name;
    }

    public Set<Integer> getDomain() {
        return this.domain;
    }

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof Variable)) {
            throw new ClassCastException();
        }
        return this.name.compareTo(((Variable) o).name);
    }
}
