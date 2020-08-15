package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Analysis.IntensityCalculator;
import wbif.sjx.common.MathFunc.CumStat;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class MeasureImageIntensity extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public MeasureImageIntensity(ModuleCollection modules) {
        super("Measure image intensity",modules);
    }


    public interface Measurements {
        String MEAN = "INTENSITY // MEAN";
        String MIN = "INTENSITY // MIN";
        String MAX = "INTENSITY // MAX";
        String SUM = "INTENSITY // SUM";
        String STDEV = "INTENSITY // STDEV";

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        writeStatus("Loading image ("+inputImageName+")");
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running measurement
        CumStat cs = IntensityCalculator.calculate(inputImagePlus.getImageStack());

        // Adding measurements to image
        inputImage.addMeasurement(new Measurement(Measurements.MEAN, cs.getMean()));
        inputImage.addMeasurement(new Measurement(Measurements.MIN, cs.getMin()));
        inputImage.addMeasurement(new Measurement(Measurements.MAX, cs.getMax()));
        inputImage.addMeasurement(new Measurement(Measurements.STDEV, cs.getStd(CumStat.SAMPLE)));
        inputImage.addMeasurement(new Measurement(Measurements.SUM, cs.getSum()));

        if (showOutput) inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        ImageMeasurementRef mean = imageMeasurementRefs.getOrPut(Measurements.MEAN);
        mean.setImageName(inputImageName);
        mean.setDescription("Mean intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(mean);

        ImageMeasurementRef min = imageMeasurementRefs.getOrPut(Measurements.MIN);
        min.setImageName(inputImageName);
        min.setDescription("Minimum intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(min);

        ImageMeasurementRef max = imageMeasurementRefs.getOrPut(Measurements.MAX);
        max.setImageName(inputImageName);
        max.setDescription("Maximum intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(max);

        ImageMeasurementRef stdev = imageMeasurementRefs.getOrPut(Measurements.STDEV);
        stdev.setImageName(inputImageName);
        stdev.setDescription("Standard deviation of intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");        
        returnedRefs.add(stdev);

        ImageMeasurementRef sum = imageMeasurementRefs.getOrPut(Measurements.SUM);
        sum.setImageName(inputImageName);
        sum.setDescription("Summed intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(sum);

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
