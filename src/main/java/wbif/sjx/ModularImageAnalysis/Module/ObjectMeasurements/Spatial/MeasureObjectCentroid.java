// TODO: Add measurements

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 11/05/2017.
 */
public class MeasureObjectCentroid extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CENTROID_METHOD = "Centroid method";

    public interface Methods {
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String BOTH = "Both";

        String[] ALL = new String[]{MEAN, MEDIAN, BOTH};

    }

    public interface Measurements {
        String MEAN_X_PX = "CENTROID // MEAN_X_(PX)";
        String MEAN_Y_PX = "CENTROID // MEAN_Y_(PX)";
        String MEAN_Z_SLICE = "CENTROID // MEAN_Z_(SLICE)";
        String MEAN_X_CAL = "CENTROID // MEAN_X_(${CAL})";
        String MEAN_Y_CAL = "CENTROID // MEAN_Y_(${CAL})";
        String MEAN_Z_CAL = "CENTROID // MEAN_Z_(${CAL})";
        String MEDIAN_X_PX = "CENTROID // MEDIAN_X_(PX)";
        String MEDIAN_Y_PX = "CENTROID // MEDIAN_Y_(PX)";
        String MEDIAN_Z_SLICE = "CENTROID // MEDIAN_Z_(SLICE)";
        String MEDIAN_X_CAL = "CENTROID // MEDIAN_X_(${CAL})";
        String MEDIAN_Y_CAL = "CENTROID // MEDIAN_Y_(${CAL})";
        String MEDIAN_Z_CAL = "CENTROID // MEDIAN_Z_(${CAL})";

    }


    @Override
    public String getTitle() {
        return "Measure object centroid";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "Z-coordinates are specified in terms of slices (not pixels)";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting which centroid measures to calculate
        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);
        writeMessage("Calculating centroid as "+choice);

        // Getting the centroids of each and saving them to the objects
        for (Obj object:inputObjects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            if (useMean) {
                if (x != null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_X_PX,object.getXMean(true)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_X_CAL),object.getXMean(false)));
                }
                if (y!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_Y_PX,object.getYMean(true)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Y_CAL),object.getYMean(false)));
                }
                if (z!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_Z_SLICE,object.getZMean(true,false)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Z_CAL),object.getZMean(false,false)));
                }
            }

            if (useMedian) {
                if (x != null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_X_PX,object.getXMedian(true)));
                    object.addMeasurement(new Measurement(Units.replace(Measurements.MEDIAN_X_CAL),object.getXMedian(false)));
                }
                if (y!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Y_PX,object.getYMedian(true)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Y_CAL,object.getYMedian(false)));
                }
                if (z!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Z_SLICE,object.getZMedian(true,false)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Z_CAL,object.getZMedian(false,false)));
                }
            }
        }

        return true;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CENTROID_METHOD, Parameter.CHOICE_ARRAY,Methods.MEAN,Methods.ALL));

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
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);

        if (useMean) {
            MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_X_PX);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Mean x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in pixel units.");

            reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_Y_PX);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Mean y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in pixel units.");

            reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_Z_SLICE);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Mean z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in slice units.");

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_X_CAL));
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Mean x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_Y_CAL));
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Mean y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_Z_CAL));
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Mean z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

        }

        if (useMedian) {
            MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.MEDIAN_X_PX);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Median x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in pixel units.");

            reference = objectMeasurementReferences.getOrPut(Measurements.MEDIAN_Y_PX);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Median y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in pixel units.");

            reference = objectMeasurementReferences.getOrPut(Measurements.MEDIAN_Z_SLICE);
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Median z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in slice units.");

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEDIAN_X_CAL));
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Median x-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEDIAN_Y_CAL));
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Median y-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

            reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEDIAN_Z_CAL));
            reference.setImageObjName(inputObjectsName);
            reference.setCalculated(true);
            reference.setDescription("Median z-position of all pixels in the object, \""+inputObjectsName+"\"." +
                    "  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

        }

        return objectMeasurementReferences;

    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
