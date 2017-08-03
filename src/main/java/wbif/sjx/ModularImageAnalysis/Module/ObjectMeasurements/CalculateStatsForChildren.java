package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.Iterator;
import java.util.Set;

/**
 * Runs through all the different children HCObjectSets assigned to an object and calculates statistics for all
 * measurements.  Eventually it would be good to offer the option which statistics will be processed.
 */
public class CalculateStatsForChildren extends HCModule {
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String CALCULATE_MEAN = "Calculate mean";
    public static final String CALCULATE_STD = "Calculate standard deviation";
    public static final String CALCULATE_MIN = "Calculate minimum";
    public static final String CALCULATE_MAX = "Calculate maximum";
    public static final String CALCULATE_SUM = "Calculate sum";


    @Override
    public String getTitle() {
        return "Calculate statistics for children";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        ObjSet parentObjects = workspace.getObjects().get(parentObjectsName);

        // Getting child objects to calculate statistics for
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);

        // Getting a list of the measurement names from the first child object in the set
        if (!parentObjects.values().iterator().hasNext()) return;

        Iterator<Obj> iterator = parentObjects.values().iterator();
        ObjSet children = iterator.next().getChildren(childObjectsName);
        while (children == null) {
            if (!iterator.hasNext()) {
                return;
            }
            children = iterator.next().getChildren(childObjectsName);
        }
        if (!children.values().iterator().hasNext()) return;

        Set<String> exampleMeasurements = children.values().iterator().next().getMeasurements().keySet();

        // Running through objects, calculating statistics for selected children
        for (Obj parentObject:parentObjects.values()) {
            ObjSet childObjects = parentObject.getChildren(childObjectsName);

            for (String measurement : exampleMeasurements) {
                // For each measurement type, calculating the mean, standard deviation, etc. (unless the value is NaN)
                CumStat cs = new CumStat();
                for (Obj childObject : childObjects.values()) {
                    if (childObject.getMeasurement(measurement).getValue() != Double.NaN) {
                        cs.addMeasure(childObject.getMeasurement(measurement).getValue());
                    }
                }

                // Checking at least one measurement was taken
                if (cs.getN() == 0) {
                    // Adding measurements to parent object
                    MIAMeasurement summaryMeasurement;

                    if (parameters.getValue(CALCULATE_MEAN)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_MEAN_OF_"+childObjectsName, Double.NaN);
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_STD)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_STD_OF_"+childObjectsName, Double.NaN);
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_MIN)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_MIN_OF_"+childObjectsName, Double.NaN);
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_MAX)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_MAX_OF_"+childObjectsName, Double.NaN);
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_SUM)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_SUM_OF_"+childObjectsName, Double.NaN);
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                } else {
                    // Adding measurements to parent object
                    MIAMeasurement summaryMeasurement;

                    if (parameters.getValue(CALCULATE_MEAN)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_MEAN_OF_"+childObjectsName, cs.getMean());
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_STD)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_STD_OF_"+childObjectsName, cs.getStd());
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_MIN)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_MIN_OF_"+childObjectsName, cs.getMin());
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_MAX)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_MAX_OF_"+childObjectsName, cs.getMax());
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                    if (parameters.getValue(CALCULATE_SUM)) {
                        summaryMeasurement = new MIAMeasurement(measurement + "_SUM_OF_"+childObjectsName, cs.getSum());
                        summaryMeasurement.setSource(this);
                        parentObject.addMeasurement(summaryMeasurement);
                    }

                }
            }
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(PARENT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CHILD_OBJECTS, Parameter.CHILD_OBJECTS,null,null));
        parameters.addParameter(new Parameter(CALCULATE_MEAN, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(CALCULATE_STD, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(CALCULATE_MIN, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(CALCULATE_MAX, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(CALCULATE_SUM, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CALCULATE_MEAN));
        returnedParameters.addParameter(parameters.getParameter(CALCULATE_STD));
        returnedParameters.addParameter(parameters.getParameter(CALCULATE_MIN));
        returnedParameters.addParameter(parameters.getParameter(CALCULATE_MAX));
        returnedParameters.addParameter(parameters.getParameter(CALCULATE_SUM));

        // Updating measurements with measurement choices from currently-selected object
        String objectName = parameters.getValue(PARENT_OBJECTS);
        if (objectName != null) {
            parameters.updateValueSource(CHILD_OBJECTS, objectName);

        } else {
            parameters.updateValueSource(CHILD_OBJECTS, null);

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        if (parameters.getValue(PARENT_OBJECTS) != null & parameters.getValue(CHILD_OBJECTS) != null) {
            String childName = parameters.getValue(CHILD_OBJECTS);

            String[] names = measurements.getMeasurementNames(parameters.getValue(CHILD_OBJECTS));

            for (String name:names) {
                if (parameters.getValue(CALCULATE_MEAN)) {
                    measurements.addMeasurement(parameters.getValue(PARENT_OBJECTS), name + "_MEAN_OF_"+childName);
                }

                if (parameters.getValue(CALCULATE_STD)) {
                    measurements.addMeasurement(parameters.getValue(PARENT_OBJECTS), name + "_STD_OF_"+childName);
                }

                if (parameters.getValue(CALCULATE_MIN)) {
                    measurements.addMeasurement(parameters.getValue(PARENT_OBJECTS), name + "_MIN_OF_"+childName);
                }

                if (parameters.getValue(CALCULATE_MAX)) {
                    measurements.addMeasurement(parameters.getValue(PARENT_OBJECTS), name + "_MAX_OF_"+childName);
                }

                if (parameters.getValue(CALCULATE_SUM)) {
                    measurements.addMeasurement(parameters.getValue(PARENT_OBJECTS), name + "_SUM_OF_"+childName);
                }

            }
        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
