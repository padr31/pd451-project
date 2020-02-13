package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.List;

public interface InferenceAlgorithm<T> {
    void setModel(T model);
    double infer(Assignment events, Assignment evidence);
    void setEvidence(Assignment evidence);
}
