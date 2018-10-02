package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

public class OrientationRelativeToPoint extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String DIMENSIONALITY_MODE = "Dimensionality mode";
    public static final String ORIENTATION_IN_X_Y_MEASUREMENT = "Orientation in X/Y measurement";
    public static final String ORIENTATION_IN_XY_Z_MEASUREMENT = "Orientation in XY/Z measurement";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String MEASUREMENT_OBJECTS = "Measurement objects";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";
    public static final String OBJECT_CHOICE_MODE = "Object choice mode";


    public interface DimensionalityModes {
        String TWO_D = "Process in 2D";
        String THREE_D = "Process in 3D";

        String[] ALL = new String[]{TWO_D,THREE_D};

    }

    public interface RelativeModes {
        String IMAGE_CENTRE = "Image centre";
        String POSITION_MEASUREMENTS = "Position measurements";

        String[] ALL = new String[]{IMAGE_CENTRE,POSITION_MEASUREMENTS};

    }

    public interface ObjectChoiceModes {
        String LARGEST_OBJECT = "Largest object";
        String SMALLEST_OBJECT = "Smallest object";

        String[] ALL = new String[]{LARGEST_OBJECT,SMALLEST_OBJECT};

    }


    @Override
    public String getTitle() {
        return "Measure orientation relative to a point";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(DIMENSIONALITY_MODE,Parameter.CHOICE_ARRAY,DimensionalityModes.TWO_D,DimensionalityModes.ALL));
        parameters.add(new Parameter(ORIENTATION_IN_X_Y_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(ORIENTATION_IN_XY_Z_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(RELATIVE_MODE,Parameter.CHOICE_ARRAY,RelativeModes.IMAGE_CENTRE,RelativeModes.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASUREMENT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(X_POSITION_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(Y_POSITION_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(Z_POSITION_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(OBJECT_CHOICE_MODE,Parameter.CHOICE_ARRAY,ObjectChoiceModes.LARGEST_OBJECT,ObjectChoiceModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(DIMENSIONALITY_MODE));
        switch ((String) parameters.getValue(DIMENSIONALITY_MODE)) {
            case DimensionalityModes.TWO_D:
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT));
                break;
            case DimensionalityModes.THREE_D:
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT));
                break;
        }

        returnedParameters.add(parameters.getParameter(RELATIVE_MODE));
        switch ((String) parameters.getValue(RELATIVE_MODE)) {
            case RelativeModes.IMAGE_CENTRE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
            case RelativeModes.POSITION_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_OBJECTS));
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
                if (parameters.getValue(DIMENSIONALITY_MODE).equals(DimensionalityModes.THREE_D)) {
                    returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));
                }
                returnedParameters.add(parameters.getParameter(OBJECT_CHOICE_MODE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
