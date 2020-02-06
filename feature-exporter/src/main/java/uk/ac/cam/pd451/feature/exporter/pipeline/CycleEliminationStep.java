package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.Clause;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.neo4j.provenance.Neo4jOGMProvenanceConnector;

import java.util.*;
import java.util.stream.Collectors;

public class CycleEliminationStep implements Step<List<Clause>, List<Clause>> {

    private Map<Predicate, Double> tupleTStamps = new HashMap<>();

    @Override
    public List<Clause> process(List<Clause> input) throws PipeException {
        /*Neo4jOGMProvenanceConnector provenanceConnector = Neo4jOGMProvenanceConnector.getInstance();
        provenanceConnector.clearDatabase();
        provenanceConnector.loadGraph(input);*/
        System.out.println("Initial clause count: " + input.size());

        //initialise timestamps
        for(Clause cl : input) {
            putTimestamp(cl.getHead());
            for(Predicate bodyPredicate : cl.getBody()) {
                putTimestamp(bodyPredicate);
            }
        }
        System.out.println("number of tuples before elimination: " + tupleTStamps.size());

        //propagate timestamp increments
        boolean done = false;
        while(!done) {
            done = true;
            for(Clause cl : input) {
                double maxBodyTimeStamp = getMaxTimestamp(cl.getBody());
                if(getTimestamp(cl.getHead()) > maxBodyTimeStamp + 1) {
                    done = false;
                    putTimestamp(cl.getHead(), maxBodyTimeStamp + 1);
                }
            }
        }

        //filter clauses involved in cycles
        List<Clause> result = input.stream().filter(cl ->
            getTimestamp(cl.getHead()) > getMaxTimestamp(cl.getBody())
        ).collect(Collectors.toList());

        Set<Predicate> outputTuples = new HashSet<>();
        for(Clause cl : result) {
            outputTuples.add(cl.getHead());
            outputTuples.addAll(cl.getBody());
        }
        System.out.println("number of tuples after elimination: " + outputTuples.size());

        return result;
    }

    private Double getMaxTimestamp(List<Predicate> body) {
        return body.stream().mapToDouble(p -> tupleTStamps.get(p)).max().getAsDouble();
    }

    private void putTimestamp(Predicate p) {
        if(p.getName().contains("read_csv")) {
            tupleTStamps.put(p, 0.0);
        } else {
            tupleTStamps.put(p, Double.POSITIVE_INFINITY);
        }
    }

    private void putTimestamp(Predicate p, Double timeStamp) {
        tupleTStamps.put(p, timeStamp);
    }

    private Double getTimestamp(Predicate p) {
        return tupleTStamps.get(p);
    }
}
