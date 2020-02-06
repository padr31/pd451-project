package uk.ac.cam.pd451.feature.exporter.inference;

import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.*;

public class Factor {

    public List<Variable> variables;
    public Map<Assignment, Double> function;

    public Factor(List<Variable> variables, Map<Assignment, Double> function) {
        // TODO make sure that variables reflects the actual variables contained in assignments, variables is redundant
        this.variables = variables;
        this.function = function;
    }

    public Double get(Assignment a) {
        return function.get(a);
    }

    public Factor eliminate(Variable v) {
        List<Variable> eliminatedVariables = new ArrayList<>(variables);
        if (!eliminatedVariables.remove(v))
            throw new ArrayIndexOutOfBoundsException("Cannot eliminate a variable that is not contained in the factor.");
        
        Map<Assignment, Double> eliminatedFunction = coalesceFunctionOver(v);

        return new Factor(eliminatedVariables, eliminatedFunction);
    }

    private Map<Assignment, Double> coalesceFunctionOver(Variable v) {
        Map<Assignment, Double> coalescedFunction = new HashMap<>();

        for(Map.Entry<Assignment, Double> entry : function.entrySet()) {
            Assignment removed = entry.getKey().remove(v);
            coalescedFunction.put(removed, coalescedFunction.getOrDefault(removed, 0.0) + entry.getValue());
        }
        return coalescedFunction;
    }

    public Factor product(Factor other) {
        List<Variable> varUnion = new ArrayList<>(variables);
        varUnion.addAll(other.variables);
        varUnion = new ArrayList<>(new HashSet<>(varUnion));

        Map<Assignment, Double> probs = new HashMap<Assignment, Double>();
        for (Assignment assignment : Assignment.allAssignments(varUnion)) {
            Double prob = 1.0;

            for (Assignment a : other.function.keySet())
                if (assignment.contains(a))
                    prob *= other.get(a);

            for (Assignment a : this.function.keySet())
                if (assignment.contains(a))
                    prob *= get(a);

            probs.put(assignment, prob);
        }

        return new Factor(varUnion, probs);
    }

    public void normalise() {
        double sum = this.function.values().stream().mapToDouble(d -> d).sum();
        if(!(sum == 1.0 || sum == 0.0)) {
            for(Assignment a: this.function.keySet()) {
                this.function.put(a, function.get(a)/sum);
            }
        }
    }
}
