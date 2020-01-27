package uk.ac.cam.pd451.feature.exporter;

import java.io.IOException;
import org.apache.commons.cli.*;
import uk.ac.cam.pd451.feature.exporter.pipeline.*;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.PipelineDirectoryIO;

public class ExportPipeline {

    public static void main(String[] args) throws IOException, ParseException {
        Options option = new Options();
        option.addOption("i", "input-file", true, "Input filename");
        option.addOption("o", "output-file", true, "Output filename");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(option, args);

        /*
         * Pipeline compilationAndExtractionPipeline = new Pipeline<>(
         *                 new ExtractorStep()
         *         );
         *         compilationAndExtractionPipeline.process(cmd.getOptionValue("input-file"));
         */

        /*Pipeline compilationAndExtractionPipeline = new Pipeline<>(
                          new CompilerStep()
                  ).addStep(new ExtractorStep());
                  compilationAndExtractionPipeline.process(new CompilerStep.CompilerPipeInput(cmd.getOptionValue("input-file"),cmd.getOptionValue("input-file") + "/target"));
        */

        /*Pipeline provenanceImportStep = new Pipeline<>(
                new ProvenanceImportStep()
            ).addStep(new ProvenanceCreationStep());

        System.out.println(provenanceImportStep.process(new EmptyIO()));
*/
        /**
        try (FileInputStream fis = new FileInputStream(cmd.getOptionValue("input-file"))) {
            GraphProtos.Graph graph = GraphProtos.Graph.parseFrom(fis);
            System.out.println("Loading graph into Neo4J");
            Neo4jConnector.getInstance().loadGraph(graph);

            AndersenPointsToAnalysisExtractor extractor = new AndersenPointsToAnalysisExtractor();
            extractor.extractAnalysis();
            extractor.writeToCSV(new File(cmd.getOptionValue("output-file")));

            Neo4jConnector.getInstance().closeConnections();
        }**/
    }
}
