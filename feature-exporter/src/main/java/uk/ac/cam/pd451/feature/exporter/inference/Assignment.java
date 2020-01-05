package uk.ac.cam.pd451.feature.exporter.inference;

import java.util.*;
import java.util.stream.Collectors;

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

    public boolean contradicts(Assignment evidence) {
        Map<String, Event> m = new HashMap<>();
        this.events.forEach(e -> m.put(e.getVariable().getName(), e));
        for(Event e : evidence.events) {
            String varName = e.getVariable().getName();
            if (
                m.containsKey(varName) &&
                m.get(e.getVariable().getName()).getValue() != e.getValue()
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
     * If the evidence contains overlap with others then deal with it
     * @param evidence
     * @return
     */
    public Assignment combineWith(Assignment evidence) {
        List<Event> eventsAndEvidence = new ArrayList<>(this.events);
        eventsAndEvidence.addAll(evidence.events);
        return new Assignment(eventsAndEvidence);
    }
}
