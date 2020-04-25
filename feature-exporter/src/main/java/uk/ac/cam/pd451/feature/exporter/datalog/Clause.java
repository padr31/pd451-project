package uk.ac.cam.pd451.feature.exporter.datalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public String getRule() {
        return this.head.getName();
    }

    public String getFullRule() {
        return this.head.getName() + ":-" + this.getBody().stream().map(Predicate::getName).sorted().collect(Collectors.joining(","));
    }

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
