package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 07/03/2018.
 */
public class ExtendedMinima extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY_3D = "Connectivity (3D)";
    public static final String SHOW_IMAGE = "Show image";


    @Override
    public String getTitle() {
        return "Extended minima";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int dynamic = parameters.getValue(DYNAMIC);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY_3D));

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Getting region minima
        inputImagePlus.setStack(MinimaAndMaxima3D.extendedMinima(inputImagePlus.getImageStack(),dynamic,connectivity));
        inputImagePlus.setPosition(1,1,1);

        // MorphoLibJ gives white objects on a black background.  Inverting this to match the logic of ImageJ
        IJ.run(inputImagePlus,"Invert","stack");

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.show();
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(DYNAMIC, Parameter.INTEGER,1));
        parameters.add(new Parameter(CONNECTIVITY_3D, Parameter.CHOICE_ARRAY, BinaryOperations.Connectivity3D.SIX, BinaryOperations.Connectivity3D.ALL));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(DYNAMIC));
        returnedParameters.add(parameters.getParameter(CONNECTIVITY_3D));
        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

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
