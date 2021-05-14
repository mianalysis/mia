package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import mpicbg.ij.util.Util;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract.AbstractAffineRegistration;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Process.Interactable.Interactable;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;

public class ManualRegistration extends AbstractAffineRegistration implements Interactable {
    public static final String FEATURE_SEPARATOR = "Feature detection";
    public static final String POINT_SELECTION_MODE = "Point selection mode";

    public interface PointSelectionModes {
        String PRESELECTED = "Pre-selected points";
        String RUNTIME = "Select at runtime";

        String[] ALL = new String[] { PRESELECTED, RUNTIME };

    }

    public ManualRegistration(ModuleCollection modules) {
        super("Manual registration", modules);
    }

    @Override
    public String getDescription() {
        // return "Align an image from the workspace to another image from the workspace
        // using manually-selected reference points. When the image runs, the input and
        // reference images are displayed. The user then selects matching points on each
        // image and clicks \"Add pair(s)\". Points must be added in the same order on
        // each image (ID numbers next to each point provide a reference). Points are
        // shown in the control window and can be deleted by highlighting the relevant
        // entry and clicking \"Remove pair\". The alignment can be tested by clicking
        // \"Test process\". Finally, the alignment is accepted by clicking \"Finish
        // adding pairs\", at which point the images are closed and the transform is
        // applied. The transformed input image can either overwrite the input image in
        // the workspace, or be saved to the workspace with a new name."

        // + "Alignments are calculated using the <a
        // href=\"https://github.com/axtimwalde/mpicbg\">MPICBG</a> image transformation
        // library."

        // + "<br><br>Note: This module currently only aligns single slice images;
        // however, a future update will add multi-slice processing.";

        return "";

    }

    @Override
    public Param getParameters(Workspace workspace) {
        // Setting up the parameters
        ManualParam param = new ManualParam();
        param.pointSelectionMode = parameters.getValue(POINT_SELECTION_MODE);

        // Getting any ROI attached to the warped image
        switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case CalculationSources.EXTERNAL:
                String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
                param.warpedRoi = workspace.getImage(externalSourceName).getImagePlus().getRoi();
                break;
            case CalculationSources.INTERNAL:
                String inputImageName = parameters.getValue(INPUT_IMAGE);
                param.warpedRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
        }

        // Getting any ROI attached to the reference image
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.FIRST_FRAME:
            case ReferenceModes.PREVIOUS_N_FRAMES:
                String inputImageName = parameters.getValue(INPUT_IMAGE);
                param.referenceRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
            case ReferenceModes.SPECIFIC_IMAGE:
                String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
                param.referenceRoi = workspace.getImage(referenceImageName).getImagePlus().getRoi();
                break;
        }

        return param;

    }

    @Override
    public AbstractAffineModel2D getAffineModel2D(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {

        ManualParam p = (ManualParam) param;

        ImagePlus referenceIpl = new ImagePlus("Reference", referenceIpr.duplicate());
        ImagePlus warpedIpl = new ImagePlus("Warped", warpedIpr.duplicate());

        // Appling any ROIs stored in the parameters
        warpedIpl.setRoi(p.warpedRoi);
        referenceIpl.setRoi(p.referenceRoi);

        ArrayList<PointPair> pairs = null;
        switch (p.pointSelectionMode) {
            case PointSelectionModes.PRESELECTED:
                pairs = PointPairSelector.getPreselectedPoints(new Image("Warped", warpedIpl),
                        new Image("Reference", referenceIpl));
                break;
            case PointSelectionModes.RUNTIME:
            default:
                pairs = new PointPairSelector(this, true).getPointPairs(warpedIpl, referenceIpl);

                // Updating ROIs in memory
                PointRoi[] rois = createRoiFromPointPairs(pairs);
                p.warpedRoi = rois[0];
                p.referenceRoi = rois[1];

                break;
        }

        if (pairs == null) {
            MIA.log.writeWarning("No points selected");
            return null;
        }

        // Getting transform
        AbstractAffineModel2D model = getModel(p.transformationMode);
        final ArrayList<PointMatch> candidates = new ArrayList<PointMatch>();

        for (PointPair pair : pairs)
            candidates.addAll(Util.pointRoisToPointMatches(pair.getPoint1(), pair.getPoint2()));

        try {
            model.fit(candidates);
        } catch (NotEnoughDataPointsException | IllDefinedDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        return model;

    }

    public static PointRoi[] createRoiFromPointPairs(ArrayList<PointPair> pairs) {
        PointRoi warpedRoi = new PointRoi();
        PointRoi referenceRoi = new PointRoi();
        
        for (PointPair pair : pairs) {
            warpedRoi.addPoint(pair.getPoint1().getXBase(), pair.getPoint1().getYBase());
            referenceRoi.addPoint(pair.getPoint2().getXBase(), pair.getPoint2().getYBase());
        }

        return new PointRoi[] { warpedRoi, referenceRoi };

    }

    @Override
    public void doAction(Object[] objects) {
        writeStatus("Running test registration");

        // String transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        // String fillMode = parameters.getValue(FILL_MODE);
        // boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // ArrayList<PointPair> pairs = (ArrayList<PointPair>) objects[0];

        // // Duplicating image
        // ImagePlus dupIpl = inputImage.getImagePlus().duplicate();
        // Image<T> dupImage = new Image<T>("Registered", dupIpl);

        // // Getting transform
        // Object[] output = getLandmarkTransformation(pairs, transformationMode);
        // InverseTransformMapping mapping = (InverseTransformMapping) output[0];

        // // Iterate over each time-step
        // int count = 0;
        // int total = dupImage.getImagePlus().getNFrames();
        // for (int t = 1; t <= dupImage.getImagePlus().getNFrames(); t++) {
        // writeStatus("Processing timepoint " + (++count) + " of " + total);

        // // Applying the transformation to the whole stack.
        // // All channels should move in the same way, so are processed with the same
        // // transformation.
        // for (int c = 1; c <= dupImage.getImagePlus().getNChannels(); c++) {
        // Image warped = ExtractSubstack.extractSubstack(dupImage, "Warped",
        // String.valueOf(c), "1-end",
        // String.valueOf(t));
        // try {
        // applyTransformation(warped, mapping, fillMode, multithread);
        // } catch (InterruptedException e) {
        // return;
        // }
        // replaceStack(dupImage, warped, c, t);
        // }

        // mapping = null;

        // }

        // ArrayList<Image<T>> images = new ArrayList<>();
        // images.add(reference);
        // images.add(dupImage);
        // ConcatenateStacks.concatenateImages(images,
        // ConcatenateStacks.AxisModes.CHANNEL, "Registration comparison")
        // .showImage();

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new ChoiceP(POINT_SELECTION_MODE, this, PointSelectionModes.RUNTIME, PointSelectionModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(POINT_SELECTION_MODE));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

    }

    public class ManualParam extends Param {
        String pointSelectionMode = PointSelectionModes.RUNTIME;
        Roi warpedRoi = null;
        Roi referenceRoi = null;

    }
}