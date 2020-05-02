package uk.ac.cam.pd451.feature.exporter.graph;

import java.util.*;

/**
 * Abstract class that represents a graphical model that has a topological sort method.
 * This is the parent class of both FactorGraph and BayesianNetwork.
 * @param <T>
 */
public class AbstractGraph<T extends AbstractNode> {
    protected List<T> nodes;

    public List<T> topologicalOrdering(){
        List<T> topologicallyOrderedNodes = new ArrayList<>();
        Stack<T> stack = new Stack<>();
        Set<T> visited = new HashSet<>();

        for (T node : nodes)
            if (!visited.contains(node))
                topSort(node, visited, stack);

        // Print contents of stack
        while (!stack.empty())
            topologicallyOrderedNodes.add(stack.pop());

        return topologicallyOrderedNodes;
    }

    private void topSort(T node, Set<T> visited, Stack<T> stack) {
        visited.add(node);
        for(AbstractNode child : node.getChildren()) {
            if (!visited.contains((T) child))
                topSort((T) child, visited, stack);
        }
        stack.push(node);
    }
}
