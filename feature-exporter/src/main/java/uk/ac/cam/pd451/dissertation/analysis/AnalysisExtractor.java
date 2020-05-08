package uk.ac.cam.pd451.dissertation.analysis;

import uk.ac.cam.pd451.dissertation.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.dissertation.neo4j.ast.Neo4jOGMConnector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An AnalysisExtractor connects to Neo4j, takes a source folder
 * and returns a list of relations that are the features extracted
 * by an analysis. Different analyses can extend this class and
 * override the extractAnalysis method.
 */
public abstract class AnalysisExtractor {

    protected List<Relation> relations = new ArrayList<>();
    protected Neo4jConnector neo4JConnector = Neo4jOGMConnector.getInstance();

    abstract List<Relation> extractAnalysis();

    public void writeToCSV(File directory) {
        this.relations.forEach(r -> {
            try {
                r.writeToCSV(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void appendRelations(List<Relation> result) {
        for(Relation r : this.relations) {
            if(result.contains(r)) {
                r.appendTo(result.get(result.indexOf(r)));
            } else {
                result.add(r);
            }
        }
    }
}
