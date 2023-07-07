package io.github.mianalysis.mia.module.images.transform.registration;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.tracking.jaqaman.JaqamanLinker;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.DefaultCostMatrixCreator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.module.objects.relate.Linkable;
import io.github.mianalysis.mia.module.objects.relate.RelateOneToOne;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AffineCentroids extends AbstractAffineRegistration {
    public static final String FEATURE_SEPARATOR = "Feature detection";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MAXIMUM_SEPARATION = "Maximum separation (px)";
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
        centroidParam.centroidObjects = (Objs) workspace.getObjects(parameters.getValue(INPUT_OBJECTS,workspace));
        centroidParam.maxSeparation = (float) (double) parameters.getValue(MAXIMUM_SEPARATION,workspace);
        centroidParam.maxEpsilon = (float) (double) parameters.getValue(MAX_EPSILON,workspace);
        centroidParam.minInlierRatio = (float) (double) parameters.getValue(MIN_INLIER_RATIO,workspace);
        centroidParam.referenceMode = (String) parameters.getValue(REFERENCE_MODE,workspace);
        centroidParam.numPrevFrames = (int) parameters.getValue(NUM_PREV_FRAMES,workspace);

    }

    @Override
    protected Object[] fitModel(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param) {
        CentroidParam p = (CentroidParam) param;

        String referenceMode = p.referenceMode;
        int numPrevFrames = p.numPrevFrames; 

        Objs candidates1 = new Objs("Candidates1", p.centroidObjects);
        Objs candidates2 = new Objs("Candidates2", p.centroidObjects);

        for (Obj obj : p.centroidObjects.values()) {
            if ((referenceMode.equals(ReferenceModes.FIRST_FRAME) && obj.getT() == 0)
                    || (referenceMode.equals(ReferenceModes.PREVIOUS_N_FRAMES)
                            && ((p.t - obj.getT()) <= numPrevFrames && (p.t - obj.getT()) > 0))) {
                Obj candidateObj = candidates1.createAndAddNewObject(obj.getVolumeType());
                candidateObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
            } else if (obj.getT() == p.t) {
                Obj candidateObj = candidates2.createAndAddNewObject(obj.getVolumeType());
                candidateObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
            }
        }

        ArrayList<Linkable> linkables = RelateOneToOne.getCentroidSeparationLinkables(candidates1, candidates2, false,
                p.maxSeparation);
        DefaultCostMatrixCreator<Integer, Integer> creator = RelateOneToOne.getCostMatrixCreator(linkables);
        JaqamanLinker<Integer, Integer> linker = new JaqamanLinker<>(creator);
        if (!linker.checkInput() || !linker.process())
            return null;
        Map<Integer, Integer> assignment = linker.getResult();

        Vector<PointMatch> candidates = new Vector<>();
        for (int ID1 : assignment.keySet()) {
            int ID2 = assignment.get(ID1);
            Point point1 = new Point(
                    new double[] { candidates1.get(ID1).getXMean(true), candidates1.get(ID1).getYMean(true) });
            Point point2 = new Point(
                    new double[] { candidates2.get(ID2).getXMean(true), candidates2.get(ID2).getYMean(true) });
            candidates.add(new PointMatch(point2, point1));
        }

        ArrayList<PointMatch> inliers = new ArrayList<PointMatch>();
        AbstractAffineModel2D model = getModel(p.transformationMode);

        try {
            model.filterRansac(candidates, inliers, 1000, p.maxEpsilon, p.minInlierRatio);
        } catch (NotEnoughDataPointsException e) {
            if (candidates.size() == 1) {
                MIA.log.writeWarning("Single pair detected, enforcing translation model");
                double dx = candidates.get(0).getP2().getL()[0] - candidates.get(0).getP1().getL()[0];
                double dy = candidates.get(0).getP2().getL()[1] - candidates.get(0).getP1().getL()[1];
                model = new AffineModel2D();
                ((AffineModel2D) model).set(1d, 0d, 0d, 1d, dx, dy);
            } else {
                return null;
            }
        }

        return new Object[] { model, candidates };

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new DoubleP(MAXIMUM_SEPARATION, this, 100));
        parameters.add(new DoubleP(MAX_EPSILON, this, 25.0));
        parameters.add(new DoubleP(MIN_INLIER_RATIO, this, 0.05));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
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

        parameters.get(MAXIMUM_SEPARATION).setDescription(
                "Maximum spatial separation between object centroids for them to be linked and used in the alignment.");

        parameters.get(MAX_EPSILON).setDescription(
                "\"Matching local descriptors gives many false positives, but true positives are consistent with respect to a common transformation while false positives are not. This consistent set and the underlying transformation are identified using RANSAC. This value is the maximal allowed transfer error of a match to be counted as a good one. Tip: Set this to about 10% of the image size.\".  "
                        + siteRef);

        parameters.get(MIN_INLIER_RATIO).setDescription(
                "\"The ratio of the number of true matches to the number of all matches including both true and false used by RANSAC. 0.05 means that minimally 5% of all matches are expected to be good while 0.9 requires that 90% of the matches were good. Only transformations with this minimal ratio of true consent matches are accepted. Tip: Do not go below 0.05 (and only if 5% is more than about 7 matches) except with a very small maximal alignment error to avoid wrong solutions.\".  "
                        + siteRef);

    }

    public class CentroidParam extends AffineParam {
        // Fitting parameters
        float maxSeparation = 100f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;

        Objs centroidObjects = null;

        String referenceMode = null;
        int numPrevFrames = 0;

    }
}
