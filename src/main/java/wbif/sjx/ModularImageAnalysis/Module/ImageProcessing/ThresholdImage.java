package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import fiji.threshold.Auto_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import org.apache.poi.ss.formula.functions.T;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class ThresholdImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String THRESHOLD_MODE = "Threshold mode";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";
    public static final String WHITE_BACKGROUND = "Black objects/white background";
    public static final String SHOW_IMAGE = "Show image";

    public interface ThresholdModes {
        String HUANG = "Huang";
        String OTSU = "Otsu";

        String[] ALL = new String[]{HUANG, OTSU};

    }


    @Override
    public String getTitle() {
        return "Threshold image";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String thresholdMode = parameters.getValue(THRESHOLD_MODE);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        IntensityMinMax.run(inputImagePlus,true);
        IJ.run(inputImagePlus,"8-bit",null);

        // Applying selected threshold
        Auto_Threshold auto_threshold = new Auto_Threshold();
        Object[] results1 = new Object[]{0};

        if (thresholdMode.equals(ThresholdModes.HUANG)) {
            if (verbose) System.out.println("["+moduleName+"] Applying global Huang threshold (multplier = "+thrMult+" x)");
            results1 = auto_threshold.exec(inputImagePlus,"Huang",true,false,true,true,false,true);

        } else if (thresholdMode.equals(ThresholdModes.OTSU)) {
            if (verbose) System.out.println("["+moduleName+"] Applying global Huang threshold (multplier = "+thrMult+" x)");
            results1 = auto_threshold.exec(inputImagePlus,"Otsu",true,false,true,true,false,true);

        }

        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    inputImagePlus.getProcessor().threshold((int) Math.round((int) results1[0]*thrMult));

                }
            }
        }

        if (whiteBackground) {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                    for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                        inputImagePlus.setPosition(c, z, t);
                        inputImagePlus.getProcessor().invert();
                    }
                }
            }
            inputImagePlus.setPosition(1,1,1);
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(outputImage.getImagePlus()).show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(inputImagePlus).show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(THRESHOLD_MODE, Parameter.CHOICE_ARRAY,ThresholdModes.HUANG,ThresholdModes.ALL));
        parameters.addParameter(new Parameter(THRESHOLD_MULTIPLIER, Parameter.DOUBLE,1.0));
        parameters.addParameter(new Parameter(WHITE_BACKGROUND, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MODE));
        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MULTIPLIER));
        returnedParameters.addParameter(parameters.getParameter(WHITE_BACKGROUND));
        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
