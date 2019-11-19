package uk.ac.cam.pd451.feature.exporter.pipeline;

public interface Step<I, O> {

    O process(I input) throws PipeException;

    class PipeException extends RuntimeException {
        public PipeException(Throwable t) {
            super(t);
        }
    }

}
