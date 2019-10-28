package uk.ac.cam.pd451.feature.exporter;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jConnector {

    /**
     * Session factory for connecting to Neo4j database
     */
    private final SessionFactory sessionFactory;

    //  Configuration info for connecting to the Neo4J database
    static private final String SERVER_URI = "bolt://localhost:7687";
    static private final String SERVER_USERNAME = "neo4j";
    static private final String SERVER_PASSWORD = "password";

    public Neo4jConnector() {
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "uk.ac.cam.pd451.feature.exporter");
    }

    public static void main(String[] args) {
        //Driver driver = GraphDatabase.driver(
        //        "bolt://localhost:7687", AuthTokens.basic("neo4j", "prgr@MMr"));
        //Session session = driver.session();
        //runQuery("CREATE (baeldung:Company {name:\"Baeldung\"}) " +
        //        "-[:owns]-> (tesla:Car {make: 'tesla', model: 'modelX'})" +
        //        "RETURN baeldung, tesla", session);
        //clearDatabase(session);
        //session.close();
        //driver.close();
        //new Neo4jConnector().process();
    }

    public void closeConnections() {
        sessionFactory.close();
    }

    public void loadGraph(GraphProtos.Graph graph) {
        Session session = sessionFactory.openSession();
        session.purgeDatabase();

        Transaction txn = session.beginTransaction();
        Map<Long, FeatureNodePOJO> featureNodePOJOs = new HashMap<>();
        for(GraphProtos.FeatureNode featureNode : graph.getNodeList()) {
            featureNodePOJOs.put(featureNode.getId(), new FeatureNodePOJO(
                    featureNode.getId(),
                    featureNode.getType().toString(),
                    featureNode.getContents(),
                    featureNode.getStartPosition(),
                    featureNode.getEndPosition(),
                    featureNode.getStartLineNumber(),
                    featureNode.getEndLineNumber()
            ));
        }

        List<FeatureEdgePOJO> featureEdgePOJOs = new ArrayList<>();
        for(GraphProtos.FeatureEdge featureEdge : graph.getEdgeList()) {
            featureEdgePOJOs.add(new FeatureEdgePOJO(
                    featureEdge.getSourceId(),
                    featureEdge.getDestinationId(),
                    featureEdge.getType().toString(),
                    featureNodePOJOs.get(featureEdge.getSourceId()),
                    featureNodePOJOs.get(featureEdge.getDestinationId())
            ));
        }
        for(FeatureNodePOJO featureNodePOJO : featureNodePOJOs.values()) session.save(featureNodePOJO);
        for(FeatureEdgePOJO featureEdgePOJO : featureEdgePOJOs) session.save(featureEdgePOJO);

        txn.commit();
    }
}
