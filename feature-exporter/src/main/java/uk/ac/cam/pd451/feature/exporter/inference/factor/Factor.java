package uk.ac.cam.pd451.feature.exporter.inference.factor;

import uk.ac.cam.pd451.feature.exporter.inference.Assignment;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

public abstract class Factor {

    public abstract Factor product(Factor other);

    public abstract Factor eliminate(Variable v);

    public abstract Double get(Assignment a);

    public abstract void normalise();

    @Override
    public abstract boolean equals(Object obj);
}
