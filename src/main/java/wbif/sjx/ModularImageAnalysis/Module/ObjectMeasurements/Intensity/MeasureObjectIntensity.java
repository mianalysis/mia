// TODO: Could add an optional parameter to select the channel of the input image to use for measurement

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

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
//        String MEAN_EDGE_DISTANCE_CAL = "MEAN_EDGE_DISTANCE (CAL)";
        String STD_EDGE_DISTANCE_PX = "STD_EDGE_DISTANCE (PX)";
//        String STD_EDGE_DISTANCE_CAL = "STD_EDGE_DISTANCE (CAL)";

    }

    public interface EdgeDistanceModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside";
        String INSIDE_ONLY = "Inside only";
        String OUTSIDE_ONLY = "Outside only";

        String[] ALL = new String[]{INSIDE_AND_OUTSIDE,INSIDE_ONLY,OUTSIDE_ONLY};

    }


    private String getFullName(String imageName, String measurement) {
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

    private void measureWeightedEdgeDistance(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);
        String edgeDistanceMode = parameters.getValue(EDGE_DISTANCE_MODE);

        // Duplicating the input image, so it isn't altered
        ImagePlus intensityIpl = new Duplicator().run(ipl);

        // Getting a binary mask of the object
        ImagePlus distIpl = object.getAsImage("Binary").getImagePlus();
        InvertIntensity.process(distIpl);

        // If only the inside or outside intensity is to be considered, making a copy of the binary mask
        ImagePlus binIpl = null;
        if (edgeDistanceMode.equals(EdgeDistanceModes.INSIDE_ONLY) || edgeDistanceMode.equals(EdgeDistanceModes.OUTSIDE_ONLY)) {
            binIpl = new Duplicator().run(distIpl);
        }
        binIpl.show();

        // Calculating the distance map
        BinaryOperations.applyDistanceMap3D(distIpl,true);

        CumStat cs = new CumStat();

        for (int z = 0; z < distIpl.getNSlices(); z++) {
            for (int c = 0; c < distIpl.getNChannels(); c++) {
                for (int t = 0; t < distIpl.getNFrames(); t++) {
                    distIpl.setPosition(c+1, z+1, t+1);
                    intensityIpl.setPosition(c+1, z+1, t+1);

                    float[][] distVals = distIpl.getProcessor().getFloatArray();
                    float[][] inputVals = intensityIpl.getProcessor().getFloatArray();

                    for (int x=0;x<distVals.length;x++) {
                        for (int y=0;y<distVals[0].length;y++) {
                            float dist = distVals[x][y];
                            float val = inputVals[x][y];

                            if (edgeDistanceMode.equals(EdgeDistanceModes.INSIDE_ONLY)) {
                                binIpl.setPosition(c+1, z+1, t+1);
                                if (binIpl.getProcessor().getPixel(x,y) == 255) continue;
                            }

                            if (edgeDistanceMode.equals(EdgeDistanceModes.OUTSIDE_ONLY)) {
                                binIpl.setPosition(c+1, z+1, t+1);
                                if (binIpl.getProcessor().getPixel(x,y) == 0) continue;
                            }

                            cs.addMeasure(dist,val);

                        }
                    }
                }
            }
        }

        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN_EDGE_DISTANCE_PX), cs.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.STD_EDGE_DISTANCE_PX), cs.getStd()));

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
            for (Obj object:objects.values()) measureWeightedEdgeDistance(object,ipl);
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

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
