package uk.ac.cam.pd451.feature.exporter.pipeline;

public class Pipeline<I,O> {
    private final Pipe<I, O> current;

    private Pipeline(Pipe<I, O> current) {
        this.current = current;
    }

    private <NewO> Pipeline<I, NewO> pipe(Pipe<O, NewO> next) {
        return new Pipeline<>(input -> next.process(current.process(input)));
    }

    public O execute(I input) throws Pipe.PipeException {
        return current.process(input);
    }
}