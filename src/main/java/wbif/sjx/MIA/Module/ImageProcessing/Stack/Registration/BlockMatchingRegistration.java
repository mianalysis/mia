package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import mpicbg.ij.blockmatching.BlockMatching;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.ErrorStatistic;
import mpicbg.models.PointMatch;
import mpicbg.models.SpringMesh;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.Vertex;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddObjectCentroid;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Units.TemporalUnit;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;

public class BlockMatchingRegistration extends AutomaticRegistration {
    public static final String FEATURE_SEPARATOR = "Feature detection";
    public static final String LAYER_SCALE = "Layer scale";
    public static final String SEARCH_RADIUS = "Search radius (px)";
    public static final String BLOCK_RADIUS = "Block radius (px)";
    public static final String RESOLUTION = "Resolution";
    public static final String MIN_PMCC_R = "Minimal PMCC r";
    public static final String MAX_CURVATURE = "Maximal curvature ratio";
    public static final String ROD = "Closest/next closest ratio";
    public static final String LOCAL_REGION_SIGMA = "Local region sigma";
    public static final String MAX_ABS_LOCAL_DISPLACEMENT = "Maximal absolute local displacement (px)";
    public static final String MAX_REL_LOCAL_DISPLACEMENT = "Maximal relative local displacement (px)";

    public BlockMatchingRegistration(ModuleCollection modules) {
        super("Automatic block-matching registration", modules);
    }

    @Override
    public String getDescription() {
        return "Apply slice-by-slice (2D) affine-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration can be performed along either the time or Z axes.  The non-registered axis (e.g. time axis when registering in Z) can be \"linked\" (all frames given the same registration) or \"independent\" (each stack registered separately)."

                + "<br><br>This module uses the <a href=\"https://github.com/fiji/blockmatching\">Block Matching</a> plugin and associated MPICBG tools to detect matching regions from the input images and calculate and apply the necessary 2D affine transforms.";
    }

