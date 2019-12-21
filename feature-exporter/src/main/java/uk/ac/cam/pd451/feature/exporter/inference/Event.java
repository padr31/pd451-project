package uk.ac.cam.pd451.feature.exporter.inference;

public class Event implements Comparable {
    private Variable variable;
    private int value;

    public Event(Variable variable, int value) {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Event)) return false;
        Event o = (Event) other;
        return (o.getValue() == this.value) && o.getVariable().equals(this.variable);
    }

    @Override
    public int hashCode() {
        return this.getVariable().getName().hashCode();
    }

    public Variable getVariable() {
        return variable;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(Object o) {
        if(!(o instanceof Event)) {
            throw new ClassCastException();
        }
        return this.variable.compareTo(((Event) o).variable);
    }
}
