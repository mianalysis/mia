package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 29/06/2017.
 */
public class MeasureObjectShape extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";

    private interface Measurements {
        String N_VOXELS = "N_VOXELS";

        String[] ALL = new String[]{N_VOXELS};
    }


    @Override
    public String getTitle() {
        return "Measure object shape";
    }

    @Override
    public String getHelp() {
        return "+++INCOMPLETE+++" +
                "\nCurrently only measures the number of voxels per object";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Running through each object, making the measurements
        for (Obj inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getXCoords();

            // Adding the relevant measurements
            inputObject.addMeasurement(new Measurement(Measurements.N_VOXELS,x.size(),this));

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.N_VOXELS));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        objectMeasurementReferences.updateImageObjectName(Measurements.N_VOXELS,inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
