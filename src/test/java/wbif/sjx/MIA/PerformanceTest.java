package wbif.sjx.MIA;

import ij.IJ;
import ij.ImagePlus;
import org.apache.spark.util.SizeEstimator;
import org.junit.Test;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.Logging.LogRenderer;
import wbif.sjx.common.Object.Point;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;

public class PerformanceTest {
    private static final String INPUT_IMAGE = "Input image";
    private static final String OUTPUT_OBJECTS = "Output objects";

    @Test
    public static void main(String[] args) throws UnsupportedEncodingException {
        String path = URLDecoder.decode(PerformanceTest.class.getResource("/performance/Spot1.tif").getPath(),"UTF-8");
        runImage(path);
    }

    public static void runImage(String path) {
        // Testing PointVolume
        System.out.println("PointVolume");
        runTest(path,Obj.ObjectType.POINTLIST);

        // Testing Quadtree
        System.out.println("QuadTreeVolume");
        runTest(path,Obj.ObjectType.QUADTREE);

        // Testing Octree
        System.out.println("OctreeVolulme");
        runTest(path,Obj.ObjectType.OCTREE);

    }

    public static void runTest(String path, Obj.ObjectType type) {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);
        ModuleCollection modules = new ModuleCollection();
        modules.getWorkflowParameters().setObjectType(type);

        // Loading the test image and adding to workspace
        ImagePlus ipl = IJ.openImage(path);
        Image image = new Image(INPUT_IMAGE,ipl);
        workspace.addImage(image);

        // Creating the object
        Obj obj = getObject(workspace,modules);

        // Testing object memory properties
        evaluateObjectMemory(obj);

        // Testing full coordinate access time (via iterator)
        evaluateFullAccessTime(obj);

        // Testing random coordinate access time
//        evaluateRandomAccessTime(obj);

    }

    public static Obj getObject(Workspace workspace, ModuleCollection modules) {
        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(modules);
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,INPUT_IMAGE);
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,OUTPUT_OBJECTS);
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,true);

        // Running IdentifyObjects
        long startTime = System.nanoTime();
        identifyObjects.execute(workspace);
        long endTime = System.nanoTime();

        Obj obj = workspace.getObjectSet(OUTPUT_OBJECTS).getFirst();

        // Reporting creation time
        System.out.println("    Load objects time = "+((endTime-startTime)*1E6)+" ms");

        return obj;

    }

    public static void evaluateObjectMemory(Obj obj) {
        // Reporting Obj memory usage
        System.out.println("    Memory usage = "+(((double)SizeEstimator.estimate(obj)/1048576d))+" MB");

        // Reporting number of Obj elements (points, nodes, etc.)
        System.out.println("    Number of elements = "+obj.getNumberOfElements());

    }

    public static void evaluateFullAccessTime(Obj obj) {
        long startTime = System.nanoTime();
        for (Point<Integer> point:obj);
        long endTime = System.nanoTime();

        // Reporting full volume access time
        System.out.println("    Full access time = "+((endTime-startTime)*1E6)+" ms");

    }

    public static void evaluateRandomAccessTime(Obj obj) {
        // Testing 5% of points
        long nPoints = (long) Math.floor(((double) obj.size())*0.05);

        // Getting list of coordinates to test
        System.out.println("    Testing "+nPoints+" points");
        HashSet<Point<Integer>> testPoints = new HashSet<>();
        while (testPoints.size()<nPoints) {

        }

        long startTime = System.nanoTime();
        long endTime = System.nanoTime();

        System.out.println("    Full access time = "+((endTime-startTime)*1E6)+" ms");

    }
}
