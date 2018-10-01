package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.CreateDistanceMap;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

public class MeasureRadialIntensityProfile extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String NUMBER_OF_RADIAL_SAMPLES = "Number of radial samples";
    public static final String RANGE_MODE = "Range mode";
    public static final String RANGE_VALUE = "Range value";
    //public static final String CALIBRATED_UNITS = "Calibrated units"; // To be added


    public interface ReferenceModes extends CreateDistanceMap.ReferenceModes {}

    public interface RangeModes {
        String ABSOLUTE_VALUE = "Absolute value";
        // String FRACTIONAL_DISTANCE_TO_EDGE = "Fractional distance to edge";

        String[] ALL = new String[]{ABSOLUTE_VALUE};

    }


    static void processObject(Obj inputObject, Image inputImage, String referencePoint) {
        // Getting the distance map
        Image distanceMap = getDistanceMap(inputObject,inputImage,referencePoint);



    }

    static Image getDistanceMap(Obj inputObject, Image inputImage, String referencePoint) {
        switch (referencePoint) {
            case ReferenceModes.ABSOLUTE_CENTROID_DISTANCE:
                return getCentroidDistanceMap(inputObject,inputImage);

        }

        return null;
    }

    static Image getCentroidDistanceMap(Obj inputObject, Image inputImage) {
        int x = (int) Math.round(inputObject.getXMean(true));
        int y = (int) Math.round(inputObject.getYMean(true));
        int z = (int) Math.round(inputObject.getZMean(true,false));

        // Creating a blank image for the distance map
        return null;

    }


    @Override
    public String getTitle() {
        return "Measure radial intensity profile";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting other parameters
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        int nRadialSample = parameters.getValue(NUMBER_OF_RADIAL_SAMPLES);
        String rangeMode = parameters.getValue(RANGE_MODE);
        double rangeValue = parameters.getValue(RANGE_VALUE);

        // Processing each object

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(REFERENCE_MODE,Parameter.CHOICE_ARRAY,ReferenceModes.ABSOLUTE_CENTROID_DISTANCE,ReferenceModes.ALL));
        parameters.add(new Parameter(NUMBER_OF_RADIAL_SAMPLES,Parameter.INTEGER,10));
        parameters.add(new Parameter(RANGE_MODE,Parameter.CHOICE_ARRAY,RangeModes.ABSOLUTE_VALUE,RangeModes.ALL));
        parameters.add(new Parameter(RANGE_VALUE,Parameter.DOUBLE,1d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_RADIAL_SAMPLES));

        returnedParameters.add(parameters.getParameter(RANGE_MODE));
        switch ((String) parameters.getValue(RANGE_MODE)) {
            case RangeModes.ABSOLUTE_VALUE:
                returnedParameters.add(parameters.getParameter(RANGE_VALUE));
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
