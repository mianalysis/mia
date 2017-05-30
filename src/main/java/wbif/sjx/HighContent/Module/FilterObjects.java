package wbif.sjx.HighContent.Module;

import wbif.sjx.HighContent.Object.*;

/**
 * Created by sc13967 on 23/05/2017.
 */
public class FilterObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";

    private static final String[] FILTER_METHODS = new String[]{"Remove objects with missing measurements"};

    @Override
    public String getTitle() {
        return "Filter objects";
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
        if (method.equals(FILTER_METHODS[0])) {
            String measurement = parameters.getValue(MEASUREMENT);

            inputObjects.entrySet().removeIf(
                    e -> ((Double) e.getValue().getMeasurement(measurement).getValue()).equals(Double.NaN));

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(FILTER_METHOD,HCParameter.CHOICE_ARRAY,FILTER_METHODS[0],FILTER_METHODS));
        parameters.addParameter(new HCParameter(MEASUREMENT, HCParameter.MEASUREMENT,null,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(FILTER_METHOD));

        if (parameters.getValue(FILTER_METHOD).equals(FILTER_METHODS[0])) {
            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT));
            if (parameters.getValue(INPUT_OBJECTS) != null) {
                parameters.updateValueRange(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

            }
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
