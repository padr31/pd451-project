package uk.ac.cam.pd451.feature.exporter.pipeline;

import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.inference.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserInteractionStep implements Step<ProvenanceGraph, RankingStatistics> {
    @Override
    public RankingStatistics process(ProvenanceGraph g) throws PipeException {
        //collect varPointsTo variables

        Map<Predicate, Variable> pointsToSet = g.getPredicateToUUDI()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals("varPointsTo"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> g.getUUIDToNode().get(e.getValue()).getVariable()));


        BayessianGibbsSamplingInference i = new BayessianGibbsSamplingInference();
        i.setModel(g.getBayesianNetwork());
        for(Map.Entry<Predicate, Variable> pointsToVar : pointsToSet.entrySet()) {
            System.out.println(pointsToVar.getKey().getTerms() + " Probability: " + i.infer(new Assignment(List.of(new Event(pointsToVar.getValue(), 1)))));
        }

        return null;
    }
}
