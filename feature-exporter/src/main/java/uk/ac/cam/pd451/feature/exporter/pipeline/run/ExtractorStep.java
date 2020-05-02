package uk.ac.cam.pd451.feature.exporter.pipeline.run;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.feature.exporter.analysis.AndersenPointsToAnalysisExtractor;
import uk.ac.cam.pd451.feature.exporter.analysis.Relation;
import uk.ac.cam.pd451.feature.exporter.neo4j.ast.Neo4jJavaConnector;
import uk.ac.cam.pd451.feature.exporter.pipeline.Step;
import uk.ac.cam.pd451.feature.exporter.utils.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class takes a directory,
 * walks it to find all the .proto files,
 * extracts analysis feature relations using Neo4j,
 * and returns these relations.
  */
public class ExtractorStep implements Step<String, List<Relation>> {

    @Override
    public List<Relation> process(String input) throws PipeException {
        List<Relation> result = new ArrayList<>();

        Timer t = new Timer();

        List<Path> protoFilePaths;
        try (Stream<Path> walk = Files.walk(Paths.get(input))) {
            protoFilePaths = walk.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".proto")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new PipeException(e);
        }
        protoFilePaths.forEach(System.out::println);
        t.printLastTimeSegment("loading file paths");

        ProgressBar pb = new ProgressBar("Feature Extraction", protoFilePaths.size(), ProgressBarStyle.ASCII); // name, initial max
        pb.start();
        for(Path filePath : protoFilePaths) {
            pb.step();
            try (FileInputStream fis = new FileInputStream(filePath.toString())) {
                GraphProtos.Graph graph = GraphProtos.Graph.parseFrom(fis);
                System.out.println("Loading graph into Neo4J: " + filePath.toString());
                System.out.println("Graph size: (nodes)" + graph.getNodeCount());
                System.out.println("Graph size: (edges)" + graph.getEdgeCount());
                t.printLastTimeSegment("proto parse");

                Neo4jJavaConnector.getInstance().loadGraph(graph);
                t.printLastTimeSegment("neo4j graph load");

                AndersenPointsToAnalysisExtractor extractor = new AndersenPointsToAnalysisExtractor();
                t.printLastTimeSegment("new analysis object creation");

                extractor.extractAnalysis();
                t.printLastTimeSegment("analysis extraction");

                extractor.appendRelations(result);
                extractor.writeToCSV(new File("./out"));
                t.printLastTimeSegment("writing to csv");

            } catch (IOException e) {
                throw new PipeException(e);
            }
        }
        pb.stop();
        Neo4jJavaConnector.getInstance().closeConnections();
        return result;
    }
}
