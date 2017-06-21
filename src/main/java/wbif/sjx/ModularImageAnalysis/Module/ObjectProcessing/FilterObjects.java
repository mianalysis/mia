package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 23/05/2017.
 */
public class FilterObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String PARENT_OBJECT = "Parent object";

    private static final String MISSING_MEASUREMENTS = "Remove objects with missing measurements";
    private static final String NO_PARENT = "Remove objects without parent";
    private static final String[] FILTER_METHODS = new String[]{MISSING_MEASUREMENTS,NO_PARENT};

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

            inputObjects.entrySet().removeIf(
                    e -> ((Double) e.getValue().getMeasurement(measurement).getValue()).equals(Double.NaN));

        } else if (method.equals(NO_PARENT)) {
            HCName parentObjectName = parameters.getValue(PARENT_OBJECT);
            inputObjects.entrySet().removeIf(e -> e.getValue().getParent(parentObjectName) == null);

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(FILTER_METHOD,HCParameter.CHOICE_ARRAY,FILTER_METHODS[0],FILTER_METHODS));
        parameters.addParameter(new HCParameter(MEASUREMENT, HCParameter.MEASUREMENT,null,null));
        parameters.addParameter(new HCParameter(PARENT_OBJECT,HCParameter.PARENT_OBJECTS,null,null));

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

            HCName childObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueRange(PARENT_OBJECT,childObjectsName);

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
