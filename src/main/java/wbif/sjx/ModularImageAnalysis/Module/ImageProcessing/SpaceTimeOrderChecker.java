package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.SwitchTAndZ;

/**
 * Created by sc13967 on 19/06/2017.
 */
public class SpaceTimeOrderChecker extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    @Override
    public String getTitle() {
        return "Space-time order checker";
    }

    @Override
    public String getHelp() {
        return "Checks if there is only 1 frame, but multiple Z-sections.  In this case, the Z and T ordering will be " +
                "switched";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        if (inputImagePlus.getNFrames() == 1 & inputImagePlus.getNSlices() > 1) SwitchTAndZ.run(inputImagePlus);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
