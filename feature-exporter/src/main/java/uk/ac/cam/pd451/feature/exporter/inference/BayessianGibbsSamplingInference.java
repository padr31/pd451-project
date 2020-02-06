package uk.ac.cam.pd451.feature.exporter.inference;

import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BayessianGibbsSamplingInference implements InferenceAlgorithm<BayesianNetwork> {

    private BayesianNetwork bn;
    private Assignment evidence = new Assignment(List.of());

    //Should be Map<Variable, Map<Event, Integer>> but I will avoid creating Event objects every time
    private Map<Variable, Map<Integer, Double>> counts = new HashMap<>();

    private final static int DEFAULT_GIBBS_ITERATIONS = 100;

    @Override
    public void setModel(BayesianNetwork model) {
        this.bn = model;
    }

    @Override
    public double infer(Assignment events, Assignment evidence) {
        setEvidence(evidence);
        return infer(events);
    }

    public double infer(Assignment events) {
        Map<Variable, Event> state = new HashMap<>();
        double eventCount = 0.0;
        List<BayesianNode> topsort = bn.topologicalOrdering();

        //initialise counts to zero
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            counts.put(v, new HashMap<>());
            for(int i : v.getDomain()) {
                counts.get(v).put(i, 1.0);
            }
        }

        //initialise state - set unobserved variables to random samples from their domains
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            if(this.evidence.contains(v)) {
                state.put(v, new Event(v, this.evidence.getValue(v)));
            } else state.put(v, new Event(v, v.randomSample().getValue()));
        }

        for(int j = 0; j < DEFAULT_GIBBS_ITERATIONS; j++) {
            for(BayesianNode n : topsort) {
                Variable v = n.getVariable();
                if(!this.evidence.contains(v)) {
                    Event e = sampleBasedOnMarkovBlanket(v, state);
                    state.put(v, e);
                    counts.get(v).put(e.getValue(), counts.get(v).get(e.getValue()) + 1.0);
                }
            }
            boolean equalsInferredAssignment = true;
            for(Event e : events.events) {
                if(!state.get(e.getVariable()).equals(e)) equalsInferredAssignment = false;
            }
            if(equalsInferredAssignment) eventCount++;
        }

        //normalise counts
        for(Map<Integer, Double> count : counts.values()) {
            double sum = count.values().stream().mapToDouble(Double::doubleValue).sum();
            count.replaceAll((k, v) -> count.get(k) / sum);
        }

        double result = 1.0;
        for(Event e : events.events) {
            result *= this.counts.get(e.getVariable()).get(e.getValue());
        }
        return eventCount/DEFAULT_GIBBS_ITERATIONS;
    }

    private Event sampleBasedOnMarkovBlanket(Variable v, Map<Variable, Event> state) {
        BayesianNode nodeX = bn.getNode(v);

        Map<Integer, Double> childrenScalingFactorFunction = new HashMap<>();
        for(int domElem : v.getDomain()) {
            Event e = new Event(nodeX.getVariable(), domElem);
            childrenScalingFactorFunction.put(domElem, 1.0);

            for(BayesianNode child : nodeX.getChildSet()) {
                Event ce = state.get(child.getVariable());
                List<Event> parentsOfChildEvents = child.getParentSet().stream().map(n -> state.get(n.getVariable())).filter(ev -> !ev.getVariable().equals(v)).collect(Collectors.toList());
                Assignment parentsOfChildAssignment = new Assignment(parentsOfChildEvents);
                parentsOfChildAssignment = parentsOfChildAssignment.addEvent(e).addEvent(ce);
                if(child.getCPT().get(parentsOfChildAssignment) == null) {
                    continue;
                };
                childrenScalingFactorFunction.put(
                        domElem, childrenScalingFactorFunction.get(domElem)*child.getCPT().get(parentsOfChildAssignment));
            }
        }

        Map<Assignment, Double> xProbs = new HashMap<>();

        List<Event> parentsEvents = nodeX.getParentSet().stream().map(n -> state.get(n.getVariable())).collect(Collectors.toList());
        Assignment parentsAssignment = new Assignment(parentsEvents);

        for(int domElem : nodeX.getVariable().getDomain()) {
            Event e = new Event(nodeX.getVariable(), domElem);
            xProbs.put(new Assignment(List.of(e)), childrenScalingFactorFunction.get(domElem)*nodeX.getCPT().get(parentsAssignment.addEvent(e)));
        }

        Factor xFactor = new Factor(List.of(nodeX.getVariable()), xProbs);
        xFactor.normalise();

        return nodeX.getVariable().sampleFromDistribution(xFactor);
    }

    @Override
    public void setEvidence(Assignment evidence) {
        this.evidence = evidence;
    }

    public void addEvidence(Event e) {
        this.evidence.addEvent(e);
    }
}
