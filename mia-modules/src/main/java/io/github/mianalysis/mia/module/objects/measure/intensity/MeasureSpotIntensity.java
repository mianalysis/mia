package io.github.mianalysis.mia.module.objects.measure.intensity;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.objects.process.GetLocalObjectRegion;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
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
 * Similar to MeasureObjectIntensity, but performed on circular (or spherical)
 * regions of interest around each point in 3D. Allows the user to specify the
 * region around each point to be measured. Intensity traces are stored as
 * HCMultiMeasurements
 */

/**
* DEPRECATED: Please use separate "Get local object region" and "Measure object intensity" modules.<br><br>Measures the intensity of an image for a circular (2D object*) or spherical (3D object*) region coincident with the mean centroid of each object in a specified object collection.  Measurements are associated with the corresponding input objects.  The radius of the measurement region can be specified as a fixed value or determined on an object-by-object basis from associated object (or parent) measurements.<br><br>Note: This module differs from the "Measure object intensity" module, which measures the intensity of all coordinates of an object.<br><br>* 2D objects are defined as objects identified from a single-slice image.  Objects with coordinates confined to a single plane, but identified from a 3D image stack are still considered 3D objects.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MeasureSpotIntensity extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object and image input";

	/**
	* Image from the workspace to measure the intensity of.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Object collection from the workspace for which spot intensities will be measured.  One spot will be measured for each object.
	*/
    public static final String INPUT_OBJECTS = "Input spot objects";


	/**
	* 
	*/
    public static final String SPOT_SEPARATOR = "Spot size control";

	/**
	* Controls how the radius of the spot is defined:<br><ul><li>"Fixed value" A single radius, defined by "Fixed value" will be used for all objects.</li><li>"Measurement" The radius will be equal to the value of a measurement (specified by "Radius measurement") associated with the object being measured.  Radii will potentially be different for each object.</li><li>"Parent measurement" The radius will be equal to the value of a measurement (specified by "Parent radius measurement") associated a parent of the object being measured (specified by "Parent object").  Radii will potentially be different for each object..</li></ul>
	*/
    public static final String RADIUS_SOURCE = "Radius value source";

	/**
	* Fixed spot radius to use for all object measurements when "Radius value source" is in "Fixed value" mode.
	*/
    public static final String FIXED_VALUE = "Fixed value";

	/**
	* Measurement associated with the input object.  This will be used as spot the radius for spot intensity measurements when "Radius value source" is in "Measurement" mode.
	*/
    public static final String RADIUS_MEASUREMENT = "Radius measurement";

	/**
	* Parent object of the input object being measured.  This parent will provide the measurement (specified by "Parent radius measurement") to be used as the spot radius for spot intensity measurements when "Radius value source" is in "Parent measurement" mode.
	*/
    public static final String PARENT_OBJECT = "Parent object";

	/**
	* Measurement associated with a parent of the input object.  This will be used as the spot radius for spot intensity measurements when "Radius value source" is in "Parent measurement" mode.
	*/
    public static final String PARENT_RADIUS_MEASUREMENT = "Parent radius measurement";

	/**
	* When selected, spot radius values (irrespective of whether they are fixed values, measurements or parent measurements) are assumed to be specified in calibrated units (as defined by the "Input control" parameter "Spatial unit").  Otherwise, pixel units are assumed.
	*/
    public static final String CALIBRATED_UNITS = "Calibrated units";


	/**
	* 
	*/
    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";

	/**
	* When selected, the mean intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.
	*/
    public static final String MEASURE_MEAN = "Measure mean";

	/**
	* When selected, the standard deviation of intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.
	*/
    public static final String MEASURE_STDEV = "Measure standard deviation";

	/**
	* When selected, the minimum intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.
	*/
    public static final String MEASURE_MIN = "Measure minimum";

	/**
	* When selected, the maximum intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.
	*/
    public static final String MEASURE_MAX = "Measure maximum";

	/**
	* When selected, the summed intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.
	*/
    public static final String MEASURE_SUM = "Measure sum";

    public interface RadiusSources extends GetLocalObjectRegion.RadiusSources {
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String STDEV = "STDEV";
        String SUM = "SUM";

    }

    public static String getFullName(String imageName, String measurement) {
        return "SPOT_INTENSITY // " + imageName + "_" + measurement;
    }

    public MeasureSpotIntensity(Modules modules) {
        super("Measure spot intensity", modules);
        deprecated = true;
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
        return "DEPRECATED: Please use separate \"" + new GetLocalObjectRegion(null).getName() + "\" and \""
                + new MeasureObjectIntensity(null).getName() + "\" modules."

                + "<br><br>Measures the intensity of an image for a circular (2D object*) or spherical (3D object*) region coincident with the mean centroid of each object in a specified object collection.  Measurements are associated with the corresponding input objects.  The radius of the measurement region can be specified as a fixed value or determined on an object-by-object basis from associated object (or parent) measurements."

                + "<br><br>Note: This module differs from the \"" + new MeasureObjectIntensity(null).getName()
                + "\" module, which measures the intensity of all coordinates of an object."

                + "<br><br>* 2D objects are defined as objects identified from a single-slice image.  Objects with coordinates confined to a single plane, but identified from a 3D image stack are still considered 3D objects.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting image to measure spot intensity for
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        double radius = parameters.getValue(FIXED_VALUE,workspace);
        boolean calibrated = parameters.getValue(CALIBRATED_UNITS,workspace);
        String radiusSource = parameters.getValue(RADIUS_SOURCE,workspace);
        String radiusMeasurement = parameters.getValue(RADIUS_MEASUREMENT,workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT,workspace);
        String parentRadiusMeasurement = parameters.getValue(PARENT_RADIUS_MEASUREMENT,workspace);

        // Checking if there are any objects to measure
        if (inputObjects.size() == 0) {
            for (Obj inputObject : inputObjects.values()) {
                if ((boolean) parameters.getValue(MEASURE_MEAN,workspace))
                    inputObject.getParent(inputObjectsName).addMeasurement(
                            new Measurement(getFullName(inputImageName, Measurements.MEAN), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_MIN,workspace))
                    inputObject.getParent(inputObjectsName)
                            .addMeasurement(new Measurement(getFullName(inputImageName, Measurements.MIN), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_MAX,workspace))
                    inputObject.getParent(inputObjectsName)
                            .addMeasurement(new Measurement(getFullName(inputImageName, Measurements.MAX), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_STDEV,workspace))
                    inputObject.getParent(inputObjectsName).addMeasurement(
                            new Measurement(getFullName(inputImageName, Measurements.STDEV), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_SUM,workspace))
                    inputObject.getParent(inputObjectsName)
                            .addMeasurement(new Measurement(getFullName(inputImageName, Measurements.SUM), Double.NaN));

            }

            return Status.PASS;

        }

        Objs tempObjects = new Objs("Temp", inputObjects);
        for (Obj inputObject : inputObjects.values()) {
            switch (radiusSource) {
                case RadiusSources.MEASUREMENT:
                    radius = inputObject.getMeasurement(radiusMeasurement).getValue();
                    break;
                case RadiusSources.PARENT_MEASUREMENT:
                    Obj parentObject = inputObject.getParent(parentObjectsName);
                    if (parentObject == null)
                        radius = Double.NaN;
                    else
                        radius = parentObject.getMeasurement(parentRadiusMeasurement).getValue();
                    break;
            }

            double xPosition = inputObject.getXMean(true);
            double yPosition = inputObject.getYMean(true);
            double zPosition = inputObject.getZMean(true, false);
            int[] centroid = new int[] { (int) Math.round(xPosition), (int) Math.round(yPosition),
                    (int) Math.round(zPosition) };
            Obj spotObject = GetLocalObjectRegion.getLocalRegion(inputObject, tempObjects, centroid, (int) Math.round(radius), false);

            CumStat cs = new CumStat();

            // Running through all pixels in this object and adding the intensity to the
            // MultiCumStat object
            Integer t = spotObject.getT();
            for (Point<Integer> point : spotObject.getCoordinateSet()) {
                ipl.setPosition(1, point.z + 1, t + 1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(point.x, point.y));
            }

            if ((boolean) parameters.getValue(MEASURE_MEAN,workspace))
                inputObject
                        .addMeasurement(new Measurement(getFullName(inputImageName, Measurements.MEAN), cs.getMean()));
            if ((boolean) parameters.getValue(MEASURE_MIN,workspace))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName, Measurements.MIN), cs.getMin()));
            if ((boolean) parameters.getValue(MEASURE_MAX,workspace))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName, Measurements.MAX), cs.getMax()));
            if ((boolean) parameters.getValue(MEASURE_STDEV,workspace))
                inputObject.addMeasurement(
                        new Measurement(getFullName(inputImageName, Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
            if ((boolean) parameters.getValue(MEASURE_SUM,workspace))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName, Measurements.SUM), cs.getSum()));

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        parameters.add(new SeparatorP(SPOT_SEPARATOR, this));
        parameters.add(new ChoiceP(RADIUS_SOURCE, this, RadiusSources.FIXED_VALUE, RadiusSources.ALL));
        parameters.add(new DoubleP(FIXED_VALUE, this, 2.0));
        parameters.add(new ObjectMeasurementP(RADIUS_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(PARENT_RADIUS_MEASUREMENT, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(MEASURE_MEAN, this, true));
        parameters.add(new BooleanP(MEASURE_MIN, this, true));
        parameters.add(new BooleanP(MEASURE_MAX, this, true));
        parameters.add(new BooleanP(MEASURE_STDEV, this, true));
        parameters.add(new BooleanP(MEASURE_SUM, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT,workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(SPOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RADIUS_SOURCE));
        switch ((String) parameters.getValue(RADIUS_SOURCE,workspace)) {
            case RadiusSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(FIXED_VALUE));
                break;

            case RadiusSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT)).setObjectName(inputObjectsName);
                break;

            case RadiusSources.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
                returnedParameters.add(parameters.getParameter(PARENT_RADIUS_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(PARENT_RADIUS_MEASUREMENT))
                        .setObjectName(parentObjectsName);
                break;
        }
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_MEAN));
        returnedParameters.add(parameters.getParameter(MEASURE_MIN));
        returnedParameters.add(parameters.getParameter(MEASURE_MAX));
        returnedParameters.add(parameters.getParameter(MEASURE_STDEV));
        returnedParameters.add(parameters.getParameter(MEASURE_SUM));

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

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);

        if ((boolean) parameters.getValue(MEASURE_MEAN,workspace)) {
            String name = getFullName(inputImageName, Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MIN,workspace)) {
            String name = getFullName(inputImageName, Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MAX,workspace)) {
            String name = getFullName(inputImageName, Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_STDEV,workspace)) {
            String name = getFullName(inputImageName, Measurements.STDEV);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_SUM,workspace)) {
            String name = getFullName(inputImageName, Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
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
        parameters.get(INPUT_IMAGE).setDescription("Image from the workspace to measure the intensity of.");

        parameters.get(INPUT_OBJECTS).setDescription(
                "Object collection from the workspace for which spot intensities will be measured.  One spot will be measured for each object.");

        parameters.get(RADIUS_SOURCE).setDescription("Controls how the radius of the spot is defined:<br><ul>"

                + "<li>\"" + RadiusSources.FIXED_VALUE + "\" A single radius, defined by \"" + FIXED_VALUE
                + "\" will be used for all objects.</li>"

                + "<li>\"" + RadiusSources.MEASUREMENT
                + "\" The radius will be equal to the value of a measurement (specified by \"" + RADIUS_MEASUREMENT
                + "\") associated with the object being measured.  Radii will potentially be different for each object.</li>"

                + "<li>\"" + RadiusSources.PARENT_MEASUREMENT
                + "\" The radius will be equal to the value of a measurement (specified by \""
                + PARENT_RADIUS_MEASUREMENT + "\") associated a parent of the object being measured (specified by \""
                + PARENT_OBJECT + "\").  Radii will potentially be different for each object..</li></ul>");

        parameters.get(FIXED_VALUE).setDescription("Fixed spot radius to use for all object measurements when \""
                + RADIUS_SOURCE + "\" is in \"" + RadiusSources.FIXED_VALUE + "\" mode.");

        parameters.get(RADIUS_MEASUREMENT).setDescription(
                "Measurement associated with the input object.  This will be used as spot the radius for spot intensity measurements when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.MEASUREMENT + "\" mode.");

        parameters.get(PARENT_OBJECT).setDescription(
                "Parent object of the input object being measured.  This parent will provide the measurement (specified by \""
                        + PARENT_RADIUS_MEASUREMENT
                        + "\") to be used as the spot radius for spot intensity measurements when \"" + RADIUS_SOURCE
                        + "\" is in \"" + RadiusSources.PARENT_MEASUREMENT + "\" mode.");

        parameters.get(PARENT_RADIUS_MEASUREMENT).setDescription(
                "Measurement associated with a parent of the input object.  This will be used as the spot radius for spot intensity measurements when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.PARENT_MEASUREMENT + "\" mode.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, spot radius values (irrespective of whether they are fixed values, measurements or parent measurements) are assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

        parameters.get(MEASURE_MEAN).setDescription(
                "When selected, the mean intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.");

        parameters.get(MEASURE_STDEV).setDescription(
                "When selected, the standard deviation of intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.");

        parameters.get(MEASURE_MIN).setDescription(
                "When selected, the minimum intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.");

        parameters.get(MEASURE_MAX).setDescription(
                "When selected, the maximum intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.");

        parameters.get(MEASURE_SUM).setDescription(
                "When selected, the summed intensity of all coordinates in the spot is calculated and stored as a measurement associated with the input object.");

    }
}
