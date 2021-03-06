package uk.ac.cam.pd451.dissertation;

import java.io.IOException;
import org.apache.commons.cli.*;
import uk.ac.cam.pd451.dissertation.datalog.Clause;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.pipeline.Pipeline;
import uk.ac.cam.pd451.dissertation.pipeline.learning.NetworkLearningStep;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.FullNarrowingStep;
import uk.ac.cam.pd451.dissertation.pipeline.run.*;
import uk.ac.cam.pd451.dissertation.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.CycleEliminationStep;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.ProvenancePruningStep;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.SingularChainCompressionStep;
import uk.ac.cam.pd451.dissertation.utils.Timer;

/**
 * This class provides a convenient way to run different pre-built pipelines.
 * This is intended for development purposes.
 * To productionise any of the provided pipelines, it is recommended to separate
 * them into their own class and customise its I/O accordingly.
 */
public class RunPipeline {

    public static void main(String[] args) throws IOException, ParseException {
        Options option = new Options();
        option.addOption("i", "input-file", true, "Input filename");
        option.addOption("o", "output-file", true, "Output filename");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(option, args);

        String pipe = "import";

        Pipeline pipeline;
        Pipeline optimisationsPipeline = new Pipeline(
                new CycleEliminationStep())
                .addStep(new ProvenancePruningStep())
                .addStep(new SingularChainCompressionStep())
                .addStep(new FullNarrowingStep());

        Timer t = new Timer();
        switch (pipe) {
            case "compile":
                pipeline = new Pipeline<>(
                        new CompilerStep()
                );
                pipeline.process(new CompilerStep.CompilerPipeInput(cmd.getOptionValue("input-file"),cmd.getOptionValue("output-file") + "/target"));
                break;
            case "extract":
                pipeline = new Pipeline<>(
                        new ExtractorStep()
                );
                pipeline.process(cmd.getOptionValue("input-file"));
                break;
            case "datalog":
                pipeline = new Pipeline<>(
                        new DatalogStep()
                );
                pipeline.process(null);
                break;
            case "exalog":
                pipeline = new Pipeline<>(
                        new ExtractorStep())
                        .addStep(new DatalogStep());
                pipeline.process(cmd.getOptionValue("input-file"));
                break;
            case "learn":
                pipeline = new Pipeline<>(
                        new ProvenanceImportStep())
                        .addStep(optimisationsPipeline)
                        .addStep(new NetworkLearningStep());
                pipeline.process(new EmptyIO());
                break;
            case "import":
                pipeline = new Pipeline<>(
                        new ProvenanceImportStep())
                        .addStep(optimisationsPipeline)
                        .addStep(new NetworkCreationStep())
                        .addStep(new UserInteractionStep())
                        .addStep(new RankingProcessorStep());
                        //.addStep(new InferenceEvalStep());
                pipeline.process(new EmptyIO());
                break;
        }
        t.printTimeFromStart();
    }
}
