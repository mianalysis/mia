package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;
import wbif.sjx.common.Object.LUTs;

import java.util.HashMap;


/**
 * Created by sc13967 on 03/05/2017.
 */
public class ShowObjects extends Module {
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
    public void run(Workspace workspace, boolean verbose) {
        // Loading objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        Image templateImage;
        if (parameters.getParameter(TEMPLATE_IMAGE) == null) {
            templateImage = null;

        } else {
            String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
            templateImage = workspace.getImages().get(templateImageName);

        }

        // Converting objects to an image
        HashMap<Obj,Float> hues = inputObjects.getHue(ObjCollection.ColourModes.RANDOM_COLOUR,"","",false);
        Image image = inputObjects.convertObjectsToImage("Objects",templateImage.getImagePlus(),ObjectImageConverter.ColourModes.RANDOM_COLOUR,hues,false);
        image.getImagePlus().setTitle(inputObjectName);

        // Creating a random colour LUT and assigning it to the image (maximising intensity range to 0-255)
        image.getImagePlus().getProcessor().setLut(LUTs.Random(true));
        image.getImagePlus().getProcessor().setMinAndMax(0,255);

        // Showing the image
        image.getImagePlus().show();

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(TEMPLATE_IMAGE, Parameter.INPUT_IMAGE,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}