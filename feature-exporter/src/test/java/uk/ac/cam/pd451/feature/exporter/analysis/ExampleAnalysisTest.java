package uk.ac.cam.pd451.feature.exporter.analysis;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.feature.exporter.pipeline.CompilerStep;
import uk.ac.cam.pd451.feature.exporter.pipeline.ExtractorStep;
import uk.ac.cam.pd451.feature.exporter.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleAnalysisTest {
    static List<Relation> extractedRelations = new ArrayList<>();

    @BeforeAll
    static void setup() {
        String inputDirectory = "/Users/padr/repos/pd451-project/feature-exporter/src/main/java/uk/ac/cam/pd451/feature/exporter/example";
        Pipeline pipeline = new Pipeline<>(
                new CompilerStep()
        ).addStep(new ExtractorStep());
        extractedRelations = (List<Relation>) pipeline.process(new CompilerStep.CompilerPipeInput(inputDirectory,inputDirectory + "/target"));
        System.out.println();
    }

    @Test
    void testAnalysisRelations() {
        assertTrue(extractedRelations.get(extractedRelations.indexOf(new Relation("ALLOC", 3))).contains(new RelationEntry("uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.features","14","uk.ac.cam.pd451.feature.exporter.examples.FeatureWorld.FeatureWorld(java.lang.String)")));
    }
}
