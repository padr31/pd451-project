package uk.ac.cam.pd451.dissertation.graph.bn;

import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.inference.variable.VariableIdentifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class encalsulates the nodes of a bayesian network.
 * BayesianNodes can be accessed by their corresponding variables.
 */
public class BayesianNetwork {

    private Map<VariableIdentifier, BayesianNode> nodes;

    /**
     * Constructs a BayesianNetwork from a list of BayesianNodes.
     * The nodes need to be set up with correct parent and child pointers.
     * @param nodes
     */
    public BayesianNetwork(List<BayesianNode> nodes) {
        this.nodes = nodes.stream().collect(Collectors.toMap(n -> n.getVariable().getId(), n -> n));
    }

    /**
     * @return A list of bayesian nodes in the topological order,
     * which means that if node a comes before node b,
     * then it is guaranteed that there is no directed path from b to a.
     */
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
        return this.nodes.get(v.getId());
    }

    /**
     * @return A list of nodes that do not have any parents.
     */
    public List<BayesianNode> getRoots() {
        return this.nodes.values().stream().filter(BayesianNode::isRoot).collect(Collectors.toList());
    }
}
