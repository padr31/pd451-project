package uk.ac.cam.pd451.feature.exporter;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class FeatureNodePOJO {

    public FeatureNodePOJO() {

    }

    public FeatureNodePOJO(long id, String type, String contents, int startPosition, int endPosition, int startLineNumber, int endLineNumber) {
        this.id = id;
        this.type = type;
        this.contents = contents;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
    }

    @Id
    private long id;

    @Property
    private String type;

    @Property
    private String contents;

    @Property
    private int startPosition;

    @Property
    private int endPosition;

    @Property
    private int startLineNumber;

    @Property
    private int endLineNumber;
}
