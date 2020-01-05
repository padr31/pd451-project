package uk.ac.cam.pd451.feature.exporter.graph.bn;

import uk.ac.cam.pd451.feature.exporter.graph.factor.FactorNode;
import uk.ac.cam.pd451.feature.exporter.inference.Factor;
import uk.ac.cam.pd451.feature.exporter.inference.Variable;

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
    private Factor cpt;
    private String id = UUID.randomUUID().toString();

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

    public Factor getCPT() {
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

    public void setCPT(Factor cpt) {
        this.cpt = cpt;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof FactorNode)) return false;
        return this.id.equals(((FactorNode) other).getId());
    }

    public boolean isRoot() {
        if (this.parentSet == null) return true;
        else return this.parentSet.isEmpty();
    }
}
