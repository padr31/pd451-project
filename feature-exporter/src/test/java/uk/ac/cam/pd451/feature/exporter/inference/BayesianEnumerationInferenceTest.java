package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BayesianEnumerationInferenceTest {

    private static BayesianEnumerationInference uut;
    private static BayesianNetwork bn;
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
        BayesianNode difficultyNode = new BayesianNode(difficulty);
        difficultyNode.setCPT(d);

        Map<Assignment, Double> imap = Map.of(
                new Assignment(List.of(new Event(intelligence, 0))), 0.7,
                new Assignment(List.of(new Event(intelligence, 1))), 0.3
        );
        Factor i = new Factor(List.of(intelligence), imap);
        BayesianNode intelligenceNode = new BayesianNode(intelligence);
        intelligenceNode.setCPT(i);

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
        BayesianNode gradeNode = new BayesianNode(grade);
        gradeNode.setCPT(g);

        Map<Assignment, Double> smap = Map.of(
                new Assignment(List.of(new Event(sat, 0), new Event(intelligence, 0))), 0.95,
                new Assignment(List.of(new Event(sat, 1), new Event(intelligence, 0))), 0.05,
                new Assignment(List.of(new Event(sat, 0), new Event(intelligence, 1))), 0.2,
                new Assignment(List.of(new Event(sat, 1), new Event(intelligence, 1))), 0.8
        );
        Factor s = new Factor(List.of(sat, intelligence), smap);
        BayesianNode satNode = new BayesianNode(sat);
        satNode.setCPT(s);

        Map<Assignment, Double> lmap = Map.of(
                new Assignment(List.of(new Event(letter, 0), new Event(grade, 1))), 0.1,
                new Assignment(List.of(new Event(letter, 1), new Event(grade, 1))), 0.9,
                new Assignment(List.of(new Event(letter, 0), new Event(grade, 2))), 0.4,
                new Assignment(List.of(new Event(letter, 1), new Event(grade, 2))), 0.6,
                new Assignment(List.of(new Event(letter, 0), new Event(grade, 3))), 0.99,
                new Assignment(List.of(new Event(letter, 1), new Event(grade, 3))), 0.01
        );
        Factor l = new Factor(List.of(letter, grade), lmap);
        BayesianNode letterNode = new BayesianNode(letter);
        letterNode.setCPT(l);

        gradeNode.addParent(difficultyNode);
        gradeNode.addParent(intelligenceNode);
        satNode.addParent(intelligenceNode);
        letterNode.addParent(gradeNode);

        bn = new BayesianNetwork(List.of(difficultyNode, intelligenceNode, gradeNode, satNode, letterNode));
    }

    private static final double DELTA_TOLLERANCE = 0.001;

    @Test
    void testVariableElimination() {
        uut = new BayesianEnumerationInference();
        uut.setModel(bn);

        Assignment question = new Assignment(List.of(new Event(grade, 1)));
        double result = uut.infer(question);
        assertEquals(0.362, result, DELTA_TOLLERANCE);

        Assignment question2 = new Assignment(List.of(new Event(grade, 2)));
        double result2 = uut.infer(question2);
        assertEquals(0.2884, result2, DELTA_TOLLERANCE);

        Assignment question3 = new Assignment(List.of(new Event(sat, 1)));
        double result3 = uut.infer(question3);
        assertEquals(0.275, result3, DELTA_TOLLERANCE);

        Assignment question4 = new Assignment(List.of(new Event(difficulty, 1)));
        double result4 = uut.infer(question4);
        assertEquals(0.4, result4, DELTA_TOLLERANCE);

        Assignment question5 = new Assignment(List.of(new Event(intelligence, 0)));
        double result5 = uut.infer(question5);
        assertEquals(0.7, result5, DELTA_TOLLERANCE);

        Assignment question6 = new Assignment(List.of(new Event(letter, 1)));
        double result6 = uut.infer(question6);
        assertEquals(0.5023, result6, DELTA_TOLLERANCE);
    }

    @Test
    void testVariableEliminationWithEvidence() {
        uut = new BayesianEnumerationInference();
        uut.setModel(bn);

        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0))));
        Assignment question = new Assignment(List.of(new Event(grade, 1)));
        double result = uut.infer(question);
        assertEquals(0.2, result, DELTA_TOLLERANCE);

        Assignment question2 = new Assignment(List.of(new Event(intelligence, 1)));
        double result2 = uut.infer(question2);
        assertEquals(0.0, result2, DELTA_TOLLERANCE);

        Assignment question3 = new Assignment(List.of(new Event(intelligence, 0)));
        double result3 = uut.infer(question3);
        assertEquals(1.0, result3, DELTA_TOLLERANCE);

        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0))));
        Assignment question4 = new Assignment(List.of(new Event(grade, 2)));
        double result4 = uut.infer(question4);
        assertEquals(0.34, result4, DELTA_TOLLERANCE);

        uut.setEvidence(new Assignment(List.of(new Event(intelligence, 0))));
        Assignment question5 = new Assignment(List.of(new Event(letter, 1)));
        double result5 = uut.infer(question5);
        assertEquals(0.3886, result5, DELTA_TOLLERANCE);

        uut.setEvidence(new Assignment(List.of()));
        Assignment question7 = new Assignment(List.of(new Event(letter, 1), new Event(intelligence, 0), new Event(difficulty, 1)));
        double result7 = uut.infer(question7);
        assertEquals(0.05656, result7, DELTA_TOLLERANCE);

        //TODO make sure letter
        uut.setEvidence(new Assignment(List.of(new Event(letter, 1), new Event(intelligence, 0), new Event(difficulty, 1))));
        Assignment question6 = new Assignment(List.of(new Event(grade, 3)));
        double result6 = uut.infer(question6);
        assertEquals(0.0347, result6, DELTA_TOLLERANCE);
    }


}