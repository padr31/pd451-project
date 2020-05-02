package uk.ac.cam.pd451.feature.exporter.inference;

import com.google.common.collect.Lists;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of the randomised schedule for belief propagation
 * that allows inference on graphs containing loops,
 * for example Bayesian Networks.
 */
public class LoopyPropagationInference implements InferenceAlgorithm<BayesianNetwork> {

    /**
     * The number of messages sent. This value should change based on the network size.
     */
    private final static long DEFAULT_LOOPY_ITERATIONS = 100000;
    private long iterations = DEFAULT_LOOPY_ITERATIONS;

    private BayesianNetwork bn;
    private Assignment evidence = new Assignment(List.of());

    Map<Variable, ConditionalProbabilityTable> lambdaValues = new HashMap<>();
    Map<Variable, Map<Variable, ConditionalProbabilityTable>> lambdaMessages = new HashMap<>();
    Map<Variable, ConditionalProbabilityTable> piValues = new HashMap<>();
    Map<Variable, Map<Variable, ConditionalProbabilityTable>> piMessages = new HashMap<>();
    Map<Variable, ConditionalProbabilityTable> conditionalProbs = new HashMap<>();
    private String strategy = "random";

    @Override
    public void setModel(BayesianNetwork bn) {
        this.bn = bn;
        this.initialise();
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    private void initialise() {
        this.evidence = new Assignment(List.of());
        for(BayesianNode nodeX : bn.topologicalOrdering()) {
            Variable X = nodeX.getVariable();

            //init lambda values
            lambdaValues.put(X, getUnitFactorFor(X));


            //init lambda messages
            for(BayesianNode nodeZ : nodeX.getParentSet()) {
                Variable Z = nodeZ.getVariable();
                if(!lambdaMessages.containsKey(X)) lambdaMessages.put(X, new HashMap<>());
                lambdaMessages.get(X).put(Z, getUnitFactorFor(Z));
            }

            //pi values
            ConditionalProbabilityTable f = getHalfHalfFactorFor(X);
            piValues.put(X, f);

            //conditional probs
            conditionalProbs.put(X, f);

            //init pi messages
            for(BayesianNode nodeY : nodeX.getChildSet()) {
                Variable Y = nodeY.getVariable();
                if(!piMessages.containsKey(Y)) piMessages.put(Y, new HashMap<>());
                piMessages.get(Y).put(X, getUnitFactorFor(X));
            }
        }

        //init pi values
        for(BayesianNode nodeR : bn.getRoots()) {
            conditionalProbs.put(nodeR.getVariable(), nodeR.getCPT());
            piValues.put(nodeR.getVariable(), nodeR.getCPT());
        }

        //loopyPropagation(1);
        randomPropagation(DEFAULT_LOOPY_ITERATIONS);
    }

    private void sendPiMessage(BayesianNode nodeZ, BayesianNode nodeX) {
        //update pi message Z -> pi_x(z) -> X
        ConditionalProbabilityTable piXZ = piValues.get(nodeZ.getVariable());
        for(BayesianNode nodeY : nodeZ.getChildSet()) {
            if(!nodeY.getVariable().equals(nodeX.getVariable())) {
                piXZ = piXZ.product(
                        lambdaMessages
                                .get(nodeY.getVariable())
                                .get(nodeZ.getVariable())
                );
            }
        }
        piXZ.normalise();
        piMessages.get(nodeX.getVariable()).put(nodeZ.getVariable(), piXZ);

        //calculate pi value of X
        if(!evidence.contains(nodeX.getVariable())) {
            ConditionalProbabilityTable piX = nodeX.getCPT();
            Map<Variable, ConditionalProbabilityTable> piXMessages = piMessages.get(nodeX.getVariable());
            for(BayesianNode nodeZi : nodeX.getParentSet()) {
                piX = piX.product(piXMessages.get(nodeZi.getVariable()));
            }
            //TODO put multinomial elimination inside the Factor class
            for(BayesianNode nodeZi : nodeX.getParentSet()) {
                piX = piX.eliminate(nodeZi.getVariable());
            }
            piX.normalise();
            piValues.put(nodeX.getVariable(), piX);

            //calculate conditional prob of X
            ConditionalProbabilityTable probX = lambdaValues.get(nodeX.getVariable()).product(piX);
            probX.normalise();
            conditionalProbs.put(nodeX.getVariable(), probX);
        }
    }

    private void sendLambdaMessage(BayesianNode nodeY, BayesianNode nodeX) {
        //Y sends X a message
        ConditionalProbabilityTable lambdaYX = nodeY.getCPT();
        for(BayesianNode nodeWi : nodeY.getParentSet()) {
            if(!nodeWi.getVariable().equals(nodeX.getVariable()))
                lambdaYX = lambdaYX.product(piMessages.get(nodeY.getVariable()).get(nodeWi.getVariable()));
        }
        for(BayesianNode nodeWi : nodeY.getParentSet()) {
            if(!nodeWi.getVariable().equals(nodeX.getVariable()))
                lambdaYX = lambdaYX.eliminate(nodeWi.getVariable());
        }
        lambdaYX = lambdaYX.product(lambdaValues.get(nodeY.getVariable()));
        lambdaYX = lambdaYX.eliminate(nodeY.getVariable());
        lambdaYX.normalise();
        lambdaMessages.get(nodeY.getVariable()).put(nodeX.getVariable(), lambdaYX);

        //update lambda of X from the lambda messages of its children
        if(!nodeX.getChildSet().isEmpty()) {
            List<BayesianNode> childList = new ArrayList<>(nodeX.getChildSet());
            ConditionalProbabilityTable lambdaX = lambdaMessages.get(childList.get(0).getVariable()).get(nodeX.getVariable());
            for(int i = 1; i < childList.size(); i++) {
                lambdaX = lambdaX.product(lambdaMessages.get(childList.get(i).getVariable()).get(nodeX.getVariable()));
            }
            if(!evidence.contains(nodeX.getVariable())) {
                lambdaX.normalise();
                lambdaValues.put(nodeX.getVariable(), lambdaX);
            }
        }

        //update conditional probs of X
        if(!evidence.contains(nodeX.getVariable())) {
            ConditionalProbabilityTable probsX = lambdaValues.get(nodeX.getVariable()).product(piValues.get(nodeX.getVariable()));
            probsX.normalise();
            conditionalProbs.put(nodeX.getVariable(), probsX);
        }
    }

    private ConditionalProbabilityTable getUnitFactorFor(Variable x) {
        return new ConditionalProbabilityTable(List.of(x), Assignment.allAssignments(List.of(x)).stream().collect(Collectors.toMap(a -> a, a -> 1.0)));
    }

    private ConditionalProbabilityTable getOneHotFactorFor(Event e) {
        return new ConditionalProbabilityTable(List.of(e.getVariable()), Assignment.allAssignments(List.of(e.getVariable())).stream().collect(Collectors.toMap(a -> a, a -> a.contains(e) ? 1.0 : 0.0)));
    }

    private ConditionalProbabilityTable getHalfHalfFactorFor(Variable x) {
        return new ConditionalProbabilityTable(List.of(x), Assignment.allAssignments(List.of(x)).stream().collect(Collectors.toMap(a -> a, a -> Math.random() > 0.5 ? 0.1 : 0.9)));
    }

    @Override
    public double infer(Assignment events, Assignment evidence) {
        this.setEvidence(evidence);
        return infer(events);
    }

    public Map<Event, Double> infer(List<Event> events) {
        return events
                .stream()
                .collect(Collectors.toMap(
                        e -> e,
                        e -> conditionalProbs.get(e.getVariable()).get(new Assignment(List.of(e)))
                    )
                );
    }

    public double infer(Assignment events) {
        double result = 1.0;
        for(Event e : events.events) {
            result *= this.conditionalProbs.get(e.getVariable()).get(new Assignment(List.of(e)));
        }
        return result;
    }

    public void addEvidence(Event e) {
        // instantiate the network with the evidence
        this.evidence = this.evidence.addEvent(e);
        lambdaValues.put(e.getVariable(), getOneHotFactorFor(e));
        piValues.put(e.getVariable(), getOneHotFactorFor(e));
        conditionalProbs.put(e.getVariable(), getOneHotFactorFor(e));

        randomPropagation(DEFAULT_LOOPY_ITERATIONS);
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    private void loopyPropagation(long iterations) {
        for(int i = 0; i < iterations; i++) {
            for(BayesianNode n : bn.topologicalOrdering()) {
                n.getChildSet().forEach(c -> sendPiMessage(n, c));
            }

            for(BayesianNode n : Lists.reverse(bn.topologicalOrdering())) {
                n.getParentSet().forEach(p -> sendLambdaMessage(n, p));
            }
        }
    }

    private void randomPropagation(long iterations) {
        List<BayesianNode> nodes = new ArrayList<>(bn.topologicalOrdering());
        Random r = new Random();
        for(int i = 0; i < iterations; i++) {
            BayesianNode n = nodes.get(r.nextInt(nodes.size()));
            n.getChildSet().forEach(c -> sendPiMessage(n, c));
            n.getParentSet().forEach(p -> sendLambdaMessage(n, p));
        }
    }

    @Override
    public void setEvidence(Assignment evidence) {
        this.initialise();
        for(Event e : evidence.events) {
            this.addEvidence(e);
        }
    }
}
