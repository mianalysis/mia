// TODO: Enable ability to extract multiple images from .lif files

package wbif.sjx.ModularImageAnalysis.Module;

import ij.ImagePlus;
import ij.io.Opener;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by sc13967 on 08/05/2017.
 */
public class BioformatsImageLoader extends HCModule {
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show loaded image";

    @Override
    public String getTitle() {
        return "Bio-formats image loader";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting image name
        HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Running Bio-formats importer
        if (verbose) System.out.println("["+moduleName+"] Loading image");

        // Bio-formats writes lots of unwanted information to System.out.  This diverts it to a fake PrintStream
        PrintStream realStream = System.out;
        PrintStream fakeStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
        });

        System.setOut(fakeStream);
        ImagePlus ipl = Opener.openUsingBioFormats(workspace.getMetadata().getFile().getAbsolutePath());
        System.setOut(realStream);

        if (ipl != null) {
            // Adding image to workspace
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName.getName()+") to workspace");
            workspace.addImage(new HCImage(outputImageName, ipl));

            // (If selected) displaying the loaded image
            boolean showImage = parameters.getValue(SHOW_IMAGE);
            if (showImage) {
                if (verbose) System.out.println("["+moduleName+"] Displaying loaded image");
                ipl.show();
            }

        } else {
            // Warning that no image loaded
            if (verbose) System.out.println("["+moduleName+"] Image ("+outputImageName.getName()+") failed to load");

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE, HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(SHOW_IMAGE, HCParameter.BOOLEAN,false));

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
