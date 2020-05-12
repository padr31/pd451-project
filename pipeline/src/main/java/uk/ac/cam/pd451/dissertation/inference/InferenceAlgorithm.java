package uk.ac.cam.pd451.dissertation.inference;

/**
 * Encapsulates an inference algorithm with the three basic operations.
 *
 * @param <T> Type of the probabilistic model over which inference is performed.
 *           Example types are FactorGraph and BayesianNetwork.
 */
public interface InferenceAlgorithm<T> {

    /**
     * Sets the model over which inference is performed.
     * @param model
     */
    void setModel(T model);

    /**
     *
     * @param events The valuation whose joint probability should be inferred.
     * @param evidence The valuation of observed variables.
     * @return The joint probability of the events valuation under the observed evidence.
     */
    double infer(Assignment events, Assignment evidence);

    /**
     * This is a method that resets the observed variables to the evidence argument.
     * @param evidence The valuation of observed variables.
     */
    void setEvidence(Assignment evidence);
}
