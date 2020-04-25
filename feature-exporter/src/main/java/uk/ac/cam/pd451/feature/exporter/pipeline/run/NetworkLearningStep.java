package uk.ac.cam.pd451.feature.exporter.pipeline.run;

import uk.ac.cam.pd451.feature.exporter.datalog.*;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.*;
import uk.ac.cam.pd451.feature.exporter.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;
import uk.ac.cam.pd451.feature.exporter.inference.variable.VariableClauseIdentifier;
import uk.ac.cam.pd451.feature.exporter.inference.variable.VariablePredicateIdentifier;
import uk.ac.cam.pd451.feature.exporter.pipeline.Step;
import uk.ac.cam.pd451.feature.exporter.utils.CSV;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkLearningStep implements Step<List<Clause>, ProvenanceGraph> {

    private static final int DEFAULT_LEARNING_ITERATIONS = 10;
    private static final double DEFAULT_RULE_PROB = 0.95;

    @Override
    public ProvenanceGraph process(List<Clause> groundClauses) throws PipeException {
        Map<String, Boolean> isPositive = CSV.readRankCSV();

        ProvenanceGraph graph = initialiseNetwork(groundClauses);

        // set evidence
        List<Event> evidenceEvents = new ArrayList<>();
        for(Predicate p : graph.getPredicateToNode().keySet()) {
            if(isPositive.containsKey(p.getTerms())) {
                evidenceEvents.add(
                    new Event(
                        graph.getPredicateToNode().get(p).getVariable(),
                        isPositive.get(p.getTerms()) ? 1 : 0
                    )
                );
            }
        }

        BayessianGibbsSamplingInference i = new BayessianGibbsSamplingInference();
        i.setModel(graph.getBayesianNetwork());

        Map<Event, Double> inferredEvidenceEvents = i.infer(evidenceEvents);
        System.out.println("Initial loss: " + inferredEvidenceEvents.values().stream().mapToDouble(d -> d).sum()/inferredEvidenceEvents.size());

        Map<String, Double> ruleProbabilities = new HashMap<>();
        // sample rule probabilities
        // construct network with new probabilities
        for(int iter = 0; iter < DEFAULT_LEARNING_ITERATIONS; iter++) {
            System.out.println("Learning network iteration: " + iter);
            i.setEvidence(new Assignment(evidenceEvents));
            ruleProbabilities = i.sampleRuleProbabilities();
            ruleProbabilities.forEach((rule, prob) -> System.out.println(rule + " : " + prob));
            setRuleProbabilities(ruleProbabilities, graph);

            i.setEvidence(new Assignment(List.of()));
            inferredEvidenceEvents = i.infer(evidenceEvents);
            System.out.println("Loss: " + inferredEvidenceEvents.values().stream().mapToDouble(d -> d).sum()/inferredEvidenceEvents.size());
            inferredEvidenceEvents.forEach((e, d) -> System.out.println("Event: " + e.getValue() + " Prob: " + d));
            Map<Event, Double> trueEvidence = i.infer(evidenceEvents.stream().map(e -> new Event(e.getVariable(), 1)).collect(Collectors.toList()));
            System.out.println("True evidence");
            inferredEvidenceEvents.forEach((e, d) -> System.out.println("Event true value: " + e.getValue() + " Prob being true: " + trueEvidence.get(new Event(e.getVariable(), 1))));
        }

        //write out
        File dir = new File("out_learning");
        if(!dir.exists()) dir.mkdir();

        File inspectedPredicatesCSV = new File(dir.getAbsolutePath() + File.separator + "rule_probs.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(inspectedPredicatesCSV, true))) {
            ruleProbabilities.forEach((rule, prob) -> pw.println(rule + ';' + prob));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return graph;
    }

    private void setRuleProbabilities(Map<String, Double> ruleProbabilities, ProvenanceGraph graph) {
        for(BayesianNode node : graph.getClauseToNode().values()) {
            String rule = ((VariableClauseIdentifier) node.getVariable().getId()).getClause().getFullRule();
            double ruleProb = ruleProbabilities.containsKey(rule) ? ruleProbabilities.get(rule) : DEFAULT_RULE_PROB;
            List<Variable> bodyVariables = node.getParentSet().stream().map(BayesianNode::getVariable).collect(Collectors.toList());
            Variable clauseVariable = node.getVariable();

            List<Variable> assignmentVariables = new ArrayList<>(bodyVariables);
            assignmentVariables.add(clauseVariable);

            Map<Assignment, Double> function = Assignment.allAssignments(assignmentVariables).stream().collect(Collectors.toMap(a -> a, a -> {
                boolean allBodyVariablesTrue = true;
                for(Variable bv: bodyVariables)
                    if(a.getValue(bv) == 0) {
                        allBodyVariablesTrue = false;
                        break;
                    }
                if(allBodyVariablesTrue && a.getValue(clauseVariable) == 1) return ruleProb;
                else if(allBodyVariablesTrue && a.getValue(clauseVariable) == 0) return 1-ruleProb;
                else if(!allBodyVariablesTrue && a.getValue(clauseVariable) == 1) return 0.1;
                else return 0.9;
            }));

            ConditionalProbabilityTable CPT = new ConditionalProbabilityTable(assignmentVariables, function);
            node.setCPT(CPT);
        }
    }

    private ProvenanceGraph initialiseNetwork(List<Clause> groundClauses) {
        // create predicates and map them to unique provenance network variable names
        // some of them are inserted twice - this is not an issue as the uuid will just be updated
        System.out.println("Collecting predicates");
        Set<Predicate> predicates = new HashSet<>();
        for(Clause groundClause : groundClauses) {
            predicates.add(groundClause.getHead());
            predicates.addAll(groundClause.getBody());
        }

        // create bayesian nodes for predicated
        System.out.println("Creating bayesian nodes for predicates");
        Set<Integer> binaryDomain = Set.of(0, 1);
        Map<Predicate, BayesianNode> predicateToNode = predicates
                .stream()
                .collect(Collectors.toMap(
                        predicate -> predicate,
                        predicate -> new BayesianNode(new Variable(new VariablePredicateIdentifier(predicate), binaryDomain)
                        ))
                );

        // create bayesian nodes for ground clauses
        System.out.println("Creating bayesian nodes for clauses");

        Map<Clause, BayesianNode> clauseToNode = groundClauses
                .stream()
                .collect(Collectors.toMap(
                        clause -> clause,
                        clause -> new BayesianNode(new Variable(new VariableClauseIdentifier(clause), binaryDomain))
                ));

        System.out.println("Connecting predicate nodes");
        final int[] maxAntecedents = {0};
        //Connect nodes for all groundClauses do
        /*
         * a1     a2     a3  - antecedents (body)
         *  \    |    /
         *   \   |   /
         *    | gc |
         *       |
         *       c  - consequent (head)
         */
        clauseToNode.forEach((cl, node) -> {
            node.addChild(predicateToNode.get(cl.getHead()));
            cl.getBody().forEach(bodyPred -> node.addParent(predicateToNode.get(bodyPred)));
            if(cl.getBody().size() > maxAntecedents[0]){
                maxAntecedents[0] = cl.getBody().size();
                if(maxAntecedents[0] == 6) {
                    System.out.println();
                }
            }
        });

        System.out.println("Setting CPTs for ground clauses");

        // Assign CPTs to groundClause nodes
        /*
         *    a1        a2         a3
         *      \        |        /
         *       \       |       /
         *            | gc |
         *  P( gc | a1 & a2 & a3)    = 0.95
         *  P(!gc | a1 & a2 & a3)    = 0.05
         *  P( gc | !(a1 & a2 & a3)) = 0.1
         *  P(!gc | !(a1 & a2 & a3)) = 0.9
         *
         */
        clauseToNode.forEach((cl, node) -> {
            List<Variable> bodyVariables = node.getParentSet().stream().map(BayesianNode::getVariable).collect(Collectors.toList());
            Variable clauseVariable = node.getVariable();

            List<Variable> assignmentVariables = new ArrayList<>(bodyVariables);
            assignmentVariables.add(clauseVariable);

            Map<Assignment, Double> function = Assignment.allAssignments(assignmentVariables).stream().collect(Collectors.toMap(a -> a, a -> {
                boolean allBodyVariablesTrue = true;
                for(Variable bv: bodyVariables)
                    if(a.getValue(bv) == 0) {
                        allBodyVariablesTrue = false;
                        break;
                    }
                if(allBodyVariablesTrue && a.getValue(clauseVariable) == 1) return 0.95;
                else if(allBodyVariablesTrue && a.getValue(clauseVariable) == 0) return 0.05;
                else if(!allBodyVariablesTrue && a.getValue(clauseVariable) == 1) return 0.1;
                else return 0.9;
            }));

            ConditionalProbabilityTable CPT = new ConditionalProbabilityTable(assignmentVariables, function);
            node.setCPT(CPT);
        });

        System.out.println("Setting CPTs for predicates");

        // Assign CPTs for predicate nodes
        /*
         *    | gc1 |  | gc2 |  | gc3 |
         *        \       |       /
         *         \      |      /
         *                c
         *  P( c | gc1 || gc2 || gc3)    = 1.0
         *  P(!c | gc1 || gc2 || gc3)    = 0.0
         *  P( c | !(gc1 || gc2 || gc3)) = 0.0
         *  P(!c | !(gc1 || gc2 || gc3)) = 1.0
         *
         * if node is root, then its p is 1
         *
         *       c
         *      /|\
         * P( c) = 1.0
         * P(!c) = 0.0
         */
        predicateToNode.forEach((pred, node) -> {
            // if node is root - predicate is an input clause
            if(node.getParentSet().size() == 0) {
                node.setCPT(
                        new ConditionalProbabilityTable(List.of(node.getVariable()), Map.of(
                                new Assignment(List.of(new Event(node.getVariable(), 0))), 0.0,
                                new Assignment(List.of(new Event(node.getVariable(), 1))), 1.0
                        )
                        )
                );
            }
            // if node is non root
            else {
                List<Variable> ancestorVariables = node.getParentSet().stream().map(BayesianNode::getVariable).collect(Collectors.toList());

                if(ancestorVariables.size() > maxAntecedents[0]) {
                    maxAntecedents[0] = ancestorVariables.size();
                    if(maxAntecedents[0] == 6) {
                        System.out.println();
                    }
                }
                Variable predVariable = node.getVariable();
                List<Variable> assignmentVariables = new ArrayList<>(ancestorVariables);
                assignmentVariables.add(predVariable);

                List<Variable> finalAncestorVariables = ancestorVariables;
                Map<Assignment, Double> function = Assignment.allAssignments(assignmentVariables).stream().collect(Collectors.toMap(a -> a, a -> {
                    boolean orOfAncestors = false;
                    for(Variable bv: finalAncestorVariables)
                        if(a.getValue(bv) == 1) {
                            orOfAncestors = true;
                            break;
                        }
                    if(orOfAncestors && a.getValue(predVariable) == 1) return 0.9;
                    else if(orOfAncestors && a.getValue(predVariable) == 0) return 0.1;
                    else if(!orOfAncestors && a.getValue(predVariable) == 1) return 0.1;
                    else return 0.9;
                }));

                ConditionalProbabilityTable CPT = new ConditionalProbabilityTable(assignmentVariables, function);
                node.setCPT(CPT);
            }
        });

        System.out.println("max antecedents: " + maxAntecedents[0]);
        System.out.println("Creating bayesian network");
        List<BayesianNode> allNodes = new ArrayList<>(clauseToNode.values());
        allNodes.addAll(predicateToNode.values());
        BayesianNetwork bn = new BayesianNetwork(allNodes);

        return new ProvenanceGraph(bn, predicateToNode, clauseToNode);
    }
}
