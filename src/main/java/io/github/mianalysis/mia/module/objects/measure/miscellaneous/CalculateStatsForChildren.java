package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.mathfunc.CumStat;


/**
* Calculates statistics for a measurement associated with all child objects of parent object.  The calculated statistics are stored as new measurements, associated with the relevant parent object.  For example, calculating the summed volume of all child objects (from a specified collection) of each parent object.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CalculateStatsForChildren extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Objects input";

	/**
	* Input object collection from the workspace for which statistics of child object measurements will be calculated.  This object collection is a parent to those selected by the "Child objects" parameter.  Statistics for one measurement associated with all children of each input parent object will be calculated and added to this object as a new measurement.
	*/
    public static final String PARENT_OBJECTS = "Parent objects";

	/**
	* Input object collection from the workspace, where these objects are children of the collection selected by the "Parent objects" parameter.)
	*/
    public static final String CHILD_OBJECTS = "Child objects";


	/**
	* 
	*/
    public static final String STATISTIC_SEPARATOR = "Statistics";

	/**
	* Measurement associated with the child objects for which statistics will be calculated.  Statistics will be calculated for all children of a parent object.
	*/
    public static final String MEASUREMENT = "Measurement";

	/**
	* When selected, the mean value of the measurements will be calculated and added to the relevant parent object.
	*/
    public static final String CALCULATE_MEAN = "Calculate mean";

	/**
	* When selected, the median value of the measurements will be calculated and added to the relevant parent object.
	*/
    public static final String CALCULATE_MEDIAN = "Calculate median";

	/**
	* When selected, the standard deviation of the measurements will be calculated and added to the relevant parent object.
	*/
    public static final String CALCULATE_STD = "Calculate standard deviation";

	/**
	* When selected, the minimum value of the measurements will be calculated and added to the relevant parent object.
	*/
    public static final String CALCULATE_MIN = "Calculate minimum";

	/**
	* When selected, the maximum value of the measurements will be calculated and added to the relevant parent object.
	*/
    public static final String CALCULATE_MAX = "Calculate maximum";

	/**
	* When selected, the sum of the measurements will be calculated and added to the relevant parent object.
	*/
    public static final String CALCULATE_SUM = "Calculate sum";

    public CalculateStatsForChildren(Modules modules) {
        super("Calculate statistics for children", modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MEDIAN = "MEDIAN";
        String STD = "STD";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";

    }

    public static String getFullName(String childObjectName, String measurement, String measurementType) {
        return "CHILD_STATS // " + measurementType + "_" + childObjectName + "_\"" + measurement + "\"";
    }

    public static void processObject(Obj parentObject, String childObjectsName, String measurement,
            boolean[] statsToCalculate) {
        if (statsToCalculate[0] || statsToCalculate[1] || statsToCalculate[2] || statsToCalculate[3]
                || statsToCalculate[4]) {
            Objs childObjects = parentObject.getChildren(childObjectsName);

            // Calculating statistics for measurement
            CumStat cs = new CumStat();
            if (childObjects != null) {
                for (Obj childObject : childObjects.values()) {
                    // Check the measurement exists
                    if (childObject.getMeasurement(measurement) == null)
                        continue;

                    if (childObject.getMeasurement(measurement).getValue() != Double.NaN) {
                        cs.addMeasure(childObject.getMeasurement(measurement).getValue());
                    }
                }
            }

            if (statsToCalculate[0]) {
                String name = getFullName(childObjectsName, measurement, Measurements.MEAN);
                parentObject.addMeasurement(new Measurement(name, cs.getMean()));
            }

            if (statsToCalculate[1]) {
                String name = getFullName(childObjectsName, measurement, Measurements.STD);
                parentObject.addMeasurement(new Measurement(name, cs.getStd()));
            }

            if (statsToCalculate[2]) {
                String name = getFullName(childObjectsName, measurement, Measurements.MIN);
                parentObject.addMeasurement(new Measurement(name, cs.getMin()));
            }

            if (statsToCalculate[3]) {
                String name = getFullName(childObjectsName, measurement, Measurements.MAX);
                parentObject.addMeasurement(new Measurement(name, cs.getMax()));
            }

            if (statsToCalculate[4]) {
                String name = getFullName(childObjectsName, measurement, Measurements.SUM);
                parentObject.addMeasurement(new Measurement(name, cs.getSum()));
            }
        }

        if (statsToCalculate[5]) {
            double median = calculateMedian(parentObject, childObjectsName, measurement);
            String name = getFullName(childObjectsName, measurement,
                    Measurements.MEDIAN);
            parentObject.addMeasurement(new Measurement(name, median));
        }
    }

    public static double calculateMedian(Obj parentObject, String childObjectsName, String measurement) {
        double[] values = new double[parentObject.getChildren(childObjectsName).size()];
        int i = 0;
        for (Obj childObj:parentObject.getChildren(childObjectsName).values())
            values[i++] = childObj.getMeasurement(measurement).getValue();
        
        return new Median().evaluate(values);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Calculates statistics for a measurement associated with all child objects of parent object.  The calculated statistics are stored as new measurements, associated with the relevant parent object.  For example, calculating the summed volume of all child objects (from a specified collection) of each parent object.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        Objs parentObjects = workspace.getObjects().get(parentObjectsName);

        // Getting other parameters
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        String measurement = parameters.getValue(MEASUREMENT, workspace);
        boolean[] statsToCalculate = new boolean[6];
        statsToCalculate[0] = parameters.getValue(CALCULATE_MEAN, workspace);
        statsToCalculate[1] = parameters.getValue(CALCULATE_STD, workspace);
        statsToCalculate[2] = parameters.getValue(CALCULATE_MIN, workspace);
        statsToCalculate[3] = parameters.getValue(CALCULATE_MAX, workspace);
        statsToCalculate[4] = parameters.getValue(CALCULATE_SUM, workspace);
        statsToCalculate[5] = parameters.getValue(CALCULATE_MEDIAN, workspace);

        for (Obj parentObject : parentObjects.values()) {
            processObject(parentObject, childObjectsName, measurement, statsToCalculate);
        }

        if (showOutput)
            parentObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        parameters.add(new SeparatorP(STATISTIC_SEPARATOR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new BooleanP(CALCULATE_MEAN, this, true));
        parameters.add(new BooleanP(CALCULATE_MEDIAN, this, false));
        parameters.add(new BooleanP(CALCULATE_STD, this, true));
        parameters.add(new BooleanP(CALCULATE_MIN, this, true));
        parameters.add(new BooleanP(CALCULATE_MAX, this, true));
        parameters.add(new BooleanP(CALCULATE_SUM, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        String objectName = parameters.getValue(PARENT_OBJECTS, workspace);
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(objectName);

        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(childObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        String measurementName = parameters.getValue(MEASUREMENT, workspace);

        if ((boolean) parameters.getValue(CALCULATE_MEAN, workspace)) {
            String name = getFullName(childObjectsName, measurementName, Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Mean value of measurement, \"" + measurementName + "\", for child objects, \"" +
                    childObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MEDIAN, workspace)) {
            String name = getFullName(childObjectsName, measurementName, Measurements.MEDIAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Median value of measurement, \"" + measurementName + "\", for child objects, \"" +
                    childObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_STD, workspace)) {
            String name = getFullName(childObjectsName, measurementName, Measurements.STD);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription(
                    "Standard deviation of measurement, \"" + measurementName + "\", for child objects, \"" +
                            childObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MIN, workspace)) {
            String name = getFullName(childObjectsName, measurementName, Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference
                    .setDescription("Minimum value of measurement, \"" + measurementName + "\", for child objects, \"" +
                            childObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MAX, workspace)) {
            String name = getFullName(childObjectsName, measurementName, Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference
                    .setDescription("Maximum value of measurement, \"" + measurementName + "\", for child objects, \"" +
                            childObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_SUM, workspace)) {
            String name = getFullName(childObjectsName, measurementName, Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(parentObjectsName);
            reference.setDescription("Summed value of measurement, \"" + measurementName + "\", for child objects, \"" +
                    childObjectsName + "\".");
            returnedRefs.add(reference);
        }

        return returnedRefs;

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
        parameters.get(PARENT_OBJECTS).setDescription(
                "Input object collection from the workspace for which statistics of child object measurements will be calculated.  This object collection is a parent to those selected by the \""
                        + CHILD_OBJECTS
                        + "\" parameter.  Statistics for one measurement associated with all children of each input parent object will be calculated and added to this object as a new measurement.");

        parameters.get(CHILD_OBJECTS).setDescription(
                "Input object collection from the workspace, where these objects are children of the collection selected by the \""
                        + PARENT_OBJECTS + "\" parameter.)");

        parameters.get(MEASUREMENT).setDescription(
                "Measurement associated with the child objects for which statistics will be calculated.  Statistics will be calculated for all children of a parent object.");

        parameters.get(CALCULATE_MEAN).setDescription(
                "When selected, the mean value of the measurements will be calculated and added to the relevant parent object.");

        parameters.get(CALCULATE_MEDIAN).setDescription(
                "When selected, the median value of the measurements will be calculated and added to the relevant parent object.");

        parameters.get(CALCULATE_STD).setDescription(
                "When selected, the standard deviation of the measurements will be calculated and added to the relevant parent object.");

        parameters.get(CALCULATE_MIN).setDescription(
                "When selected, the minimum value of the measurements will be calculated and added to the relevant parent object.");

        parameters.get(CALCULATE_MAX).setDescription(
                "When selected, the maximum value of the measurements will be calculated and added to the relevant parent object.");

        parameters.get(CALCULATE_SUM).setDescription(
                "When selected, the sum of the measurements will be calculated and added to the relevant parent object.");

    }
}
