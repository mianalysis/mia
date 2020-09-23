package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.IJ;
import ij.Prefs;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Edge;
import sc.fiji.analyzeSkeleton.Graph;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import sc.fiji.analyzeSkeleton.Vertex;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.Skeletonise;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects.FilterOnImageEdge;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputSkeletonObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.CoordinateSet;
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
    public static final String EXPORT_LOOP_OBJECTS = "Export loop objects";
    public static final String OUTPUT_LOOP_OBJECTS = "Output loop objects";
    public static final String ANALYSIS_SEPARATOR = "Analysis settings";
    public static final String MINIMUM_BRANCH_LENGTH = "Minimum branch length";
    public static final String CALIBRATED_UNITS = "Calibrated units";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface Measurements {
        String edgeLengthPx = "SKELETON // LENGTH_(PX)";
        String edgeLengthCal = "SKELETON // LENGTH_(${CAL})";

    }

    public MeasureSkeleton(ModuleCollection modules) {
        super("Measure skeleton", modules);
    }

    static Image getSkeletonImage(Obj inputObject) {
        // Getting tight image of object
        Image skeletonImage = inputObject.getAsTightImage("Skeleton");
        // Inverting, so the objects are black on white
        InvertIntensity.process(skeletonImage);

        // Running 3D skeletonisation
        Skeletonise.process(skeletonImage);

        // Inverting, so the objects are white on black
        InvertIntensity.process(skeletonImage);

        return skeletonImage;

    }

    static Obj createEdgeJunctionObjects(Obj inputObject, SkeletonResult result, ObjCollection skeletonObjects,
            ObjCollection edgeObjects, ObjCollection junctionObjects) {

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        // The Skeleton object doesn't contain any coordinate data, it just links
        // branches, junctions and loops.
        Obj skeletonObject = new Obj(VolumeType.POINTLIST, skeletonObjects.getName(), inputObject.getID(), inputObject);
        skeletonObject.setT(inputObject.getT());
        inputObject.addChild(skeletonObject);
        skeletonObject.addParent(inputObject);
        skeletonObjects.add(skeletonObject);

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
                Measurement lengthCal = new Measurement(Units.replace(Measurements.edgeLengthCal), calLength);
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

    static void createLoopObjects(ObjCollection loopObjects, String edgeObjectsName, String junctionObjectsName,
            String loopObjectsName, Obj skeletonObject) {

        // Creating an object for the entire skeleton
        Obj tempObject = new Obj("Temp", 1, skeletonObject);
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
        ObjCollection tempLoopObjects = IdentifyObjects.process(binaryImage, loopObjectsName, true, false, 6,
                Image.VolumeTypes.QUADTREE, false, 0, false);

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

    static Obj createEdgeObject(Obj skeletonObject, ObjCollection edgeObjects, Edge edge, int xOffs, int yOffs,
            int zOffs) {
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

    static Obj createJunctionObject(Obj skeletonObject, ObjCollection junctionObjects, Vertex junction, int xOffs,
            int yOffs, int zOffs) {
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

    static void applyLoopPartnerships(ObjCollection loopObjects, ObjCollection edgeObjects,
            ObjCollection junctionObjects) {
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
                ObjCollection junctionPartners = edgeObject.getPartners(junctionObjects.getName());
                boolean matchFound = true;

                for (Obj junctionPartnerObject : junctionPartners.values()) {
                    ObjCollection loopPartners = junctionPartnerObject.getPartners(loopObjects.getName());

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

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        boolean addToWorkspace = parameters.getValue(ADD_SKELETONS_TO_WORKSPACE);
        String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS);
        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        boolean exportLoops = parameters.getValue(EXPORT_LOOP_OBJECTS);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);
        double minLength = parameters.getValue(MINIMUM_BRANCH_LENGTH);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If necessary, converting to calibrated units (Skeletonise takes calibrated
        // measurements, so unlike most modules, we want to convert to calibrated units)
        if (!calibratedUnits)
            minLength = minLength * inputObjects.getFirst().getDppXY();

        // Creating empty output object collections
        final ObjCollection skeletonObjects = addToWorkspace ? new ObjCollection(skeletonObjectsName, inputObjects)
                : null;
        final ObjCollection edgeObjects = addToWorkspace ? new ObjCollection(edgeObjectsName, inputObjects) : null;
        final ObjCollection junctionObjects = addToWorkspace ? new ObjCollection(junctionObjectsName, inputObjects)
                : null;
        final ObjCollection loopObjects = addToWorkspace & exportLoops
                ? new ObjCollection(loopObjectsName, inputObjects)
                : null;

        if (addToWorkspace) {
            workspace.addObjects(skeletonObjects);
            workspace.addObjects(edgeObjects);
            workspace.addObjects(junctionObjects);
            if (exportLoops)
                workspace.addObjects(loopObjects);
        }

        // Configuring multithreading
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int total = inputObjects.size();
        AtomicInteger count = new AtomicInteger();

        // For each object, create a child projected object, then generate a binary
        // image. This is run through ImageJ's skeletonize plugin to ensure it has 4-way
        // connectivity. Finally, it is processed with the AnalyzeSkeleton plugin.
        final double minLengthFinal = minLength;
        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                Image skeletonImage = getSkeletonImage(inputObject);

                AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
                analyzeSkeleton.setup("", skeletonImage.getImagePlus());
                SkeletonResult skeletonResult = analyzeSkeleton.run(AnalyzeSkeleton_.NONE, minLengthFinal, false,
                        skeletonImage.getImagePlus(), true, false);

                // Adding the skeleton to the input object
                if (addToWorkspace) {
                    try {
                        Obj skeletonObject = createEdgeJunctionObjects(inputObject, skeletonResult, skeletonObjects,
                                edgeObjects, junctionObjects);

                        // Creating loop objects
                        if (exportLoops) {
                            createLoopObjects(loopObjects, edgeObjectsName, junctionObjectsName, loopObjectsName,
                                    skeletonObject);
                            workspace.addObjects(loopObjects);
                            applyLoopPartnerships(loopObjects, edgeObjects, junctionObjects);
                        }

                    } catch (IntegerOverflowException e) {
                        e.printStackTrace();
                    }
                }

                writeStatus("Processed " + count + " of " + total + " ("
                        + Math.floorDiv(100 * count.getAndIncrement(), total) + "%)");

            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Status.FAIL;
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

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
        parameters.add(new BooleanP(EXPORT_LOOP_OBJECTS, this, true));
        parameters.add(new OutputSkeletonObjectsP(OUTPUT_LOOP_OBJECTS, this));
        parameters.add(new ParamSeparatorP(ANALYSIS_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_BRANCH_LENGTH, this, 0d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR, this));
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
            returnedParameters.add(parameters.getParameter(EXPORT_LOOP_OBJECTS));
            if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_LOOP_OBJECTS));
            }
        }

        returnedParameters.add(parameters.getParameter(ANALYSIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_BRANCH_LENGTH));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
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

        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.edgeLengthPx);
        ref.setObjectsName(edgeObjectsName);
        returnedRefs.add(ref);
        ref = objectMeasurementRefs.getOrPut(Units.replace(Measurements.edgeLengthCal));
        ref.setObjectsName(edgeObjectsName);
        returnedRefs.add(ref);

        // ref = objectMeasurementRefs.getOrPut(Measurements.maxBranchLengthPx);
        // ref.setObjectsName(inputObjectsName);
        // returnedRefs.add(ref);

        // ref = objectMeasurementRefs.getOrPut(Measurements.maxBranchLengthCal);
        // ref.setObjectsName(inputObjectsName);
        // returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRefs = new ParentChildRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String skeletonObjectsName = parameters.getValue(OUTPUT_SKELETON_OBJECTS);
        String edgeObjectsName = parameters.getValue(OUTPUT_EDGE_OBJECTS);
        String junctionObjectsName = parameters.getValue(OUTPUT_JUNCTION_OBJECTS);
        String loopObjectsName = parameters.getValue(OUTPUT_LOOP_OBJECTS);

        returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, skeletonObjectsName));
        returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, edgeObjectsName));
        returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, junctionObjectsName));
        if ((boolean) parameters.getValue(EXPORT_LOOP_OBJECTS)) {
            returnedRefs.add(parentChildRefs.getOrPut(skeletonObjectsName, loopObjectsName));
        }

        return returnedRefs;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        PartnerRefCollection returnedRefs = new PartnerRefCollection();

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
        return "Uses the AnalyzeSkeleton plugin by Ignacio Arganda-Carreras (https://imagej.net/AnalyzeSkeleton)."
                + "<br><br>The optional, output skeleton object acts solely as a linking object for the edge, junction and loop objects.  It doesn't itself hold any coordinate data.";
    }

    @Override
    public boolean verify() {
        return true;
    }
}
