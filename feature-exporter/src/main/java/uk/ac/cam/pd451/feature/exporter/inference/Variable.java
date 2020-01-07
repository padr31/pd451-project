package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    public Event randomSample() {
        if(this.getDomain() == null || this.getDomain().isEmpty()) throw new ArrayIndexOutOfBoundsException("Cannot sample from empty domain.");
        List<Integer> dom = new ArrayList<>(domain);
        int r = new Random().nextInt(dom.size());
        return new Event(this, dom.get(r));
    }

    public Event sampleFromDistribution(Factor xFactor) {
        if(this.getDomain() == null || this.getDomain().isEmpty()) throw new ArrayIndexOutOfBoundsException("Cannot sample from empty domain.");
        double r = Math.random();
        // 0 will not be used as we have a guarantee that domain is not empty
        Event e = new Event(this, 0);
        for(int domElem : this.getDomain()) {
            e = new Event(this, domElem);
            r = r - xFactor.get(new Assignment(List.of(e)));
            if(r <= 0) return e;
        }
        return e;
    }
}
