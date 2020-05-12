package uk.ac.cam.pd451.dissertation.neo4j.provenance;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type="PREDICATE_EDGE")
public class PredicateEdgePOJO {

    public PredicateEdgePOJO() {}

    public PredicateEdgePOJO(PredicateNodePOJO parent, PredicateNodePOJO child, String rule) {
        this.parent = parent;
        this.child = child;
        this.rule = rule;
    }

    @Id @GeneratedValue
    // need to use Long for generated IDs, long for inherited
    private Long edgeId;

    @StartNode
    private PredicateNodePOJO parent;

    @EndNode
    private PredicateNodePOJO child;

    @Property
    private String rule;

}
