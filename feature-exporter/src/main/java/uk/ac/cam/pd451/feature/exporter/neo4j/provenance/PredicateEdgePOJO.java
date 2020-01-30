package uk.ac.cam.pd451.feature.exporter.neo4j.provenance;

import org.neo4j.ogm.annotation.*;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;

@RelationshipEntity(type="PREDICATE_EDGE")
public class PredicateEdgePOJO {

    public PredicateEdgePOJO() {}

    public PredicateEdgePOJO(PredicateNodePOJO parent, PredicateNodePOJO child) {
        this.parent = parent;
        this.child = child;
    }

    public PredicateEdgePOJO(Predicate parent, Predicate child) {
        this(
                new PredicateNodePOJO(parent),
                new PredicateNodePOJO(child)
        );
    }

    @Id @GeneratedValue
    // need to use Long for generated IDs, long for inherited
    private Long edgeId;

    @StartNode
    private PredicateNodePOJO parent;

    @EndNode
    private PredicateNodePOJO child;

}
