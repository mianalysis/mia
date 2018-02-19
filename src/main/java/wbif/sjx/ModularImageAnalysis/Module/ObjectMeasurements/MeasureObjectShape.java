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

    public interface Measurements {
        String AREA_PX = "SHAPE//AREA_PX";
        String AREA_CAL = "SHAPE//AREA_CAL";

        String[] ALL = new String[]{AREA_PX,AREA_CAL};
    }


    @Override
    public String getTitle() {
        return "Measure object shape";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        int[][] coordRange = inputObjects.getSpatialLimits();

        // Running through each object, making the measurements
        for (Obj inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getXCoords();

            double distXY = inputObject.getDistPerPxXY();
            double distZ = inputObject.getDistPerPxZ();
            double cal;
            inputObject.getCoordinateRange();

            // Calibration will depend on if they're 3D objects
            if (coordRange[2][1]==coordRange[2][0]) {
                cal = distXY * distXY;
            } else {
                cal = distXY * distXY * distZ;
            }

            // Adding the relevant measurements
            inputObject.addMeasurement(new Measurement(Measurements.AREA_PX,x.size(),this));
            inputObject.addMeasurement(new Measurement(Measurements.AREA_CAL,x.size()*cal,this));

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.AREA_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.AREA_CAL));

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

        objectMeasurementReferences.updateImageObjectName(Measurements.AREA_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.AREA_CAL,inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
