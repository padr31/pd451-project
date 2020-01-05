package uk.ac.cam.pd451.feature.exporter.graph.factor;

import uk.ac.cam.pd451.feature.exporter.inference.Factor;
import uk.ac.cam.pd451.feature.exporter.inference.Variable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FactorNode{

    private Set<FactorNode> parentSet;
    private Set<FactorNode> childSet;

    private Variable variable;
    private Factor parentalFactor;
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

    public Factor getParentalFactor() {
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

    public void setParentalFactor(Factor f) {
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
