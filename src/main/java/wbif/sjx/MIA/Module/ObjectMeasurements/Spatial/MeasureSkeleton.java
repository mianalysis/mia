package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.Prefs;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Edge;
import sc.fiji.analyzeSkeleton.Graph;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputSkeletonObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.VolumeType;

public class MeasureSkeleton extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String ADD_SKELETONS_TO_WORKSPACE = "Add skeletons to workspace";
    public static final String OUTPUT_SKELETON_OBJECTS = "Output skeleton objects";
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";
    public static final String OUTPUT_JUNCTION_OBJECTS = "Output junction objects";
    public static final String OUTPUT_LOOP_OBJECTS = "Output loop objects";
    public static final String ANALYSIS_SEPARATOR = "Analysis settings";
    public static final String MINIMUM_BRANCH_LENGTH = "Minimum branch length";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface Measurements {
        String nBranches = "SKELETON // NUM_BRANCHES";
        String nJunctions = "SKELETON // NUM_JUNCTIONS";
        String nEndPoints = "SKELETON // NUM_END_POINT_VOXELS";
        String nJunctionVoxels = "SKELETON // NUM_JUNCTION_VOXELS";
        String nSlabVoxels = "SKELETON // NUM_SLAB_VOXELS";
        String nTriplePoints = "SKELETON // NUM_TRIPLE_POINTS";
        String nQuadruplePoints = "SKELETON // NUM_QUADRUPLE_POINTS";
        String avBranchLengthPx = "SKELETON // AV_BRANCH_LENGTH (PX)";
        String avBranchLengthCal = "SKELETON // AV_BRANCH_LENGTH (${CAL})";
        String maxBranchLengthPx = "SKELETON // MAX_BRANCH_LENGTH (PX)";
        String maxBranchLengthCal = "SKELETON // MAX_BRANCH_LENGTH (${CAL})";

    }

    public MeasureSkeleton(ModuleCollection modules) {
        super("Measure skeleton", modules);
    }

    static Image getProjectedImage(Obj inputObject) {
        // Generate projected object
        Obj projectedObj;
        try {
            projectedObj = ProjectObjects.process(inputObject, "Projected", false);
        } catch (IntegerOverflowException e) {
            e.printStackTrace();
            return null;
        }
        Image projectedImage = projectedObj.convertObjToImage("Projected");
        InvertIntensity.process(projectedImage);

        // Run skeletonisation (ensures it is properly skeletonised before processing)
        BinaryOperations2D.process(projectedImage, BinaryOperations2D.OperationModes.SKELETONISE, 1, 1);
        InvertIntensity.process(projectedImage);

        return projectedImage;

    }

    static void createObjects(Obj inputObject, SkeletonResult result, ObjCollection skeletonObjects,
            ObjCollection edgeObjects, ObjCollection junctionObjects, ObjCollection loopObjects) {
        // The Skeleton object doesn't contain any coordinate data, it just links
        // branches, junctions and loops.
        Obj skeletonObject = new Obj(VolumeType.POINTLIST, skeletonObjects.getName(), inputObject.getID(), inputObject);
        skeletonObject.setT(inputObject.getT());
        inputObject.addChild(skeletonObject);
        skeletonObject.addParent(inputObject);
        skeletonObjects.add(skeletonObject);

        // Iterating over all edges, creating objects and adding to relevant
        // collection
        for (Graph graph : result.getGraph()) {
            for (Edge edge : graph.getEdges()) {
                createEdgeObject(skeletonObject, edgeObjects, edge);
            }
        }
    }

    static Obj createEdgeObject(Obj skeletonObject, ObjCollection edgeObjects, Edge edge) {
        Obj edgeObject = edgeObjects.createAndAddNewObject(VolumeType.POINTLIST);
        edgeObject.setT(skeletonObject.getT());
        skeletonObject.addChild(edgeObject);
        edgeObject.addParent(skeletonObject);

        // Adding coordinates
        for (Point point : edge.getSlabs()) {
            try {
                edgeObject.add(point.x, point.y, 0);
            } catch (PointOutOfRangeException e) {

            }
        }

        return edgeObject;

    }

    static void addMeasurements(Obj inputObject, SkeletonResult skeletonResult) {
        double dppXY = inputObject.getDppXY();

        inputObject.addMeasurement(new Measurement(Measurements.nBranches, skeletonResult.getBranches()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nJunctions, skeletonResult.getJunctions()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nEndPoints, skeletonResult.getEndPoints()[0]));
        inputObject
                .addMeasurement(new Measurement(Measurements.nJunctionVoxels, skeletonResult.getJunctionVoxels()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nSlabVoxels, skeletonResult.getSlabs()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nTriplePoints, skeletonResult.getTriples()[0]));
        inputObject.addMeasurement(new Measurement(Measurements.nQuadruplePoints, skeletonResult.getQuadruples()[0]));
        inputObject.addMeasurement(
                new Measurement(Measurements.avBranchLengthPx, skeletonResult.getAverageBranchLength()[0] / dppXY));
        inputObject.addMeasurement(
                new Measurement(Measurements.avBranchLengthCal, skeletonResult.getAverageBranchLength()[0]));
        inputObject.addMeasurement(
                new Measurement(Measurements.maxBranchLengthPx, skeletonResult.getMaximumBranchLength()[0] / dppXY));
        inputObject.addMeasurement(
                new Measurement(Measurements.maxBranchLengthCal, skeletonResult.getMaximumBranchLength()[0]));

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        boolean addToWorkspace = parameters.getValue(ADD_SKELETONS_TO_WORKSPACE);
        String outputSkeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS);
        String outputEdgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String outputJunctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        String outputLoopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);
        double minLength = parameters.getValue(MINIMUM_BRANCH_LENGTH);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If necessary, converting to pixel units
        if (calibratedUnits)
            minLength = minLength / inputObjects.getFirst().getDppXY();

        // Creating empty output object collections
        final ObjCollection skeletonObjects = addToWorkspace
                ? new ObjCollection(outputSkeletonObjectsName, inputObjects)
                : null;
        final ObjCollection edgeObjects = addToWorkspace ? new ObjCollection(outputEdgeObjectsName, inputObjects)
                : null;
        final ObjCollection junctionObjects = addToWorkspace
                ? new ObjCollection(outputJunctionObjectsName, inputObjects)
                : null;
        final ObjCollection loopObjects = addToWorkspace ? new ObjCollection(outputLoopObjectsName, inputObjects)
                : null;
        if (addToWorkspace) {
            workspace.addObjects(skeletonObjects);
            workspace.addObjects(edgeObjects);
            workspace.addObjects(junctionObjects);
            workspace.addObjects(loopObjects);
        }

        // Configuring multithreading
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int nTotal = inputObjects.size();
        AtomicInteger count = new AtomicInteger();

        // For each object, create a child projected object, then generate a binary
        // image. This is run through ImageJ's
        // skeletonize plugin to ensure it has 4-way connectivity. Finally, it is
        // processed with the AnalyzeSkeleton
        // plugin.
        final double minLengthFinal = minLength;
        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                Image projectedImage = getProjectedImage(inputObject);

                AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
                analyzeSkeleton.setup("", projectedImage.getImagePlus());
                SkeletonResult skeletonResult = analyzeSkeleton.run(AnalyzeSkeleton_.NONE, minLengthFinal, false,
                        projectedImage.getImagePlus(), true, false);
                // skeletons has edges and junctions
                // what is the relationship between them
                // Junction a single point with any number of edges
                // Edge is the path between two junction
                // Pathy - collection of coordinates

                // Adding the skeleton to the input object
                if (addToWorkspace) {
                    try {
                        createObjects(inputObject, skeletonResult, skeletonObjects, edgeObjects, junctionObjects,
                                loopObjects);

                    } catch (IntegerOverflowException e) {
                        e.printStackTrace();
                    }
                }

                // Taking the first result for each (in the event there was more than one
                // isolated region)
                // addMeasurements(inputObject,skeletonResult);

                writeMessage("Processed " + (count.incrementAndGet()) + " of " + nTotal + " objects");

            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(ADD_SKELETONS_TO_WORKSPACE, this, false));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_SKELETON_OBJECTS, this));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_EDGE_OBJECTS, this));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_JUNCTION_OBJECTS, this));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_LOOP_OBJECTS, this));
        parameters.add(new ParamSeparatorP(ANALYSIS_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_BRANCH_LENGTH, this, 0d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_SKELETONS_TO_WORKSPACE));

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SKELETON_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_JUNCTION_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_LOOP_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(ANALYSIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_BRANCH_LENGTH));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.nBranches);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nJunctions);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nEndPoints);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nJunctionVoxels);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nSlabVoxels);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nTriplePoints);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.nQuadruplePoints);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.avBranchLengthPx);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.avBranchLengthCal);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.maxBranchLengthPx);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(Measurements.maxBranchLengthCal);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetRelationships() {
        ParentChildRefCollection returnedRefs = new ParentChildRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS);
        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);

        returnedRefs.add(ParentChildRefs.getOrPut(inputObjectsName, skeletonObjectsName));
        returnedRefs.add(ParentChildRefs.getOrPut(skeletonObjectsName, edgeObjectsName));
        returnedRefs.add(ParentChildRefs.getOrPut(skeletonObjectsName, junctionObjectsName));
        returnedRefs.add(ParentChildRefs.getOrPut(skeletonObjectsName, loopObjectsName));

        return returnedRefs;

    }

    @Override
    public String getDescription() {
        return "Uses the AnalyzeSkeleton plugin by Ignacio Arganda-Carreras (https://imagej.net/AnalyzeSkeleton)."
                + "<br><br>The optional, output skeleton object acts solely as a linking object for the edge, junction and loop objects.  It doesn't itself hold any coordinate data.";
    }

    @Override
    public boolean verify() {
        return true;
    }
}
