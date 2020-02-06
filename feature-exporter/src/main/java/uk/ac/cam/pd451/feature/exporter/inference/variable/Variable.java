package uk.ac.cam.pd451.feature.exporter.inference.variable;

import uk.ac.cam.pd451.feature.exporter.inference.Assignment;
import uk.ac.cam.pd451.feature.exporter.inference.Event;
import uk.ac.cam.pd451.feature.exporter.inference.Factor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Variable {

    private VariableIdentifier id;
    private Set<Integer> domain;

    public Variable(String id, Set<Integer> domain) {
        this.id = new VariableNameIdentifier(id);
        this.domain = domain;
    }

    public Variable(VariableIdentifier id, Set<Integer> domain) {
        this.id = id;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Variable)) return false;
        return ((Variable) other).getId().equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public VariableIdentifier getId() {
        return this.id;
    }

    public Set<Integer> getDomain() {
        return this.domain;
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
