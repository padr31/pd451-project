package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.*;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.*;
import uk.ac.cam.pd451.feature.exporter.neo4j.provenance.Neo4jOGMProvenanceConnector;

import java.util.*;
import java.util.stream.Collectors;

public class ProvenanceCreationStep implements Step<List<Clause>, ProvenanceGraph> {
    @Override
    public ProvenanceGraph process(List<Clause> groundClauses) throws PipeException {
        System.out.println("Eliminated clause count: " + groundClauses.size());

        Neo4jOGMProvenanceConnector provenanceConnector = Neo4jOGMProvenanceConnector.getInstance();
        provenanceConnector.clearDatabase();
        provenanceConnector.loadGraph(groundClauses);
        // create predicates and map them to unique provenance network variable names
        // some of them are inserted twice - this is not an issue as the uuid will just be updated
        Map<Predicate, String> predicateToUUID = new HashMap<>();
        for(Clause groundClause : groundClauses) {
            predicateToUUID.put(groundClause.getHead(), UUID.randomUUID().toString());
            for(Predicate bodyPredicate : groundClause.getBody()) {
                predicateToUUID.put(bodyPredicate, UUID.randomUUID().toString());
            }
        }

        // create the inverse mapping for looking up predicates of variables
        Map<String, Predicate> UUIDToPredicate = new HashMap<>();
        for(Map.Entry<Predicate, String> predicateToUUIDEntry : predicateToUUID.entrySet()) {
            UUIDToPredicate.put(predicateToUUIDEntry.getValue(), predicateToUUIDEntry.getKey());
        }

        // create bayesian nodes
        Set<Integer> binaryDomain = Set.of(0, 1);
        Map<String, BayesianNode> variableNameToBayesianNode = UUIDToPredicate
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> new BayesianNode(new Variable(key, binaryDomain)
                        ))
                );

        //Connect BayseianNodes correctly and set up CPTs
        /*
         *  Body_1()  Body_2() ... Body_n()
         *      \        |        /
         *       \       |       /
         *         Rule_node - name as head
         *               |
         *               |
         *             head()
         *
         * Rule_node is not included in the map of UUIDs to nodes as we never need to access it
         */
        Variable variable = null;
        for(Clause groundClause : groundClauses) {
            Predicate head = groundClause.getHead();
            List<Predicate> body = groundClause.getBody();

            String childID = predicateToUUID.get(head);
            for(Predicate parent : body) {
                String parentID = predicateToUUID.get(parent);
                if(variableNameToBayesianNode.get(parentID) != null)
                    variableNameToBayesianNode.get(childID)
                    .addParent(variableNameToBayesianNode.get(parentID));
            }

            List<Variable> bodyVariables = body.stream().map(p -> variableNameToBayesianNode.get(predicateToUUID.get(p)).getVariable()).collect(Collectors.toList());
            Variable headVariable = variableNameToBayesianNode.get(predicateToUUID.get(head)).getVariable();
            variable = headVariable;
            List<Variable> variables = new ArrayList<>(bodyVariables);
            variables.add(headVariable);

            Map<Assignment, Double> function = Assignment.allAssignments(variables).stream().collect(Collectors.toMap(a -> a, a -> {
                boolean allBodyVariablesTrue = true;
                for(Variable bv: bodyVariables)
                    if(a.getValue(bv) == 0) {
                        allBodyVariablesTrue = false;
                        break;
                    }
                if(allBodyVariablesTrue && a.getValue(headVariable) == 1) return 0.95;
                else if(allBodyVariablesTrue && a.getValue(headVariable) == 0) return 0.05;
                else if(!allBodyVariablesTrue && a.getValue(headVariable) == 1) return 0.0;
                else return 1.0;
            }));

            Factor cpt = new Factor(variables, function);
            variableNameToBayesianNode.get(childID).setCPT(cpt);
        }

        BayesianNetwork bn = new BayesianNetwork(new ArrayList<>(variableNameToBayesianNode.values()));

        // set probability of input relations to 1
        for(BayesianNode rootNode : bn.getRoots()) {
            rootNode.setCPT(new Factor(List.of(rootNode.getVariable()), Map.of(
                    new Assignment(List.of(new Event(rootNode.getVariable(), 0))), 0.0,
                    new Assignment(List.of(new Event(rootNode.getVariable(), 1))), 1.0
            )));
        }

        return new ProvenanceGraph(bn, variableNameToBayesianNode, predicateToUUID, UUIDToPredicate);
    }
}
