package uk.ac.cam.pd451.feature.exporter.analysis;

import uk.ac.cam.pd451.feature.exporter.neo4j.ResultParseUtils;
import uk.ac.cam.pd451.feature.exporter.utils.Timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AndersenPointsToAnalysisExtractor extends AnalysisExtractor {

    @Override
    public List<Relation> extractAnalysis() {
        List<Relation> extractedRelations = new ArrayList<>();
        Timer t = new Timer();

        extractedRelations.add(((RelationExtractor) () -> {
            Relation alloc = new Relation("ALLOC", 3);

            Iterator<Map<String, Object>> result = neo4JConnector.query(
                    "match (var) --> (eq) --> (new)\n" +
                            "match (decl_var) --> (var)\n" +
                            "match (type) <-- (new_class) --> (new)\n" +
                            "match (met) --> (met_name) --> (name) <-- (met_sig)\n" +
                            "match p=shortestPath((met) -[*]-> (var)) where decl_var.type=\"SYMBOL_VAR\" and met.contents=\"METHOD\" and met_name.contents=\"NAME\" and name.type=\"IDENTIFIER_TOKEN\" and new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\"  and new_class.contents=\"NEW_CLASS\" and type.type=\"TYPE\" and met_sig.type=\"SYMBOL_MTH\"\n" +
                            "return decl_var.contents, new.startLineNumber as heap, collect([met_sig.contents, toString(length(p))]) as inMeth, met_sig.type");

            result.forEachRemaining(resultEntry -> {
                String inMeth = ResultParseUtils.getMinDistanceMethodName((List<String>[]) resultEntry.get("inMeth"));
                alloc.addEntry(new RelationEntry(
                        (String) resultEntry.get("decl_var.contents"),
                        Long.toString((Long) resultEntry.get("heap")),
                        inMeth)
                );
            });
            return alloc;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - ALLOC");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation move = new Relation("MOVE", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (a) --> (v) --> (i) --> (n) --> (to) <-- (decl_to)\n" +
                    "match (a) --> (e) --> (i2) --> (n2) -->  (fro) <-- (decl_from)\n" +
                    "where\n" +
                    "a.contents=\"ASSIGNMENT\" AND v.contents=\"VARIABLE\" AND i.contents=\"IDENTIFIER\" AND n.contents=\"NAME\" AND e.contents=\"EXPRESSION\" AND i2.contents=\"IDENTIFIER\" and n2.contents=\"NAME\" AND decl_from.type=\"SYMBOL_VAR\" AND decl_to.type=\"SYMBOL_VAR\"\n" +
                    "return to.contents, decl_to.contents, fro.contents, decl_from.contents");
            result.forEachRemaining(resultEntry -> {
                move.addEntry(new RelationEntry((String) resultEntry.get("decl_to.contents"), (String) resultEntry.get("decl_from.contents")));
            });
            return move;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - MOVE");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation heaptype = new Relation("HEAPTYPE", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (var) --> (eq) --> (new)\n" +
                    "match (type) <-- (new_class) --> (new)\n" +
                    "where new.contents=\"NEW\" and eq.contents=\"EQ\" and var.type=\"IDENTIFIER_TOKEN\" and type.type=\"TYPE\"\n" +
                    "return new.startLineNumber, type.contents");
            result.forEachRemaining(resultEntry -> {
                heaptype.addEntry(new RelationEntry(Long.toString((Long) resultEntry.get("new.startLineNumber")), (String) resultEntry.get("type.contents")));
            });
            return heaptype;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - HEAPTYPE");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation load = new Relation("LOAD", 3);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (to) --> (eq) --> (base) --> (dot) --> (field)\n" +
                    "match (decl_base) --> (base)\n" +
                    "match (variable) --> (identifier) --> (name) --> (to) <-- (decl_to)\n" +
                    "where eq.contents=\"EQ\" AND dot.contents=\"DOT\"  AND to.type=\"IDENTIFIER_TOKEN\" AND base.type=\"IDENTIFIER_TOKEN\" AND field.type=\"IDENTIFIER_TOKEN\" AND variable.contents=\"VARIABLE\" AND identifier.contents=\"IDENTIFIER\" AND name.contents=\"NAME\" AND decl_to.type=\"SYMBOL_VAR\" AND decl_base.type=\"SYMBOL_VAR\"\n" +
                    "return to.contents, decl_to.contents, base.contents, decl_base.contents, field.contents");
            result.forEachRemaining(resultEntry -> {
                load.addEntry(new RelationEntry((String) resultEntry.get("decl_to.contents"), (String) resultEntry.get("decl_base.contents"), (String) resultEntry.get("field.contents")));
            });
            return load;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - LOAD");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation store = new Relation("STORE", 3);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (assignment) --> (variable) --> (member_select) --> (exp1) --> (identifier1) --> (name1) --> (base) <-- (decl_base)\n" +
                    "match (member_select) --> (identifier2) --> (field)\n" +
                    "match (assignment) --> (exp2) --> (identifier3) --> (name2) --> (frm) <-- (decl_from)\n" +
                    "where assignment.contents=\"ASSIGNMENT\" AND variable.contents=\"VARIABLE\"  AND member_select.contents=\"MEMBER_SELECT\" AND exp1.contents=\"EXPRESSION\" AND exp2.contents=\"EXPRESSION\" AND field.type=\"IDENTIFIER_TOKEN\" AND base.type=\"IDENTIFIER_TOKEN\" AND identifier1.contents=\"IDENTIFIER\" and identifier2.contents=\"IDENTIFIER\" and identifier3.contents=\"IDENTIFIER\" and name1.contents=\"NAME\" and name2.contents=\"NAME\" AND decl_base.type=\"SYMBOL_VAR\" AND decl_from.type=\"SYMBOL_VAR\"\n" +
                    "return base.contents, decl_base.contents, field.contents, frm.contents, decl_from.contents");
            result.forEachRemaining(resultEntry -> {
                store.addEntry(new RelationEntry((String) resultEntry.get("decl_base.contents"), (String) resultEntry.get("field.contents"), (String) resultEntry.get("decl_from.contents")));
            });
            return store;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - STORE");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation formalArg = new Relation("FORMALARG", 3);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (method) --> (name1) --> (method_name) <-- (decl_meth)\n" +
                    "match (method) --> (parameters) --> (variable) --> (name2) --> (par_name) <-- (decl_par)\n" +
                    "where method.contents=\"METHOD\" and method_name.type=\"IDENTIFIER_TOKEN\" and parameters.contents=\"PARAMETERS\" and variable.contents=\"VARIABLE\" and par_name.type=\"IDENTIFIER_TOKEN\" and name1.contents=\"NAME\" and name2.contents=\"NAME\" and decl_meth.type=\"SYMBOL_MTH\" and decl_par.type=\"SYMBOL_VAR\"\n" +
                    "return decl_meth.contents as method, collect([decl_par.contents, toString(par_name.startLineNumber), toString(par_name.startPosition)]) as arguments");
            result.forEachRemaining(resultEntry -> {
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
        t.printLastTimeSegment("TIMER 3 - FORMALARG");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation actualArg = new Relation("ACTUALARG", 3);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (method_invocation) --> (paren)\n" +
                    "match (method_invocation) --> (arguments) --> (identifier) --> (name) --> (arg)\n" +
                    "match (arg) <-- (decl_arg)\n" +
                    "where method_invocation.contents=\"METHOD_INVOCATION\" and paren.contents=\"LPAREN\" and arguments.contents=\"ARGUMENTS\" and arg.type=\"IDENTIFIER_TOKEN\" and identifier.contents=\"IDENTIFIER\" and name.contents=\"NAME\" and decl_arg.type=\"SYMBOL_VAR\"\n" +
                    "return method_invocation.startLineNumber as invocation_site, collect([decl_arg.contents, toString(arg.startLineNumber), toString(arg.startPosition)]) as arguments");
            result.forEachRemaining(resultEntry -> {
                String method = (String) resultEntry.get("method");
                List<String> args = ResultParseUtils.getOrderedArguments((List<String>[]) resultEntry.get("arguments"));
                int i = 0;
                for (String arg : args) {
                    i++;
                    actualArg.addEntry(new RelationEntry(Long.toString((Long)resultEntry.get("invocation_site")), Integer.toString(i), arg));
                }
            });
            return actualArg;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - ACTUALARG");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation formalReturn = new Relation("FORMALRETURN", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (ret) --> (expression) --> (identifier) --> (name) --> (ret_name) <-- (decl_ret)\n" +
                    "where ret.contents=\"RETURN\" and expression.contents=\"EXPRESSION\" and identifier.contents=\"IDENTIFIER\" and name.contents=\"NAME\" and decl_ret.type=\"SYMBOL_VAR\"\n" +
                    "with ret, decl_ret\n" +
                    "match (met) --> (body)\n" +
                    "match (met) --> (name2) --> (meth_name) <-- (decl_meth)\n" +
                    "match p=shortestPath((body) -[*]-> (ret)) where met.contents=\"METHOD\" and body.contents=\"BODY\" and name2.contents=\"NAME\" and decl_meth.type=\"SYMBOL_MTH\"\n" +
                    "return decl_meth.contents, decl_ret.contents, min(length(p))");
            result.forEachRemaining(resultEntry -> {
                formalReturn.addEntry(new RelationEntry((String) resultEntry.get("decl_meth.contents"), (String) resultEntry.get("decl_ret.contents")));
            });
            return formalReturn;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - FORMALRETURN");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation actualReturn = new Relation("ACTUALRETURN", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (assign) --> (var) --> (identifier) --> (name1) --> (var_name) <-- (decl_var)\n" +
                    "match (assign) --> (exp) --> (invoc)\n" +
                    "where assign.contents=\"ASSIGNMENT\" and var.contents=\"VARIABLE\" and identifier.contents=\"IDENTIFIER\" and name1.contents=\"NAME\" and decl_var.type=\"SYMBOL_VAR\"  and exp.contents=\"EXPRESSION\" and invoc.contents=\"METHOD_INVOCATION\"\n" +
                    "return assign.startLineNumber, decl_var.contents");
            result.forEachRemaining(resultEntry -> {
                actualReturn.addEntry(new RelationEntry(Long.toString((Long)resultEntry.get("assign.startLineNumber")), (String) resultEntry.get("decl_var.contents")));
            });
            return actualReturn;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - ACTUALRETURN");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation thisVar = new Relation("THISVAR", 2);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (method) --> (name) --> (meth_name) <-- (decl_meth)\n" +
                    "where method.contents=\"METHOD\" and name.contents=\"NAME\" and decl_meth.type=\"SYMBOL_MTH\"\n" +
                    "return decl_meth.contents");
            result.forEachRemaining(resultEntry -> {
                thisVar.addEntry(new RelationEntry((String) resultEntry.get("decl_meth.contents"), ((String) resultEntry.get("decl_meth.contents")) + "-this"));
            });
            return thisVar;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - THISVAR");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation lookup = new Relation("LOOKUP", 3);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (class) --> (members) --> (method) \n" +
                    "match (class) --> (simple_name) --> (class_name) <-- (decl_class)\n" +
                    "match (method) --> (name1) --> (meth_name) <-- (decl_meth) --> (sig_meth) where\n" +
                    "class.contents=\"CLASS\" and members.contents=\"MEMBERS\" and method.contents=\"METHOD\" and simple_name.contents=\"SIMPLE_NAME\" and decl_class.type=\"SYMBOL_TYP\" and name1.contents=\"NAME\" and  decl_meth.type=\"SYMBOL_MTH\" and sig_meth.type=\"METHOD_SIGNATURE\"\n" +
                    "return decl_class.contents, sig_meth.contents, decl_meth.contents");
            result.forEachRemaining(resultEntry -> {
                lookup.addEntry(new RelationEntry((String) resultEntry.get("decl_class.contents"), (String) resultEntry.get("sig_meth.contents"), (String) resultEntry.get("decl_meth.contents")));
            });
            return lookup;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - LOOKUP");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation vcall = new Relation("VCALL", 4);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (meth_invoc) --> (meth_select) --> (mem_select) --> (expression) --> (identifier1) --> (name1) --> (base) <-- (decl_base)\n" +
                    "match (mem_select) --> (identifier2) --> (meth) <-- (decl_meth1) --> (sig_meth1)\n" +
                    "where meth_invoc.contents=\"METHOD_INVOCATION\" and meth_select.contents=\"METHOD_SELECT\" and decl_base.type=\"SYMBOL_VAR\" and mem_select.contents=\"MEMBER_SELECT\" and expression.contents=\"EXPRESSION\" and identifier1.contents=\"IDENTIFIER\" and identifier2.contents=\"IDENTIFIER\" and decl_meth1.type=\"SYMBOL_MTH\" and name1.contents=\"NAME\" and sig_meth1.type=\"METHOD_SIGNATURE\"\n" +
                    "with decl_base, base, sig_meth1, meth_invoc\n" +
                    "match (met) --> (body)\n" +
                    "match (met) --> (name2) --> (meth_name) <-- (decl_meth2)\n" +
                    "match p=shortestPath((body) -[*]-> (meth_invoc)) where met.contents=\"METHOD\" and body.contents=\"BODY\" and name2.contents=\"NAME\" and decl_meth2.type=\"SYMBOL_MTH\"\n" +
                    "return decl_base.contents, sig_meth1.contents, meth_invoc.startLineNumber, collect([decl_meth2.contents, toString(length(p))]) as methods");
            result.forEachRemaining(resultEntry -> {
                String inMethod = ResultParseUtils.getMinDistanceMethodName((List<String>[]) resultEntry.get("methods"));
                vcall.addEntry(
                        new RelationEntry((String) resultEntry.get("decl_base.contents"),
                        (String) resultEntry.get("sig_meth1.contents"),
                        Long.toString((Long)resultEntry.get("meth_invoc.startLineNumber")),
                        inMethod)
                );
            });
            return vcall;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - VCALL");

        extractedRelations.add(((RelationExtractor) () -> {
            Relation reachable = new Relation("REACHABLE", 1);
            Iterator<Map<String, Object>> result = neo4JConnector.query("match (decl_meth) where decl_meth.type=\"SYMBOL_MTH\" and decl_meth.contents CONTAINS \"main\" return decl_meth.contents\n");
            result.forEachRemaining(resultEntry -> {
                reachable.addEntry(
                        new RelationEntry((String) resultEntry.get("decl_meth.contents"))
                );
            });
            return reachable;
        }).extractRelation());
        t.printLastTimeSegment("TIMER 3 - REACHABLE");

        this.relations = extractedRelations;
        return extractedRelations;
    }
}
