package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.*;
import java.util.stream.Collectors;

public class FactorGraph {
    private Map<String, Variable> variables;

    public FactorGraph(List<Variable> variables) {
        this.variables = variables.stream().collect(Collectors.toMap(Variable::getName, v -> v));
    }

    public List<Variable> topologicalOrdering(){
        List<Variable> topologicallyOrderedNodes = new ArrayList<>();
        Stack<Variable> stack = new Stack<>();
        Set<Variable> visited = new HashSet<>();

        for (Variable var : this.variables.values())
            if (!visited.contains(var))
                topSort(var, visited, stack);

        // Print contents of stack
        while (!stack.empty())
            topologicallyOrderedNodes.add(stack.pop());

        return topologicallyOrderedNodes;
    }

    private void topSort(Variable variable, Set<Variable> visited, Stack<Variable> stack) {
        visited.add(variable);
        for(Variable var : variable.getChildSet()) {
            if (!visited.contains(var))
                topSort(var, visited, stack);
        }
        stack.push(variable);
    }
}
