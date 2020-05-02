package uk.ac.cam.pd451.feature.exporter.pipeline;

/**
 * A Step is the processing unit that is part of a Pipeline consisting of multiple Steps.
 * @param <I> The input type.
 * @param <O> The output type.
 */
public interface Step<I, O> {

    /**
     * The method responsible for processing the input to this Step and returning a specific output type.
     * @param input
     * @return
     * @throws PipeException
     */
    O process(I input) throws PipeException;

    /**
     * An exception that is thrown when there is an error relating to how the pipeline was constructed.
     */
    class PipeException extends RuntimeException {
        public PipeException(Throwable t) {
            super(t);
        }
    }

}
