package uk.ac.cam.pd451.feature.exporter;

import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.util.*;

public class ASTHelper {

    private Map<Long, Set<Long>> nodeIDsToEdgeIDs = new HashMap<>();
    private Map<Long, GraphProtos.FeatureNode> nodeIDsToFeatureNodes = new HashMap<>();

    public ASTHelper(GraphProtos.Graph graph) {
        graph.getNodeList().forEach(featureNode -> nodeIDsToFeatureNodes.put(featureNode.getId(), featureNode));

        graph.getNodeList().forEach(featureNode -> nodeIDsToEdgeIDs.put(featureNode.getId(), new HashSet<>()));
        graph.getEdgeList().forEach(featureEdge -> nodeIDsToEdgeIDs.get(featureEdge.getSourceId()).add(featureEdge.getDestinationId()));
    }

    public Set<Long> getEdges(long nodeID) {
        return nodeIDsToEdgeIDs.get(nodeID);
    }

    public GraphProtos.FeatureNode getNode(long nodeID) {
        return this.nodeIDsToFeatureNodes.get(nodeID);
    }

    public Set<Long> getNodeIDs() {
        return nodeIDsToEdgeIDs.keySet();
    }
}
