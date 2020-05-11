package uk.ac.cam.pd451.dissertation.inference.variable;

public class VariableNameIdentifier extends VariableIdentifier {

    private String name;

    public VariableNameIdentifier(String name) {
        this.name = name;
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof VariableNameIdentifier)) {
            return false;
        } else {
            return this.name.equals(((VariableNameIdentifier) other).name);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
