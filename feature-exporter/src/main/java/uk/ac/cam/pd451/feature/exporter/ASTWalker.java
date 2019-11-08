package uk.ac.cam.pd451.feature.exporter;

import org.neo4j.ogm.model.Result;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.feature.exporter.relation.Relation;
import uk.ac.cam.pd451.feature.exporter.relation.RelationEntry;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ASTWalker {

    Neo4jConnector neo4jConnector;

    public ASTWalker(Neo4jConnector neo4jConnector) {
        this.neo4jConnector = neo4jConnector;
    }

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
        extractedRelations.add(extractAllocRelation(graph));
        extractedRelations.add(extractMoveRelation(graph));
        extractedRelations.add(extractAlloc2Relation(graph));
        extractedRelations.add(extractHeaptypeRelation(graph));
        extractedRelations.add(extractLoadRelation(graph));
        extractedRelations.add(extractStoreRelation(graph));
        extractedRelations.add(extractFormalArgRelation(graph));
        extractedRelations.add(extractActualArgRelation(graph));
        extractedRelations.add(extractFormalReturnRelation(graph));
        extractedRelations.add(extractActualReturnRelation(graph));
        extractedRelations.add(extractThisVarRelation(graph));
        extractedRelations.add(extractLookupRelation(graph));
        extractedRelations.add(extractVcallRelation(graph));
        return extractedRelations;
    }

    private Relation extractMoveRelation(GraphProtos.Graph graph) {
        Relation move = new Relation("MOVE", 2);
        Result result = neo4jConnector.query("match (a) --> (v) -[e1:FEATURE_EDGE{edgeType:\"ASSOCIATED_TOKEN\"}]-> (to)\n" +
                "match (a) --> (e) -[e2:FEATURE_EDGE{edgeType:\"ASSOCIATED_TOKEN\"}]-> (fro)\n" +
                "where\n" +
                "a.contents=\"ASSIGNMENT\" AND\n" +
                "v.contents=\"VARIABLE\" AND\n" +
                "e.contents=\"EXPRESSION\"\n" +
                "return to.contents,fro.contents\n");
        result.queryResults().forEach(resultEntry -> {
            move.addEntry(new RelationEntry(2, (String) resultEntry.get("to.contents"), (String) resultEntry.get("fro.contents")));
        });
        return move;
    }

    private Relation extractAlloc2Relation(GraphProtos.Graph graph) {
        Relation move = new Relation("ALLOC2", 2);
        Result result = neo4jConnector.query("match (var) --> (eq) --> (new)\n" +
                "match (type) --> (new_class) --> (new)\n" +
                "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\"  and new_class.contents=\"NEW_CLASS\" and type.type=\"SYMBOL_MTH\"\n" +
                "return var.contents, type.contents\n");
        result.queryResults().forEach(resultEntry -> {
            move.addEntry(new RelationEntry(2, (String) resultEntry.get("var.contents"), (String) resultEntry.get("type.contents")));
        });
        return move;
    }

    private Relation extractAllocRelation(GraphProtos.Graph graph) {
        Relation alloc = new Relation("ALLOC", 3);
        Result result = neo4jConnector.query("match (var) --> (eq) --> (new)\n" +
                "match (type) --> (new_class) --> (new)\n" +
                "match shortestPath( (method {contents: \"METHOD\"})-[*]->(new))\n" +
                "match (method_name) --> (method)\n" +
                "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\"  and new_class.contents=\"NEW_CLASS\" and type.type=\"SYMBOL_MTH\" and method_name.type=\"SYMBOL_MTH\"\n" +
                "return var.contents, type.contents, method_name.contents, new.startLineNumber");
        result.queryResults().forEach(resultEntry -> {
            alloc.addEntry(new RelationEntry(3, (String) resultEntry.get("var.contents"), Long.toString((Long) resultEntry.get("new.startLineNumber")), (String) resultEntry.get("method_name.contents")));
        });
        return alloc;
    }

    private Relation extractHeaptypeRelation(GraphProtos.Graph graph) {
        Relation heaptype = new Relation("HEAPTYPE", 2);
        Result result = neo4jConnector.query("match (var) --> (eq) --> (new)\n" +
                "match (type) --> (new_class) --> (new)\n" +
                "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\" and type.type=\"SYMBOL_MTH\"\n" +
                "return new.startLineNumber, type.contents\n");
        result.queryResults().forEach(resultEntry -> {
            heaptype.addEntry(new RelationEntry(2, Long.toString((Long) resultEntry.get("new.startLineNumber")), (String) resultEntry.get("type.contents")));
        });
        return heaptype;
    }

    private Relation extractLoadRelation(GraphProtos.Graph graph) {
        Relation load = new Relation("LOAD", 3);
        Result result = neo4jConnector.query("match (to) --> (eq) --> (base) --> (dot) --> (field)\n" +
                "match (variable) --> (to)\n" +
                "where eq.contents=\"EQ\" AND dot.contents=\"DOT\"  AND to.type=\"IDENTIFIER_TOKEN\" AND base.type=\"IDENTIFIER_TOKEN\" AND field.type=\"IDENTIFIER_TOKEN\" AND variable.contents=\"VARIABLE\"\n" +
                "return to.contents,base.contents,field.contents\n");
        result.queryResults().forEach(resultEntry -> {
            load.addEntry(new RelationEntry(3, (String) resultEntry.get("to.contents"), (String) resultEntry.get("base.contents"), (String) resultEntry.get("field.contents")));
        });
        return load;
    }

    private Relation extractStoreRelation(GraphProtos.Graph graph) {
        Relation store = new Relation("STORE", 3);
        Result result = neo4jConnector.query("match (assignment) --> (variable) --> (member_select) --> (exp1) --> (base)\n" +
                "    match (member_select) --> (field)\n" +
                "    match (assignment) --> (exp2) --> (frm)\n" +
                "    where assignment.contents=\"ASSIGNMENT\" AND variable.contents=\"VARIABLE\"  AND member_select.contents=\"MEMBER_SELECT\" AND exp1.contents=\"EXPRESSION\" AND exp2.contents=\"EXPRESSION\" AND field.type=\"IDENTIFIER_TOKEN\" AND base.type=\"IDENTIFIER_TOKEN\" AND frm.type=\"IDENTIFIER_TOKEN\"\n" +
                "            return base.contents, field.contents, frm.contents");
        result.queryResults().forEach(resultEntry -> {
            store.addEntry(new RelationEntry(3, (String) resultEntry.get("base.contents"), (String) resultEntry.get("field.contents"), (String) resultEntry.get("frm.contents")));
        });
        return store;
    }

    private Relation extractFormalArgRelation(GraphProtos.Graph graph) {
        Relation formalArg = new Relation("FORMALARG", 3);
        Result result = neo4jConnector.query("match (method) --> (method_name)\n" +
                "    match (method) --> (parameters) --> (variable) --> (name)\n" +
                "    where method.contents=\"METHOD\" and method_name.type=\"IDENTIFIER_TOKEN\" and parameters.contents=\"PARAMETERS\" and variable.contents=\"VARIABLE\" and name.type=\"IDENTIFIER_TOKEN\"\n" +
                "            return method_name.contents as method, collect(name.contents) as arguments");
        result.queryResults().forEach(resultEntry -> {
            String method = (String) resultEntry.get("method");
            List<String> args = Arrays.asList((String[]) resultEntry.get("arguments"));
            for (String arg : args) {
                formalArg.addEntry(new RelationEntry(3, method, Integer.toString(args.size()-args.indexOf(arg)), arg));
            }
        });
        return formalArg;
    }

    private Relation extractActualArgRelation(GraphProtos.Graph graph) {
        Relation actualArg = new Relation("ACTUALARG", 3);
        Result result = neo4jConnector.query("match (method_invocation) --> (paren)\n" +
                "match (method_invocation) --> (arguments) --> (arg)\n" +
                "where method_invocation.contents=\"METHOD_INVOCATION\" and paren.contents=\"LPAREN\" and arguments.contents=\"ARGUMENTS\" and arg.type=\"IDENTIFIER_TOKEN\"\n" +
                "return method_invocation.startLineNumber as invocation_site, collect(arg.contents) as arguments");
        result.queryResults().forEach(resultEntry -> {
            List<String> args = Arrays.asList((String[]) resultEntry.get("arguments"));
            for (String arg : args) {
                actualArg.addEntry(new RelationEntry(3, Long.toString((Long)resultEntry.get("invocation_site")), Integer.toString(args.indexOf(arg)+1), arg));
            }
        });
        return actualArg;
    }

    private Relation extractFormalReturnRelation(GraphProtos.Graph graph) {
        Relation formalReturn = new Relation("FORMALRETURN", 2);
        Result result = neo4jConnector.query("match (semi) <-- (ret_block) --> (ret)\n" +
                "match (ret) -[*]-> (n) -[*]-> (semi)\n" +
                "where ret_block.contents=\"RETURN\" and ret.contents=\"RETURN\" and semi.contents=\"SEMI\"\n" +
                "with  ret_block, collect(n.contents) as args\n" +
                "match p=shortestPath((method) -[*]-> (ret_block)) where method.type=\"SYMBOL_MTH\"\n" +
                "return method.contents, args, min(length(p))");
        result.queryResults().forEach(resultEntry -> {
            String args = String.join("", (String[]) resultEntry.get("args"));
            formalReturn.addEntry(new RelationEntry(2, (String) resultEntry.get("method.contents"), args));
        });
        return formalReturn;
    }

    private Relation extractActualReturnRelation(GraphProtos.Graph graph) {
        Relation actualReturn = new Relation("ACTUALRETURN", 2);
        Result result = neo4jConnector.query("match (assign) --> (var) --> (var_name)\n" +
                "match (assign) --> (exp) --> (invoc)\n" +
                "where assign.contents=\"ASSIGNMENT\" and var.contents=\"VARIABLE\" and var_name.type=\"IDENTIFIER_TOKEN\" and exp.contents=\"EXPRESSION\" and invoc.contents=\"METHOD_INVOCATION\"\n" +
                "return assign.startLineNumber, var_name.contents\n");
        result.queryResults().forEach(resultEntry -> {
            actualReturn.addEntry(new RelationEntry(2, Long.toString((Long)resultEntry.get("assign.startLineNumber")), (String) resultEntry.get("var_name.contents")));
        });
        return actualReturn;
    }

    private Relation extractThisVarRelation(GraphProtos.Graph graph) {
        Relation thisVar = new Relation("THISVAR", 2);
        Result result = neo4jConnector.query("match (symbol) --> (method)\n" +
                "where method.contents=\"METHOD\" and symbol.type=\"SYMBOL_MTH\"\n" +
                "return symbol.contents\n");
        result.queryResults().forEach(resultEntry -> {
            thisVar.addEntry(new RelationEntry(2, (String) resultEntry.get("symbol.contents"), ((String) resultEntry.get("symbol.contents")) + "-this"));
        });
        return thisVar;
    }

    private Relation extractLookupRelation(GraphProtos.Graph graph) {
        Relation lookup = new Relation("LOOKUP", 3);
        Result result = neo4jConnector.query("match (class_symbol) --> (class) --> (members) --> (method) \n" +
                "match (method_symbol) --> (method) where\n" +
                "class.contents=\"CLASS\" and members.contents=\"MEMBERS\" and method.contents=\"METHOD\" and class_symbol.type=\"SYMBOL_TYP\" and method_symbol.type=\"SYMBOL_MTH\"\n" +
                "return class_symbol.contents, method_symbol.contents");
        result.queryResults().forEach(resultEntry -> {
            String methodSignature = (String) resultEntry.get("method_symbol.contents");
            lookup.addEntry(new RelationEntry(3, (String) resultEntry.get("class_symbol.contents"), methodSignature, methodSignature.substring(0, methodSignature.indexOf("("))));
        });
        return lookup;
    }

    private Relation extractVcallRelation(GraphProtos.Graph graph) {
        Relation vcall = new Relation("VCALL", 4);
        Result result = neo4jConnector.query("match (meth_sym) --> (meth_invoc) --> (meth_select) --> (mem_select) --> (expression) --> (base) where meth_invoc.contents=\"METHOD_INVOCATION\" and meth_select.contents=\"METHOD_SELECT\" and mem_select.contents=\"MEMBER_SELECT\" and expression.contents=\"EXPRESSION\" and meth_sym.type=\"SYMBOL_MTH\"\n" +
                "with base, meth_sym, meth_invoc\n" +
                "match (in_method) --> (met) --> (body)\n" +
                "match p=shortestPath((body) -[*]-> (base)) where in_method.type=\"SYMBOL_MTH\" and met.contents=\"METHOD\" and body.contents=\"BODY\"\n" +
                "return base.contents, meth_sym.contents, meth_invoc.startLineNumber, collect([in_method.contents, toString(length(p))]) as methods");
        result.queryResults().forEach(resultEntry -> {
            Object[] methods = (Object[]) resultEntry.get("methods");
            int min = Integer.MAX_VALUE;
            String inMethod = "";
            for(Object o : methods) {
                List<String> l = (List<String>) o;
                int len = Integer.parseInt(l.get(1));
                if(len < min) {
                    min = len;
                    inMethod = l.get(0);
                }
            }
            vcall.addEntry(new RelationEntry(4, (String) resultEntry.get("base.contents"), (String) resultEntry.get("meth_sym.contents"), Long.toString((Long)resultEntry.get("meth_invoc.startLineNumber")), inMethod));
        });
        return vcall;
    }
}
