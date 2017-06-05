package wbif.sjx.ModularImageAnalysis.Module;

import ij.ImagePlus;
import wbif.sjx.common.Analysis.IntensityCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class MeasureImageIntensity extends HCModule {
    public static final String INPUT_IMAGE = "Input image";

    @Override
    public String getTitle() {
        return "Measure image intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+inputImageName+")");
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running measurement
        CumStat cs = IntensityCalculator.calculate(inputImagePlus);

        // Adding measurements to image
        HCMeasurement meanIntensity = new HCMeasurement(HCMeasurement.MEAN_INTENSITY,cs.getMean()[0]);
        meanIntensity.setSource(this);
        inputImage.addMeasurement(meanIntensity.getName(),meanIntensity);
        if (verbose) System.out.println("["+moduleName+"] Mean intensity = "+meanIntensity.getValue());

        HCMeasurement stdIntensity = new HCMeasurement(HCMeasurement.STD_INTENSITY,cs.getStd(CumStat.SAMPLE)[0]);
        stdIntensity.setSource(this);
        inputImage.addMeasurement(stdIntensity.getName(),stdIntensity);
        if (verbose) System.out.println("["+moduleName+"] Std intensity (sample) = "+stdIntensity.getValue());

        HCMeasurement minIntensity = new HCMeasurement(HCMeasurement.MIN_INTENSITY,cs.getMin()[0]);
        minIntensity.setSource(this);
        inputImage.addMeasurement(minIntensity.getName(),minIntensity);
        if (verbose) System.out.println("["+moduleName+"] Min intensity = "+minIntensity.getValue());

        HCMeasurement maxIntensity = new HCMeasurement(HCMeasurement.MAX_INTENSITY,cs.getMax()[0]);
        maxIntensity.setSource(this);
        inputImage.addMeasurement(maxIntensity.getName(),maxIntensity);
        if (verbose) System.out.println("["+moduleName+"] Max intensity = "+maxIntensity.getValue());

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE, HCParameter.INPUT_IMAGE,null));

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
