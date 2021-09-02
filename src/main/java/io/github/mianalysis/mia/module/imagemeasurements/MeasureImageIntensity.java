package io.github.mianalysis.mia.module.imagemeasurements;

import ij.ImagePlus;
import ij.process.StackStatistics;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class MeasureImageIntensity extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public MeasureImageIntensity(Modules modules) {
        super("Measure image intensity",modules);
    }


    public interface Measurements {
        String MEAN = "INTENSITY // MEAN";
        String MEDIAN = "INTENSITY // MEDIAN";
        String MODE = "INTENSITY // MODE";
        String MIN = "INTENSITY // MIN";
        String MAX = "INTENSITY // MAX";
        String SUM = "INTENSITY // SUM";
        String STDEV = "INTENSITY // STDEV";

    }



    @Override
    public Category getCategory() {
        return Categories.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "Measure intensity statistics (mean, median, mode, minimum, maximum, sum and standard deviation) for an image in the workspace.  Measurements are associated with the input image, so can be used later on or exported to the results spreadsheet.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        writeStatus("Loading image ("+inputImageName+")");
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running measurement
        StackStatistics statistics = new StackStatistics(inputImagePlus);
        
        // Adding measurements to image
        inputImage.addMeasurement(new Measurement(Measurements.MEAN, statistics.mean));
        inputImage.addMeasurement(new Measurement(Measurements.MEDIAN, statistics.median));
        inputImage.addMeasurement(new Measurement(Measurements.MODE, statistics.mode));
        inputImage.addMeasurement(new Measurement(Measurements.MIN, statistics.min));
        inputImage.addMeasurement(new Measurement(Measurements.MAX, statistics.max));
        inputImage.addMeasurement(new Measurement(Measurements.STDEV, statistics.stdDev));
        inputImage.addMeasurement(new Measurement(Measurements.SUM, statistics.mean * statistics.longPixelCount));

        if (showOutput) inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        ImageMeasurementRef mean = imageMeasurementRefs.getOrPut(Measurements.MEAN);
        mean.setImageName(inputImageName);
        mean.setDescription("Mean intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(mean);

        ImageMeasurementRef median = imageMeasurementRefs.getOrPut(Measurements.MEDIAN);
        median.setImageName(inputImageName);
        median.setDescription("Median intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(median);

        ImageMeasurementRef mode = imageMeasurementRefs.getOrPut(Measurements.MODE);
        mode.setImageName(inputImageName);
        mode.setDescription("Mode intensity of all pixels in the image \""+parameters.getValue(INPUT_IMAGE)+"\".");
        returnedRefs.add(mode);

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
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to measure intensity statistics for.  The resulting measurements will be associated with this image for use in subsequent modules.");

    }
}
