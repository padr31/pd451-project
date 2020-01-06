package uk.ac.cam.pd451.feature.exporter.inference;

import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;

public class BayessianGibbsSamplingInference implements InferenceAlgorithm<BayesianNetwork> {

    @Override
    public void setModel(BayesianNetwork model) {

    }

    @Override
    public double infer(Assignment events, Assignment evidence) {
        return 0;
    }

    @Override
    public void setEvidence(Assignment evidence) {

    }
}
