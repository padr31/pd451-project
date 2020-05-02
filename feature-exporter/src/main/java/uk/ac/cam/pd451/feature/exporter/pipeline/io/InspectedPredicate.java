package uk.ac.cam.pd451.feature.exporter.pipeline.io;

import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;

/**
 * Class encalsulating a predicate (tuple) that was observed by the user
 * and associated with a rank, probability, and positivity at the end
 * of Bayesian ranking.
 */
public class InspectedPredicate {
    private Predicate predicate;
    private int rank;
    private double probability;
    private boolean isPositive;

    public InspectedPredicate(Predicate predicate, int rank, double probability, boolean isPositive) {
        this.predicate = predicate;
        this.rank = rank;
        this.probability = probability;
        this.isPositive = isPositive;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public int getRank() {
        return rank;
    }

    public double getProbability() {
        return probability;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public String getCSVString() {
        return this.predicate.getTerms() + ";" + this.rank + ";" + this.probability + ";" + (this.isPositive ? "1" : "0");
    }
}
