package uk.ac.cam.pd451.feature.exporter.graph.factor;

import uk.ac.cam.pd451.feature.exporter.inference.variable.VariableIdentifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A FactorGraph is a graph that contains two types of nodes --- factors, and basic nodes.
 * Two nodes of the same type can not be neighboring.
 * Factors specify a discrete function on its neighboring variables.
 * A BayesianNetwork is a factor graph.
 * The structure is represented as only basic nodes that contain their relevant factors.
 */
public class FactorGraph {
    private Map<VariableIdentifier, FactorNode> nodes;

    public FactorGraph(List<FactorNode> nodes) {
        this.nodes = nodes.stream().collect(Collectors.toMap(n -> n.getVariable().getId(), n -> n));
    }

    /**
     * @return Topological ordering of nodes in a factor graph.
     * If node a comes before node b then there is no directed path from b to a.
     */
    public List<FactorNode> topologicalOrdering(){
        List<FactorNode> topologicallyOrderedNodes = new ArrayList<>();

        Stack<FactorNode> stack = new Stack<>();
        Set<FactorNode> visited = new HashSet<>();

        for (FactorNode node : this.nodes.values())
            if (!visited.contains(node))
                topSort(node, visited, stack);

        while (!stack.empty())
            topologicallyOrderedNodes.add(stack.pop());

        return topologicallyOrderedNodes;
    }

    private void topSort(FactorNode node, Set<FactorNode> visited, Stack<FactorNode> stack) {
        visited.add(node);
        for(FactorNode n : node.getChildSet()) {
            if (!visited.contains(n))
                topSort(n, visited, stack);
        }
        stack.push(node);
    }
}
