package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import fiji.threshold.Auto_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
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

    private static final String HUANG = "Huang";
    private static final String OTSU = "Otsu";
    private static final String[] THRESHOLD_MODES = new String[]{HUANG,OTSU};

    @Override
    public String getTitle() {
        return "Threshold image";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
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

        if (thresholdMode.equals(HUANG)) {
            if (verbose) System.out.println("["+moduleName+"] Applying global Huang threshold (multplier = "+thrMult+" x)");
            results1 = auto_threshold.exec(inputImagePlus,"Huang",true,false,true,true,false,true);

        } else if (thresholdMode.equals(OTSU)) {
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

        if (!applyToInput) {
            HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
            HCImage outputImage = new HCImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(APPLY_TO_INPUT,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(THRESHOLD_MODE,HCParameter.CHOICE_ARRAY,THRESHOLD_MODES[0],THRESHOLD_MODES));
        parameters.addParameter(new HCParameter(THRESHOLD_MULTIPLIER, HCParameter.DOUBLE,1.0));
        parameters.addParameter(new HCParameter(WHITE_BACKGROUND,HCParameter.BOOLEAN,true));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MODE));
        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MULTIPLIER));
        returnedParameters.addParameter(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
