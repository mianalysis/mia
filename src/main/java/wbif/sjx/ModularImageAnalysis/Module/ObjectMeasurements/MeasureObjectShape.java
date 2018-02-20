package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ProjectObjects;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by sc13967 on 29/06/2017.
 */
public class MeasureObjectShape extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";

    public interface Measurements {
        String VOLUME_PX = "SHAPE//VOLUME_PX";
        String VOLUME_CAL = "SHAPE//VOLUME_CAL";
        String PROJ_DIA_PX = "SHAPE//PROJ_DIA_PX";
        String PROJ_DIA_CAL = "SHAPE//PROJ_DIA_CAL";

        String[] ALL = new String[]{VOLUME_PX, VOLUME_CAL, PROJ_DIA_PX, PROJ_DIA_CAL};

    }

    /**
     * Calculates the maximum distance between any two points of the
     * @return
     */
    public double calculateMaximumPointPointDistance(Obj object) {
        double[] x = object.getX(true);
        double[] y = object.getY(true);
        double[] z = object.getZ(true,true);

        TreeSet<Point<Integer>> points = object.getPoints();

        double maxDistance = 0;

        for (int i=0;i<x.length;i++) {
            for (int j=0;j<x.length;j++) {
                if (i == j) continue;

                double x1 = x[i];
                double y1 = y[i];
                double z1 = z[i];
                double x2 = x[j];
                double y2 = y[j];
                double z2 = z[j];

                double distance = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));

                maxDistance = Math.max(distance,maxDistance);

            }
        }

        return maxDistance;

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
        boolean is2D = (coordRange[2][1]==coordRange[2][0]);

        // Running through each object, making the measurements
        for (Obj inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getXCoords();

            double distXY = inputObject.getDistPerPxXY();
            double distZ = inputObject.getDistPerPxZ();
            double cal;
            inputObject.getCoordinateRange();

            // Calibration will depend on if they're 3D objects
            if (is2D) {
                cal = distXY * distXY;
            } else {
                cal = distXY * distXY * distZ;
            }

            // Adding the volume measurements
            inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX,x.size(),this));
            inputObject.addMeasurement(new Measurement(Measurements.VOLUME_CAL,x.size()*cal,this));

            // Adding the projected-object diameter measurements
            Obj projectedObject = ProjectObjects.createProjection(inputObject,"Projected");
            double maxDistance = calculateMaximumPointPointDistance(projectedObject);
            inputObject.addMeasurement(new Measurement(Measurements.PROJ_DIA_PX,maxDistance,this));
            inputObject.addMeasurement(new Measurement(Measurements.PROJ_DIA_CAL,maxDistance*distXY,this));

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.VOLUME_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.VOLUME_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.PROJ_DIA_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.PROJ_DIA_CAL));

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

        objectMeasurementReferences.updateImageObjectName(Measurements.VOLUME_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.VOLUME_CAL,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.PROJ_DIA_PX,inputObjectsName);
        objectMeasurementReferences.updateImageObjectName(Measurements.PROJ_DIA_CAL,inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
