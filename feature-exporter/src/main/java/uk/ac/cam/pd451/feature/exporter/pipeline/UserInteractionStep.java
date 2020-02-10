package uk.ac.cam.pd451.feature.exporter.pipeline;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.feature.exporter.inference.*;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;

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

        BayessianGibbsSamplingInference i = new BayessianGibbsSamplingInference();
        i.setModel(g.getBayesianNetwork());

        Map<Predicate, Event> evidence = new HashMap<>();
        Map<Predicate, Double> alarmProbabilities = new HashMap<>();

        // insert all alarms with their prior probabilities
        System.out.println("Inferring prior probabilities");
        ProgressBar pb = new ProgressBar("Inference of priors", pointsToSet.size(), ProgressBarStyle.ASCII);
        pb.start();
        for(Map.Entry<Predicate, Variable> pointsToVar : pointsToSet.entrySet()) {
            pb.step();
            alarmProbabilities.put(pointsToVar.getKey(), i.infer(new Assignment(List.of(new Event(pointsToVar.getValue(), 1)))));
        }
        pb.stop();

        // re-rank based on user feedback (y/n)
        Scanner scanner = new Scanner(System.in);
        while(alarmProbabilities.size() != 0) {

            // pick alarm with largest probability and present for inspection
            Predicate topAlarm = alarmProbabilities.entrySet().stream().min((a, b) -> a.getValue() - b.getValue() < 0 ? -1 : 1).get().getKey();
            System.out.println("Is this alarm a true positive? (1/0)");
            System.out.println(topAlarm.getTerms());

            if(scanner.nextInt() == 1) {
                // true positive
                Event e = new Event(pointsToSet.get(topAlarm), 1);
                evidence.put(topAlarm, e);
                i.addEvidence(e);
            } else {
                // false positive
                Event e = new Event(pointsToSet.get(topAlarm), 0);
                evidence.put(topAlarm, e);
                i.addEvidence(e);
            }
            // remove the inspected alarm
            alarmProbabilities.remove(topAlarm);

            //re-calculate probabilities of remaining alarms by inference
            System.out.println("Recalculating...");
            alarmProbabilities.replaceAll((alarm, prob) -> i.infer(new Assignment(List.of(new Event(pointsToSet.get(alarm), 1)))));
            //print new ranking (optional)
            alarmProbabilities.forEach((key, value) -> System.out.println(key.getTerms() + " prob: " + value));
        }

        System.out.println("Ranking done");
        return null;

    }
}
