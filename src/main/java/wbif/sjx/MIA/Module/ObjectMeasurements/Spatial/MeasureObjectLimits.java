package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.*;

public class MeasureObjectLimits extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public MeasureObjectLimits(ModuleCollection modules) {
        super("Measure object limits",modules);
    }

    public interface Measurements {
        String MIN_X_PX = "LIMITS // MIN_X_(PX)";
        String MIN_X_CAL = "LIMITS // MIN_X_(${CAL})";
        String MAX_X_PX = "LIMITS // MAX_X_(PX)";
        String MAX_X_CAL = "LIMITS // MAX_X_(${CAL})";
        String MIN_Y_PX = "LIMITS // MIN_Y_(PX)";
        String MIN_Y_CAL = "LIMITS // MIN_Y_(${CAL})";
        String MAX_Y_PX = "LIMITS // MAX_Y_(PX)";
        String MAX_Y_CAL = "LIMITS // MAX_Y_(${CAL})";
        String MIN_Z_PX = "LIMITS // MIN_Z_(PX)";
        String MAX_Z_PX = "LIMITS // MAX_Z_(PX)";
        String MIN_Z_SLICE = "LIMITS // MIN_Z_(SLICE)";
        String MIN_Z_CAL = "LIMITS // MIN_Z_(${CAL})";
        String MAX_Z_SLICE = "LIMITS // MAX_Z_(SLICE)";
        String MAX_Z_CAL = "LIMITS // MAX_Z_(${CAL})";

    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Measures the XYZ spatial limits of each object relative to the origin (x = 0, y = 0, z = 0) of the original image.  Limits are stored as measurements associated with each object.  Measurements are reported in pixel (slice for z) coordinates and calibrated units.";
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            double[][] extentsPx = inputObject.getExtents(true,true);
            double[][] extentsSlice = inputObject.getExtents(true,false);

            double dppXY = inputObject.getDppXY();
            double dppZ = inputObject.getDppZ();

            inputObject.addMeasurement(new Measurement(Measurements.MIN_X_PX,extentsPx[0][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_X_CAL,extentsPx[0][0]*dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_X_PX,extentsPx[0][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_X_CAL,extentsPx[0][1]*dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Y_PX,extentsPx[1][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Y_CAL,extentsPx[1][0]*dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Y_PX,extentsPx[1][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Y_CAL,extentsPx[1][1]*dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Z_PX,extentsPx[2][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Z_PX,extentsPx[2][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Z_SLICE,extentsSlice[2][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Z_CAL,extentsSlice[2][0]*dppZ));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Z_SLICE,extentsSlice[2][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Z_CAL,extentsSlice[2][1]*dppZ));

        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return Status.PASS;
        
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "Objects to measure."));

        addParameterDescriptions();
        
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.MIN_X_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_X_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_X_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_X_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Y_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Y_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Z_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Z_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured as slice index.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Z_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured as slice index.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects to measure spatial limits for.  Measurements will be associated with each object.");

    }
}
