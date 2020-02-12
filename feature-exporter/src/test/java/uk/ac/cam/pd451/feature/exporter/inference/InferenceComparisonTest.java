package uk.ac.cam.pd451.feature.exporter.inference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.feature.exporter.graph.bn.BayesianNode;
import uk.ac.cam.pd451.feature.exporter.graph.factor.FactorGraph;
import uk.ac.cam.pd451.feature.exporter.graph.factor.FactorNode;
import uk.ac.cam.pd451.feature.exporter.inference.factor.AssignmentTableFactor;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InferenceComparisonTest {

    private static BayesianEnumerationInference bayesianEnumerationInference;
    private static FactorEliminationInference eliminationInference;
    private static BayesianPropagationInference bayesianPropagationInference;

    private static BayesianNetwork bn;
    private static FactorGraph fg;
    private static Variable burglary;
    private static Variable earthquake;
    private static Variable alarm;
    private static Variable john;
    private static Variable mary;


    /**
     * BURGLARY   EARTHQUAKE
     *      \     /
     *       \   /
     *       ALARM
     *       /   \
     *  JOHNCALL  MARYCALL
     */
    @BeforeAll
    static void setup() {
        Set<Integer> binaryDomain = Set.of(0,1);

        burglary = new Variable("burglary", binaryDomain);
        earthquake = new Variable("earthquake", binaryDomain);
        alarm = new Variable("alarm", binaryDomain);
        john = new Variable("john", binaryDomain);
        mary = new Variable("mary", binaryDomain);

        Map<Assignment, Double> dmap = Map.of(
                new Assignment(List.of(new Event(burglary, 0))), 0.999,
                new Assignment(List.of(new Event(burglary, 1))), 0.001
        );
        AssignmentTableFactor b = new AssignmentTableFactor(List.of(burglary), dmap);
        BayesianNode bnBurglaryNode = new BayesianNode(burglary);
        bnBurglaryNode.setCPT(b);
        FactorNode fgBurglaryNode = new FactorNode(burglary);
        fgBurglaryNode.setParentalFactor(b);

        Map<Assignment, Double> emap = Map.of(
                new Assignment(List.of(new Event(earthquake, 0))), 0.998,
                new Assignment(List.of(new Event(earthquake, 1))), 0.002
        );
        AssignmentTableFactor e = new AssignmentTableFactor(List.of(earthquake), emap);
        BayesianNode bnEarthquakeNode = new BayesianNode(earthquake);
        bnEarthquakeNode.setCPT(e);
        FactorNode fgEarthquakeNode = new FactorNode(earthquake);
        fgEarthquakeNode.setParentalFactor(e);

        Map<Assignment, Double> amap1 = Map.of(
                new Assignment(List.of(new Event(alarm, 1), new Event(burglary, 1), new Event(earthquake, 1))), 0.95,
                new Assignment(List.of(new Event(alarm, 0), new Event(burglary, 1), new Event(earthquake, 1))), 0.05,
                new Assignment(List.of(new Event(alarm, 1), new Event(burglary, 1), new Event(earthquake, 0))), 0.94,
                new Assignment(List.of(new Event(alarm, 0), new Event(burglary, 1), new Event(earthquake, 0))), 0.06,
                new Assignment(List.of(new Event(alarm, 1), new Event(burglary, 0), new Event(earthquake, 1))), 0.29,
                new Assignment(List.of(new Event(alarm, 0), new Event(burglary, 0), new Event(earthquake, 1))), 0.71
        );
        Map<Assignment, Double> amap2 = Map.of(
                new Assignment(List.of(new Event(alarm, 1), new Event(burglary, 0), new Event(earthquake, 0))), 0.001,
                new Assignment(List.of(new Event(alarm, 0), new Event(burglary, 0), new Event(earthquake, 0))), 0.999
        );
        Map<Assignment, Double> amap = new HashMap<>(amap1);
        amap.putAll(amap2);
        AssignmentTableFactor g = new AssignmentTableFactor(List.of(alarm, burglary, earthquake), amap);
        BayesianNode bnAlarmNode = new BayesianNode(alarm);
        bnAlarmNode.setCPT(g);
        FactorNode fgAlarmNode = new FactorNode(alarm);
        fgAlarmNode.setParentalFactor(g);

        Map<Assignment, Double> jmap = Map.of(
                new Assignment(List.of(new Event(alarm, 1), new Event(john, 1))), 0.90,
                new Assignment(List.of(new Event(alarm, 1), new Event(john, 0))), 0.10,
                new Assignment(List.of(new Event(alarm, 0), new Event(john, 1))), 0.05,
                new Assignment(List.of(new Event(alarm, 0), new Event(john, 0))), 0.95
        );
        AssignmentTableFactor s = new AssignmentTableFactor(List.of(alarm, john), jmap);
        BayesianNode bnJohnNode = new BayesianNode(john);
        bnJohnNode.setCPT(s);
        FactorNode fgJohnNode = new FactorNode(john);
        fgJohnNode.setParentalFactor(s);

        Map<Assignment, Double> mmap = Map.of(
                new Assignment(List.of(new Event(alarm, 1), new Event(mary, 1))), 0.7,
                new Assignment(List.of(new Event(alarm, 1), new Event(mary, 0))), 0.3,
                new Assignment(List.of(new Event(alarm, 0), new Event(mary, 1))), 0.01,
                new Assignment(List.of(new Event(alarm, 0), new Event(mary, 0))), 0.99
        );
        AssignmentTableFactor m = new AssignmentTableFactor(List.of(alarm, mary), mmap);
        BayesianNode bnMaryNode = new BayesianNode(mary);
        bnMaryNode.setCPT(m);
        FactorNode fgMaryNode = new FactorNode(mary);
        fgMaryNode.setParentalFactor(m);

        bnAlarmNode.addParent(bnBurglaryNode);
        bnAlarmNode.addParent(bnEarthquakeNode);
        bnJohnNode.addParent(bnAlarmNode);
        bnMaryNode.addParent(bnAlarmNode);

        fgAlarmNode.addParent(fgBurglaryNode);
        fgAlarmNode.addParent(fgEarthquakeNode);
        fgJohnNode.addParent(fgAlarmNode);
        fgMaryNode.addParent(fgAlarmNode);

        bn = new BayesianNetwork(List.of(bnBurglaryNode, bnEarthquakeNode, bnAlarmNode, bnJohnNode, bnMaryNode));
        fg = new FactorGraph(List.of(fgAlarmNode, fgBurglaryNode, fgEarthquakeNode, fgJohnNode, fgMaryNode));
    }

    private static final double DELTA_TOLLERANCE = 0.001;

    @Test
    public void testCompareInferenceMethods() {
        bayesianEnumerationInference = new BayesianEnumerationInference();
        bayesianEnumerationInference.setModel(bn);

        eliminationInference = new FactorEliminationInference();
        eliminationInference.setModel(fg);

        bayesianPropagationInference = new BayesianPropagationInference();
        bayesianPropagationInference.setModel(bn);

        Assignment evidence = new Assignment(List.of(new Event(john, 1), new Event(mary, 1)));
        Assignment question = new Assignment(List.of(new Event(burglary, 1)));
        bayesianEnumerationInference.setEvidence(evidence);
        eliminationInference.setEvidence(evidence);
        bayesianPropagationInference.setEvidence(evidence);

        double groundTruthBurglary = 0.284;
        double resultEnumeration = bayesianEnumerationInference.infer(question);
        double resultElimination = eliminationInference.infer(question);
        double resultPropagation = bayesianPropagationInference.infer(question);
        assertEquals(groundTruthBurglary, resultElimination, DELTA_TOLLERANCE);
        assertEquals(groundTruthBurglary, resultEnumeration, DELTA_TOLLERANCE);
        assertEquals(groundTruthBurglary, resultPropagation, DELTA_TOLLERANCE);
    }
}
