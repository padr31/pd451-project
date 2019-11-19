package uk.ac.cam.pd451.feature.exporter.neo4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResultParseUtils {

    public static String getMinDistanceMethodName(List<String>[] methodsWithDistances) {
        int minDistance = Integer.MAX_VALUE;
        String minMethod = "";
        for(List<String> methodDistancePair : methodsWithDistances) {
            String method = methodDistancePair.get(0);
            int distance = Integer.parseInt(methodDistancePair.get(1));
            if(distance < minDistance) {
                minDistance = distance;
                minMethod = method;
            }
        }
        return minMethod;
    }

    public static List<String> getOrderedArguments(List<String>[] arguments) {
        Arrays.sort(arguments, (arg1, arg2) -> {
            int lineDiff = Integer.parseInt(arg1.get(1)) - Integer.parseInt(arg2.get(1));
            int charDiff = Integer.parseInt(arg1.get(2)) - Integer.parseInt(arg2.get(2));
            if(lineDiff != 0) return lineDiff;
            else return charDiff;
        });
        return Arrays.stream(arguments).map(arg -> arg.get(0)).collect(Collectors.toList());
    }
}
