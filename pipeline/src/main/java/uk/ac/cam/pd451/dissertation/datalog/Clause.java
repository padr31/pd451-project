package uk.ac.cam.pd451.dissertation.datalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Represents a Datalog clause head_tuple("A") :- body1_tuple("1"), body2_tuple("2"), ... .
 * The class is immutable and can be used as map keys.
 */
public class Clause {

    private Predicate head;
    private List<Predicate> body;

    public Clause(Predicate head, Predicate...body) {
        this.head = head;
        this.body = Arrays.asList(body);
    }

    public Clause(Predicate head, List<Predicate> body) {
        this.head = head;
        this.body = new ArrayList<>(body);
    }

    /**
     * @return The name of the clause as given by the head tuple.
     */
    public String getRule() {
        return this.head.getName();
    }

    /**
     * @return The name of the clause as given fully by its antecedent tuples and head tuple.
     * This is more unique than getRule().
     */
    public String getFullRule() {
        return this.head.getName() + ":-" + this.getBody().stream().map(Predicate::getName).sorted().collect(Collectors.joining(","));
    }

    /**
     * Two clauses are equal if and only if their head and body tuples are both equal.
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Clause)) return false;
        return this.head.equals(((Clause) other).head) && this.body.equals(((Clause) other).body);
    }

    @Override
    public int hashCode() {
        return this.head.hashCode() + this.body.hashCode();
    }

    public Predicate getHead() {
        return head;
    }

    public List<Predicate> getBody() {
        return Collections.unmodifiableList(body);
    }
}
