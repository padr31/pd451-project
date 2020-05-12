package uk.ac.cam.pd451.dissertation.inference;

import uk.ac.cam.pd451.dissertation.inference.variable.Variable;
import uk.ac.cam.pd451.dissertation.inference.variable.VariableIdentifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulates a valuation of a set of variables.
 * The valuation is given as a set of Event objects.
 *
 * An assignment is immutable.
 * An assignment can and is used as map keys in ConditionalProbabilityTable.
 */
public class Assignment {
    List<Event> events;

    public Assignment(List<Event> events) {
        this.events = new ArrayList<>(events);
    }

    public Assignment(Assignment assignment, Assignment newAssignment) {
        this.events = new ArrayList<>(newAssignment.events);
        assignment.events.forEach(e -> {
            if(!this.contains(e.getVariable())) this.addEvent(e);
        });
    }

    /**
     *
     * @param varList
     * @return Returns all possible assignments of the variables in the argument
     * to binary values 0 and 1. The number of all assignments is exponential
     * in the number of variables.
     */
    public static List<Assignment> allAssignments(List<Variable> varList) {
        List<Variable> varListCopy = new ArrayList<>(varList);
        return recAllAssignments(varListCopy);
    }

    private static List<Assignment> recAllAssignments(List<Variable> varList) {
        List<Assignment> result = new ArrayList<>();
        if(varList.size() == 0) return result;
        else if(varList.size() == 1) {
            Variable v = varList.get(0);
            for(int i : v.getDomain()) {
                result.add(new Assignment(List.of(new Event(v, i))));
            }
            return result;
        } else {
            Variable v = varList.remove(0);
            List<Assignment> rest = allAssignments(varList);
            for(int i: v.getDomain()) {
                for(Assignment a: rest) {
                    result.add(a.addEvent(new Event(v, i)));
                }
            }
            return result;
        }
    }

    public int getValue(Variable v) {
        Optional<Event> first = this.events.stream().filter(e -> e.getVariable().equals(v)).findFirst();
        if (!first.isPresent()) {
            throw new RuntimeException("Does not contain the variable.");
        } else {
            return first.get().getValue();
        }
    }

    public Assignment addEvent(Event e) {
        List<Event> events = new ArrayList<>(this.events);
        events.add(e);
        return new Assignment(events);
    }

    public boolean contains(Variable v) {
        return events.stream().anyMatch(e -> e.getVariable().equals(v));
    }

    public boolean contains(Event e) {
        return events.contains(e);
    }

    public boolean contains(Assignment a) {
        for(Event e : a.events)
            if(!this.contains(e)) return false;
        return true;
    }

    /**
     * @param evidence
     * @return Returns true if two assignments value one variable in two different ways, false otherwise.
     */
    public boolean contradicts(Assignment evidence) {
        Map<VariableIdentifier, Event> m = new HashMap<>();
        this.events.forEach(e -> m.put(e.getVariable().getId(), e));
        for(Event e : evidence.events) {
            VariableIdentifier varName = e.getVariable().getId();
            if (
                m.containsKey(varName) &&
                m.get(e.getVariable().getId()).getValue() != e.getValue()
            ) return true;
        }
        return false;
    }

    public Assignment remove(Variable v) {
        return new Assignment(events
                .stream()
                .filter(e -> !e.getVariable().equals(v))
                .collect(Collectors.toList())
        );
    }

    public boolean equals(Object other) {
        if (!(other instanceof Assignment)) return false;
        Assignment o = (Assignment) other;
        if (events.size() != o.events.size()) return false;

        return (new HashSet<>(this.events)).equals(new HashSet<>(((Assignment) other).events));
    }

    public int hashCode() {
        int ret = 1;
        for (Event e : events)
            ret *= e.hashCode();
        return ret;
    }

    public Set<Variable> getVariables() {
        return new HashSet<>(this.events.stream().map(e -> e.getVariable()).collect(Collectors.toList()));
    }

    /**
     * Creates an assignment that combines the valuation of variables in this and the evidence argument.
     * Beware that there is no check for overlapping variables and therefore the result of this function
     * may be a contradicting valuation that values the same variable in two different ways. This is not
     * a bug but a feature of this function.
     * @param evidence
     * @return
     */
    public Assignment combineWith(Assignment evidence) {
        List<Event> eventsAndEvidence = new ArrayList<>(this.events);
        eventsAndEvidence.addAll(evidence.events);
        return new Assignment(eventsAndEvidence);
    }
}
