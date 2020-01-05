package uk.ac.cam.pd451.feature.exporter.graph.bn;

import uk.ac.cam.pd451.feature.exporter.inference.Variable;

import java.util.*;
import java.util.stream.Collectors;

public class BayesianNetwork {

    private Map<String, BayesianNode> nodes;

    public BayesianNetwork(List<BayesianNode> nodes) {
        this.nodes = nodes.stream().collect(Collectors.toMap(n -> n.getVariable().getName(), n -> n));
    }

    public List<BayesianNode> topologicalOrdering(){
        List<BayesianNode> topologicallyOrderedNodes = new ArrayList<>();

        Stack<BayesianNode> stack = new Stack<>();
        Set<BayesianNode> visited = new HashSet<>();

        for (BayesianNode node : this.nodes.values())
            if (!visited.contains(node))
                topSort(node, visited, stack);

        while (!stack.empty())
            topologicallyOrderedNodes.add(stack.pop());

        return topologicallyOrderedNodes;
    }

    private void topSort(BayesianNode node, Set<BayesianNode> visited, Stack<BayesianNode> stack) {
        visited.add(node);
        for(BayesianNode n : node.getChildSet()) {
            if (!visited.contains(n))
                topSort(n, visited, stack);
        }
        stack.push(node);
    }

    public BayesianNode getNode(Variable v) {
        return this.nodes.get(v.getName());
    }

    public List<BayesianNode> getRoots() {
        return this.nodes.values().stream().filter(BayesianNode::isRoot).collect(Collectors.toList());
    }
}
