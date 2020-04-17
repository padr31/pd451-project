package uk.ac.cam.pd451.feature.exporter.pipeline.io;

import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;

import java.util.List;
import java.util.Map;

public class RankingStatistics {

    private List<InspectedPredicate> inspectedPredicates;

    private List<Map<Predicate, Double>> overallRanks;

    public RankingStatistics(List<InspectedPredicate> inspectedPredicates, List<Map<Predicate, Double>> overallRanks) {
        this.inspectedPredicates = inspectedPredicates;
        this.overallRanks = overallRanks;
    }

    public List<InspectedPredicate> getInspectedPredicates() {
        return inspectedPredicates;
    }

    public List<Map<Predicate, Double>> getOverallRanks() {
        return overallRanks;
    }

}
