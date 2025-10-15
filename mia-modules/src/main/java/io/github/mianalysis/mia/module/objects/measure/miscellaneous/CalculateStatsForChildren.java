package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import java.util.LinkedHashMap;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
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
 * Calculates statistics for a measurement associated with all child objects of
 * parent object. The calculated statistics are stored as new measurements,
 * associated with the relevant parent object. For example, calculating the
 * summed volume of all child objects (from a specified collection) of each
 * parent object.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CalculateStatsForChildren extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Objects input";

    /**
     * Input object collection from the workspace for which statistics of child
     * object measurements will be calculated. This object collection is a parent to
     * those selected by the "Child objects" parameter. Statistics for one
     * measurement associated with all children of each input parent object will be
     * calculated and added to this object as a new measurement.
     */
    public static final String PARENT_OBJECTS = "Parent objects";

    /**
     * Input object collection from the workspace, where these objects are children
     * of the collection selected by the "Parent objects" parameter.)
     */
    public static final String CHILD_OBJECTS = "Child objects";

    /**
    * 
    */
    public static final String STATISTIC_SEPARATOR = "Statistics";

    /**
     * 
     */
    public static final String MEASUREMENT_SEPARATOR = "Measurements";

    /**
    * 
    */
    public static final String ADD_MEASUREMENT = "Add measurement";

    /**
     * Measurement associated with the child objects for which statistics will be
     * calculated. Statistics will be calculated for all children of a parent
     * object.
     */
    public static final String MEASUREMENT = "Measurement";

    /**
     * When selected, the mean value of the measurements will be calculated and
     * added to the relevant parent object.
     */
    public static final String CALCULATE_MEAN = "Calculate mean";

    /**
     * When selected, the median value of the measurements will be calculated and
     * added to the relevant parent object.
     */
    public static final String CALCULATE_MEDIAN = "Calculate median";

    /**
     * When selected, the standard deviation of the measurements will be calculated
     * and added to the relevant parent object.
     */
    public static final String CALCULATE_STD = "Calculate standard deviation";

    /**
     * When selected, the minimum value of the measurements will be calculated and
     * added to the relevant parent object.
     */
    public static final String CALCULATE_MIN = "Calculate minimum";

    /**
     * When selected, the maximum value of the measurements will be calculated and
     * added to the relevant parent object.
     */
    public static final String CALCULATE_MAX = "Calculate maximum";

    /**
     * When selected, the sum of the measurements will be calculated and added to
     * the relevant parent object.
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
        return "CHILD_STATS // " + childObjectName + " // " + measurementType + " // [" + measurement + "]";
    }

    public static void processObject(ObjI parentObject, String childObjectsName, String measurement,
            boolean[] statsToCalculate) {
        if (statsToCalculate[0] || statsToCalculate[1] || statsToCalculate[2] || statsToCalculate[3]
                || statsToCalculate[4]) {
            ObjsI childObjects = parentObject.getChildren(childObjectsName);

            // Calculating statistics for measurement
            CumStat cs = new CumStat();
            if (childObjects != null) {
                for (ObjI childObject : childObjects.values()) {
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

    public static double calculateMedian(ObjI parentObject, String childObjectsName, String measurement) {
        double[] values = new double[parentObject.getChildren(childObjectsName).size()];
        int i = 0;
        for (ObjI childObj : parentObject.getChildren(childObjectsName).values())
            values[i++] = childObj.getMeasurement(measurement).getValue();

        return new Median().evaluate(values);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Calculates statistics for a measurement associated with all child objects of parent object.  The calculated statistics are stored as new measurements, associated with the relevant parent object.  For example, calculating the summed volume of all child objects (from a specified collection) of each parent object.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_MEASUREMENT, workspace);
        boolean[] statsToCalculate = new boolean[6];
        statsToCalculate[0] = parameters.getValue(CALCULATE_MEAN, workspace);
        statsToCalculate[1] = parameters.getValue(CALCULATE_STD, workspace);
        statsToCalculate[2] = parameters.getValue(CALCULATE_MIN, workspace);
        statsToCalculate[3] = parameters.getValue(CALCULATE_MAX, workspace);
        statsToCalculate[4] = parameters.getValue(CALCULATE_SUM, workspace);
        statsToCalculate[5] = parameters.getValue(CALCULATE_MEDIAN, workspace);

        // Getting objects
        ObjsI parentObjects = workspace.getObjects(parentObjectsName);

        int count = 0;
        int total = parentObjects.size();
        for (ObjI parentObject : parentObjects.values()) {
            for (Parameters collection : collections.values()) {
                String measurement = collection.getValue(MEASUREMENT, workspace);
                processObject(parentObject, childObjectsName, measurement, statsToCalculate);
            }

            writeProgressStatus(++count, total, "objects");

        }

        if (showOutput)
            parentObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ParameterGroup(ADD_MEASUREMENT, this, collection));

        parameters.add(new SeparatorP(STATISTIC_SEPARATOR, this));
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
        WorkspaceI workspace = null;

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));

        String objectName = parameters.getValue(PARENT_OBJECTS, workspace);
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(objectName);

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_MEASUREMENT));

        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        for (Parameters collection : parameterGroup.getCollections(true).values())
            ((ObjectMeasurementP) collection.getParameter(MEASUREMENT)).setObjectName(childObjectsName);

        returnedParameters.add(parameters.getParameter(STATISTIC_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATE_MAX));
        returnedParameters.add(parameters.getParameter(CALCULATE_MEAN));
        returnedParameters.add(parameters.getParameter(CALCULATE_MEDIAN));
        returnedParameters.add(parameters.getParameter(CALCULATE_MIN));
        returnedParameters.add(parameters.getParameter(CALCULATE_STD));
        returnedParameters.add(parameters.getParameter(CALCULATE_SUM));

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

        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);

        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        for (Parameters collection : parameterGroup.getCollections(true).values()) {
            String measurementName = collection.getValue(MEASUREMENT, workspace);

            if ((boolean) parameters.getValue(CALCULATE_MEAN, workspace)) {
                String name = getFullName(childObjectsName, measurementName, Measurements.MEAN);
                ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(parentObjectsName);
                reference.setDescription(
                        "Mean value of measurement, \"" + measurementName + "\", for child objects, \"" +
                                childObjectsName + "\".");
                returnedRefs.add(reference);
            }

            if ((boolean) parameters.getValue(CALCULATE_MEDIAN, workspace)) {
                String name = getFullName(childObjectsName, measurementName, Measurements.MEDIAN);
                ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(parentObjectsName);
                reference.setDescription(
                        "Median value of measurement, \"" + measurementName + "\", for child objects, \"" +
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
                        .setDescription(
                                "Minimum value of measurement, \"" + measurementName + "\", for child objects, \"" +
                                        childObjectsName + "\".");
                returnedRefs.add(reference);
            }

            if ((boolean) parameters.getValue(CALCULATE_MAX, workspace)) {
                String name = getFullName(childObjectsName, measurementName, Measurements.MAX);
                ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(parentObjectsName);
                reference
                        .setDescription(
                                "Maximum value of measurement, \"" + measurementName + "\", for child objects, \"" +
                                        childObjectsName + "\".");
                returnedRefs.add(reference);
            }

            if ((boolean) parameters.getValue(CALCULATE_SUM, workspace)) {
                String name = getFullName(childObjectsName, measurementName, Measurements.SUM);
                ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(parentObjectsName);
                reference.setDescription(
                        "Summed value of measurement, \"" + measurementName + "\", for child objects, \"" +
                                childObjectsName + "\".");
                returnedRefs.add(reference);
            }
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
        parameters.get(PARENT_OBJECTS).setDescription(
                "Input object collection from the workspace for which statistics of child object measurements will be calculated.  This object collection is a parent to those selected by the \""
                        + CHILD_OBJECTS
                        + "\" parameter.  Statistics for one measurement associated with all children of each input parent object will be calculated and added to this object as a new measurement.");

        parameters.get(CHILD_OBJECTS).setDescription(
                "Input object collection from the workspace, where these objects are children of the collection selected by the \""
                        + PARENT_OBJECTS + "\" parameter.)");

        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        for (Parameters collection : parameterGroup.getCollections(true).values())
            collection.get(MEASUREMENT).setDescription(
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