    @Override
    public AbstractAffineModel2D getAffineModel2D(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints) {
        BMParam p = (BMParam) param;

        // Converting to FloatProcessors and padding
        FloatProcessor ipr1 = padImage(referenceIpr, p).convertToFloatProcessor();
        FloatProcessor ipr2 = padImage(warpedIpr, p).convertToFloatProcessor();

        TranslationModel2D transform = new TranslationModel2D();
        SpringMesh mesh = new SpringMesh(p.resolution, ipr1.getWidth(), ipr2.getHeight(), 1, 1000, 0.9f);
        Collection<Vertex> vertices = mesh.getVertices();
        Vector<PointMatch> candidates = new Vector<PointMatch>();

        try {
            BlockMatching.matchByMaximalPMCC(ipr1, ipr2, null, null, p.scale, transform, p.blockR, p.blockR, p.searchR,
                    p.searchR, p.minR, p.rod, p.maxCurvature, vertices, candidates, new ErrorStatistic(1));

            if (showDetectedPoints)
                showDetectedPoints(referenceIpr, warpedIpr, candidates);

            AbstractAffineModel2D model = getModel(p.transformationMode);
            model.localSmoothnessFilter(candidates, candidates, p.sigma, p.maxAbsDisp, p.maxRelDisp);

            return model.createInverse();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;

    }

    static ImageProcessor padImage(ImageProcessor iprIn, BMParam param) {
        int padR = param.searchR + param.blockR;

        int widthIn = iprIn.getWidth();
        int heightIn = iprIn.getHeight();
        int widthOut = widthIn + 2 * padR;
        int heightOut = heightIn + 2 * padR;

        // Creating new ImageProcessor
        ImageProcessor iprOut = IJ.createImage("", widthOut, heightOut, 1, iprIn.getBitDepth()).getProcessor();

        // Setting pixel intensities
        for (int x = 0; x < widthIn; x++) {
            for (int y = 0; y < heightIn; y++) {
                iprOut.setf(x + padR, y + padR, iprIn.getf(x, y));
            }
        }

        return iprOut;

    }

    @Override
    public Status process(Workspace workspace) {
        IJ.setBackgroundColor(255, 255, 255);

        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String regAxis = parameters.getValue(REGISTRATION_AXIS);
        String otherAxisMode = parameters.getValue(OTHER_AXIS_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        String relativeMode = parameters.getValue(RELATIVE_MODE);
        int numPrevFrames = parameters.getValue(NUM_PREV_FRAMES);
        String prevFramesStatMode = parameters.getValue(PREV_FRAMES_STAT_MODE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean showDetectedPoints = parameters.getValue(SHOW_DETECTED_POINTS);

        // Getting the input image and duplicating if the output will be stored
        // separately
        Image inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());

        // If comparing to a fixed image, get this now
        Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName)
                : null;

        // Getting the image the registration will be calculated from.
        String calcC = String.valueOf(calculationChannel);
        Image calculationImage = null;
        switch (calculationSource) {
        case CalculationSources.EXTERNAL:
            Image externalImage = workspace.getImage(externalSourceName);
            calculationImage = ExtractSubstack.extractSubstack(externalImage, "CalcIm", calcC, "1-end", "1-end");
            break;

        case CalculationSources.INTERNAL:
            calculationImage = ExtractSubstack.extractSubstack(inputImage, "CalcIm", calcC, "1-end", "1-end");
            break;
        }

        // Registration will be performed in time, so ensure actual axis to be
        // registered is reordered to be in time axis
        switch (regAxis) {
        case RegistrationAxes.Z:
            changeStackOrder(inputImage);
            changeStackOrder(calculationImage);
            break;
        }

        // If non-registration dimension is "linked", calculation image potentially
        // needs to be projected. Since the images have been transformed such that the
        // registration dimension is always "Time", then this is a Z projection. A
        // maximum intensity projection is used. It only needs be performed if there is
        // at least one Z-slice.
        if (calculationImage.getImagePlus().getNSlices() > 1) {
            switch (otherAxisMode) {
            case OtherAxisModes.LINKED:
                calculationImage = ProjectImage.projectImageInZ(calculationImage, "CalcIm",
                        ProjectImage.ProjectionModes.MAX);
                break;
            }
        }

        // Ensuring calculation image has the correct dimensions
        if (testReferenceValidity(inputImage, calculationImage, otherAxisMode)) {
            // Setting up the parameters
            BMParam param = new BMParam();
            param.transformationMode = parameters.getValue(TRANSFORMATION_MODE);
            param.scale = (float) (double) parameters.getValue(LAYER_SCALE);
            param.searchR = parameters.getValue(SEARCH_RADIUS);
            param.blockR = parameters.getValue(BLOCK_RADIUS);
            param.resolution = parameters.getValue(RESOLUTION);
            param.minR = (float) (double) parameters.getValue(MIN_PMCC_R);
            param.maxCurvature = (float) (double) parameters.getValue(MAX_CURVATURE);
            param.rod = (float) (double) parameters.getValue(ROD);
            param.sigma = (float) (double) parameters.getValue(LOCAL_REGION_SIGMA);
            param.maxAbsDisp = (float) (double) parameters.getValue(MAX_ABS_LOCAL_DISPLACEMENT);
            param.maxRelDisp = (float) (double) parameters.getValue(MAX_REL_LOCAL_DISPLACEMENT);

            switch (otherAxisMode) {
            case OtherAxisModes.INDEPENDENT:
                processIndependent(inputImage, calculationImage, relativeMode, numPrevFrames, prevFramesStatMode, param,
                        fillMode, showDetectedPoints, multithread, reference);
                break;

            case OtherAxisModes.LINKED:
                processLinked(inputImage, calculationImage, relativeMode, numPrevFrames, prevFramesStatMode, param,
                        fillMode, showDetectedPoints, multithread, reference);
                break;
            }

            // If stack order was adjusted, now swap it back
            switch (regAxis) {
            case RegistrationAxes.Z:
                changeStackOrder(inputImage);
                changeStackOrder(calculationImage);
                break;
            }

        } else {
            MIA.log.writeWarning("Input stack has not been registered");
        }

        if (showOutput) {
            if (relativeMode.equals(RelativeModes.SPECIFIC_IMAGE)) {
                createOverlay(inputImage, reference).showImage();
            } else {
                inputImage.showImage();
            }
        }

        // Dealing with module outputs
        if (!applyToInput)
            workspace.addImage(inputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new DoubleP(LAYER_SCALE, this, 1.0));
        parameters.add(new IntegerP(SEARCH_RADIUS, this, 50));
        parameters.add(new IntegerP(BLOCK_RADIUS, this, 50));
        parameters.add(new IntegerP(RESOLUTION, this, 24));
        parameters.add(new DoubleP(MIN_PMCC_R, this, 0.1));
        parameters.add(new DoubleP(MAX_CURVATURE, this, 1000.0));
        parameters.add(new DoubleP(ROD, this, 1.0));
        parameters.add(new DoubleP(LOCAL_REGION_SIGMA, this, 65.0));
        parameters.add(new DoubleP(MAX_ABS_LOCAL_DISPLACEMENT, this, 12.0));
        parameters.add(new DoubleP(MAX_REL_LOCAL_DISPLACEMENT, this, 3.0));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LAYER_SCALE));
        returnedParameters.add(parameters.getParameter(SEARCH_RADIUS));
        returnedParameters.add(parameters.getParameter(BLOCK_RADIUS));
        returnedParameters.add(parameters.getParameter(RESOLUTION));
        returnedParameters.add(parameters.getParameter(MIN_PMCC_R));
        returnedParameters.add(parameters.getParameter(MAX_CURVATURE));
        returnedParameters.add(parameters.getParameter(ROD));
        returnedParameters.add(parameters.getParameter(LOCAL_REGION_SIGMA));
        returnedParameters.add(parameters.getParameter(MAX_ABS_LOCAL_DISPLACEMENT));
        returnedParameters.add(parameters.getParameter(MAX_REL_LOCAL_DISPLACEMENT));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        String siteRef1 = "Description taken from <a href=\"https://imagej.net/Feature_Extraction\">https://imagej.net/Feature_Extraction</a>";

        parameters.get(LAYER_SCALE).setDescription("");

        parameters.get(SEARCH_RADIUS).setDescription("");

        parameters.get(BLOCK_RADIUS).setDescription("");

        parameters.get(RESOLUTION).setDescription("");

        parameters.get(MIN_PMCC_R).setDescription("");

        parameters.get(MAX_CURVATURE).setDescription("");

        parameters.get(ROD).setDescription(
                "\"Correspondence candidates from local descriptor matching are accepted only if the Euclidean distance to the nearest neighbour is significantly smaller than that to the next nearest neighbour. Lowe (2004) suggests a ratio of r=0.8 which requires some increase when matching things that appear significantly distorted.\".  "
                        + siteRef1);

        parameters.get(LOCAL_REGION_SIGMA).setDescription("");

        parameters.get(MAX_ABS_LOCAL_DISPLACEMENT).setDescription("");

        parameters.get(MAX_REL_LOCAL_DISPLACEMENT).setDescription("");

    }

    public class BMParam extends Param {
        float scale = 1.0f;
        int searchR = 50;
        int blockR = 50;
        int resolution = 24;
        float minR = 0.1f;
        float maxCurvature = 1000.0f;
        float rod = 1.0f;
        float sigma = 65.0f;
        float maxAbsDisp = 12.0f;
        float maxRelDisp = 3.0f;

    }
}
