package uk.ac.cam.pd451.feature.exporter.pipeline.eval;

import uk.ac.cam.pd451.feature.exporter.datalog.Predicate;
import uk.ac.cam.pd451.feature.exporter.datalog.ProvenanceGraph;
import uk.ac.cam.pd451.feature.exporter.inference.BayessianGibbsSamplingInference;
import uk.ac.cam.pd451.feature.exporter.inference.EvaluableLoopyPropagationInference;
import uk.ac.cam.pd451.feature.exporter.inference.Event;
import uk.ac.cam.pd451.feature.exporter.inference.LoopyPropagationInference;
import uk.ac.cam.pd451.feature.exporter.inference.variable.Variable;
import uk.ac.cam.pd451.feature.exporter.pipeline.Step;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.RankingStatistics;

import java.util.*;
import java.util.stream.Collectors;

public class InferenceEvalStep implements Step<ProvenanceGraph, RankingStatistics> {
    @Override
    public RankingStatistics process(ProvenanceGraph g) throws PipeException {
        //collect varPointsTo variables

        System.out.println("Collecting nullPointer variables");
        Map<Predicate, Variable> pointsToSet = g.getPredicateToNode()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals("nullPointer"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getVariable()));

        System.out.println("Initialising the Gibbs Sampling inference algorithm");

        /*
        BayessianGibbsSamplingInference gibbsReference = new BayessianGibbsSamplingInference();
        gibbsReference.setModel(g.getBayesianNetwork());
        gibbsReference.setIterations(100000);

        // insert all alarms with their prior probabilities
        System.out.println("Inferring prior probabilities with Gibbs Sampling");
        List<Event> inferredEvents = pointsToSet.values().stream().map(v -> new Event(v, 1)).collect(Collectors.toList());
        Map<Event, Double> gibbsReferenceProbs = gibbsReference.infer(inferredEvents);

        BayessianGibbsSamplingInference gibbs = new BayessianGibbsSamplingInference();
        gibbs.setModel(g.getBayesianNetwork());
        int steps = 1;
        while(steps <= 50000){
            steps = steps*2;
            gibbs.setIterations(steps);
            Map<Event, Double> gibbsProbs = gibbs.infer(inferredEvents);
            System.out.println("Steps: " + steps + " KL: " + klDistance(gibbsReferenceProbs, gibbsProbs) + " MSE: " + meanSquaredDistance(gibbsReferenceProbs, gibbsProbs));
        }
        */
        List<Event> eventsToInfer = pointsToSet.values().stream().map(v -> new Event(v, 1)).collect(Collectors.toList());

        BayessianGibbsSamplingInference gibbs = new BayessianGibbsSamplingInference();
        gibbs.setModel(g.getBayesianNetwork());

        System.out.println("Initialising loopy belief propagation");

        EvaluableLoopyPropagationInference loopy = new EvaluableLoopyPropagationInference(100000, 100, eventsToInfer);
        loopy.setModel(g.getBayesianNetwork());

        Map<Integer, Map<Event, Double>> loopyInferred = loopy.getResult();
        Map<Event, Double> loopyReference = loopyInferred.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getKey)).get().getValue();

        Map<Integer, Map<Event, Double>> gibbsInferred = gibbs.inferUpToFactorMultiplications(eventsToInfer, 100000, 100);
        Map<Event, Double> gibbsReference = gibbsInferred.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getKey)).get().getValue();

        System.out.println("Gibbs diff");

        for(int fact : gibbsInferred.keySet().stream().sorted().collect(Collectors.toList())) {
            System.out.println(fact + "," + meanSquaredDistance(gibbsReference, gibbsInferred.get(fact)));
        }

        System.out.println("Loopy diff");
        for(int fact : loopyInferred.keySet().stream().sorted().collect(Collectors.toList())) {
            System.out.println(fact + "," + meanSquaredDistance(loopyReference, loopyInferred.get(fact)));
        }

        System.out.println("Evaluation done");


        return null;

    }

    double klDistance(Map<Event, Double> distribution1, Map<Event, Double> distribution2) {
        double kl = 0;
        for(Map.Entry<Event, Double> e : distribution1.entrySet()) {
            kl += e.getValue()*Math.log(e.getValue()/distribution2.get(e.getKey()));
        }
        return kl;
    }

    double meanSquaredDistance(Map<Event, Double> distribution1, Map<Event, Double> distribution2) {
        double msd = 0;
        for(Map.Entry<Event, Double> e : distribution1.entrySet()) {
            msd += Math.pow(e.getValue() - distribution2.get(e.getKey()), 2);
        }
        msd = msd/distribution1.size();
        return msd;
    }

    double l1Distance(Map<Event, Double> distribution1, Map<Event, Double> distribution2) {
        double msd = 0;
        for(Map.Entry<Event, Double> e : distribution1.entrySet()) {
            msd += Math.abs(e.getValue() - distribution2.get(e.getKey()));
        }
        msd = msd/distribution1.size();
        return msd;
    }
}
