package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.filter.Convolver;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.measure.region2d.GeodesicDiameter;
import inra.ijpb.measure.region2d.GeodesicDiameter.Result;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageCalculator;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Threshold.ManualThreshold;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Analysis.CurvatureCalculator;
import wbif.sjx.common.MathFunc.BresenhamLine;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Vertex;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.VolumeType;

/**
 * Created by sc13967 on 24/01/2018.
 */
public class FitSpline extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String EXPORT_EVERY_N_POINTS = "Export every N points";
    public static final String RELATE_TO_REFERENCE_POINT = "Relate to reference point";
    public static final String X_REF_MEASUREMENT = "X-axis reference measurement";
    public static final String Y_REF_MEASUREMENT = "Y-axis reference measurement";
    public static final String SPLINE_FITTING_METHOD = "Spline fitting method";
    public static final String N_NEIGHBOURS = "Number of neighbours (smoothing)";
    public static final String ITERATIONS = "Iterations";
    public static final String ACCURACY = "Accuracy";
    public static final String ABSOLUTE_CURVATURE = "Measure absolute curvature";
    public static final String SIGNED_CURVATURE = "Measure signed curvature";
    public static final String DRAW_SPLINE = "Draw spline";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_IMAGE = "Apply to image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String LINE_WIDTH = "Line width";
    public static final String MAX_CURVATURE = "Maximum curvature (for colour)";
    public static final String CALCULATE_END_END_ANGLE = "Calculate angle between ends";
    public static final String FITTING_RANGE_PX = "Fitting range (px)";

    public FitSpline(ModuleCollection modules) {
        super("Fit spline", modules);
    }

    public interface ObjectOutputModes {
        String DO_NOT_STORE = "Do not store";
        String CONTROL_POINTS = "Individual control points";
        String FULL_CONTOUR = "Full contour";

        String[] ALL = new String[] { DO_NOT_STORE, CONTROL_POINTS, FULL_CONTOUR };

    }

    public interface SplineFittingMethods {
        String LOESS = "LOESS (smooth fitting)";
        String STANDARD = "Standard (fits all points)";

        String[] ALL = new String[] { LOESS, STANDARD };

    }

    public interface Measurements {
        String MEAN_ABSOLUTE_CURVATURE_PX = "CURVATURE // MEAN_ABSOLUTE_CURVATURE_(PX^-1)";
        String MIN_ABSOLUTE_CURVATURE_PX = "CURVATURE // MIN_ABSOLUTE_CURVATURE_(PX^-1)";
        String MAX_ABSOLUTE_CURVATURE_PX = "CURVATURE // MAX_ABSOLUTE_CURVATURE_(PX^-1)";
        String STD_ABSOLUTE_CURVATURE_PX = "CURVATURE // STD_ABSOLUTE_CURVATURE_(PX^-1)";
        String MEAN_ABSOLUTE_CURVATURE_CAL = "CURVATURE // MEAN_ABSOLUTE_CURVATURE_(${SCAL}^-1)";
        String MIN_ABSOLUTE_CURVATURE_CAL = "CURVATURE // MIN_ABSOLUTE_CURVATURE_(${SCAL}^-1)";
        String MAX_ABSOLUTE_CURVATURE_CAL = "CURVATURE // MAX_ABSOLUTE_CURVATURE_(${SCAL}^-1)";
        String STD_ABSOLUTE_CURVATURE_CAL = "CURVATURE // STD_ABSOLUTE_CURVATURE_(${SCAL}^-1)";
        String MEAN_SIGNED_CURVATURE_PX = "CURVATURE // MEAN_SIGNED_CURVATURE_(PX^-1)";
        String MIN_SIGNED_CURVATURE_PX = "CURVATURE // MIN_SIGNED_CURVATURE_(PX^-1)";
        String MAX_SIGNED_CURVATURE_PX = "CURVATURE // MAX_SIGNED_CURVATURE_(PX^-1)";
        String STD_SIGNED_CURVATURE_PX = "CURVATURE // STD_SIGNED_CURVATURE_(PX^-1)";
        String MEAN_SIGNED_CURVATURE_CAL = "CURVATURE // MEAN_SIGNED_CURVATURE_(${SCAL}^-1)";
        String MIN_SIGNED_CURVATURE_CAL = "CURVATURE // MIN_SIGNED_CURVATURE_(${SCAL}^-1)";
        String MAX_SIGNED_CURVATURE_CAL = "CURVATURE // MAX_SIGNED_CURVATURE_(${SCAL}^-1)";
        String STD_SIGNED_CURVATURE_CAL = "CURVATURE // STD_SIGNED_CURVATURE_(${SCAL}^-1)";
        String SPLINE_LENGTH_PX = "CURVATURE // SPLINE_LENGTH_(PX)";
        String SPLINE_LENGTH_CAL = "CURVATURE // SPLINE_LENGTH_(${SCAL})";
        String FIRST_POINT_X_PX = "CURVATURE // FIRST_POINT_X_(PX)";
        String FIRST_POINT_Y_PX = "CURVATURE // FIRST_POINT_Y_(PX)";
        String REL_LOC_OF_MIN_CURVATURE = "CURVATURE // REL_LOC_OF_MIN_CURVATURE";
        String REL_LOC_OF_MAX_CURVATURE = "CURVATURE // REL_LOC_OF_MAX_CURVATURE";
        String HEAD_TAIL_ANGLE_DEGS = "CURVATURE // HEAD_TAIL_ANGLE_DEGS";

    }

    public static ArrayList<Vertex> getSkeletonBackbone(Obj inputObject) {
        Image objectImage = getInitialSkeleton(inputObject);

        eliminateLoops(objectImage);

        return getLongestPath(objectImage);

    }

    static Image getInitialSkeleton(Obj inputObject) {
        // Converting object to image, then inverting, so we have a black object on a
        // white background
        Image objectImage = inputObject.getAsImage("Objects",true);
        InvertIntensity.process(objectImage);

        // Skeletonise fish to get single backbone
        BinaryOperations2D.process(objectImage, BinaryOperations2D.OperationModes.SKELETONISE, 1, 1);
        InvertIntensity.process(objectImage);

        return objectImage;

    }

    static void eliminateLoops(Image image) {
        String calculationMode = ImageCalculator.CalculationMethods.SUBTRACT;
        String overwriteMode = ImageCalculator.OverwriteModes.OVERWRITE_IMAGE1;

        ImagePlus objectIpl = image.getImagePlus();

        // Using AnalyzeSkeleton_ to remove loops
        AnalyzeSkeleton_ analyzeSkeleton_ = new AnalyzeSkeleton_();

        analyzeSkeleton_.setup("", objectIpl);
        analyzeSkeleton_.run(AnalyzeSkeleton_.SHORTEST_BRANCH, false, false, null, true, false);

        // Creating a connectivity image
        ImagePlus connIpl = objectIpl.duplicate();
        connIpl.getProcessor().multiply(1d / 255d);

        float[] kernel = new float[] { 1, 1, 1, 1, 0, 1, 1, 1, 1 };
        Convolver convolver = new Convolver();
        convolver.setNormalize(false);
        convolver.convolve(connIpl.getProcessor(), kernel, 3, 3);

        // Thresholding the connectivity image to show only values with 1
        ManualThreshold.applyThreshold(connIpl, 1);
        InvertIntensity.process(connIpl);
        ImageCalculator.process(objectIpl, connIpl, calculationMode, overwriteMode, image.getName(), false, false);

    }

    static ArrayList<Vertex> getLongestPath(Image image) {
        // Using MorphoLibJ to identify longest path
        GeodesicDiameter geodesicDiameter = new GeodesicDiameter(ChamferWeights.BORGEFORS);
        geodesicDiameter.setComputePaths(true);

        // Removing calibration, as GeodesicDiameter will output calibrated units
        ImagePlus ipl = image.getImagePlus();
        ipl.setCalibration(null);

        Map<Integer, Result> results = geodesicDiameter.analyzeRegions(ipl);
        
        // Converting coordinates to expected format
        ArrayList<Vertex> longestPath = new ArrayList<>();
        for (Result result : results.values())
            for (Point2D point : result.path)
                longestPath.add(new Vertex((int) Math.round(point.getX()), (int) Math.round(point.getY()), 0));
            
        return longestPath;

    }

    static boolean checkForLoop(ArrayList<Vertex> longestPath) {
        // Determining if it was a loop based on the proximity of the longest path ends
        Vertex firstPoint = longestPath.get(0);
        Vertex finalPoint = longestPath.get(longestPath.size() - 1);

        return firstPoint.getEdgeLength(finalPoint) <= 6;

    }

    /*
     * Checks if the longest path (skeleton backbone) needs to be inverted to have
     * the first point closer to the reference than the last point.
     */
    public static boolean testForPathInversion(ArrayList<Vertex> longestPath, double xRef, double yRef) {
        Point<Integer> referencePoint = new Point<Integer>((int) xRef, (int) yRef, 0);
        Iterator<Vertex> iterator = longestPath.iterator();

        double firstPointDistance = iterator.next().calculateDistanceToPoint(referencePoint);
        double lastPointDistance = Double.MAX_VALUE;

        while (iterator.hasNext()) {
            Vertex nextVertex = iterator.next();

            // Only calculate the distance for the final point
            if (!iterator.hasNext())
                lastPointDistance = nextVertex.calculateDistanceToPoint(referencePoint);
        }

        // If the last point is closer to the reference than the first, return true
        return (lastPointDistance < firstPointDistance);

    }

    public static CurvatureCalculator getCurvatureCalculator(ArrayList<Vertex> longestPath, String splineFittingMethod,
            int nNeighbours, int iterations, double accuracy, boolean isLoop) {
        // Calculating local curvature along the path
        CurvatureCalculator curvatureCalculator = new CurvatureCalculator(longestPath, isLoop);
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

    public static void measureCurvature(Obj inputObject, TreeMap<Double, Double> curvature, boolean absoluteCurvature,
            boolean signedCurvature) {
        double dppXY = inputObject.getDppXY();

        CumStat cumStatSigned = new CumStat();
        CumStat cumStatAbsolute = new CumStat();

        for (double value : curvature.values()) {
            if (absoluteCurvature)
                cumStatAbsolute.addMeasure(Math.abs(value));
            if (signedCurvature)
                cumStatSigned.addMeasure(value);
        }

        // Adding measurements
        if (absoluteCurvature) {
            inputObject.addMeasurement(
                    new Measurement(Measurements.MEAN_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getMean()));
            inputObject
                    .addMeasurement(new Measurement(Measurements.MIN_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getMin()));
            inputObject
                    .addMeasurement(new Measurement(Measurements.MAX_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getMax()));
            inputObject
                    .addMeasurement(new Measurement(Measurements.STD_ABSOLUTE_CURVATURE_PX, cumStatAbsolute.getStd()));
            inputObject.addMeasurement(
                    new Measurement(Measurements.MEAN_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getMean() / dppXY));
            inputObject.addMeasurement(
                    new Measurement(Measurements.MIN_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getMin() / dppXY));
            inputObject.addMeasurement(
                    new Measurement(Measurements.MAX_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getMax() / dppXY));
            inputObject.addMeasurement(
                    new Measurement(Measurements.STD_ABSOLUTE_CURVATURE_CAL, cumStatAbsolute.getStd() / dppXY));
        }

        if (signedCurvature) {
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_SIGNED_CURVATURE_PX, cumStatSigned.getMean()));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_SIGNED_CURVATURE_PX, cumStatSigned.getMin()));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_SIGNED_CURVATURE_PX, cumStatSigned.getMax()));
            inputObject.addMeasurement(new Measurement(Measurements.STD_SIGNED_CURVATURE_PX, cumStatSigned.getStd()));
            inputObject.addMeasurement(
                    new Measurement(Measurements.MEAN_SIGNED_CURVATURE_CAL, cumStatSigned.getMean() / dppXY));
            inputObject.addMeasurement(
                    new Measurement(Measurements.MIN_SIGNED_CURVATURE_CAL, cumStatSigned.getMin() / dppXY));
            inputObject.addMeasurement(
                    new Measurement(Measurements.MAX_SIGNED_CURVATURE_CAL, cumStatSigned.getMax() / dppXY));
            inputObject.addMeasurement(
                    new Measurement(Measurements.STD_SIGNED_CURVATURE_CAL, cumStatSigned.getStd() / dppXY));
        }
    }

    public static void measureRelativeCurvature(Obj inputObject, ArrayList<Vertex> longestPath,
            TreeMap<Double, Double> curvature, boolean useReference) {
        double pathLength = 0;
        double posMin = 0;
        double posMax = 0;
        double minCurvature = Double.MAX_VALUE;
        double maxCurvature = -Double.MAX_VALUE;

        double dppXY = inputObject.getDppXY();

        Iterator<Double> iterator = curvature.keySet().iterator();
        while (iterator.hasNext()) {
            pathLength = iterator.next();

            // Only evaluate the following if the points are ordered relative to a reference
            if (!useReference)
                continue;
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

        inputObject.addMeasurement(new Measurement(Measurements.SPLINE_LENGTH_PX, pathLength));
        inputObject.addMeasurement(new Measurement(Measurements.SPLINE_LENGTH_CAL, pathLength * dppXY));

        if (useReference) {
            Vertex firstPoint = longestPath.iterator().next();

            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_X_PX, firstPoint.getX()));
            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_Y_PX, firstPoint.getY()));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MIN_CURVATURE, (posMin / pathLength)));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MAX_CURVATURE, (posMax / pathLength)));

        }
    }

    public static void measureHeadTailAngle(Obj inputObject, ArrayList<Vertex> longestPath, int nPoints) {
        // Getting starting and ending points for comparison
        double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
        int pathLength = longestPath.size();

        // If the path is too short for the fitting range
        if (pathLength < nPoints)
            return;

        int count = 0;
        for (Vertex vertex : longestPath) {
            if (count == 0) {
                x1 = vertex.getX();
                y1 = vertex.getY();
            }
            if (count == nPoints - 1) {
                x1 = vertex.getX() - x1;
                y1 = vertex.getY() - y1;
            }
            if (count == pathLength - nPoints) {
                x2 = vertex.getX();
                y2 = vertex.getY();
            }
            if (count == pathLength - 1) {
                x2 = vertex.getX() - x2;
                y2 = vertex.getY() - y2;
            }
            count++;
        }

        double angle = Math.toDegrees(Math.atan2(x1 * y2 - y1 * x2, x1 * x2 + y1 * y2));

        inputObject.addMeasurement(new Measurement(Measurements.HEAD_TAIL_ANGLE_DEGS, angle));

    }

    private void initialiseObjectMeasurements(Obj inputObject, boolean absoluteCurvature, boolean signedCurvature,
            boolean useReference) {
        if (absoluteCurvature) {
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_ABSOLUTE_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_ABSOLUTE_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_ABSOLUTE_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.STD_ABSOLUTE_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_ABSOLUTE_CURVATURE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_ABSOLUTE_CURVATURE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_ABSOLUTE_CURVATURE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.STD_ABSOLUTE_CURVATURE_CAL, Double.NaN));
        }

        if (signedCurvature) {
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_SIGNED_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_SIGNED_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_SIGNED_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.STD_SIGNED_CURVATURE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MEAN_SIGNED_CURVATURE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MIN_SIGNED_CURVATURE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.MAX_SIGNED_CURVATURE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.STD_SIGNED_CURVATURE_CAL, Double.NaN));
        }

        if (useReference) {
            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_X_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.FIRST_POINT_Y_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MIN_CURVATURE, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.REL_LOC_OF_MAX_CURVATURE, Double.NaN));

        }

        inputObject.addMeasurement(new Measurement(Measurements.SPLINE_LENGTH_PX, Double.NaN));
        inputObject.addMeasurement(new Measurement(Measurements.SPLINE_LENGTH_CAL, Double.NaN));
        inputObject.addMeasurement(new Measurement(Measurements.HEAD_TAIL_ANGLE_DEGS, Double.NaN));

    }

    public Obj createFullContour(Obj inputObject, ObjCollection outputObjects, ArrayList<Vertex> spline,
            int everyNPoints, boolean isLoop) {
        if (spline == null)
            return null;

        Obj splineObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);

        Vertex previousVertex = null;
        for (Vertex currentVertex : spline) {
            if (previousVertex == null) {
                previousVertex = currentVertex;
                continue;
            }

            addLineSegment(splineObject, previousVertex, currentVertex, everyNPoints);

            previousVertex = currentVertex;
        }

        // If a loop, connect the ends
        if (isLoop) {
            addLineSegment(splineObject, spline.get(0), spline.get(spline.size() - 1), everyNPoints);
        }

        splineObject.setT(inputObject.getT());
        splineObject.addParent(inputObject);
        inputObject.addChild(splineObject);
        outputObjects.add(splineObject);

        return splineObject;

    }

    static void addLineSegment(Obj splineObject, Vertex previousVertex, Vertex currentVertex, int everyNPoints) {
        try {
            // Getting points linking the previous and current vertices
            int x1 = previousVertex.x;
            int y1 = previousVertex.y;
            int x2 = currentVertex.x;
            int y2 = currentVertex.y;

            int[][] line = BresenhamLine.getLine(x1, x2, y1, y2);

            for (int i = 0; i < line.length; i = i + everyNPoints) {
                splineObject.add(line[i][0], line[i][1], 0);
            }

            // It seems to sometimes mis the final point
            splineObject.add(x2, y2, 0);

        } catch (PointOutOfRangeException e) {
        }
    }

    public void createControlPointObjects(Obj inputObject, ObjCollection outputObjects, ArrayList<Vertex> spline,
            int everyNPoints) {
        int i = 0;
        for (Vertex vertex : spline) {
            try {
                if (i++ % everyNPoints == 0) {
                    Obj splineObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
                    splineObject.add(vertex.x, vertex.y, vertex.z);
                    splineObject.setT(inputObject.getT());
                    splineObject.addParent(inputObject);
                    inputObject.addChild(splineObject);
                }
            } catch (PointOutOfRangeException e) {
            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        int exportEveryNPoints = parameters.getValue(EXPORT_EVERY_N_POINTS);
        boolean useReference = parameters.getValue(RELATE_TO_REFERENCE_POINT);
        String xReference = parameters.getValue(X_REF_MEASUREMENT);
        String yReference = parameters.getValue(Y_REF_MEASUREMENT);
        String splineFittingMethod = parameters.getValue(SPLINE_FITTING_METHOD);
        int nNeighbours = parameters.getValue(N_NEIGHBOURS);
        int iterations = parameters.getValue(ITERATIONS);
        double accuracy = parameters.getValue(ACCURACY);
        boolean absoluteCurvature = parameters.getValue(ABSOLUTE_CURVATURE);
        boolean signedCurvature = parameters.getValue(SIGNED_CURVATURE);
        boolean drawSpline = parameters.getValue(DRAW_SPLINE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToImage = parameters.getValue(APPLY_TO_IMAGE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        double maxCurvature = parameters.getValue(MAX_CURVATURE);
        boolean calculateEndEndAngle = parameters.getValue(CALCULATE_END_END_ANGLE);
        int fittingRange = parameters.getValue(FITTING_RANGE_PX);

        // If necessary, creating a new ObjCollection and adding it to the Workspace
        ObjCollection outputObjects = null;
        if (!objectOutputMode.equals(ObjectOutputModes.DO_NOT_STORE)) {
            outputObjects = new ObjCollection(outputObjectsName, inputObjects);
            workspace.addObjects(outputObjects);
        }

        // If there are no objects, exit the module
        if (inputObjects.size() == 0)
            return Status.PASS;

        // If no reference is provided there's nothing to tell the sign of the curvature
        if (!useReference) {
            absoluteCurvature = true;
            signedCurvature = false;
        }

        ImagePlus inputIpl = null;
        if (drawSpline) {
            Image inputImage = workspace.getImage(inputImageName);
            inputIpl = inputImage.getImagePlus();
            if (!applyToImage)
                inputIpl = new Duplicator().run(inputIpl);
        }

        int count = 1;
        int total = inputObjects.size();
        for (Obj inputObject : inputObjects.values()) {            
            initialiseObjectMeasurements(inputObject, absoluteCurvature, signedCurvature, useReference);

            // Getting the backbone of the object
            ArrayList<Vertex> longestPath = getSkeletonBackbone(inputObject);
            boolean isLoop = checkForLoop(longestPath);

            // If the object is too small to be fit
            if (longestPath.size() < 3)
                continue;

            // If necessary, inverting the longest path so the first point is closest to the
            // reference
            if (useReference) {
                double xRef = inputObject.getMeasurement(xReference).getValue();
                double yRef = inputObject.getMeasurement(yReference).getValue();

                if (testForPathInversion(longestPath, xRef, yRef)) {
                    // Store the longest path in a list, then iterate through this backwards
                    LinkedList<Vertex> temporaryPathList = new LinkedList<>(longestPath);
                    Iterator<Vertex> reverseIterator = temporaryPathList.descendingIterator();

                    longestPath = new ArrayList<>();
                    while (reverseIterator.hasNext()) {
                        longestPath.add(reverseIterator.next());
                    }
                }
            }

            CurvatureCalculator calculator = getCurvatureCalculator(longestPath, splineFittingMethod, nNeighbours,
                    iterations, accuracy, isLoop);
            TreeMap<Double, Double> curvature = calculator.getCurvature();

            if (curvature == null) {
                continue;
            }

            measureCurvature(inputObject, curvature, absoluteCurvature, signedCurvature);
            measureRelativeCurvature(inputObject, longestPath, curvature, useReference);

            if (drawSpline) {
                int[] position = new int[] { 1, (int) (inputObject.getZ(false, false)[0] + 1),
                        (inputObject.getT() + 1) };
                inputIpl.setPosition(1, (int) (inputObject.getZ(false, false)[0] + 1), inputObject.getT() + 1);
                calculator.showOverlay(inputIpl, maxCurvature, position, lineWidth);
            }

            if (calculateEndEndAngle)
                measureHeadTailAngle(inputObject, longestPath, fittingRange);

            switch (objectOutputMode) {
                case ObjectOutputModes.FULL_CONTOUR:
                    createFullContour(inputObject, outputObjects, calculator.getSpline(), exportEveryNPoints, isLoop);

                    break;
                case ObjectOutputModes.CONTROL_POINTS:
                    createControlPointObjects(inputObject, outputObjects, calculator.getSpline(), exportEveryNPoints);
                    break;
            }

            writeProgressStatus(count++, total, "objects");
            
        }

        if (drawSpline & !applyToImage)
            workspace.addImage(new Image(outputImageName, inputIpl));

        if (showOutput && drawSpline)
            new Image("Spline", inputIpl).showImage();
        if (showOutput)
            inputObjects.showMeasurements(this, modules);
        if (showOutput & !objectOutputMode.equals(ObjectOutputModes.DO_NOT_STORE)) {
            outputObjects.convertToImageRandomColours().showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, ObjectOutputModes.DO_NOT_STORE, ObjectOutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new IntegerP(EXPORT_EVERY_N_POINTS, this, 1));
        parameters.add(new BooleanP(RELATE_TO_REFERENCE_POINT, this, false));
        parameters.add(new ObjectMeasurementP(X_REF_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Y_REF_MEASUREMENT, this));
        parameters.add(new ChoiceP(SPLINE_FITTING_METHOD, this, SplineFittingMethods.LOESS, SplineFittingMethods.ALL));
        parameters.add(new IntegerP(N_NEIGHBOURS, this, 20));
        parameters.add(new IntegerP(ITERATIONS, this, 10));
        parameters.add(new DoubleP(ACCURACY, this, 1d));
        parameters.add(new BooleanP(ABSOLUTE_CURVATURE, this, true));
        parameters.add(new BooleanP(SIGNED_CURVATURE, this, true));
        parameters.add(new BooleanP(DRAW_SPLINE, this, false));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_IMAGE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1d));
        parameters.add(new DoubleP(MAX_CURVATURE, this, 1d));
        parameters.add(new BooleanP(CALCULATE_END_END_ANGLE, this, true));
        parameters.add(new IntegerP(FITTING_RANGE_PX, this, 5));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case ObjectOutputModes.CONTROL_POINTS:
            case ObjectOutputModes.FULL_CONTOUR:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(EXPORT_EVERY_N_POINTS));

                break;
        }

        returnedParameters.add(parameters.getParameter(RELATE_TO_REFERENCE_POINT));
        if ((boolean) parameters.getValue(RELATE_TO_REFERENCE_POINT)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

            ((ObjectMeasurementP) parameters.getParameter(X_REF_MEASUREMENT)).setObjectName(inputObjectsName);
            ((ObjectMeasurementP) parameters.getParameter(Y_REF_MEASUREMENT)).setObjectName(inputObjectsName);

            returnedParameters.add(parameters.getParameter(X_REF_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(Y_REF_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(ABSOLUTE_CURVATURE));
            returnedParameters.add(parameters.getParameter(SIGNED_CURVATURE));

        }

        returnedParameters.add(parameters.getParameter(SPLINE_FITTING_METHOD));
        switch ((String) parameters.getValue(SPLINE_FITTING_METHOD)) {
            case SplineFittingMethods.LOESS:
                returnedParameters.add(parameters.getParameter(N_NEIGHBOURS));
                returnedParameters.add(parameters.getParameter(ITERATIONS));
                returnedParameters.add(parameters.getParameter(ACCURACY));
                break;
        }

        returnedParameters.add(parameters.getParameter(DRAW_SPLINE));
        if ((boolean) parameters.getValue(DRAW_SPLINE)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.add(parameters.getParameter(APPLY_TO_IMAGE));
            if (!(boolean) parameters.getValue(APPLY_TO_IMAGE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
            returnedParameters.add(parameters.getParameter(LINE_WIDTH));
            returnedParameters.add(parameters.getParameter(MAX_CURVATURE));
        }

        returnedParameters.add(parameters.getParameter(CALCULATE_END_END_ANGLE));
        if ((boolean) parameters.getValue(CALCULATE_END_END_ANGLE)) {
            returnedParameters.add(parameters.getParameter(FITTING_RANGE_PX));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef meanCurvatureAbsolutePx = objectMeasurementRefs
                .getOrPut(Measurements.MEAN_ABSOLUTE_CURVATURE_PX);
        ObjMeasurementRef minCurvatureAbsolutePx = objectMeasurementRefs
                .getOrPut(Measurements.MIN_ABSOLUTE_CURVATURE_PX);
        ObjMeasurementRef maxCurvatureAbsolutePx = objectMeasurementRefs
                .getOrPut(Measurements.MAX_ABSOLUTE_CURVATURE_PX);
        ObjMeasurementRef stdCurvatureAbsolutePx = objectMeasurementRefs
                .getOrPut(Measurements.STD_ABSOLUTE_CURVATURE_PX);
        ObjMeasurementRef meanCurvatureAbsoluteCal = objectMeasurementRefs
                .getOrPut(Measurements.MEAN_ABSOLUTE_CURVATURE_CAL);
        ObjMeasurementRef minCurvatureAbsoluteCal = objectMeasurementRefs
                .getOrPut(Measurements.MIN_ABSOLUTE_CURVATURE_CAL);
        ObjMeasurementRef maxCurvatureAbsoluteCal = objectMeasurementRefs
                .getOrPut(Measurements.MAX_ABSOLUTE_CURVATURE_CAL);
        ObjMeasurementRef stdCurvatureAbsoluteCal = objectMeasurementRefs
                .getOrPut(Measurements.STD_ABSOLUTE_CURVATURE_CAL);
        ObjMeasurementRef meanCurvatureSignedPx = objectMeasurementRefs.getOrPut(Measurements.MEAN_SIGNED_CURVATURE_PX);
        ObjMeasurementRef minCurvatureSignedPx = objectMeasurementRefs.getOrPut(Measurements.MIN_SIGNED_CURVATURE_PX);
        ObjMeasurementRef maxCurvatureSignedPx = objectMeasurementRefs.getOrPut(Measurements.MAX_SIGNED_CURVATURE_PX);
        ObjMeasurementRef stdCurvatureSignedPx = objectMeasurementRefs.getOrPut(Measurements.STD_SIGNED_CURVATURE_PX);
        ObjMeasurementRef meanCurvatureSignedCal = objectMeasurementRefs
                .getOrPut(Measurements.MEAN_SIGNED_CURVATURE_CAL);
        ObjMeasurementRef minCurvatureSignedCal = objectMeasurementRefs.getOrPut(Measurements.MIN_SIGNED_CURVATURE_CAL);
        ObjMeasurementRef maxCurvatureSignedCal = objectMeasurementRefs.getOrPut(Measurements.MAX_SIGNED_CURVATURE_CAL);
        ObjMeasurementRef stdCurvatureSignedCal = objectMeasurementRefs.getOrPut(Measurements.STD_SIGNED_CURVATURE_CAL);
        ObjMeasurementRef splineLengthPx = objectMeasurementRefs.getOrPut(Measurements.SPLINE_LENGTH_PX);
        ObjMeasurementRef splineLengthCal = objectMeasurementRefs.getOrPut(Measurements.SPLINE_LENGTH_CAL);
        ObjMeasurementRef firstPointX = objectMeasurementRefs.getOrPut(Measurements.FIRST_POINT_X_PX);
        ObjMeasurementRef firstPointY = objectMeasurementRefs.getOrPut(Measurements.FIRST_POINT_Y_PX);
        ObjMeasurementRef relLocMinCurvature = objectMeasurementRefs.getOrPut(Measurements.REL_LOC_OF_MIN_CURVATURE);
        ObjMeasurementRef relLocMaxCurvature = objectMeasurementRefs.getOrPut(Measurements.REL_LOC_OF_MAX_CURVATURE);
        ObjMeasurementRef headTailAngle = objectMeasurementRefs.getOrPut(Measurements.HEAD_TAIL_ANGLE_DEGS);

        meanCurvatureAbsolutePx.setObjectsName(inputObjectsName);
        minCurvatureAbsolutePx.setObjectsName(inputObjectsName);
        maxCurvatureAbsolutePx.setObjectsName(inputObjectsName);
        stdCurvatureAbsolutePx.setObjectsName(inputObjectsName);
        meanCurvatureAbsoluteCal.setObjectsName(inputObjectsName);
        minCurvatureAbsoluteCal.setObjectsName(inputObjectsName);
        maxCurvatureAbsoluteCal.setObjectsName(inputObjectsName);
        stdCurvatureAbsoluteCal.setObjectsName(inputObjectsName);
        meanCurvatureSignedPx.setObjectsName(inputObjectsName);
        minCurvatureSignedPx.setObjectsName(inputObjectsName);
        maxCurvatureSignedPx.setObjectsName(inputObjectsName);
        stdCurvatureSignedPx.setObjectsName(inputObjectsName);
        meanCurvatureSignedCal.setObjectsName(inputObjectsName);
        minCurvatureSignedCal.setObjectsName(inputObjectsName);
        maxCurvatureSignedCal.setObjectsName(inputObjectsName);
        stdCurvatureSignedCal.setObjectsName(inputObjectsName);
        splineLengthPx.setObjectsName(inputObjectsName);
        splineLengthCal.setObjectsName(inputObjectsName);
        firstPointX.setObjectsName(inputObjectsName);
        firstPointY.setObjectsName(inputObjectsName);
        relLocMinCurvature.setObjectsName(inputObjectsName);
        relLocMaxCurvature.setObjectsName(inputObjectsName);
        headTailAngle.setObjectsName(inputObjectsName);

        boolean relateToReference = false;
        boolean absoluteCurvature = false;
        boolean signedCurvature = false;
        boolean calculateHeadTailAngle = parameters.getValue(CALCULATE_END_END_ANGLE);

        if ((boolean) parameters.getValue(RELATE_TO_REFERENCE_POINT)) {
            relateToReference = true;
            if ((boolean) parameters.getValue(ABSOLUTE_CURVATURE))
                absoluteCurvature = true;
            if ((boolean) parameters.getValue(SIGNED_CURVATURE))
                signedCurvature = true;

        } else {
            absoluteCurvature = true;
            signedCurvature = false;
        }

        returnedRefs.add(splineLengthPx);
        returnedRefs.add(splineLengthCal);
        if (absoluteCurvature)
            returnedRefs.add(meanCurvatureAbsolutePx);
        if (absoluteCurvature)
            returnedRefs.add(minCurvatureAbsolutePx);
        if (absoluteCurvature)
            returnedRefs.add(maxCurvatureAbsolutePx);
        if (absoluteCurvature)
            returnedRefs.add(stdCurvatureAbsolutePx);
        if (absoluteCurvature)
            returnedRefs.add(meanCurvatureAbsoluteCal);
        if (absoluteCurvature)
            returnedRefs.add(minCurvatureAbsoluteCal);
        if (absoluteCurvature)
            returnedRefs.add(maxCurvatureAbsoluteCal);
        if (absoluteCurvature)
            returnedRefs.add(stdCurvatureAbsoluteCal);
        if (signedCurvature)
            returnedRefs.add(meanCurvatureSignedPx);
        if (signedCurvature)
            returnedRefs.add(minCurvatureSignedPx);
        if (signedCurvature)
            returnedRefs.add(maxCurvatureSignedPx);
        if (signedCurvature)
            returnedRefs.add(stdCurvatureSignedPx);
        if (signedCurvature)
            returnedRefs.add(meanCurvatureSignedCal);
        if (signedCurvature)
            returnedRefs.add(minCurvatureSignedCal);
        if (signedCurvature)
            returnedRefs.add(maxCurvatureSignedCal);
        if (signedCurvature)
            returnedRefs.add(stdCurvatureSignedCal);
        if (relateToReference)
            returnedRefs.add(firstPointX);
        if (relateToReference)
            returnedRefs.add(firstPointY);
        if (relateToReference)
            returnedRefs.add(relLocMinCurvature);
        if (relateToReference)
            returnedRefs.add(relLocMaxCurvature);
        if (calculateHeadTailAngle)
            returnedRefs.add(headTailAngle);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection refCollection = new ParentChildRefCollection();

        if (!parameters.getValue(OBJECT_OUTPUT_MODE).equals(ObjectOutputModes.DO_NOT_STORE)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
            refCollection.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));
        }

        return refCollection;

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
