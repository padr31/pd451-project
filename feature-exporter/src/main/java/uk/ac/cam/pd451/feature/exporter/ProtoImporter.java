package uk.ac.cam.pd451.feature.exporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.cli.*;
import uk.ac.cam.acr31.features.javac.proto.GraphProtos;

public class ProtoImporter {

    public static void main(String[] args) throws IOException, ParseException {
        Options option = new Options();
        option.addOption("i", "input-file", true, "Input filename");
        option.addOption("o", "output-file", true, "Output filename");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(option, args);


        try (FileInputStream fis = new FileInputStream(cmd.getOptionValue("input-file"))) {
            GraphProtos.Graph graph = GraphProtos.Graph.parseFrom(fis);
            Neo4jConnector neo4jConnector = new Neo4jConnector();
            neo4jConnector.loadGraph(graph);
            neo4jConnector.closeConnections();

            new ASTWalker().writeToCSV(
                    new File(cmd.getOptionValue("output-file")),
                    graph);
        }
    }
}
