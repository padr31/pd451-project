package uk.ac.cam.pd451.dissertation.pipeline.run;

import uk.ac.cam.pd451.dissertation.datalog.Clause;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.dissertation.inference.Assignment;
import uk.ac.cam.pd451.dissertation.inference.Event;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNode;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.inference.variable.VariableClauseIdentifier;
import uk.ac.cam.pd451.dissertation.inference.variable.VariablePredicateIdentifier;
import uk.ac.cam.pd451.dissertation.pipeline.Step;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This step is responsible for converting ground clauses into a Bayesian network.
 * It creates links between BayesianNodes, and assigns ConditionalProbabilityTables.
 */
public class NetworkCreationStep implements Step<List<Clause>, ProvenanceGraph> {

    @Override
    public ProvenanceGraph process(List<Clause> groundClauses) throws PipeException {
        /*Neo4jOGMProvenanceConnector provenanceConnector = Neo4jOGMProvenanceConnector.getInstance();
        provenanceConnector.clearDatabase();
        provenanceConnector.loadGraph(groundClauses);*/

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
