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
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";

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
        if (parameters.getValue(MEASURE_MEAN))
            inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_INTENSITY, cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MIN_INTENSITY, cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MAX_INTENSITY, cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.STD_INTENSITY, cs.getStd(CumStat.SAMPLE)));
        if (parameters.getValue(MEASURE_SUM))
            inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.SUM_INTENSITY, cs.getSum()));

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_SUM, Parameter.BOOLEAN, true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        if (parameters.getValue(MEASURE_MEAN))
            measurements.addMeasurement(inputImageName,MIAMeasurement.MEAN_INTENSITY);

        if (parameters.getValue(MEASURE_MIN))
            measurements.addMeasurement(inputImageName,MIAMeasurement.MIN_INTENSITY);

        if (parameters.getValue(MEASURE_MAX))
            measurements.addMeasurement(inputImageName,MIAMeasurement.MAX_INTENSITY);

        if (parameters.getValue(MEASURE_STDEV))
            measurements.addMeasurement(inputImageName,MIAMeasurement.STD_INTENSITY);

        if (parameters.getValue(MEASURE_SUM))
            measurements.addMeasurement(inputImageName,MIAMeasurement.SUM_INTENSITY);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
