package uk.ac.cam.pd451.dissertation.inference;

import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNode;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.inference.variable.VariableClauseIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class implementing Gibbs Sampling on bayesian networks, which is a Markov Chain
 * Monte-Carlo approach to inference.
 *
 * The burn in period is set experimentally. There is not general advice on
 * what it should be for complex Bayesian Networks. The important thing is
 * for the algorithm to avoid the variance in samples at the beginning so
 * that it converges steadily.
 */
public class BayessianGibbsSamplingInference implements InferenceAlgorithm<BayesianNetwork> {

    private BayesianNetwork bn;
    private Assignment evidence = new Assignment(List.of());

    private final static int DEFAULT_GIBBS_ITERATIONS = 1000;
    private final static double BURN_IN_PERIOD = 0.2;
    private int iterations = DEFAULT_GIBBS_ITERATIONS;

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

        //initialise state - set unobserved variables to random samples from their domains
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            if(this.evidence.contains(v)) {
                state.put(v, new Event(v, this.evidence.getValue(v)));
            } else {
                Event e = new Event(v, v.randomSample().getValue());
                state.put(v, e);
            }
        }

        for(int j = 0; j < this.iterations; j++) {
            for(BayesianNode n : topsort) {
                Variable v = n.getVariable();
                if(!this.evidence.contains(v)) {
                    Event e = sampleBasedOnMarkovBlanket(v, state);
                    state.put(v, e);
                    //counts.get(v).put(e.getValue(), counts.get(v).get(e.getValue()) + 1.0);
                }
            }
            if(j > iterations*BURN_IN_PERIOD) {
                boolean equalsInferredAssignment = true;
                for(Event e : events.events) {
                    if(!state.get(e.getVariable()).equals(e)) equalsInferredAssignment = false;
                }
                if(equalsInferredAssignment) eventCount++;
            }
        }

        return eventCount/(iterations-iterations*BURN_IN_PERIOD);
    }

    public Map<Event, Double> infer(List<Event> events) {
        Map<Event, Double> resultMap = events.stream().collect(Collectors.toMap(e -> e, e -> 0.0));

        Map<Variable, Event> state = new HashMap<>();
        List<BayesianNode> topsort = bn.topologicalOrdering();

        //initialise state - set unobserved variables to random samples from their domains
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            if(this.evidence.contains(v)) {
                state.put(v, new Event(v, this.evidence.getValue(v)));
            } else {
                Event e = new Event(v, v.randomSample().getValue());
                state.put(v, e);
            }
        }

        for(int j = 0; j < this.iterations; j++) {
            for(BayesianNode n : topsort) {
                Variable v = n.getVariable();
                if(!this.evidence.contains(v)) {
                    Event e = sampleBasedOnMarkovBlanket(v, state);
                    state.put(v, e);
                }
            }
            if(j > this.iterations*BURN_IN_PERIOD) {
                for(Event e : events) {
                    if(state.get(e.getVariable()).equals(e)) resultMap.put(e, resultMap.get(e) + 1);
                }
            }
        }

        return resultMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()/(this.iterations-this.iterations*BURN_IN_PERIOD)));
    }

    public Map<String, Double> sampleRuleProbabilities() {
        Map<String, Double> ruleProbabilities = new HashMap<>();
        Map<String, Double> ruleCounts = new HashMap<>();

        Map<Variable, Event> state = new HashMap<>();
        List<BayesianNode> topsort = bn.topologicalOrdering();

        //initialise state - set unobserved variables to random samples from their domains
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            if(this.evidence.contains(v)) {
                state.put(v, new Event(v, this.evidence.getValue(v)));
            } else {
                Event e = new Event(v, v.randomSample().getValue());
                state.put(v, e);
                if(v.getId() instanceof VariableClauseIdentifier) {
                    VariableClauseIdentifier clID = (VariableClauseIdentifier) v.getId();
                    if(!ruleProbabilities.containsKey(clID.getClause().getFullRule())) {
                        ruleProbabilities.put(clID.getClause().getFullRule(), 0.0);
                    }
                    if(!ruleCounts.containsKey(clID.getClause().getFullRule())) {
                        ruleCounts.put(clID.getClause().getFullRule(), 0.0);
                    }
                }
            }
        }

        for(int j = 0; j < this.iterations; j++) {
            for(BayesianNode n : topsort) {
                Variable v = n.getVariable();
                if(!this.evidence.contains(v)) {
                    Event e = sampleBasedOnMarkovBlanket(v, state);
                    state.put(v, e);

                    // figure out if all body variables were true
                    final boolean[] allBodyVariablesTrue = {true};
                    n.getParentSet().stream().map(BayesianNode::getVariable).forEach(bv -> {
                        if(state.get(v).getValue()==0) allBodyVariablesTrue[0] =false;
                    });

                    if(v.getId() instanceof VariableClauseIdentifier) {
                        VariableClauseIdentifier clID = (VariableClauseIdentifier) v.getId();
                        if(ruleProbabilities.containsKey(clID.getClause().getFullRule())){
                            ruleCounts.put(
                                    clID.getClause().getFullRule(),
                                    ruleCounts.get(clID.getClause().getFullRule())+1.0
                            );
                            if(e.getValue() == 1 && allBodyVariablesTrue[0]) {
                                ruleProbabilities.put(
                                        clID.getClause().getFullRule(),
                                        ruleProbabilities.get(clID.getClause().getFullRule())+1.0
                                );
                            }
                        }
                    }
                }
            }
        }
        ruleProbabilities.replaceAll((rule, trueCount) -> trueCount/ruleCounts.get(rule));
        return ruleProbabilities;
    }

    // run inference up to a certain amount of factor multiplications, return map of samples after each sampleRate-amount of factor multiplications
    // one blanket sampling corresponds roughly to one factor multiplication due to optimization done in blanket sampling
    public Map<Long, Map<Event, Double>> inferUpToFactorMultiplications(List<Event> events, long requiredFactorMultiplications, long sampleRate) {
        Map<Event, Double> resultMap = events.stream().collect(Collectors.toMap(e -> e, e -> 0.0));
        Map<Long, Map<Event, Double>> result = new HashMap<>();
        Map<Variable, Event> state = new HashMap<>();
        List<BayesianNode> topsort = bn.topologicalOrdering();

        //initialise state - set unobserved variables to random samples from their domains
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            if(this.evidence.contains(v)) {
                state.put(v, new Event(v, this.evidence.getValue(v)));
            } else {
                Event e = new Event(v, v.randomSample().getValue());
                state.put(v, e);
            }
        }

        long iter = 0;
        long fact = 0;
        long factDiff = 0;
        while(fact <= requiredFactorMultiplications) {
            for(BayesianNode n : topsort) {
                Variable v = n.getVariable();
                if(!this.evidence.contains(v)) {
                    Event e = sampleBasedOnMarkovBlanket(v, state);
                    fact++;
                    factDiff++;
                    state.put(v, e);
                }
            }
            for(Event e : events) {
                if(state.get(e.getVariable()).equals(e)) resultMap.put(e, resultMap.get(e) + 1);
            }
            iter++;
            if(factDiff > sampleRate) {
                factDiff = 0;
                long finalIter = iter;
                result.put(fact, new HashMap<>(resultMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()/(finalIter)))));
            }
        }

        return result;
    }

    public Map<Long, Map<Event, Double>> inferUpToTime(List<Event> events, long requiredTimeMillis, long sampleRateMillis) {
        Map<Event, Double> resultMap = events.stream().collect(Collectors.toMap(e -> e, e -> 0.0));
        Map<Long, Map<Event, Double>> result = new HashMap<>();
        Map<Variable, Event> state = new HashMap<>();
        List<BayesianNode> topsort = bn.topologicalOrdering();

        //initialise state - set unobserved variables to random samples from their domains
        for(BayesianNode node : topsort) {
            Variable v = node.getVariable();
            if(this.evidence.contains(v)) {
                state.put(v, new Event(v, this.evidence.getValue(v)));
            } else {
                Event e = new Event(v, v.randomSample().getValue());
                state.put(v, e);
            }
        }

        int iter = 0;
        long time = System.currentTimeMillis();
        long timeDiff = System.currentTimeMillis();
        while(System.currentTimeMillis()-time <= requiredTimeMillis) {
            for(BayesianNode n : topsort) {
                Variable v = n.getVariable();
                if(!this.evidence.contains(v)) {
                    Event e = sampleBasedOnMarkovBlanket(v, state);
                    state.put(v, e);
                }
            }
            for(Event e : events) {
                if(state.get(e.getVariable()).equals(e)) resultMap.put(e, resultMap.get(e) + 1);
            }
            iter++;
            if(System.currentTimeMillis() - timeDiff > sampleRateMillis) {
                timeDiff = System.currentTimeMillis();
                int finalIter = iter;
                result.put(System.currentTimeMillis() - time, new HashMap<>(resultMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()/(finalIter)))));
            }
        }

        return result;
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

        ConditionalProbabilityTable xFactor = new ConditionalProbabilityTable(List.of(nodeX.getVariable()), xProbs);
        xFactor.normalise();

        return nodeX.getVariable().sampleFromDistribution(xFactor);
    }

    @Override
    public void setEvidence(Assignment evidence) {
        this.evidence = evidence;
    }

    public void addEvidence(Event e) {
        this.evidence = this.evidence.addEvent(e);
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }


}
