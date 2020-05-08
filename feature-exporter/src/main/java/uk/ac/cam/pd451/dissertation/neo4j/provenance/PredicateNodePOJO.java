package uk.ac.cam.pd451.dissertation.neo4j.provenance;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;

@NodeEntity
public class PredicateNodePOJO {

    public PredicateNodePOJO() {}

    public PredicateNodePOJO(long id, String name, String terms) {
        this.id = id;
        this.name = name;
        this.terms = terms;
    }

    public PredicateNodePOJO(long id, Predicate predicate) {
        this(
                id,
                predicate.getName(),
                predicate.getTerms()
        );
    }

    @Id
    // need to use Long for generated IDs, long for inherited
    private long id;

    @Property
    private String name;

    @Property
    private String terms;
}
