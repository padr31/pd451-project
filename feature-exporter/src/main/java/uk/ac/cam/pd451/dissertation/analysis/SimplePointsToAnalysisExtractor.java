package uk.ac.cam.pd451.dissertation.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class contains the queries required to extract a very
 * simple points-to analysis --- the transitive closure of allocations
 * points and variable assignments --- that only contains two rules operating
 * on two relations --- alloc and move.
 *
 * The analysis is given below:
 * rule1: points_to(a, b) :- alloc(a, b).
 * rule2: points_to(a, c) :- alloc(b, c), move(a, c).
 */
public class SimplePointsToAnalysisExtractor extends AnalysisExtractor {

    @Override
    public List<Relation> extractAnalysis() {
        List<Relation> extractedRelations = new ArrayList<>();

        extractedRelations.add(((RelationExtractor) () -> {
            Relation move = new Relation("MOVE", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (a) --> (v) -[e1:FEATURE_EDGE{edgeType:\"ASSOCIATED_TOKEN\"}]-> (to)\n" +
                    "match (a) --> (e) -[e2:FEATURE_EDGE{edgeType:\"ASSOCIATED_TOKEN\"}]-> (fro)\n" +
                    "where\n" +
                    "a.contents=\"ASSIGNMENT\" AND\n" +
                    "v.contents=\"VARIABLE\" AND\n" +
                    "e.contents=\"EXPRESSION\"\n" +
                    "return to.contents,fro.contents\n");
            result.forEachRemaining(resultEntry -> {
                move.addEntry(new RelationEntry((String) resultEntry.get("to.contents"), (String) resultEntry.get("fro.contents")));
            });
            return move;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation alloc2 = new Relation("ALLOC2", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (var) --> (eq) --> (new)\n" +
                    "match (type) --> (new_class) --> (new)\n" +
                    "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\"  and new_class.contents=\"NEW_CLASS\" and type.type=\"SYMBOL_MTH\"\n" +
                    "return var.contents, type.contents\n");
            result.forEachRemaining(resultEntry -> {
                alloc2.addEntry(new RelationEntry((String) resultEntry.get("var.contents"), (String) resultEntry.get("type.contents")));
            });
            return alloc2;
        }).extractRelation());

        this.relations = extractedRelations;

        return extractedRelations;
    }
}
