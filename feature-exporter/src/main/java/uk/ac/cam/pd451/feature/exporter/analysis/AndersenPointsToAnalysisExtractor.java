package uk.ac.cam.pd451.feature.exporter.analysis;

import org.neo4j.ogm.model.Result;
import uk.ac.cam.pd451.feature.exporter.neo4j.ResultParseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndersenPointsToAnalysisExtractor extends AnalysisExtractor {

    @Override
    public List<Relation> extractAnalysis() {
        List<Relation> extractedRelations = new ArrayList<>();

        extractedRelations.add(((RelationExtractor) () -> {
            Relation alloc = new Relation("ALLOC", 3);

            Result result = neo4jConnector.query(
                    "match (var) --> (eq) --> (new)\n" +
                    "match (type) <-- (new_class) --> (new)\n" +
                    "match (met) --> (met_name) --> (name) <-- (met_sig)\n" +
                    "match p=shortestPath((met) -[*]-> (var)) where met.contents=\"METHOD\" and met_name.contents=\"NAME\" and name.type=\"IDENTIFIER_TOKEN\" and new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\"  and new_class.contents=\"NEW_CLASS\" and type.type=\"TYPE\" and met_sig.type=\"SYMBOL_MTH\"\n" +
                    "return var.contents as var, new.startLineNumber as heap, collect([met_sig.contents, toString(length(p))]) as inMeth, met_sig.type");

            result.queryResults().forEach(resultEntry -> {
                String inMeth = ResultParseUtils.getMinDistanceMethodName((List<String>[]) resultEntry.get("inMeth"));
                alloc.addEntry(new RelationEntry(
                        (String) resultEntry.get("var"),
                        Long.toString((Long) resultEntry.get("heap")),
                        ResultParseUtils.fromSgiToMeth(inMeth))
                );
            });
            return alloc;
        }).extractRelation());

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
            Relation heaptype = new Relation("HEAPTYPE", 2);
            Result result = neo4jConnector.query("match (var) --> (eq) --> (new)\n" +
                    "match (type) --> (new_class) --> (new)\n" +
                    "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\" and type.type=\"SYMBOL_MTH\"\n" +
                    "return new.startLineNumber, type.contents\n");
            result.queryResults().forEach(resultEntry -> {
                heaptype.addEntry(new RelationEntry(Long.toString((Long) resultEntry.get("new.startLineNumber")), (String) resultEntry.get("type.contents")));
            });
            return heaptype;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation load = new Relation("LOAD", 3);
            Result result = neo4jConnector.query("match (to) --> (eq) --> (base) --> (dot) --> (field)\n" +
                    "match (variable) --> (to)\n" +
                    "where eq.contents=\"EQ\" AND dot.contents=\"DOT\"  AND to.type=\"IDENTIFIER_TOKEN\" AND base.type=\"IDENTIFIER_TOKEN\" AND field.type=\"IDENTIFIER_TOKEN\" AND variable.contents=\"VARIABLE\"\n" +
                    "return to.contents,base.contents,field.contents\n");
            result.queryResults().forEach(resultEntry -> {
                load.addEntry(new RelationEntry((String) resultEntry.get("to.contents"), (String) resultEntry.get("base.contents"), (String) resultEntry.get("field.contents")));
            });
            return load;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation store = new Relation("STORE", 3);
            Result result = neo4jConnector.query("match (assignment) --> (variable) --> (member_select) --> (exp1) --> (base)\n" +
                    "    match (member_select) --> (field)\n" +
                    "    match (assignment) --> (exp2) --> (frm)\n" +
                    "    where assignment.contents=\"ASSIGNMENT\" AND variable.contents=\"VARIABLE\"  AND member_select.contents=\"MEMBER_SELECT\" AND exp1.contents=\"EXPRESSION\" AND exp2.contents=\"EXPRESSION\" AND field.type=\"IDENTIFIER_TOKEN\" AND base.type=\"IDENTIFIER_TOKEN\" AND frm.type=\"IDENTIFIER_TOKEN\"\n" +
                    "            return base.contents, field.contents, frm.contents");
            result.queryResults().forEach(resultEntry -> {
                store.addEntry(new RelationEntry((String) resultEntry.get("base.contents"), (String) resultEntry.get("field.contents"), (String) resultEntry.get("frm.contents")));
            });
            return store;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation formalArg = new Relation("FORMALARG", 3);
            Result result = neo4jConnector.query("match (method) --> (method_name)\n" +
                    "match (method) --> (parameters) --> (variable) --> (name)\n" +
                    "where method.contents=\"METHOD\" and method_name.type=\"IDENTIFIER_TOKEN\" and parameters.contents=\"PARAMETERS\" and variable.contents=\"VARIABLE\" and name.type=\"IDENTIFIER_TOKEN\"\n" +
                    "return method_name.contents as method, collect([name.contents, toString(name.startLineNumber), toString(name.startPosition)]) as arguments");
            result.queryResults().forEach(resultEntry -> {

                String method = (String) resultEntry.get("method");
                List<String> args = ResultParseUtils.getOrderedArguments((List<String>[]) resultEntry.get("arguments"));
                int i = 0;
                for (String arg : args) {
                    i++;
                    formalArg.addEntry(new RelationEntry(method, Integer.toString(i), arg));
                }
            });
            return formalArg;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation actualArg = new Relation("ACTUALARG", 3);
            Result result = neo4jConnector.query("match (method_invocation) --> (paren)\n" +
                    "match (method_invocation) --> (arguments) --> (arg)\n" +
                    "where method_invocation.contents=\"METHOD_INVOCATION\" and paren.contents=\"LPAREN\" and arguments.contents=\"ARGUMENTS\" and arg.type=\"IDENTIFIER_TOKEN\"\n" +
                    "return method_invocation.startLineNumber as invocation_site, collect(arg.contents) as arguments");
            result.queryResults().forEach(resultEntry -> {
                List<String> args = Arrays.asList((String[]) resultEntry.get("arguments"));
                for (String arg : args) {
                    actualArg.addEntry(new RelationEntry(Long.toString((Long)resultEntry.get("invocation_site")), Integer.toString(args.indexOf(arg)+1), arg));
                }
            });
            return actualArg;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation formalReturn = new Relation("FORMALRETURN", 2);
            Result result = neo4jConnector.query("match (semi) <-- (ret_block) --> (ret)\n" +
                    "match (ret) -[*]-> (n) -[*]-> (semi)\n" +
                    "where ret_block.contents=\"RETURN\" and ret.contents=\"RETURN\" and semi.contents=\"SEMI\"\n" +
                    "with  ret_block, collect(n.contents) as args\n" +
                    "match p=shortestPath((method) -[*]-> (ret_block)) where method.type=\"SYMBOL_MTH\"\n" +
                    "return method.contents, args, min(length(p))");
            result.queryResults().forEach(resultEntry -> {
                String args = String.join("", (String[]) resultEntry.get("args"));
                formalReturn.addEntry(new RelationEntry((String) resultEntry.get("method.contents"), args));
            });
            return formalReturn;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation actualReturn = new Relation("ACTUALRETURN", 2);
            Result result = neo4jConnector.query("match (assign) --> (var) --> (var_name)\n" +
                    "match (assign) --> (exp) --> (invoc)\n" +
                    "where assign.contents=\"ASSIGNMENT\" and var.contents=\"VARIABLE\" and var_name.type=\"IDENTIFIER_TOKEN\" and exp.contents=\"EXPRESSION\" and invoc.contents=\"METHOD_INVOCATION\"\n" +
                    "return assign.startLineNumber, var_name.contents\n");
            result.queryResults().forEach(resultEntry -> {
                actualReturn.addEntry(new RelationEntry(Long.toString((Long)resultEntry.get("assign.startLineNumber")), (String) resultEntry.get("var_name.contents")));
            });
            return actualReturn;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation thisVar = new Relation("THISVAR", 2);
            Result result = neo4jConnector.query("match (symbol) --> (method)\n" +
                    "where method.contents=\"METHOD\" and symbol.type=\"SYMBOL_MTH\"\n" +
                    "return symbol.contents\n");
            result.queryResults().forEach(resultEntry -> {
                thisVar.addEntry(new RelationEntry((String) resultEntry.get("symbol.contents"), ((String) resultEntry.get("symbol.contents")) + "-this"));
            });
            return thisVar;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation lookup = new Relation("LOOKUP", 3);
            Result result = neo4jConnector.query("match (class_symbol) --> (class) --> (members) --> (method) \n" +
                    "match (method_symbol) --> (method) where\n" +
                    "class.contents=\"CLASS\" and members.contents=\"MEMBERS\" and method.contents=\"METHOD\" and class_symbol.type=\"SYMBOL_TYP\" and method_symbol.type=\"SYMBOL_MTH\"\n" +
                    "return class_symbol.contents, method_symbol.contents");
            result.queryResults().forEach(resultEntry -> {
                String methodSignature = (String) resultEntry.get("method_symbol.contents");
                lookup.addEntry(new RelationEntry((String) resultEntry.get("class_symbol.contents"), methodSignature, methodSignature.substring(0, methodSignature.indexOf("("))));
            });
            return lookup;
        }).extractRelation());

        extractedRelations.add(((RelationExtractor) () -> {
            Relation vcall = new Relation("VCALL", 4);
            Result result = neo4jConnector.query("match (meth_sym) --> (meth_invoc) --> (meth_select) --> (mem_select) --> (expression) --> (base) where meth_invoc.contents=\"METHOD_INVOCATION\" and meth_select.contents=\"METHOD_SELECT\" and mem_select.contents=\"MEMBER_SELECT\" and expression.contents=\"EXPRESSION\" and meth_sym.type=\"SYMBOL_MTH\"\n" +
                    "with base, meth_sym, meth_invoc\n" +
                    "match (in_method) --> (met) --> (body)\n" +
                    "match p=shortestPath((body) -[*]-> (base)) where in_method.type=\"SYMBOL_MTH\" and met.contents=\"METHOD\" and body.contents=\"BODY\"\n" +
                    "return base.contents, meth_sym.contents, meth_invoc.startLineNumber, collect([in_method.contents, toString(length(p))]) as methods");
            result.queryResults().forEach(resultEntry -> {
                String inMethod = ResultParseUtils.getMinDistanceMethodName((List<String>[]) resultEntry.get("methods"));
                vcall.addEntry(
                        new RelationEntry((String) resultEntry.get("base.contents"),
                        (String) resultEntry.get("meth_sym.contents"),
                        Long.toString((Long)resultEntry.get("meth_invoc.startLineNumber")),
                        ResultParseUtils.fromSgiToMeth(inMethod))
                );
            });
            return vcall;
        }).extractRelation());

        this.relations = extractedRelations;
        return extractedRelations;
    }
}
