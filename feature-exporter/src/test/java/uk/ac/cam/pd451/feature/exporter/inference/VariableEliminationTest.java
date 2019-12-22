package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class VariableEliminationTest {

    private static VariableElimination uut;
    private static FactorGraph fg;
    private static Variable grade;
    private static Variable intelligence;
    private static Variable sat;
    private static Variable difficulty;
    private static Variable letter;

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

        Map<Assignment, Double> dmap = Map.of(
            new Assignment(List.of(new Event(difficulty, 0))), 0.6,
            new Assignment(List.of(new Event(difficulty, 1))), 0.4
        );
        Factor d = new Factor(List.of(difficulty), dmap);
        difficulty.setParentalFactor(d);

        Map<Assignment, Double> imap = Map.of(
                new Assignment(List.of(new Event(intelligence, 0))), 0.7,
                new Assignment(List.of(new Event(intelligence, 1))), 0.3
        );
        Factor i = new Factor(List.of(intelligence), imap);
        intelligence.setParentalFactor(i);

        Map<Assignment, Double> gmap1 = Map.of(
                new Assignment(List.of(new Event(grade, 1), new Event(intelligence, 0), new Event(difficulty, 0))), 0.3,
                new Assignment(List.of(new Event(grade, 2), new Event(intelligence, 0), new Event(difficulty, 0))), 0.4,
                new Assignment(List.of(new Event(grade, 3), new Event(intelligence, 0), new Event(difficulty, 0))), 0.3,
                new Assignment(List.of(new Event(grade, 1), new Event(intelligence, 0), new Event(difficulty, 1))), 0.05,
                new Assignment(List.of(new Event(grade, 2), new Event(intelligence, 0), new Event(difficulty, 1))), 0.25,
                new Assignment(List.of(new Event(grade, 3), new Event(intelligence, 0), new Event(difficulty, 1))), 0.7
        );
        Map<Assignment, Double> gmap2 = Map.of(
                new Assignment(List.of(new Event(grade, 1), new Event(intelligence, 1), new Event(difficulty, 0))), 0.9,
                new Assignment(List.of(new Event(grade, 2), new Event(intelligence, 1), new Event(difficulty, 0))), 0.08,
                new Assignment(List.of(new Event(grade, 3), new Event(intelligence, 1), new Event(difficulty, 0))), 0.02,
                new Assignment(List.of(new Event(grade, 1), new Event(intelligence, 1), new Event(difficulty, 1))), 0.5,
                new Assignment(List.of(new Event(grade, 2), new Event(intelligence, 1), new Event(difficulty, 1))), 0.3,
                new Assignment(List.of(new Event(grade, 3), new Event(intelligence, 1), new Event(difficulty, 1))), 0.2
        );
        Map<Assignment, Double> gmap = new HashMap<>(gmap1);
        gmap.putAll(gmap2);
        Factor g = new Factor(List.of(grade, intelligence, difficulty), gmap);
        grade.setParentalFactor(g);

        Map<Assignment, Double> smap = Map.of(
                new Assignment(List.of(new Event(sat, 0), new Event(intelligence, 0))), 0.95,
                new Assignment(List.of(new Event(sat, 1), new Event(intelligence, 0))), 0.05,
                new Assignment(List.of(new Event(sat, 0), new Event(intelligence, 1))), 0.2,
                new Assignment(List.of(new Event(sat, 1), new Event(intelligence, 1))), 0.8
        );
        Factor s = new Factor(List.of(sat, intelligence), smap);
        sat.setParentalFactor(s);

        Map<Assignment, Double> lmap = Map.of(
                new Assignment(List.of(new Event(letter, 0), new Event(grade, 1))), 0.1,
                new Assignment(List.of(new Event(letter, 1), new Event(grade, 1))), 0.9,
                new Assignment(List.of(new Event(letter, 0), new Event(grade, 2))), 0.4,
                new Assignment(List.of(new Event(letter, 1), new Event(grade, 2))), 0.6,
                new Assignment(List.of(new Event(letter, 0), new Event(grade, 3))), 0.99,
                new Assignment(List.of(new Event(letter, 1), new Event(grade, 3))), 0.01
        );
        Factor l = new Factor(List.of(letter, grade), lmap);
        letter.setParentalFactor(l);

        grade.addParent(difficulty);
        grade.addParent(intelligence);
        sat.addParent(intelligence);
        letter.addParent(grade);

        fg = new FactorGraph(List.of(difficulty, intelligence, grade, sat, letter));
    }

    @Test
    void testVariableElimination() {
        uut = new VariableElimination();
        uut.setModel(fg);

        Assignment question = new Assignment(List.of(new Event(grade, 1)));
        double result = uut.infer(question);
        assertEquals(0.362, result);

        Assignment question2 = new Assignment(List.of(new Event(grade, 2)));
        double result2 = uut.infer(question2);
        assertEquals(0.28839999999999993, result2);

        Assignment question3 = new Assignment(List.of(new Event(sat, 1)));
        double result3 = uut.infer(question3);
        assertEquals(0.27499999999999997, result3);

        Assignment question4 = new Assignment(List.of(new Event(difficulty, 1)));
        double result4 = uut.infer(question4);
        assertEquals(0.4, result4);

        Assignment question5 = new Assignment(List.of(new Event(intelligence, 0)));
        double result5 = uut.infer(question5);
        assertEquals(0.7, result5);

        Assignment question6 = new Assignment(List.of(new Event(letter, 1)));
        double result6 = uut.infer(question6);
        assertEquals(0.5023360000000001, result6);
    }

    @Test
    void testVariableEliminationWithEvidence() {
        uut = new VariableElimination();
        uut.setModel(fg);

        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0))));
        Assignment question = new Assignment(List.of(new Event(grade, 1)));
        double result = uut.infer(question);
        assertEquals(0.19999999999999998, result);

        Assignment question2 = new Assignment(List.of(new Event(intelligence, 1)));
        double result2 = uut.infer(question2);
        assertEquals(0.0, result2);

        Assignment question3 = new Assignment(List.of(new Event(intelligence, 0)));
        double result3 = uut.infer(question3);
        assertEquals(1.0, result3);

        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0))));
        Assignment question4 = new Assignment(List.of(new Event(grade, 2)));
        double result4 = uut.infer(question4);
        assertEquals(0.33999999999999997, result4);

        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0))));
        Assignment question5 = new Assignment(List.of(new Event(letter, 1)));
        double result5 = uut.infer(question5);
        assertEquals(0.38860000000000006, result5);

        //TODO make sure letter
        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0), new Event(letter, 1))));
        Assignment question6 = new Assignment(List.of(new Event(grade, 1)));
        double result6 = uut.infer(question6);
        assertEquals(0.33999999999999997, result6);
    }
}