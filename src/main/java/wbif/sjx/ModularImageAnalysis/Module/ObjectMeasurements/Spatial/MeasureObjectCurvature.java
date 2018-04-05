package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Analysis.CurvatureCalculator;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Vertex;
import wbif.sjx.common.Process.SkeletonTools.Skeleton;

import java.util.*;

/**
 * Created by sc13967 on 24/01/2018.
 */
public class MeasureObjectCurvature extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String RELATE_TO_REFERENCE_POINT = "Relate to reference point";
    public static final String X_REF_MEASUREMENT = "X-axis reference measurement";
    public static final String Y_REF_MEASUREMENT = "Y-axis reference measurement";
    public static final String FIT_SPLINE = "Fit spline";
    public static final String SPLINE_FITTING_METHOD = "Spline fitting method";
    public static final String N_NEIGHBOURS = "Number of neighbours (smoothing)";
    public static final String ITERATIONS = "Iterations";
    public static final String ACCURACY = "Accuracy";
    public static final String ABSOLUTE_CURVATURE = "Measure absolute curvature";
    public static final String SIGNED_CURVATURE = "Measure signed curvature";
    public static final String DRAW_SPLINE = "Draw spline";
    public static final String MAX_CURVATURE = "Maximum curvature (for colour)";
    public static final String APPLY_TO_IMAGE = "Apply to image";
    public static final String SHOW_IMAGE = "Show image";
    public static final String CALCULATE_END_END_ANGLE = "Calculate angle between ends";
    public static final String FITTING_RANGE_PX = "Fitting range (px)";

    interface SplineFittingMethods {
        String LOESS = "LOESS (smooth fitting)";
        String STANDARD = "Standard (fits all points)";

        String[] ALL = new String[]{LOESS,STANDARD};

    }

    public interface Measurements {
        String MEAN_ABSOLUTE_CURVATURE_PX = "CURVATURE//MEAN_ABSOLUTE_CURVATURE_PX";
        String MIN_ABSOLUTE_CURVATURE_PX = "CURVATURE//MIN_ABSOLUTE_CURVATURE_PX";
        String MAX_ABSOLUTE_CURVATURE_PX = "CURVATURE//MAX_ABSOLUTE_CURVATURE_PX";
        String STD_ABSOLUTE_CURVATURE_PX = "CURVATURE//STD_ABSOLUTE_CURVATURE_PX";
        String MEAN_ABSOLUTE_CURVATURE_CAL = "CURVATURE//MEAN_ABSOLUTE_CURVATURE_CAL";
        String MIN_ABSOLUTE_CURVATURE_CAL = "CURVATURE//MIN_ABSOLUTE_CURVATURE_CAL";
        String MAX_ABSOLUTE_CURVATURE_CAL = "CURVATURE//MAX_ABSOLUTE_CURVATURE_CAL";
        String STD_ABSOLUTE_CURVATURE_CAL = "CURVATURE//STD_ABSOLUTE_CURVATURE_CAL";
        String MEAN_SIGNED_CURVATURE_PX = "CURVATURE//MEAN_SIGNED_CURVATURE_PX";
        String MIN_SIGNED_CURVATURE_PX = "CURVATURE//MIN_SIGNED_CURVATURE_PX";
        String MAX_SIGNED_CURVATURE_PX = "CURVATURE//MAX_SIGNED_CURVATURE_PX";
        String STD_SIGNED_CURVATURE_PX = "CURVATURE//STD_SIGNED_CURVATURE_PX";
        String MEAN_SIGNED_CURVATURE_CAL = "CURVATURE//MEAN_SIGNED_CURVATURE_CAL";
        String MIN_SIGNED_CURVATURE_CAL = "CURVATURE//MIN_SIGNED_CURVATURE_CAL";
        String MAX_SIGNED_CURVATURE_CAL = "CURVATURE//MAX_SIGNED_CURVATURE_CAL";
        String STD_SIGNED_CURVATURE_CAL = "CURVATURE//STD_SIGNED_CURVATURE_CAL";
        String SPLINE_LENGTH_PX = "CURVATURE//SPLINE_LENGTH_PX";
        String SPLINE_LENGTH_CAL = "CURVATURE//SPLINE_LENGTH_CAL";
        String FIRST_POINT_X_PX = "CURVATURE//FIRST_POINT_X_PX";
        String FIRST_POINT_Y_PX = "CURVATURE//FIRST_POINT_Y_PX";
        String REL_LOC_OF_MIN_CURVATURE = "CURVATURE//REL_LOC_OF_MIN_CURVATURE";
        String REL_LOC_OF_MAX_CURVATURE = "CURVATURE//REL_LOC_OF_MAX_CURVATURE";
        String HEAD_TAIL_ANGLE_DEGS = "CURVATURE//HEAD_TAIL_ANGLE_DEGS";

    }


    public static LinkedHashSet<Vertex> getSkeletonBackbone(Obj inputObject, ImagePlus templateImage) {
        // Converting object to image, then inverting, so we have a black object on a white background
        ObjCollection tempObjects = new ObjCollection("Backbone");
        tempObjects.add(inputObject);

        HashMap<Integer,Float> hues = tempObjects.getHue(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        ImagePlus objectIpl = tempObjects.convertObjectsToImage("Objects", templateImage, ConvertObjectsToImage.ColourModes.SINGLE_COLOUR, hues, false).getImagePlus();
        InvertIntensity.process(objectIpl);

        // Skeletonise fish to get single backbone
        BinaryOperations.applyStockBinaryTransform(objectIpl, BinaryOperations.OperationModes.SKELETONISE_2D, 1);

        // Using the Common library's Skeleton tools to extract the longest branch.  This requires coordinates for the
        Skeleton skeleton = new Skeleton(objectIpl);
        return skeleton.getLongestPath();
    }

    /**
     * Checks if the longest path (skeleton backbone) needs to be inverted to have the first point closer to the
     * reference than the last point.
     * @param longestPath
     * @param xRef
     * @param yRef
     * @return
     */
    public static boolean testForPathInversion(LinkedHashSet<Vertex> longestPath, double xRef, double yRef) {
        Point<Integer> referencePoint = new Point<Integer>((int) xRef,(int) yRef, 0);
        Iterator<Vertex> iterator = longestPath.iterator();

        double firstPointDistance = iterator.next().calculateDistanceToPoint(referencePoint);
        double lastPointDistance = Double.MAX_VALUE;

        while (iterator.hasNext()) {
            Vertex nextVertex = iterator.next();

            // Only calculate the distance for the final point
            if (!iterator.hasNext()) lastPointDistance = nextVertex.calculateDistanceToPoint(referencePoint);
        }

        // If the last point is closer to the reference than the first, return true
        return (lastPointDistance < firstPointDistance);

    }


    public static CurvatureCalculator getCurvatureCalculator(LinkedHashSet<Vertex> longestPath, String splineFittingMethod,
                                                             int nNeighbours, int iterations, double accuracy) {
        // Calculating local curvature along the path
        CurvatureCalculator curvatureCalculator = new CurvatureCalculator(longestPath);
        switch (splineFittingMethod) {
            case SplineFittingMethods.LOESS:
                curvatureCalculator.setLoessNNeighbours(nNeighbours);
                curvatureCalculator.setLoessIterations(iterations);
                curvatureCalculator.setLoessAccuracy(accuracy);
                curvatureCalculator.setFittingMethod(CurvatureCalculator.FittingMethod.LOESS);
                break;

            case SplineFittingMethods.STANDARD:
                curvatureCalculator.setFittingMethod(CurvatureCalculator.FittingMethod.STANDARD);
                break;
        }

        return curvatureCalculator;

    }

    public static void measureCurvature(Obj inputObject, TreeMap<Double,Double> curvature, boolean absoluteCurvature,
                                        boolean signedCurvature) {
        double dppXY = inputObject.getDistPerPxXY();

        CumStat cumStatSigned = new CumStat();
        CumStat cumStatAbsolute = new CumStat();

        for (double value:curvature.values()) {
            if (absoluteCurvature) cumStatAbsolute.addMeasure(Math.abs(value));
            if (signedCurvature) cumStatSigned.addMeasure(value);
        }

        // Adding measurements
        if (absoluteCurvature) {
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getMean()));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getMin()));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getMax()));
            inputObject.addMeasurement(new Measurement(Measurements.STD_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getStd()));
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getMean() / dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getMin() / dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getMax() / dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.STD_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getStd() / dppXY));
        }

        if (signedCurvature) {
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_SIGNED_CURVATURE_PX, cumStatSigned.getMean()));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_SIGNED_CURVATURE_PX, cumStatSigned.getMin()));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_SIGNED_CURVATURE_PX, cumStatSigned.getMax()));
            inputObject.addMeasurement(new Measurement(Measurements.STD_SIGNED_CURVATURE_PX, cumStatSigned.getStd()));
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_SIGNED_CURVATURE_CAL, cumStatSigned.getMean() / dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_SIGNED_CURVATURE_CAL, cumStatSigned.getMin() / dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_SIGNED_CURVATURE_CAL, cumStatSigned.getMax() / dppXY));
            inputObject.addMeasurement(new Measurement(Measurements.STD_SIGNED_CURVATURE_CAL, cumStatSigned.getStd() / dppXY));
        }
    }

    public static void measureRelativeCurvature(Obj inputObject, LinkedHashSet<Vertex> longestPath,
                                          TreeMap<Double,Double> curvature, boolean useReference) {
        double pathLength = 0;
        double posMin = 0;
        double posMax = 0;
        double minCurvature = Double.MAX_VALUE;
        double maxCurvature = Double.MIN_VALUE;

        double dppXY = inputObject.getDistPerPxXY();

        Iterator<Double> iterator = curvature.keySet().iterator();
        while (iterator.hasNext()) {
            pathLength = iterator.next();

            // Only evaluate the following if the points are ordered relative to a reference
            if (!useReference) continue;
            double localCurvature = curvature.get(pathLength);

            if (localCurvature < minCurvature) {
                minCurvature = localCurvature;
                posMin = pathLength;
            }
            if (localCurvature > maxCurvature) {
                maxCurvature = localCurvature;
                posMax = pathLength;
            }
        }

        inputObject.addMeasurement(new Measurement(Measurements.SPLINE_LENGTH_PX,pathLength));
        inputObject.addMeasurement(new Measurement(Measurements.SPLINE_LENGTH_CAL,pathLength*dppXY));

        if (useReference) {
            Vertex firstPoint = longestPath.iterator().next();

            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_X_PX, firstPoint.getX()));
            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_Y_PX, firstPoint.getY()));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MIN_CURVATURE, (posMin / pathLength)));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MAX_CURVATURE, (posMax / pathLength)));

        } else {
            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_X_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_Y_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MIN_CURVATURE, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MAX_CURVATURE, Double.NaN));

        }
    }

    public static void measureHeadTailAngle(Obj inputObject, LinkedHashSet<Vertex> longestPath, int nPoints) {
        // Getting starting and ending points for comparison
        double startX1 = 0,startX2 = 0,startY1 = 0,startY2 = 0;
        double endX1 = 0, endX2 = 0,endY1 = 0,endY2 = 0;

        int pathLength = longestPath.size();

        // If the path is too short for the fitting range
        if (pathLength < nPoints) {
            inputObject.addMeasurement(new Measurement(Measurements.HEAD_TAIL_ANGLE_DEGS,Double.NaN));
            return;
        }

        int count = 0;
        for (Vertex vertex:longestPath) {
            if (count == 0) {
                startX1 = vertex.getX();
                startY1 = vertex.getY();

            } else if (count == nPoints-1) {
                startX2  = vertex.getX();
                startY2 = vertex.getY();

            } else if (count == pathLength-nPoints) {
                endX1 = vertex.getX();
                endY1 = vertex.getY();

            } else if (count == pathLength-1) {
                endX2 = vertex.getX();
                endY2  = vertex.getY();

            }
            count++;
        }

        Vector2D startVector = new Vector2D(startX2-startX1,startY2-startY1);
        Vector2D endVector = new Vector2D(endX2-endX1,endY2-endY1);

        double angleDegs = Math.toDegrees(Vector2D.angle(startVector,endVector));

        inputObject.addMeasurement(new Measurement(Measurements.HEAD_TAIL_ANGLE_DEGS,angleDegs));

    }

    @Override
    public String getTitle() {
        return "Measure object curvature";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        Image referenceImage = workspace.getImage(referenceImageName);
        ImagePlus referenceImageImagePlus = referenceImage.getImagePlus();

        // Getting parameters
        boolean useReference = parameters.getValue(RELATE_TO_REFERENCE_POINT);
        String xReference = parameters.getValue(X_REF_MEASUREMENT);
        String yReference = parameters.getValue(Y_REF_MEASUREMENT);
        boolean fitSpline = parameters.getValue(FIT_SPLINE);
        String splineFittingMethod = parameters.getValue(SPLINE_FITTING_METHOD);
        int nNeighbours = parameters.getValue(N_NEIGHBOURS);
        int iterations = parameters.getValue(ITERATIONS);
        double accuracy = parameters.getValue(ACCURACY);
        boolean absoluteCurvature = parameters.getValue(ABSOLUTE_CURVATURE);
        boolean signedCurvature = parameters.getValue(SIGNED_CURVATURE);
        boolean showSplines = parameters.getValue(DRAW_SPLINE);
        boolean applyToImage = parameters.getValue(APPLY_TO_IMAGE);
        double maxCurvature = parameters.getValue(MAX_CURVATURE);
        boolean showImage = parameters.getValue(SHOW_IMAGE);
        boolean calculateEndEndAngle = parameters.getValue(CALCULATE_END_END_ANGLE);
        int fittingRange = parameters.getValue(FITTING_RANGE_PX);

        // If no reference is provided there's nothing to tell the sign of the curvature
        if (!useReference) {
            absoluteCurvature = true;
            signedCurvature = false;
        }

        if (showSplines &! applyToImage) {
            referenceImageImagePlus = new Duplicator().run(referenceImageImagePlus);
        }

        ImagePlus templateImage = IJ.createImage("Template",referenceImageImagePlus.getWidth(),referenceImageImagePlus.getHeight(),1,8);
        int count = 1;
        int total = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Processing object " + (count++) + " of " + total);

            // Getting the backbone of the object
            LinkedHashSet<Vertex> longestPath = getSkeletonBackbone(inputObject, templateImage);

            // If necessary, inverting the longest path so the first point is closest to the reference
            if (useReference) {
                // Checking the point has a reference (some may not)
                if (Double.isNaN(inputObject.getMeasurement(xReference).getValue())) continue;

                double xRef = inputObject.getMeasurement(xReference).getValue();
                double yRef = inputObject.getMeasurement(yReference).getValue();

                if (testForPathInversion(longestPath, xRef, yRef)) {
                    // Store the longest path in a list, then iterate through this backwards
                    LinkedList<Vertex> temporaryPathList = new LinkedList<>(longestPath);
                    Iterator<Vertex> reverseIterator = temporaryPathList.descendingIterator();

                    longestPath = new LinkedHashSet<>();
                    while (reverseIterator.hasNext()) {
                        longestPath.add(reverseIterator.next());
                    }
                }
            }

            if (fitSpline) {
                CurvatureCalculator calculator = getCurvatureCalculator(longestPath, splineFittingMethod, nNeighbours, iterations, accuracy);
                TreeMap<Double,Double> curvature = calculator.getCurvature();
                measureCurvature(inputObject, curvature, absoluteCurvature, signedCurvature);
                measureRelativeCurvature(inputObject, longestPath, curvature, useReference);

                if (showSplines) {
                    int[] position = new int[]{1,(int) (inputObject.getZ(false,false)[0]+1),(inputObject.getT()+1)};
                    referenceImageImagePlus.setPosition(1,(int) (inputObject.getZ(false,false)[0]+1),inputObject.getT()+1);
                    calculator.showOverlay(referenceImageImagePlus, maxCurvature, position);
                }
            }

            if (calculateEndEndAngle) measureHeadTailAngle(inputObject, longestPath, fittingRange);

        }

        if (showImage) new Duplicator().run(referenceImageImagePlus).show();

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(REFERENCE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(RELATE_TO_REFERENCE_POINT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(X_REF_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(Y_REF_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(FIT_SPLINE, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(SPLINE_FITTING_METHOD, Parameter.CHOICE_ARRAY,SplineFittingMethods.LOESS,SplineFittingMethods.ALL));
        parameters.add(new Parameter(N_NEIGHBOURS, Parameter.INTEGER,20));
        parameters.add(new Parameter(ITERATIONS, Parameter.INTEGER,10));
        parameters.add(new Parameter(ACCURACY, Parameter.DOUBLE,1d));
        parameters.add(new Parameter(ABSOLUTE_CURVATURE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(SIGNED_CURVATURE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(DRAW_SPLINE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(APPLY_TO_IMAGE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MAX_CURVATURE,Parameter.DOUBLE,1d));
        parameters.add(new Parameter(SHOW_IMAGE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_END_END_ANGLE, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(FITTING_RANGE_PX, Parameter.INTEGER,5));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_ABSOLUTE_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN_ABSOLUTE_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX_ABSOLUTE_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_ABSOLUTE_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_ABSOLUTE_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN_ABSOLUTE_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX_ABSOLUTE_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_ABSOLUTE_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_SIGNED_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN_SIGNED_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX_SIGNED_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_SIGNED_CURVATURE_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_SIGNED_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN_SIGNED_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX_SIGNED_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STD_SIGNED_CURVATURE_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SPLINE_LENGTH_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SPLINE_LENGTH_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.FIRST_POINT_X_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.FIRST_POINT_Y_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.REL_LOC_OF_MAX_CURVATURE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.REL_LOC_OF_MIN_CURVATURE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.HEAD_TAIL_ANGLE_DEGS));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));

        returnedParameters.add(parameters.getParameter(RELATE_TO_REFERENCE_POINT));
        if (parameters.getValue(RELATE_TO_REFERENCE_POINT)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

            parameters.updateValueSource(X_REF_MEASUREMENT,inputObjectsName);
            parameters.updateValueSource(Y_REF_MEASUREMENT,inputObjectsName);

            returnedParameters.add(parameters.getParameter(X_REF_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(Y_REF_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(ABSOLUTE_CURVATURE));
            returnedParameters.add(parameters.getParameter(SIGNED_CURVATURE));

        }

        returnedParameters.add(parameters.getParameter(FIT_SPLINE));
        if (parameters.getValue(FIT_SPLINE)) {
            returnedParameters.add(parameters.getParameter(SPLINE_FITTING_METHOD));
            switch ((String) parameters.getValue(SPLINE_FITTING_METHOD)) {
                case SplineFittingMethods.LOESS:
                    returnedParameters.add(parameters.getParameter(N_NEIGHBOURS));
                    returnedParameters.add(parameters.getParameter(ITERATIONS));
                    returnedParameters.add(parameters.getParameter(ACCURACY));
                    break;
            }

            returnedParameters.add(parameters.getParameter(DRAW_SPLINE));
            if (parameters.getValue(DRAW_SPLINE)) {
                returnedParameters.add(parameters.getParameter(APPLY_TO_IMAGE));
                returnedParameters.add(parameters.getParameter(MAX_CURVATURE));
                returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(CALCULATE_END_END_ANGLE));
        if (parameters.getValue(CALCULATE_END_END_ANGLE)) {
            returnedParameters.add(parameters.getParameter(FITTING_RANGE_PX));
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

        MeasurementReference meanCurvatureAbsolutePx = objectMeasurementReferences.get(Measurements.MEAN_ABSOLUTE_CURVATURE_PX);
        MeasurementReference minCurvatureAbsolutePx = objectMeasurementReferences.get(Measurements.MIN_ABSOLUTE_CURVATURE_PX);
        MeasurementReference maxCurvatureAbsolutePx = objectMeasurementReferences.get(Measurements.MAX_ABSOLUTE_CURVATURE_PX);
        MeasurementReference stdCurvatureAbsolutePx = objectMeasurementReferences.get(Measurements.STD_ABSOLUTE_CURVATURE_PX);
        MeasurementReference meanCurvatureAbsoluteCal = objectMeasurementReferences.get(Measurements.MEAN_ABSOLUTE_CURVATURE_CAL);
        MeasurementReference minCurvatureAbsoluteCal = objectMeasurementReferences.get(Measurements.MIN_ABSOLUTE_CURVATURE_CAL);
        MeasurementReference maxCurvatureAbsoluteCal = objectMeasurementReferences.get(Measurements.MAX_ABSOLUTE_CURVATURE_CAL);
        MeasurementReference stdCurvatureAbsoluteCal = objectMeasurementReferences.get(Measurements.STD_ABSOLUTE_CURVATURE_CAL);
        MeasurementReference meanCurvatureSignedPx = objectMeasurementReferences.get(Measurements.MEAN_SIGNED_CURVATURE_PX);
        MeasurementReference minCurvatureSignedPx = objectMeasurementReferences.get(Measurements.MIN_SIGNED_CURVATURE_PX);
        MeasurementReference maxCurvatureSignedPx = objectMeasurementReferences.get(Measurements.MAX_SIGNED_CURVATURE_PX);
        MeasurementReference stdCurvatureSignedPx = objectMeasurementReferences.get(Measurements.STD_SIGNED_CURVATURE_PX);
        MeasurementReference meanCurvatureSignedCal = objectMeasurementReferences.get(Measurements.MEAN_SIGNED_CURVATURE_CAL);
        MeasurementReference minCurvatureSignedCal = objectMeasurementReferences.get(Measurements.MIN_SIGNED_CURVATURE_CAL);
        MeasurementReference maxCurvatureSignedCal = objectMeasurementReferences.get(Measurements.MAX_SIGNED_CURVATURE_CAL);
        MeasurementReference stdCurvatureSignedCal = objectMeasurementReferences.get(Measurements.STD_SIGNED_CURVATURE_CAL);
        MeasurementReference splineLengthPx = objectMeasurementReferences.get(Measurements.SPLINE_LENGTH_PX);
        MeasurementReference splineLengthCal = objectMeasurementReferences.get(Measurements.SPLINE_LENGTH_CAL);
        MeasurementReference firstPointX = objectMeasurementReferences.get(Measurements.FIRST_POINT_X_PX);
        MeasurementReference firstPointY = objectMeasurementReferences.get(Measurements.FIRST_POINT_Y_PX);
        MeasurementReference relLocMinCurvature = objectMeasurementReferences.get(Measurements.REL_LOC_OF_MIN_CURVATURE);
        MeasurementReference relLocMaxCurvature= objectMeasurementReferences.get(Measurements.REL_LOC_OF_MAX_CURVATURE);
        MeasurementReference headTailAngle = objectMeasurementReferences.get(Measurements.HEAD_TAIL_ANGLE_DEGS);

        meanCurvatureAbsolutePx.setImageObjName(inputObjectsName);
        minCurvatureAbsolutePx.setImageObjName(inputObjectsName);
        maxCurvatureAbsolutePx.setImageObjName(inputObjectsName);
        stdCurvatureAbsolutePx.setImageObjName(inputObjectsName);
        meanCurvatureAbsoluteCal.setImageObjName(inputObjectsName);
        minCurvatureAbsoluteCal.setImageObjName(inputObjectsName);
        maxCurvatureAbsoluteCal.setImageObjName(inputObjectsName);
        stdCurvatureAbsoluteCal.setImageObjName(inputObjectsName);
        meanCurvatureSignedPx.setImageObjName(inputObjectsName);
        minCurvatureSignedPx.setImageObjName(inputObjectsName);
        maxCurvatureSignedPx.setImageObjName(inputObjectsName);
        stdCurvatureSignedPx.setImageObjName(inputObjectsName);
        meanCurvatureSignedCal.setImageObjName(inputObjectsName);
        minCurvatureSignedCal.setImageObjName(inputObjectsName);
        maxCurvatureSignedCal.setImageObjName(inputObjectsName);
        stdCurvatureSignedCal.setImageObjName(inputObjectsName);
        splineLengthPx.setImageObjName(inputObjectsName);
        splineLengthCal.setImageObjName(inputObjectsName);
        firstPointX.setImageObjName(inputObjectsName);
        firstPointY.setImageObjName(inputObjectsName);
        relLocMinCurvature.setImageObjName(inputObjectsName);
        relLocMaxCurvature.setImageObjName(inputObjectsName);
        headTailAngle.setImageObjName(inputObjectsName);

        boolean relateToReference = false;
        boolean absoluteCurvature = false;
        boolean signedCurvature = false;
        boolean calculateHeadTailAngle = parameters.getValue(CALCULATE_END_END_ANGLE);

        if (parameters.getValue(FIT_SPLINE)) {
            if (parameters.getValue(RELATE_TO_REFERENCE_POINT)) {
                relateToReference = true;
                if (parameters.getValue(ABSOLUTE_CURVATURE)) absoluteCurvature = true;
                if (parameters.getValue(SIGNED_CURVATURE)) signedCurvature = true;

            } else {
                absoluteCurvature = true;
                signedCurvature = false;
            }
        }

        meanCurvatureAbsolutePx.setCalculated(absoluteCurvature);
        minCurvatureAbsolutePx.setCalculated(absoluteCurvature);
        maxCurvatureAbsolutePx.setCalculated(absoluteCurvature);
        stdCurvatureAbsolutePx.setCalculated(absoluteCurvature);
        meanCurvatureAbsoluteCal.setCalculated(absoluteCurvature);
        minCurvatureAbsoluteCal.setCalculated(absoluteCurvature);
        maxCurvatureAbsoluteCal.setCalculated(absoluteCurvature);
        stdCurvatureAbsoluteCal.setCalculated(absoluteCurvature);
        meanCurvatureSignedPx.setCalculated(signedCurvature);
        minCurvatureSignedPx.setCalculated(signedCurvature);
        maxCurvatureSignedPx.setCalculated(signedCurvature);
        stdCurvatureSignedPx.setCalculated(signedCurvature);
        meanCurvatureSignedCal.setCalculated(signedCurvature);
        minCurvatureSignedCal.setCalculated(signedCurvature);
        maxCurvatureSignedCal.setCalculated(signedCurvature);
        stdCurvatureSignedCal.setCalculated(signedCurvature);
        firstPointX.setCalculated(relateToReference);
        firstPointY.setCalculated(relateToReference);
        relLocMinCurvature.setCalculated(relateToReference);
        relLocMaxCurvature.setCalculated(relateToReference);
        headTailAngle.setCalculated(calculateHeadTailAngle);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
