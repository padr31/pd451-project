package uk.ac.cam.pd451.dissertation.graph.bn;

import uk.ac.cam.pd451.dissertation.graph.factor.FactorNode;
import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This is a mutable class. It can not be used in maps. It's variable is immutable.
 * The topological sort distinguishes nodes only based on their id's, which are unique.
 * Equals should only be used for the purpose of sorting, that is why id is private.
 */
public class BayesianNode {

    private Set<BayesianNode> parentSet;
    private Set<BayesianNode> childSet;

    private Variable variable;
    private ConditionalProbabilityTable cpt;
    private String id = UUID.randomUUID().toString();

    /**
     * Creates a BayesianNode representing a particular random variable.
     * @param variable
     */
    public BayesianNode(Variable variable) {
        this.variable = variable;
        this.parentSet = new HashSet<>();
        this.childSet = new HashSet<>();
    }


    public Variable getVariable() {
        return this.variable;
    }

    public Set<BayesianNode> getParentSet() {
        return this.parentSet;
    }

    public Set<BayesianNode> getChildSet() {
        return this.childSet;
    }

    /**
     * This needs to be updated whenever a parent is added.
     * @return The conditional probability table that gives how the node depends on parents.
     */
    public ConditionalProbabilityTable getCPT() {
        return this.cpt;
    }

    public void addParent(BayesianNode node) {
        this.parentSet.add(node);
        node.childSet.add(this);
    }

    public void addChild(BayesianNode node) {
        this.childSet.add(node);
        node.parentSet.add(this);
    }

    public void setCPT(ConditionalProbabilityTable cpt) {
        this.cpt = cpt;
    }

    /**
     * For nodes to be equal their unique ids are equal.
     * This is to avoid nodes with same content being treated equal.
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof FactorNode)) return false;
        return this.id.equals(((FactorNode) other).getId());
    }

    /**
     * @return A boolean that is true if and only if the node does not depend on parents.
     */
    public boolean isRoot() {
        if (this.parentSet == null) return true;
        else return this.parentSet.isEmpty();
    }
}
