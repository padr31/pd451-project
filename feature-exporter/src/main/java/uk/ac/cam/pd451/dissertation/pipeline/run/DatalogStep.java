package uk.ac.cam.pd451.dissertation.pipeline.run;

import uk.ac.cam.pd451.dissertation.analysis.Relation;
import uk.ac.cam.pd451.dissertation.pipeline.Step;
import uk.ac.cam.pd451.dissertation.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.dissertation.utils.Props;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Responsible for taking a directory of .csv relations,
 * running a datalog analysis on them using exalog,
 * and outputting the provenance graph of the analysis
 * into the output directory.
 */
public class DatalogStep implements Step<List<Relation>, EmptyIO> {

    @Override
    public EmptyIO process(List<Relation> input) throws PipeException {
        Runtime rt = Runtime.getRuntime();
        try {
            String vanillalogPath = Props.get("vanillalogPath");
            String analysisPath = Props.get("analysisPath");
            String provenanceOutputFolder = Props.get("provenanceOutputFolder");

            File provenanceFolder = new File(provenanceOutputFolder);
            if(!provenanceFolder.exists()) provenanceFolder.mkdir();

            Process proc = rt.exec(new String[]{"sh","-c","stack exec -- vanillalog run -f " + analysisPath + " --keep-all-predicates --json --provenance > " + provenanceFolder.getAbsolutePath() + "/provenance.json"},
                    null, new File(vanillalogPath));

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
