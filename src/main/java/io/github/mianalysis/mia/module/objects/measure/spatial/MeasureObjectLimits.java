package io.github.mianalysis.mia.module.objects.measure.spatial;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.*;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.*;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.units.SpatialUnit;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MeasureObjectLimits extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public MeasureObjectLimits(Modules modules) {
        super("Measure object limits",modules);
    }

    public interface Measurements {
        String MIN_X_PX = "LIMITS // MIN_X_(PX)";
        String MIN_X_CAL = "LIMITS // MIN_X_(${SCAL})";
        String MAX_X_PX = "LIMITS // MAX_X_(PX)";
        String MAX_X_CAL = "LIMITS // MAX_X_(${SCAL})";
        String MIN_Y_PX = "LIMITS // MIN_Y_(PX)";
        String MIN_Y_CAL = "LIMITS // MIN_Y_(${SCAL})";
        String MAX_Y_PX = "LIMITS // MAX_Y_(PX)";
        String MAX_Y_CAL = "LIMITS // MAX_Y_(${SCAL})";
        String MIN_Z_PX = "LIMITS // MIN_Z_(PX)";
        String MAX_Z_PX = "LIMITS // MAX_Z_(PX)";
        String MIN_Z_SLICE = "LIMITS // MIN_Z_(SLICE)";
        String MIN_Z_CAL = "LIMITS // MIN_Z_(${SCAL})";
        String MAX_Z_SLICE = "LIMITS // MAX_Z_(SLICE)";
        String MAX_Z_CAL = "LIMITS // MAX_Z_(${SCAL})";

    }



    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Measures the XYZ spatial limits of each object relative to the origin (x = 0, y = 0, z = 0) of the original image.  Limits are stored as measurements associated with each object.  Measurements are reported in pixel (slice for z) coordinates and calibrated units.";
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

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
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "Objects from workspace to measure centroid of.  Measurements will be associated with the corresponding object in this collection."));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.MIN_X_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_X_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_X_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_X_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum x-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Y_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MIN_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Minimum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Y_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum y-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
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
                "Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured as slice index.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAX_Z_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Maximum z-coordinate for all pixels in the object, \""+inputObjectsName+"\".  " +
                "Measured in calibrated ("+SpatialUnit.getOMEUnit().getSymbol()+") units.");
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
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
