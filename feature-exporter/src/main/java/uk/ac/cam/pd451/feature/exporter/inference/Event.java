package uk.ac.cam.pd451.feature.exporter.inference;

import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

/**
 * Encapsulates the valuation of a single variable --- or a random event.
 *
 * This class is immutable and can be used as map keys.
 */
public class Event {
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
        return this.getVariable().getId().hashCode();
    }

    public Variable getVariable() {
        return variable;
    }

    public int getValue() {
        return value;
    }

}
