package uk.ac.cam.pd451.dissertation.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {

    private static Variable x, y;

    @BeforeAll
    static void setup() {
        Set<Integer> binary = new HashSet<Integer>();
        binary.add(0);
        binary.add(1);

        x = new Variable("x", binary);
        y = new Variable("y", binary);
    }

    @Test
    void testAssignmentCanBeUsedAsMapIndex() {
        Event ex0 = new Event(x, 0);
        Event ex1 = new Event(x, 1);
        Event ey0 = new Event(y, 0);
        Event ey1 = new Event(y, 1);

        Assignment a00 = new Assignment(List.of(ex0, ey0));
        Assignment a01 = new Assignment(List.of(ex0, ey1));
        Assignment a10 = new Assignment(List.of(ex1, ey0));
        Assignment a11 = new Assignment(List.of(ex1, ey1));

        Map<Assignment, Double> map = new HashMap<>();
        map.put(a00, 0.0);
        map.put(a01, 0.0);
        assertEquals(2, map.size());

        a10 = a10.remove(y);
        a11 = a11.remove(y);
        map.put(a10, 0.0);
        map.put(a11, 0.0);
        assertEquals(3, map.size());

    }

    @org.junit.jupiter.api.Test
    void testEqual() {
        Event ex0 = new Event(x, 0);
        Event ex1 = new Event(x, 1);
        Event ey0 = new Event(y, 0);
        Event ey1 = new Event(y, 1);

        Assignment a00 = new Assignment(List.of(ex0, ey0));
        Assignment a01 = new Assignment(List.of(ex0, ey1));

        assertNotEquals(a00, a01);

        //order does not matter
        assertEquals(a00, new Assignment(List.of(ey0, ex0)));

        a00 = a00.remove(y);
        a01 = a01.remove(y);
        assertEquals(a00, a01);
    }

    @org.junit.jupiter.api.Test
    void testAllAssignments() {
        List<Variable> vars = List.of(x, y);
        List<Assignment> allAssignments = Assignment.allAssignments(vars);

        //vars are not affected
        assertEquals(vars, List.of(x, y));

        Event ex0 = new Event(x, 0);
        Event ex1 = new Event(x, 1);
        Event ey0 = new Event(y, 0);
        Event ey1 = new Event(y, 1);

        assertEquals(4, allAssignments.size());
        assertTrue(allAssignments.contains(new Assignment(List.of(ex0, ey0))));
        assertTrue(allAssignments.contains(new Assignment(List.of(ex0, ey1))));
        assertTrue(allAssignments.contains(new Assignment(List.of(ex1, ey0))));
        assertTrue(allAssignments.contains(new Assignment(List.of(ex1, ey1))));
    }
}