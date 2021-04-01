//TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.

// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
// image, then use the local maxima as starting points for the cluster centroids.

package wbif.sjx.MIA.Module.ObjectProcessing.Relationships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.LocationWrapper;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputClusterObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.CoordinateSet;
import wbif.sjx.common.Object.Volume.VolumeType;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class SingleClassCluster extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
    public static final String APPLY_VOLUME = "Apply volume";
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
    public static final String K_CLUSTERS = "Number of clusters";
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";
    public static final String MIN_POINTS = "Minimum number of points per cluster";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";


    public SingleClassCluster(ModuleCollection modules) {
        super("Single class cluster",modules);
    }

    public interface ClusteringAlgorithms {
        String KMEANSPLUSPLUS = "KMeans++";
        String DBSCAN = "DBSCAN";

        String[] ALL = new String[]{KMEANSPLUSPLUS, DBSCAN};

    }

    public ObjCollection runKMeansPlusPlus(ObjCollection outputObjects, List<LocationWrapper> locations, int width, int height, int nSlices, double dppXY, double dppZ, String calibratedUnits) {
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        int kClusters = parameters.getValue(K_CLUSTERS);
        int maxIterations = parameters.getValue(MAX_ITERATIONS);

        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<>(kClusters,maxIterations);
        List<CentroidCluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (CentroidCluster<LocationWrapper> cluster : clusters) {
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
            
            for (LocationWrapper point:cluster.getPoints()) {
                Obj pointObject = point.getObject();
                outputObject.setT(pointObject.getT());
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }
        }

        // Add single object cluster object for unclustered objects
        for (LocationWrapper wrapper:locations) {
            Obj obj = wrapper.getObject();
            if (obj.getParent(outputObjectsName) == null) {
                Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
                outputObject.setT(obj.getT());

                obj.addParent(outputObject);
                outputObject.addChild(obj);

            }
        }

        return outputObjects;

    }

    public ObjCollection runDBSCAN(ObjCollection outputObjects, List<LocationWrapper> locations, int width, int height, int nSlices, double dppXY, double dppZ, String calibratedUnits) {
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        double eps = parameters.getValue(EPS);
        int minPoints = parameters.getValue(MIN_POINTS);

        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps, minPoints);
        List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (Cluster<LocationWrapper> cluster:clusters) {
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);

            for (LocationWrapper point:cluster.getPoints()) {
                Obj pointObject = point.getObject();
                outputObject.setT(pointObject.getT());
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }
        }

        // Add single object cluster object for unclustered objects
        for (LocationWrapper wrapper:locations) {
            Obj obj = wrapper.getObject();
            if (obj.getParent(outputObjectsName) == null) {
                Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
                outputObject.setT(obj.getT());

                obj.addParent(outputObject);
                outputObject.addChild(obj);

            }
        }

        return outputObjects;

    }

    public void applyClusterVolume(Obj outputObject, ObjCollection childObjects, double eps) throws IntegerOverflowException {
        ObjCollection children = outputObject.getChildren(childObjects.getName());

        CoordinateSet coordinateSet = outputObject.getCoordinateSet();

        // Initial pass, adding all coordinates to cluster object
        ObjCollection tempObjects = new ObjCollection("Cluster", childObjects);
        for (Obj child:children.values()) {
            // Getting local region around children (local region with radius equal to epsilon)
            Obj region = GetLocalObjectRegion.getLocalRegion(child, tempObjects, eps, false, false);

            // Adding coordinates from region to the cluster object
            coordinateSet.addAll(region.getCoordinateSet());
            outputObject.setT(0);
        }

        // Reducing the size of the cluster area by eps
        Image objectImage = outputObject.getAsImage("Object",false);
        InvertIntensity.process(objectImage);
        objectImage = DistanceMap.process(objectImage, "Distance", DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true, false);
        ImagePlus objectIpl = objectImage.getImagePlus();

        // Iterating over each coordinate in the object, removing it if its distance to the edge is less than eps
        Iterator<Point<Integer>> iterator = outputObject.getCoordinateSet().iterator();
        double conv = outputObject.getDppZ()/outputObject.getDppXY();
        while (iterator.hasNext()) {
            Point<Integer> point = iterator.next();

            // Checking value
            objectIpl.setPosition(1,point.getZ()+1,outputObject.getT()+1);
            ImageProcessor ipr = objectIpl.getProcessor();
            double value = ipr.getPixelValue(point.getX(), point.getY());

            if (value < (eps-Math.ceil(conv))) iterator.remove();

        }
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_RELATIONSHIPS;
    }

    @Override
    public String getDescription() {
        return "Clusters objects using K-Means and/or DB-SCAN algorithms." +
                "\nUses Apache Commons Math library for clustering.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting output objects name
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,inputObjects);

        // Getting parameters
        boolean applyVolume = parameters.getValue(APPLY_VOLUME);
        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM);
        double eps = parameters.getValue(EPS);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);

        // If there are no input objects skipping this module
        Obj firstObject = inputObjects.getFirst();
        if (firstObject == null) {
            workspace.addObjects(outputObjects);
            return Status.PASS;
        }

        // Getting object parameters
        int width = firstObject.getWidth();
        int height = firstObject.getHeight();
        int nSlices = firstObject.getNSlices();
        double dppXY = firstObject.getDppXY();
        double dppZ = firstObject.getDppZ();
        String calibratedUnits = firstObject.getUnits();

        int[] temporalLimits = inputObjects.getTemporalLimits();
        if (linkInSameFrame) {
            int count = 1;
            int total = temporalLimits[1]-temporalLimits[0]+1;
            for (int f=temporalLimits[0];f<=temporalLimits[1];f++) {
                writeStatus("Processing frame "+(count++)+" of "+total);
                // Getting locations in current frame
                List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
                for (Obj inputObject:inputObjects.values()) {
                    if (inputObject.getT() == f) {
                        locations.add(new LocationWrapper(inputObject));
                    }
                }

                // Running clustering system
                switch (clusteringAlgorithm) {
                    case ClusteringAlgorithms.KMEANSPLUSPLUS:
                        runKMeansPlusPlus(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
                        break;
                    case ClusteringAlgorithms.DBSCAN:
                        runDBSCAN(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
                        break;
                }
            }
        } else {
            // Adding points to collection
            writeStatus("Adding points to clustering algorithm");
            List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
            for (Obj inputObject:inputObjects.values()) {
                locations.add(new LocationWrapper(inputObject));
            }

            // Running clustering system
            writeStatus("Running clustering algorithm");
            switch (clusteringAlgorithm) {
                case ClusteringAlgorithms.KMEANSPLUSPLUS:
                    runKMeansPlusPlus(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
                    break;

                case ClusteringAlgorithms.DBSCAN:
                    runDBSCAN(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
                    break;
            }
        }

        // Adding measurement to each cluster and adding coordinates to clusters
        if (applyVolume) {
            for (Obj outputObject : outputObjects.values()) {
                try {
                    applyClusterVolume(outputObject, inputObjects, eps);
                } catch (IntegerOverflowException e) {
                    return Status.FAIL;
                }
            }
        }

        writeStatus("Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        // Showing clustered objects colour coded by parent
        if (showOutput) {
            // Generating colours
            HashMap<Integer,Float> hues = ColourFactory.getParentIDHues(inputObjects,outputObjectsName,true);
            ImagePlus dispIpl = inputObjects.convertToImage(outputObjectsName,hues,8,true).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputClusterObjectsP(CLUSTER_OBJECTS, this));
        parameters.add(new BooleanP(APPLY_VOLUME, this, false));
        parameters.add(new ChoiceP(CLUSTERING_ALGORITHM, this,ClusteringAlgorithms.DBSCAN,ClusteringAlgorithms.ALL));
        parameters.add(new IntegerP(K_CLUSTERS, this,100));
        parameters.add(new IntegerP(MAX_ITERATIONS, this,10000));
        parameters.add(new DoubleP(EPS, this,10.0));
        parameters.add(new IntegerP(MIN_POINTS, this,5));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME,this,true));

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

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        String clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        returnedRelationships.add(parentChildRefs.getOrPut(clusterObjectsName,inputObjectsName));

        return returnedRelationships;

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

////TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.
//
//// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
//// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
//// image, then use the local maxima as starting points for the cluster centroids.
//
//package wbif.sjx.MIA.Module.ObjectProcessing.Relationships;
//
//import ij.ImagePlus;
//import ij.process.ImageProcessor;
//import org.apache.commons.math3.ml.clustering.CentroidCluster;
//import org.apache.commons.math3.ml.clustering.Cluster;
//import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
//import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
//import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
//import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
//import wbif.sjx.MIA.Module.Module;
//import wbif.sjx.MIA.Module.ModuleCollection;
//import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
//import wbif.sjx.MIA.Module.PackageNames;
//import wbif.sjx.MIA.Module.Category;
//import wbif.sjx.MIA.Object.*;
//import wbif.sjx.MIA.Object.Parameters.*;
//import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
//import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
//import wbif.sjx.MIA.Object.References.MetadataRefCollection;
//import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
//import wbif.sjx.MIA.Process.ColourFactory;
//import wbif.sjx.common.Exceptions.IntegerOverflowException;
//import wbif.sjx.common.Object.LUTs;
//import wbif.sjx.common.Object.Point;
//import wbif.sjx.common.Object.Volume.CoordinateSet;
//import wbif.sjx.common.Object.Volume.VolumeType;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * Created by sc13967 on 21/06/2017.
// */
//public class SingleClassCluster extends Module {
//    public static final String INPUT_OBJECTS = "Input objects";
//    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
//    public static final String APPLY_VOLUME = "Apply volume";
//    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
//    public static final String K_CLUSTERS = "Number of clusters";
//    public static final String MAX_ITERATIONS = "Maximum number of iterations";
//    public static final String EPS = "Neighbourhood for clustering (epsilon)";
//    public static final String MIN_POINTS = "Minimum number of points per cluster";
//
//    public static final String ADD_COST = "Add cost";
//    public static final String COST_MEASUREMENT = "Cost measurement";
//    public static final String COST_CALCULATION = "Cost calculation";
//    public static final String ENFORCE_COST_LIMIT = "Enforce cost limit";
//    public static final String COST_LIMIT = "Cost limit";
//
//    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";
//
//
//    public SingleClassCluster(ModuleCollection modules) {
//        super("Single class cluster",modules);
//    }
//
//    public interface ClusteringAlgorithms {
//        String DBSCAN = "DBSCAN";
//        String KMEANSPLUSPLUS = "KMeans++";
//        String MUNKRES = "Munkres";
//
//        String[] ALL = new String[]{DBSCAN,KMEANSPLUSPLUS,MUNKRES};
//
//    }
//
//    public interface CostCalculations {
//        String DIFFERENCE = "Difference";
//        String SUM = "Sum";
//
//        String[] ALL = new String[]{DIFFERENCE,SUM};
//
//    }
//
//    public ObjCollection runKMeansPlusPlus(ObjCollection outputObjects, List<LocationWrapper> locations, int width, int height, int nSlices, double dppXY, double dppZ, String calibratedUnits) {
//        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
//        int kClusters = parameters.getValue(K_CLUSTERS);
//        int maxIterations = parameters.getValue(MAX_ITERATIONS);
//
//        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<>(kClusters,maxIterations);
//        List<CentroidCluster<LocationWrapper>> clusters = clusterer.cluster(locations);
//
//        // Assigning relationships between points and clusters
//        for (CentroidCluster<LocationWrapper> cluster:clusters) {
//            Obj outputObject = new Obj(VolumeType.POINTLIST,outputObjectsName,outputObjects.getAndIncrementID(),width,height,nSlices,dppXY,dppZ,calibratedUnits);
//
//            for (LocationWrapper point:cluster.getPoints()) {
//                Obj pointObject = point.getObject();
//                outputObject.setT(pointObject.getT());
//                pointObject.addParent(outputObject);
//                outputObject.addChild(pointObject);
//            }
//
//            outputObjects.add(outputObject);
//
//        }
//
//        // Add single object cluster object for unclustered objects
//        for (LocationWrapper wrapper:locations) {
//            Obj obj = wrapper.getObject();
//            if (obj.getParent(outputObjectsName) == null) {
//                int ID = outputObjects.getAndIncrementID();
//                VolumeType type = VolumeType.POINTLIST;
//                Obj outputObject = new Obj(type,outputObjectsName,ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//                outputObject.setT(obj.getT());
//
//                obj.addParent(outputObject);
//                outputObject.addChild(obj);
//
//                outputObjects.add(outputObject);
//
//            }
//        }
//
//        return outputObjects;
//
//    }
//
//    public ObjCollection runDBSCAN(ObjCollection outputObjects, List<LocationWrapper> locations, int width, int height, int nSlices, double dppXY, double dppZ, String calibratedUnits) {
//        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
//        double eps = parameters.getValue(EPS);
//        int minPoints = parameters.getValue(MIN_POINTS);
//
//        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps, minPoints);
//        List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);
//
//        // Assigning relationships between points and clusters
//        for (Cluster<LocationWrapper> cluster:clusters) {
//            int ID = outputObjects.getAndIncrementID();
//            VolumeType type = VolumeType.POINTLIST;
//            Obj outputObject = new Obj(type,outputObjectsName,ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//
//            for (LocationWrapper point:cluster.getPoints()) {
//                Obj pointObject = point.getObject();
//                outputObject.setT(pointObject.getT());
//                pointObject.addParent(outputObject);
//                outputObject.addChild(pointObject);
//            }
//
//            outputObjects.add(outputObject);
//
//        }
//
//        // Add single object cluster object for unclustered objects
//        for (LocationWrapper wrapper:locations) {
//            Obj obj = wrapper.getObject();
//            if (obj.getParent(outputObjectsName) == null) {
//                int ID = outputObjects.getAndIncrementID();
//                VolumeType type = VolumeType.POINTLIST;
//                Obj outputObject = new Obj(type,outputObjectsName,ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//                outputObject.setT(obj.getT());
//
//                obj.addParent(outputObject);
//                outputObject.addChild(obj);
//
//                outputObjects.add(outputObject);
//
//            }
//        }
//
//        return outputObjects;
//
//    }
//
//    public void applyClusterVolume(Obj outputObject, ObjCollection childObjects, double eps) throws IntegerOverflowException {
//        ObjCollection children = outputObject.getChildren(childObjects.getName());
//
//        CoordinateSet coordinateSet = outputObject.getCoordinateSet();
//
//        // Initial pass, adding all coordinates to cluster object
//        for (Obj child:children.values()) {
//            // Getting local region around children (local region with radius equal to epsilon)
//            Obj region = GetLocalObjectRegion.getLocalRegion(child,"Cluster",eps,false,false);
//
//            // Adding coordinates from region to the cluster object
//            coordinateSet.addAll(region.getCoordinateSet());
//            outputObject.setT(0);
//        }
//
//        // Reducing the size of the cluster area by eps
//        ImagePlus objectIpl = outputObject.convertObjToImage("Object").getImagePlus();
//        InvertIntensity.process(objectIpl);
//        objectIpl = DistanceMap.getDistanceMap(objectIpl,true);
//
//        // Iterating over each coordinate in the object, removing it if its distance to the edge is less than eps
//        Iterator<Point<Integer>> iterator = outputObject.getPoints().iterator();
//        double conv = outputObject.getDppZ()/outputObject.getDppXY();
//        while (iterator.hasNext()) {
//            Point<Integer> point = iterator.next();
//
//            // Checking value
//            objectIpl.setPosition(1,point.getZ()+1,outputObject.getT()+1);
//            ImageProcessor ipr = objectIpl.getProcessor();
//            double value = ipr.getPixelValue(point.getX(),point.getY());
//
//            if (value < (eps-Math.ceil(conv))) iterator.remove();
//
//        }
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.OBJECT_PROCESSING_RELATIONSHIPS;
//    }
//
//    @Override
//    public String getDescription() {
//        return "Clusters objects using K-Means and/or DB-SCAN algorithms." +
//                "\nUses Apache Commons Math library for clustering.";
//
//    }
//
//    @Override
//    public Status process(Workspace workspace) {
//        // Getting objects to measure
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
//
//        // Getting output objects name
//        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);
//        ObjCollection outputObjects = new ObjCollection(outputObjectsName);
//
//        // Getting parameters
//        boolean applyVolume = parameters.getValue(APPLY_VOLUME);
//        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM);
//        int kClusters = parameters.getValue(K_CLUSTERS);
//        int maxIterations = parameters.getValue(MAX_ITERATIONS);
//        double eps = parameters.getValue(EPS);
//        int minPoints = parameters.getValue(MIN_POINTS);
//        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);
//
//        // If there are no input objects skipping this module
//        Obj firstObject = inputObjects.getFirst();
//        if (firstObject == null) {
//            workspace.addObjects(outputObjects);
//            return true;
//        }
//
//        // Getting object parameters
//        int width = firstObject.getWidth();
//        int height = firstObject.getHeight();
//        int nSlices = firstObject.getNSlices();
//        double dppXY = firstObject.getDppXY();
//        double dppZ = firstObject.getDppZ();
//        String calibratedUnits = firstObject.getUnits();
//        boolean twoD = firstObject.is2D();
//
//        int[] temporalLimits = inputObjects.getTemporalLimits();
//        if (linkInSameFrame) {
//            int count = 1;
//            int total = temporalLimits[1]-temporalLimits[0]+1;
//            for (int f=temporalLimits[0];f<=temporalLimits[1];f++) {
//                writeMessage("Processing frame "+(count++)+" of "+total);
//                // Getting locations in current frame
//                List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
//                for (Obj inputObject:inputObjects.values()) {
//                    if (inputObject.getT() == f) {
//                        locations.add(new LocationWrapper(inputObject));
//                    }
//                }
//
//                // Running clustering system
//                switch (clusteringAlgorithm) {
//                    case ClusteringAlgorithms.KMEANSPLUSPLUS:
//                        runKMeansPlusPlus(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//                        break;
//                    case ClusteringAlgorithms.DBSCAN:
//                        runDBSCAN(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//                        break;
//                }
//            }
//        } else {
//            // Adding points to collection
//            writeMessage("Adding points to clustering algorithm");
//            List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
//            for (Obj inputObject:inputObjects.values()) {
//                locations.add(new LocationWrapper(inputObject));
//            }
//
//            // Running clustering system
//            writeMessage("Running clustering algorithm");
//            switch (clusteringAlgorithm) {
//                case ClusteringAlgorithms.KMEANSPLUSPLUS:
//                    runKMeansPlusPlus(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//                    break;
//
//                case ClusteringAlgorithms.DBSCAN:
//                    runDBSCAN(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//                    break;
//            }
//        }
//
//        // Adding measurement to each cluster and adding coordinates to clusters
//        if (applyVolume) {
//            for (Obj outputObject : outputObjects.values()) {
//                try {
//                    applyClusterVolume(outputObject, inputObjects, eps);
//                } catch (IntegerOverflowException e) {
//                    return false;
//                }
//            }
//        }
//
//        writeMessage("Adding objects ("+outputObjectsName+") to workspace");
//        workspace.addObjects(outputObjects);
//
//        // Showing clustered objects colour coded by parent
//        if (showOutput) {
//            // Generating colours
//            HashMap<Integer,Float> hues = ColourFactory.getParentIDHues(inputObjects,outputObjectsName,true);
//            ImagePlus dispIpl = inputObjects.convertToImage(outputObjectsName,null,hues,8,true).getImagePlus();
//            dispIpl.setLut(LUTs.Random(true));
//            dispIpl.setPosition(1,1,1);
//            dispIpl.updateChannelAndDraw();
//            dispIpl.show();
//        }
//
//        return true;
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
//        parameters.add(new OutputClusterObjectsP(CLUSTER_OBJECTS, this));
//        parameters.add(new BooleanP(APPLY_VOLUME, this, false));
//        parameters.add(new ChoiceP(CLUSTERING_ALGORITHM, this,ClusteringAlgorithms.DBSCAN,ClusteringAlgorithms.ALL));
//        parameters.add(new IntegerP(K_CLUSTERS, this,100));
//        parameters.add(new IntegerP(MAX_ITERATIONS, this,10000));
//        parameters.add(new DoubleP(EPS, this,10.0));
//        parameters.add(new IntegerP(MIN_POINTS, this,5));
//
//        ParameterCollection collection = new ParameterCollection();
//        collection.add(new ObjectMeasurementP(COST_MEASUREMENT,this));
//        collection.add(new ChoiceP(COST_CALCULATION,this,CostCalculations.DIFFERENCE,CostCalculations.ALL));
//        collection.add(new BooleanP(ENFORCE_COST_LIMIT,this,false));
//        collection.add(new DoubleP(COST_LIMIT,this,1));
//        parameters.add(new ParameterGroup(ADD_COST,this,collection,1));
//
//        parameters.add(new BooleanP(LINK_IN_SAME_FRAME,this,true));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
//        returnedParameters.add(parameters.getParameter(CLUSTER_OBJECTS));
//        returnedParameters.add(parameters.getParameter(APPLY_VOLUME));
//
//        returnedParameters.add(parameters.getParameter(CLUSTERING_ALGORITHM));
//        switch ((String) parameters.getValue(CLUSTERING_ALGORITHM)) {
//            case ClusteringAlgorithms.DBSCAN:
//                returnedParameters.add(parameters.getParameter(EPS));
//                returnedParameters.add(parameters.getParameter(MIN_POINTS));
//                break;
//            case ClusteringAlgorithms.KMEANSPLUSPLUS:
//                returnedParameters.add(parameters.getParameter(K_CLUSTERS));
//                returnedParameters.add(parameters.getParameter(MAX_ITERATIONS));
//                break;
//            case ClusteringAlgorithms.MUNKRES:
//                returnedParameters.add(parameters.getParameter(ADD_COST));
//                break;
//        }
//
//        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        return null;
//    }
//
//    @Override
//    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
//        return null;
//    }
//
//    @Override
//    public MetadataRefCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public ParentChildRefCollection updateAndGetParentChildRefs() {
//        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();
//
//        String clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS);
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//
//        returnedRelationships.add(parentChildRefs.getOrPut(clusterObjectsName,inputObjectsName));
//
//        return returnedRelationships;
//
//    }
//
//    @Override
//    public boolean verify() {
//        return true;
//    }
//}