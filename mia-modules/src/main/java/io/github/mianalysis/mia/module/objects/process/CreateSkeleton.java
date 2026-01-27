package io.github.mianalysis.mia.module.objects.process;

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
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.images.process.binary.Skeletonise;
import io.github.mianalysis.mia.module.objects.detect.IdentifyObjects;
import io.github.mianalysis.mia.module.objects.filter.FilterOnImageEdge;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.OutputSkeletonObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Edge;
import sc.fiji.analyzeSkeleton.Graph;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import sc.fiji.analyzeSkeleton.Vertex;

/**
 * Creates and measures the skeletonised form of specified input objects. This
 * module uses the
 * <a href="https://imagej.net/AnalyzeSkeleton">AnalyzeSkeleton</a> plugin by
 * Ignacio Arganda-Carreras.<br>
 * <br>
 * The optional, output skeleton object acts solely as a linking object for the
 * edge, junction and loop objects. It doesn't itself hold any coordinate data.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateSkeleton extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image/object input";

    /**
     * Input image from the workspace to be skeletonised.
     */
    public static final String INPUT_MODE = "Input mode";

    /**
     * Input image from the workspace to be skeletonised.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Controls whether objects are considered to be white (255 intensity) on a
     * black (0 intensity) background, or black on a white background.
     */
    public static final String BINARY_LOGIC = "Binary logic";

    /**
     * Input objects from the workspace to be skeletonised. These can be either 2D
     * or 3D objects. Skeleton measurements will be added to this object.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
     * When selected, the coordinates for the various skeleton components (edges,
     * junctions and loops) will be stored as new objects. These objects will all be
     * children of a parent "Skeleton" object, which itself will be a child of the
     * corresponding input object.
     */
    public static final String ADD_SKELETONS_TO_WORKSPACE = "Add skeletons to workspace";

    /**
     * If "Add skeletons to workspace" is selected, a single "Skeleton" object will
     * be created per input object. This skeleton object will act as a linking
     * object (parent) for the edges, junctions and loops that comprise that
     * skeleton. As such, the skeleton object itself doesn't store any coordinate
     * information.
     */
    public static final String OUTPUT_SKELETON_OBJECTS = "Output skeleton objects";

    /**
     * If "Add skeletons to workspace" is selected, the edges of each skeleton will
     * be stored in these objects. An "Edge" is comprised of a continuous run of
     * points each with one (end points) or two neighbours. These edge objects are
     * children of a "Skeleton" object (specified by the "Output skeleton objects"
     * parameter), which itself is the child of the corresponding input object. Each
     * edge object has a partner relationship with its adjacent "Junction" and
     * (optionally) "Loop" objects (specified by the "Output junction objects" and
     * "Output loop objects" parameters, respectively).
     */
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";

    /**
     * If "Add skeletons to workspace" is selected, the junctions of each skeleton
     * will be stored in these objects. A "Junction" is comprised of a contiguous
     * regions of points each with three or neighbours. These junction objects are
     * children of a "Skeleton" object (specified by the "Output skeleton objects"
     * parameter), which itself is the child of the corresponding input object. Each
     * junction object has a partner relationship with its adjacent "Edge" and
     * (optionally) "Loop" objects (specified by the "Output edge objects" and
     * "Output loop objects" parameters, respectively).
     */
    public static final String OUTPUT_JUNCTION_OBJECTS = "Output junction objects";

    /**
     * When selected (and if "Add skeletons to workspace" is also selected), the
     * loops of each skeleton will be stored in the workspace as new objects. The
     * name for the output loop objects is determined by the "Output loop objects"
     * parameter.
     */
    public static final String EXPORT_LOOP_OBJECTS = "Export loop objects";

    /**
     * If both "Add skeletons to workspace" and "Export loop objects" are selected,
     * the loops of each skeleton will be stored in these objects. A "Loop" is
     * comprised of a continuous region of points bounded on all sides by either
     * "Edge" or "Junction" points. These loop objects are children of a "Skeleton"
     * object (specified by the "Output skeleton objects" parameter), which itself
     * is the child of the corresponding input object. Each loop object has a
     * partner relationship with its adjacent "Edge" and "Junction" objects
     * (specified by the "Output edge objects" and "Output junction objects"
     * parameters, respectively).
     */
    public static final String OUTPUT_LOOP_OBJECTS = "Output loop objects";

    /**
     * When selected, the largest shortest path between any two points in the
     * skeleton will be stored in the workspace as a new object. For each input
     * object, the shortest path between all point pairs within the skeleton is
     * calculated and the largest of all these paths stored as a new object. The
     * name for the output largest shortest path object associated with each input
     * object is determined by the "Output largest shortest path" parameter.
     * <a href="https://imagej.net/plugins/analyze-skeleton/">Analyse Skeleton</a>
     * calculates the largest shortest path using <a href=
     * "https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm">Floyd-Warshall
     * algorithm</a>. Note: These objects are not the same as the
     * <a href="https://en.wikipedia.org/wiki/Longest_path_problem">longest possible
     * path</a>.
     */
    public static final String EXPORT_LARGEST_SHORTEST_PATH = "Export largest shortest path";

    /**
     * If "Export largest shortest path"is selected, the largest shortest path for
     * each skeleton will be stored in the workspace. For each skeleton, the
     * shortest path between all point pairs is calculated; the largest shortest
     * path is the longest of all these paths. The largest shortest path objects are
     * children of the corresponding input object.
     */
    public static final String OUTPUT_LARGEST_SHORTEST_PATH = "Output largest shortest path";

    /**
    * 
    */
    public static final String SKELETONISATION_SEPARATOR = "Skeletonisation settings";

    /**
     * The minimum length of a branch (edge terminating in point with just one
     * neighbour) for it to be included in skeleton measurements and (optionally)
     * exported as an object.
     */
    public static final String MINIMUM_BRANCH_LENGTH = "Minimum branch length";

    /**
     * When selected, spatial values are assumed to be specified in calibrated units
     * (as defined by the "Input control" parameter "Spatial unit"). Otherwise,
     * pixel units are assumed.
     */
    public static final String CALIBRATED_UNITS = "Calibrated units";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Break the image down into strips, each one processed on a separate CPU
     * thread. The overhead required to do this means it's best for large multi-core
     * CPUs, but should be left disabled for small images or on CPUs with few cores.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface InputModes {
        String IMAGE = "Image";
        String OBJECTS = "Objects";

        String[] ALL = new String[] { IMAGE, OBJECTS };

    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface Measurements {
        String SUM_LENGTH_PX = "SKELETON // SUM_LENGTH_(PX)";
        String SUM_LENGTH_CAL = "SKELETON // SUM_LENGTH_(${SCAL})";
        String EDGE_LENGTH_PX = "SKELETON // LENGTH_(PX)";
        String EDGE_LENGTH_CAL = "SKELETON // LENGTH_(${SCAL})";

    }

    public CreateSkeleton(Modules modules) {
        super("Create skeleton", modules);
    }

    public static ImageI getSkeletonImage(ObjI inputObject) {
        // Getting tight image of object
        ImageI skeletonImage = inputObject.getAsTightImage("Skeleton");

        // Running 3D skeletonisation
        Skeletonise.process(skeletonImage, true);

        return skeletonImage;

    }

    public static Object[] initialiseAnalyzer(ObjI inputObject, double minLengthFinal,
            boolean exportLargestShortestPathFinal) {
        ImageI skeletonImage = getSkeletonImage(inputObject);

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

    public static ObjI createEdgeJunctionObjects(ObjI inputObject, SkeletonResult result, ObjsI skeletonObjects,
            ObjsI edgeObjects,
            ObjsI junctionObjects) {
        return createEdgeJunctionObjects(inputObject, result, skeletonObjects, edgeObjects, junctionObjects, true);

    }

    public static ObjI createEdgeJunctionObjects(ObjI inputObject, SkeletonResult result, ObjsI skeletonObjects,
            ObjsI edgeObjects,
            ObjsI junctionObjects, boolean addRelationship) {

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        // The Skeleton object links branches, junctions and loops.
        ObjI skeletonObject = skeletonObjects.createAndAddNewObject(new PointListFactory());
        skeletonObject.setT(inputObject.getT());
        if (addRelationship) {
            inputObject.addChild(skeletonObject);
            skeletonObject.addParent(inputObject);
        }

        // For the purpose of linking edges and junctions, these are stored in a
        // HashMap.
        HashMap<Edge, ObjI> edgeObjs = new HashMap<>();
        HashMap<Vertex, ObjI> junctionObjs = new HashMap<>();

        // Creating objects
        double dppXY = inputObject.getDppXY();
        for (Graph graph : result.getGraph()) {
            for (Edge edge : graph.getEdges()) {
                ObjI edgeObj = createEdgeObject(skeletonObject, edgeObjects, edge, xOffs, yOffs, zOffs);
                edgeObjs.put(edge, edgeObj);

                // Adding edge length measurements
                double calLength = edge.getLength();
                MeasurementI lengthPx = MeasurementFactories.getDefaultFactory().createMeasurement(Measurements.EDGE_LENGTH_PX, calLength / dppXY);
                edgeObj.addMeasurement(lengthPx);
                MeasurementI lengthCal = MeasurementFactories.getDefaultFactory().createMeasurement(Measurements.EDGE_LENGTH_CAL, calLength);
                edgeObj.addMeasurement(lengthCal);

            }

            for (Vertex junction : graph.getVertices()) {
                ObjI junctionObj = createJunctionObject(skeletonObject, junctionObjects, junction, xOffs, yOffs, zOffs);
                junctionObjs.put(junction, junctionObj);
            }
        }

        // Applying partnerships between edges and junctions
        applyEdgeJunctionPartnerships(edgeObjs, junctionObjs);

        // Returning skeleton (linking) object
        return skeletonObject;

    }

    public static void createLoopObjects(ObjsI loopObjects, String edgeObjectsName, String junctionObjectsName,
            String loopObjectsName, ObjI skeletonObject) {

        // Creating an object for the entire skeleton
        ObjsI tempCollection = ObjsFactories.getDefaultFactory().createFromExample("Skeleton", loopObjects);
        ObjI tempObject = tempCollection.createAndAddNewObject(new PointListFactory());
        CoordinateSetI coords = tempObject.getCoordinateSet();

        // Adding all points from edges and junctions
        for (ObjI edgeObject : skeletonObject.getChildren(edgeObjectsName).values())
            coords.addAll(edgeObject.getCoordinateSet());

        for (ObjI junctionObject : skeletonObject.getChildren(junctionObjectsName).values())
            coords.addAll(junctionObject.getCoordinateSet());

        // Creating a binary image of all the points with a 1px border, so we can remove
        // objects on the image edge still
        int[][] borders = new int[][] { { 1, 1 }, { 1, 1 }, { 0, 0 } };
        ImageI binaryImage = tempObject.getAsTightImageWithBorders("outputName", borders);

        // Converting binary image to loop objects
        ObjsI tempLoopObjects = IdentifyObjects.process(binaryImage, loopObjectsName, false, false,
                IdentifyObjects.DetectionModes.THREE_D, 6,
                new QuadtreeFactory(), false, 0, false);

        // Removing any objects on the image edge, as these aren't loops
        FilterOnImageEdge.process(tempLoopObjects, 0, null, false, true, null);

        // Shifting objects back to the correct positions
        double[][] extents = tempObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]) - 1;
        int yOffs = (int) Math.round(extents[1][0]) - 1;
        int zOffs = (int) Math.round(extents[2][0]);
        tempLoopObjects.setCalibrationFromExample(loopObjects, true);

        for (ObjI tempLoopObject : tempLoopObjects.values())
            tempLoopObject.translateCoords(xOffs, yOffs, zOffs);

        for (ObjI tempLoopObject : tempLoopObjects.values()) {
            tempLoopObject.setID(loopObjects.getAndIncrementID());
            tempLoopObject.addParent(skeletonObject);
            skeletonObject.addChild(tempLoopObject);
            loopObjects.add(tempLoopObject);
        }
    }

    public static ObjI createEdgeObject(ObjI skeletonObject, ObjsI edgeObjects, Edge edge, int xOffs, int yOffs,
            int zOffs) {
        ObjI edgeObject = edgeObjects.createAndAddNewObject(new PointListFactory());
        edgeObject.setT(skeletonObject.getT());
        skeletonObject.addChild(edgeObject);
        edgeObject.addParent(skeletonObject);

        // Adding coordinates
        for (Point point : edge.getSlabs()) {
            try {
                skeletonObject.addCoord(point.x + xOffs, point.y + yOffs, point.z + zOffs);
                edgeObject.addCoord(point.x + xOffs, point.y + yOffs, point.z + zOffs);
            } catch (PointOutOfRangeException e) {
            }
        }

        return edgeObject;

    }

    public static ObjI createJunctionObject(ObjI skeletonObject, ObjsI junctionObjects, Vertex junction, int xOffs,
            int yOffs,
            int zOffs) {
        ObjI junctionObject = junctionObjects.createAndAddNewObject(new PointListFactory());
        junctionObject.setT(skeletonObject.getT());
        skeletonObject.addChild(junctionObject);
        junctionObject.addParent(skeletonObject);

        // Adding coordinates
        for (Point point : junction.getPoints()) {
            try {
                skeletonObject.addCoord(point.x + xOffs, point.y + yOffs, point.z + zOffs);
                junctionObject.addCoord(point.x + xOffs, point.y + yOffs, point.z + zOffs);
            } catch (PointOutOfRangeException e) {
            }
        }

        return junctionObject;

    }

    public static ArrayList<io.github.mianalysis.mia.object.coordinates.Point<Integer>> getLargestShortestPath(
            ObjI inputObject) {
        Object[] result = initialiseAnalyzer(inputObject, 0, true);
        AnalyzeSkeleton_ analyzeSkeleton = (AnalyzeSkeleton_) result[0];
        SkeletonResult skeletonResult = (SkeletonResult) result[1];

        return getLargestShortestPath(inputObject, analyzeSkeleton, skeletonResult);

    }

    public static ArrayList<io.github.mianalysis.mia.object.coordinates.Point<Integer>> getLargestShortestPath(
            ObjI inputObject,
            AnalyzeSkeleton_ analyzeSkeleton, SkeletonResult skeletonResult) {
        ArrayList<io.github.mianalysis.mia.object.coordinates.Point<Integer>> points2 = new ArrayList<>();

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
            points2.add(new io.github.mianalysis.mia.object.coordinates.Point<Integer>(point.x + xOffs, point.y + yOffs,
                    point.z + zOffs));

        return points2;

    }

    static void createLargestShortestPath(ObjI inputObject, ObjsI largestShortestPathObjects,
            AnalyzeSkeleton_ analyzeSkeleton, SkeletonResult skeletonResult, boolean addRelationship) {

        ArrayList<io.github.mianalysis.mia.object.coordinates.Point<Integer>> points = getLargestShortestPath(
                inputObject,
                analyzeSkeleton, skeletonResult);

        ObjI largestShortestPath = largestShortestPathObjects.createAndAddNewObject(new PointListFactory());
        largestShortestPath.getCoordinateSet().addAll(points);
        largestShortestPath.setT(inputObject.getT());

        if (addRelationship) {
            largestShortestPath.addParent(inputObject);
            inputObject.addChild(largestShortestPath);
        }
    }

    static void applyEdgeJunctionPartnerships(HashMap<Edge, ObjI> edgeObjs, HashMap<Vertex, ObjI> junctionObjs) {
        // Iterating over each edge, adding the two vertices at either end as partners
        for (Edge edge : edgeObjs.keySet()) {
            ObjI edgeObject = edgeObjs.get(edge);
            ObjI junction1 = junctionObjs.get(edge.getV1());
            ObjI junction2 = junctionObjs.get(edge.getV2());

            edgeObject.addPartner(junction1);
            junction1.addPartner(edgeObject);
            edgeObject.addPartner(junction2);
            junction2.addPartner(edgeObject);

        }
    }

    static void applyLoopPartnerships(ObjsI loopObjects, ObjsI edgeObjects, ObjsI junctionObjects) {
        // Linking junctions and loops with surfaces separated by 1px or less
        for (ObjI loopObject : loopObjects.values()) {
            for (ObjI junctionObject : junctionObjects.values()) {
                if (loopObject.getSurfaceSeparation(junctionObject, true, false, false, false) <= 1) {
                    loopObject.addPartner(junctionObject);
                    junctionObject.addPartner(loopObject);
                }
            }
        }

        // Linking edges with both junctions linked to the loop
        for (ObjI loopObject : loopObjects.values()) {
            for (ObjI edgeObject : edgeObjects.values()) {
                ObjsI junctionPartners = edgeObject.getPartners(junctionObjects.getName());
                boolean matchFound = true;

                for (ObjI junctionPartnerObject : junctionPartners.values()) {
                    ObjsI loopPartners = junctionPartnerObject.getPartners(loopObjects.getName());

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

    static void addMeasurements(ObjI inputObject, SkeletonResult result) {
        double length = 0;

        // If the skeleton has no voxels (edge, end or junction), the graphs will be
        // null
        if (result.getGraph() != null) {
            for (Graph graph : result.getGraph()) {
                for (Edge edge : graph.getEdges())
                    length = length + edge.getLength();
            }
        }

        double dppXY = inputObject.getDppXY();
        inputObject.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement(Measurements.SUM_LENGTH_PX, length / dppXY));
        inputObject.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement(Measurements.SUM_LENGTH_CAL, length));

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public Status process(WorkspaceI workspace) {
        String inputMode = parameters.getValue(INPUT_MODE, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        boolean addToWorkspace = (boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE, workspace)
                || inputMode.equals(InputModes.IMAGE);
        String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS, workspace);
        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS, workspace);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS, workspace);
        boolean exportLoops = parameters.getValue(EXPORT_LOOP_OBJECTS, workspace);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS, workspace);
        boolean exportLargestShortestPath = parameters.getValue(EXPORT_LARGEST_SHORTEST_PATH, workspace);
        String largestShortestPathName = parameters.getValue(OUTPUT_LARGEST_SHORTEST_PATH, workspace);
        double minLength = parameters.getValue(MINIMUM_BRANCH_LENGTH, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // If processing an image, create a temporary object set
        ObjsI inputObjects;
        switch (inputMode) {
            case InputModes.IMAGE:
                ImageI inputImage = workspace.getImage(inputImageName);
                boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
                String detectionMode = IdentifyObjects.DetectionModes.THREE_D;
                CoordinateSetFactoryI factory = new QuadtreeFactory();
                inputObjects = IdentifyObjects.process(inputImage, "TempObjects", blackBackground, false, detectionMode,
                        26, factory, multithread, 60, false);
                break;
            case InputModes.OBJECTS:
            default:
                inputObjects = workspace.getObjects(inputObjectsName);
                break;
        }

        if (inputObjects == null || inputObjects.size() == 0)
            return Status.PASS;

        // If necessary, converting to calibrated units (Skeletonise takes calibrated
        // measurements, so unlike most modules, we want to convert to calibrated units)
        if (!calibratedUnits)
            minLength = minLength * inputObjects.getDppXY();

        // Creating empty output object collections
        final ObjsI skeletonObjects = addToWorkspace ? ObjsFactories.getDefaultFactory().createFromExample(skeletonObjectsName, inputObjects) : null;
        final ObjsI edgeObjects = addToWorkspace ? ObjsFactories.getDefaultFactory().createFromExample(edgeObjectsName, inputObjects) : null;
        final ObjsI junctionObjects = addToWorkspace ? ObjsFactories.getDefaultFactory().createFromExample(junctionObjectsName, inputObjects) : null;
        final ObjsI loopObjects = addToWorkspace & exportLoops ? ObjsFactories.getDefaultFactory().createFromExample(loopObjectsName, inputObjects) : null;
        final ObjsI largestShortestPathObjects = exportLargestShortestPath
                ? ObjsFactories.getDefaultFactory().createFromExample(largestShortestPathName, inputObjects)
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
        for (ObjI inputObject : inputObjects.values()) {
            Runnable task = () -> {
                try {
                    Object[] result = initialiseAnalyzer(inputObject, minLengthFinal, exportLargestShortestPathFinal);

                    // Adding the skeleton to the input object
                    if (addToWorkspace) {

                        ObjI skeletonObject = createEdgeJunctionObjects(inputObject, (SkeletonResult) result[1],
                                skeletonObjects, edgeObjects, junctionObjects, inputMode.equals(InputModes.OBJECTS));

                        // Creating loop objects
                        if (exportLoops)
                            createLoopObjects(loopObjects, edgeObjectsName, junctionObjectsName, loopObjectsName,
                                    skeletonObject);

                    }

                    if (exportLargestShortestPathFinal)
                        createLargestShortestPath(inputObject, largestShortestPathObjects, (AnalyzeSkeleton_) result[0],
                                (SkeletonResult) result[1], inputMode.equals(InputModes.OBJECTS));

                    if (((SkeletonResult) result[1]) != null && inputMode.equals(InputModes.OBJECTS))
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
        } catch (InterruptedException e) {
            return Status.FAIL;
        } catch (Throwable t) {
            MIA.log.writeError(t);
            return Status.FAIL;
        }

        if (exportLoops)
            applyLoopPartnerships(loopObjects, edgeObjects, junctionObjects);

        if (showOutput) {
            switch (inputMode) {
                case InputModes.IMAGE:
                    edgeObjects.showMeasurements(this, modules);
                    skeletonObjects.convertToImageIDColours().showWithNormalisation(false);
                    break;

                case InputModes.OBJECTS:
                    inputObjects.showMeasurements(this, modules);
                    if (addToWorkspace) {
                        edgeObjects.showMeasurements(this, modules);
                        skeletonObjects.convertToImageIDColours().showWithNormalisation(false);
                    }
                    break;
            }
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(INPUT_MODE, this, InputModes.OBJECTS, InputModes.ALL));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
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
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_MODE));
        switch ((String) parameters.getValue(INPUT_MODE, workspace)) {
            case InputModes.IMAGE:
                returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
                returnedParameters.add(parameters.getParameter(BINARY_LOGIC));
                break;
            case InputModes.OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));

        if (((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.OBJECTS))
            returnedParameters.add(parameters.getParameter(ADD_SKELETONS_TO_WORKSPACE));

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE, workspace)
                || ((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.IMAGE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SKELETON_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_JUNCTION_OBJECTS));

            returnedParameters.add(parameters.getParameter(EXPORT_LOOP_OBJECTS));
            if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS, workspace))
                returnedParameters.add(parameters.getParameter(OUTPUT_LOOP_OBJECTS));

        }

        returnedParameters.add(parameters.getParameter(EXPORT_LARGEST_SHORTEST_PATH));
        if ((boolean) parameters.getValue(EXPORT_LARGEST_SHORTEST_PATH, workspace))
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
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        if (((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.OBJECTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.SUM_LENGTH_PX);
            ref.setObjectsName(inputObjectsName);
            returnedRefs.add(ref);
            ref = objectMeasurementRefs.getOrPut(Measurements.SUM_LENGTH_CAL);
            ref.setObjectsName(inputObjectsName);
            returnedRefs.add(ref);
        }

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE, workspace)
                || ((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.IMAGE)) {
            String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS, workspace);
            ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.EDGE_LENGTH_PX);
            ref.setObjectsName(edgeObjectsName);
            returnedRefs.add(ref);
            ref = objectMeasurementRefs.getOrPut(Measurements.EDGE_LENGTH_CAL);
            ref.setObjectsName(edgeObjectsName);
            returnedRefs.add(ref);
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        WorkspaceI workspace = null;
        ParentChildRefs returnedRefs = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE, workspace)
                || ((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.IMAGE)) {
            String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS, workspace);
            String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS, workspace);
            String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS, workspace);
            String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS, workspace);

            if (((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.OBJECTS))
                returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, skeletonObjectsName));

            returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, edgeObjectsName));
            returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, junctionObjectsName));
            if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS, workspace))
                returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, loopObjectsName));

        }

        if ((boolean) parameters.getValue(EXPORT_LARGEST_SHORTEST_PATH, workspace)
                && ((String) parameters.getValue(INPUT_MODE, workspace)).equals(InputModes.OBJECTS)) {
            String largestShortestPathName = parameters.getValue(OUTPUT_LARGEST_SHORTEST_PATH, workspace);
            returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, largestShortestPathName));

        }

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        WorkspaceI workspace = null;
        PartnerRefs returnedRefs = new PartnerRefs();

        if ((boolean) parameters.getValue(ADD_SKELETONS_TO_WORKSPACE, workspace)) {
            String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS, workspace);
            String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS, workspace);
            String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS, workspace);

            returnedRefs.add(partnerRefs.getOrPut(edgeObjectsName, junctionObjectsName));
            if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS, workspace)) {
                returnedRefs.add(partnerRefs.getOrPut(edgeObjectsName, loopObjectsName));
                returnedRefs.add(partnerRefs.getOrPut(junctionObjectsName, loopObjectsName));
            }
        }

        return returnedRefs;

    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Creates and measures the skeletonised form of specified input objects.  This module uses the <a href=\"https://imagej.net/AnalyzeSkeleton\">AnalyzeSkeleton</a> plugin by Ignacio Arganda-Carreras."
                + "<br><br>The optional, output skeleton object acts solely as a linking object for the edge, junction and loop objects.  It doesn't itself hold any coordinate data.";
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Controls whether the skeleton will be created from existing objects in the workspace or taken from a binary image.");

        parameters.get(INPUT_IMAGE).setDescription(
                "Input image from the workspace to be skeletonised.");

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
