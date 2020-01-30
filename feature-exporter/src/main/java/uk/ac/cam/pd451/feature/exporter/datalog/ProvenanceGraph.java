package uk.ac.cam.pd451.feature.exporter.datalog;

import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;

import java.util.Map;

public class ProvenanceGraph {

    private BayesianNetwork bn;


    private Map<String, BayesianNode> UUIDToNode;
    private Map<Predicate, String> predicateToUUDI;
    private Map<String, Predicate> UUDIToPredicate;

    public ProvenanceGraph(BayesianNetwork bn, Map<String, BayesianNode> UUIDToNode, Map<Predicate, String> predicateToUUDI, Map<String, Predicate> UUDIToPredicate) {
        this.bn = bn;
        this.UUIDToNode = UUIDToNode;
        this.predicateToUUDI = predicateToUUDI;
        this.UUDIToPredicate = UUDIToPredicate;
    }

    public BayesianNetwork getBayesianNetwork() {
        return this.bn;
    }
    public Map<String, BayesianNode> getUUIDToNode() {
        return UUIDToNode;
    }

    public Map<Predicate, String> getPredicateToUUDI() {
        return predicateToUUDI;
    }

    public Map<String, Predicate> getUUDIToPredicate() {
        return UUDIToPredicate;
    }
}
