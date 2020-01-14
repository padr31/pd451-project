package uk.ac.cam.pd451.feature.exporter.neo4j;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", this.id);
        m.put("type", this.type);
        m.put("contents", this.contents);
        m.put("startPosition", this.startPosition);
        m.put("endPosition", this.endPosition);
        m.put("startLineNumber", this.startLineNumber);
        m.put("endLineNumber", this.endLineNumber);

        return m;
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
