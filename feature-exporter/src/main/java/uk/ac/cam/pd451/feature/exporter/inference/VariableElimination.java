package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VariableElimination implements InferenceAlgorithm {

    private FactorGraph model;
    private Assignment evidence = new Assignment(List.of());

    @Override
    public void setModel(FactorGraph g) {
        this.model = g;
    }

    @Override
    public Factor infer(Assignment events, Assignment evidence) {
        this.setEvidence(evidence);
        return this.infer(events);
    }

    @Override
    public Factor infer(Assignment events) {
        List<Variable> order = model.topologicalOrdering();
        Collections.reverse(order);

        List<Factor> factors = new ArrayList<Factor>();
        for (Variable v : order) {
            factors.add(v.getParentalFactor());

            // don't sum out if variable is evidence or is of interest
            if (!events.contains(v) && !evidence.contains(v)) {
                Factor temp = factors.get(0);
                for (int i = 1; i < factors.size(); i++)
                    temp = temp.product(factors.get(i));
                temp = temp.eliminate(v);
                factors.clear();
                factors.add(temp);
            }
        }

        // Point wise product of all remaining factors.
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++)
            result = result.product(factors.get(i));

        // Normalize the result factor
        result.normalise();

        // Return the result matching the query in string format.
        return result;
    }

    @Override
    public void setEvidence(Assignment e) {
        this.evidence = e;
    }
}
