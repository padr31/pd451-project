package uk.ac.cam.pd451.feature.exporter.pipeline.run;

import uk.ac.cam.pd451.feature.exporter.pipeline.Step;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.InspectedPredicate;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.RankingStatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class RankingProcessorStep implements Step<RankingStatistics, EmptyIO> {

    @Override
    public EmptyIO process(RankingStatistics input) throws PipeException {
        File dir = new File("out_ranking");
        if(!dir.exists()) dir.mkdir();

        File inspectedPredicatesCSV = new File(dir.getAbsolutePath() + File.separator + "inspected_predicates.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(inspectedPredicatesCSV, true))) {
            input.getInspectedPredicates().forEach(ip -> pw.println(ip.getCSVString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int[] index = {0};
        input.getOverallRanks().forEach(probs -> {
            File probsCSV = new File(dir.getAbsolutePath() + File.separator + index[0] + ".csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(probsCSV, true))) {
                probs.forEach((pred, prob) -> {
                    pw.println(pred.getTerms() + ";" + prob);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            index[0]++;
        });

        return null;
    }
}
