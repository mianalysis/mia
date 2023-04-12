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
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.mathfunc.CumStat;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CalculateStatsForPartners extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PARTNER_OBJECTS = "Partner objects";

    public static final String STATISTIC_SEPARATOR = "Statistics";
    public static final String MEASUREMENT = "Measurement";
    public static final String CALCULATE_MEAN = "Calculate mean";
    public static final String CALCULATE_MEDIAN = "Calculate median";
    public static final String CALCULATE_STD = "Calculate standard deviation";
    public static final String CALCULATE_MIN = "Calculate minimum";
    public static final String CALCULATE_MAX = "Calculate maximum";
    public static final String CALCULATE_SUM = "Calculate sum";

    public CalculateStatsForPartners(Modules modules) {
        super("Calculate statistics for partners", modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MEDIAN = "MEDIAN";
        String STD = "STD";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";

    }

    public static String getFullName(String partnerObjectName, String measurement, String measurementType) {
        return "PARTNER_STATS // " + measurementType + "_" + partnerObjectName + "_\"" + measurement + "\"";
    }

    public static void processObject(Obj inputObject, String partnerObjectsName, String measurement,
            boolean[] statsToCalculate) {
        if (statsToCalculate[0] || statsToCalculate[1] || statsToCalculate[2] || statsToCalculate[3]
                || statsToCalculate[4]) {
            Objs partnerObjects = inputObject.getPartners(partnerObjectsName);

            // Calculating statistics for measurement
            CumStat cs = new CumStat();
            if (partnerObjects != null) {
                for (Obj partnerObject : partnerObjects.values()) {
                    // Check the measurement exists
                    if (partnerObject.getMeasurement(measurement) == null)
                        continue;

                    if (partnerObject.getMeasurement(measurement).getValue() != Double.NaN) {
                        cs.addMeasure(partnerObject.getMeasurement(measurement).getValue());
                    }
                }
            }

            if (statsToCalculate[0]) {
                String name = getFullName(partnerObjectsName, measurement, Measurements.MEAN);
                inputObject.addMeasurement(new Measurement(name, cs.getMean()));
            }

            if (statsToCalculate[1]) {
                String name = getFullName(partnerObjectsName, measurement, Measurements.STD);
                inputObject.addMeasurement(new Measurement(name, cs.getStd()));
            }

            if (statsToCalculate[2]) {
                String name = getFullName(partnerObjectsName, measurement, Measurements.MIN);
                inputObject.addMeasurement(new Measurement(name, cs.getMin()));
            }

            if (statsToCalculate[3]) {
                String name = getFullName(partnerObjectsName, measurement, Measurements.MAX);
                inputObject.addMeasurement(new Measurement(name, cs.getMax()));
            }

            if (statsToCalculate[4]) {
                String name = getFullName(partnerObjectsName, measurement, Measurements.SUM);
                inputObject.addMeasurement(new Measurement(name, cs.getSum()));
            }
        }

        if (statsToCalculate[5]) {
            double median = calculateMedian(inputObject, partnerObjectsName, measurement);
            String name = getFullName(partnerObjectsName, measurement,
                    Measurements.MEDIAN);
            inputObject.addMeasurement(new Measurement(name, median));
        }
    }

    public static double calculateMedian(Obj inputObject, String partnerObjectsName, String measurement) {
        double[] values = new double[inputObject.getPartners(partnerObjectsName).size()];
        int i = 0;
        for (Obj partnerObj : inputObject.getPartners(partnerObjectsName).values())
            values[i++] = partnerObj.getMeasurement(measurement).getValue();

        return new Median().evaluate(values);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Calculates statistics for a measurement associated with all partner objects of an input object.  The calculated statistics are stored as new measurements, associated with the relevant input object.  For example, calculating the summed volume of all partner objects (from a specified collection) of each input object.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting other parameters
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS, workspace);
        String measurement = parameters.getValue(MEASUREMENT, workspace);
        boolean[] statsToCalculate = new boolean[5];
        statsToCalculate[0] = parameters.getValue(CALCULATE_MEAN, workspace);
        statsToCalculate[1] = parameters.getValue(CALCULATE_STD, workspace);
        statsToCalculate[2] = parameters.getValue(CALCULATE_MIN, workspace);
        statsToCalculate[3] = parameters.getValue(CALCULATE_MAX, workspace);
        statsToCalculate[4] = parameters.getValue(CALCULATE_SUM, workspace);
        statsToCalculate[5] = parameters.getValue(CALCULATE_MEDIAN, workspace);

        for (Obj inputObject : inputObjects.values()) {
            processObject(inputObject, partnerObjectsName, measurement, statsToCalculate);
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS, this));

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
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS)).setPartnerObjectsName(objectName);

        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS, workspace);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(partnerObjectsName);

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

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS, workspace);
        String measurementName = parameters.getValue(MEASUREMENT, workspace);

        if ((boolean) parameters.getValue(CALCULATE_MEAN, workspace)) {
            String name = getFullName(partnerObjectsName, measurementName, Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean value of measurement, \"" + measurementName + "\", for partner objects, \"" +
                    partnerObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MEDIAN, workspace)) {
            String name = getFullName(partnerObjectsName, measurementName, Measurements.MEDIAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Median value of measurement, \"" + measurementName + "\", for partner objects, \"" +
                            partnerObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_STD, workspace)) {
            String name = getFullName(partnerObjectsName, measurementName, Measurements.STD);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Standard deviation of measurement, \"" + measurementName + "\", for partner objects, \"" +
                            partnerObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MIN, workspace)) {
            String name = getFullName(partnerObjectsName, measurementName, Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Minimum value of measurement, \"" + measurementName + "\", for partner objects, \"" +
                            partnerObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_MAX, workspace)) {
            String name = getFullName(partnerObjectsName, measurementName, Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Maximum value of measurement, \"" + measurementName + "\", for partner objects, \"" +
                            partnerObjectsName + "\".");
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(CALCULATE_SUM, workspace)) {
            String name = getFullName(partnerObjectsName, measurementName, Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Summed value of measurement, \"" + measurementName + "\", for partner objects, \"" +
                            partnerObjectsName + "\".");
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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Input object collection from the workspace for which statistics of partner object measurements will be calculated.  This object collection is a partner to those selected by the \""
                        + PARTNER_OBJECTS
                        + "\" parameter.  Statistics for one measurement associated with all partners of each input object will be calculated and added to this object as a new measurement.");

        parameters.get(PARTNER_OBJECTS).setDescription(
                "Input object collection from the workspace, where these objects are partners of the collection selected by the \""
                        + INPUT_OBJECTS + "\" parameter.)");

        parameters.get(MEASUREMENT).setDescription(
                "Measurement associated with the partner objects for which statistics will be calculated.  Statistics will be calculated for all partners of an input object.");

        parameters.get(CALCULATE_MEAN).setDescription(
                "When selected, the mean value of the measurements will be calculated and added to the relevant input object.");

        parameters.get(CALCULATE_MEDIAN).setDescription(
                "When selected, the median value of the measurements will be calculated and added to the relevant input object.");

        parameters.get(CALCULATE_STD).setDescription(
                "When selected, the standard deviation of the measurements will be calculated and added to the relevant input object.");

        parameters.get(CALCULATE_MIN).setDescription(
                "When selected, the minimum value of the measurements will be calculated and added to the relevant input object.");

        parameters.get(CALCULATE_MAX).setDescription(
                "When selected, the maximum value of the measurements will be calculated and added to the relevant input object.");

        parameters.get(CALCULATE_SUM).setDescription(
                "When selected, the sum of the measurements will be calculated and added to the relevant input object.");

    }
}
