package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import uk.ac.cam.pd451.feature.exporter.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class VariableTest {

    private static final double DELTA_TOLERANCE = 0.02;
    private static Set<Integer> binaryDomain;

    @BeforeAll
    public void setUp() throws Exception {
        binaryDomain = Set.of(0, 1);
    }

    @Test
    public void testSampleFromDistribution() {
        Variable x = new Variable("x", Set.of(0, 1, 2, 3));
        ConditionalProbabilityTable distribution = new ConditionalProbabilityTable(
            List.of(x),
            Map.of(
                new Assignment(List.of(new Event(x, 0))), 0.3,
                new Assignment(List.of(new Event(x, 1))), 0.1,
                new Assignment(List.of(new Event(x, 2))), 0.4,
                new Assignment(List.of(new Event(x, 3))), 0.2
        ));
        double[] samples = {0.0, 0.0, 0.0, 0.0};
        for(int i = 0; i < 10000; i++) {
            samples[x.sampleFromDistribution(distribution).getValue()]++;
        }
        double sum = Arrays.stream(samples).sum();
        samples = Arrays.stream(samples).map(d -> d/sum).toArray();
        assertEquals(0.3, samples[0], DELTA_TOLERANCE);
        assertEquals(0.1, samples[1], DELTA_TOLERANCE);
        assertEquals(0.4, samples[2], DELTA_TOLERANCE);
        assertEquals(0.2, samples[3], DELTA_TOLERANCE);
    }
}