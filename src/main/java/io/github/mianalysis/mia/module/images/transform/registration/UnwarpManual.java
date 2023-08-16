package io.github.mianalysis.mia.module.images.transform.registration;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import bunwarpj.Transformation;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ConcatenateStacks;
import io.github.mianalysis.mia.module.images.transform.registration.abstrakt.AbstractBUnwarpJRegistration;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.process.interactable.Interactable;
import io.github.mianalysis.mia.process.interactable.PointPairSelector;
import io.github.mianalysis.mia.process.interactable.PointPairSelector.PointPair;
import io.github.mianalysis.mia.thirdparty.bUnwarpJ_Mod;


/**
* Apply 2D B-spline unwarping transforms to align images from the workspace to other images from the workspace using manually-selected reference points.  When the module runs, the input and reference images are displayed. The user then selects matching points on each image and clicks "Add pair(s)". Points must be added in the same order on each image (ID numbers next to each point provide a reference). Points are shown in the control window and can be deleted by highlighting the relevant entry and clicking "Remove pair". Finally, the alignment is accepted by clicking "Finish adding pairs", at which point the images are closed and the transform is applied.  If multiple slices/timepoints are to be aligned, the next image pair will immediately be displayed and the processes is repeated.  The transformed input image can either overwrite the input image in the workspace, or be saved to the workspace with a new name.Alignments are calculated using the <a href="https://imagej.net/BUnwarpJ">BUnwarpJ</a> image transformation library.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class UnwarpManual<T extends RealType<T> & NativeType<T>> extends AbstractBUnwarpJRegistration
        implements Interactable {

	/**
	* The source for points to be used in calculation of image registration:<br><ul><li>"Pre-selected points" Points have been previously-selected on the input images as multi-point ROIs.  These points are passed directly into the registration calculation.  This negates the need for user-interaction at runtime.</li><li>"Select at runtime" Points must be manually-selected by the user at analysis runtime.  The two images to be aligned are displayed and a dialog box opens to allow selection of point pairs.  Point pairs must be added in the same order on each image.  For images where multiple slices/timepoints need to be registered, image pairs will be opened sequentially, with the point selections from the previous slice/timepoint being pre-selected for convenience.</li></ul>
	*/
    public static final String POINT_SELECTION_MODE = "Point selection mode";

    public interface PointSelectionModes {
        String PRESELECTED = "Pre-selected points";
        String RUNTIME = "Select at runtime";

        String[] ALL = new String[] { PRESELECTED, RUNTIME };

    }

    public UnwarpManual(Modules modules) {
        super("Unwarp (manual)", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Apply 2D B-spline unwarping transforms to align images from the workspace to other images from the workspace using manually-selected reference points.  When the module runs, the input and reference images are displayed. The user then selects matching points on each image and clicks \"Add pair(s)\". Points must be added in the same order on each image (ID numbers next to each point provide a reference). Points are shown in the control window and can be deleted by highlighting the relevant entry and clicking \"Remove pair\". Finally, the alignment is accepted by clicking \"Finish adding pairs\", at which point the images are closed and the transform is applied.  If multiple slices/timepoints are to be aligned, the next image pair will immediately be displayed and the processes is repeated.  The transformed input image can either overwrite the input image in the workspace, or be saved to the workspace with a new name."

                + "Alignments are calculated using the <a href=\"https://imagej.net/BUnwarpJ\">BUnwarpJ</a> image transformation library.";

    }

    @Override
    public ManualBUnwarpJParam createParameterSet() {
        return new ManualBUnwarpJParam();
    }

    @Override
    public void getParameters(Param param, Workspace workspace) {
        super.getParameters(param, workspace);

        // Setting up the parameters
        ManualBUnwarpJParam manualParam = (ManualBUnwarpJParam) param;
        manualParam.pointSelectionMode = parameters.getValue(POINT_SELECTION_MODE,workspace);

        // In test points mode we don't want to update the ROIs, so the workspace is set
        // to null
        if (workspace == null)
            return;

        // Getting any ROI attached to the warped image
        switch ((String) parameters.getValue(CALCULATION_SOURCE,workspace)) {
            case CalculationSources.EXTERNAL:
                String externalSourceName = parameters.getValue(EXTERNAL_SOURCE,workspace);
                manualParam.warpedRoi = workspace.getImage(externalSourceName).getImagePlus().getRoi();
                break;
            case CalculationSources.INTERNAL:
                String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
                manualParam.warpedRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
        }

        // Getting any ROI attached to the reference image
        switch ((String) parameters.getValue(REFERENCE_MODE,workspace)) {
            case ReferenceModes.FIRST_FRAME:
            case ReferenceModes.PREVIOUS_N_FRAMES:
                String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
                manualParam.referenceRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
            case ReferenceModes.SPECIFIC_IMAGE:
                String referenceImageName = parameters.getValue(REFERENCE_IMAGE,workspace);
                manualParam.referenceRoi = workspace.getImage(referenceImageName).getImagePlus().getRoi();
                break;
        }
    }

    @Override
    public Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {
        ManualBUnwarpJParam p = (ManualBUnwarpJParam) param;

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
                PointRoi[] rois = convertPointPairsToRoi(pairs);
                p.warpedRoi = rois[0];
                p.referenceRoi = rois[1];

                break;
        }

        if (pairs == null) {
            MIA.log.writeWarning("No points selected");
            return null;
        }

        if (showDetectedPoints)
            showDetectedPoints(referenceIpr, warpedIpr, pairs);

        ArrayList<Stack<Point>> points = convertPointPairsToPointStacks(pairs);
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

    public static PointRoi[] convertPointPairsToRoi(ArrayList<PointPair> pairs) {
        PointRoi warpedRoi = new PointRoi();
        PointRoi referenceRoi = new PointRoi();

        for (PointPair pair : pairs) {
            warpedRoi.addPoint(pair.getPoint1().getXBase(), pair.getPoint1().getYBase());
            referenceRoi.addPoint(pair.getPoint2().getXBase(), pair.getPoint2().getYBase());
        }

        return new PointRoi[] { warpedRoi, referenceRoi };

    }

    public static ArrayList<Stack<Point>> convertPointPairsToPointStacks(ArrayList<PointPair> pairs) {
        // Converting point pairs into format for bUnwarpJ
        Stack<Point> points1 = new Stack<>();
        Stack<Point> points2 = new Stack<>();
        for (PointPair pair : pairs) {
            PointRoi pointRoi1 = pair.getPoint1();
            PointRoi pointRoi2 = pair.getPoint2();

            points1.push(new Point((int) pointRoi1.getXBase(), (int) pointRoi1.getYBase()));
            points2.push(new Point((int) pointRoi2.getXBase(), (int) pointRoi2.getYBase()));

        }

        ArrayList<Stack<Point>> stacks = new ArrayList<Stack<Point>>();
        stacks.add(points1);
        stacks.add(points2);

        return stacks;

    }

    @Override
    public void doAction(Object[] objects) {
        ManualBUnwarpJParam params = new ManualBUnwarpJParam();
        getParameters(params, null);

        String fillMode = parameters.getValue(FILL_MODE,null);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,null);

        ArrayList<PointPair> pairs = (ArrayList<PointPair>) objects[0];
        ImagePlus ipl1 = ((ImagePlus) objects[1]).duplicate();
        ImagePlus ipl2 = ((ImagePlus) objects[2]).duplicate();

        // Duplicating image
        Image image1 = ImageFactory.createImage("Registered", ipl1);
        Image image2 = ImageFactory.createImage("Reference", ipl2);

        ArrayList<Stack<Point>> points = convertPointPairsToPointStacks(pairs);
        Transformation transformation = bUnwarpJ_Mod.computeTransformationBatch(ipl1.getProcessor(),
                ipl2.getProcessor(), points.get(0), points.get(1), params.bParam);

        BUnwarpJTransform transform = new BUnwarpJTransform();
        try {
            File tempFile = File.createTempFile("unwarp", ".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            bufferedWriter.close();

            String tempPath = tempFile.getAbsolutePath();
            transformation.saveDirectTransformation(tempPath);
            transform.transformPath = tempPath;

            applyTransformation(image1, transform, fillMode, multithread);

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return;
        } catch (IOException e) {
            MIA.log.writeError(e);
            return;
        }

        ArrayList<Image> images = new ArrayList<>();
        images.add(image1);
        images.add(image2);
        ConcatenateStacks.concatenateImages(images, ConcatenateStacks.AxisModes.CHANNEL, "Registration comparison")
                .show();

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ChoiceP(POINT_SELECTION_MODE, this, PointSelectionModes.RUNTIME, PointSelectionModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
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

    class ManualBUnwarpJParam extends BUnwarpJParam {
        public String pointSelectionMode = PointSelectionModes.RUNTIME;
        public Roi warpedRoi = null;
        public Roi referenceRoi = null;

    }
}
