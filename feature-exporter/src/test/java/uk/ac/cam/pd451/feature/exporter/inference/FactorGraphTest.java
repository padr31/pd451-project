package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FactorGraphTest {

    private static Variable difficulty;
    private static Variable intelligence;
    private static Variable grade;
    private static Variable sat;
    private static Variable letter;

    private static FactorGraph uut;

    /**
     * DIFFICULTY   INTELLIGENCE
     *      \     /      \
     *       \   /        \
     *       GRADE       SAT
     *         |
     *      LETTER
     */
    @BeforeAll
    static void setup() {
        Set<Integer> binaryDomain = Set.of(0,1);
        Set<Integer> gradesDomain = Set.of(1,2,3);

        difficulty = new Variable("difficulty", binaryDomain);
        intelligence = new Variable("intelligence", binaryDomain);
        grade = new Variable("grade", gradesDomain);
        sat = new Variable("sat", binaryDomain);
        letter = new Variable("letter", binaryDomain);

        grade.addParent(difficulty);
        grade.addParent(intelligence);
        sat.addParent(intelligence);
        letter.addParent(grade);
        //we don't set up any factors, only connections

        uut = new FactorGraph(List.of(difficulty, intelligence, grade, sat,letter));
    }
    @Test
    void topologicalOrdering() {
        List<Variable> order = uut.topologicalOrdering();

        // there are multiple correct orderings
        // the expected one is due to the fact that we start with the node difficulty
        List<Variable> expectedOrder = List.of(intelligence, sat, difficulty, grade, letter);
        assertEquals(expectedOrder, order);
    }
}