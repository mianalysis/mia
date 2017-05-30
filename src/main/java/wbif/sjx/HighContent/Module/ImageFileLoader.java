package wbif.sjx.HighContent.Module;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.plugin.RGBStackConverter;
import wbif.sjx.HighContent.Object.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class ImageFileLoader extends HCModule {
    public static final String FILE_PATH = "File path";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String USE_BIOFORMATS = "Use Bio-formats importer";
    public static final String SHOW_IMAGE = "Show image";


    @Override
    public String getTitle() {
        return "Load image from file";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input file
        String filePath = parameters.getValue(FILE_PATH);

        // Getting name to save
        HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Importing the file
        ImagePlus ipl;
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
            System.setOut(realStream);

        } else {
            ipl = IJ.openImage(filePath);

        }

        // If the image is an RGB, converting to composite
        ipl = new CompositeImage(ipl);

        // Displaying the image
        if (parameters.getValue(SHOW_IMAGE)) ipl.show();


        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        workspace.addImage(new HCImage(outputImageName,ipl));

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(FILE_PATH,HCParameter.FILE_PATH,null));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(USE_BIOFORMATS,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(SHOW_IMAGE,HCParameter.BOOLEAN,false));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
