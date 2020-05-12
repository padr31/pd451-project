package uk.ac.cam.pd451.dissertation.pipeline.io;

import uk.ac.cam.pd451.dissertation.datalog.Predicate;

import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the information collected during
 * ranking a single program.
 */
public class RankingStatistics {

    /**
     * Contains the list of InspectedPredicates which have associated ranks,
     * probabilities (at inspection time), and positivity that was set by the user.
     */
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
