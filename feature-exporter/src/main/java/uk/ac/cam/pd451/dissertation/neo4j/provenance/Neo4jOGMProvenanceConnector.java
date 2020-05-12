package uk.ac.cam.pd451.dissertation.neo4j.provenance;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import uk.ac.cam.pd451.dissertation.datalog.Clause;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.dissertation.utils.Props;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A class for communication that is specialised for sending and querying
 * provenance graphs.
 */
public class Neo4jOGMProvenanceConnector implements Neo4jConnector<List<Clause>> {
    private static Neo4jOGMProvenanceConnector instance;

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
    private Neo4jOGMProvenanceConnector() {
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "uk.ac.cam.pd451.dissertation.neo4j.provenance");
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
        Session session = sessionFactory.openSession();
        session.purgeDatabase();

        Transaction txn = session.beginTransaction();

        Map<Predicate, Long> predToId = new HashMap<>();
        long c = 1;
        for(Clause cl : groundClauses) {
            predToId.put(cl.getHead(), c++);
            for(Predicate p : cl.getBody()) {
                predToId.put(p, c++);
            }
        }

        Map<Long, PredicateNodePOJO> predicateNodePOJOs = predToId
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, e -> new PredicateNodePOJO(e.getValue(), e.getKey())));

        List<PredicateEdgePOJO> predicateEdgePOJOs = new ArrayList<>();
        for(Clause cl : groundClauses) {
            for(Predicate p : cl.getBody()) {
                predicateEdgePOJOs.add(
                    new PredicateEdgePOJO(
                        predicateNodePOJOs.get(predToId.get(p)),
                        predicateNodePOJOs.get(predToId.get(cl.getHead())),
                        cl.getRule()
                    )
                );
            }
        }

        for(PredicateNodePOJO predicateNodePOJO : predicateNodePOJOs.values()) session.save(predicateNodePOJO);
        for(PredicateEdgePOJO predicateEdgePOJO : predicateEdgePOJOs) session.save(predicateEdgePOJO);

        txn.commit();
    }
}
