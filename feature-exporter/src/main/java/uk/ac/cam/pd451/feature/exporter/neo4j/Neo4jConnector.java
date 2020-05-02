package uk.ac.cam.pd451.feature.exporter.neo4j;

import java.util.Iterator;
import java.util.Map;

/**
 * An interface encapsulating the communication between Java and Neo4j.
 * Different specialised connectors can be created for loading different graphs.
 *
 * Specialisation is required for performance reasons. It is important to decide
 * when to use the OGM connector which is more convenient but slower than the
 * classical Bolt Driver.
 * @param <G>
 */
public interface Neo4jConnector<G> {

    /**
     * Safely terminates all connections to the database.
     */
    void closeConnections();

    /**
     * Clears every node and edge from the database.
     */
    void clearDatabase();

    /**
     * Loads a specific graphical structure into database
     * that is defined by the G type.
     * @param graph
     */
    void loadGraph(G graph);

    /**
     * Performs a Cypher query and returns its results in the form of a key value map.
     * @param query
     * @return Map where the key is the name of the variable returned
     * and the value is its contents which is mostly a JSON, string, or number depending on the query.
     * Cypher queries can return any number of variables that were extracted from a graph.
     */
    Iterator<Map<String, Object>> query(String query);
}
