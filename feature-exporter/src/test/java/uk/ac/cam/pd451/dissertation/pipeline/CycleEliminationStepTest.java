package uk.ac.cam.pd451.dissertation.pipeline;

import org.junit.jupiter.api.Test;
import uk.ac.cam.pd451.dissertation.datalog.Clause;
import uk.ac.cam.pd451.dissertation.datalog.Predicate;
import uk.ac.cam.pd451.dissertation.pipeline.optimisations.CycleEliminationStep;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CycleEliminationStepTest {

    /**
     * P(p1,p2),N(p2,p3), U(p1,p3).
     * r1(L6, L6, L7)
     *      \
     *      P()
     */
    @Test
    void testCycleEliminationStep() {
        Pipeline<List<Clause>, List<Clause>> cPipe = new Pipeline<>(new CycleEliminationStep());

        List<Clause> clauses = List.of(
            new Clause(new Predicate("B", "X", "Y"), new Predicate("read_csv1", "X"), new Predicate("read_csv1", "Y")),
            new Clause(new Predicate("C", "X", "Y"), new Predicate("read_csv2", "X"), new Predicate("read_csv2", "Y")),
            new Clause(new Predicate("B", "X", "Y"), new Predicate("C", "X", "Y")),
            new Clause(new Predicate("C", "X", "Y"), new Predicate("B", "X", "Y"))
        );

        List<Clause> clausesWithoutCycle = List.of(
            new Clause(new Predicate("B", "X", "Y"), new Predicate("read_csv1", "X"), new Predicate("read_csv1", "Y")),
            new Clause(new Predicate("C", "X", "Y"), new Predicate("read_csv2", "X"), new Predicate("read_csv2", "Y"))
        );

        List<Clause> cycleEliminatedClauses = cPipe.process(clauses);
        assertTrue(cycleEliminatedClauses.equals(clausesWithoutCycle));

        clauses = List.of(
                new Clause(new Predicate("B", "X", "Y"), new Predicate("read_csv1", "X"), new Predicate("read_csv1", "Y")),
                new Clause(new Predicate("C", "X", "Y"), new Predicate("read_csv2", "X"), new Predicate("read_csv2", "Y")),
                new Clause(new Predicate("B", "X", "Y"), new Predicate("C", "X", "Y")),
                new Clause(new Predicate("C", "X", "Y"), new Predicate("B", "X", "Y")),
                new Clause(new Predicate("D", "X"), new Predicate("B", "X", "Y")),
                new Clause(new Predicate("E", "Y"), new Predicate("C", "X", "Y")),
                new Clause(new Predicate("C", "X", "Y"), new Predicate("D", "X"))
        );

        clausesWithoutCycle = List.of(
                new Clause(new Predicate("B", "X", "Y"), new Predicate("read_csv1", "X"), new Predicate("read_csv1", "Y")),
                new Clause(new Predicate("C", "X", "Y"), new Predicate("read_csv2", "X"), new Predicate("read_csv2", "Y")),
                new Clause(new Predicate("D", "X"), new Predicate("B", "X", "Y")),
                new Clause(new Predicate("E", "Y"), new Predicate("C", "X", "Y")),
                new Clause(new Predicate("C", "X", "Y"), new Predicate("D", "X"))
        );

        cycleEliminatedClauses = cPipe.process(clauses);
        //assertTrue(cycleEliminatedClauses.equals(clausesWithoutCycle));
    }
}