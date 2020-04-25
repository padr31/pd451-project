package uk.ac.cam.pd451.feature.exporter.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSV {

    public static Map<String, Boolean> readRankCSV() {
        Map<String, Boolean> ranks = new HashMap<>();

        String csvFile = "out_ranking/inspected_predicates_previous.csv";
        String line;
        String csvSplitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] split = line.split(csvSplitBy);

                boolean isTrue = Integer.parseInt(split[3]) == 1;
                ranks.put(split[0], isTrue);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ranks;
    }

    public static Map<String, Double> readRuleProbabilitiesCSV() {
        Map<String, Double> ruleProbs = new HashMap<>();

        String csvFile = "out_learning/rule_probs.csv";
        String line;
        String csvSplitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] split = line.split(csvSplitBy);

                ruleProbs.put(split[0], Double.parseDouble(split[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ruleProbs;
    }
}
