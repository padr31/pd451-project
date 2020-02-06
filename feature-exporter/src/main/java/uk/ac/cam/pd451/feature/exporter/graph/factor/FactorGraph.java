package uk.ac.cam.pd451.feature.exporter.graph.factor;

import uk.ac.cam.pd451.feature.exporter.inference.variable.VariableIdentifier;

import java.util.*;
import java.util.stream.Collectors;

public class FactorGraph {
    private Map<VariableIdentifier, FactorNode> nodes;

    public FactorGraph(List<FactorNode> nodes) {
        this.nodes = nodes.stream().collect(Collectors.toMap(n -> n.getVariable().getId(), n -> n));
    }

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
