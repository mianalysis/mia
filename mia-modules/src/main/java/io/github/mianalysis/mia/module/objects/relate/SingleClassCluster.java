//TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.

// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
// image, then use the local maxima as starting points for the cluster centroids.

package io.github.mianalysis.mia.module.objects.relate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputClusterObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 21/06/2017.
 */

/**
 * Clusters objects (based on centroid locations) using K-Means++ and/or DBSCAN
 * algorithms. In K-Means++ [1], an optimisation of the standard K-Means
 * algorithm, the points are assigned to a pre-determined number of clusters
 * such that each point is assigned to its closest cluster mean position (this
 * process is repeated until the cluster assignments stabilise or a maximum
 * number of iterations is reached). For DBSCAN [2], points are clustered based
 * on a minimum number of neighbours within a specified spatial range. As such,
 * this algorithm doesn't require prior knowledge of the number of clusters.
 * Both algorithms use their respective
 * <a href="https://commons.apache.org/proper/commons-math/">Apache Commons Math
 * implementations.</a><br>
 * <br>
 * References:<br>
 * [1] Arthur, D.; Vassilvitskii, S. (2007). "k-means++: the advantages of
 * careful seeding." <i>Proceedings of the eighteenth annual ACM-SIAM symposium
 * on Discrete algorithms. Society for Industrial and Applied Mathematics
 * Philadelphia, PA, USA.</i> pp. 1027–1035<br>
 * [2] Ester, M.; Kriegel, H.-P.; Sander, J.; Xu, X. (1996). "A density-based
 * algorithm for discovering clusters in large spatial databases with noise."
 * <i>Proceedings of the Second International Conference on Knowledge Discovery
 * and Data Mining (KDD-96). AAAI Press.</i> pp. 226–231
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SingleClassCluster extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input/output";

    /**
     * Objects from the workspace to be grouped into clusters. Clusters are
     * determined based on the centroid postions of the input objects. Input objects
     * will be children of their assigned clusters. Each input object will be
     * assigned to a single cluster.
     */
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";

    /**
     * When selected, the output cluster object will be assigned volume based on the
     * extents of all child objects.
     */
    public static final String APPLY_VOLUME = "Apply volume";

    /**
    * 
    */
    public static final String CLUSTER_SEPARATOR = "Cluster controls";

    /**
     * The clustering algorithm to use:<br>
     * <ul>
     * <li>"DBSCAN" Points are clustered based on a minimum number of neighbours
     * ("Minimum number of points per cluster") within a specified distance
     * ("Neighbourhood for clustering (epsilon)"). All proximal points which satisfy
     * these criteria are added to a common cluster. This uses the <a href=
     * "https://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/stat/clustering/DBSCANClusterer.html">Apache
     * Commons Math3</a> implementation of DBSCAN, which describes the algorithm as:
     * "A point p is density connected to another point q, if there exists a chain
     * of points pi, with i = 1 .. n and p1 = p and pn = q, such that each pair (pi,
     * pi+1) is directly density-reachable. A point q is directly density-reachable
     * from point p if it is in the ε-neighborhood of this point.".</li>
     * <li>"KMeans++" Points are assigned into a pre-determined number of clusters
     * (defined by "Number of clusters"), with each point assigned to the cluster
     * with the closest centroid. Since the cluster centroids will vary with each
     * added point, this process is optimised iteratively. The algorithm continues
     * until either no points switch clusters or the maximum number of allowed
     * iterations ("Maximum number of iterations") is reached.</li>
     * </ul>
     */
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";

    /**
     * If "Clustering algorithm" is set to "KMeans++", this is the number of
     * clusters the points will be assigned to.
     */
    public static final String K_CLUSTERS = "Number of clusters";

    /**
     * If "Clustering algorithm" is set to "KMeans++", this is the maximum number of
     * optimisation iterations that will be performed. If cluster assignment has
     * stabilised prior to reaching this number of iterations the algorithm will
     * terminate early.
     */
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";

    /**
     * If "Clustering algorithm" is set to "DBSCAN", this is the minimum number of
     * neighbour points which must be within a specified distance ("Neighbourhood
     * for clustering (epsilon)") of a point for that point to be included in the
     * cluster.
     */
    public static final String MIN_POINTS = "Minimum number of points per cluster";

    /**
     * When selected, objects must be in the same time frame for them to be assigned
     * to a common cluster.
     */
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public SingleClassCluster(Modules modules) {
        super("Single class cluster", modules);
    }

    public interface ClusteringAlgorithms {
        String KMEANSPLUSPLUS = "KMeans++";
        String DBSCAN = "DBSCAN";

        String[] ALL = new String[] { KMEANSPLUSPLUS, DBSCAN };

    }

    public static Objs runKMeansPlusPlus(Objs outputObjects, List<LocationWrapper> locations, int kClusters,
            int maxIterations) {
        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<>(kClusters, maxIterations);
        List<CentroidCluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (CentroidCluster<LocationWrapper> cluster : clusters) {
            Obj outputObject = outputObjects.createAndAddNewObject(new PointListFactory());

            for (LocationWrapper point : cluster.getPoints()) {
                Obj pointObject = point.getObject();
                outputObject.setT(pointObject.getT());
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }
        }

        // Add single object cluster object for unclustered objects
        for (LocationWrapper wrapper : locations) {
            Obj obj = wrapper.getObject();
            if (obj.getParent(outputObjects.getName()) == null) {
                Obj outputObject = outputObjects.createAndAddNewObject(new PointListFactory());
                outputObject.setT(obj.getT());

                obj.addParent(outputObject);
                outputObject.addChild(obj);

            }
        }

        return outputObjects;

    }

    public static Objs runDBSCAN(Objs outputObjects, List<LocationWrapper> locations, double eps, int minPoints) {
        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps, minPoints);
        List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (Cluster<LocationWrapper> cluster : clusters) {
            Obj outputObject = outputObjects.createAndAddNewObject(new PointListFactory());

            for (LocationWrapper point : cluster.getPoints()) {
                Obj pointObject = point.getObject();
                outputObject.setT(pointObject.getT());
                pointObject.addParent(outputObject);
                outputObject.addChild(pointObject);
            }
        }

        // Add single object cluster object for unclustered objects
        for (LocationWrapper wrapper : locations) {
            Obj obj = wrapper.getObject();
            if (obj.getParent(outputObjects.getName()) == null) {
                Obj outputObject = outputObjects.createAndAddNewObject(new PointListFactory());
                outputObject.setT(obj.getT());

                obj.addParent(outputObject);
                outputObject.addChild(obj);

            }
        }

        return outputObjects;

    }

    public void applyClusterVolume(Obj outputObject, Objs childObjects, double eps) throws IntegerOverflowException {
        Objs children = outputObject.getChildren(childObjects.getName());
        CoordinateSetI coordinateSet = outputObject.getCoordinateSet();

        for (Obj child : children.values())
            coordinateSet.addAll(child.getCoordinateSet().duplicate());

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Clusters objects (based on centroid locations) using K-Means++ and/or DBSCAN algorithms.  In K-Means++ [1], an optimisation of the standard K-Means algorithm, the points are assigned to a pre-determined number of clusters such that each point is assigned to its closest cluster mean position (this process is repeated until the cluster assignments stabilise or a maximum number of iterations is reached).  For DBSCAN [2], points are clustered based on a minimum number of neighbours within a specified spatial range.  As such, this algorithm doesn't require prior knowledge of the number of clusters.  Both algorithms use their respective <a href=\"https://commons.apache.org/proper/commons-math/\">Apache Commons Math implementations.</a><br><br>"

                + "References:<br>"
                + "[1] Arthur, D.; Vassilvitskii, S. (2007). \"k-means++: the advantages of careful seeding.\" <i>Proceedings of the eighteenth annual ACM-SIAM symposium on Discrete algorithms. Society for Industrial and Applied Mathematics Philadelphia, PA, USA.</i> pp. 1027–1035<br>"
                + "[2] Ester, M.; Kriegel, H.-P.; Sander, J.; Xu, X. (1996). \"A density-based algorithm for discovering clusters in large spatial databases with noise.\" <i>Proceedings of the Second International Conference on Knowledge Discovery and Data Mining (KDD-96). AAAI Press.</i> pp. 226–231";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting output objects name
        String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS, workspace);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // Getting parameters
        boolean applyVolume = parameters.getValue(APPLY_VOLUME, workspace);
        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM, workspace);
        int kClusters = parameters.getValue(K_CLUSTERS, workspace);
        int maxIterations = parameters.getValue(MAX_ITERATIONS, workspace);
        double eps = parameters.getValue(EPS, workspace);
        int minPoints = parameters.getValue(MIN_POINTS, workspace);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);

        // If there are no input objects skipping this module
        Obj firstObject = inputObjects.getFirst();
        if (firstObject == null) {
            workspace.addObjects(outputObjects);
            return Status.PASS;
        }

        // Clearing previous instances of these relationships
        inputObjects.removeParents(outputObjectsName);

        int[] temporalLimits = inputObjects.getTemporalLimits();
        if (linkInSameFrame) {
            int count = 1;
            int total = temporalLimits[1] - temporalLimits[0] + 1;
            for (int f = temporalLimits[0]; f <= temporalLimits[1]; f++) {
                // Getting locations in current frame
                List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
                for (Obj inputObject : inputObjects.values()) {
                    if (inputObject.getT() == f) {
                        locations.add(new LocationWrapper(inputObject));
                    }
                }

                // Running clustering system
                switch (clusteringAlgorithm) {
                    case ClusteringAlgorithms.KMEANSPLUSPLUS:
                        runKMeansPlusPlus(outputObjects, locations, kClusters, maxIterations);
                        break;
                    case ClusteringAlgorithms.DBSCAN:
                        runDBSCAN(outputObjects, locations, eps, minPoints);
                        break;
                }

                writeProgressStatus(count++, total, "frames");

            }
        } else {
            // Adding points to collection
            List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
            for (Obj inputObject : inputObjects.values())
                locations.add(new LocationWrapper(inputObject));

            // Running clustering system
            switch (clusteringAlgorithm) {
                case ClusteringAlgorithms.KMEANSPLUSPLUS:
                    runKMeansPlusPlus(outputObjects, locations, kClusters, maxIterations);
                    break;

                case ClusteringAlgorithms.DBSCAN:
                    runDBSCAN(outputObjects, locations, eps, minPoints);
                    break;
            }
        }

        // Adding measurement to each cluster and adding coordinates to clusters
        int count = 0;
        int total = outputObjects.size();
        if (applyVolume) {
            for (Obj outputObject : outputObjects.values()) {
                try {
                    applyClusterVolume(outputObject, inputObjects, eps);
                } catch (IntegerOverflowException e) {
                    return Status.FAIL;
                }
                writeProgressStatus(++count, total, "clusters");
            }
        }

        workspace.addObjects(outputObjects);

        // Showing clustered objects colour coded by parent
        if (showOutput) {
            // Generating colours
            HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(inputObjects, outputObjectsName, true);
            ImagePlus dispIpl = inputObjects.convertToImage(outputObjectsName, hues, 8, true).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1, 1, 1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputClusterObjectsP(CLUSTER_OBJECTS, this));
        parameters.add(new BooleanP(APPLY_VOLUME, this, false));

        parameters.add(new SeparatorP(CLUSTER_SEPARATOR, this));
        parameters.add(new ChoiceP(CLUSTERING_ALGORITHM, this, ClusteringAlgorithms.DBSCAN, ClusteringAlgorithms.ALL));
        parameters.add(new IntegerP(K_CLUSTERS, this, 100));
        parameters.add(new IntegerP(MAX_ITERATIONS, this, 10000));
        parameters.add(new DoubleP(EPS, this, 10.0));
        parameters.add(new IntegerP(MIN_POINTS, this, 5));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CLUSTER_OBJECTS));
        returnedParameters.add(parameters.getParameter(APPLY_VOLUME));

        returnedParameters.add(parameters.getParameter(CLUSTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CLUSTERING_ALGORITHM));
        if (parameters.getValue(CLUSTERING_ALGORITHM, workspace).equals(ClusteringAlgorithms.KMEANSPLUSPLUS)) {
            // Running KMeans++ clustering
            returnedParameters.add(parameters.getParameter(K_CLUSTERS));
            returnedParameters.add(parameters.getParameter(MAX_ITERATIONS));

        } else if (parameters.getValue(CLUSTERING_ALGORITHM, workspace).equals(ClusteringAlgorithms.DBSCAN)) {
            // Running DBSCAN clustering
            returnedParameters.add(parameters.getParameter(EPS));
            returnedParameters.add(parameters.getParameter(MIN_POINTS));

        }

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
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
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        returnedRelationships.add(parentChildRefs.getOrPut(clusterObjectsName, inputObjectsName));

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from the workspace to be grouped into clusters.  Clusters are determined based on the centroid postions of the input objects.  Input objects will be children of their assigned clusters.  Each input object will be assigned to a single cluster.");

        parameters.get(CLUSTER_OBJECTS).setDescription(
                "Output cluster objects to be added to the workspace.  Each cluster will be a parent of the associated input objects.");

        parameters.get(APPLY_VOLUME).setDescription(
                "When selected, the output cluster object will gain all coordinates of any child objects.");

        parameters.get(CLUSTERING_ALGORITHM).setDescription("The clustering algorithm to use:<br><ul>"

                + "<li>\"" + ClusteringAlgorithms.DBSCAN
                + "\" Points are clustered based on a minimum number of neighbours (\"" + MIN_POINTS
                + "\") within a specified distance (\"" + EPS
                + "\").  All proximal points which satisfy these criteria are added to a common cluster.  This uses the <a href=\"https://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/stat/clustering/DBSCANClusterer.html\">Apache Commons Math3</a> implementation of DBSCAN, which describes the algorithm as: \"A point p is density connected to another point q, if there exists a chain of points pi, with i = 1 .. n and p1 = p and pn = q, such that each pair <pi, pi+1> is directly density-reachable. A point q is directly density-reachable from point p if it is in the ε-neighborhood of this point.\".</li>"

                + "<li>\"" + ClusteringAlgorithms.KMEANSPLUSPLUS
                + "\" Points are assigned into a pre-determined number of clusters (defined by \"" + K_CLUSTERS
                + "\"), with each point assigned to the cluster with the closest centroid.  Since the cluster centroids will vary with each added point, this process is optimised iteratively.  The algorithm continues until either no points switch clusters or the maximum number of allowed iterations (\""
                + MAX_ITERATIONS + "\") is reached.</li></ul>");

        parameters.get(K_CLUSTERS)
                .setDescription("If \"" + CLUSTERING_ALGORITHM + "\" is set to \"" + ClusteringAlgorithms.KMEANSPLUSPLUS
                        + "\", this is the number of clusters the points will be assigned to.");

        parameters.get(MAX_ITERATIONS).setDescription("If \"" + CLUSTERING_ALGORITHM + "\" is set to \""
                + ClusteringAlgorithms.KMEANSPLUSPLUS
                + "\", this is the maximum number of optimisation iterations that will be performed.  If cluster assignment has stabilised prior to reaching this number of iterations the algorithm will terminate early.");

        parameters.get(EPS).setDescription("If \"" + CLUSTERING_ALGORITHM + "\" is set to \""
                + ClusteringAlgorithms.DBSCAN
                + "\", this is the minimum distance to neighbour points that must be satisfied for a point to be added to a cluster.  This distance is specified in pixel units.");

        parameters.get(MIN_POINTS).setDescription("If \"" + CLUSTERING_ALGORITHM + "\" is set to \""
                + ClusteringAlgorithms.DBSCAN
                + "\", this is the minimum number of neighbour points which must be within a specified distance (\""
                + EPS + "\") of a point for that point to be included in the cluster.");

        parameters.get(LINK_IN_SAME_FRAME).setDescription(
                "When selected, objects must be in the same time frame for them to be assigned to a common cluster.");

    }

    class LocationWrapper implements Clusterable {
        private Obj object;
        private double[] location;

        public LocationWrapper(Obj object) {
            this.object = object;

            // Getting the centroid of the current object
            int x = (int) Math.round(object.getXMean(true));
            int y = (int) Math.round(object.getYMean(true));
            int z = (int) Math.round(object.getZMean(true, true));

            this.location = new double[] { x, y, z };

        }

        @Override
        public double[] getPoint() {
            return location;

        }

        public Obj getObject() {
            return object;

        }
    }
}

//// TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density
//// map maxima for initial centroid guesses.
//
//// Calculates clusters using the DBSCAN algorithm. This has the advantage over
//// K-means clustering that it doesn't
//// require the number of clusters to be known in advance. Ricci, et al. (Cell,
//// 2015) calculate a density map for the
//// image, then use the local maxima as starting points for the cluster
//// centroids.
//
// package io.github.mianalysis.MIA.Module.ObjectProcessing.Relationships;
//
// import ij.ImagePlus;
// import ij.process.ImageProcessor;
// import org.apache.commons.math3.ml.clustering.CentroidCluster;
// import org.apache.commons.math3.ml.clustering.Cluster;
// import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
// import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
// import
//// io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
// import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
// import io.github.mianalysis.MIA.Module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.module.Module;
// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;
// import
//// io.github.mianalysis.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
// import io.github.mianalysis.MIA.Module.PackageNames;
// import io.github.mianalysis.MIA.Module.Category;
// import io.github.mianalysis.MIA.Object.*;
// import io.github.mianalysis.MIA.Object.Parameters.*;
// import io.github.mianalysis.MIA.Object.References.ImageMeasurementRefs;
// import io.github.mianalysis.MIA.Object.References.ObjMeasurementRefs;
// import io.github.mianalysis.MIA.Object.References.MetadataRefs;
// import io.github.mianalysis.MIA.Object.References.ParentChildRefs;
// import io.github.mianalysis.MIA.Process.ColourFactory;
// import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
// import io.github.sjcross.sjcommon.object.LUTs;
// import io.github.mianalysis.mia.object.coordinates.Point;
// import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
// import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.List;
//
/// **
// * Created by sc13967 on 21/06/2017.
// */
// public class SingleClassCluster extends Module {
// public static final String INPUT_OBJECTS = "Input objects";
// public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
// public static final String APPLY_VOLUME = "Apply volume";
// public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
// public static final String K_CLUSTERS = "Number of clusters";
// public static final String MAX_ITERATIONS = "Maximum number of iterations";
// public static final String EPS = "Neighbourhood for clustering (epsilon)";
// public static final String MIN_POINTS = "Minimum number of points per
//// cluster";
//
// public static final String ADD_COST = "Add cost";
// public static final String COST_MEASUREMENT = "Cost measurement";
// public static final String COST_CALCULATION = "Cost calculation";
// public static final String ENFORCE_COST_LIMIT = "Enforce cost limit";
// public static final String COST_LIMIT = "Cost limit";
//
// public static final String LINK_IN_SAME_FRAME = "Only link objects in same
//// frame";
//
//
// public SingleClassCluster(Modules modules) {
// super("Single class cluster",modules);
// }
//
// public interface ClusteringAlgorithms {
// String DBSCAN = "DBSCAN";
// String KMEANSPLUSPLUS = "KMeans++";
// String MUNKRES = "Munkres";
//
// String[] ALL = new String[]{DBSCAN,KMEANSPLUSPLUS,MUNKRES};
//
// }
//
// public interface CostCalculations {
// String DIFFERENCE = "Difference";
// String SUM = "Sum";
//
// String[] ALL = new String[]{DIFFERENCE,SUM};
//
// }
//
// public Objs runKMeansPlusPlus(Objs outputObjects, List<LocationWrapper>
//// locations, int width, int height, int nSlices, double dppXY, double dppZ,
//// String calibratedUnits) {
// String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS,workspace);
// int kClusters = parameters.getValue(K_CLUSTERS,workspace);
// int maxIterations = parameters.getValue(MAX_ITERATIONS,workspace);
//
// KMeansPlusPlusClusterer<LocationWrapper> clusterer = new
//// KMeansPlusPlusClusterer<>(kClusters,maxIterations);
// List<CentroidCluster<LocationWrapper>> clusters =
//// clusterer.cluster(locations);
//
// // Assigning relationships between points and clusters
// for (CentroidCluster<LocationWrapper> cluster:clusters) {
// Obj outputObject = new
//// Obj(new PointListFactory(),outputObjectsName,outputObjects.getAndIncrementID(),width,height,nSlices,dppXY,dppZ,calibratedUnits);
//
// for (LocationWrapper point:cluster.getPoints()) {
// Obj pointObject = point.getObject();
// outputObject.setT(pointObject.getT());
// pointObject.addParent(outputObject);
// outputObject.addChild(pointObject);
// }
//
// outputObjects.add(outputObject);
//
// }
//
// // Add single object cluster object for unclustered objects
// for (LocationWrapper wrapper:locations) {
// Obj obj = wrapper.getObject();
// if (obj.getParent(outputObjectsName) == null) {
// int ID = outputObjects.getAndIncrementID();
// VolumeType type = new PointListFactory();
// Obj outputObject = new
//// Obj(type,outputObjectsName,ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
// outputObject.setT(obj.getT());
//
// obj.addParent(outputObject);
// outputObject.addChild(obj);
//
// outputObjects.add(outputObject);
//
// }
// }
//
// return outputObjects;
//
// }
//
// public Objs runDBSCAN(Objs outputObjects, List<LocationWrapper> locations,
//// int width, int height, int nSlices, double dppXY, double dppZ, String
//// calibratedUnits) {
// String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS,workspace);
// double eps = parameters.getValue(EPS,workspace);
// int minPoints = parameters.getValue(MIN_POINTS,workspace);
//
// DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<>(eps,
//// minPoints);
// List<Cluster<LocationWrapper>> clusters = clusterer.cluster(locations);
//
// // Assigning relationships between points and clusters
// for (Cluster<LocationWrapper> cluster:clusters) {
// int ID = outputObjects.getAndIncrementID();
// VolumeType type = new PointListFactory();
// Obj outputObject = new
//// Obj(type,outputObjectsName,ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
//
// for (LocationWrapper point:cluster.getPoints()) {
// Obj pointObject = point.getObject();
// outputObject.setT(pointObject.getT());
// pointObject.addParent(outputObject);
// outputObject.addChild(pointObject);
// }
//
// outputObjects.add(outputObject);
//
// }
//
// // Add single object cluster object for unclustered objects
// for (LocationWrapper wrapper:locations) {
// Obj obj = wrapper.getObject();
// if (obj.getParent(outputObjectsName) == null) {
// int ID = outputObjects.getAndIncrementID();
// VolumeType type = new PointListFactory();
// Obj outputObject = new
//// Obj(type,outputObjectsName,ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
// outputObject.setT(obj.getT());
//
// obj.addParent(outputObject);
// outputObject.addChild(obj);
//
// outputObjects.add(outputObject);
//
// }
// }
//
// return outputObjects;
//
// }
//
// public void applyClusterVolume(Obj outputObject, Objs childObjects, double
//// eps) throws IntegerOverflowException {
// Objs children = outputObject.getChildren(childObjects.getName());
//
// CoordinateSet coordinateSet = outputObject.getCoordinateSet();
//
// // Initial pass, adding all coordinates to cluster object
// for (Obj child:children.values()) {
// // Getting local region around children (local region with radius equal to
//// epsilon)
// Obj region =
//// GetLocalObjectRegion.getLocalRegion(child,"Cluster",eps,false,false);
//
// // Adding coordinates from region to the cluster object
// coordinateSet.addAll(region.getCoordinateSet());
// outputObject.setT(0);
// }
//
// // Reducing the size of the cluster area by eps
// ImagePlus objectIpl =
//// outputObject.convertObjToImage("Object").getImagePlus();
// InvertIntensity.process(objectIpl);
// objectIpl = DistanceMap.getDistanceMap(objectIpl,true);
//
// // Iterating over each coordinate in the object, removing it if its distance
//// to the edge is less than eps
// Iterator<Point<Integer>> iterator = outputObject.getPoints().iterator();
// double conv = outputObject.getDppZ()/outputObject.getDppXY();
// while (iterator.hasNext()) {
// Point<Integer> point = iterator.next();
//
// // Checking value
// objectIpl.setPosition(1,point.getZ()+1,outputObject.getT()+1);
// ImageProcessor ipr = objectIpl.getProcessor();
// double value = ipr.getPixelValue(point.getX(),point.getY());
//
// if (value < (eps-Math.ceil(conv))) iterator.remove();
//
// }
// }
//
// @Override
// public String getPackageName() {
// return PackageNames.OBJECT_PROCESSING_RELATIONSHIPS;
// }
//
// @Override
// public String getDescription() {
// return "Clusters objects using K-Means and/or DB-SCAN algorithms." +
// "\nUses Apache Commons Math library for clustering.";
//
// }
//
// @Override
// public Status process(WorkspaceI workspace) {
// // Getting objects to measure
// String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
// Objs inputObjects = workspace.getObjects(inputObjectsName);
//
// // Getting output objects name
// String outputObjectsName = parameters.getValue(CLUSTER_OBJECTS,workspace);
// Objs outputObjects = new Objs(outputObjectsName);
//
// // Getting parameters
// boolean applyVolume = parameters.getValue(APPLY_VOLUME,workspace);
// String clusteringAlgorithm =
//// parameters.getValue(CLUSTERING_ALGORITHM,workspace);
// int kClusters = parameters.getValue(K_CLUSTERS,workspace);
// int maxIterations = parameters.getValue(MAX_ITERATIONS,workspace);
// double eps = parameters.getValue(EPS,workspace);
// int minPoints = parameters.getValue(MIN_POINTS,workspace);
// boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME,workspace);
//
// // If there are no input objects skipping this module
// Obj firstObject = inputObjects.getFirst();
// if (firstObject == null) {
// workspace.addObjects(outputObjects);
// return true;
// }
//
// // Getting object parameters
// int width = firstObject.getWidth();
// int height = firstObject.getHeight();
// int nSlices = firstObject.getNSlices();
// double dppXY = firstObject.getDppXY();
// double dppZ = firstObject.getDppZ();
// String calibratedUnits = firstObject.getUnits();
// boolean twoD = firstObject.is2D();
//
// int[] temporalLimits = inputObjects.getTemporalLimits();
// if (linkInSameFrame) {
// int count = 1;
// int total = temporalLimits[1]-temporalLimits[0]+1;
// for (int f=temporalLimits[0];f<=temporalLimits[1];f++) {
// writeMessage("Processing frame "+(count++)+" of "+total);
// // Getting locations in current frame
// List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
// for (Obj inputObject:inputObjects.values()) {
// if (inputObject.getT() == f) {
// locations.add(new LocationWrapper(inputObject));
// }
// }
//
// // Running clustering system
// switch (clusteringAlgorithm) {
// case ClusteringAlgorithms.KMEANSPLUSPLUS:
// runKMeansPlusPlus(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
// break;
// case ClusteringAlgorithms.DBSCAN:
// runDBSCAN(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
// break;
// }
// }
// } else {
// // Adding points to collection
// writeMessage("Adding points to clustering algorithm");
// List<LocationWrapper> locations = new ArrayList<>(inputObjects.size());
// for (Obj inputObject:inputObjects.values()) {
// locations.add(new LocationWrapper(inputObject));
// }
//
// // Running clustering system
// writeMessage("Running clustering algorithm");
// switch (clusteringAlgorithm) {
// case ClusteringAlgorithms.KMEANSPLUSPLUS:
// runKMeansPlusPlus(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
// break;
//
// case ClusteringAlgorithms.DBSCAN:
// runDBSCAN(outputObjects,locations,width,height,nSlices,dppXY,dppZ,calibratedUnits);
// break;
// }
// }
//
// // Adding measurement to each cluster and adding coordinates to clusters
// if (applyVolume) {
// for (Obj outputObject : outputObjects.values()) {
// try {
// applyClusterVolume(outputObject, inputObjects, eps);
// } catch (IntegerOverflowException e) {
// return false;
// }
// }
// }
//
// writeMessage("Adding objects ("+outputObjectsName+") to workspace");
// workspace.addObjects(outputObjects);
//
// // Showing clustered objects colour coded by parent
// if (showOutput) {
// // Generating colours
// HashMap<Integer,Float> hues =
//// ColourFactory.getParentIDHues(inputObjects,outputObjectsName,true);
// ImagePlus dispIpl =
//// inputObjects.convertToImage(outputObjectsName,null,hues,8,true).getImagePlus();
// dispIpl.setLut(LUTs.Random(true));
// dispIpl.setPosition(1,1,1);
// dispIpl.updateChannelAndDraw();
// dispIpl.show();
// }
//
// return true;
//
// }
//
// @Override
// public void initialiseParameters() {
// parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
// parameters.add(new OutputClusterObjectsP(CLUSTER_OBJECTS, this));
// parameters.add(new BooleanP(APPLY_VOLUME, this, false));
// parameters.add(new ChoiceP(CLUSTERING_ALGORITHM,
//// this,ClusteringAlgorithms.DBSCAN,ClusteringAlgorithms.ALL));
// parameters.add(new IntegerP(K_CLUSTERS, this,100));
// parameters.add(new IntegerP(MAX_ITERATIONS, this,10000));
// parameters.add(new DoubleP(EPS, this,10.0));
// parameters.add(new IntegerP(MIN_POINTS, this,5));
//
// Parameters collection = new Parameters();
// collection.add(new ObjectMeasurementP(COST_MEASUREMENT,this));
// collection.add(new
//// ChoiceP(COST_CALCULATION,this,CostCalculations.DIFFERENCE,CostCalculations.ALL));
// collection.add(new BooleanP(ENFORCE_COST_LIMIT,this,false));
// collection.add(new DoubleP(COST_LIMIT,this,1));
// parameters.add(new ParameterGroup(ADD_COST,this,collection,1));
//
// parameters.add(new BooleanP(LINK_IN_SAME_FRAME,this,true));
//
// }
//
// @Override
// public Parameters updateAndGetParameters() {
// Parameters returnedParameters = new Parameters();
// returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
// returnedParameters.add(parameters.getParameter(CLUSTER_OBJECTS));
// returnedParameters.add(parameters.getParameter(APPLY_VOLUME));
//
// returnedParameters.add(parameters.getParameter(CLUSTERING_ALGORITHM));
// switch ((String) parameters.getValue(CLUSTERING_ALGORITHM,workspace)) {
// case ClusteringAlgorithms.DBSCAN:
// returnedParameters.add(parameters.getParameter(EPS));
// returnedParameters.add(parameters.getParameter(MIN_POINTS));
// break;
// case ClusteringAlgorithms.KMEANSPLUSPLUS:
// returnedParameters.add(parameters.getParameter(K_CLUSTERS));
// returnedParameters.add(parameters.getParameter(MAX_ITERATIONS));
// break;
// case ClusteringAlgorithms.MUNKRES:
// returnedParameters.add(parameters.getParameter(ADD_COST));
// break;
// }
//
// returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));
//
// return returnedParameters;
//
// }
//
// @Override
// public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
// return null;
// }
//
// @Override
// public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
// return null;
// }
//
// @Override
// public MetadataRefs updateAndGetMetadataReferences() {
// return null;
// }
//
// @Override
// public ParentChildRefs updateAndGetParentChildRefs() {
// ParentChildRefs returnedRelationships = new ParentChildRefs();
//
// String clusterObjectsName = parameters.getValue(CLUSTER_OBJECTS,workspace);
// String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
//
// returnedRelationships.add(parentChildRefs.getOrPut(clusterObjectsName,inputObjectsName));
//
// return returnedRelationships;
//
// }
//
// @Override
// public boolean verify() {
// return true;
// }
// }
