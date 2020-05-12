package uk.ac.cam.pd451.dissertation.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.graph.factor.FactorGraph;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.graph.factor.FactorNode;

import java.util.*;
import java.util.stream.Collectors;

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

        //we don't set up any factors, only connections
        FactorNode difficultyNode = new FactorNode(difficulty);
        FactorNode intelligenceNode = new FactorNode(intelligence);
        FactorNode gradeNode = new FactorNode(grade);
        FactorNode satNode = new FactorNode(sat);
        FactorNode letterNode = new FactorNode(letter);

        gradeNode.addParent(difficultyNode);
        gradeNode.addParent(intelligenceNode);
        satNode.addParent(intelligenceNode);
        letterNode.addParent(gradeNode);

        uut = new FactorGraph(List.of(difficultyNode, intelligenceNode, gradeNode, satNode, letterNode));
    }
    @Test
    void topologicalOrdering() {
        List<FactorNode> order = uut.topologicalOrdering();

        // there are multiple correct orderings
        // the expected one is due to the fact that we start with the node difficulty
        List<Variable> expectedOrder = List.of(intelligence, sat, difficulty, grade, letter);
        assertEquals(expectedOrder, order.stream().map(FactorNode::getVariable).collect(Collectors.toList()));
    }
}