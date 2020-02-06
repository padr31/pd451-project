package uk.ac.cam.pd451.feature.exporter.inference;

import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.List;
import java.util.stream.Collectors;

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
        Factor jointDistribution = bnNodes.get(0).getCPT();
        for(int i = 1; i < bnNodes.size(); i++) {
            jointDistribution = jointDistribution.product(bnNodes.get(i).getCPT());
        }

        for(Variable v : bnNodes.stream().map(BayesianNode::getVariable).collect(Collectors.toList())) {
            if(!(evidence.contains(v) || events.contains(v))) {
                jointDistribution = jointDistribution.eliminate(v);
            }
        }


        Factor evidenceDistribution = jointDistribution;
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

    /*@Override
    public double infer(Assignment events, Assignment evidence) {
        if(this.bn == null) throw new RuntimeException("No model set for inference.");
        if(evidence.contains(events)) return 1.0;
        if(events.contradicts(evidence)) return 0.0;

        List<BayesianNode> bnNodes = bn.topologicalOrdering();
        List<Variable> variables = new ArrayList<>(events.getVariables());

        Map<Assignment, Double> distribution = new HashMap<Assignment, Double>();
        for (Assignment a : Assignment.allAssignments(variables)) {
            distribution.put(a, enumerateAll(bnNodes, evidence.combineWith(a)));
        }

        Factor result = new Factor(variables, distribution);
        result.normalise();
        return result.get(events);
    }

    private double enumerateAll(List<BayesianNode> bnNodes, Assignment evidence) {
        if(bnNodes.isEmpty()) return 1.0;
        Variable v = bnNodes.get(0).getVariable();
        List<BayesianNode> rest = bnNodes.subList(1, bnNodes.size());
        if(evidence.contains(v)) {
            return evidence.
        }
    }*/

    @Override
    public void setEvidence(Assignment evidence) {
        this.evidence = evidence;
    }
}
