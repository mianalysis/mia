package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.Iterator;

/**
 * Created by sc13967 on 23/05/2017.
 */
public class FilterObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String MIN_CHILD_N = "Minimum number of children";

    private static final String MISSING_MEASUREMENTS = "Remove objects with missing measurements";
    private static final String NO_PARENT = "Remove objects without parent";
    private static final String MIN_NUMBER_OF_CHILDREN = "Remove objects with too few children";
    private static final String[] FILTER_METHODS = new String[]{MISSING_MEASUREMENTS,NO_PARENT,MIN_NUMBER_OF_CHILDREN};

    @Override
    public String getTitle() {
        return "Filter objects";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String method = parameters.getValue(FILTER_METHOD);

        // Removing objects with a missing measurement (i.e. value set to null)
        if (method.equals(MISSING_MEASUREMENTS)) {
            String measurement = parameters.getValue(MEASUREMENT);

            Iterator<HCObject> iterator = inputObjects.values().iterator();
            while (iterator.hasNext()) {
                HCObject inputObject = iterator.next();

                if (inputObject.getMeasurement(measurement).getValue() == Double.NaN) {
                    inputObject.removeRelationships();
                    iterator.remove();
                }
            }

        } else if (method.equals(NO_PARENT)) {
            HCName parentObjectName = parameters.getValue(PARENT_OBJECT);

            Iterator<HCObject> iterator = inputObjects.values().iterator();
            while (iterator.hasNext()) {
                HCObject inputObject = iterator.next();

                if (inputObject.getParent(parentObjectName) == null) {
                    inputObject.removeRelationships();
                    iterator.remove();
                }
            }

        } else if (method.equals(MIN_NUMBER_OF_CHILDREN)) {
            HCName childObjectsName = parameters.getValue(CHILD_OBJECTS);
            int minChildN = parameters.getValue(MIN_CHILD_N);

            Iterator<HCObject> iterator = inputObjects.values().iterator();
            while (iterator.hasNext()) {
                HCObject inputObject = iterator.next();
                HCObjectSet childObjects = inputObject.getChildren(childObjectsName);

                // Removing the object if it has no children
                if (childObjects == null) {
                    iterator.remove();
                    continue;

                }

                // Removing the object if it has too few children
                if (childObjects.size() < minChildN) {
                    inputObject.removeRelationships();
                    iterator.remove();

                }
            }
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(FILTER_METHOD,HCParameter.CHOICE_ARRAY,FILTER_METHODS[0],FILTER_METHODS));
        parameters.addParameter(new HCParameter(MEASUREMENT, HCParameter.MEASUREMENT,null,null));
        parameters.addParameter(new HCParameter(PARENT_OBJECT,HCParameter.PARENT_OBJECTS,null,null));
        parameters.addParameter(new HCParameter(CHILD_OBJECTS,HCParameter.CHILD_OBJECTS,null,null));
        parameters.addParameter(new HCParameter(MIN_CHILD_N,HCParameter.INTEGER,1));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(FILTER_METHOD));

        if (parameters.getValue(FILTER_METHOD).equals(MISSING_MEASUREMENTS)) {
            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT));
            if (parameters.getValue(INPUT_OBJECTS) != null) {
                parameters.updateValueRange(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

            }

        } else if (parameters.getValue(FILTER_METHOD).equals(NO_PARENT)) {
            returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECT));

            HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueRange(PARENT_OBJECT,inputObjectsName);

        } else if (parameters.getValue(FILTER_METHOD).equals(MIN_NUMBER_OF_CHILDREN)) {
            returnedParameters.addParameter(parameters.getParameter(CHILD_OBJECTS));
            returnedParameters.addParameter(parameters.getParameter(MIN_CHILD_N));

            HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueRange(CHILD_OBJECTS,inputObjectsName);

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
