package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
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
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public class SingleClassCluster extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CLUSTER_OBJECTS = "Cluster (parent) objects";
    public static final String APPLY_VOLUME = "Apply volume";
    public static final String CLUSTER_SEPARATOR = "Cluster controls";
    public static final String CLUSTERING_ALGORITHM = "Clustering algorithm";
    public static final String K_CLUSTERS = "Number of clusters";
    public static final String MAX_ITERATIONS = "Maximum number of iterations";
    public static final String EPS = "Neighbourhood for clustering (epsilon)";
    public static final String MIN_POINTS = "Minimum number of points per cluster";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public SingleClassCluster(Modules modules) {
        super("Single class cluster", modules);
    }

    public interface ClusteringAlgorithms {
        String KMEANSPLUSPLUS = "KMeans++";
        String DBSCAN = "DBSCAN";

        String[] ALL = new String[] { KMEANSPLUSPLUS, DBSCAN };

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";

    }

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
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

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
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

}