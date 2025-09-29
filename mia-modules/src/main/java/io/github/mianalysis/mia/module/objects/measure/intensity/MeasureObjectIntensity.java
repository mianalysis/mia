// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package io.github.mianalysis.mia.module.objects.measure.intensity;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 05/05/2017.
 */

/**
 * Measure intensity of each object in a specified image. Measurements of
 * intensity are taken at all pixel coordinates corresponding to each object. By
 * default, basic measurements such as mean, minimum and maximum will be
 * calculated. Additional measurements can optionally be enabled.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectIntensity extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object and image input";

    /**
     * Objects from the workspace for which intensities will be measured.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Image from which pixel intensities will be measured. This image can be 8-bit,
     * 16-bit or 32-bit. Measurements are always taken from the first channel if
     * more than one channel is present (to measure additional channels, please
     * first use the "Extract substack" module).
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String OPTIONAL_MEASUREMENTS_SEPARATOR = "Optional measurements";

    /**
     * When selected, the median intensity of each input object will be calculated.
     * For very large objects, this can require a lot of memory.
     */
    public static final String MEASURE_MEDIAN = "Measure median intensity";

    /**
     * When selected, the intensity-weighted centroid of each input object will be
     * calculated. With this, the greater the intensity in a particular region of an
     * object, the more the "centre of mass" will be drawn towards it.
     */
    public static final String MEASURE_WEIGHTED_CENTRE = "Measure weighted centre";

    /**
     * When selected, the location of the brightest pixel in the object will be
     * calculated.
     * In instances where multiple pixels have the same brightest value, the mean of
     * their
     * positions will be used.
     */
    public static final String MEASURE_PEAK_LOCATION = "Measure peak brightness location";

    public MeasureObjectIntensity(Modules modules) {
        super("Measure object intensity", modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MEDIAN = "MEDIAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";
        String STDEV = "STDEV";

        String X_CENT_MEAN = "X_CENTRE_MEAN (PX)";
        String X_CENT_STDEV = "X_CENTRE_STDEV (PX)";
        String Y_CENT_MEAN = "Y_CENTRE_MEAN (PX)";
        String Y_CENT_STDEV = "Y_CENTRE_STDEV (PX)";
        String Z_CENT_MEAN = "Z_CENTRE_MEAN (SLICE)";
        String Z_CENT_STDEV = "Z_CENTRE_STDEV (SLICE)";

        String X_PEAK = "X_PEAK (PX)";
        String Y_PEAK = "Y_PEAK (PX)";
        String Z_PEAK = "Z_PEAK (SLICE)";
        
        String MEAN_EDGE_DISTANCE_PX = "MEAN_EDGE_DISTANCE (PX)";
        String MEAN_EDGE_DISTANCE_CAL = "MEAN_EDGE_DISTANCE (${SCAL})";
        String STD_EDGE_DISTANCE_PX = "STD_EDGE_DISTANCE (PX)";
        String STD_EDGE_DISTANCE_CAL = "STD_EDGE_DISTANCE (${SCAL})";

        String EDGE_PROFILE = "EDGE_PROFILE";

    }

    public static String getFullName(String imageName, String measurement) {
        return "INTENSITY // " + imageName + "_" + measurement;
    }

    public static CumStat measureIntensity(Obj object, ImageI image, boolean measureMedian, boolean addMeasurements) {
        // Getting parameters
        String imageName = image.getName();

        // Running through all pixels in this object and adding the intensity to the
        // MultiCumStat object
        int t = object.getT();
        CumStat cs = new CumStat();

        double[] vals = null;
        if (measureMedian)
            vals = new double[object.size()];

        int i = 0;
        ImagePlus ipl = image.getImagePlus();
        for (Point<Integer> point : object.getCoordinateSet()) {
            ipl.setPosition(1, point.getZ() + 1, t + 1);
            float value = ipl.getProcessor().getf(point.getX(), point.getY());
            cs.addMeasure(value);

            if (measureMedian)
                vals[i++] = value;
        }

        // Calculating mean, std, min and max intensity
        if (addMeasurements) {
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN), cs.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MIN), cs.getMin()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MAX), cs.getMax()));
            object.addMeasurement(
                    new Measurement(getFullName(imageName, Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.SUM), cs.getSum()));

            if (measureMedian)
                object.addMeasurement(
                        new Measurement(getFullName(imageName, Measurements.MEDIAN), new Median().evaluate(vals)));
        }

        return cs;

    }

    public static CumStat[] measureWeightedCentre(Obj object, ImageI image, boolean addMeasurements) {
        // Getting parameters
        String imageName = image.getName();

        ImagePlus ipl = image.getImagePlus();

        // Initialising the cumulative statistics objects to store pixel intensities in
        // each direction.
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the
        // MultiCumStat object
        for (Point<Integer> pt:object.getCoordinateSet()) {
            ipl.setPosition(1, pt.z + 1, tPos + 1);
            csX.addMeasure(pt.x, ipl.getProcessor().getPixelValue(pt.x, pt.y));
            csY.addMeasure(pt.y, ipl.getProcessor().getPixelValue(pt.x, pt.y));
            csZ.addMeasure(pt.z, ipl.getProcessor().getPixelValue(pt.x, pt.y));
        }

        if (addMeasurements) {
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.X_CENT_MEAN), csX.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.X_CENT_STDEV), csX.getStd()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.Y_CENT_MEAN), csY.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.Y_CENT_STDEV), csY.getStd()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.Z_CENT_MEAN), csZ.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.Z_CENT_STDEV), csZ.getStd()));
        }

        return new CumStat[] { csX, csY, csZ };

    }

    public static CumStat[] measurePeakLocation(Obj object, ImageI image, boolean addMeasurements) {
        // Getting parameters
        String imageName = image.getName();

        ImagePlus ipl = image.getImagePlus();

        // Iterating over each pixel, determining if this is the brightest pixel.  If it is, adding it's location to the relevant CumStat
        float maxValue = -Float.MAX_VALUE;
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();
        int tPos = object.getT();

        for (Point<Integer> pt:object.getCoordinateSet()) {
            ipl.setPosition(1, pt.getZ() + 1, tPos + 1);
            float currVal = ipl.getProcessor().getPixelValue(pt.getX(), pt.getY());

            if (currVal > maxValue) {
                maxValue = currVal;

                // Restarting the CumStats
                csX = new CumStat();
                csY = new CumStat();
                csZ = new CumStat();

                // Adding current position
                csX.addMeasure(pt.getX());
                csY.addMeasure(pt.getY());
                csZ.addMeasure(pt.getZ());

            } else if (currVal == maxValue) {
                // Adding current position
                csX.addMeasure(pt.getX());
                csY.addMeasure(pt.getY());
                csZ.addMeasure(pt.getZ());
            }
        }

        if (addMeasurements) {
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.X_PEAK), csX.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.Y_PEAK), csY.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.Z_PEAK), csZ.getMean()));
        }

        return new CumStat[] { csX, csY, csZ };

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measure intensity of each object in a specified image.  Measurements of intensity are taken at all pixel coordinates corresponding to each object.  By default, basic measurements such as mean, minimum and maximum will be calculated.  Additional measurements can optionally be enabled.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs objects = workspace.getObjects(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImages().get(imageName);

        boolean measureMedian = parameters.getValue(MEASURE_MEDIAN, workspace);
        boolean measureWeightedCentre = parameters.getValue(MEASURE_WEIGHTED_CENTRE, workspace);
        boolean measurePeakLocation = parameters.getValue(MEASURE_PEAK_LOCATION, workspace);

        // Measuring intensity for each object and adding the measurement to that object
        int count = 0;
        int total = objects.size();
        for (Obj object : objects.values()) {
            measureIntensity(object, inputImage, measureMedian, true);

            // If specified, measuring weighted centre for intensity
            if (measureWeightedCentre)
                measureWeightedCentre(object, inputImage, true);

            // If specified, measuring location of peak brightness
            if (measurePeakLocation)
                measurePeakLocation(object, inputImage, true);

            writeProgressStatus(++count, total, "objects");

        }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OPTIONAL_MEASUREMENTS_SEPARATOR, this));
        parameters.add(new BooleanP(MEASURE_MEDIAN, this, false));
        parameters.add(new BooleanP(MEASURE_WEIGHTED_CENTRE, this, false));
        parameters.add(new BooleanP(MEASURE_PEAK_LOCATION, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(OPTIONAL_MEASUREMENTS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_MEDIAN));
        returnedParameters.add(parameters.getParameter(MEASURE_WEIGHTED_CENTRE));
        returnedParameters.add(parameters.getParameter(MEASURE_PEAK_LOCATION));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        String name = getFullName(inputImageName, Measurements.MEAN);
        ObjMeasurementRef mean = objectMeasurementRefs.getOrPut(name);
        mean.setObjectsName(inputObjectsName);
        mean.setDescription("Mean intensity of pixels from the image \"" + inputImageName + "\" contained within each"
                + " \"" + inputObjectsName + "\" object");
        returnedRefs.add(mean);

        name = getFullName(inputImageName, Measurements.MIN);
        ObjMeasurementRef min = objectMeasurementRefs.getOrPut(name);
        min.setObjectsName(inputObjectsName);
        min.setDescription("Minimum intensity of pixels from the image \"" + inputImageName + "\" contained within each"
                + " \"" + inputObjectsName + "\" object");
        returnedRefs.add(min);

        name = getFullName(inputImageName, Measurements.MAX);
        ObjMeasurementRef max = objectMeasurementRefs.getOrPut(name);
        max.setObjectsName(inputObjectsName);
        max.setDescription("Maximum intensity of pixels from the image \"" + inputImageName + "\" contained within each"
                + " \"" + inputObjectsName + "\" object");
        returnedRefs.add(max);

        name = getFullName(inputImageName, Measurements.STDEV);
        ObjMeasurementRef stdev = objectMeasurementRefs.getOrPut(name);
        stdev.setObjectsName(inputObjectsName);
        stdev.setDescription("Standard deviation of intensity of pixels from the image \"" + inputImageName + "\" "
                + "contained within each \"" + inputObjectsName + "\" object");
        returnedRefs.add(stdev);

        name = getFullName(inputImageName, Measurements.SUM);
        ObjMeasurementRef sum = objectMeasurementRefs.getOrPut(name);
        sum.setObjectsName(inputObjectsName);
        sum.setDescription("Sum intensity of pixels from the image \"" + inputImageName + "\" contained within each"
                + " \"" + inputObjectsName + "\" object");
        returnedRefs.add(sum);

        if ((boolean) parameters.getValue(MEASURE_MEDIAN, workspace)) {
            name = getFullName(inputImageName, Measurements.MEDIAN);
            ObjMeasurementRef median = objectMeasurementRefs.getOrPut(name);
            median.setObjectsName(inputObjectsName);
            median.setDescription(
                    "Median intensity of pixels from the image \"" + inputImageName + "\" contained within each"
                            + " \"" + inputObjectsName + "\" object");
            returnedRefs.add(median);
        }

        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_CENTRE, workspace)) {
            name = getFullName(inputImageName, Measurements.X_CENT_MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity weighted x-position for each \"" + inputObjectsName + "\" object, "
                    + "with weighting coming from the image \"" + inputImageName + "\".  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.X_CENT_STDEV);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of intensity weighted x-position for each \""
                    + inputObjectsName + "\" object, with weighting coming from the image \"" + inputImageName + "\".  "
                    + "Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Y_CENT_MEAN);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity weighted y-position for each \"" + inputObjectsName + "\" object, "
                    + "with weighting coming from the image \"" + inputImageName + "\".  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Y_CENT_STDEV);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of intensity weighted y-position for each \""
                    + inputObjectsName + "\" object, with weighting coming from the image \"" + inputImageName + "\".  "
                    + "Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Z_CENT_MEAN);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity weighted z-position for each \"" + inputObjectsName + "\" object, "
                    + "with weighting coming from the image \"" + inputImageName + "\".  Measured in slice units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Z_CENT_STDEV);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of intensity weighted z-position for each \""
                    + inputObjectsName + "\" object, with weighting coming from the image \"" + inputImageName + "\".  "
                    + "Measured in slice units.");
            returnedRefs.add(reference);

        }

        if ((boolean) parameters.getValue(MEASURE_PEAK_LOCATION, workspace)) {
            name = getFullName(inputImageName, Measurements.X_PEAK);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("X-position of the brightest pixel for each \"" + inputObjectsName + "\" object.  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Y_PEAK);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Y-position of the brightest pixel for each \"" + inputObjectsName + "\" object.  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Z_PEAK);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Z-position of the brightest pixel for each \"" + inputObjectsName + "\" object.  Measured in slice units.");
            returnedRefs.add(reference);

        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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
        parameters.get(INPUT_OBJECTS)
                .setDescription("Objects from the workspace for which intensities will be measured.");

        parameters.get(INPUT_IMAGE).setDescription(
                "Image from which pixel intensities will be measured.  This image can be 8-bit, 16-bit or 32-bit.  Measurements are always taken from the first channel if more than one channel is present (to measure additional channels, please first use the \""
                        + new ExtractSubstack(null).getName() + "\" module).");

        parameters.get(MEASURE_MEDIAN).setDescription(
                "When selected, the median intensity of each input object will be calculated.  For very large objects, this can require a lot of memory");

        parameters.get(MEASURE_WEIGHTED_CENTRE).setDescription(
                "When selected, the intensity-weighted centroid of each input object will be calculated.  With this, the greater the intensity in a particular region of an object, the more the \"centre of mass\" will be drawn towards it.");

        parameters.get(MEASURE_PEAK_LOCATION).setDescription(
                "When selected, the location of the brightest pixel in the object will be calculated.  In instances where multiple pixels have the same brightest value, the mean of their positions will be used");

    }
}
