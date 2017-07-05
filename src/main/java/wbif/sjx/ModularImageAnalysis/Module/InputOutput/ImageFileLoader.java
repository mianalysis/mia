package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class ImageFileLoader extends HCModule {
    public static final String IMPORT_MODE = "Import mode";
    public static final String FILE_PATH = "File path";
    public static final String USE_BIOFORMATS = "Use Bio-formats importer";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    private static final String CURRENT_FILE = "Current file";
    private static final String SPECIFIC_FILE = "Specific file";
    private static final String[] IMPORT_MODES = new String[]{CURRENT_FILE,SPECIFIC_FILE};


    @Override
    public String getTitle() {
        return "Load image from file";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) throws GenericMIAException {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting parameters
        String importMode = parameters.getValue(IMPORT_MODE);
        String filePath = parameters.getValue(FILE_PATH);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // If the file currently in the workspace is to be used, update the file path accordingly
        if (importMode.equals(CURRENT_FILE)) {
            if (workspace.getMetadata().getFile() == null) throw new GenericMIAException("Load file using Analysis > Set file to analyse");
            filePath = workspace.getMetadata().getFile().getAbsolutePath();
        }

        ImagePlus ipl;
        // Importing the file
        if (parameters.getValue(USE_BIOFORMATS)) {
            // Bio-formats writes lots of unwanted information to System.out.  This diverts it to a fake PrintStream
            PrintStream realStream = System.out;
            PrintStream fakeStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            });

            System.setOut(fakeStream);
            ipl = Opener.openUsingBioFormats(filePath);
            System.out.println(ipl.getNChannels());
            System.setOut(realStream);

        } else {
            ipl = IJ.openImage(filePath);

        }

        // If the image is an RGB, converting to composite
        ipl = new CompositeImage(ipl);

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        workspace.addImage(new HCImage(outputImageName,ipl));

        // Displaying the image (the image is duplicated, so it doesn't get deleted if the window is closed)
        if (parameters.getValue(SHOW_IMAGE)) {
            ipl = new Duplicator().run(ipl);
            ipl.show();
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(IMPORT_MODE,HCParameter.CHOICE_ARRAY,IMPORT_MODES[0],IMPORT_MODES));
        parameters.addParameter(new HCParameter(FILE_PATH,HCParameter.FILE_PATH,null));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(USE_BIOFORMATS,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(SHOW_IMAGE,HCParameter.BOOLEAN,false));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(IMPORT_MODE));
        if (parameters.getValue(IMPORT_MODE).equals(SPECIFIC_FILE)) {
            returnedParameters.addParameter(parameters.getParameter(FILE_PATH));

        }

        returnedParameters.addParameter(parameters.getParameter(USE_BIOFORMATS));
        returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}