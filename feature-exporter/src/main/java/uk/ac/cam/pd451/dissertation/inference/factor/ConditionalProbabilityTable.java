package uk.ac.cam.pd451.dissertation.inference.factor;

import uk.ac.cam.pd451.dissertation.inference.Assignment;
import uk.ac.cam.pd451.dissertation.inference.variable.Variable;

import java.util.*;

/**
 * The conditional probability table encapsulates the discrete function
 * attached to every node of a bayesian network that describes
 * how a variable in a node depends on variables in parent nodes.
 *
 * Beware the size of the table grows exponentially with the number of variables.
 *
 * For convenience in creating and modifying these tables, the function is public and the table is mutable.
 */
public class ConditionalProbabilityTable extends Factor {

    public List<Variable> variables;


    public Map<Assignment, Double> function;

    public ConditionalProbabilityTable(List<Variable> variables, Map<Assignment, Double> function) {
        // TODO make sure that variables reflects the actual variables contained in assignments, variables is redundant
        this.variables = variables;
        this.function = function;
    }

    public Double get(Assignment a) {
        return function.get(a);
    }

    /**
     * @param v
     * @return A conditional table that contains all variables except the argument variable
     * --- v that has been marginalised (summed out).
     */
    public ConditionalProbabilityTable eliminate(Variable v) {
        List<Variable> eliminatedVariables = new ArrayList<>(variables);
        if (!eliminatedVariables.remove(v))
            throw new ArrayIndexOutOfBoundsException("Cannot eliminate a variable that is not contained in the factor.");
        
        Map<Assignment, Double> eliminatedFunction = coalesceFunctionOver(v);

        return new ConditionalProbabilityTable(eliminatedVariables, eliminatedFunction);
    }

    private Map<Assignment, Double> coalesceFunctionOver(Variable v) {
        Map<Assignment, Double> coalescedFunction = new HashMap<>();

        for(Map.Entry<Assignment, Double> entry : function.entrySet()) {
            Assignment removed = entry.getKey().remove(v);
            coalescedFunction.put(removed, coalescedFunction.getOrDefault(removed, 0.0) + entry.getValue());
        }
        return coalescedFunction;
    }

    /**
     * @param other
     * @return Factor that is the multiplication this factor with the other factor
     * and contains the union of variables from both multiplicants.
     */
    public ConditionalProbabilityTable product(Factor other) {
        if(!(other instanceof ConditionalProbabilityTable)) throw new RuntimeException("The product of an AssignmentTableFactor must be with a factor of identical type.");
        List<Variable> varUnion = new ArrayList<>(variables);
        varUnion.addAll(((ConditionalProbabilityTable) other).variables);
        varUnion = new ArrayList<>(new HashSet<>(varUnion));

        Map<Assignment, Double> probs = new HashMap<Assignment, Double>();
        for (Assignment assignment : Assignment.allAssignments(varUnion)) {
            Double prob = 1.0;

            for (Assignment a : ((ConditionalProbabilityTable) other).function.keySet())
                if (assignment.contains(a))
                    prob *= other.get(a);

            for (Assignment a : this.function.keySet())
                if (assignment.contains(a))
                    prob *= get(a);

            probs.put(assignment, prob);
        }

        return new ConditionalProbabilityTable(varUnion, probs);
    }

    /**
     * Normalises the factor so that all values sum to 1.
     */
    public void normalise() {
        double sum = this.function.values().stream().mapToDouble(d -> d).sum();
        if(!(sum == 1.0 || sum == 0.0)) {
            for(Assignment a: this.function.keySet()) {
                this.function.put(a, function.get(a)/sum);
            }
        }
    }

    /**
     * A factor equals another factor if the variables contained are the same
     * and the two functions over the variables yield the same outputs for
     * the same assignment inputs.
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof ConditionalProbabilityTable))
            return false;
        if(!(new HashSet<>(this.variables).equals(new HashSet<>(((ConditionalProbabilityTable) other).variables))))
            return false;
        for(Map.Entry<Assignment, Double> e : this.function.entrySet()) {
            if(!((ConditionalProbabilityTable) other).get(e.getKey()).equals(e.getValue())) return false;
        }
        return true;
    }
}
