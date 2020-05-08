package uk.ac.cam.pd451.dissertation.datalog;

import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNode;

import java.util.Map;

public class ProvenanceGraph {

    private BayesianNetwork bn;

    private Map<Predicate, BayesianNode> predicateToNode;
    private Map<Clause, BayesianNode> clauseToNode;

    public ProvenanceGraph(BayesianNetwork bn, Map<Predicate, BayesianNode> predicateToNode, Map<Clause, BayesianNode> clauseToNode) {
        this.bn = bn;
        this.predicateToNode = predicateToNode;
        this.clauseToNode = clauseToNode;
    }

    public BayesianNetwork getBayesianNetwork() {
        return this.bn;
    }

    public Map<Predicate, BayesianNode> getPredicateToNode() {
        return this.predicateToNode;
    }

    public Map<Clause, BayesianNode> getClauseToNode() {
        return this.clauseToNode;
    }
}
