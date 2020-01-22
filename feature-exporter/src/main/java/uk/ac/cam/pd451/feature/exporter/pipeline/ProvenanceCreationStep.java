package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.*;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.inference.BayessianGibbsSamplingInference;
import uk.ac.cam.pd451.feature.exporter.inference.Variable;

import java.util.*;
import java.util.stream.Collectors;

public class ProvenanceCreationStep implements Step<List<GroundClausePOJO>, ProvenanceGraph> {
    @Override
    public ProvenanceGraph process(List<GroundClausePOJO> groundClauses) throws PipeException {
        // create predicates and map them to unique provenance network variable names
        Map<Predicate, String> predicateToUUID = new HashMap<>();
        for(GroundClausePOJO groundClause : groundClauses) {
            predicateToUUID.put(new Predicate(groundClause.getPredicate(), groundClause.getTerms()), UUID.randomUUID().toString());
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
        for(GroundClausePOJO groundClause : groundClauses) {
            Predicate head = new Predicate(groundClause.getPredicate(), groundClause.getTerms());
            List<Predicate> body = groundClause.getProvenance()
                    .getBody()
                    .stream()
                    .map(p -> new Predicate(
                            p.getPredicate(),
                            p.getTerms().stream().map(ClauseTermPOJO::getContents).collect(Collectors.toList()))).collect(Collectors.toList());

            String childID = predicateToUUID.get(head);
            for(Predicate parent : body) {
                String parentID = predicateToUUID.get(parent);
                if(variableNameToBayesianNode.get(parentID) != null)
                    variableNameToBayesianNode.get(childID)
                    .addParent(variableNameToBayesianNode.get(parentID));
            }
        }

        BayesianNetwork bn = new BayesianNetwork(new ArrayList<>(variableNameToBayesianNode.values()));
        BayessianGibbsSamplingInference i = new BayessianGibbsSamplingInference();
        i.setModel(bn);

        return new ProvenanceGraph(bn, variableNameToBayesianNode, predicateToUUID, UUIDToPredicate);
    }
}
