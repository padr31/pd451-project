package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.Clause;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.neo4j.provenance.Neo4jOGMProvenanceConnector;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProvenancePruningStep implements Step<List<Clause>, List<Clause>> {

    @Override
    public List<Clause> process(List<Clause> input) throws PipeException {
        System.out.println("Number of clauses before pruning: " + input.size());

        /*Neo4jOGMProvenanceConnector provenanceConnector = Neo4jOGMProvenanceConnector.getInstance();
        provenanceConnector.clearDatabase();
        provenanceConnector.loadGraph(input);*/

        // initialize set of useful predicates - e.g. nullPointers
        Set<Predicate> essentialPredicates = input.stream().filter(cl -> cl.getHead().getName().equals("nullPointer")).map(Clause::getHead).collect(Collectors.toSet());

        // add predicates needed to derive null pointers until fixpoint
        long previousSize = essentialPredicates.size();
        while(true) {
            for(Clause cl : input) {
                if(essentialPredicates.contains(cl.getHead())) {
                    essentialPredicates.addAll(cl.getBody());
                }
            }
            if(previousSize < essentialPredicates.size()) {
                //fixpoint
                previousSize = essentialPredicates.size();
            } else {
                break;
            }
        }

        //filter gc based on essential predicates
        List<Clause> result = input.stream().filter(cl -> essentialPredicates.contains(cl.getHead())).collect(Collectors.toList());
        System.out.println("Number of clauses after pruning: " + result.size());
        return result;
    }
}
