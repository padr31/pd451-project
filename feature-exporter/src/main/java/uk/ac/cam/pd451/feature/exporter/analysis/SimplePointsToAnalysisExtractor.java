package uk.ac.cam.pd451.feature.exporter.analysis;

import org.neo4j.ogm.model.Result;

import java.util.ArrayList;
import java.util.List;

public class SimplePointsToAnalysisExtractor extends AnalysisExtractor {

    @Override
    public List<Relation> extractAnalysis() {
        List<Relation> extractedRelations = new ArrayList<>();

        extractedRelations.add(((RelationExtractor) () -> {
            Relation move = new Relation("MOVE", 2);
            Result result = neo4jConnector.query("match (a) --> (v) -[e1:FEATURE_EDGE{edgeType:\"ASSOCIATED_TOKEN\"}]-> (to)\n" +
                    "match (a) --> (e) -[e2:FEATURE_EDGE{edgeType:\"ASSOCIATED_TOKEN\"}]-> (fro)\n" +
                    "where\n" +
                    "a.contents=\"ASSIGNMENT\" AND\n" +
                    "v.contents=\"VARIABLE\" AND\n" +
                    "e.contents=\"EXPRESSION\"\n" +
                    "return to.contents,fro.contents\n");
            result.queryResults().forEach(resultEntry -> {
                move.addEntry(new RelationEntry((String) resultEntry.get("to.contents"), (String) resultEntry.get("fro.contents")));
            });
            return move;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation alloc2 = new Relation("ALLOC2", 2);
            Result result = neo4jConnector.query("match (var) --> (eq) --> (new)\n" +
                    "match (type) --> (new_class) --> (new)\n" +
                    "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\"  and new_class.contents=\"NEW_CLASS\" and type.type=\"SYMBOL_MTH\"\n" +
                    "return var.contents, type.contents\n");
            result.queryResults().forEach(resultEntry -> {
                alloc2.addEntry(new RelationEntry((String) resultEntry.get("var.contents"), (String) resultEntry.get("type.contents")));
            });
            return alloc2;
        }).extractRelation());

        this.relations = extractedRelations;
        return extractedRelations;
    }
}
