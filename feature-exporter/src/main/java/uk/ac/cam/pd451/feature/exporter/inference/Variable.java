package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.HashSet;
import java.util.Set;

public class Variable implements Comparable{

    private String name;
    private Set<Integer> domain;

    private Set<Variable> parentSet;
    private Set<Variable> childSet;

    private Factor parentalFactor;

    public Variable(String name, Set<Integer> domain) {
        this.name = name;
        this.domain = domain;
        this.parentSet = new HashSet<>();
        this.childSet = new HashSet<>();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Variable)) return false;
        return ((Variable) other).getName().equals(this.name);
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

    public Set<Variable> getParentSet() {
        return parentSet;
    }

    public Set<Variable> getChildSet() {
        return childSet;
    }

    public Factor getParentalFactor() {
        return parentalFactor;
    }

    public void addParent(Variable p) {
        this.parentSet.add(p);
        p.childSet.add(this);
    }

    public void addChild(Variable c) {
        this.childSet.add(c);
        c.parentSet.add(this);
    }

    public void setParentalFactor(Factor f) {
        this.parentalFactor = f;
    }
}
