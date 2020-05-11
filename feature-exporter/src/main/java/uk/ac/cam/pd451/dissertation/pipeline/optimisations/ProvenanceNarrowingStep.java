package uk.ac.cam.pd451.dissertation.pipeline.optimisations;

import uk.ac.cam.pd451.dissertation.datalog.Clause;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.pipeline.Step;

import java.util.*;
import java.util.stream.Collectors;

public class ProvenanceNarrowingStep implements Step<List<Clause>, List<Clause>> {

    //Must be higher than 2 to prevent looping indefinitely as every OR gate has at least 2 parents
    private static final int MAX_PARENTS_THRESHOLD = 4;

    @Override
    public List<Clause> process(List<Clause> groundClauses) throws PipeException {
        System.out.println("Number of clauses before gadgeting: " + groundClauses.size());

        /*Neo4jOGMProvenanceConnector provenanceConnector = Neo4jOGMProvenanceConnector.getInstance();
        provenanceConnector.clearDatabase();
        provenanceConnector.loadGraph(groundClauses);*/

        Set<Clause> result = new HashSet<>(groundClauses);

        while(true) {
            Map<Predicate, List<Clause>> parents = new HashMap<>();

            for(Clause cl : result) {
                if(!parents.containsKey(cl.getHead())) parents.put(cl.getHead(), new ArrayList<>());
                parents.get(cl.getHead()).add(cl);
            }

            parents = parents.entrySet().stream().filter(e -> e.getValue().size() >= MAX_PARENTS_THRESHOLD).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if(parents.size() == 0) break;

            for(Predicate p : parents.keySet()) {
                if(parents.get(p).size() >= MAX_PARENTS_THRESHOLD) {
                    Predicate dummyLeft = new Predicate("dummy", UUID.randomUUID().toString());
                    Predicate dummyRight = new Predicate("dummy", UUID.randomUUID().toString());

                    List<Clause> par = parents.get(p);
                    List<Clause> left = par.subList(0, par.size()/2);
                    List<Clause> right = par.subList(par.size()/2, par.size());
                    left.forEach(cl -> {
                        result.remove(cl);
                        result.add(new Clause(dummyLeft, cl.getBody()));
                    });
                    right.forEach(cl -> {
                        result.remove(cl);
                        result.add(new Clause(dummyRight, cl.getBody()));
                    });
                    result.add(new Clause(dummyLeft, p));
                    result.add(new Clause(dummyRight, p));
                }
            }
        }

        System.out.println("Number of clauses after gadgeting: " + result.size());
        return new ArrayList<>(result);
    }
}
