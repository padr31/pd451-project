package uk.ac.cam.pd451.dissertation.neo4j.ast;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.dissertation.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.dissertation.utils.Props;
import uk.ac.cam.pd451.dissertation.utils.Timer;

import java.util.*;

/**
 * A class for communication between Java and Neo4j using the Object Graph Mapping
 * framework that makes it convenient to map Java objects into the database.
 *
 * This framework is slow in some cases like loading a lot of grpahs, however,
 * it is quick enough for running and retrieving queries on existing objects.
 */
public class Neo4jOGMConnector implements Neo4jConnector<GraphProtos.Graph> {

    private static Neo4jOGMConnector instance;

    /**
     * Session factory for connecting to Neo4j database
     */
    private final SessionFactory sessionFactory;

    //  Configuration info for connecting to the Neo4J database
    private static final String SERVER_URI = Props.get("neo4jServerURI");
    private static final String SERVER_USERNAME = Props.get("neo4jServerUsername");
    private static final String SERVER_PASSWORD = Props.get("neo4jServerPassword");

    /**
     * Singleton class Neo4jConnector maintains a connection to the Neo4j server instance running locally.
     * Facilitates methods for loading a Graph, clearing, and querying the database.
     */
    private Neo4jOGMConnector() {
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "uk.ac.cam.pd451.dissertation.neo4j.ast");
    }

    public static Neo4jOGMConnector getInstance() {
        if(instance == null) {
            instance = new Neo4jOGMConnector();
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

    public Iterator<Map<String, Object>> query(String query) {
        Session session = sessionFactory.openSession();
        Map<String, Object> params = new HashMap<>(1);
        Result result = session.query(query, params);
        return result.iterator();
    }

    public void loadGraph(GraphProtos.Graph graph) {
        //Timer t = new Timer();
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
        //t.printLastTimeSegment("TIMER 2 - opening session and purging database");

        Transaction txn = session.beginTransaction();
        //t.printLastTimeSegment("TIMER 2 - beginning transaction");

        Map<Long, FeatureNodePOJO> featureNodePOJOs = new HashMap<>();
        for(GraphProtos.FeatureNode featureNode : graph.getNodeList()) {
            featureNodePOJOs.put(featureNode.getId(), new FeatureNodePOJO(featureNode));
        }
        //t.printLastTimeSegment("TIMER 2 - creating FeatureNodePOJOs");

        List<FeatureEdgePOJO> featureEdgePOJOs = new ArrayList<>();
        for(GraphProtos.FeatureEdge featureEdge : graph.getEdgeList()) {
            featureEdgePOJOs.add(new FeatureEdgePOJO(featureEdge, featureNodePOJOs));
        }
        //t.printLastTimeSegment("TIMER 2 - creating FeatureEdgePOJOs");

        for(FeatureNodePOJO featureNodePOJO : featureNodePOJOs.values()) session.save(featureNodePOJO);
        //t.printLastTimeSegment("TIMER 2 - saving nodes");
        for(FeatureEdgePOJO featureEdgePOJO : featureEdgePOJOs) session.save(featureEdgePOJO);
        //t.printLastTimeSegment("TIMER 2 - saving edges");

        txn.commit();
        //t.printLastTimeSegment("TIMER 2 - commiting transaction");
    }
}
