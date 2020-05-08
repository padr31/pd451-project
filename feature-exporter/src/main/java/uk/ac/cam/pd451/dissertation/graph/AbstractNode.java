package uk.ac.cam.pd451.dissertation.graph;

import java.util.List;
import java.util.UUID;

public abstract class AbstractNode {
    private List<AbstractNode> parents;
    private List<AbstractNode> children;
    private String id = UUID.randomUUID().toString();

    public List<AbstractNode> getParents() {
        return parents;
    }

    public void setParents(List<AbstractNode> parents) {
        this.parents = parents;
    }

    public List<AbstractNode> getChildren() {
        return children;
    }

    public void setChildren(List<AbstractNode> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AbstractNode)) return false;
        return this.id.equals(((AbstractNode) obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
