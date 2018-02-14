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
    public static final String NORMALISE_SCORES = "Normalise scores";
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
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        int minRadius = parameters.getValue(MIN_RADIUS);
        int maxRadius = parameters.getValue(MAX_RADIUS);
        boolean normaliseScores = parameters.getValue(NORMALISE_SCORES);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE);
        boolean showObjects = parameters.getValue(SHOW_OBJECTS);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE);
        int labelSize = parameters.getValue(LABEL_SIZE);

        // Storing the image calibration
        Calibration calibration = inputImagePlus.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        // Iterating over all images in the ImagePlus
        int count = 1;
        int total = inputImagePlus.getNChannels()*inputImagePlus.getNSlices()*inputImagePlus.getNFrames();

        for (int c=0;c<inputImagePlus.getNChannels();c++) {
            for (int z = 0; z < inputImagePlus.getNSlices(); z++) {
                for (int t = 0; t < inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c + 1, z + 1, t + 1);

                    // Initialising the Hough transform
                    int[][] parameterRanges =
                            new int[][]{{0, inputImagePlus.getWidth() - 1}, {0, inputImagePlus.getHeight() - 1},
                                    {minRadius, maxRadius}};
                    CircleHoughTransform circleHoughTransform =
                            new CircleHoughTransform(inputImagePlus.getProcessor(), parameterRanges);

                    // Running the transforms
                    if (verbose) System.out.println(
                            "[" + moduleName + "] Running transform (image " + (count) + " of " + total+")");
                    circleHoughTransform.run();

                    // Normalising scores based on the number of points in that circle
                    if (normaliseScores) {
                        if (verbose) System.out.println(
                                "[" + moduleName + "] Normalising scores (image " + (count) + " of " + total+")");
                        circleHoughTransform.normaliseScores();
                    }

                    // Getting the accumulator as an image
                    if (showTransformImage) circleHoughTransform.getAccumulatorAsImage().show();

                    // Getting circle objects and adding to workspace
                    if (verbose) System.out.println(
                            "[" + moduleName + "] Detecting objects (image " + (count++) + " of " + total+")");
                    ArrayList<double[]> circles = circleHoughTransform.getObjects(detectionThreshold, exclusionRadius);
                    Indexer indexer = new Indexer(inputImagePlus.getWidth(), inputImagePlus.getHeight());
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
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);
        workspace.addObjects(outputObjects);

        if (showObjects) {
            ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
            IntensityMinMax.run(dispIpl,true);

            String colourMode = AddObjectsOverlay.ColourModes.RANDOM_COLOUR;
            HashMap<Obj,Color> colours = AddObjectsOverlay.getColours(outputObjects,colourMode,"","");

            HashMap<Obj, String> IDs = null;
            if (showHoughScore) {
                String labelMode = AddObjectsOverlay.LabelModes.MEASUREMENT_VALUE;
                IDs = AddObjectsOverlay.getIDs(outputObjects, labelMode, Measurements.SCORE, "",0);
            }
            String positionMode = AddObjectsOverlay.PositionModes.OUTLINE;

            AddObjectsOverlay.createOverlay(dispIpl,outputObjects,positionMode,null,colours,IDs,labelSize);

            dispIpl.show();

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(MIN_RADIUS,Parameter.INTEGER,10));
        parameters.add(new Parameter(MAX_RADIUS,Parameter.INTEGER,20));
        parameters.add(new Parameter(NORMALISE_SCORES,Parameter.BOOLEAN,true));
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
        returnedParameters.add(parameters.getParameter(NORMALISE_SCORES));
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
