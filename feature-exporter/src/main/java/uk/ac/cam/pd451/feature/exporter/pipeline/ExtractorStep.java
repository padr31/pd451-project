package uk.ac.cam.pd451.feature.exporter.pipeline;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;
import uk.ac.cam.pd451.feature.exporter.analysis.AndersenPointsToAnalysisExtractor;
import uk.ac.cam.pd451.feature.exporter.neo4j.Neo4jConnector;
import uk.ac.cam.pd451.feature.exporter.pipeline.Step;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class takes a directory, walks it to find all the .proto files and generates output relations in .csv files and returns the path to the containing directory.
  */
public class ExtractorStep implements Step<String, String> {

    @Override
    public String process(String input) throws PipeException {
        List<Path> protoFilePaths;
        try (Stream<Path> walk = Files.walk(Paths.get(input))) {
            protoFilePaths = walk.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".proto")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new PipeException(e);
        }
        protoFilePaths.forEach(System.out::println);

        for(Path filePath : protoFilePaths) {
            try (FileInputStream fis = new FileInputStream(filePath.toString())) {
                GraphProtos.Graph graph = GraphProtos.Graph.parseFrom(fis);
                System.out.println("Loading graph into Neo4J");
                Neo4jConnector.getInstance().loadGraph(graph);

                AndersenPointsToAnalysisExtractor extractor = new AndersenPointsToAnalysisExtractor();
                extractor.extractAnalysis();
                extractor.writeToCSV(new File("./out"));

            } catch (IOException e) {
                throw new PipeException(e);
            }
        }
        Neo4jConnector.getInstance().closeConnections();
        return null;
    }
}
