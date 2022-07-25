// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.sjcommon.mathfunc.CumStat;
import io.github.sjcross.sjcommon.object.Point;

/**
 * Created by sc13967 on 05/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MeasureObjectIntensity extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";

    public static final String WEIGHTED_CENTRE_SEPARATOR = "Weighted centre";
    public static final String MEASURE_WEIGHTED_CENTRE = "Measure weighted centre";

    public MeasureObjectIntensity(Modules modules) {
        super("Measure object intensity", modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
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

        String MEAN_EDGE_DISTANCE_PX = "MEAN_EDGE_DISTANCE (PX)";
        String MEAN_EDGE_DISTANCE_CAL = "MEAN_EDGE_DISTANCE (${SCAL})";
        String STD_EDGE_DISTANCE_PX = "STD_EDGE_DISTANCE (PX)";
        String STD_EDGE_DISTANCE_CAL = "STD_EDGE_DISTANCE (${SCAL})";

        String EDGE_PROFILE = "EDGE_PROFILE";

    }

    public static String getFullName(String imageName, String measurement) {
        return "INTENSITY // " + imageName + "_" + measurement;
    }

    public static CumStat measureIntensity(Obj object, Image image, boolean addMeasurements) {
        // Getting parameters
        String imageName = image.getName();

        // Running through all pixels in this object and adding the intensity to the
        // MultiCumStat object
        int t = object.getT();
        CumStat cs = new CumStat();

        ImagePlus ipl = image.getImagePlus();
        for (Point<Integer> point : object.getCoordinateSet()) {
            ipl.setPosition(1, point.getZ() + 1, t + 1);
            float value = ipl.getProcessor().getf(point.getX(), point.getY());
            cs.addMeasure(value);
        }

        // Calculating mean, std, min and max intensity
        if (addMeasurements) {
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN), cs.getMean()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MIN), cs.getMin()));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MAX), cs.getMax()));
            object.addMeasurement(
                    new Measurement(getFullName(imageName, Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
            object.addMeasurement(new Measurement(getFullName(imageName, Measurements.SUM), cs.getSum()));
        }

        return cs;

    }

    public static CumStat[] measureWeightedCentre(Obj object, Image image, boolean addMeasurements) {
        // Getting parameters
        String imageName = image.getName();

        ImagePlus ipl = image.getImagePlus();

        // Initialising the cumulative statistics objects to store pixel intensities in
        // each direction.
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getXCoords();
        ArrayList<Integer> y = object.getYCoords();
        ArrayList<Integer> z = object.getZCoords();
        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the
        // MultiCumStat object
        for (int i = 0; i < x.size(); i++) {
            ipl.setPosition(1, z.get(i) + 1, tPos + 1);
            csX.addMeasure(x.get(i), ipl.getProcessor().getPixelValue(x.get(i), y.get(i)));
            csY.addMeasure(y.get(i), ipl.getProcessor().getPixelValue(x.get(i), y.get(i)));
            csZ.addMeasure(z.get(i), ipl.getProcessor().getPixelValue(x.get(i), y.get(i)));

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

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Measure intensity of each object in a specified image.  Measurements of intensity are taken at all pixel coordinates corresponding to each object.  By default, basic measurements such as mean, minimum and maximum will be calculated.  Additional measurements can optionally be enabled.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(imageName);

        // Measuring intensity for each object and adding the measurement to that object
        int count = 0;
        int total = objects.size();
        for (Obj object : objects.values()) {
            measureIntensity(object, inputImage,true);

            // If specified, measuring weighted centre for intensity
            if ((boolean) parameters.getValue(MEASURE_WEIGHTED_CENTRE,workspace))
                measureWeightedCentre(object, inputImage, true);

            writeProgressStatus(++count, total, "objects");

        }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(WEIGHTED_CENTRE_SEPARATOR, this));
        parameters.add(new BooleanP(MEASURE_WEIGHTED_CENTRE, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(WEIGHTED_CENTRE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_WEIGHTED_CENTRE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);

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

        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_CENTRE,workspace)) {
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

        return returnedRefs;

    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
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

        parameters.get(MEASURE_WEIGHTED_CENTRE).setDescription(
                "When selected, the intensity-weighted centroid of each input object will be calculated.  With this, the greater the intensity in a particular region of an object, the more the \"centre of mass\" will be drawn towards it.");

    }
}
