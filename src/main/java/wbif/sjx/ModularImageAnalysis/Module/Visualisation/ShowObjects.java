package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.HCParameterCollection;
import wbif.sjx.common.Object.LUTs;


/**
 * Created by sc13967 on 03/05/2017.
 */
public class ShowObjects extends HCModule {
    public final static String INPUT_OBJECTS = "Input objects";
    public final static String TEMPLATE_IMAGE = "Template image";

    @Override
    public String getTitle() {
        return "Show objects";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Loading objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectName);

        HCImage templateImage;
        if (parameters.getParameter(TEMPLATE_IMAGE) == null) {
            templateImage = null;

        } else {
            String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
            templateImage = workspace.getImages().get(templateImageName);

        }

        // Converting objects to an image
        HCImage image = new ObjectImageConverter().convertObjectsToImage(inputObjects,"Object image",templateImage,ObjectImageConverter.COLOUR_MODES[1],null);
        image.getImagePlus().setTitle(inputObjectName);

        // Creating a random colour LUT and assigning it to the image (maximising intensity range to 0-255)
        image.getImagePlus().getProcessor().setLut(LUTs.Random(true));
        image.getImagePlus().getProcessor().setMinAndMax(0,255);

        // Showing the image
        image.getImagePlus().show();

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(TEMPLATE_IMAGE, HCParameter.INPUT_IMAGE,null));

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