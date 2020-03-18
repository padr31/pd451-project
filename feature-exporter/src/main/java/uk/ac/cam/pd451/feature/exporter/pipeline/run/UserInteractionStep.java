package uk.ac.cam.pd451.feature.exporter.pipeline.run;

import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.feature.exporter.inference.*;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.RankingStatistics;
import uk.ac.cam.pd451.feature.exporter.pipeline.Step;

import java.util.*;
import java.util.stream.Collectors;

public class UserInteractionStep implements Step<ProvenanceGraph, RankingStatistics> {
    @Override
    public RankingStatistics process(ProvenanceGraph g) throws PipeException {
        //collect varPointsTo variables

        System.out.println("Collecting nullPointer variables");
        Map<Predicate, Variable> pointsToSet = g.getPredicateToNode()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals("nullPointer"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getVariable()));

        System.out.println("Initialising inference algorithm");

        LoopyPropagationInference i = new LoopyPropagationInference();
        i.setModel(g.getBayesianNetwork());

        Map<Predicate, Event> evidence = new HashMap<>();
        List<Boolean> trueFalse = new ArrayList<>();
        Map<Predicate, Double> alarmProbabilities = new HashMap<>();

        // insert all alarms with their prior probabilities
        System.out.println("Inferring prior probabilities");

        Map<Event, Double> probs = i.infer(pointsToSet.values().stream().map(v -> new Event(v, 1)).collect(Collectors.toList()));

        for(Map.Entry<Predicate, Variable> pointsToVar : pointsToSet.entrySet()) {
            alarmProbabilities.put(pointsToVar.getKey(), probs.get(new Event(pointsToVar.getValue(), 1)));
        }

        alarmProbabilities.forEach((key, value) -> System.out.println(key.getTerms() + " prob: " + value));

        // re-rank based on user feedback (y/n)
        Scanner scanner = new Scanner(System.in);
        while(alarmProbabilities.size() != 0) {

            // pick alarm with largest probability and present for inspection
            Predicate topAlarm = alarmProbabilities.entrySet().stream().min((a, b) -> b.getValue() - a.getValue() < 0 ? -1 : 1).get().getKey();
            System.out.println("Is this alarm a true positive? (1/0)");
            System.out.println(topAlarm.getTerms());

            if(scanner.nextInt() == 1) {
                // true positive
                Event e = new Event(pointsToSet.get(topAlarm), 1);
                evidence.put(topAlarm, e);
                trueFalse.add(true);
                i.addEvidence(e);
            } else {
                // false positive
                Event e = new Event(pointsToSet.get(topAlarm), 0);
                evidence.put(topAlarm, e);
                trueFalse.add(false);
                i.addEvidence(e);
            }

            // remove the inspected alarm
            alarmProbabilities.remove(topAlarm);

            //re-calculate probabilities of remaining alarms by inference
            System.out.println("Recalculating...");

            Map<Event, Double> newProbs = i.infer(alarmProbabilities.keySet().stream().map(p -> new Event(pointsToSet.get(p), 1)).collect(Collectors.toList()));
            alarmProbabilities.replaceAll((alarm, prob) -> newProbs.get(new Event(pointsToSet.get(alarm), 1)));

            //print new ranking (optional)
            alarmProbabilities.forEach((key, value) -> System.out.println(key.getTerms() + " prob: " + value));
        }
        System.out.println("Ranking done");
        System.out.println("Alarms: " + trueFalse.size());
        System.out.println("Last true positive: " + trueFalse.lastIndexOf(true));
        System.out.println("True positives in first half: " + trueFalse.subList(0, trueFalse.size()/2).stream().filter(b -> b).count());
        System.out.println("True positives in second half: " + trueFalse.subList(trueFalse.size()/2, trueFalse.size()).stream().filter(b -> b).count());
        return null;

    }
}
