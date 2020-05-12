package uk.ac.cam.pd451.dissertation.pipeline.run;

import uk.ac.cam.pd451.dissertation.pipeline.Step;
import uk.ac.cam.pd451.dissertation.pipeline.io.RankingStatistics;
import uk.ac.cam.pd451.dissertation.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.dissertation.utils.Props;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This step outputs .csv files containing full ranking statistics such
 * as final ranks of alarms, their final probabilities, and ground truth.
 * A separate .csv is also outputted after each inspection in order to
 * capture how the ranking changes after each inspection.
 */
public class RankingProcessorStep implements Step<RankingStatistics, EmptyIO> {

    @Override
    public EmptyIO process(RankingStatistics input) throws PipeException {
        String rankingStatisticsOutputFolder = Props.get("rankingStatisticsOutputFolder");
        File dir = new File(rankingStatisticsOutputFolder);
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
