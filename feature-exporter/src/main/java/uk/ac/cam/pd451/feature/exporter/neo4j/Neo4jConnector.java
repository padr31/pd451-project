package uk.ac.cam.pd451.feature.exporter.neo4j;

import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.util.Iterator;
import java.util.Map;

public interface Neo4jConnector<G> {

    void closeConnections();
    void clearDatabase();
    void loadGraph(G graph);
    Iterator<Map<String, Object>> query(String query);
}
