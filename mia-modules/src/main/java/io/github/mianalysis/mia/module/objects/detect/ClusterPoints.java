//TODO: Implement Ricci, et al. (Cell, 2015) clustering method using density map maxima for initial centroid guesses.

// Calculates clusters using the DBSCAN algorithm.  This has the advantage over K-means clustering that it doesn't
// require the number of clusters to be known in advance.  Ricci, et al. (Cell, 2015) calculate a density map for the
// image, then use the local maxima as starting points for the cluster centroids.

package io.github.mianalysis.mia.module.objects.detect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.objects.process.GetLocalObjectRegion;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSet;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 21/06/2017.
 */

/**
* Clusters objects (based on centroid locations) using K-Means++ and/or DBSCAN algorithms.  In K-Means++ [1], an optimisation of the standard K-Means algorithm, the points are assigned to a pre-determined number of clusters such that each point is assigned to its closest cluster mean position (this process is repeated until the cluster assignments stabilise or a maximum number of iterations is reached).  For DBSCAN [2], points are clustered based on a minimum number of neighbours within a specified spatial range.  As such, this algorithm doesn't require prior knowledge of the number of clusters.  Both algorithms use their respective <a href="https://commons.apache.org/proper/commons-math/">Apache Commons Math implementations.</a><br><br>References:<br>[1] Arthur, D.; Vassilvitskii, S. (2007). "k-means++: the advantages of careful seeding." <i>Proceedings of the eighteenth annual ACM-SIAM symposium on Discrete algorithms. Society for Industrial and Applied Mathematics Philadelphia, PA, USA.</i> pp. 1027–1035<br>[2] Ester, M.; Kriegel, H.-P.; Sander, J.; Xu, X. (1996). "A density-based algorithm for discovering clusters in large spatial databases with noise." <i>Proceedings of the Second International Conference on Knowledge Discovery and Data Mining (KDD-96). AAAI Press.</i> pp. 226–231
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ClusterPoints extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input, object output";

	/**
	* 
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* 
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";


	/**
	* 
	*/
    public static final String CLUSTER_SEPARATOR = "Cluster controls";

	/**
	* 
	*/
    public static final String BINARY_LOGIC = "Binary logic";

	/**
	* The clustering algorithm to use:<br><ul><li>"DBSCAN" Points are clustered based on a minimum number of neighbours ("Minimum number of points per cluster") within a specified distance ("Neighbourhood for clustering (epsilon)").  All proximal points which satisfy these criteria are added to a common cluster.  This uses the <a href="https://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/stat/clustering/DBSCANClusterer.html">Apache Commons Math3</a> implementation of DBSCAN, which describes the algorithm as: "A point p is density connected to another point q, if there exists a chain of points pi, with i = 1 .. n and p1 = p and pn = q, such that each pair (pi, pi+1) is directly density-reachable. A point q is directly density-reachable from point p if it is in the ε-neighborhood of this point.".</li><li>"KMeans++" Points are assigned into a pre-determined number of clusters (defined by "Number of clusters"), with each point assigned to the cluster with the closest centroid.  Since the cluster centroids will vary with each added point, this process is optimised iteratively.  The algorithm continues until either no points switch clusters or the maximum number of allowed iterations ("Maximum number of iterations") is reached.</li></ul>
	*/
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";

	/**
	* If "Clustering algorithm" is set to "KMeans++", this is the number of clusters the points will be assigned to.
	*/
    public static final String K_CLUSTERS = "Number of clusters";

	/**
	* If "Clustering algorithm" is set to "KMeans++", this is the maximum number of optimisation iterations that will be performed.  If cluster assignment has stabilised prior to reaching this number of iterations the algorithm will terminate early.
	*/
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";

	/**
	* If "Clustering algorithm" is set to "DBSCAN", this is the minimum number of neighbour points which must be within a specified distance ("Neighbourhood for clustering (epsilon)") of a point for that point to be included in the cluster.
	*/
    public static final String MIN_POINTS = "Minimum number of points per cluster";

    public ClusterPoints(Modules modules) {
        super("Cluster points", modules);
    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface ClusteringAlgorithms {
        String KMEANSPLUSPLUS = "KMeans++";
        String DBSCAN = "DBSCAN";

        String[] ALL = new String[] { KMEANSPLUSPLUS, DBSCAN };

    }

    public Objs runKMeansPlusPlus(Objs outputObjects, List<DoublePoint> locations, int kClusters, int maxIterations,
            int t) {
        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(kClusters, maxIterations);
        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (CentroidCluster<DoublePoint> cluster : clusters) {
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);

            for (DoublePoint point : cluster.getPoints()) {
                outputObject.setT(t);
                double[] coord = point.getPoint();
                try {
                    outputObject.add((int) Math.round(coord[0]), (int) Math.round(coord[1]),
                            (int) Math.round(coord[2]));
                } catch (PointOutOfRangeException e) {
                }
            }
        }

        return outputObjects;

    }

    public Objs runDBSCAN(Objs outputObjects, List<DoublePoint> locations, double eps, int minPoints, int t) {
        DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<>(eps, minPoints);
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(locations);

        // Assigning relationships between points and clusters
        for (Cluster<DoublePoint> cluster : clusters) {
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);

            for (DoublePoint point : cluster.getPoints()) {
                outputObject.setT(t);
                double[] coord = point.getPoint();
                try {
                    outputObject.add((int) Math.round(coord[0]), (int) Math.round(coord[1]),
                            (int) Math.round(coord[2]));
                } catch (PointOutOfRangeException e) {
                }
            }
        }

        return outputObjects;

    }

    public void applyClusterVolume(Obj outputObject, Objs childObjects, double eps) throws IntegerOverflowException {
        Objs children = outputObject.getChildren(childObjects.getName());

        CoordinateSet coordinateSet = outputObject.getCoordinateSet();

        // Initial pass, adding all coordinates to cluster object
        Objs tempObjects = new Objs("Cluster", childObjects);
        for (Obj child : children.values()) {
            // Getting local region around children (local region with radius equal to
            // epsilon)
            Point<Double> cent = child.getMeanCentroid(true, false);
            int[] centroid = new int[] { (int) Math.round(cent.getX()), (int) Math.round(cent.getY()),
                    (int) Math.round(cent.getZ()) };
            Obj region = GetLocalObjectRegion.getLocalRegion(child, tempObjects, centroid, (int) Math.round(eps),
                    false);

            // Adding coordinates from region to the cluster object
            coordinateSet.addAll(region.getCoordinateSet());
            outputObject.setT(0);
        }

        // Reducing the size of the cluster area by eps
        Image objectImage = outputObject.getAsTightImage("Object");
        objectImage = DistanceMap.process(objectImage, "Distance", true, DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true,
                false);
        ImagePlus objectIpl = objectImage.getImagePlus();

        // We're using a tight image, so the coordinates are offset
        double[][] extents = outputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        // Iterating over each coordinate in the object, removing it if its distance to
        // the edge is less than eps
        Iterator<Point<Integer>> iterator = outputObject.getCoordinateSet().iterator();
        double conv = outputObject.getDppZ() / outputObject.getDppXY();
        while (iterator.hasNext()) {
            Point<Integer> point = iterator.next();

            // Checking value
            objectIpl.setPosition(1, point.getZ() - zOffs + 1, outputObject.getT() + 1);
            ImageProcessor ipr = objectIpl.getProcessor();
            double value = ipr.getPixelValue(point.getX() - xOffs, point.getY() - yOffs);

            if (value < (eps - Math.ceil(conv)))
                iterator.remove();

        }

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Clusters objects (based on centroid locations) using K-Means++ and/or DBSCAN algorithms.  In K-Means++ [1], an optimisation of the standard K-Means algorithm, the points are assigned to a pre-determined number of clusters such that each point is assigned to its closest cluster mean position (this process is repeated until the cluster assignments stabilise or a maximum number of iterations is reached).  For DBSCAN [2], points are clustered based on a minimum number of neighbours within a specified spatial range.  As such, this algorithm doesn't require prior knowledge of the number of clusters.  Both algorithms use their respective <a href=\"https://commons.apache.org/proper/commons-math/\">Apache Commons Math implementations.</a><br><br>"

                + "References:<br>"
                + "[1] Arthur, D.; Vassilvitskii, S. (2007). \"k-means++: the advantages of careful seeding.\" <i>Proceedings of the eighteenth annual ACM-SIAM symposium on Discrete algorithms. Society for Industrial and Applied Mathematics Philadelphia, PA, USA.</i> pp. 1027–1035<br>"
                + "[2] Ester, M.; Kriegel, H.-P.; Sander, J.; Xu, X. (1996). \"A density-based algorithm for discovering clusters in large spatial databases with noise.\" <i>Proceedings of the Second International Conference on Knowledge Discovery and Data Mining (KDD-96). AAAI Press.</i> pp. 226–231";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting objects to measure
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output objects name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        SpatCal cal = SpatCal.getFromImage(inputImagePlus);
        int nFrames = inputImagePlus.getNFrames();
        double frameInterval = inputImagePlus.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, cal, nFrames, frameInterval, TemporalUnit.getOMEUnit());

        // Getting parameters
        String binaryLogic = parameters.getValue(BINARY_LOGIC,workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        String clusteringAlgorithm = parameters.getValue(CLUSTERING_ALGORITHM,workspace);
        int kClusters = parameters.getValue(K_CLUSTERS,workspace);
        int maxIterations = parameters.getValue(MAX_ITERATIONS,workspace);
        double eps = parameters.getValue(EPS,workspace);
        int minPoints = parameters.getValue(MIN_POINTS,workspace);

        // Getting object parameters
        double dppXY = outputObjects.getDppXY();
        double dppZ = outputObjects.getDppZ();

        // Iterating over each pixel
        for (int t = 0; t < inputImagePlus.getNFrames(); t++) {
            List<DoublePoint> locations = new ArrayList<>();
            for (int z = 0; z < inputImagePlus.getNSlices(); z++) {
                inputImagePlus.setPosition(1, z + 1, t + 1);
                ImageProcessor ipr = inputImagePlus.getProcessor().duplicate();
                if (!blackBackground)
                    ipr.invert();

                for (int x = 0; x < inputImagePlus.getWidth(); x++) {
                    for (int y = 0; y < inputImagePlus.getHeight(); y++) {
                        if (ipr.get(x, y) == 255)
                            locations.add(new DoublePoint(new double[] { x, y, z * dppZ / dppXY }));

                    }
                }
            }

            // Running clustering system
            switch (clusteringAlgorithm) {
            case ClusteringAlgorithms.KMEANSPLUSPLUS:
                runKMeansPlusPlus(outputObjects, locations, kClusters, maxIterations, t);
                break;

            case ClusteringAlgorithms.DBSCAN:
                runDBSCAN(outputObjects, locations, eps, minPoints, t);
                break;
            }
        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CLUSTER_SEPARATOR, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new ChoiceP(CLUSTERING_ALGORITHM, this, ClusteringAlgorithms.DBSCAN, ClusteringAlgorithms.ALL));
        parameters.add(new IntegerP(K_CLUSTERS, this, 100));
        parameters.add(new IntegerP(MAX_ITERATIONS, this, 10000));
        parameters.add(new DoubleP(EPS, this, 10.0));
        parameters.add(new IntegerP(MIN_POINTS, this, 5));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CLUSTER_SEPARATOR));
        returnedParameters.add(parameters.get(BINARY_LOGIC));
        returnedParameters.add(parameters.getParameter(CLUSTERING_ALGORITHM));
        if (parameters.getValue(CLUSTERING_ALGORITHM,workspace).equals(ClusteringAlgorithms.KMEANSPLUSPLUS)) {
            // Running KMeans++ clustering
            returnedParameters.add(parameters.getParameter(K_CLUSTERS));
            returnedParameters.add(parameters.getParameter(MAX_ITERATIONS));

        } else if (parameters.getValue(CLUSTERING_ALGORITHM,workspace).equals(ClusteringAlgorithms.DBSCAN)) {
            // Running DBSCAN clustering
            returnedParameters.add(parameters.getParameter(EPS));
            returnedParameters.add(parameters.getParameter(MIN_POINTS));

        }

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
public MetadataRefs updateAndGetMetadataReferences() {
return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
return null;
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
        // parameters.get(INPUT_OBJECTS).setDescription(
        // "Objects from the workspace to be grouped into clusters. Clusters are
        // determined based on the centroid postions of the input objects. Input objects
        // will be children of their assigned clusters. Each input object will be
        // assigned to a single cluster.");

        // parameters.get(OUTPUT_OBJECTS).setDescription(
        // "Output cluster objects to be added to the workspace.");

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

    }
}
