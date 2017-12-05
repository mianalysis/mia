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

    private Reference inputImage;

    private MeasurementReference meanMeasurement;
    private MeasurementReference minMeasurement;
    private MeasurementReference maxMeasurement;
    private MeasurementReference stdevMeasurement;
    private MeasurementReference sumMeasurement;

    private interface Measurements {
        String MEAN = "INTENSITY//MEAN";
        String MIN = "INTENSITY//MIN";
        String MAX = "INTENSITY//MAX";
        String SUM = "INTENSITY//SUM";
        String STDEV = "INTENSITY//STDEV";

    }


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
            inputImage.addMeasurement(new Measurement(Measurements.MEAN, cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            inputImage.addMeasurement(new Measurement(Measurements.MIN, cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            inputImage.addMeasurement(new Measurement(Measurements.MAX, cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            inputImage.addMeasurement(new Measurement(Measurements.STDEV, cs.getStd(CumStat.SAMPLE)));
        if (parameters.getValue(MEASURE_SUM))
            inputImage.addMeasurement(new Measurement(Measurements.SUM, cs.getSum()));

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
    public void initialiseReferences() {
        inputImage = new Reference();
        imageReferences.add(inputImage);

        meanMeasurement = new MeasurementReference(Measurements.MEAN);
        minMeasurement = new MeasurementReference(Measurements.MIN);
        maxMeasurement = new MeasurementReference(Measurements.MAX);
        stdevMeasurement = new MeasurementReference(Measurements.STDEV);
        sumMeasurement = new MeasurementReference(Measurements.SUM);
        inputImage.addMeasurementReference(meanMeasurement);
        inputImage.addMeasurementReference(minMeasurement);
        inputImage.addMeasurementReference(maxMeasurement);
        inputImage.addMeasurementReference(stdevMeasurement);
        inputImage.addMeasurementReference(sumMeasurement);

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        // Updating image name
        inputImage.setName(parameters.getValue(INPUT_IMAGE));

        // Updating measurements
        meanMeasurement.setCalculated(parameters.getValue(MEASURE_MEAN));
        minMeasurement.setCalculated(parameters.getValue(MEASURE_MIN));
        maxMeasurement.setCalculated(parameters.getValue(MEASURE_MAX));
        stdevMeasurement.setCalculated(parameters.getValue(MEASURE_STDEV));
        sumMeasurement.setCalculated(parameters.getValue(MEASURE_SUM));

        return imageReferences;

    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
