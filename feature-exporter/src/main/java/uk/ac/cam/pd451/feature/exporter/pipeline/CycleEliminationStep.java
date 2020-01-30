package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.Clause;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;

import java.util.*;
import java.util.stream.Collectors;

public class CycleEliminationStep implements Step<List<Clause>, List<Clause>> {

    private Map<Predicate, Integer> tupleTStamps = new HashMap<>();

    @Override
    public List<Clause> process(List<Clause> input) throws PipeException {
        System.out.println("Initial clause count: " + input.size());

        //initialise timestamps
        for(Clause cl : input) {
            putTimestamp(cl.getHead());
            for(Predicate bodyPredicate : cl.getBody()) {
                putTimestamp(bodyPredicate);
            }
        }

        //propagate timestamp increments
        boolean done = false;
        while(!done) {
            done = true;
            for(Clause cl : input) {
                int maxBodyTimeStamp = getMaxTimestamp(cl.getBody());
                if(getTimestamp(cl.getHead()) > maxBodyTimeStamp + 1) {
                    done = false;
                    putTimestamp(cl.getHead(), maxBodyTimeStamp + 1);
                }
            }
        }

        //filter clauses involved in cycles
        return input.stream().filter(cl ->
            getTimestamp(cl.getHead()) > getMaxTimestamp(cl.getBody())
        ).collect(Collectors.toList());
    }

    private Integer getMaxTimestamp(List<Predicate> body) {
        return body.stream().map(p -> tupleTStamps.get(p)).max(Comparator.naturalOrder()).get();
    }

    private void putTimestamp(Predicate p) {
        if(p.getName().contains("read_csv")) {
            tupleTStamps.put(p, 0);
        } else {
            tupleTStamps.put(p, Integer.MAX_VALUE);
        }
    }

    private void putTimestamp(Predicate p, Integer timeStamp) {
        tupleTStamps.put(p, timeStamp);
    }

    private Integer getTimestamp(Predicate p) {
        return tupleTStamps.get(p);
    }
}
