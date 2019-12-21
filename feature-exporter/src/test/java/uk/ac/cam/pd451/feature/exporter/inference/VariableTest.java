package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VariableTest {

    private static Set<Integer> binaryDomain;

    @BeforeAll
    public void setUp() throws Exception {
        binaryDomain = Set.of(0, 1);
    }

    @Test
    public void testCompareTo() {
        Variable x = new Variable("x", binaryDomain);
        Variable y = new Variable("y", binaryDomain);
        assertTrue(x.compareTo(y) < 0);
        assertTrue(y.compareTo(x) > 0);
        assertEquals(0, x.compareTo(x));

    }
}