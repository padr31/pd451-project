package uk.ac.cam.pd451.feature.exporter.pipeline.io;

/**
 * The IO Object used to store the source folder that is input to the main pipeline.
 * The output directory is where ranking results are written.
 */
public class PipelineDirectoryIO {
    private String inputDirectory;
    private String outputDirectory;

    public PipelineDirectoryIO(String inputDirectory, String outputDirectory) {
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
