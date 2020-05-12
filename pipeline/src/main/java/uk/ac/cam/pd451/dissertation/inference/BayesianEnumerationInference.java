package uk.ac.cam.pd451.dissertation.inference;

import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The simplest exact inference method that works by summing out
 * the variables that are not of interest.
 *
 * The time complexity of this inference algorithm is exponential.
 */
public class BayesianEnumerationInference implements InferenceAlgorithm<BayesianNetwork> {

    private BayesianNetwork bn;
    private Assignment evidence = new Assignment(List.of());

    @Override
    public void setModel(BayesianNetwork bn) {
        this.bn = bn;
    }

    public double infer(Assignment events) {
        if(this.bn == null) throw new RuntimeException("No model set for inference.");
        if(evidence.contains(events)) return 1.0;
        if(events.contradicts(evidence)) return 0.0;

        List<BayesianNode> bnNodes = bn.topologicalOrdering();
        ConditionalProbabilityTable jointDistribution = bnNodes.get(0).getCPT();
        for(int i = 1; i < bnNodes.size(); i++) {
            jointDistribution = jointDistribution.product(bnNodes.get(i).getCPT());
        }

        for(Variable v : bnNodes.stream().map(BayesianNode::getVariable).collect(Collectors.toList())) {
            if(!(evidence.contains(v) || events.contains(v))) {
                jointDistribution = jointDistribution.eliminate(v);
            }
        }


        ConditionalProbabilityTable evidenceDistribution = jointDistribution;
        for(Variable v : events.getVariables()) {
            evidenceDistribution = evidenceDistribution.eliminate(v);
        }

        jointDistribution.normalise();
        evidenceDistribution.normalise();
        return jointDistribution.get(events.combineWith(evidence))/evidenceDistribution.get(evidence);
    }

    @Override
    public double infer(Assignment events, Assignment evidence) {
        this.setEvidence(evidence);
        return this.infer(events);
    }

    @Override
    public void setEvidence(Assignment evidence) {
        this.evidence = evidence;
    }
}
