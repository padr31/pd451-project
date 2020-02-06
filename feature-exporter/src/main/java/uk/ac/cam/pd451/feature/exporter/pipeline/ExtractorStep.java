package uk.ac.cam.pd451.feature.exporter.pipeline;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.feature.exporter.analysis.AndersenPointsToAnalysisExtractor;
import uk.ac.cam.pd451.feature.exporter.neo4j.ast.Neo4jJavaConnector;
import uk.ac.cam.pd451.feature.exporter.utils.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class takes a directory, walks it to find all the .proto files and generates output relations in .csv files and returns the path to the containing directory.
  */
public class ExtractorStep implements Step<String, String> {

    @Override
    public String process(String input) throws PipeException {
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

                extractor.writeToCSV(new File("./out"));
                t.printLastTimeSegment("writing to csv");

            } catch (IOException e) {
                throw new PipeException(e);
            }
        }
        pb.stop();
        Neo4jJavaConnector.getInstance().closeConnections();
        return null;
    }
}
