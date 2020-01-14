package uk.ac.cam.pd451.feature.exporter.neo4j;

import org.neo4j.ogm.annotation.*;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.util.HashMap;
import java.util.Map;

@RelationshipEntity(type="FEATURE_EDGE")
public class FeatureEdgePOJO {

    public FeatureEdgePOJO() {}

    public FeatureEdgePOJO(long sourceId, long destinationId, String edgeType, FeatureNodePOJO sourceNode, FeatureNodePOJO destinationNode) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.edgeType = edgeType;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
    }

    public FeatureEdgePOJO(GraphProtos.FeatureEdge featureEdge,  Map<Long, FeatureNodePOJO> featureNodePOJOs) {
        this(
            featureEdge.getSourceId(),
            featureEdge.getDestinationId(),
            featureEdge.getType().toString(),
            featureNodePOJOs.get(featureEdge.getSourceId()),
            featureNodePOJOs.get(featureEdge.getDestinationId())
        );
    }

    @Property
    private long sourceId;

    @Property
    private long destinationId;

    @Property
    private String edgeType;

    @Id @GeneratedValue
    // need to use Long for generated IDs, long for inherited
    private Long edgeId;

    @StartNode
    private FeatureNodePOJO sourceNode;

    @EndNode
    private FeatureNodePOJO destinationNode;

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("sourceId", this.sourceId);
        m.put("destinationId", this.destinationId);
        m.put("edgeType", this.edgeType);
        return m;
    }
}
