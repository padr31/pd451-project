package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.Clause;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.neo4j.provenance.Neo4jOGMProvenanceConnector;

import java.util.*;
import java.util.stream.Collectors;

public class SingularChainCompressionStep implements Step<List<Clause>, List<Clause>> {
    @Override
    public List<Clause> process(List<Clause> input) throws PipeException {

        System.out.println("Clauses before chain compression: " + input.size());

        /*Neo4jOGMProvenanceConnector provenanceConnector = Neo4jOGMProvenanceConnector.getInstance();
        provenanceConnector.clearDatabase();
        provenanceConnector.loadGraph(input);*/

        Set<Clause> gc = new HashSet<>(input);

        Map<Predicate, List<Clause>> sources = new HashMap<>();
        Map<Predicate, List<Clause>> sinks = new HashMap<>();
        for(Clause cl : input) {
            if(!(sources.containsKey(cl.getHead()))) sources.put(cl.getHead(), new ArrayList<>());
            sources.get(cl.getHead()).add(cl);
            for(Predicate bodyPredicate : cl.getBody()) {
                if(!(sinks.containsKey(bodyPredicate))) sinks.put(bodyPredicate, new ArrayList<>());
                sinks.get(bodyPredicate).add(cl);
            }
        }

        Set<Predicate> conclusions = input.stream().map(Clause::getHead).collect(Collectors.toSet());
        Set<Predicate> alarms = input.stream().filter(cl -> cl.getHead().getName().equals("nullPointer")).map(Clause::getHead).collect(Collectors.toSet());

        conclusions.removeAll(alarms);

        // set of predicates that are sorce and sink for only one clause
        List<Predicate> removable = conclusions.stream().filter(p -> sources.get(p).size() == 1 && sinks.get(p).size() == 1).distinct().collect(Collectors.toList());

        while(!removable.isEmpty()) {
            Predicate p = removable.get(0);

            Clause source = sources.get(p).get(0);
            Clause sink = sinks.get(p).get(0);

            if(sink.getBody().size() != 1 && source.getBody().size() != 1) {
                // none of source or sink have only one head variable - dont compress as it would create larger parents
                removable.remove(0);
                continue;
            }

            List<Predicate> connectiveBodies = new ArrayList<>(source.getBody());
            connectiveBodies.addAll(sink.getBody());
            connectiveBodies.remove(p);
            Clause connective = new Clause(sink.getHead(), connectiveBodies);

            gc.add(connective);
            gc.remove(source);
            gc.remove(sink);
            removable.remove(0);

            //recompute sources and sinks
            sink.getBody().forEach(sinkBody -> {
                sinks.get(sinkBody).remove(sink);
                sinks.get(sinkBody).add(connective);
            });

            sources.get(sink.getHead()).remove(sink);
            sources.get(sink.getHead()).add(connective);

            source.getBody().forEach(sourceBody -> {
                sinks.get(sourceBody).remove(source);
                sinks.get(sourceBody).add(connective);
            });

            sinks.remove(p);
            sources.remove(p);
        }

        System.out.println("Clauses after chain compression: " + gc.size());
        return new ArrayList<>(gc);
    }
}
