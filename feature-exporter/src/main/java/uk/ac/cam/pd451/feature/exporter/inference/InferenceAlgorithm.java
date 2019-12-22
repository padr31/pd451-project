package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.Set;

public interface InferenceAlgorithm {
    void setModel(FactorGraph g);
    double infer(Assignment events);
    double infer(Assignment events, Assignment evidence);
    void setEvidence(Assignment evidence);
}
