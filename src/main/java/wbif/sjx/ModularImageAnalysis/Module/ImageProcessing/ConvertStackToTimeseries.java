package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import fiji.stacks.Hyperstack_rearranger;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 19/06/2017.
 */
public class ConvertStackToTimeseries extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    @Override
    public String getTitle() {
        return "Convert stack to timeseries";
    }

    @Override
    public String getHelp() {
        return "Checks if there is only 1 frame, but multiple Z-sections.  " +
                "In this case, the Z and T ordering will be switched";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        int nChannels = inputImagePlus.getNChannels();
        int nFrames = inputImagePlus.getNFrames();
        int nSlices = inputImagePlus.getNSlices();

        if (inputImagePlus.getNSlices() == 1 || inputImagePlus.getNFrames() > 1) return;

        ImagePlus processedImagePlus = HyperStackConverter.toHyperStack(inputImagePlus,nChannels,nFrames,nSlices);
        processedImagePlus = Hyperstack_rearranger.reorderHyperstack(processedImagePlus,"CTZ",true,false);
        inputImagePlus.setStack(processedImagePlus.getStack());

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));

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
