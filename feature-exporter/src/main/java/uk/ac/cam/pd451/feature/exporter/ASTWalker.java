package uk.ac.cam.pd451.feature.exporter;

import javafx.util.Pair;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ASTWalker {
    public void writeToCSV(File file, GraphProtos.Graph graph) {
        List<Relation> relations = extractRelations(graph);
        relations.forEach(r -> {
            try {
                r.writeToCSV(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private List<Relation> extractRelations(GraphProtos.Graph graph) {
        List<Relation> extractedRelations = new ArrayList<>();
        extractedRelations.add(extractEdgeRelation(graph));
        extractedRelations.add(extractAllocRelation(graph));
        return extractedRelations;
    }

    private Relation extractEdgeRelation(GraphProtos.Graph graph){
        Relation edge = new Relation("EDGE");
        edge.addAllFromSet(graph.getEdgeList()
                .stream()
                .map(e -> new Pair<>(Long.toString(e.getSourceId()), Long.toString(e.getDestinationId())))
                .collect(Collectors.toSet()));
        return edge;
    }

    private Relation extractAllocRelation(GraphProtos.Graph graph) {
        Relation alloc = new Relation("ALLOC");
        graph.getNodeList().forEach(featureNode -> {
            if(featureNode.getContents().equals("NEW_CLASS"))
                alloc.addPair(new Pair<String, String>(""+featureNode.getStartLineNumber(), ""+featureNode.getEndLineNumber()));
        });
        /*
        ASTHelper ast = new ASTHelper(graph);
        Queue<Long> q = new LinkedList<>();
        Set<Long> seen = new HashSet<>();
        long rootId = graph.getAstRoot().getId();
        q.add(rootId);
        seen.add(rootId);
        while(!q.isEmpty()) {
            long currentNode = q.poll();
            GraphProtos.FeatureNode currentFeatureNode = ast.getNode(currentNode);

            if(currentFeatureNode.getContents().equals("ASSIGNMENT")) {
                alloc.addPair(new Pair<String, String>(
                        Integer.toString(currentFeatureNode.getStartLineNumber()),
                        Integer.toString(currentFeatureNode.getEndLineNumber())));
            }

            ast.getEdges(currentNode).forEach(edgeId -> {
                GraphProtos.FeatureEdge edge = graph.getEdge(edgeId.intValue());
                long toNode = edge.getDestinationId();
                if(!seen.contains(toNode)) {
                    q.add(toNode);
                    seen.add(toNode);
                }
            });
        }
        */
        return alloc;
    }
}
