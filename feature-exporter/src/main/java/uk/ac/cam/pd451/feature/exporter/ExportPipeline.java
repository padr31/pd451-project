package uk.ac.cam.pd451.feature.exporter;

import java.io.IOException;
import org.apache.commons.cli.*;
import uk.ac.cam.pd451.feature.exporter.pipeline.*;
import uk.ac.cam.pd451.feature.exporter.pipeline.eval.InferenceEvalStep;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.feature.exporter.pipeline.run.*;
import uk.ac.cam.pd451.feature.exporter.utils.Timer;

public class ExportPipeline {

    public static void main(String[] args) throws IOException, ParseException {
        Options option = new Options();
        option.addOption("i", "input-file", true, "Input filename");
        option.addOption("o", "output-file", true, "Output filename");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(option, args);

        String pipe = "extract";

        Pipeline pipeline;
        Pipeline optimisationsPipeline = new Pipeline(
                new CycleEliminationStep())
                .addStep(new ProvenancePruningStep())
                .addStep(new SingularChainCompressionStep())
                .addStep(new FullNarrowingStep());

        Timer t = new Timer();
        switch (pipe) {
            case "extract":
                pipeline = new Pipeline<>(
                        new ExtractorStep()
                );
                pipeline.process(cmd.getOptionValue("input-file"));
                break;
            case "compile":
                pipeline = new Pipeline<>(
                    new CompilerStep()
            );
                pipeline.process(new CompilerStep.CompilerPipeInput(cmd.getOptionValue("input-file"),cmd.getOptionValue("input-file") + "/target"));
                break;
            case "datalog":
                pipeline = new Pipeline<>(
                        new DatalogStep()
                );
                pipeline.process(null);
                break;
            case "extract/evaluate":
                pipeline = new Pipeline<>(
                        new ExtractorStep())
                        .addStep(new DatalogStep());
                pipeline.process(cmd.getOptionValue("input-file"));
                break;
            case "import":
                pipeline = new Pipeline<>(
                        new ProvenanceImportStep())
                        .addStep(optimisationsPipeline)
                        .addStep(new ProvenanceCreationStep())
                        .addStep(new AssistedRankingStep())
                        .addStep(new RankingProcessorStep());
                        //.addStep(new InferenceEvalStep());
                pipeline.process(new EmptyIO());
                break;
        }
        t.printTimeFromStart();
    }
}
