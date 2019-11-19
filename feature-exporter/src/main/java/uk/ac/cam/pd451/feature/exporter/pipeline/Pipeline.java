package uk.ac.cam.pd451.feature.exporter.pipeline;

public class Pipeline<I,O> implements Step<I,O> {
    private final Step<I, O> currentStep;

    public Pipeline(Step<I, O> initialStep) {
        this.currentStep = initialStep;
    }

    public <NewO> Pipeline<I, NewO> addStep(Step<O, NewO> nextStep) {
        return new Pipeline<>(input -> nextStep.process(currentStep.process(input)));
    }

    @Override
    public O process(I input) throws PipeException {
        return currentStep.process(input);
    }
}