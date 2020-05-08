package uk.ac.cam.pd451.dissertation.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNode;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BayessianGibbsSamplingInferenceTest {

    private static BayessianGibbsSamplingInference uut;

    private static BayesianNetwork bn1;
    private static BayesianNetwork bn2;

    private static Variable grade;
    private static Variable intelligence;
    private static Variable sat;
    private static Variable difficulty;
    private static Variable letter;

    private static Variable b;
    private static Variable f;
    private static Variable a;


    /**
     * bn1 - bayesian network 1
     *
     * DIFFICULTY   INTELLIGENCE
     *      \     /      \
     *       \   /        \
     *       GRADE       SAT
     *         |
     *      LETTER
     *
     * bn2 - bayesian network 2
     *
     *      B   F
     *       \ /
     *        A
     */
    @BeforeAll
    static void setup() {
        Set<Integer> binaryDomain = Set.of(0,1);
        Set<Integer> gradesDomain = Set.of(1,2,3);

        //Bayesian Network 1

        difficulty = new Variable("difficulty", binaryDomain);
        intelligence = new Variable("intelligence", binaryDomain);
        grade = new Variable("grade", gradesDomain);
        sat = new Variable("sat", binaryDomain);
        letter = new Variable("letter", binaryDomain);

        Map<Assignment, Double> dmap = Map.of(
                new Assignment(List.of(new Event(difficulty, 0))), 0.6,
                new Assignment(List.of(new Event(difficulty, 1))), 0.4
        );
        ConditionalProbabilityTable d = new ConditionalProbabilityTable(List.of(difficulty), dmap);
        BayesianNode difficultyNode = new BayesianNode(difficulty);
        difficultyNode.setCPT(d);

        Map<Assignment, Double> imap = Map.of(
                new Assignment(List.of(new Event(intelligence, 0))), 0.7,
                new Assignment(List.of(new Event(intelligence, 1))), 0.3
        );
        ConditionalProbabilityTable i = new ConditionalProbabilityTable(List.of(intelligence), imap);
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
        ConditionalProbabilityTable g = new ConditionalProbabilityTable(List.of(grade, intelligence, difficulty), gmap);
        BayesianNode gradeNode = new BayesianNode(grade);
        gradeNode.setCPT(g);

        Map<Assignment, Double> smap = Map.of(
                new Assignment(List.of(new Event(sat, 0), new Event(intelligence, 0))), 0.95,
                new Assignment(List.of(new Event(sat, 1), new Event(intelligence, 0))), 0.05,
                new Assignment(List.of(new Event(sat, 0), new Event(intelligence, 1))), 0.2,
                new Assignment(List.of(new Event(sat, 1), new Event(intelligence, 1))), 0.8
        );
        ConditionalProbabilityTable s = new ConditionalProbabilityTable(List.of(sat, intelligence), smap);
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
        ConditionalProbabilityTable l = new ConditionalProbabilityTable(List.of(letter, grade), lmap);
        BayesianNode letterNode = new BayesianNode(letter);
        letterNode.setCPT(l);

        gradeNode.addParent(difficultyNode);
        gradeNode.addParent(intelligenceNode);
        satNode.addParent(intelligenceNode);
        letterNode.addParent(gradeNode);

        bn1 = new BayesianNetwork(List.of(difficultyNode, intelligenceNode, gradeNode, satNode, letterNode));

        // Bayesian Network 2

        a = new Variable("a", binaryDomain);
        b = new Variable("b", binaryDomain);
        f = new Variable("f", binaryDomain);

        Map<Assignment, Double> bmap = Map.of(
                new Assignment(List.of(new Event(b, 0))), 0.995,
                new Assignment(List.of(new Event(b, 1))), 0.005
        );
        ConditionalProbabilityTable bcpt = new ConditionalProbabilityTable(List.of(b), bmap);
        BayesianNode bNode = new BayesianNode(b);
        bNode.setCPT(bcpt);

        Map<Assignment, Double> fmap = Map.of(
                new Assignment(List.of(new Event(f, 0))), 0.97,
                new Assignment(List.of(new Event(f, 1))), 0.03
        );
        ConditionalProbabilityTable fcpt = new ConditionalProbabilityTable(List.of(f), fmap);
        BayesianNode fNode = new BayesianNode(f);
        fNode.setCPT(fcpt);

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
        BayesianNode aNode = new BayesianNode(a);
        aNode.setCPT(acpt);

        aNode.addParent(fNode);
        aNode.addParent(bNode);

        bn2 = new BayesianNetwork(List.of(aNode, bNode, fNode));

        uut = new BayessianGibbsSamplingInference();
        uut.setIterations(50000);
    }

    private static final double DELTA_TOLLERANCE = 0.02;

    @Test
    void testBeliefPropagationWithNetwork1() {
        uut.setModel(bn1);

        Assignment question = new Assignment(List.of(new Event(grade, 1)));
        double result = uut.infer(question);
        assertEquals(0.362, result, DELTA_TOLLERANCE);

        Assignment question2 = new Assignment(List.of(new Event(grade, 2)));
        double result2 = uut.infer(question2);
        assertEquals(0.2884, result2, DELTA_TOLLERANCE);

        Assignment question3 = new Assignment(List.of(new Event(sat, 1)));
        double result3 = uut.infer(question3);
        assertEquals(0.275, result3, DELTA_TOLLERANCE);

        Map<Event, Double> res = uut.infer(List.of(new Event(grade, 1), new Event(grade, 2), new Event(sat, 1)));
        assertEquals(0.362, res.get(new Event(grade, 1)), DELTA_TOLLERANCE);
        assertEquals(0.2884, res.get(new Event(grade, 2)), DELTA_TOLLERANCE);
        assertEquals(0.275, res.get(new Event(sat, 1)), DELTA_TOLLERANCE);

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
    void testBeliefPropagationWithNetwork1Evidence() {
        uut = new BayessianGibbsSamplingInference();
        uut.setIterations(50000);
        uut.setModel(bn1);

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

        uut.setEvidence(new Assignment(List.of(new Event(letter, 1), new Event(intelligence, 0), new Event(difficulty, 1))));
        Assignment question6 = new Assignment(List.of(new Event(grade, 3)));
        double result6 = uut.infer(question6);
        assertEquals(0.0347, result6, DELTA_TOLLERANCE);
    }

    @Test
    public void testBeliefPropagationWithNetwork2() {
        uut.setModel(bn2);

        Assignment question8 = new Assignment(List.of(new Event(a, 1)));
        assertEquals(0.013815, uut.infer(question8), DELTA_TOLLERANCE);

        Assignment question9 = new Assignment(List.of(new Event(a, 0)));
        assertEquals(0.986, uut.infer(question9), DELTA_TOLLERANCE);

        uut.setEvidence(new Assignment(List.of(new Event(a, 1))));
        assertEquals(0.0, uut.infer(question9), DELTA_TOLLERANCE);
        assertEquals(1.0, uut.infer(question8), DELTA_TOLLERANCE);

        Assignment question10 = new Assignment(List.of(new Event(b, 1)));
        assertEquals(0.3583, uut.infer(question10), DELTA_TOLLERANCE);

        Assignment question11 = new Assignment(List.of(new Event(b, 0)));
        assertEquals(0.642, uut.infer(question11), DELTA_TOLLERANCE);

        Assignment question12 = new Assignment(List.of(new Event(f, 1)));
        assertEquals(0.442, uut.infer(question12), DELTA_TOLLERANCE);

        Assignment question13 = new Assignment(List.of(new Event(f, 0)));
        assertEquals(0.557, uut.infer(question13), DELTA_TOLLERANCE);

        uut.setEvidence(new Assignment(List.of(new Event(a, 1), new Event(f, 1))));
        assertEquals(0.025, uut.infer(question10), DELTA_TOLLERANCE);
        assertEquals(0.975, uut.infer(question11), DELTA_TOLLERANCE);
    }

}