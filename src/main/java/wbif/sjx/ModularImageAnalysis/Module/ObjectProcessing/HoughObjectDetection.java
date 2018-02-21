package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.MathFunc.MidpointCircle;
import wbif.sjx.common.Process.HoughTransform.Transforms.CircleHoughTransform;
import wbif.sjx.common.Process.IntensityMinMax;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sc13967 on 15/01/2018.
 */
public class HoughObjectDetection extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String MIN_RADIUS = "Minimum radius (px)";
    public static final String MAX_RADIUS = "Maximum radius (px)";
    public static final String RANDOM_SAMPLING = "Random sampling (faster, but less accurate)";
    public static final String SAMPLE_FRACTION = "Sample fraction (0 = none, 1 = all)";
    public static final String DETECTION_THRESHOLD = "Detection threshold";
    public static final String EXCLUSION_RADIUS = "Exclusion radius (px)";
    public static final String SHOW_TRANSFORM_IMAGE = "Show transform image";
    public static final String SHOW_OBJECTS = "Show detected objects";
    public static final String SHOW_HOUGH_SCORE = "Show detection score";
    public static final String LABEL_SIZE = "Label size";


    private interface Measurements {
        String SCORE = "HOUGH_DETECTION//SCORE";

    }

    @Override
    public String getTitle() {
        return "Hough-based detection";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        int minR = parameters.getValue(MIN_RADIUS);
        int maxR = parameters.getValue(MAX_RADIUS);
        boolean randomSampling = parameters.getValue(RANDOM_SAMPLING);
        double sampleFraction = parameters.getValue(SAMPLE_FRACTION);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE);
        boolean showObjects = parameters.getValue(SHOW_OBJECTS);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE);
        int labelSize = parameters.getValue(LABEL_SIZE);

        // Storing the image calibration
        Calibration calibration = ipl.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        // Iterating over all images in the ImagePlus
        int count = 1;
        int total = ipl.getNChannels()*ipl.getNSlices()*ipl.getNFrames();

        for (int c=0;c<ipl.getNChannels();c++) {
            for (int z = 0; z < ipl.getNSlices(); z++) {
                for (int t = 0; t < ipl.getNFrames(); t++) {
                    ipl.setPosition(c + 1, z + 1, t + 1);

                    // Initialising the Hough transform
                    int[][] paramRanges = new int[][]{{0,ipl.getWidth() - 1}, {0,ipl.getHeight() - 1}, {minR,maxR}};
                    CircleHoughTransform circleHoughTransform = new CircleHoughTransform(ipl.getProcessor(),paramRanges);
                    circleHoughTransform.setRandomSampling(randomSampling);
                    circleHoughTransform.setSampleFraction(sampleFraction);

                    // Running the transforms
                    writeMessage("Running transform (image " + (count) + " of " + total+")",verbose);
                    circleHoughTransform.run();

                    // Normalising scores based on the number of points in that circle
                    writeMessage("Normalising scores (image " + (count) + " of " + total+")",verbose);
                        circleHoughTransform.normaliseScores();

                    // Getting the accumulator as an image
                    if (showTransformImage) circleHoughTransform.getAccumulatorAsImage().show();

                    // Getting circle objects and adding to workspace
                    writeMessage("Detecting objects (image " + (count++) + " of " + total+")",verbose);
                    ArrayList<double[]> circles = circleHoughTransform.getObjects(detectionThreshold, exclusionRadius);
                    Indexer indexer = new Indexer(ipl.getWidth(), ipl.getHeight());
                    for (double[] circle : circles) {
                        // Initialising the object
                        Obj outputObject = new Obj(outputObjectsName, outputObjects.getNextID(), dppXY, dppZ,
                                calibrationUnits);

                        // Getting circle parameters
                        int x = (int) Math.round(circle[0]);
                        int y = (int) Math.round(circle[1]);
                        int r = (int) Math.round(circle[2]);
                        double score = circle[3];

                        // Getting coordinates corresponding to circle
                        MidpointCircle midpointCircle = new MidpointCircle(r);
                        int[] xx = midpointCircle.getXCircleFill();
                        int[] yy = midpointCircle.getYCircleFill();

                        for (int i = 0; i < xx.length; i++) {
                            int idx = indexer.getIndex(new int[]{xx[i] + x, yy[i] + y});
                            if (idx == -1) continue;

                            outputObject.addCoord(xx[i] + x, yy[i] + y, z);

                        }

                        // Adding measurements
                        outputObject.setT(t);
                        outputObject.addMeasurement(new Measurement(Measurements.SCORE, score));

                        // Adding object to object set
                        outputObjects.add(outputObject);

                    }

                    writeMessage(circles.size()+" circles detected in frame C="+c+", Z="+z+", T="+t,verbose);

                }
            }
        }

        ipl.setPosition(1,1,1);
        workspace.addObjects(outputObjects);

        if (showObjects) {
            ImagePlus dispIpl = new Duplicator().run(ipl);
            IntensityMinMax.run(dispIpl,true);

            String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
            HashMap<Obj,Float> hues = outputObjects.getHue(colourMode,"","",true);

            HashMap<Obj, String> IDs = null;
            if (showHoughScore) {
                String labelMode = ObjCollection.LabelModes.MEASUREMENT_VALUE;
                IDs = outputObjects.getIDs(labelMode, Measurements.SCORE, "",0);
            }
            String positionMode = AddObjectsOverlay.PositionModes.OUTLINE;

            AddObjectsOverlay.createOverlay(dispIpl,outputObjects,positionMode,null,hues,IDs,labelSize);

            dispIpl.show();

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(MIN_RADIUS,Parameter.INTEGER,10));
        parameters.add(new Parameter(MAX_RADIUS,Parameter.INTEGER,20));
        parameters.add(new Parameter(RANDOM_SAMPLING,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SAMPLE_FRACTION,Parameter.DOUBLE,0.5));
        parameters.add(new Parameter(DETECTION_THRESHOLD,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(EXCLUSION_RADIUS,Parameter.INTEGER,10));
        parameters.add(new Parameter(SHOW_TRANSFORM_IMAGE,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SHOW_OBJECTS,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SHOW_HOUGH_SCORE,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(LABEL_SIZE,Parameter.INTEGER,12));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SCORE));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MIN_RADIUS));
        returnedParameters.add(parameters.getParameter(MAX_RADIUS));
        returnedParameters.add(parameters.getParameter(RANDOM_SAMPLING));

        if (parameters.getValue(RANDOM_SAMPLING)) {
            returnedParameters.add(parameters.getParameter(SAMPLE_FRACTION));
        }

        returnedParameters.add(parameters.getParameter(DETECTION_THRESHOLD));
        returnedParameters.add(parameters.getParameter(EXCLUSION_RADIUS));
        returnedParameters.add(parameters.getParameter(SHOW_TRANSFORM_IMAGE));
        returnedParameters.add(parameters.getParameter(SHOW_OBJECTS));

        if (parameters.getValue(SHOW_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(SHOW_HOUGH_SCORE));

            if (parameters.getValue(SHOW_HOUGH_SCORE)) {
                returnedParameters.add(parameters.getParameter(LABEL_SIZE));
            }
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        MeasurementReference score = objectMeasurementReferences.get(Measurements.SCORE);
        score.setImageObjName(parameters.getValue(OUTPUT_OBJECTS));

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
