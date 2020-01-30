package uk.ac.cam.pd451.feature.exporter.analysis;

import uk.ac.cam.pd451.feature.exporter.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.feature.exporter.neo4j.ast.Neo4jOGMConnector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
}
