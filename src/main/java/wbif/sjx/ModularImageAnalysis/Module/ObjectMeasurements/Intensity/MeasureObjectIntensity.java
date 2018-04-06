// TODO: Could add an optional parameter to select the channel of the input image to use for measurement

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureIntensityDistribution;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MeasureObjectIntensity extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";
    public static final String MEASURE_WEIGHTED_CENTRE = "Measure weighted centre";
    public static final String MEASURE_WEIGHTED_EDGE_DISTANCE = "Measure weighted distance to edge";
    public static final String EDGE_DISTANCE_MODE = "Edge distance mode";
    public static final String MEASURE_EDGE_INTENSITY_PROFILE = "Measure intensity profile from edge";
    public static final String MINIMUM_DISTANCE = "Minimum distance";
    public static final String MAXIMUM_DISTANCE = "Maximum distance";
    public static final String CALIBRATED_DISTANCES = "Calibrated distances";
    public static final String NUMBER_OF_MEASUREMENTS = "Number of measurements";

    public interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";
        String STDEV = "STDEV";

        String X_CENT_MEAN = "X_CENTRE_MEAN (PX)";
        String X_CENT_STDEV = "X_CENTRE_STDEV (PX)";
        String Y_CENT_MEAN = "Y_CENTRE_MEAN (PX)";
        String Y_CENT_STDEV = "Y_CENTRE_STDEV (PX)";
        String Z_CENT_MEAN = "Z_CENTRE_MEAN (SLICE)";
        String Z_CENT_STDEV = "Z_CENTRE_STDEV (SLICE)";

        String MEAN_EDGE_DISTANCE_PX = "MEAN_EDGE_DISTANCE (PX)";
        String MEAN_EDGE_DISTANCE_CAL = "MEAN_EDGE_DISTANCE (CAL)";
        String STD_EDGE_DISTANCE_PX = "STD_EDGE_DISTANCE (PX)";
        String STD_EDGE_DISTANCE_CAL = "STD_EDGE_DISTANCE (CAL)";

    }

    public interface EdgeDistanceModes extends MeasureIntensityDistribution.EdgeDistanceModes {}


    public static String getFullName(String imageName, String measurement) {
        return "INTENSITY//"+imageName+"_"+measurement;
    }

    private void measureIntensity(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics object to store pixel intensities
        CumStat cs = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getXCoords();
        ArrayList<Integer> y = object.getYCoords();
        ArrayList<Integer> z = object.getZCoords();
        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<x.size();i++) {
            ipl.setPosition(1,z.get(i)+1,tPos+1);
            cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

        }

        // Calculating mean, std, min and max intensity
        if (parameters.getValue(MEASURE_MEAN))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MEAN), cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MIN), cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MAX), cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
        if (parameters.getValue(MEASURE_SUM))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.SUM), cs.getSum()));

    }

    private void measureWeightedCentre(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics objects to store pixel intensities in each direction.
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getXCoords();
        ArrayList<Integer> y = object.getYCoords();
        ArrayList<Integer> z = object.getZCoords();
        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<x.size();i++) {
            ipl.setPosition(1,z.get(i)+1,tPos+1);
            csX.addMeasure(x.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));
            csY.addMeasure(y.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));
            csZ.addMeasure(z.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

        }

        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.X_CENT_MEAN), csX.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.X_CENT_STDEV), csX.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Y_CENT_MEAN), csY.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Y_CENT_STDEV), csY.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Z_CENT_MEAN), csZ.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Z_CENT_STDEV), csZ.getStd()));

    }

    private void measureWeightedEdgeDistance(Obj object, Image image) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);
        String edgeDistanceMode = parameters.getValue(EDGE_DISTANCE_MODE);

        ObjCollection collection = new ObjCollection(object.getName());
        collection.add(object);
        CumStat cs = MeasureIntensityDistribution.measureIntensityWeightedProximity(collection,image,edgeDistanceMode);

        double distPerPxXY = object.getDistPerPxXY();

        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN_EDGE_DISTANCE_PX), cs.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN_EDGE_DISTANCE_CAL), cs.getMean()*distPerPxXY));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.STD_EDGE_DISTANCE_PX), cs.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.STD_EDGE_DISTANCE_CAL), cs.getStd()*distPerPxXY));

    }

    private void measureEdgeIntensityProfile(Obj object, ImagePlus ipl) {
        // Getting parameters
        double minDist = parameters.getValue(MINIMUM_DISTANCE);
        double maxDist = parameters.getValue(MAXIMUM_DISTANCE);
        boolean calibratedDistances = parameters.getValue(CALIBRATED_DISTANCES);
        int nMeasurements = parameters.getValue(NUMBER_OF_MEASUREMENTS);

        // If distances are calibrated, converting them to pixel  units
        if (calibratedDistances) {
            minDist = minDist/object.getDistPerPxXY();
            maxDist = maxDist/object.getDistPerPxXY();
        }

        // Creating an object image
        ImagePlus objIpl = object.convertObjToImage("Inside dist", ipl).getImagePlus();
        objIpl.show();

        // Calculating the distance maps.  The inside map is set to negative
        ImagePlus outsideDistIpl = BinaryOperations.applyDistanceMap3D(objIpl,false,true);
        outsideDistIpl.show();

        IJ.runMacro("waitForUser");

    }

    @Override
    public String getTitle() {
        return "Measure object intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(imageName);
        ImagePlus ipl = image.getImagePlus();

        // Measuring intensity for each object and adding the measurement to that object
        for (Obj object:objects.values()) measureIntensity(object,ipl);

        // If specified, measuring weighted centre for intensity
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            for (Obj object:objects.values()) measureWeightedCentre(object,ipl);
        }

        // If specified, measuring weighted distance to the object edge
        if (parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            for (Obj object:objects.values()) measureWeightedEdgeDistance(object,image);
        }

        // If specified, measuring intensity profiles relative to the object edge
        if (parameters.getValue(MEASURE_EDGE_INTENSITY_PROFILE)) {
            for (Obj object:objects.values()) measureEdgeIntensityProfile(object,ipl);
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_SUM, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_WEIGHTED_CENTRE, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_WEIGHTED_EDGE_DISTANCE, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(EDGE_DISTANCE_MODE,Parameter.CHOICE_ARRAY,EdgeDistanceModes.INSIDE_AND_OUTSIDE,EdgeDistanceModes.ALL));
        parameters.add(new Parameter(MEASURE_EDGE_INTENSITY_PROFILE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(MINIMUM_DISTANCE,Parameter.DOUBLE,0d));
        parameters.add(new Parameter(MAXIMUM_DISTANCE,Parameter.DOUBLE,1d));
        parameters.add(new Parameter(CALIBRATED_DISTANCES, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(NUMBER_OF_MEASUREMENTS,Parameter.INTEGER,10));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STDEV));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SUM));

        objectMeasurementReferences.add(new MeasurementReference(Measurements.X_CENT_MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.X_CENT_STDEV));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Y_CENT_MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Y_CENT_STDEV));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Z_CENT_MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Z_CENT_STDEV));

        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_EDGE_DISTANCE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_EDGE_DISTANCE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_EDGE_DISTANCE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_EDGE_DISTANCE_CAL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(MEASURE_MEAN));
        returnedParameters.add(parameters.getParameter(MEASURE_MIN));
        returnedParameters.add(parameters.getParameter(MEASURE_MAX));
        returnedParameters.add(parameters.getParameter(MEASURE_STDEV));
        returnedParameters.add(parameters.getParameter(MEASURE_SUM));
        returnedParameters.add(parameters.getParameter(MEASURE_WEIGHTED_CENTRE));
        returnedParameters.add(parameters.getParameter(MEASURE_WEIGHTED_EDGE_DISTANCE));

        if (parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            returnedParameters.add(parameters.getParameter(EDGE_DISTANCE_MODE));
        }

        returnedParameters.add(parameters.getParameter(MEASURE_EDGE_INTENSITY_PROFILE));
        if (parameters.getValue(MEASURE_EDGE_INTENSITY_PROFILE)) {
            returnedParameters.add(parameters.getParameter(MINIMUM_DISTANCE));
            returnedParameters.add(parameters.getParameter(MAXIMUM_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));
            returnedParameters.add(parameters.getParameter(NUMBER_OF_MEASUREMENTS));
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        MeasurementReference mean = objectMeasurementReferences.get(Measurements.MEAN);
        mean.setImageObjName(inputObjectsName);
        mean.setCalculated(false);
        if (parameters.getValue(MEASURE_MEAN)) {
            mean.setCalculated(true);
            mean.setNickName(getFullName(inputImageName, Measurements.MEAN));
        }

        MeasurementReference min = objectMeasurementReferences.get(Measurements.MIN);
        min.setImageObjName(inputObjectsName);
        min.setCalculated(false);
        if (parameters.getValue(MEASURE_MIN)) {
            min.setCalculated(true);
            min.setNickName(getFullName(inputImageName, Measurements.MIN));
        }

        MeasurementReference max = objectMeasurementReferences.get(Measurements.MAX);
        max.setImageObjName(inputObjectsName);
        max.setCalculated(false);
        if (parameters.getValue(MEASURE_MAX)) {
            max.setCalculated(true);
            max.setNickName(getFullName(inputImageName, Measurements.MAX));
        }


        MeasurementReference stdev = objectMeasurementReferences.get(Measurements.STDEV);
        stdev.setImageObjName(inputObjectsName);
        stdev.setCalculated(false);
        if (parameters.getValue(MEASURE_STDEV)) {
            stdev.setCalculated(true);
            stdev.setNickName(getFullName(inputImageName, Measurements.STDEV));
        }

        MeasurementReference sum = objectMeasurementReferences.get(Measurements.SUM);
        sum.setImageObjName(inputObjectsName);
        sum.setCalculated(false);
        if (parameters.getValue(MEASURE_SUM)) {
            sum.setCalculated(true);
            sum.setNickName(getFullName(inputImageName, Measurements.SUM));
        }

        MeasurementReference xCentMean = objectMeasurementReferences.get(Measurements.X_CENT_MEAN);
        xCentMean.setImageObjName(inputObjectsName);
        xCentMean.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            xCentMean.setCalculated(true);
            xCentMean.setNickName(getFullName(inputImageName,Measurements.X_CENT_MEAN));
        }

        MeasurementReference xCentStdev = objectMeasurementReferences.get(Measurements.X_CENT_STDEV);
        xCentStdev.setImageObjName(inputObjectsName);
        xCentStdev.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            xCentStdev.setCalculated(true);
            xCentStdev.setNickName(getFullName(inputImageName,Measurements.X_CENT_STDEV));
        }

        MeasurementReference yCentMean = objectMeasurementReferences.get(Measurements.Y_CENT_MEAN);
        yCentMean.setImageObjName(inputObjectsName);
        yCentMean.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            yCentMean.setCalculated(true);
            yCentMean.setNickName(getFullName(inputImageName,Measurements.Y_CENT_MEAN));
        }

        MeasurementReference yCentStdev = objectMeasurementReferences.get(Measurements.Y_CENT_STDEV);
        yCentStdev.setImageObjName(inputObjectsName);
        yCentStdev.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            yCentStdev.setCalculated(true);
            yCentStdev.setNickName(getFullName(inputImageName,Measurements.Y_CENT_STDEV));
        }

        MeasurementReference zCentMean = objectMeasurementReferences.get(Measurements.Z_CENT_MEAN);
        zCentMean.setImageObjName(inputObjectsName);
        zCentMean.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            zCentMean.setCalculated(true);
            zCentMean.setNickName(getFullName(inputImageName,Measurements.Z_CENT_MEAN));
        }

        MeasurementReference zCentStdev = objectMeasurementReferences.get(Measurements.Z_CENT_STDEV);
        zCentStdev.setImageObjName(inputObjectsName);
        zCentStdev.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            zCentStdev.setCalculated(true);
            zCentStdev.setNickName(getFullName(inputImageName,Measurements.Z_CENT_STDEV));
        }

        MeasurementReference meanEdgeDistPx = objectMeasurementReferences.get(Measurements.MEAN_EDGE_DISTANCE_PX);
        meanEdgeDistPx.setImageObjName(inputObjectsName);
        meanEdgeDistPx.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            meanEdgeDistPx.setCalculated(true);
            meanEdgeDistPx.setNickName(getFullName(inputImageName,Measurements.MEAN_EDGE_DISTANCE_PX));
        }

        MeasurementReference meanEdgeDistCal = objectMeasurementReferences.get(Measurements.MEAN_EDGE_DISTANCE_CAL);
        meanEdgeDistCal.setImageObjName(inputObjectsName);
        meanEdgeDistCal.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            meanEdgeDistCal.setCalculated(true);
            meanEdgeDistCal.setNickName(getFullName(inputImageName,Measurements.MEAN_EDGE_DISTANCE_CAL));
        }

        MeasurementReference stdevEdgeDistPx = objectMeasurementReferences.get(Measurements.STD_EDGE_DISTANCE_PX);
        stdevEdgeDistPx.setImageObjName(inputObjectsName);
        stdevEdgeDistPx.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            stdevEdgeDistPx.setCalculated(true);
            stdevEdgeDistPx.setNickName(getFullName(inputImageName,Measurements.STD_EDGE_DISTANCE_PX));
        }

        MeasurementReference stdevEdgeDistCal = objectMeasurementReferences.get(Measurements.STD_EDGE_DISTANCE_CAL);
        stdevEdgeDistCal.setImageObjName(inputObjectsName);
        stdevEdgeDistCal.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            stdevEdgeDistCal.setCalculated(true);
            stdevEdgeDistCal.setNickName(getFullName(inputImageName,Measurements.STD_EDGE_DISTANCE_CAL));
        }

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
