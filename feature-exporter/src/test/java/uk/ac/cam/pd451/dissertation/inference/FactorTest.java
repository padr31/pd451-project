package uk.ac.cam.pd451.dissertation.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FactorTest {

    private static Variable x;
    private static Variable y;
    private static Variable z;

    private static Assignment xy00;
    private static Assignment xy01;
    private static Assignment xy10;
    private static Assignment xy11;

    private static Assignment yz00;
    private static Assignment yz01;
    private static Assignment yz10;
    private static Assignment yz11;

    private static Event ex0;
    private static Event ex1;
    private static Event ey0;
    private static Event ey1;
    private static Event ez0;
    private static Event ez1;

    @BeforeAll
    static void setup() {
        Set<Integer> binary = new HashSet<Integer>();
        binary.add(0);
        binary.add(1);

        x = new Variable("x", binary);
        y = new Variable("y", binary);
        z = new Variable("z", binary);

        ex0 = new Event(x, 0);
        ex1 = new Event(x, 1);
        ey0 = new Event(y, 0);
        ey1 = new Event(y, 1);
        ez0 = new Event(z, 0);
        ez1 = new Event(z, 1);

        xy00 = new Assignment(List.of(ex0, ey0));
        xy01 = new Assignment(List.of(ex0, ey1));
        xy10 = new Assignment(List.of(ex1, ey0));
        xy11 = new Assignment(List.of(ex1, ey1));

        yz00 = new Assignment(List.of(ey0, ez0));
        yz01 = new Assignment(List.of(ey0, ez1));
        yz10 = new Assignment(List.of(ey1, ez0));
        yz11 = new Assignment(List.of(ey1, ez1));
    }

    @Test
    void eliminate() {
        Map<Assignment, Double> probs = new HashMap<>();
        probs.put(xy00, 0.2);
        probs.put(xy01, 0.2);
        probs.put(xy10, 0.2);
        probs.put(xy11, 0.4);

        ConditionalProbabilityTable f = new ConditionalProbabilityTable(List.of(x, y), probs);

        Map<Assignment, Double> probsx = new HashMap<>();
        probsx.put(xy00.remove(y), 0.4);
        probsx.put(xy10.remove(y), 0.6000000000000001);

        ConditionalProbabilityTable fx = f.eliminate(y);

        assertEquals(probsx, fx.function);
        assertEquals(List.of(x), fx.variables);
    }

    @Test
    void productTest1() {
        Map<Assignment, Double> probsXY = new HashMap<>();
        probsXY.put(xy00, 0.2);
        probsXY.put(xy01, 0.2);
        probsXY.put(xy10, 0.2);
        probsXY.put(xy11, 0.4);

        ConditionalProbabilityTable fXY = new ConditionalProbabilityTable(List.of(x, y), probsXY);

        Map<Assignment, Double> probsYZ = new HashMap<>();
        probsYZ.put(yz00, 0.1);
        probsYZ.put(yz01, 0.3);
        probsYZ.put(yz10, 0.4);
        probsYZ.put(yz11, 0.2);

        ConditionalProbabilityTable fYZ = new ConditionalProbabilityTable(List.of(y, z), probsYZ);

        Assignment xyz000 = new Assignment(List.of(ex0, ey0, ez0));
        Assignment xyz001 = new Assignment(List.of(ex0, ey0, ez1));
        Assignment xyz010 = new Assignment(List.of(ex0, ey1, ez0));
        Assignment xyz011 = new Assignment(List.of(ex0, ey1, ez1));
        Assignment xyz100 = new Assignment(List.of(ex1, ey0, ez0));
        Assignment xyz101 = new Assignment(List.of(ex1, ey0, ez1));
        Assignment xyz110 = new Assignment(List.of(ex1, ey1, ez0));
        Assignment xyz111 = new Assignment(List.of(ex1, ey1, ez1));
        Map<Assignment, Double> probsXYZ = new HashMap<>();
        probsXYZ.put(xyz000, 0.020000000000000004);
        probsXYZ.put(xyz001, 0.06);
        probsXYZ.put(xyz010, 0.08000000000000002);
        probsXYZ.put(xyz011, 0.04000000000000001);
        probsXYZ.put(xyz100, 0.020000000000000004);
        probsXYZ.put(xyz101, 0.06);
        probsXYZ.put(xyz110, 0.16000000000000003);
        probsXYZ.put(xyz111, 0.08000000000000002);

        ConditionalProbabilityTable fXYZ = fXY.product(fYZ);
        assertEquals(probsXYZ, fXYZ.function);
    }

    @Test
    public void productTest2() {
        Variable a = new Variable("a", Set.of(0, 1));
        Variable b = new Variable("b", Set.of(0, 1));
        Variable f = new Variable("f", Set.of(0, 1));

        Map<Assignment, Double> amap = Map.of(
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 0))), 0.997,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 0))), 0.003,
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 1))), 0.8,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 1))), 0.2,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 0))), 0.01,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 0))), 0.99,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 1))), 0.008,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 1))), 0.992
        );
        ConditionalProbabilityTable acpt = new ConditionalProbabilityTable(List.of(a), amap);
    }

    @Test
    public void equalsTest() {
        Variable a = new Variable("a", Set.of(0, 1));
        Variable b = new Variable("b", Set.of(0, 1));
        Variable f = new Variable("f", Set.of(0, 1));

        Map<Assignment, Double> amap = Map.of(
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 0))), 0.997,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 0))), 0.003,
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 1))), 0.8,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 1))), 0.2,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 0))), 0.01,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 0))), 0.99,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 1))), 0.008,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 1))), 0.992
        );
        ConditionalProbabilityTable acpt = new ConditionalProbabilityTable(List.of(a, b, f), amap);

        Map<Assignment, Double> bmap = Map.of(
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 0))), 0.997,
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 1))), 0.8,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 0))), 0.003,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 1))), 0.2,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 0))), 0.99,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 0))), 0.01,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 1))), 0.008,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 1))), 0.992
        );
        ConditionalProbabilityTable bcpt = new ConditionalProbabilityTable(List.of(b, a, f), amap);
        assertEquals(acpt, bcpt);

        Map<Assignment, Double> cmap = Map.of(
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 0))), 0.97,
                new Assignment(List.of(new Event(a, 0), new Event(b, 0), new Event(f, 1))), 0.8,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 0))), 0.203,
                new Assignment(List.of(new Event(a, 1), new Event(b, 0), new Event(f, 1))), 0.2,
                new Assignment(List.of(new Event(a, 1), new Event(b, 1), new Event(f, 0))), 0.99,
                new Assignment(List.of(new Event(a, 0), new Event(b, 1), new Event(f, 0))), 0.01
        );
        ConditionalProbabilityTable ccpt = new ConditionalProbabilityTable(List.of(b, a, f), cmap);

        //assertNotEquals(acpt, ccpt);
        //TODO fix this test as it is not deterministic
    }
}