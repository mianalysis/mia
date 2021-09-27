package io.github.mianalysis.mia.module.imageprocessing.stack.registration;

import java.util.ArrayList;
import java.util.Vector;

import ij.process.ImageProcessor;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AffineCentroids extends AbstractAffineRegistration {
    public static final String FEATURE_SEPARATOR = "Feature detection";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String ROD = "Closest/next closest ratio";
    public static final String MAX_EPSILON = "Maximal alignment error (px)";
    public static final String MIN_INLIER_RATIO = "Inlier ratio";

    public AffineCentroids(Modules modules) {
        super("Affine (object centroids)", modules);
    }

    @Override
    public String getDescription() {
        return "Apply slice-by-slice (2D) affine-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration can be performed along either the time or Z axes.  The non-registered axis (e.g. time axis when registering in Z) can be \"linked\" (all frames given the same registration) or \"independent\" (each stack registered separately)."

                + "<br><br>This module uses centroids of previously-detected objects as the reference points for image alignment";
    }

    @Override
    public CentroidParam createParameterSet() {
        return new CentroidParam();
    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        super.getParameters(param, workspace);

        // Setting up the parameters
        CentroidParam centroidParam = (CentroidParam) param;
        centroidParam.centroidObjects = (Objs) workspace.getObjectSet(parameters.getValue(INPUT_OBJECTS));
        centroidParam.rod = (float) (double) parameters.getValue(ROD);
        centroidParam.maxEpsilon = (float) (double) parameters.getValue(MAX_EPSILON);
        centroidParam.minInlierRatio = (float) (double) parameters.getValue(MIN_INLIER_RATIO);

    }

    @Override
    protected Object[] fitModel(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param) {
        CentroidParam p = (CentroidParam) param;

        String referenceMode = parameters.getValue(REFERENCE_MODE);
        int numPrevFrames = parameters.getValue(NUM_PREV_FRAMES);

        // Extracting features
        ArrayList<Feature> featureList1 = new ArrayList<Feature>();
        ArrayList<Feature> featureList2 = new ArrayList<Feature>();

        for (Obj obj : p.centroidObjects.values()) {
            if ((referenceMode.equals(ReferenceModes.FIRST_FRAME) && obj.getT() == 0)
                    || (referenceMode.equals(ReferenceModes.PREVIOUS_N_FRAMES)
                            && ((p.t - obj.getT()) <= numPrevFrames && (p.t - obj.getT()) > 0))) {
                Feature f = new Feature();
                f.location = new double[] { obj.getXMean(true), obj.getYMean(true) };
                f.scale = 1;
                f.descriptor = new float[] { (float) obj.getXMean(true), (float) obj.getYMean(true) };
                featureList1.add(f);
            } else if (obj.getT() == p.t) {
                Feature f = new Feature();
                f.location = new double[] { obj.getXMean(true), obj.getYMean(true) };
                f.scale = 1;
                f.descriptor = new float[] { (float) obj.getXMean(true), (float) obj.getYMean(true) };
                featureList2.add(f);
            }
        }

        // Running registration
        AbstractAffineModel2D model = getModel(p.transformationMode);
        Vector<PointMatch> candidates = FloatArray2DSIFT.createMatches(featureList2, featureList1, p.rod);
        ArrayList<PointMatch> inliers = new ArrayList<PointMatch>();

        try {
            model.filterRansac(candidates, inliers, 1000, p.maxEpsilon, p.minInlierRatio);
        } catch (NotEnoughDataPointsException e) {
            return null;
        }

        return new Object[] { model, candidates };

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new DoubleP(ROD, this, 0.92));
        parameters.add(new DoubleP(MAX_EPSILON, this, 25.0));
        parameters.add(new DoubleP(MIN_INLIER_RATIO, this, 0.05));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(ROD));
        returnedParameters.add(parameters.getParameter(MAX_EPSILON));
        returnedParameters.add(parameters.getParameter(MIN_INLIER_RATIO));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        String siteRef = "Description taken from <a href=\"https://imagej.net/Feature_Extraction\">https://imagej.net/Feature_Extraction</a>";

        parameters.get(INPUT_OBJECTS).setDescription(
                "Centroids for these objects will be used as the references for image alignment.");

        parameters.get(ROD).setDescription(
                "\"Correspondence candidates from local descriptor matching are accepted only if the Euclidean distance to the nearest neighbour is significantly smaller than that to the next nearest neighbour. Lowe (2004) suggests a ratio of r=0.8 which requires some increase when matching things that appear significantly distorted.\".  "
                        + siteRef);

        parameters.get(MAX_EPSILON).setDescription(
                "\"Matching local descriptors gives many false positives, but true positives are consistent with respect to a common transformation while false positives are not. This consistent set and the underlying transformation are identified using RANSAC. This value is the maximal allowed transfer error of a match to be counted as a good one. Tip: Set this to about 10% of the image size.\".  "
                        + siteRef);

        parameters.get(MIN_INLIER_RATIO).setDescription(
                "\"The ratio of the number of true matches to the number of all matches including both true and false used by RANSAC. 0.05 means that minimally 5% of all matches are expected to be good while 0.9 requires that 90% of the matches were good. Only transformations with this minimal ratio of true consent matches are accepted. Tip: Do not go below 0.05 (and only if 5% is more than about 7 matches) except with a very small maximal alignment error to avoid wrong solutions.\".  "
                        + siteRef);

    }

public class CentroidParam extends AffineParam {
        // Fitting parameters
        float rod = 0.92f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;

        Objs centroidObjects = null;

    }
}
