package uk.ac.cam.pd451.dissertation.pipeline;

import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.datalog.Clause;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.dissertation.inference.Assignment;
import uk.ac.cam.pd451.dissertation.inference.BayesianPropagationInference;
import uk.ac.cam.pd451.dissertation.inference.BayessianGibbsSamplingInference;
import uk.ac.cam.pd451.dissertation.inference.Event;
import uk.ac.cam.pd451.dissertation.inference.variable.VariableClauseIdentifier;
import uk.ac.cam.pd451.dissertation.inference.variable.VariablePredicateIdentifier;
import uk.ac.cam.pd451.dissertation.pipeline.run.NetworkCreationStep;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNetwork;
import uk.ac.cam.pd451.dissertation.graph.bn.BayesianNode;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NetworkCreationStepTest {

    @Test
    void process() {
        Predicate a = new Predicate("a", List.of("X"));
        Predicate b = new Predicate("b", List.of("Y"));
        Predicate c = new Predicate("c", List.of("Z"));
        Predicate ab = new Predicate("ab", List.of("X", "Y"));
        Predicate bc = new Predicate("bc", List.of("Y", "Z"));
        Predicate abc = new Predicate("abc", List.of("X", "Y", "Z"));

        Clause abCl = new Clause(ab, List.of(a, b, c));
        Clause bcCl = new Clause(bc, List.of(b, c));
        Clause abcCl = new Clause(abc, List.of(ab, bc));
        Clause abcCl2 = new Clause(abc, List.of(ab));

        List<Clause> groundClauses = List.of(abCl, bcCl, abcCl, abcCl2);

        Pipeline<List<Clause>, ProvenanceGraph> provenanceCreationPipeline = new Pipeline<>(new NetworkCreationStep());
        BayesianNetwork bnResult = provenanceCreationPipeline.process(groundClauses).getBayesianNetwork();

        Set<Integer> binaryDomain = Set.of(0, 1);
        Variable aVariable = new Variable(new VariablePredicateIdentifier(a), binaryDomain);
        BayesianNode aNode = new BayesianNode(aVariable);
        Variable bVariable = new Variable(new VariablePredicateIdentifier(b), binaryDomain);
        BayesianNode bNode = new BayesianNode(bVariable);
        Variable cVariable = new Variable(new VariablePredicateIdentifier(c), binaryDomain);
        BayesianNode cNode = new BayesianNode(cVariable);
        Variable abVariable = new Variable(new VariablePredicateIdentifier(ab), binaryDomain);
        BayesianNode abNode = new BayesianNode(abVariable);
        Variable bcVariable = new Variable(new VariablePredicateIdentifier(bc), binaryDomain);
        BayesianNode bcNode = new BayesianNode(bcVariable);
        Variable abcVariable = new Variable(new VariablePredicateIdentifier(abc), binaryDomain);
        BayesianNode abcNode = new BayesianNode(abcVariable);
        Variable abClVariable = new Variable(new VariableClauseIdentifier(abCl), binaryDomain);
        BayesianNode abClNode = new BayesianNode(abClVariable);
        Variable bcClVariable = new Variable(new VariableClauseIdentifier(bcCl), binaryDomain);
        BayesianNode bcClNode = new BayesianNode(bcClVariable);
        Variable abcClVariable = new Variable(new VariableClauseIdentifier(abcCl), binaryDomain);
        BayesianNode abcClNode = new BayesianNode(abcClVariable);
        Variable abcCl2Variable = new Variable(new VariableClauseIdentifier(abcCl2), binaryDomain);
        BayesianNode abcCl2Node = new BayesianNode(abcCl2Variable);

        // Parental connections
        abNode.addParent(abClNode);

        bcNode.addParent(bcClNode);

        abcNode.addParent(abcClNode);

        abcNode.addParent(abcCl2Node);

        abClNode.addParent(aNode);
        abClNode.addParent(bNode);
        abClNode.addParent(cNode);

        bcClNode.addParent(bNode);
        bcClNode.addParent(cNode);

        abcClNode.addParent(abNode);
        abcClNode.addParent(bcNode);
        abcCl2Node.addParent(abNode);

        // CPTs
        ConditionalProbabilityTable aCPT = new ConditionalProbabilityTable(List.of(aVariable), Map.of(
                new Assignment(List.of(new Event(aVariable, 0))), 0.0,
                new Assignment(List.of(new Event(aVariable, 1))), 1.0
        ));
        aNode.setCPT(aCPT);

        ConditionalProbabilityTable bCPT = new ConditionalProbabilityTable(List.of(bVariable), Map.of(
                new Assignment(List.of(new Event(bVariable, 0))), 0.0,
                new Assignment(List.of(new Event(bVariable, 1))), 1.0
        ));
        bNode.setCPT(bCPT);

        ConditionalProbabilityTable cCPT = new ConditionalProbabilityTable(List.of(cVariable), Map.of(
                new Assignment(List.of(new Event(cVariable, 0))), 0.0,
                new Assignment(List.of(new Event(cVariable, 1))), 1.0
        ));
        cNode.setCPT(cCPT);

        Map<Assignment, Double> m1 = Map.of(
                new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 0), new Event(cVariable, 0), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 0), new Event(cVariable, 1), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 1), new Event(cVariable, 0), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 1), new Event(cVariable, 1), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 0), new Event(cVariable, 0), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 0), new Event(cVariable, 1), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 1), new Event(cVariable, 0), new Event(abClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 1), new Event(cVariable, 1), new Event(abClVariable, 0))), 0.05
        );
        Map<Assignment, Double> m2 = Map.of(
                    new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 0), new Event(cVariable, 0), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 0), new Event(cVariable, 1), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 1), new Event(cVariable, 0), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 0), new Event(bVariable, 1), new Event(cVariable, 1), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 0), new Event(cVariable, 0), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 0), new Event(cVariable, 1), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 1), new Event(cVariable, 0), new Event(abClVariable, 1))), 0.1,
                    new Assignment(List.of(new Event(aVariable, 1), new Event(bVariable, 1), new Event(cVariable, 1), new Event(abClVariable, 1))), 0.95);
        Map<Assignment, Double> m = new HashMap<>(m1);
        m.putAll(m2);
        ConditionalProbabilityTable abClCPT = new ConditionalProbabilityTable(List.of(aVariable, bVariable, cVariable, abClVariable), m);
        abClNode.setCPT(abClCPT);

        ConditionalProbabilityTable bcClCPT = new ConditionalProbabilityTable(List.of(bVariable, cVariable, bcClVariable), Map.of(
                new Assignment(List.of(new Event(bVariable, 0), new Event(cVariable, 0), new Event(bcClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(bVariable, 0), new Event(cVariable, 1), new Event(bcClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(bVariable, 1), new Event(cVariable, 0), new Event(bcClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(bVariable, 1), new Event(cVariable, 1), new Event(bcClVariable, 0))), 0.05,
                new Assignment(List.of(new Event(bVariable, 0), new Event(cVariable, 0), new Event(bcClVariable, 1))), 0.1,
                new Assignment(List.of(new Event(bVariable, 0), new Event(cVariable, 1), new Event(bcClVariable, 1))), 0.1,
                new Assignment(List.of(new Event(bVariable, 1), new Event(cVariable, 0), new Event(bcClVariable, 1))), 0.1,
                new Assignment(List.of(new Event(bVariable, 1), new Event(cVariable, 1), new Event(bcClVariable, 1))), 0.95
        ));
        bcClNode.setCPT(bcClCPT);

        ConditionalProbabilityTable abcClCPT = new ConditionalProbabilityTable(List.of(abVariable, bcVariable, abcClVariable), Map.of(
                new Assignment(List.of(new Event(abVariable, 0), new Event(bcVariable, 0), new Event(abcClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(abVariable, 0), new Event(bcVariable, 1), new Event(abcClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(abVariable, 1), new Event(bcVariable, 0), new Event(abcClVariable, 0))), 0.9,
                new Assignment(List.of(new Event(abVariable, 1), new Event(bcVariable, 1), new Event(abcClVariable, 0))), 0.05,
                new Assignment(List.of(new Event(abVariable, 0), new Event(bcVariable, 0), new Event(abcClVariable, 1))), 0.1,
                new Assignment(List.of(new Event(abVariable, 0), new Event(bcVariable, 1), new Event(abcClVariable, 1))), 0.1,
                new Assignment(List.of(new Event(abVariable, 1), new Event(bcVariable, 0), new Event(abcClVariable, 1))), 0.1,
                new Assignment(List.of(new Event(abVariable, 1), new Event(bcVariable, 1), new Event(abcClVariable, 1))), 0.95
        ));
        abcClNode.setCPT(abcClCPT);

        ConditionalProbabilityTable abcCl2CPT = new ConditionalProbabilityTable(List.of(abVariable, abcCl2Variable), Map.of(
                new Assignment(List.of(new Event(abVariable, 0), new Event(abcCl2Variable, 0))), 0.9,
                new Assignment(List.of(new Event(abVariable, 1), new Event(abcCl2Variable, 0))), 0.05,
                new Assignment(List.of(new Event(abVariable, 0), new Event(abcCl2Variable, 1))), 0.1,
                new Assignment(List.of(new Event(abVariable, 1), new Event(abcCl2Variable, 1))), 0.95
        ));
        abcCl2Node.setCPT(abcCl2CPT);

        ConditionalProbabilityTable abCPT = new ConditionalProbabilityTable(List.of(abClVariable, abVariable), Map.of(
                new Assignment(List.of(new Event(abClVariable, 0), new Event(abVariable, 0))), 0.9,
                new Assignment(List.of(new Event(abClVariable, 1), new Event(abVariable, 0))), 0.1,
                new Assignment(List.of(new Event(abClVariable, 0), new Event(abVariable, 1))), 0.1,
                new Assignment(List.of(new Event(abClVariable, 1), new Event(abVariable, 1))), 0.9
        ));
        abNode.setCPT(abCPT);

        ConditionalProbabilityTable bcCPT = new ConditionalProbabilityTable(List.of(bcClVariable, bcVariable), Map.of(
                new Assignment(List.of(new Event(bcClVariable, 0), new Event(bcVariable, 0))), 0.9,
                new Assignment(List.of(new Event(bcClVariable, 1), new Event(bcVariable, 0))), 0.1,
                new Assignment(List.of(new Event(bcClVariable, 0), new Event(bcVariable, 1))), 0.1,
                new Assignment(List.of(new Event(bcClVariable, 1), new Event(bcVariable, 1))), 0.9
        ));
        bcNode.setCPT(bcCPT);

        ConditionalProbabilityTable abcCPT = new ConditionalProbabilityTable(List.of(abcClVariable, abcCl2Variable, abcVariable), Map.of(
                new Assignment(List.of(new Event(abcClVariable, 0), new Event(abcCl2Variable, 0), new Event(abcVariable, 0))), 0.9,
                new Assignment(List.of(new Event(abcClVariable, 0), new Event(abcCl2Variable, 1), new Event(abcVariable, 0))), 0.1,
                new Assignment(List.of(new Event(abcClVariable, 1), new Event(abcCl2Variable, 0), new Event(abcVariable, 0))), 0.1,
                new Assignment(List.of(new Event(abcClVariable, 1), new Event(abcCl2Variable, 1), new Event(abcVariable, 0))), 0.1,
                new Assignment(List.of(new Event(abcClVariable, 0), new Event(abcCl2Variable, 0), new Event(abcVariable, 1))), 0.1,
                new Assignment(List.of(new Event(abcClVariable, 0), new Event(abcCl2Variable, 1), new Event(abcVariable, 1))), 0.9,
                new Assignment(List.of(new Event(abcClVariable, 1), new Event(abcCl2Variable, 0), new Event(abcVariable, 1))), 0.9,
                new Assignment(List.of(new Event(abcClVariable, 1), new Event(abcCl2Variable, 1), new Event(abcVariable, 1))), 0.9
                ));
        abcNode.setCPT(abcCPT);

        BayesianNetwork expectedResult = new BayesianNetwork(List.of(aNode, bNode, cNode, abNode, bcNode, abcNode, abClNode, bcClNode, abcClNode, abcCl2Node));

        bnResult.topologicalOrdering().forEach(resultNode -> {
            BayesianNode expectedNode = expectedResult.topologicalOrdering().stream().filter(node -> resultNode.getVariable().equals(node.getVariable())).findFirst().get();
            assertEquals(
                    expectedNode.getVariable(),
                    resultNode.getVariable());
            assertEquals(
                    expectedNode.getParentSet().stream().map(BayesianNode::getVariable).collect(Collectors.toSet()),
                    resultNode.getParentSet().stream().map(BayesianNode::getVariable).collect(Collectors.toSet()));
            assertEquals(
                    expectedNode.getChildSet().stream().map(BayesianNode::getVariable).collect(Collectors.toSet()),
                    resultNode.getChildSet().stream().map(BayesianNode::getVariable).collect(Collectors.toSet()));

            /*assertEquals(
                    expectedNode.getCPT(),
                    resultNode.getCPT()
            );*/
        });

        BayessianGibbsSamplingInference i = new BayessianGibbsSamplingInference();
        i.setModel(expectedResult);

        BayesianPropagationInference i2 = new BayesianPropagationInference();
        i2.setModel(expectedResult);

        System.out.println(i.infer(new Assignment(List.of(new Event(abcVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(aVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(aVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));

        i.addEvidence(new Event(abVariable, 0));
        i2.addEvidence(new Event(abVariable, 0));
        System.out.println("Evidence set");

        System.out.println(i.infer(new Assignment(List.of(new Event(abcVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(aVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(aVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));

        i.addEvidence(new Event(aVariable, 0));
        i2.addEvidence(new Event(aVariable, 0));
        System.out.println("Evidence set");

        System.out.println(i.infer(new Assignment(List.of(new Event(abcVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(aVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(aVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));

        i.addEvidence(new Event(abcVariable, 1));
        i2.addEvidence(new Event(abcVariable, 1));
        System.out.println("Evidence set");

        System.out.println(i.infer(new Assignment(List.of(new Event(abcVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(aVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(aVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(bcClVariable, 1)))));

        System.out.println(i.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));
        System.out.println(i2.infer(new Assignment(List.of(new Event(abcClVariable, 1)))));
    }
}