package io.github.mianalysis.mia.module.images.transform.registration;

import java.util.ArrayList;
import java.util.Vector;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ConcatenateStacks2;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.PointPair;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.process.selectors.Interactable;
import io.github.mianalysis.mia.process.selectors.PointPairSelector;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.util.Util;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;


/**
* Apply 2D affine transforms to align images from the workspace to other images from the workspace using manually-selected reference points.  When the module runs, the input and reference images are displayed. The user then selects matching points on each image and clicks "Add pair(s)". Points must be added in the same order on each image (ID numbers next to each point provide a reference). Points are shown in the control window and can be deleted by highlighting the relevant entry and clicking "Remove pair". Finally, the alignment is accepted by clicking "Finish adding pairs", at which point the images are closed and the transform is applied.  If multiple slices/timepoints are to be aligned, the next image pair will immediately be displayed and the processes is repeated.  The transformed input image can either overwrite the input image in the workspace, or be saved to the workspace with a new name.Alignments are calculated using the <a href="https://github.com/axtimwalde/mpicbg">MPICBG</a> image transformation library.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AffineManual<T extends RealType<T> & NativeType<T>> extends AbstractAffineRegistration
        implements Interactable {

	/**
	* 
	*/
    public static final String FEATURE_SEPARATOR = "Feature detection";

	/**
	* The source for points to be used in calculation of image registration:<br><ul><li>"Pre-selected points" Points have been previously-selected on the input images as multi-point ROIs.  These points are passed directly into the registration calculation.  This negates the need for user-interaction at runtime.</li><li>"Select at runtime" Points must be manually-selected by the user at analysis runtime.  The two images to be aligned are displayed and a dialog box opens to allow selection of point pairs.  Point pairs must be added in the same order on each image.  For images where multiple slices/timepoints need to be registered, image pairs will be opened sequentially, with the point selections from the previous slice/timepoint being pre-selected for convenience.</li></ul>
	*/
    public static final String POINT_SELECTION_MODE = "Point selection mode";

    public interface PointSelectionModes {
        String PRESELECTED = "Pre-selected points";
        String RUNTIME = "Select at runtime";

        String[] ALL = new String[] { PRESELECTED, RUNTIME };

    }

    public AffineManual(Modules modules) {
        super("Affine (manual)", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.1";
    }

    @Override
    public String getDescription() {
        return "Apply 2D affine transforms to align images from the workspace to other images from the workspace using manually-selected reference points.  When the module runs, the input and reference images are displayed. The user then selects matching points on each image and clicks \"Add pair(s)\". Points must be added in the same order on each image (ID numbers next to each point provide a reference). Points are shown in the control window and can be deleted by highlighting the relevant entry and clicking \"Remove pair\". Finally, the alignment is accepted by clicking \"Finish adding pairs\", at which point the images are closed and the transform is applied.  If multiple slices/timepoints are to be aligned, the next image pair will immediately be displayed and the processes is repeated.  The transformed input image can either overwrite the input image in the workspace, or be saved to the workspace with a new name."

                + "Alignments are calculated using the <a href=\"https://github.com/axtimwalde/mpicbg\">MPICBG</a> image transformation library.";

    }

    @Override
    public ManualParam createParameterSet() {
        return new ManualParam();
    }

    @Override
    public void getParameters(Param param, WorkspaceI workspace) {
        super.getParameters(param, workspace);

        // Setting up the parameters
        ManualParam manualParam = (ManualParam) param;
        manualParam.pointSelectionMode = parameters.getValue(POINT_SELECTION_MODE, workspace);

        // Getting any ROI attached to the warped image
        switch ((String) parameters.getValue(CALCULATION_SOURCE, workspace)) {
            case CalculationSources.EXTERNAL:
                String externalSourceName = parameters.getValue(EXTERNAL_SOURCE, workspace);
                manualParam.warpedRoi = workspace.getImage(externalSourceName).getImagePlus().getRoi();
                break;
            case CalculationSources.INTERNAL:
                String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
                manualParam.warpedRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
        }

        // Getting any ROI attached to the reference image
        switch ((String) parameters.getValue(REFERENCE_MODE, workspace)) {
            case ReferenceModes.FIRST_FRAME:
            case ReferenceModes.PREVIOUS_N_FRAMES:
                String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
                manualParam.referenceRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
            case ReferenceModes.SPECIFIC_IMAGE:
                String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
                manualParam.referenceRoi = workspace.getImage(referenceImageName).getImagePlus().getRoi();
                break;
        }
    }

    @Override
    protected Object[] fitModel(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param) {
        ManualParam p = (ManualParam) param;

        ImagePlus referenceIpl = new ImagePlus("Reference", referenceIpr.duplicate());
        ImagePlus warpedIpl = new ImagePlus("Warped", warpedIpr.duplicate());

        // Appling any ROIs stored in the parameters
        warpedIpl.setRoi(p.warpedRoi);
        referenceIpl.setRoi(p.referenceRoi);

        ArrayList<PointPair> pairs = null;
        switch (p.pointSelectionMode) {
            case PointSelectionModes.PRESELECTED:
                pairs = PointPairSelector.getPreselectedPoints(ImageFactory.createImage("Warped", warpedIpl),
                        ImageFactory.createImage("Reference", referenceIpl));
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
        final Vector<PointMatch> candidates = new Vector<PointMatch>();
        for (PointPair pair : pairs)
            candidates.addAll(Util.pointRoisToPointMatches(pair.getPoint1(), pair.getPoint2()));

        try {
            model.fit(candidates);
        } catch (NotEnoughDataPointsException | IllDefinedDataPointsException e) {
            MIA.log.writeError(e);
            return null;
        }

        return new Object[] { model, candidates };

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
        String transformationMode = parameters.getValue(TRANSFORMATION_MODE, null);
        String fillMode = parameters.getValue(FILL_MODE, null);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, null);

        ArrayList<PointPair> pairs = (ArrayList<PointPair>) objects[0];
        ImagePlus ipl1 = ((ImagePlus) objects[1]).duplicate();
        ImagePlus ipl2 = ((ImagePlus) objects[2]).duplicate();

        // Duplicating image
        ImageI<T> image1 = ImageFactory.createImage("Registered", ipl1);
        ImageI<T> image2 = ImageFactory.createImage("Reference", ipl2);

        AbstractAffineModel2D model = getModel(transformationMode);
        final ArrayList<PointMatch> candidates = new ArrayList<PointMatch>();
        for (PointPair pair : pairs)
            candidates.addAll(Util.pointRoisToPointMatches(pair.getPoint1(), pair.getPoint2()));

        try {
            model.fit(candidates);
        } catch (NotEnoughDataPointsException | IllDefinedDataPointsException e) {
            MIA.log.writeError(e);
            return;
        }

        AffineTransform transform = new AffineTransform();
        transform.mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);

        try {
            applyTransformation(image1, transform, fillMode, multithread);
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }

        ArrayList<ImageI<T>> images = new ArrayList<>();
        images.add(image1);
        images.add(image2);
        ConcatenateStacks2.process(images, ConcatenateStacks2.AxisModes.CHANNEL, "Registration comparison")
                .showAsIs();

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new ChoiceP(POINT_SELECTION_MODE, this, PointSelectionModes.RUNTIME, PointSelectionModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(POINT_SELECTION_MODE));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(POINT_SELECTION_MODE)
                .setDescription("The source for points to be used in calculation of image registration:<br><ul>"

                        + "<li>\"" + PointSelectionModes.PRESELECTED
                        + "\" Points have been previously-selected on the input images as multi-point ROIs.  These points are passed directly into the registration calculation.  This negates the need for user-interaction at runtime.</li>"

                        + "<li>\"" + PointSelectionModes.RUNTIME
                        + "\" Points must be manually-selected by the user at analysis runtime.  The two images to be aligned are displayed and a dialog box opens to allow selection of point pairs.  Point pairs must be added in the same order on each image.  For images where multiple slices/timepoints need to be registered, image pairs will be opened sequentially, with the point selections from the previous slice/timepoint being pre-selected for convenience.</li></ul>");

    }

    public class ManualParam extends AffineParam {
        String pointSelectionMode = PointSelectionModes.RUNTIME;
        Roi warpedRoi = null;
        Roi referenceRoi = null;

    }
}
