package uk.ac.cam.pd451.feature.exporter.inference.factor;

import uk.ac.cam.pd451.feature.exporter.inference.Assignment;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

import java.util.*;

public class AssignmentTableFactor extends Factor {

    public List<Variable> variables;
    public Map<Assignment, Double> function;

    public AssignmentTableFactor(List<Variable> variables, Map<Assignment, Double> function) {
        // TODO make sure that variables reflects the actual variables contained in assignments, variables is redundant
        this.variables = variables;
        this.function = function;
    }

    public Double get(Assignment a) {
        return function.get(a);
    }

    public AssignmentTableFactor eliminate(Variable v) {
        List<Variable> eliminatedVariables = new ArrayList<>(variables);
        if (!eliminatedVariables.remove(v))
            throw new ArrayIndexOutOfBoundsException("Cannot eliminate a variable that is not contained in the factor.");
        
        Map<Assignment, Double> eliminatedFunction = coalesceFunctionOver(v);

        return new AssignmentTableFactor(eliminatedVariables, eliminatedFunction);
    }

    private Map<Assignment, Double> coalesceFunctionOver(Variable v) {
        Map<Assignment, Double> coalescedFunction = new HashMap<>();

        for(Map.Entry<Assignment, Double> entry : function.entrySet()) {
            Assignment removed = entry.getKey().remove(v);
            coalescedFunction.put(removed, coalescedFunction.getOrDefault(removed, 0.0) + entry.getValue());
        }
        return coalescedFunction;
    }

    public AssignmentTableFactor product(Factor other) {
        if(!(other instanceof AssignmentTableFactor)) throw new RuntimeException("The product of an AssignmentTableFactor must be with a factor of identical type.");
        List<Variable> varUnion = new ArrayList<>(variables);
        varUnion.addAll(((AssignmentTableFactor) other).variables);
        varUnion = new ArrayList<>(new HashSet<>(varUnion));

        Map<Assignment, Double> probs = new HashMap<Assignment, Double>();
        for (Assignment assignment : Assignment.allAssignments(varUnion)) {
            Double prob = 1.0;

            for (Assignment a : ((AssignmentTableFactor) other).function.keySet())
                if (assignment.contains(a))
                    prob *= other.get(a);

            for (Assignment a : this.function.keySet())
                if (assignment.contains(a))
                    prob *= get(a);

            probs.put(assignment, prob);
        }

        return new AssignmentTableFactor(varUnion, probs);
    }

    public void normalise() {
        double sum = this.function.values().stream().mapToDouble(d -> d).sum();
        if(!(sum == 1.0 || sum == 0.0)) {
            for(Assignment a: this.function.keySet()) {
                this.function.put(a, function.get(a)/sum);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof AssignmentTableFactor))
            return false;
        if(!(new HashSet<>(this.variables).equals(new HashSet<>(((AssignmentTableFactor) other).variables))))
            return false;
        for(Map.Entry<Assignment, Double> e : this.function.entrySet()) {
            if(!((AssignmentTableFactor) other).get(e.getKey()).equals(e.getValue())) return false;
        }
        return true;
    }
}
