package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Analysis.EllipseCalculator;
import wbif.sjx.common.Analysis.EllipsoidCalculator;

import java.util.ArrayList;

/**
 * Created by sc13967 on 29/06/2017.
 */
public class MeasureObjectShape extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASURE_VOLUME = "Measure volume";
    public static final String FITTING_MODE = "Fit convex hull to";
    public static final String MEASURE_PROJECTED_AREA = "Measure projected area";
    public static final String MEASURE_PROJECTED_DIA = "Measure projected diameter";
    public static final String MEASURE_PROJECTED_ELLIPSE = "Measure projected ellipse";

    public interface Measurements {
        String N_VOXELS = "SHAPE // N_VOXELS";
        String VOLUME_PX = "SHAPE // VOLUME_(PX^3)";
        String VOLUME_CAL = "SHAPE // VOLUME_(${CAL}^3)";
        String ELLIPSE_THETA = "SHAPE // ELLIPSE_ANGLE_(DEGS)";
        String PROJ_AREA_PX = "SHAPE // PROJ_AREA_(PX^2)";
        String PROJ_AREA_CAL = "SHAPE // PROJ_AREA_(${CAL}^2)";
        String PROJ_DIA_PX = "SHAPE // PROJ_DIA_(PX)";
        String PROJ_DIA_CAL = "SHAPE // PROJ_DIA_(${CAL})";

    }

    /**
     * Calculates the maximum distance between any two points of the
     * @return
     */
    public double calculateMaximumPointPointDistance(Obj object) {
        double[] x = object.getX(true);
        double[] y = object.getY(true);
        double[] z = object.getZ(true,true);

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
        return "Ellipsoid fitting using BoneJ.  " +
                "\nOrientation 1 relative to X-axis, Orientation 2 relative to XY-plane";
    }

    @Override
    public void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting parameters
        boolean measureVolume = parameters.getValue(MEASURE_VOLUME);
        boolean measureProjectedArea = parameters.getValue(MEASURE_PROJECTED_AREA);
        boolean measureProjectedDiameter = parameters.getValue(MEASURE_PROJECTED_DIA);
        boolean measureProjectedEllipse = parameters.getValue(MEASURE_PROJECTED_ELLIPSE);

        // Running through each object, making the measurements
        for (Obj inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getXCoords();

            // Adding the volume measurements
            if (measureVolume) {
                inputObject.addMeasurement(new Measurement(Measurements.N_VOXELS,x.size(),this));

                double containedVolumePx = inputObject.getContainedVolume(true);
                inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX, containedVolumePx, this));

                double containedVolumeCal = inputObject.getContainedVolume(false);
                inputObject.addMeasurement(new Measurement(Units.replace(Measurements.VOLUME_CAL), containedVolumeCal, this));
            }

            // If necessary analyses are included
            Obj projectedObject = null;
            if (measureProjectedArea || measureProjectedDiameter || measureProjectedEllipse) {
                projectedObject = ProjectObjects.createProjection(inputObject, "Projected",inputObject.is2D());
            }

            // Adding the projected-object area measurements
            if (measureProjectedArea) {
                double areaPx = projectedObject.getNVoxels();
                double areaCal = areaPx*projectedObject.getDistPerPxXY()*projectedObject.getDistPerPxXY();
                inputObject.addMeasurement(new Measurement(Measurements.PROJ_AREA_PX, areaPx, this));
                inputObject.addMeasurement(new Measurement(Units.replace(Measurements.PROJ_AREA_CAL), areaCal, this));
            }

            // Adding the projected-object diameter measurements
            if (measureProjectedDiameter) {
                double maxDistancePx = calculateMaximumPointPointDistance(projectedObject);
                double maxDistanceCal = calculateMaximumPointPointDistance(projectedObject)*inputObject.getDistPerPxXY();
                inputObject.addMeasurement(new Measurement(Measurements.PROJ_DIA_PX, maxDistancePx, this));
                inputObject.addMeasurement(new Measurement(Units.replace(Measurements.PROJ_DIA_CAL), maxDistanceCal, this));
            }

            // Adding the projected-object ellipse fitting measurements
            if (measureProjectedEllipse) {
                try {
                    EllipseCalculator ellipseCalculator = new EllipseCalculator(projectedObject);
                    double val = Math.toDegrees(ellipseCalculator.getEllipseThetaRads());
                    inputObject.addMeasurement(new Measurement(Measurements.ELLIPSE_THETA,val,this));
                } catch (RuntimeException e) {
                    inputObject.addMeasurement(new Measurement(Measurements.ELLIPSE_THETA,Double.NaN,this));
                }
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(MEASURE_VOLUME, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_PROJECTED_AREA, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(MEASURE_PROJECTED_DIA, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(MEASURE_PROJECTED_ELLIPSE, Parameter.BOOLEAN, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MEASURE_VOLUME));
        returnedParameters.add(parameters.getParameter(MEASURE_PROJECTED_AREA));
        returnedParameters.add(parameters.getParameter(MEASURE_PROJECTED_DIA));
        returnedParameters.add(parameters.getParameter(MEASURE_PROJECTED_ELLIPSE));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        if (parameters.getValue(MEASURE_VOLUME)) {
            MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.N_VOXELS);
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);

            reference = objectMeasurementReferences.getOrPut(Measurements.VOLUME_PX);
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.VOLUME_CAL));
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);
        }

        if (parameters.getValue(MEASURE_PROJECTED_ELLIPSE)) {
            MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.ELLIPSE_THETA);
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);
        }

        if (parameters.getValue(MEASURE_PROJECTED_AREA)) {
            MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.PROJ_AREA_PX);
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.PROJ_AREA_CAL));
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);
        }

        if (parameters.getValue(MEASURE_PROJECTED_DIA)) {
            MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.PROJ_DIA_PX);
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.PROJ_DIA_CAL));
            reference.setCalculated(true);
            reference.setImageObjName(inputObjectsName);
        }

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
