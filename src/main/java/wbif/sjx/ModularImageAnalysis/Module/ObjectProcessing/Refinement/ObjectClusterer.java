//TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.

// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
// image, then use the local maxima as starting points for the cluster centroids.

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.apache.commons.math3.ml.clustering.*;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class ObjectClusterer extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
    public static final String APPLY_VOLUME = "Apply volume";
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
    public static final String K_CLUSTERS = "Number of clusters";
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";
    public static final String MIN_POINTS = "Minimum number of points per cluster";

    public interface ClusteringAlgorithms {
        String KMEANSPLUSPLUS = "KMeans++";
        String DBSCAN = "DBSCAN";

        String[] ALL = new String[]{KMEANSPLUSPLUS, DBSCAN};

    }

    public ObjCollection runKMeansPlusPlus(ObjCollection outputObjects, List<LocationWrapper> locations, double dppXY, double dppZ, String calibratedUnits, boolean is2D) {
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        int kClusters = parameters.getValue(K_CLUSTERS);
        int maxIterations = parameters.getValue(MAX_ITERATIONS);

        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<>(kClusters,maxIterations);
        List<CentroidCluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (CentroidCluster<LocationWrapper> cluster:clusters) {
            Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibratedUnits,is2D);

            for (LocationWrapper point:cluster.getPoints()) {
                Obj pointObject = point.getObject();
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }

            outputObjects.add(outputObject);

        }

        return outputObjects;

    }

    public ObjCollection runDBSCAN(ObjCollection outputObjects, List<LocationWrapper> locations, double dppXY, double dppZ, String calibratedUnits, boolean is2D) {
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        double eps = parameters.getValue(EPS);
        int minPoints = parameters.getValue(MIN_POINTS);

        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps, minPoints);
        List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (Cluster<LocationWrapper> cluster:clusters) {
            Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibratedUnits,is2D);

            for (LocationWrapper point:cluster.getPoints()) {
                Obj pointObject = point.getObject();
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }

            outputObjects.add(outputObject);

        }

        return outputObjects;

    }

    public void applyClusterVolume(Obj outputObject, ObjCollection childObjects, double eps) {
        ObjCollection children = outputObject.getChildren(childObjects.getName());

        // Initial pass, adding all coordinates to cluster object
        for (Obj child:children.values()) {
            // Getting local region around children (local region with radius equal to epsilon)
            Obj region = GetLocalObjectRegion.getLocalRegion(child,"Cluster",null,eps,false);

            // Adding coordinates from region to the cluster object
            for (Point<Integer> point:region.getPoints()) outputObject.addCoord(point.getX(),point.getY(),point.getZ());
            outputObject.setT(0);
        }

        // Reducing the size of the cluster area by eps
        ImagePlus objectIpl = outputObject.convertObjToImage("Object").getImagePlus();
        InvertIntensity.process(objectIpl);
        objectIpl = BinaryOperations.getDistanceMap3D(objectIpl,true);

        // Iterating over each coordinate in the object, removing it if its distance to the edge is less than eps
        Iterator<Point<Integer>> iterator = outputObject.getPoints().iterator();
        double conv = outputObject.getDistPerPxZ()/outputObject.getDistPerPxXY();
        while (iterator.hasNext()) {
            Point<Integer> point = iterator.next();

            // Checking value
            objectIpl.setPosition(1,point.getZ()+1,outputObject.getT()+1);
            ImageProcessor ipr = objectIpl.getProcessor();
            double value = ipr.getPixelValue(point.getX(),point.getY());

            if (value < (eps-Math.ceil(conv))) iterator.remove();

        }
    }

    @Override
    public String getTitle() {
        return "Object clustering";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return "Clusters objects using K-Means and/or DB-SCAN algorithms." +
                "\nUses Apache Commons Math library for clustering.";

    }

    @Override
    public void run(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting output objects name
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        boolean applyVolume = parameters.getValue(APPLY_VOLUME);
        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM);
        int kClusters = parameters.getValue(K_CLUSTERS);
        int maxIterations = parameters.getValue(MAX_ITERATIONS);
        double eps = parameters.getValue(EPS);
        int minPoints = parameters.getValue(MIN_POINTS);

        // If there are no input objects skipping this module
        Obj firstObject = inputObjects.getFirst();
        if (firstObject == null) {
            workspace.addObjects(outputObjects);
            return;
        }

        // Getting object parameters
        double dppXY = firstObject.getDistPerPxXY();
        double dppZ = firstObject.getDistPerPxZ();
        String calibratedUnits = firstObject.getCalibratedUnits();
        boolean twoD = firstObject.is2D();

        // Adding points to collection
        writeMessage("Adding points to clustering algorithm");
        List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
        for (Obj inputObject:inputObjects.values()) {
            locations.add(new LocationWrapper(inputObject));
        }

        // Running clustering system
        writeMessage("Running clustering algorithm");
        switch (clusteringAlgorithm) {
            case ClusteringAlgorithms.KMEANSPLUSPLUS:
                runKMeansPlusPlus(outputObjects, locations, dppXY, dppZ, calibratedUnits, twoD);
                break;

            case ClusteringAlgorithms.DBSCAN:
                runDBSCAN(outputObjects, locations, dppXY, dppZ, calibratedUnits,twoD);
                break;
        }

        // Adding measurement to each cluster and adding coordinates to clusters
        if (applyVolume) {
            for (Obj outputObject : outputObjects.values()) {
                applyClusterVolume(outputObject, inputObjects, eps);
            }
        }

        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        // Showing outlines of clustered objects
        if (showOutput) {
            // Creating the fake image
            int[][] spatialLimits = inputObjects.getSpatialLimits();
            int[] temporalLimits = inputObjects.getTemporalLimits();
            ImagePlus dispIpl = IJ.createHyperStack(outputObjectsName,spatialLimits[0][1],spatialLimits[1][1],1,spatialLimits[2][1],temporalLimits[1],8);

            // Initialising the overlay module
            AddObjectsOverlay addObjectsOverlay = new AddObjectsOverlay();
            addObjectsOverlay.updateParameterValue(AddObjectsOverlay.POSITION_MODE,AddObjectsOverlay.PositionModes.OUTLINE)
                    .updateParameterValue(AddObjectsOverlay.LINE_WIDTH,0.5)
                    .updateParameterValue(AddObjectsOverlay.LABEL_SIZE,8);

            // Generating colours
            HashMap<Integer,Color> colours = inputObjects.getColours(ObjCollection.ColourModes.PARENT_ID,outputObjectsName,true);

            // Adding overlay and displaying image
            addObjectsOverlay.createOverlay(dispIpl,inputObjects,colours,null);
            dispIpl.show();

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CLUSTER_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(APPLY_VOLUME, Parameter.BOOLEAN, false));
        parameters.add(new Parameter(CLUSTERING_ALGORITHM, Parameter.CHOICE_ARRAY,ClusteringAlgorithms.DBSCAN,ClusteringAlgorithms.ALL));
        parameters.add(new Parameter(K_CLUSTERS, Parameter.INTEGER,100));
        parameters.add(new Parameter(MAX_ITERATIONS, Parameter.INTEGER,10000));
        parameters.add(new Parameter(EPS, Parameter.DOUBLE,10.0));
        parameters.add(new Parameter(MIN_POINTS, Parameter.INTEGER,5));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CLUSTER_OBJECTS));
        returnedParameters.add(parameters.getParameter(APPLY_VOLUME));

        returnedParameters.add(parameters.getParameter(CLUSTERING_ALGORITHM));
        if (parameters.getValue(CLUSTERING_ALGORITHM).equals(ClusteringAlgorithms.KMEANSPLUSPLUS)) {
            // Running KMeans++ clustering
            returnedParameters.add(parameters.getParameter(K_CLUSTERS));
            returnedParameters.add(parameters.getParameter(MAX_ITERATIONS));

        } else if (parameters.getValue(CLUSTERING_ALGORITHM).equals(ClusteringAlgorithms.DBSCAN)) {
            // Running DBSCAN clustering
            returnedParameters.add(parameters.getParameter(EPS));
            returnedParameters.add(parameters.getParameter(MIN_POINTS));

        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        String clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(clusterObjectsName,inputObjectsName);

    }
}