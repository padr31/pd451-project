package uk.ac.cam.pd451.feature.exporter.pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import uk.ac.cam.pd451.feature.exporter.datalog.*;
import uk.ac.cam.pd451.feature.exporter.pipeline.io.EmptyIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsible for taking a directory of .java files, compiling them with the feature extractor and outputing the target directory.
 */
public class ProvenanceImportStep implements Step<EmptyIO, EmptyIO> {

    @Override
    public EmptyIO process(EmptyIO input) throws PipeException {
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(new String[]{"sh","-c","stack exec -- vanillalog run -f ~/repos/pd451-project/analysis/andersen-analysis.datalog --keep-all-predicates --json --provenance > /Users/padr/repos/pd451-project/feature-exporter/out_provenance/provenance.json"},
                    null, new File("/Users/padr/repos/pd451-project/vanillalog"));

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // Read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // Read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            throw new PipeException(e);
        }
        return new EmptyIO();
    }
}