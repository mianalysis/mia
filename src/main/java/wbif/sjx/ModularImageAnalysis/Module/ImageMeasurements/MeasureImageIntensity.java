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
    public ParameterCollection initialiseParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        returnedParameters.addParameter(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        returnedParameters.addParameter(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        returnedParameters.addParameter(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        returnedParameters.addParameter(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        returnedParameters.addParameter(new Parameter(MEASURE_SUM, Parameter.BOOLEAN, true));

        return returnedParameters;

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    protected MeasurementReferenceCollection initialiseImageMeasurementReferences() {
        MeasurementReferenceCollection references = new MeasurementReferenceCollection();

        references.add(new MeasurementReference(Measurements.MEAN));
        references.add(new MeasurementReference(Measurements.MIN));
        references.add(new MeasurementReference(Measurements.MAX));
        references.add(new MeasurementReference(Measurements.STDEV));
        references.add(new MeasurementReference(Measurements.SUM));

        return references;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        MeasurementReference mean = imageMeasurementReferences.get(Measurements.MEAN);
        mean.setImageObjName(inputImageName);
        mean.setExportable(parameters.getValue(MEASURE_MEAN));

        MeasurementReference min = imageMeasurementReferences.get(Measurements.MIN);
        min.setImageObjName(inputImageName);
        min.setExportable(parameters.getValue(MEASURE_MIN));

        MeasurementReference max = imageMeasurementReferences.get(Measurements.MAX);
        max.setImageObjName(inputImageName);
        max.setExportable(parameters.getValue(MEASURE_MAX));

        MeasurementReference stdev = imageMeasurementReferences.get(Measurements.STDEV);
        stdev.setImageObjName(inputImageName);
        stdev.setExportable(parameters.getValue(MEASURE_STDEV));

        MeasurementReference sum = imageMeasurementReferences.get(Measurements.SUM);
        sum.setImageObjName(inputImageName);
        sum.setExportable(parameters.getValue(MEASURE_SUM));

        return imageMeasurementReferences;

    }

    @Override
    protected MeasurementReferenceCollection initialiseObjectMeasurementReferences() {
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
