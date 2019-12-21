package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.Set;

public interface InferenceAlgorithm {
    void setModel(FactorGraph g);
    Factor infer(Assignment events);
    Factor infer(Assignment events, Assignment evidence);
    void setEvidence(Assignment evidence);
}
