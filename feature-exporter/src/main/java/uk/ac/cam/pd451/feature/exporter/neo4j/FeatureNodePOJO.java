package uk.ac.cam.pd451.feature.exporter.neo4j;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

@NodeEntity
public class FeatureNodePOJO {

    public FeatureNodePOJO() {}

    public FeatureNodePOJO(long id, String type, String contents, int startPosition, int endPosition, int startLineNumber, int endLineNumber) {
        this.id = id;
        this.type = type;
        this.contents = contents;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
    }

    public FeatureNodePOJO(GraphProtos.FeatureNode featureNode) {
        this(
            featureNode.getId(),
            featureNode.getType().toString(),
            featureNode.getContents(),
            featureNode.getStartPosition(),
            featureNode.getEndPosition(),
            featureNode.getStartLineNumber(),
            featureNode.getEndLineNumber()
        );
    }

    @Id
    // need to use Long for generated IDs, long for inherited
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
