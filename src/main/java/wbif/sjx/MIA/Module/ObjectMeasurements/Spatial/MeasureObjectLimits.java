package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

public class MeasureObjectLimits extends Module {
    public static final String INPUT_OBJECTS = "Input objects";

    public interface Measurements {
        String MIN_X_PX = "LIMITS // MIN_X_(PX)";
        String MAX_X_PX = "LIMITS // MAX_X_(PX)";
        String MIN_Y_PX = "LIMITS // MIN_Y_(PX)";
        String MAX_Y_PX = "LIMITS // MAX_Y_(PX)";
        String MIN_Z_PX = "LIMITS // MIN_Z_(PX)";
        String MAX_Z_PX = "LIMITS // MAX_Z_(PX)";
        String MIN_Z_SLICE = "LIMITS // MIN_Z_(SLICE)";
        String MAX_Z_SLICE = "LIMITS // MAX_Z_(SLICE)";

    }

    @Override
    public String getTitle() {
        return "Measure object limits";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "Measures the spatial limits of each object in terms of pixels.";
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            double[][] extentsPx = inputObject.getExtents(true,true);
            double[][] extentsSlice = inputObject.getExtents(true,false);

            inputObject.addMeasurement(new Measurement(Measurements.MIN_X_PX,extentsPx[0][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_X_PX,extentsPx[0][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Y_PX,extentsPx[1][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Y_PX,extentsPx[1][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Z_PX,extentsPx[2][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Z_PX,extentsPx[2][1]));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_Z_SLICE,extentsSlice[2][0]));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_Z_SLICE,extentsSlice[2][1]));

        }

        if (showOutput) inputObjects.showMeasurements(this,workspace.getAnalysis().getModules());

        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this,"Objects to measure."));

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
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        objectMeasurementRefs.setAllAvailable(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        MeasurementRef.Type type = MeasurementRef.Type.OBJECT;

        MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.MIN_X_PX,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Minimum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_X_PX,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Maximum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Y_PX,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Minimum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Y_PX,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Maximum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Z_PX,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Minimum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_PX,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Z_SLICE,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Minimum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured as slice index.");

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_SLICE,type);
        reference.setAvailable(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured as slice index.");

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
