package uk.ac.cam.pd451.dissertation.neo4j.ast;

import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.v1.*;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.dissertation.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.dissertation.utils.Props;
import uk.ac.cam.pd451.dissertation.utils.Timer;

import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * A class for communication between Java and Neo4j using the standard Bolt driver.
 * This driver is less convenient but much faster than the Java OGM and should be used
 * when performance is needed, for example loading a lot of graph into the database and
 * clearing between loads.
 */
public class Neo4jJavaConnector implements Neo4jConnector<GraphProtos.Graph> {
    private static Neo4jJavaConnector instance;

    private final org.neo4j.driver.v1.Driver driver;

    //  Configuration info for connecting to the Neo4J database
    private static final String SERVER_URI = Props.get("neo4jServerURI");

    /**
     * These should be set for running on localhost.
     * Using a credential retrieval system is recommended
     * when running on non-localhost.
     */
    private static final String SERVER_USERNAME = Props.get("neo4jServerUsername");
    private static final String SERVER_PASSWORD = Props.get("neo4jServerPassword");

    /**
     * Singleton class Neo4jConnector maintains a connection to the Neo4j server instance running locally.
     * Facilitates methods for loading a Graph, clearing, and querying the database.
     */
    private Neo4jJavaConnector() {
        AuthToken token = AuthTokens.basic(SERVER_USERNAME, SERVER_PASSWORD);
        driver = GraphDatabase.driver(SERVER_URI, token);
    }

    public static Neo4jJavaConnector getInstance() {
        if(instance == null) {
            instance = new Neo4jJavaConnector();
        }
        return instance;
    }

    public void closeConnections() {
        driver.close();
    }

    public void clearDatabase() {
        query("MATCH ()-[r]-() DELETE r");
        query("MATCH (n) DELETE n");
    }

    public Iterator<Map<String, Object>> query(String query) {
        Map<String, Object> params = new HashMap<>(1);
        try (Session session = driver.session()) {
            List<Map<String, Object>> list = session.run(query, params).stream().map(r -> r.asMap(Neo4jJavaConnector::convert)).collect(Collectors.toList());
            return list.iterator();
        }
    }

    private static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(Neo4jJavaConnector::convert);
            case "LIST OF ANY?":
                return ((ListValue) value).asList();
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
        }
        return value.asObject();
    }

    /**
     * This method clears the database and loads an AST graph.
     * @param graph
     */
    public void loadGraph(GraphProtos.Graph graph) {
        uk.ac.cam.pd451.dissertation.utils.Timer t = new Timer();
        this.clearDatabase();

        //Create feature nodes
        Map<Long, FeatureNodePOJO> featureNodePOJOs = new HashMap<>();
        for(GraphProtos.FeatureNode featureNode : graph.getNodeList()) {
            featureNodePOJOs.put(featureNode.getId(), new FeatureNodePOJO(featureNode));
        }

        //Create feature edges
        List<FeatureEdgePOJO> featureEdgePOJOs = new ArrayList<>();
        for(GraphProtos.FeatureEdge featureEdge : graph.getEdgeList()) {
            featureEdgePOJOs.add(new FeatureEdgePOJO(featureEdge, featureNodePOJOs));
        }
        t.printLastTimeSegment("TIMER 2 - purging database and creating POJOs");


        try ( Session session = driver.session())
        {
            List<Map<String, Object>> nodeList = featureNodePOJOs.values().stream().map(FeatureNodePOJO::toMap).collect(Collectors.toList());

            session.writeTransaction((TransactionWork<String>) tx -> {
                StatementResult result = tx.run( "UNWIND $nodes as node\n" +
                                "CREATE (n:FeatureNodePOJO) SET n = node return n",
                        parameters( "nodes", nodeList ) );
                return null;
            });
        }
        t.printLastTimeSegment("TIMER 2 - saving nodes");

        try ( Session session = driver.session())
        {
            List<Map<String, Object>> edgeList = featureEdgePOJOs.stream().map(FeatureEdgePOJO::toMap).collect(Collectors.toList());

            session.run("CREATE CONSTRAINT ON (n:FeatureNodePOJO) ASSERT n.id IS UNIQUE;");
            session.writeTransaction((TransactionWork<String>) tx -> {
                StatementResult result = tx.run( "UNWIND $edges as edge\n" +
                                "MATCH (n1:FeatureNodePOJO {id: edge.sourceId})\n" +
                                "MATCH (n2:FeatureNodePOJO {id: edge.destinationId})\n" +
                                "CREATE (n1)-[:FeatureEdgePOJO {edgeType: edge.edgeType}]->(n2)",
                        parameters("edges", edgeList) );
                return null;
            });
        }
        t.printLastTimeSegment("TIMER 2 - saving edges");
    }
}
