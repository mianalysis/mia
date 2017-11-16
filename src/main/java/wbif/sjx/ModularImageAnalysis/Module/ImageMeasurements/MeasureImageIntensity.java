package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
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
    public void run(Workspace workspace, boolean verbose) {
       // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+inputImageName+")");
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running measurement
        CumStat cs = IntensityCalculator.calculate(inputImagePlus);

        // Adding measurements to image
        MIAMeasurement meanIntensity = new MIAMeasurement(MIAMeasurement.MEAN_INTENSITY,cs.getMean());
        meanIntensity.setSource(this);
        inputImage.addMeasurement(meanIntensity.getName(),meanIntensity);
        if (verbose) System.out.println("["+moduleName+"] Mean intensity = "+meanIntensity.getValue());

        MIAMeasurement stdIntensity = new MIAMeasurement(MIAMeasurement.STD_INTENSITY,cs.getStd(CumStat.SAMPLE));
        stdIntensity.setSource(this);
        inputImage.addMeasurement(stdIntensity.getName(),stdIntensity);
        if (verbose) System.out.println("["+moduleName+"] Std intensity (sample) = "+stdIntensity.getValue());

        MIAMeasurement minIntensity = new MIAMeasurement(MIAMeasurement.MIN_INTENSITY,cs.getMin());
        minIntensity.setSource(this);
        inputImage.addMeasurement(minIntensity.getName(),minIntensity);
        if (verbose) System.out.println("["+moduleName+"] Min intensity = "+minIntensity.getValue());

        MIAMeasurement maxIntensity = new MIAMeasurement(MIAMeasurement.MAX_INTENSITY,cs.getMax());
        maxIntensity.setSource(this);
        inputImage.addMeasurement(maxIntensity.getName(),maxIntensity);
        if (verbose) System.out.println("["+moduleName+"] Max intensity = "+maxIntensity.getValue());

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        measurements.addMeasurement(inputImageName,"MEAN_I");
        measurements.addMeasurement(inputImageName,"STD_I");
        measurements.addMeasurement(inputImageName,"MIN_I");
        measurements.addMeasurement(inputImageName,"MAX_I");

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
