package uk.ac.cam.pd451.dissertation.graph.factor;

import uk.ac.cam.pd451.dissertation.inference.factor.ConditionalProbabilityTable;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FactorNode{

    private Set<FactorNode> parentSet;
    private Set<FactorNode> childSet;

    private Variable variable;
    private ConditionalProbabilityTable parentalFactor;
    private String id = UUID.randomUUID().toString();

    public FactorNode(Variable variable) {
        this.variable = variable;
        this.parentSet = new HashSet<>();
        this.childSet = new HashSet<>();
    }


    public Variable getVariable() {
        return this.variable;
    }


    public Set<FactorNode> getParentSet() {
        return parentSet;
    }

    public Set<FactorNode> getChildSet() {
        return childSet;
    }

    public ConditionalProbabilityTable getParentalFactor() {
        return parentalFactor;
    }

    public void addParent(FactorNode node) {
        this.parentSet.add(node);
        node.childSet.add(this);
    }

    public void addChild(FactorNode node) {
        this.childSet.add(node);
        node.parentSet.add(this);
    }

    public void setParentalFactor(ConditionalProbabilityTable f) {
        this.parentalFactor = f;
    }

    public String getId(){
        return this.id;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof FactorNode)) return false;
        return this.id.equals(((FactorNode)other).getId());
    }
}
