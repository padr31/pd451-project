package uk.ac.cam.pd451.feature.exporter.neo4j;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jConnector {

    private static Neo4jConnector instance;

    /**
     * Session factory for connecting to Neo4j database
     */
    private final SessionFactory sessionFactory;

    //  Configuration info for connecting to the Neo4J database
    private static final String SERVER_URI = "bolt://localhost:7687";
    private static final String SERVER_USERNAME = "neo4j";
    private static final String SERVER_PASSWORD = "password";

    /**
     * Singleton class Neo4jConnector maintains a connection to the Neo4j server instance running locally.
     * Facilitates methods for loading a Graph, clearing, and querying the database.
     */
    private Neo4jConnector() {
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "uk.ac.cam.pd451.feature.exporter.neo4j");
    }

    public static Neo4jConnector getInstance() {
        if(instance == null) {
            instance = new Neo4jConnector();
        }
        return instance;
    }

    public void closeConnections() {
        sessionFactory.close();
    }

    public void clearDatabase() {
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    public Result query(String query) {
        Session session = sessionFactory.openSession();
        Map<String, Object> params = new HashMap<>(1);
        Result result = session.query(query, params);
        return result;
    }

    public void loadGraph(GraphProtos.Graph graph) {
        Session session = sessionFactory.openSession();
        session.purgeDatabase();

        Transaction txn = session.beginTransaction();
        Map<Long, FeatureNodePOJO> featureNodePOJOs = new HashMap<>();
        for(GraphProtos.FeatureNode featureNode : graph.getNodeList()) {
            featureNodePOJOs.put(featureNode.getId(), new FeatureNodePOJO(featureNode));
        }

        List<FeatureEdgePOJO> featureEdgePOJOs = new ArrayList<>();
        for(GraphProtos.FeatureEdge featureEdge : graph.getEdgeList()) {
            featureEdgePOJOs.add(new FeatureEdgePOJO(featureEdge, featureNodePOJOs));
        }
        for(FeatureNodePOJO featureNodePOJO : featureNodePOJOs.values()) session.save(featureNodePOJO);
        for(FeatureEdgePOJO featureEdgePOJO : featureEdgePOJOs) session.save(featureEdgePOJO);

        txn.commit();
    }
}
