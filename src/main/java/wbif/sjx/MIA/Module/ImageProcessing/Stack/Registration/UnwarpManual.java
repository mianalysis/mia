package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract.AbstractBUnwarpJRegistration;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Process.Interactable.Interactable;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;
import wbif.sjx.MIA.ThirdParty.bUnwarpJ_Mod;

public class UnwarpManual extends AbstractBUnwarpJRegistration implements Interactable {
    public static final String POINT_SELECTION_MODE = "Point selection mode";

    public interface PointSelectionModes {
        String PRESELECTED = "Pre-selected points";
        String RUNTIME = "Select at runtime";

        String[] ALL = new String[] { PRESELECTED, RUNTIME };

    }

    public UnwarpManual(ModuleCollection modules) {
        super("Unwarp (manual)", modules);
    }

    @Override
    public String getDescription() {
        return "";

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
        manualParam.pointSelectionMode = parameters.getValue(POINT_SELECTION_MODE);

        // Getting any ROI attached to the warped image
        switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case CalculationSources.EXTERNAL:
                String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
                manualParam.warpedRoi = workspace.getImage(externalSourceName).getImagePlus().getRoi();
                break;
            case CalculationSources.INTERNAL:
                String inputImageName = parameters.getValue(INPUT_IMAGE);
                manualParam.warpedRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
        }

        // Getting any ROI attached to the reference image
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.FIRST_FRAME:
            case ReferenceModes.PREVIOUS_N_FRAMES:
                String inputImageName = parameters.getValue(INPUT_IMAGE);
                manualParam.referenceRoi = workspace.getImage(inputImageName).getImagePlus().getRoi();
                break;
            case ReferenceModes.SPECIFIC_IMAGE:
                String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
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

        // Converting point pairs into format for bUnwarpJ
        Stack<Point> points1 = new Stack<>();
        Stack<Point> points2 = new Stack<>();
        for (PointPair pair : pairs) {
            PointRoi pointRoi1 = pair.getPoint1();
            PointRoi pointRoi2 = pair.getPoint2();

            points1.push(new Point((int) pointRoi1.getXBase(), (int) pointRoi1.getYBase()));
            points2.push(new Point((int) pointRoi2.getXBase(), (int) pointRoi2.getYBase()));

        }

        Transformation transformation = bUnwarpJ_Mod.computeTransformationBatch(warpedIpr, referenceIpr, points1,
                points2, p.bParam);

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
            e.printStackTrace();
            return null;
        }
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

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ChoiceP(POINT_SELECTION_MODE, this, PointSelectionModes.RUNTIME, PointSelectionModes.ALL));
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

    public class ManualBUnwarpJParam extends BUnwarpJParam {
        public String pointSelectionMode = PointSelectionModes.RUNTIME;
        public Roi warpedRoi = null;
        public Roi referenceRoi = null;

    }
}
