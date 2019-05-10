package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import java.util.ArrayList;

/**
 * Created by sc13967 on 29/06/2017.
 */
public class MeasureObjectShape extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    public static final String MEASURE_VOLUME = "Measure volume";
    public static final String MEASURE_PROJECTED_AREA = "Measure projected area";
    public static final String MEASURE_PROJECTED_DIA = "Measure projected diameter";
    public static final String MEASURE_PROJECTED_PERIM = "Measure projected perimeter";

    public MeasureObjectShape(ModuleCollection modules) {
        super(modules);
    }


    public interface Measurements {
        String N_VOXELS = "SHAPE // N_VOXELS";
        String VOLUME_PX = "SHAPE // VOLUME_(PX^3)";
        String VOLUME_CAL = "SHAPE // VOLUME_(${CAL}^3)";
        String PROJ_AREA_PX = "SHAPE // PROJ_AREA_(PX^2)";
        String PROJ_AREA_CAL = "SHAPE // PROJ_AREA_(${CAL}^2)";
        String PROJ_DIA_PX = "SHAPE // PROJ_DIA_(PX)";
        String PROJ_DIA_CAL = "SHAPE // PROJ_DIA_(${CAL})";
        String PROJ_PERIM_PX = "SHAPE // PROJ_PERIM_(PX)";
        String PROJ_PERIM_CAL = "SHAPE // PROJ_PERIM_(${CAL})";
        String PROJ_CIRCULARITY = "SHAPE // PROJ_CIRCULARITY";

    }


    /*
     * Calculates the maximum distance between any two points of the
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
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "Ellipsoid fitting using BoneJ.  " +
                "\nOrientation 1 relative to X-axis, Orientation 2 relative to XY-plane";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting parameters
        boolean measureVolume = parameters.getValue(MEASURE_VOLUME);
        boolean measureProjectedArea = parameters.getValue(MEASURE_PROJECTED_AREA);
        boolean measureProjectedDiameter = parameters.getValue(MEASURE_PROJECTED_DIA);
        boolean measureProjectedPerimeter = parameters.getValue(MEASURE_PROJECTED_PERIM);

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
            if (measureProjectedArea || measureProjectedDiameter || measureProjectedPerimeter) {
                try {
                    projectedObject = ProjectObjects.createProjection(inputObject, "Projected",inputObject.is2D());
                } catch (IntegerOverflowException e) {
                    return false;
                }
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

            // Adding the projected-object perimeter measurements
            if (measureProjectedPerimeter) {
                double areaPx = projectedObject.getNVoxels();
                double perimeterPx = projectedObject.getRoi(0).getLength();
                double perimeterCal = perimeterPx*inputObject.getDistPerPxXY();
                inputObject.addMeasurement(new Measurement(Measurements.PROJ_PERIM_PX,perimeterPx,this));
                inputObject.addMeasurement(new Measurement(Units.replace(Measurements.PROJ_PERIM_CAL),perimeterCal,this));

                double circularity = 4*Math.PI*areaPx/(perimeterPx*perimeterPx);
                inputObject.addMeasurement(new Measurement(Measurements.PROJ_CIRCULARITY, circularity, this));

            }
        }

        if (showOutput) inputObjects.showMeasurements(this,workspace.getAnalysis().getModules());

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new BooleanP(MEASURE_VOLUME, this, true));
        parameters.add(new BooleanP(MEASURE_PROJECTED_AREA, this, false));
        parameters.add(new BooleanP(MEASURE_PROJECTED_DIA, this, false));
        parameters.add(new BooleanP(MEASURE_PROJECTED_PERIM, this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllAvailable(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        MeasurementRef.Type type = MeasurementRef.Type.OBJECT;

        if (parameters.getValue(MEASURE_VOLUME)) {
            MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.N_VOXELS,type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Number of voxels (3D pixels) in the object, \""+inputObjectsName+"\".  Note: " +
                    "This doesn't take spatial scaling of XY and Z into account, so isn't a good measure of true " +
                    "object volume.");

            reference = objectMeasurementRefs.getOrPut(Measurements.VOLUME_PX,type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Volume of the object, \""+inputObjectsName+"\".  Takes spatial scaling of XY vs " +
                    "Z into account (i.e. converts object height from slice units to pixel units.  Measured in pixel " +
                    "units.");

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.VOLUME_CAL),type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Volume of the object, \""+inputObjectsName+"\".  Takes spatial scaling of XY vs " +
                    "Z into account (i.e. converts object height from slice units to pixel units prior to conversion" +
                    " to calibrated units.  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        }

        if (parameters.getValue(MEASURE_PROJECTED_AREA)) {
            MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_AREA_PX,type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Area of the 2D Z-projection of the object, \""+inputObjectsName+"\".  Measured " +
                    "in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.PROJ_AREA_CAL),type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Area of the 2D Z-projection of the object, \""+inputObjectsName+"\".  Measured " +
                    "in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        }

        if (parameters.getValue(MEASURE_PROJECTED_DIA)) {
            MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_DIA_PX,type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Longest distance between any two points of the 2D Z-projection of the object, \""
                    + inputObjectsName+"\".  Measured in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.PROJ_DIA_CAL),type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Longest distance between any two points of the 2D Z-projection of the object, \""
                    + inputObjectsName+"\".  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") " +
                    "units.");
        }

        if (parameters.getValue(MEASURE_PROJECTED_PERIM)) {
            MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_PERIM_PX,type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Perimeter of the 2D Z-projection of the object, \"" + inputObjectsName+"\".  " +
                    "Measured in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.PROJ_PERIM_CAL),type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Perimeter of the 2D Z-projection of the object, \"" + inputObjectsName+"\".  " +
                    "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") " +
                    "units.");

            reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.PROJ_CIRCULARITY),type);
            reference.setAvailable(true);
            reference.setImageObjName(inputObjectsName);
            reference.setDescription("Circularity of the 2D Z-projection of the object, \"" + inputObjectsName+"\".  " +
                    "Uses the calculation \"circularity = 4pi(area/perimeter^2)\".  This measurement has no units.");

        }

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
