package uk.ac.cam.pd451.feature.exporter.neo4j.provenance;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.feature.exporter.datalog.Clause;
import uk.ac.cam.pd451.feature.exporter.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.feature.exporter.neo4j.ast.FeatureEdgePOJO;
import uk.ac.cam.pd451.feature.exporter.neo4j.ast.FeatureNodePOJO;
import uk.ac.cam.pd451.feature.exporter.neo4j.ast.Neo4jOGMConnector;
import uk.ac.cam.pd451.feature.exporter.utils.Timer;

import java.util.*;

public class Neo4jOGMProvenanceConnector implements Neo4jConnector<List<Clause>> {
    private static Neo4jOGMProvenanceConnector instance;

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
    private Neo4jOGMProvenanceConnector() {
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "uk.ac.cam.pd451.feature.exporter.neo4j.provenance");
    }

    public static Neo4jOGMProvenanceConnector getInstance() {
        if(instance == null) {
            instance = new Neo4jOGMProvenanceConnector();
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

    public void loadGraph(List<Clause> groundClauses) {
       
    }
}
