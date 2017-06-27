//TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.

// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
// image, then use the local maxima as starting points for the cluster centroids.

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
import org.apache.commons.math3.ml.clustering.*;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by sc13967 on 21/06/2017.
 */
public class ObjectClusterer extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
    public static final String K_CLUSTERS = "Number of clusters";
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";
    public static final String MIN_POINTS = "Minimum number of points per cluster";

    private static final String KMEANSPLUSPLUS = "KMeans++";
    private static final String DBSCAN = "DBSCAN";
    private static final String[] CLUSTERING_ALGORITHMS = new String[]{KMEANSPLUSPLUS, DBSCAN};

    private static final String N_POINTS_IN_CLUSTER = "N_POINTS_IN_CLUSTER";
    private static final String CLUSTER_AREA_XY = "CLUSTER_AREA_2D";
    private static final String CLUSTER_PERIMETER_XY = "CLUSTER_PERIMETER_2D";


    private static HCObjectSet runKMeansPlusPlus(List<LocationWrapper> locations, HCName outputObjectsName, int kClusters, int maxIterations) {
        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<>(kClusters,maxIterations);
        List<CentroidCluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        HCObjectSet outputObjects = new HCObjectSet(outputObjectsName);
        for (CentroidCluster<LocationWrapper> cluster:clusters) {
            HCObject outputObject = new HCObject(outputObjectsName,outputObjects.getNextID());

            double[] centroid = cluster.getCenter().getPoint();
            outputObject.addCoordinate(HCObject.X,(int) Math.round(centroid[0]));
            outputObject.addCoordinate(HCObject.Y,(int) Math.round(centroid[1]));
            outputObject.addCoordinate(HCObject.Z,(int) Math.round(centroid[2]));

            for (LocationWrapper point:cluster.getPoints()) {
                HCObject pointObject = point.getObject();
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }

            // Copying calibration from the first point in the cluster
            outputObject.copyCalibration(cluster.getPoints().get(0).getObject());

            outputObjects.add(outputObject);

        }

        return outputObjects;

    }

    private static HCObjectSet runDBSCAN(List<LocationWrapper> locations, HCName outputObjectsName, double eps, int minPoints) {
        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps, minPoints);
        List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        HCObjectSet outputObjects = new HCObjectSet(outputObjectsName);
        for (Cluster<LocationWrapper> cluster:clusters) {
            HCObject outputObject = new HCObject(outputObjectsName,outputObjects.getNextID());

            // Calculating the centroid (DBSCAN doesn't give one)
            CumStat[] cs = new CumStat[]{new CumStat(), new CumStat(), new CumStat()};

            for (LocationWrapper point:cluster.getPoints()) {
                HCObject pointObject = point.getObject();
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);

                // Getting the centroid of the current object
                cs[0].addMeasure(pointObject.getCentroid(HCObject.X));
                cs[1].addMeasure(pointObject.getCentroid(HCObject.Y));
                cs[2].addMeasure(pointObject.getCentroid(HCObject.Z));

            }

            outputObject.addCoordinate(HCObject.X,(int) Math.round(cs[0].getMean()));
            outputObject.addCoordinate(HCObject.Y,(int) Math.round(cs[1].getMean()));
            outputObject.addCoordinate(HCObject.Z,(int) Math.round(cs[2].getMean()));

            // Copying calibration from the first point in the cluster
            outputObject.copyCalibration(cluster.getPoints().get(0).getObject());

            outputObjects.add(outputObject);

        }

        return outputObjects;

    }

    @Override
    public String getTitle() {
        return "Object clustering";

    }

    @Override
    public String getHelp() {
        return "Clusters objects using K-Means and/or DB-SCAN algorithms." +
                "\nUses Apache Commons Math library for clustering.";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting objects to measure
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting output objects name
        HCName outputObjectsName = parameters.getValue(CLUSTER_OBJECTS);

        // Getting parameters
        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM);
        int kClusters = parameters.getValue(K_CLUSTERS);
        int maxIterations = parameters.getValue(MAX_ITERATIONS);
        double eps = parameters.getValue(EPS);
        int minPoints = parameters.getValue(MIN_POINTS);

        // Adding points to collection
        if (verbose) System.out.println("["+moduleName+"] Adding points to clustering algorithm");
        List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
        for (HCObject inputObject:inputObjects.values()) {
            locations.add(new LocationWrapper(inputObject));
        }

        // Running clustering system
        if (verbose) System.out.println("["+moduleName+"] Running clustering algorithm");
        HCObjectSet outputObjects = null;

        switch (clusteringAlgorithm) {
            case KMEANSPLUSPLUS:
                outputObjects = runKMeansPlusPlus(locations, outputObjectsName, kClusters, maxIterations);
                break;

            case DBSCAN:
                outputObjects = runDBSCAN(locations, outputObjectsName, eps, minPoints);
                break;

        }

        // Checking each object has a parent.  If not, a new parent is created for that object on its own
        for (HCObject object:inputObjects.values()) {
            if (object.getParent(outputObjectsName) == null) {
                HCObject parentObject = new HCObject(outputObjectsName,outputObjects.getNextID());

                // Getting the centroid of the current object
                ArrayList<Integer> x = new ArrayList<>();
                ArrayList<Integer> y = new ArrayList<>();
                ArrayList<Integer> z = new ArrayList<>();

                x.add((int) Math.round(object.getCentroid(HCObject.X)));
                y.add((int) Math.round(object.getCentroid(HCObject.Y)));
                z.add((int) Math.round(object.getCentroid(HCObject.Z)));

                parentObject.setCoordinates(HCObject.X, x);
                parentObject.setCoordinates(HCObject.Y, y);
                parentObject.setCoordinates(HCObject.Z, z);

                parentObject.copyCalibration(object);

                object.addParent(parentObject);
                parentObject.addChild(object);

                outputObjects.add(parentObject);

            }
        }

        // Adding measurement to each cluster
        for (HCObject outputObject:outputObjects.values()) {
            HCObjectSet children = outputObject.getChildren(inputObjectsName);

            // The number of children per cluster
            HCMeasurement measurement = new HCMeasurement(N_POINTS_IN_CLUSTER,children.size(),this);
            outputObject.addMeasurement(measurement);

            // The area and perimeter of each cluster (convex hull around child centroids)
            try {
                HashSet<Vector2D> points = new HashSet<>(children.size());

                for (HCObject child : children.values()) {
                    double x = child.getCentroid(HCObject.X);
                    double y = child.getCentroid(HCObject.Y);
                    points.add(new Vector2D(x, y));

                }

                ConvexHull2D convexHull2D = new MonotoneChain().generate(points);

                double area = convexHull2D.createRegion().getSize();
                outputObject.addMeasurement(new HCMeasurement(CLUSTER_AREA_XY, area, this));

                double perimeter = convexHull2D.createRegion().getBoundarySize();
                outputObject.addMeasurement(new HCMeasurement(CLUSTER_PERIMETER_XY, perimeter, this));

            } catch (InsufficientDataException e) {
                // This exception occurs when there are fewer than 3 points or in certain point arrangements (i.e. when
                // they all lie in a straight line)
                outputObject.addMeasurement(new HCMeasurement(CLUSTER_AREA_XY, Double.NaN, this));
                outputObject.addMeasurement(new HCMeasurement(CLUSTER_PERIMETER_XY, Double.NaN, this));

            }
        }

        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CLUSTER_OBJECTS,HCParameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CLUSTERING_ALGORITHM,HCParameter.CHOICE_ARRAY,CLUSTERING_ALGORITHMS[0],CLUSTERING_ALGORITHMS));
        parameters.addParameter(new HCParameter(K_CLUSTERS,HCParameter.INTEGER,100));
        parameters.addParameter(new HCParameter(MAX_ITERATIONS,HCParameter.INTEGER,10000));
        parameters.addParameter(new HCParameter(EPS,HCParameter.DOUBLE,10.0));
        parameters.addParameter(new HCParameter(MIN_POINTS,HCParameter.INTEGER,5));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CLUSTER_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CLUSTERING_ALGORITHM));

        if (parameters.getValue(CLUSTERING_ALGORITHM).equals(KMEANSPLUSPLUS)) {
            // Running KMeans++ clustering
            returnedParameters.addParameter(parameters.getParameter(K_CLUSTERS));
            returnedParameters.addParameter(parameters.getParameter(MAX_ITERATIONS));

        } else if (parameters.getValue(CLUSTERING_ALGORITHM).equals(DBSCAN)) {
            // Running DBSCAN clustering
            returnedParameters.addParameter(parameters.getParameter(EPS));
            returnedParameters.addParameter(parameters.getParameter(MIN_POINTS));

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {
        HCName clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        measurements.addMeasurement(clusterObjectsName,N_POINTS_IN_CLUSTER);
        measurements.addMeasurement(clusterObjectsName,CLUSTER_AREA_XY);
        measurements.addMeasurement(clusterObjectsName,CLUSTER_PERIMETER_XY);

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        HCName clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS);
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(clusterObjectsName,inputObjectsName);

    }
}

class LocationWrapper implements Clusterable {
    private HCObject object;
    private double[] location;

    LocationWrapper(HCObject object) {
        this.object = object;

        // Getting the centroid of the current object
        ArrayList<Integer> xArray = object.getCoordinates(HCObject.X);
        ArrayList<Integer> yArray = object.getCoordinates(HCObject.Y);
        ArrayList<Integer> zArray = object.getCoordinates(HCObject.Z);
        int x = (int) MeasureObjectCentroid.calculateCentroid(xArray);
        int y = (int) MeasureObjectCentroid.calculateCentroid(yArray);
        int z = (int) MeasureObjectCentroid.calculateCentroid(zArray);

        this.location = new double[]{x,y,z};

    }

    @Override
    public double[] getPoint() {
        return location;

    }

    public HCObject getObject() {
        return object;

    }
}