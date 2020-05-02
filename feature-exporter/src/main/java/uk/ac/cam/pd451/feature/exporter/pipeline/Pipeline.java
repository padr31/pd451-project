package uk.ac.cam.pd451.feature.exporter.pipeline;

/**
 * Encapsulates a pipeline comprising multiple steps.
 * A pipeline itself is a Step and can be therefore appended
 * to another pipeline.
 * @param <I> The input type of the pipeline.
 * @param <O> The output type of the pipeline.
 */
public class Pipeline<I,O> implements Step<I,O> {
    /**
     * The current step that can already be made of distinct steps.
     * This is the step that will run when the pipeline is started.
     */
    private final Step<I, O> currentStep;

    public Pipeline(Step<I, O> initialStep) {
        this.currentStep = initialStep;
    }

    /**
     * Appends a new step to the endo of this pipeline.
     * @param nextStep
     * @param <NewO> The new output type of the pipeline.
     * @return New pipeline keeping the current input type with the argument step appended to the end of the pipeline.
     */
    public <NewO> Pipeline<I, NewO> addStep(Step<O, NewO> nextStep) {
        return new Pipeline<>(input -> nextStep.process(currentStep.process(input)));
    }

    /**
     * Starts the pipeline, executing all the steps sequentially.
     * @param input
     * @return
     * @throws PipeException
     */
    @Override
    public O process(I input) throws PipeException {
        return currentStep.process(input);
    }
}