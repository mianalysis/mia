//TODO: Add ability to expand objects by a couple of pixels, to give a background signal estimate.  Probably only makes
//      sense for normalised distances

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.process.StackStatistics;
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
    public static final String MIN_DISTANCE = "Minimum distance";
    public static final String MAX_DISTANCE = "Maximum distance";
//    public static final String NORMALISE_DISTANCES = "Normalise distances to object size";
    //public static final String CALIBRATED_UNITS = "Calibrated units"; // To be added


    public interface ReferenceModes extends CreateDistanceMap.ReferenceModes {}

    public interface RangeModes {
        String AUTOMATIC_RANGE = "Automatic range";
        String MANUAL_RANGE = "Manual range";

        String[] ALL = new String[]{AUTOMATIC_RANGE,MANUAL_RANGE};

    }


    static Image getDistanceMap(ObjCollection inputObjects, Image inputImage, String referenceMode) {
        switch (referenceMode) {
            case ReferenceModes.DISTANCE_FROM_CENTROID:
                return CreateDistanceMap.getCentroidDistanceMap(inputImage,inputObjects,"Distance map");

            case ReferenceModes.DISTANCE_FROM_EDGE:
                return CreateDistanceMap.getEdgeDistanceMap(inputImage,inputObjects,"Distance map",false);
        }

        return null;

    }

    /**
     * Calculates the bin centroids for the distance measurements
     * @param distanceMap
     * @param nRadialSamples
     * @return
     */
    static double[] getDistanceBins(Image distanceMap, int nRadialSamples) {
        // Getting the maximum distance measurement
        StackStatistics stackStatistics = new StackStatistics(distanceMap.getImagePlus());
        double minDistance = stackStatistics.min;
        double maxDistance = stackStatistics.max;

        double[] distanceBins = new double[nRadialSamples];
        double binWidth = (maxDistance-minDistance)/(nRadialSamples-1);
        for (int i=0;i<nRadialSamples;i++) {
            distanceBins[i] = (i*binWidth)+minDistance;
            System.err.println("Bin "+i+"_"+(i*binWidth));
        }

        return distanceBins;

    }

    static double[] getDistanceBins(int nRadialSamples, double minDistance, double maxDistance) {
        double[] distanceBins = new double[nRadialSamples];
        double binWidth = (maxDistance-minDistance)/(nRadialSamples-1);
        for (int i=0;i<nRadialSamples;i++) {
            distanceBins[i] = (i*binWidth)+minDistance;
            System.err.println("Bin "+i+"_"+(i*binWidth));
        }

        return distanceBins;

    }

    static void processObject(Obj inputObject, Image inputImage, Image distanceMap, double[] distanceBins) {


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
        double minDistance = parameters.getValue(MIN_DISTANCE);
        double maxDistance = parameters.getValue(MAX_DISTANCE);

        // Getting the distance map for all objects
        Image distanceMap = getDistanceMap(inputObjects,inputImage,referenceMode);

        // Getting the distance bin centroids
        double[] distanceBins = null;
        switch (rangeMode) {
            case RangeModes.AUTOMATIC_RANGE:
                distanceBins = getDistanceBins(distanceMap,nRadialSample);
                break;

            case RangeModes.MANUAL_RANGE:
                distanceBins = getDistanceBins(nRadialSample,minDistance,maxDistance);
                break;
        }

        // Processing each object
        int count = 0;
        int total = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Processing object "+(++count)+" of "+total);
            processObject(inputObject,inputImage,distanceMap,distanceBins);
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(REFERENCE_MODE,Parameter.CHOICE_ARRAY,ReferenceModes.DISTANCE_FROM_CENTROID,ReferenceModes.ALL));
        parameters.add(new Parameter(NUMBER_OF_RADIAL_SAMPLES,Parameter.INTEGER,10));
        parameters.add(new Parameter(RANGE_MODE,Parameter.CHOICE_ARRAY,RangeModes.AUTOMATIC_RANGE,RangeModes.ALL));
        parameters.add(new Parameter(MIN_DISTANCE,Parameter.DOUBLE,0d));
        parameters.add(new Parameter(MAX_DISTANCE,Parameter.DOUBLE,1d));

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
            case RangeModes.AUTOMATIC_RANGE:

                break;

            case RangeModes.MANUAL_RANGE:
                returnedParameters.add(parameters.getParameter(MIN_DISTANCE));
                returnedParameters.add(parameters.getParameter(MAX_DISTANCE));
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
