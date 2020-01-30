package uk.ac.cam.pd451.feature.exporter.neo4j.provenance;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;

@NodeEntity
public class PredicateNodePOJO {

    public PredicateNodePOJO() {}

    public PredicateNodePOJO(String name, String terms) {
        this.name = name;
        this.terms = terms;
    }

    public PredicateNodePOJO(Predicate predicate) {
        this(
                predicate.getName(),
                predicate.getTerms()
        );
    }

    @Id @GeneratedValue
    // need to use Long for generated IDs, long for inherited
    private Long id;

    @Property
    private String name;

    @Property
    private String terms;
}
