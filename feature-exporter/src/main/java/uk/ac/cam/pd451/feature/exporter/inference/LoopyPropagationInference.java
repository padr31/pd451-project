package uk.ac.cam.pd451.feature.exporter.inference;

import com.google.common.collect.Lists;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.factor.AssignmentTableFactor;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoopyPropagationInference implements InferenceAlgorithm<BayesianNetwork> {

    private final static long DEFAULT_LOOPY_ITERATIONS = 2;
    private BayesianNetwork bn;
    private Assignment evidence = new Assignment(List.of());

    Map<Variable, AssignmentTableFactor> lambdaValues = new HashMap<>();
    Map<Variable, Map<Variable, AssignmentTableFactor>> lambdaMessages = new HashMap<>();
    Map<Variable, AssignmentTableFactor> piValues = new HashMap<>();
    Map<Variable, Map<Variable, AssignmentTableFactor>> piMessages = new HashMap<>();
    Map<Variable, AssignmentTableFactor> conditionalProbs = new HashMap<>();


    @Override
    public void setModel(BayesianNetwork bn) {
        this.bn = bn;
        this.initialise();
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
            for (BayesianNode childX : nodeR.getChildSet()) {
                sendPiMessage(nodeR, childX);
            }
        }
        System.out.println("done");
        loopyPropagation();
    }

    private void sendPiMessage(BayesianNode nodeZ, BayesianNode nodeX) {
        //update pi message Z -> pi_x(z) -> X
        AssignmentTableFactor piXZ = piValues.get(nodeZ.getVariable());
        for(BayesianNode nodeY : nodeZ.getChildSet()) {
            if(!nodeY.getVariable().equals(nodeX.getVariable())) {
                piXZ = piXZ.product(
                        lambdaMessages
                                .get(nodeY.getVariable())
                                .get(nodeZ.getVariable())
                );
            }
        }
        piMessages.get(nodeX.getVariable()).put(nodeZ.getVariable(), piXZ);

        //calculate pi value of X
        if(!evidence.contains(nodeX.getVariable())) {
            AssignmentTableFactor piX = nodeX.getCPT();
            Map<Variable, AssignmentTableFactor> piXMessages = piMessages.get(nodeX.getVariable());
            for(BayesianNode nodeZi : nodeX.getParentSet()) {
                piX = piX.product(piXMessages.get(nodeZi.getVariable()));
            }
            //TODO put multinomial elimination inside the Factor class
            for(BayesianNode nodeZi : nodeX.getParentSet()) {
                piX = piX.eliminate(nodeZi.getVariable());
            }
            piValues.put(nodeX.getVariable(), piX);

            //calculate conditional prob of X
            AssignmentTableFactor probX = lambdaValues.get(nodeX.getVariable()).product(piX);
            probX.normalise();
            conditionalProbs.put(nodeX.getVariable(), probX);
        }
    }

    private void sendLambdaMessage(BayesianNode nodeY, BayesianNode nodeX) {
        //Y sends X a message
        AssignmentTableFactor lambdaYX = nodeY.getCPT();
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
        lambdaMessages.get(nodeY.getVariable()).put(nodeX.getVariable(), lambdaYX);

        //update lambda of X from the lambda messages of its children
        if(!nodeX.getChildSet().isEmpty()) {
            List<BayesianNode> childList = new ArrayList<>(nodeX.getChildSet());
            AssignmentTableFactor lambdaX = lambdaMessages.get(childList.get(0).getVariable()).get(nodeX.getVariable());
            for(int i = 1; i < childList.size(); i++) {
                lambdaX = lambdaX.product(lambdaMessages.get(childList.get(i).getVariable()).get(nodeX.getVariable()));
            }
            if(!evidence.contains(nodeX.getVariable())) {
                lambdaValues.put(nodeX.getVariable(), lambdaX);
            }
        }

        //update conditional probs of X
        if(!evidence.contains(nodeX.getVariable())) {
            AssignmentTableFactor probsX = lambdaValues.get(nodeX.getVariable()).product(piValues.get(nodeX.getVariable()));
            probsX.normalise();
            conditionalProbs.put(nodeX.getVariable(), probsX);
        }
    }

    private AssignmentTableFactor getUnitFactorFor(Variable x) {
        return new AssignmentTableFactor(List.of(x), Assignment.allAssignments(List.of(x)).stream().collect(Collectors.toMap(a -> a, a -> 1.0)));
    }

    private AssignmentTableFactor getOneHotFactorFor(Event e) {
        return new AssignmentTableFactor(List.of(e.getVariable()), Assignment.allAssignments(List.of(e.getVariable())).stream().collect(Collectors.toMap(a -> a, a -> a.contains(e) ? 1.0 : 0.0)));
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

        loopyPropagation();
    }

    private void loopyPropagation() {
        for(int i = 0; i < DEFAULT_LOOPY_ITERATIONS; i++) {
            System.out.println(i);
            System.out.println(bn.topologicalOrdering().size());
            for(BayesianNode n : bn.topologicalOrdering()) {
                n.getChildSet().forEach(c -> sendPiMessage(n, c));
            }

            for(BayesianNode n : Lists.reverse(bn.topologicalOrdering())) {
                n.getParentSet().forEach(p -> sendLambdaMessage(n, p));
            }
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