package uk.ac.cam.pd451.dissertation.inference;

import uk.ac.cam.pd451.dissertation.graph.factor.FactorGraph;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.graph.factor.FactorNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a faster version of the VariableEnumerationInference method.
 * In the best case it is linear but in the worst case it is still exponential.
 */
public class FactorEliminationInference implements InferenceAlgorithm<FactorGraph> {

    private FactorGraph model;
    private Assignment evidence = new Assignment(List.of());

    @Override
    public void setModel(FactorGraph g) {
        this.model = g;
    }

    @Override
    public double infer(Assignment events, Assignment evidence) {
        this.setEvidence(evidence);
        return this.infer(events);
    }

    public double infer(Assignment events) {
        // check if evidence entails or contradicts
        if(evidence.contains(events)) {
            return 1.0;
        } else if(events.contradicts(evidence)) {
            return 0.0;
        }

        // else perform variable elimination
        List<FactorNode> order = model.topologicalOrdering();
        Collections.reverse(order);

        List<ConditionalProbabilityTable> factors = new ArrayList<ConditionalProbabilityTable>();
        for (FactorNode n : order) {
            Variable v = n.getVariable();
            factors.add(n.getParentalFactor());

            // don't sum out if variable is being inferred or is evidence
            if (!events.contains(v) && !evidence.contains(v)) {
                ConditionalProbabilityTable temp = factors.get(0);
                for (int i = 1; i < factors.size(); i++)
                    temp = temp.product(factors.get(i));
                temp = temp.eliminate(v);
                factors.clear();
                factors.add(temp);
            }
        }

        // make product of all remaining factors
        ConditionalProbabilityTable result = factors.get(0);
        for (int i = 1; i < factors.size(); i++)
            result = result.product(factors.get(i));

        // no evidence to incorporate, return P(events)
        if(evidence.events.isEmpty()) {
            return result.get(events);
        }

        // calculate P(evidence) from P(events, evidence) by elimination events
        ConditionalProbabilityTable onlyEvidence = result;
        for(Variable v : events.events.stream().map(Event::getVariable).collect(Collectors.toList())) {
            onlyEvidence = onlyEvidence.eliminate(v);
        }

        // incorporate evidence using P(events|evidence) = P(events,evidence)/P(evidence)
        return result.get(events.combineWith(evidence))/onlyEvidence.get(evidence);
    }

    @Override
    public void setEvidence(Assignment e) {
        this.evidence = e;
    }
}
