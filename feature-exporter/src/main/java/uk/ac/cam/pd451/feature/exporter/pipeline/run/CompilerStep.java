package uk.ac.cam.pd451.feature.exporter.pipeline.run;

import uk.ac.cam.pd451.feature.exporter.pipeline.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Responsible for taking a directory of .java files, compiling them with the feature extractor and outputing the target directory.
 */
public class CompilerStep implements Step<CompilerStep.CompilerPipeInput, String> {

    @Override
    public String process(CompilerStep.CompilerPipeInput input) throws PipeException {
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(new String[]{"sh","-c","javac -cp /Users/padr/repos/features-javac/extractor/target/features-javac-extractor-1.0.0-SNAPSHOT-jar-with-dependencies.jar -Xplugin:FeaturePlugin $(find " + input.inputDirectory.toString() + " -name '*.java') -XDfeaturesOutputDirectory=" + input.outputDirectory.toString()},
                    null, null);

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

        return input.outputDirectory;
    }

    public static class CompilerPipeInput {
        private String inputDirectory;
        private String outputDirectory;

        public CompilerPipeInput(String inputDirectory, String outputDirectory) {
            this.inputDirectory = inputDirectory;
            this.outputDirectory = outputDirectory;
        }

        public String getInputDirectory() {
            return inputDirectory;
        }

        public void setInputDirectory(String inputDirectory) {
            this.inputDirectory = inputDirectory;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }
    }
}
