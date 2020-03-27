// TODO: Add measurements

package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 11/05/2017.
 */
public class MeasureObjectCentroid extends Module {
    public static final String INPUT_OBJECTS = "Input objects";

    public MeasureObjectCentroid(ModuleCollection modules) {
        super("Measure object centroid",modules);
    }

    public interface Measurements {
        String MEAN_X_PX = "CENTROID // MEAN_X_(PX)";
        String MEAN_Y_PX = "CENTROID // MEAN_Y_(PX)";
        String MEAN_Z_SLICE = "CENTROID // MEAN_Z_(SLICE)";
        String MEAN_X_CAL = "CENTROID // MEAN_X_(${CAL})";
        String MEAN_Y_CAL = "CENTROID // MEAN_Y_(${CAL})";
        String MEAN_Z_CAL = "CENTROID // MEAN_Z_(${CAL})";

    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Z-coordinates are specified in terms of slices (not pixels)";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting the centroids of each and saving them to the objects
        for (Obj object:inputObjects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            if (x != null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_X_PX,object.getXMean(true)));
                object.addMeasurement(new Measurement(Measurements.MEAN_X_CAL,object.getXMean(false)));
            }
            if (y!= null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_Y_PX,object.getYMean(true)));
                object.addMeasurement(new Measurement(Measurements.MEAN_Y_CAL,object.getYMean(false)));
            }
            if (z!= null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_Z_SLICE,object.getZMean(true,false)));
                object.addMeasurement(new Measurement(Measurements.MEAN_Z_CAL,object.getZMean(false,false)));
            }
        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

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

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_X_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Y_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Z_SLICE);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in slice units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_X_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Y_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_Z_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Mean z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
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
}
