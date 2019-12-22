package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    void get() {
    }

    @Test
    void eliminate() {
        Map<Assignment, Double> probs = new HashMap<>();
        probs.put(xy00, 0.2);
        probs.put(xy01, 0.2);
        probs.put(xy10, 0.2);
        probs.put(xy11, 0.4);

        Factor f = new Factor(List.of(x, y), probs);

        Map<Assignment, Double> probsx = new HashMap<>();
        probsx.put(xy00.remove(y), 0.4);
        probsx.put(xy10.remove(y), 0.6000000000000001);

        Factor fx = f.eliminate(y);

        assertEquals(probsx, fx.function);
        assertEquals(List.of(x), fx.variables);
    }

    @Test
    void product() {
        Map<Assignment, Double> probsXY = new HashMap<>();
        probsXY.put(xy00, 0.2);
        probsXY.put(xy01, 0.2);
        probsXY.put(xy10, 0.2);
        probsXY.put(xy11, 0.4);

        Factor fXY = new Factor(List.of(x, y), probsXY);

        Map<Assignment, Double> probsYZ = new HashMap<>();
        probsYZ.put(yz00, 0.1);
        probsYZ.put(yz01, 0.3);
        probsYZ.put(yz10, 0.4);
        probsYZ.put(yz11, 0.2);

        Factor fYZ = new Factor(List.of(y, z), probsYZ);

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

        Factor fXYZ = fXY.product(fYZ);
        assertEquals(probsXYZ, fXYZ.function);
    }
}