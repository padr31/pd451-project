package uk.ac.cam.pd451.dissertation.inference.variable;

import uk.ac.cam.pd451.dissertation.datalog.Predicate;

public class VariablePredicateIdentifier extends VariableIdentifier {

    private Predicate p;

    public VariablePredicateIdentifier(Predicate p) {
        this.p = p;
    }

    @Override
    public int hashCode() {
        return p.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof VariablePredicateIdentifier)) {
            return false;
        } else {
            return this.p.equals(((VariablePredicateIdentifier) other).p);
        }
    }

    @Override
    public String toString() {
        return this.p.getName();
    }
}
