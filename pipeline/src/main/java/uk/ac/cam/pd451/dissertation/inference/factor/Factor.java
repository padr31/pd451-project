package uk.ac.cam.pd451.dissertation.inference.factor;

import uk.ac.cam.pd451.dissertation.inference.Assignment;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

/**
 * An abstract representation of a Factor --- discrete function over variables.
 * Encapsulates the three most common operations:
 * factor products. summing out variables, and normalisation.
 */
public abstract class Factor {

    public abstract Factor product(Factor other);

    public abstract Factor eliminate(Variable v);

    public abstract Double get(Assignment a);

    public abstract void normalise();

    @Override
    public abstract boolean equals(Object obj);
}
