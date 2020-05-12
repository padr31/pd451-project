package uk.ac.cam.pd451.dissertation.inference.variable;

import uk.ac.cam.pd451.dissertation.datalog.Clause;

public class VariableClauseIdentifier extends VariableIdentifier {

    private Clause c;

    public VariableClauseIdentifier(Clause c) {
        this.c = c;
    }

    @Override
    public int hashCode() {
        return c.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof VariableClauseIdentifier)) return false;
        else return this.c.equals(((VariableClauseIdentifier) other).c);
    }

    @Override
    public String toString() {
        return "cl: " + this.c.getHead().getName();
    }

    public Clause getClause() {
        return c;
    }
}
