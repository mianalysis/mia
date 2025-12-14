package io.github.mianalysis.mia.module.images.transform.registration;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bunwarpj.Transformation;
import fiji.plugin.trackmate.tracking.jaqaman.JaqamanLinker;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.DefaultCostMatrixCreator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractBUnwarpJRegistration;
import io.github.mianalysis.mia.module.objects.relate.Linkable;
import io.github.mianalysis.mia.module.objects.relate.RelateOneToOne;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.thirdparty.bUnwarpJ_Mod;

/**
 * Apply slice-by-slice (2D) B-spline unwarping-based image registration to a
 * multi-dimensional stack. Images can be aligned relative to the first frame in
 * the stack, the previous frame or a separate image in the workspace. The
 * registration transform can also be calculated from a separate stack to the
 * one that it will be applied to. Registration can be performed along either
 * the time or Z axes. The non-registered axis (e.g. time axis when registering
 * in Z) can be "linked" (all frames given the same registration) or
 * "independent" (each stack registered separately).<br>
 * <br>
 * This module uses the <a href="https://imagej.net/BUnwarpJ">BUnwarpJ</a>
 * plugin to calculate and apply the necessary 2D transforms. Detailed
 * information about how the BUnwarpJ process works can be found at
 * <a href="https://imagej.net/BUnwarpJ">https://imagej.net/BUnwarpJ</a>.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class UnwarpCentroids extends AbstractBUnwarpJRegistration {
    /**
    * 
    */
    public static final String FEATURE_SEPARATOR = "Feature detection";

    /**
     * Centroids for these objects will be used as the references for image
     * alignment.
     */
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MAXIMUM_SEPARATION = "Maximum separation (px)";
    public static final String MAX_EPSILON = "Maximal alignment error (px)";

    /**
     * "The ratio of the number of true matches to the number of all matches
     * including both true and false used by RANSAC. 0.05 means that minimally 5% of
     * all matches are expected to be good while 0.9 requires that 90% of the
     * matches were good. Only transformations with this minimal ratio of true
     * consent matches are accepted. Tip: Do not go below 0.05 (and only if 5% is
     * more than about 7 matches) except with a very small maximal alignment error
     * to avoid wrong solutions.". Description taken from <a href=
     * "https://imagej.net/Feature_Extraction">https://imagej.net/Feature_Extraction</a>
     */
    public static final String MIN_INLIER_RATIO = "Inlier ratio";

    public UnwarpCentroids(Modules modules) {
        super("Unwarp (centroids)", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Apply slice-by-slice (2D) B-spline unwarping-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration can be performed along either the time or Z axes.  The non-registered axis (e.g. time axis when registering in Z) can be \"linked\" (all frames given the same registration) or \"independent\" (each stack registered separately)."

                + "<br><br>This module uses the <a href=\"https://imagej.net/BUnwarpJ\">BUnwarpJ</a> plugin to calculate and apply the necessary 2D transforms.  Detailed information about how the BUnwarpJ process works can be found at <a href=\"https://imagej.net/BUnwarpJ\">https://imagej.net/BUnwarpJ</a>.";
    }

    @Override
    public CentroidBUnwarpJParam createParameterSet() {
        return new CentroidBUnwarpJParam();
    }

    @Override
    public void getParameters(Param param, WorkspaceI workspace) {
        super.getParameters(param, workspace);

        // Setting up the parameters
        CentroidBUnwarpJParam centroidParam = (CentroidBUnwarpJParam) param;
        centroidParam.centroidObjects = (ObjsI) workspace.getObjects(parameters.getValue(INPUT_OBJECTS, workspace));
        centroidParam.maxSeparation = (float) (double) parameters.getValue(MAXIMUM_SEPARATION, workspace);
        centroidParam.maxEpsilon = (float) (double) parameters.getValue(MAX_EPSILON, workspace);
        centroidParam.minInlierRatio = (float) (double) parameters.getValue(MIN_INLIER_RATIO, workspace);
        centroidParam.referenceMode = (String) parameters.getValue(REFERENCE_MODE, workspace);
        centroidParam.numPrevFrames = (int) parameters.getValue(NUM_PREV_FRAMES, workspace);
        centroidParam.registrationAxis = (String) parameters.getValue(REGISTRATION_AXIS, workspace);

    }

    @Override
    public Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {
        CentroidBUnwarpJParam p = (CentroidBUnwarpJParam) param;

        String referenceMode = p.referenceMode;
        int numPrevFrames = p.numPrevFrames;

        ObjsI candidates1 = ObjsFactories.getDefaultFactory().createFromExample("Candidates1", p.centroidObjects);
        ObjsI candidates2 = ObjsFactories.getDefaultFactory().createFromExample("Candidates2", p.centroidObjects);

        for (ObjI obj : p.centroidObjects.values()) {
            switch ((String) p.registrationAxis) {
                case RegistrationAxes.TIME:
                    if ((referenceMode.equals(ReferenceModes.FIRST_FRAME) && obj.getT() == 0)
                            || (referenceMode.equals(ReferenceModes.PREVIOUS_N_FRAMES)
                                    && ((p.t - obj.getT()) <= numPrevFrames && (p.t - obj.getT()) > 0))) {
                        ObjI candidateObj = candidates1.createAndAddNewObject(obj.getCoordinateSetFactory());
                        candidateObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
                    } else if (obj.getT() == p.t) {
                        ObjI candidateObj = candidates2.createAndAddNewObject(obj.getCoordinateSetFactory());
                        candidateObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
                    }
                    break;

                case RegistrationAxes.Z:
                    if ((referenceMode.equals(ReferenceModes.FIRST_FRAME) && obj.getZMean(true, false) == 0)
                            || (referenceMode.equals(ReferenceModes.PREVIOUS_N_FRAMES)
                                    && ((p.t - obj.getZMean(true, false)) <= numPrevFrames
                                            && (p.t - obj.getZMean(true, false)) > 0))) {
                        ObjI candidateObj = candidates1.createAndAddNewObject(obj.getCoordinateSetFactory());
                        candidateObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
                    } else if (obj.getZMean(true, false) == p.t) {
                        ObjI candidateObj = candidates2.createAndAddNewObject(obj.getCoordinateSetFactory());
                        candidateObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
                    }
                    break;
            }
        }

        ArrayList<Linkable> linkables = RelateOneToOne.getCentroidSeparationLinkables(candidates1, candidates2, false,
                p.maxSeparation);
        DefaultCostMatrixCreator<Integer, Integer> creator = RelateOneToOne.getCostMatrixCreator(linkables);
        JaqamanLinker<Integer, Integer> linker = new JaqamanLinker<>(creator);
        if (!linker.checkInput() || !linker.process())
            return null;
        Map<Integer, Integer> assignment = linker.getResult();

        Stack<Point> points1 = new Stack<>();
        Stack<Point> points2 = new Stack<>();
        for (int ID1 : assignment.keySet()) {
            int ID2 = assignment.get(ID1);

            // The following two are correct being this way round
            points1.push(
                    new Point((int) candidates2.get(ID2).getXMean(true), (int) candidates2.get(ID2).getYMean(true)));
            points2.push(
                    new Point((int) candidates1.get(ID1).getXMean(true), (int) candidates1.get(ID1).getYMean(true)));

        }

        ArrayList<Stack<Point>> points = new ArrayList<Stack<Point>>();
        points.add(points1);
        points.add(points2);

        Transformation transformation = bUnwarpJ_Mod.computeTransformationBatch(warpedIpr, referenceIpr, points.get(0),
                points.get(1), p.bParam);

        try {
            File tempFile = File.createTempFile("unwarp", ".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            bufferedWriter.close();

            String tempPath = tempFile.getAbsolutePath();
            transformation.saveDirectTransformation(tempPath);

            BUnwarpJTransform transform = new BUnwarpJTransform();
            transform.transformPath = tempPath;

            return transform;

        } catch (IOException e) {
            MIA.log.writeError(e);
            return null;
        }
    }

    @Override
    public void initialiseParameters() {
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
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
        returnedParameters.add(parameters.getParameter(MAX_EPSILON));
        returnedParameters.add(parameters.getParameter(MIN_INLIER_RATIO));

        // This approach can't show any points
        returnedParameters.remove(SHOW_DETECTED_POINTS);

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

    public class CentroidBUnwarpJParam extends BUnwarpJParam {
        // Fitting parameters
        float maxSeparation = 100f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;
        ObjsI centroidObjects = null;
        String referenceMode = null;
        int numPrevFrames = 0;
        String registrationAxis = null;

    }
}
