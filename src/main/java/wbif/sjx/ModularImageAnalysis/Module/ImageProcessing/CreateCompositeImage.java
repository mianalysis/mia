package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 22/02/2018.
 */
public class CreateCompositeImage extends Module {
    public static final String USE_RED = "Use red channel";
    public static final String INPUT_IMAGE_RED = "Input image (red)";
    public static final String USE_GREEN = "Use green channel";
    public static final String INPUT_IMAGE_GREEN = "Input image (green)";
    public static final String USE_BLUE = "Use blue channel";
    public static final String INPUT_IMAGE_BLUE = "Input image (blue)";
    public static final String OUTPUT_IMAGE = "Output image";

    @Override
    public String getTitle() {
        return "Create composite image";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting parameters
        boolean useRed = parameters.getValue(USE_RED);
        String inputImageRedName = parameters.getValue(INPUT_IMAGE_RED);
        boolean useBlue = parameters.getValue(USE_BLUE);
        String inputImageRedName = parameters.getValue(INPUT_IMAGE_RED);
        boolean useRed = parameters.getValue(USE_RED);
        String inputImageRedName = parameters.getValue(INPUT_IMAGE_RED);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(USE_RED,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE_RED,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(USE_GREEN,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE_GREEN,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(USE_BLUE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE_BLUE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(USE_RED));
        if (parameters.getValue(USE_RED)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_RED));

        returnedParameters.add(parameters.getParameter(USE_GREEN));
        if (parameters.getValue(USE_GREEN)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_GREEN));

        returnedParameters.add(parameters.getParameter(USE_BLUE));
        if (parameters.getValue(USE_BLUE)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_BLUE));

        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        return returnedParameters;

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
