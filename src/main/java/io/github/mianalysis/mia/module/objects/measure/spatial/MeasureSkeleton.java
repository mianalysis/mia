package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.images.process.binary.Skeletonise;
import io.github.mianalysis.mia.module.objects.detect.IdentifyObjects;
import io.github.mianalysis.mia.module.objects.filter.FilterOnImageEdge;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.OutputSkeletonObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.common.object.volume.CoordinateSet;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.VolumeType;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Edge;
import sc.fiji.analyzeSkeleton.Graph;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import sc.fiji.analyzeSkeleton.Vertex;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureSkeleton extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String ADD_SKELETONS_TO_WORKSPACE = "Add skeletons to workspace";
    public static final String OUTPUT_SKELETON_OBJECTS = "Output skeleton objects";
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";
    public static final String OUTPUT_JUNCTION_OBJECTS = "Output junction objects";
    public static final String EXPORT_LOOP_OBJECTS = "Export loop objects";
    public static final String OUTPUT_LOOP_OBJECTS = "Output loop objects";
    public static final String EXPORT_LARGEST_SHORTEST_PATH = "Export largest shortest path";
    public static final String OUTPUT_LARGEST_SHORTEST_PATH = "Output largest shortest path";

    public static final String SKELETONISATION_SEPARATOR = "Skeletonisation settings";
    public static final String MINIMUM_BRANCH_LENGTH = "Minimum branch length";
    public static final String CALIBRATED_UNITS = "Calibrated units";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface Measurements {
        String sumLengthPx = "SKELETON // SUM_LENGTH_(PX)";
        String sumLengthCal = "SKELETON // SUM_LENGTH_(${SCAL})";
        String edgeLengthPx = "SKELETON // LENGTH_(PX)";
        String edgeLengthCal = "SKELETON // LENGTH_(${SCAL})";

    }

    public MeasureSkeleton(Modules modules) {
        super("Measure skeleton", modules);
    }

    static Image getSkeletonImage(Obj inputObject) {
        // Getting tight image of object
        Image skeletonImage = inputObject.getAsTightImage("Skeleton");

        // Running 3D skeletonisation
        Skeletonise.process(skeletonImage, true);

        return skeletonImage;

    }

    static Object[] initialiseAnalyzer(Obj inputObject, double minLengthFinal, boolean exportLargestShortestPathFinal) {
        Image skeletonImage = getSkeletonImage(inputObject);

        try {
            AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
            analyzeSkeleton.setup("", skeletonImage.getImagePlus());
            SkeletonResult skeletonResult = analyzeSkeleton.run(AnalyzeSkeleton_.NONE, minLengthFinal,
                    exportLargestShortestPathFinal, skeletonImage.getImagePlus(), true, false);

            return new Object[] { analyzeSkeleton, skeletonResult };

        } catch (Exception e) {
            MIA.log.writeError(e);
            return null;
        }
    }

    static Obj createEdgeJunctionObjects(Obj inputObject, SkeletonResult result, Objs skeletonObjects, Objs edgeObjects,
            Objs junctionObjects) {

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        // The Skeleton object doesn't contain any coordinate data, it just links
        // branches, junctions and loops.
        Obj skeletonObject = skeletonObjects.createAndAddNewObject(VolumeType.POINTLIST);
        skeletonObject.setT(inputObject.getT());
        inputObject.addChild(skeletonObject);
        skeletonObject.addParent(inputObject);

        // For the purpose of linking edges and junctions, these are stored in a
        // HashMap.
        HashMap<Edge, Obj> edgeObjs = new HashMap<>();
        HashMap<Vertex, Obj> junctionObjs = new HashMap<>();

        // Creating objects
        double dppXY = inputObject.getDppXY();
        for (Graph graph : result.getGraph()) {
            for (Edge edge : graph.getEdges()) {
                Obj edgeObj = createEdgeObject(skeletonObject, edgeObjects, edge, xOffs, yOffs, zOffs);
                edgeObjs.put(edge, edgeObj);

                // Adding edge length measurements
                double calLength = edge.getLength();
                Measurement lengthPx = new Measurement(Measurements.edgeLengthPx, calLength / dppXY);
                edgeObj.addMeasurement(lengthPx);
                Measurement lengthCal = new Measurement(Measurements.edgeLengthCal, calLength);
                edgeObj.addMeasurement(lengthCal);

            }

            for (Vertex junction : graph.getVertices()) {
                Obj junctionObj = createJunctionObject(skeletonObject, junctionObjects, junction, xOffs, yOffs, zOffs);
                junctionObjs.put(junction, junctionObj);
            }
        }

        // Applying partnerships between edges and junctions
        applyEdgeJunctionPartnerships(edgeObjs, junctionObjs);

        // Returning skeleton (linking) object
        return skeletonObject;

    }

    static void createLoopObjects(Objs loopObjects, String edgeObjectsName, String junctionObjectsName,
            String loopObjectsName, Obj skeletonObject) {

        // Creating an object for the entire skeleton
        Objs tempCollection = new Objs("Skeleton", loopObjects);
        Obj tempObject = tempCollection.createAndAddNewObject(VolumeType.POINTLIST);
        CoordinateSet coords = tempObject.getCoordinateSet();

        // Adding all points from edges and junctions
        for (Obj edgeObject : skeletonObject.getChildren(edgeObjectsName).values())
            coords.addAll(edgeObject.getCoordinateSet());

        for (Obj junctionObject : skeletonObject.getChildren(junctionObjectsName).values())
            coords.addAll(junctionObject.getCoordinateSet());

        // Creating a binary image of all the points with a 1px border, so we can remove
        // objects on the image edge still
        int[][] borders = new int[][] { { 1, 1 }, { 1, 1 }, { 0, 0 } };
        Image binaryImage = tempObject.getAsTightImage("outputName", borders);

        // Converting binary image to loop objects
        Objs tempLoopObjects = IdentifyObjects.process(binaryImage, loopObjectsName, false, false, 6,
                VolumeTypesInterface.QUADTREE, false, 0, false);

        // Removing any objects on the image edge, as these aren't loops
        FilterOnImageEdge.process(tempLoopObjects, 0, null, false, true, null);

        // Shifting objects back to the correct positions
        double[][] extents = tempObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]) - 1;
        int yOffs = (int) Math.round(extents[1][0]) - 1;
        int zOffs = (int) Math.round(extents[2][0]);
        tempLoopObjects.setSpatialCalibration(loopObjects.getSpatialCalibration(), true);

        for (Obj tempLoopObject : tempLoopObjects.values())
            tempLoopObject.translateCoords(xOffs, yOffs, zOffs);

        for (Obj tempLoopObject : tempLoopObjects.values()) {
            tempLoopObject.setID(loopObjects.getAndIncrementID());
            tempLoopObject.addParent(skeletonObject);
            skeletonObject.addChild(tempLoopObject);
            loopObjects.add(tempLoopObject);
        }
    }

    static Obj createEdgeObject(Obj skeletonObject, Objs edgeObjects, Edge edge, int xOffs, int yOffs, int zOffs) {
        Obj edgeObject = edgeObjects.createAndAddNewObject(VolumeType.POINTLIST);
        edgeObject.setT(skeletonObject.getT());
        skeletonObject.addChild(edgeObject);
        edgeObject.addParent(skeletonObject);

        // Adding coordinates
        for (Point point : edge.getSlabs()) {
            try {
                edgeObject.add(point.x + xOffs, point.y + yOffs, point.z + zOffs);
            } catch (PointOutOfRangeException e) {
            }
        }

        return edgeObject;

    }

    static Obj createJunctionObject(Obj skeletonObject, Objs junctionObjects, Vertex junction, int xOffs, int yOffs,
            int zOffs) {
        Obj junctionObject = junctionObjects.createAndAddNewObject(VolumeType.POINTLIST);
        junctionObject.setT(skeletonObject.getT());
        skeletonObject.addChild(junctionObject);
        junctionObject.addParent(skeletonObject);

        // Adding coordinates
        for (Point point : junction.getPoints()) {
            try {
                junctionObject.add(point.x + xOffs, point.y + yOffs, point.z + zOffs);
            } catch (PointOutOfRangeException e) {
            }
        }

        return junctionObject;

    }

    public static ArrayList<io.github.sjcross.common.object.Point<Integer>> getLargestShortestPath(Obj inputObject) {
        Object[] result = initialiseAnalyzer(inputObject, 0, true);
        AnalyzeSkeleton_ analyzeSkeleton = (AnalyzeSkeleton_) result[0];
        SkeletonResult skeletonResult = (SkeletonResult) result[1];

        return getLargestShortestPath(inputObject, analyzeSkeleton, skeletonResult);

    }

    public static ArrayList<io.github.sjcross.common.object.Point<Integer>> getLargestShortestPath(Obj inputObject,
            AnalyzeSkeleton_ analyzeSkeleton, SkeletonResult skeletonResult) {
        ArrayList<io.github.sjcross.common.object.Point<Integer>> points2 = new ArrayList<>();

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        ArrayList<Double> shortestPaths = skeletonResult.getShortestPathList();
        if (shortestPaths.size() == 0)
            return points2;

        int longestPathIdx = -1;
        double longestPathLength = -1;

        for (int i = 0; i < shortestPaths.size(); i++) {
            if (shortestPaths.get(i) > longestPathLength) {
                longestPathLength = shortestPaths.get(i);
                longestPathIdx = i;
            }
        }

        ArrayList<Point> points1 = analyzeSkeleton.getShortestPathPoints()[longestPathIdx];

        for (Point point : points1)
            points2.add(new io.github.sjcross.common.object.Point<Integer>(point.x + xOffs, point.y + yOffs,
                    point.z + zOffs));

        return points2;

    }

    static void createLargestShortestPath(Obj inputObject, Objs largestShortestPathObjects,
            AnalyzeSkeleton_ analyzeSkeleton, SkeletonResult skeletonResult) {

        ArrayList<io.github.sjcross.common.object.Point<Integer>> points = getLargestShortestPath(inputObject,
                analyzeSkeleton, skeletonResult);

        Obj largestShortestPath = largestShortestPathObjects.createAndAddNewObject(VolumeType.POINTLIST);
        largestShortestPath.getCoordinateSet().addAll(points);
        largestShortestPath.setT(inputObject.getT());
        largestShortestPath.addParent(inputObject);
        inputObject.addChild(largestShortestPath);

    }

    static void applyEdgeJunctionPartnerships(HashMap<Edge, Obj> edgeObjs, HashMap<Vertex, Obj> junctionObjs) {
        // Iterating over each edge, adding the two vertices at either end as partners
        for (Edge edge : edgeObjs.keySet()) {
            Obj edgeObject = edgeObjs.get(edge);
            Obj junction1 = junctionObjs.get(edge.getV1());
            Obj junction2 = junctionObjs.get(edge.getV2());

            edgeObject.addPartner(junction1);
            junction1.addPartner(edgeObject);
            edgeObject.addPartner(junction2);
            junction2.addPartner(edgeObject);

        }
    }

    static void applyLoopPartnerships(Objs loopObjects, Objs edgeObjects, Objs junctionObjects) {
        // Linking junctions and loops with surfaces separated by 1px or less
        for (Obj loopObject : loopObjects.values()) {
            for (Obj junctionObject : junctionObjects.values()) {
                if (loopObject.getSurfaceSeparation(junctionObject, true) <= 1) {
                    loopObject.addPartner(junctionObject);
                    junctionObject.addPartner(loopObject);
                }
            }
        }

        // Linking edges with both junctions linked to the loop
        for (Obj loopObject : loopObjects.values()) {
            for (Obj edgeObject : edgeObjects.values()) {
                Objs junctionPartners = edgeObject.getPartners(junctionObjects.getName());
                boolean matchFound = true;

                for (Obj junctionPartnerObject : junctionPartners.values()) {
                    Objs loopPartners = junctionPartnerObject.getPartners(loopObjects.getName());

                    if (loopPartners == null) {
                        matchFound = false;
                        continue;
                    }

                    if (!loopPartners.values().contains(loopObject)) {
                        matchFound = false;
                    }
                }

                if (matchFound) {
                    loopObject.addPartner(edgeObject);
                    edgeObject.addPartner(loopObject);
                }
            }
        }
    }

    static void addMeasurements(Obj inputObject, SkeletonResult result) {
        double length = 0;
        for (Graph graph : result.getGraph()) {
            for (Edge edge : graph.getEdges())
                length = length + edge.getLength();
        }

        double dppXY = inputObject.getDppXY();
        inputObject.addMeasurement(new Measurement(Measurements.sumLengthPx, length / dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.sumLengthCal, length));

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
        boolean addToWorkspace = parameters.getValue(ADD_SKELETONS_TO_WORKSPACE);
        String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS);
        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        boolean exportLoops = parameters.getValue(EXPORT_LOOP_OBJECTS);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);
        boolean exportLargestShortestPath = parameters.getValue(EXPORT_LARGEST_SHORTEST_PATH);
        String largestShortestPathName = parameters.getValue(OUTPUT_LARGEST_SHORTEST_PATH);
        double minLength = parameters.getValue(MINIMUM_BRANCH_LENGTH);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        if (inputObjects == null || inputObjects.size() == 0)
            return Status.PASS;

        // If necessary, converting to calibrated units (Skeletonise takes calibrated
        // measurements, so unlike most modules, we want to convert to calibrated units)
        if (!calibratedUnits)
            minLength = minLength / inputObjects.getDppXY();

        // Creating empty output object collections
        final Objs skeletonObjects = addToWorkspace ? new Objs(skeletonObjectsName, inputObjects) : null;
        final Objs edgeObjects = addToWorkspace ? new Objs(edgeObjectsName, inputObjects) : null;
        final Objs junctionObjects = addToWorkspace ? new Objs(junctionObjectsName, inputObjects) : null;
        final Objs loopObjects = addToWorkspace & exportLoops ? new Objs(loopObjectsName, inputObjects) : null;
        final Objs largestShortestPathObjects = exportLargestShortestPath
                ? new Objs(largestShortestPathName, inputObjects)
                : null;

        if (addToWorkspace) {
            workspace.addObjects(skeletonObjects);
            workspace.addObjects(edgeObjects);
            workspace.addObjects(junctionObjects);
            if (exportLoops)
                workspace.addObjects(loopObjects);
        }

        // These can be exported independently of the main skeleton
        if (exportLargestShortestPath) {
            workspace.addObjects(largestShortestPathObjects);
            // Largest shortest path requires calibrated units. If none present, export
            // empty collection
            if (Double.isNaN(inputObjects.getFirst().getDppXY()) || Double.isNaN(inputObjects.getFirst().getDppXY())) {
                MIA.log.writeWarning(
                        "Spatial calibration required for largest shortest path in Measure Skeleton.  No largest shortest paths output.");
                exportLargestShortestPath = false;
            }
        }

        // Configuring multithreading
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int total = inputObjects.size();
        AtomicInteger count = new AtomicInteger();

        final double minLengthFinal = minLength;
        final boolean exportLargestShortestPathFinal = exportLargestShortestPath;
        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                try {
                    Object[] result = initialiseAnalyzer(inputObject, minLengthFinal, exportLargestShortestPathFinal);
                    // Adding the skeleton to the input object
                    if (addToWorkspace) {

                        Obj skeletonObject = createEdgeJunctionObjects(inputObject, (SkeletonResult) result[1],
                                skeletonObjects, edgeObjects, junctionObjects);

                        // Creating loop objects
                        if (exportLoops) {
                            createLoopObjects(loopObjects, edgeObjectsName, junctionObjectsName, loopObjectsName,
                                    skeletonObject);
                            workspace.addObjects(loopObjects);
                            applyLoopPartnerships(loopObjects, edgeObjects, junctionObjects);
                        }
                    }

                    if (exportLargestShortestPathFinal)
                        createLargestShortestPath(inputObject, largestShortestPathObjects, (AnalyzeSkeleton_) result[0],
                                (SkeletonResult) result[1]);

                    addMeasurements(inputObject, (SkeletonResult) result[1]);

                } catch (Throwable t) {
                    MIA.log.writeError(t);
                }

                writeProgressStatus(count.incrementAndGet(), total, "objects");

            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (Throwable t) {
            MIA.log.writeError(t);
            return Status.FAIL;
        }

        if (showOutput) {
            inputObjects.showMeasurements(this, modules);
            if (addToWorkspace)
                edgeObjects.showMeasurements(this, modules);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(ADD_SKELETONS_TO_WORKSPACE, this, false));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_SKELETON_OBJECTS, this));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_EDGE_OBJECTS, this));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_JUNCTION_OBJECTS, this));
        parameters.add(new BooleanP(EXPORT_LOOP_OBJECTS, this, false));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_LOOP_OBJECTS, this));
        parameters.add(new BooleanP(EXPORT_LARGEST_SHORTEST_PATH, this, false));
        parameters.add(new OutputObjectsP(OUTPUT_LARGEST_SHORTEST_PATH, this));

        parameters.add(new SeparatorP(SKELETONISATION_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_BRANCH_LENGTH, this, 0d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_SKELETONS_TO_WORKSPACE));

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SKELETON_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_JUNCTION_OBJECTS));

            returnedParameters.add(parameters.getParameter(EXPORT_LOOP_OBJECTS));
            if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS))
                returnedParameters.add(parameters.getParameter(OUTPUT_LOOP_OBJECTS));

        }

        returnedParameters.add(parameters.getParameter(EXPORT_LARGEST_SHORTEST_PATH));
        if ((boolean) parameters.getValue(EXPORT_LARGEST_SHORTEST_PATH))
            returnedParameters.add(parameters.getParameter(OUTPUT_LARGEST_SHORTEST_PATH));

        returnedParameters.add(parameters.getParameter(SKELETONISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_BRANCH_LENGTH));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.sumLengthPx);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);
        ref = objectMeasurementRefs.getOrPut(Measurements.sumLengthCal);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE)) {
            String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
            ref = objectMeasurementRefs.getOrPut(Measurements.edgeLengthPx);
            ref.setObjectsName(edgeObjectsName);
            returnedRefs.add(ref);
            ref = objectMeasurementRefs.getOrPut(Measurements.edgeLengthCal);
            ref.setObjectsName(edgeObjectsName);
            returnedRefs.add(ref);
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        ParentChildRefs returnedRefs = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS);
        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);
        String largestShortestPathName = parameters.getValue(OUTPUT_LARGEST_SHORTEST_PATH);

        returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, skeletonObjectsName));
        returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, edgeObjectsName));
        returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, junctionObjectsName));
        if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS))
            returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, loopObjectsName));

        if ((boolean) parameters.getValue(EXPORT_LARGEST_SHORTEST_PATH))
            returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, largestShortestPathName));

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        PartnerRefs returnedRefs = new PartnerRefs();

        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);

        returnedRefs.add(partnerRefs.getOrPut(edgeObjectsName, junctionObjectsName));
        if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS)) {
            returnedRefs.add(partnerRefs.getOrPut(edgeObjectsName, loopObjectsName));
            returnedRefs.add(partnerRefs.getOrPut(junctionObjectsName, loopObjectsName));
        }

        return returnedRefs;

    }

    @Override
    public String getDescription() {
        return "Creates and measures the skeletonised form of specified input objects.  This module uses the <a href=\"https://imagej.net/AnalyzeSkeleton\">AnalyzeSkeleton</a> plugin by Ignacio Arganda-Carreras."
                + "<br><br>The optional, output skeleton object acts solely as a linking object for the edge, junction and loop objects.  It doesn't itself hold any coordinate data.";
    }

    @Override
    public boolean verify() {
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Input objects from the workspace to be skeletonised.  These can be either 2D or 3D objects.  Skeleton measurements will be added to this object.");

        parameters.get(ADD_SKELETONS_TO_WORKSPACE).setDescription(
                "When selected, the coordinates for the various skeleton components (edges, junctions and loops) will be stored as new objects.  These objects will all be children of a parent \"Skeleton\" object, which itself will be a child of the corresponding input object.");

        parameters.get(OUTPUT_SKELETON_OBJECTS).setDescription("If \"" + ADD_SKELETONS_TO_WORKSPACE
                + "\" is selected, a single \"Skeleton\" object will be created per input object.  This skeleton object will act as a linking object (parent) for the edges, junctions and loops that comprise that skeleton.  As such, the skeleton object itself doesn't store any coordinate information.");

        parameters.get(OUTPUT_EDGE_OBJECTS).setDescription("If \"" + ADD_SKELETONS_TO_WORKSPACE
                + "\" is selected, the edges of each skeleton will be stored in these objects.  An \"Edge\" is comprised of a continuous run of points each with one (end points) or two neighbours.  These edge objects are children of a \"Skeleton\" object (specified by the \""
                + OUTPUT_SKELETON_OBJECTS
                + "\" parameter), which itself is the child of the corresponding input object.  Each edge object has a partner relationship with its adjacent \"Junction\" and (optionally) \"Loop\" objects (specified by the \""
                + OUTPUT_JUNCTION_OBJECTS + "\" and \"" + OUTPUT_LOOP_OBJECTS + "\" parameters, respectively).");

        parameters.get(OUTPUT_JUNCTION_OBJECTS).setDescription("If \"" + ADD_SKELETONS_TO_WORKSPACE
                + "\" is selected, the junctions of each skeleton will be stored in these objects.  A \"Junction\" is comprised of a contiguous regions of points each with three or neighbours.  These junction objects are children of a \"Skeleton\" object (specified by the \""
                + OUTPUT_SKELETON_OBJECTS
                + "\" parameter), which itself is the child of the corresponding input object.  Each junction object has a partner relationship with its adjacent \"Edge\" and (optionally) \"Loop\" objects (specified by the \""
                + OUTPUT_EDGE_OBJECTS + "\" and \"" + OUTPUT_LOOP_OBJECTS + "\" parameters, respectively).");

        parameters.get(EXPORT_LOOP_OBJECTS).setDescription("When selected (and if \"" + ADD_SKELETONS_TO_WORKSPACE
                + "\" is also selected), the loops of each skeleton will be stored in the workspace as new objects.  The name for the output loop objects is determined by the \""
                + OUTPUT_LOOP_OBJECTS + "\" parameter.");

        parameters.get(OUTPUT_LOOP_OBJECTS).setDescription("If both \"" + ADD_SKELETONS_TO_WORKSPACE + "\" and \""
                + EXPORT_LOOP_OBJECTS
                + "\" are selected, the loops of each skeleton will be stored in these objects.  A \"Loop\" is comprised of a continuous region of points bounded on all sides by either \"Edge\" or \"Junction\" points.  These loop objects are children of a \"Skeleton\" object (specified by the \""
                + OUTPUT_SKELETON_OBJECTS
                + "\" parameter), which itself is the child of the corresponding input object.  Each loop object has a partner relationship with its adjacent \"Edge\" and \"Junction\" objects (specified by the \""
                + OUTPUT_EDGE_OBJECTS + "\" and \"" + OUTPUT_JUNCTION_OBJECTS + "\" parameters, respectively).");

        parameters.get(EXPORT_LARGEST_SHORTEST_PATH).setDescription(
                "When selected, the largest shortest path between any two points in the skeleton will be stored in the workspace as a new object.  For each input object, the shortest path between all point pairs within the skeleton is calculated and the largest of all these paths stored as a new object.  The name for the output largest shortest path object associated with each input object is determined by the \""
                        + OUTPUT_LARGEST_SHORTEST_PATH
                        + "\" parameter.  <a href=\"https://imagej.net/plugins/analyze-skeleton/\">Analyse Skeleton</a> calculates the largest shortest path using <a href=\"https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm\">Floyd-Warshall algorithm</a>.  Note: These objects are not the same as the <a href=\"https://en.wikipedia.org/wiki/Longest_path_problem\">longest possible path</a>.");

        parameters.get(OUTPUT_LARGEST_SHORTEST_PATH).setDescription("If \"" + EXPORT_LARGEST_SHORTEST_PATH
                + "\"is selected, the largest shortest path for each skeleton will be stored in the workspace.  For each skeleton, the shortest path between all point pairs is calculated; the largest shortest path is the longest of all these paths.  The largest shortest path objects are children of the corresponding input object.");

        parameters.get(MINIMUM_BRANCH_LENGTH).setDescription(
                "The minimum length of a branch (edge terminating in point with just one neighbour) for it to be included in skeleton measurements and (optionally) exported as an object.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, spatial values are assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Break the image down into strips, each one processed on a separate CPU thread.  The overhead required to do this means it's best for large multi-core CPUs, but should be left disabled for small images or on CPUs with few cores.");

    }
}
