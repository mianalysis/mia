package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.util.ArrayList;
import java.util.Vector;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DMOPS;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.UnwarpImages;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class AutomaticRegistration<T extends RealType<T> & NativeType<T>> extends CoreRegistrationHandler {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String REGISTRATION_AXIS = "Registration axis";
    public static final String OTHER_AXIS_MODE = "Other axis mode";
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String FILL_MODE = "Fill mode";

    public static final String REFERENCE_SEPARATOR = "Reference image source";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String ROLLING_CORRECTION = "Rolling correction";
    public static final String CORRECTION_INTERVAL = "Correction interval";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";

    public static final String FEATURE_SEPARATOR = "Feature detection (SIFT)";
    public static final String INITIAL_SIGMA = "Initial Gaussian blur (px)";
    public static final String STEPS = "Steps per scale";
    public static final String MINIMUM_IMAGE_SIZE = "Minimum image size (px)";
    public static final String MAXIMUM_IMAGE_SIZE = "Maximum image size (px)";
    public static final String FD_SIZE = "Feature descriptor size";
    public static final String FD_ORIENTATION_BINS = "Feature descriptor orientation bins";
    public static final String ROD = "Closest/next closest ratio";
    public static final String MAX_EPSILON = "Maximal alignment error (px)";
    public static final String MIN_INLIER_RATIO = "Inlier ratio";

    public AutomaticRegistration(ModuleCollection modules) {
        super("Automatic registration", modules);
    }

    public interface RegistrationAxes {
        String TIME = "Time";
        String Z = "Z";

        String[] ALL = new String[] { TIME, Z };

    }

    public interface OtherAxisModes {
        String INDEPENDENT = "Independent";
        String LINKED = "Linked";

        String[] ALL = new String[] { INDEPENDENT, LINKED };

    }

    public interface RelativeModes {
        final String FIRST_FRAME = "First frame";
        final String PREVIOUS_FRAME = "Previous frame";
        final String SPECIFIC_IMAGE = "Specific image";

        final String[] ALL = new String[] { FIRST_FRAME, PREVIOUS_FRAME, SPECIFIC_IMAGE };

    }

    public interface RollingCorrectionModes {
        final String NONE = "None";
        final String EVERY_NTH_FRAME = "Every nth frame";

        final String[] ALL = new String[] { NONE, EVERY_NTH_FRAME };

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public interface TransformationModes {
        String AFFINE = "Affine";
        String RIGID = "Rigid";
        String SIMILARITY = "Similarity";
        String TRANSLATION = "Translation";

        String[] ALL = new String[] { AFFINE, RIGID, SIMILARITY, TRANSLATION };

    }

    public interface FillModes {
        String BLACK = "Black";
        String WHITE = "White";

        String[] ALL = new String[] { BLACK, WHITE };

    }

    boolean testReferenceValidity(Image inputImage, Image calculationImage, String otherAxisMode) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus calculationIpl = calculationImage.getImagePlus();

        // Stacks should already be ordered such that the registration axis is along the
        // time axis. Calculation image should have equal number of frames as the input.
        // Also, if "slices" are linked, it should only have one slice. If "slices" are
        // independent, it should have the same number of slices as the input.
        switch (otherAxisMode) {
            case OtherAxisModes.INDEPENDENT:
                if (calculationIpl.getNSlices() != inputIpl.getNSlices()) {
                    MIA.log.writeWarning(
                            "Non-registration axis of calculation image stack is different length to input image stack.  Calculation stack has "
                                    + calculationIpl.getNSlices() + " images, input stack has " + inputIpl.getNSlices()
                                    + " images.  In \"" + OtherAxisModes.INDEPENDENT
                                    + "\" mode, calculation and input stacks should be the same length along non-registration axis.");
                    return false;
                }
                break;

            case OtherAxisModes.LINKED:
                if (calculationIpl.getNSlices() > 1) {
                    MIA.log.writeWarning("Non-registration axis of calculation image stack is too large ("
                            + calculationIpl.getNSlices() + ").  In \"" + OtherAxisModes.LINKED
                            + "\" mode, calculation stack should have a single image along non-registration axis.");
                    return false;
                }
                break;
        }

        // Irrespective of other axis mode, "time" axis should be the same length
        if (calculationIpl.getNFrames() != inputIpl.getNFrames()) {
            MIA.log.writeWarning("Calculation image stack has different length to input image.  Calculation stack has "
                    + calculationIpl.getNFrames() + " images and, input stack has " + inputIpl.getNFrames()
                    + " images.");
            return false;
        }

        // Reference stack is valid
        return true;

    }

    public void processIndependent(Image inputImage, Image calculationImage, String relativeMode, Param param,
            int correctionInterval, String fillMode, boolean multithread, @Nullable Image reference) {
        // This works in a very similar manner to processLinked, except it's performed
        // one slice at a time
        for (int z = 0; z < inputImage.getImagePlus().getNSlices(); z++) {
            // Getting the current slices (input and calculation)
            Image currInputImage = ExtractSubstack.extractSubstack(inputImage, inputImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");
            Image currCalcImage = ExtractSubstack.extractSubstack(calculationImage, calculationImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");

            // Performing the registration on this slice
            processLinked(currInputImage, currCalcImage, relativeMode, param, correctionInterval, fillMode, multithread,
                    reference);

            // Replacing all images in this slice of the input with the registered images
            replaceSlice(inputImage, currInputImage, z);

        }
    }

    public void processLinked(Image inputImage, Image calculationImage, String relativeMode, Param param,
            int correctionInterval, String fillMode, boolean multithread, @Nullable Image reference) {
        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1", "1");
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null)
                    return;
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int nFrames = calculationImage.getImagePlus().getNFrames();
        for (int t = 0; t < nFrames; t++) {
            writeStatus("Processing frame " + (++count) + " of " + nFrames);

            // If the reference image is the previous frame, get this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                if (t == 0)
                    continue;

                reference = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1", String.valueOf(t));

            }

            // Getting the calculation image at this time-point
            Image warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1", String.valueOf(t + 1));

            // Calculating the transformation for this image pair
            AbstractAffineModel2D model = getAffineModel2D(reference.getImagePlus().getProcessor(),
                    warped.getImagePlus().getProcessor(), param);
            InverseTransformMapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);

            // By default the calculated registration will only be applied to the current
            // timepoint; however, if using the previous frame and in rolling correction
            // mode (and at the specified interval) we apply the registration to all
            // subsequent frames.
            int t2 = t;
            switch (relativeMode) {
                case UnwarpImages.RelativeModes.PREVIOUS_FRAME:
                    if (correctionInterval != -1 && t % correctionInterval == 0)
                        t2 = nFrames - 1;
                    break;
            }

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int tt = t; tt <= t2; tt++) {
                for (int c = 0; c < inputImage.getImagePlus().getNChannels(); c++) {
                    warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c + 1), "1-end",
                            String.valueOf(tt + 1));

                    try {
                        applyTransformation(warped, mapping, fillMode, multithread);
                    } catch (InterruptedException e) {
                        return;
                    }

                    replaceStack(inputImage, warped, c, tt);

                }
            }

            // Need to apply the warp to an external image
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                for (int tt = t; tt <= t2; tt++) {
                    warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1",
                            String.valueOf(tt + 1));
                    try {
                        applyTransformation(warped, mapping, fillMode, multithread);
                    } catch (InterruptedException e) {
                        return;
                    }
                    replaceStack(calculationImage, warped, 0, tt);
                }
            }

            mapping = null;

        }
    }

    public static AbstractAffineModel2D getAffineModel2D(ImageProcessor referenceIpr, ImageProcessor warpedIpr,
            Param param) {
        // Initialising SIFT feature extractor
        FloatArray2DSIFT sift = new FloatArray2DSIFT(param);
        SIFT ijSIFT = new SIFT(sift);

        // Extracting features
        ArrayList<Feature> featureList1 = new ArrayList<Feature>();
        ijSIFT.extractFeatures(referenceIpr, featureList1);
        ArrayList<Feature> featureList2 = new ArrayList<Feature>();
        ijSIFT.extractFeatures(warpedIpr, featureList2);

        // Running registration
        AbstractAffineModel2D model = getModel(param.transformationMode);
        Vector<PointMatch> candidates = FloatArray2DSIFT.createMatches(featureList2, featureList1, 1.5f, null,
                Float.MAX_VALUE, param.rod);
        Vector<PointMatch> inliers = new Vector<PointMatch>();

        try {
            model.filterRansac(candidates, inliers, 1000, param.maxEpsilon, param.minInlierRatio);
        } catch (NotEnoughDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        return model;

    }

    static <T extends RealType<T> & NativeType<T>> Image createOverlay(Image<T> inputImage, Image<T> referenceImage) {
        // Only create the overlay if the two images have matching dimensions
        ImagePlus ipl1 = inputImage.getImagePlus();
        ImagePlus ipl2 = referenceImage.getImagePlus();

        if (ipl1.getNSlices() == ipl2.getNSlices() && ipl1.getNFrames() == ipl2.getNFrames()) {
            String axis = ConcatenateStacks.AxisModes.CHANNEL;
            ArrayList<Image<T>> images = new ArrayList<>();
            images.add(inputImage);
            images.add(referenceImage);
            return ConcatenateStacks.concatenateImages(images, axis, "Overlay");
        }

        return inputImage;

    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK_REGISTRATION;
    }

    @Override
    public String getDescription() {
        return "Apply slice-by-slice (2D) affine-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration can be performed along either the time or Z axes.  The non-registered axis (e.g. time axis when registering in Z) can be \"linked\" (all frames given the same registration) or \"independent\" (each stack registered separately)."

                + "<br><br>This module uses the <a href=\"https://imagej.net/Feature_Extraction\">Feature Extraction</a> and <a href=\"https://imagej.net/Linear_Stack_Alignment_with_SIFT\">Linear Stack Alignment with SIFT</a> plugins to detect SIFT (\"Scale Invariant Feature Transform\") features from the input images and calculate and apply the necessary 2D affine transforms."

                + "<br><br>Note: The SIFT-algorithm is protected by U.S. Patent 6,711,293: Method and apparatus for identifying scale invariant features in an image and use of same for locating an object in an image by the University of British Columbia. That is, for commercial applications the permission of the author is required. Anything else is published under the terms of the GPL, so feel free to use it for academic or personal purposes."

                + "<br><br>References:<ul>"

                + "<li>Lowe, David G. \"Object recognition from local scale-invariant features\". <i>Proceedings of the International Conference on Computer Vision</i> <b>2</b> (1999) 1150–1157.</li>"

                + "<li>Lowe, David G. \"Distinctive Image Features from Scale-Invariant Keypoints\". <i>International Journal of Computer Vision</i> <b>60</b> (2004) 91–110.</li></ul>";
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
        String relativeMode = parameters.getValue(RELATIVE_MODE);
        String rollingCorrectionMode = parameters.getValue(ROLLING_CORRECTION);
        int correctionInterval = parameters.getValue(CORRECTION_INTERVAL);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        double initialSigma = parameters.getValue(INITIAL_SIGMA);
        String transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        double rod = parameters.getValue(ROD);
        double maxEpsilon = parameters.getValue(MAX_EPSILON);
        double minInlierRatio = parameters.getValue(MIN_INLIER_RATIO);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If the rolling correction mode is off, set the interval to -1
        if (rollingCorrectionMode.equals(RollingCorrectionModes.NONE))
            correctionInterval = -1;

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
            Param param = new Param();
            param.transformationMode = transformationMode;
            param.initialSigma = (float) initialSigma;
            param.steps = parameters.getValue(STEPS);
            param.minOctaveSize = parameters.getValue(MINIMUM_IMAGE_SIZE);
            param.maxOctaveSize = parameters.getValue(MAXIMUM_IMAGE_SIZE);
            param.fdSize = parameters.getValue(FD_SIZE);
            param.fdBins = parameters.getValue(FD_ORIENTATION_BINS);
            param.rod = (float) rod;
            param.maxEpsilon = (float) maxEpsilon;
            param.minInlierRatio = (float) minInlierRatio;

            switch (otherAxisMode) {
                case OtherAxisModes.INDEPENDENT:
                    processIndependent(inputImage, calculationImage, relativeMode, param, correctionInterval, fillMode,
                            multithread, reference);
                    break;

                case OtherAxisModes.LINKED:
                    processLinked(inputImage, calculationImage, relativeMode, param, correctionInterval, fillMode,
                            multithread, reference);
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

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new ChoiceP(RELATIVE_MODE, this, RelativeModes.FIRST_FRAME, RelativeModes.ALL));
        parameters.add(new ChoiceP(ROLLING_CORRECTION, this, RollingCorrectionModes.NONE, RollingCorrectionModes.ALL));
        parameters.add(new IntegerP(CORRECTION_INTERVAL, this, 1));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));
        parameters.add(new IntegerP(CALCULATION_CHANNEL, this, 1));

        parameters.add(new SeparatorP(FEATURE_SEPARATOR, this));
        parameters.add(new DoubleP(INITIAL_SIGMA, this, 1.6));
        parameters.add(new IntegerP(STEPS, this, 3));
        parameters.add(new IntegerP(MINIMUM_IMAGE_SIZE, this, 64));
        parameters.add(new IntegerP(MAXIMUM_IMAGE_SIZE, this, 1024));
        parameters.add(new IntegerP(FD_SIZE, this, 4));
        parameters.add(new IntegerP(FD_ORIENTATION_BINS, this, 8));
        parameters.add(new DoubleP(ROD, this, 0.92));
        parameters.add(new DoubleP(MAX_EPSILON, this, 25.0));
        parameters.add(new DoubleP(MIN_INLIER_RATIO, this, 0.05));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RELATIVE_MODE));
        switch ((String) parameters.getValue(RELATIVE_MODE)) {
            case UnwarpImages.RelativeModes.PREVIOUS_FRAME:
                returnedParameters.add(parameters.getParameter(ROLLING_CORRECTION));
                switch ((String) parameters.getValue(ROLLING_CORRECTION)) {
                    case UnwarpImages.RollingCorrectionModes.EVERY_NTH_FRAME:
                        returnedParameters.add(parameters.getParameter(CORRECTION_INTERVAL));
                        break;
                }
                break;

            case UnwarpImages.RelativeModes.SPECIFIC_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
        switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case UnwarpImages.CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));

        returnedParameters.add(parameters.getParameter(FEATURE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INITIAL_SIGMA));
        returnedParameters.add(parameters.getParameter(STEPS));
        returnedParameters.add(parameters.getParameter(MINIMUM_IMAGE_SIZE));
        returnedParameters.add(parameters.getParameter(MAXIMUM_IMAGE_SIZE));
        returnedParameters.add(parameters.getParameter(FD_SIZE));
        returnedParameters.add(parameters.getParameter(FD_ORIENTATION_BINS));
        returnedParameters.add(parameters.getParameter(ROD));
        returnedParameters.add(parameters.getParameter(MAX_EPSILON));
        returnedParameters.add(parameters.getParameter(MIN_INLIER_RATIO));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        String siteRef = "Description taken from <a href=\"https://imagej.net/Feature_Extraction\">https://imagej.net/Feature_Extraction</a>";

        parameters.get(RELATIVE_MODE).setDescription("");

        parameters.get(ROLLING_CORRECTION).setDescription("");

        parameters.get(CORRECTION_INTERVAL).setDescription("");

        parameters.get(REFERENCE_IMAGE).setDescription("");

        parameters.get(CALCULATION_SOURCE).setDescription("");

        parameters.get(EXTERNAL_SOURCE).setDescription("");

        parameters.get(CALCULATION_CHANNEL).setDescription("");

        parameters.get(INITIAL_SIGMA).setDescription(
                "\"Accurate localization of keypoints requires initial smoothing of the image. If your images are blurred already, you might lower the initial blur σ0 slightly to get more but eventually less stable keypoints. Increasing σ0 increases the computational cost for Gaussian blur, setting it to σ0=3.2px is equivalent to keep σ0=1.6px and use half maximum image size. Tip: Keep the default value σ0=1.6px as suggested by Lowe (2004).\".  "
                        + siteRef);

        parameters.get(STEPS).setDescription(
                "\"Keypoint candidates are extracted at all scales between maximum image size and minimum image size. This Scale Space is represented in octaves each covering a fixed number of discrete scale steps from σ0 to 2σ0. More steps result in more but eventually less stable keypoint candidates. Tip: Keep 3 as suggested by Lowe (2004) and do not use more than 10.\".  "
                        + siteRef);

        parameters.get(MINIMUM_IMAGE_SIZE).setDescription(
                "\"The Scale Space stops if the size of the octave would be smaller than minimum image size. Tip: Increase the minimum size to discard large features (i.e. those extracted from looking at an image from far, such as the overall shape).\".  "
                        + siteRef);

        parameters.get(MAXIMUM_IMAGE_SIZE).setDescription(
                "\"The Scale Space starts with the first octave equal or smaller than the maximum image size. Tip: By reducing the size, fine scaled features will be discarded. Increasing the size beyond that of the actual images has no effect.\".  "
                        + siteRef);

        parameters.get(FD_SIZE).setDescription(
                "\"The SIFT-descriptor consists of n×n gradient histograms, each from a 4×4px block. n is this value. Lowe (2004) uses n=4. We found larger descriptors with n=8 perform better for Transmission Electron Micrographs from serial sections.\".  "
                        + siteRef);

        parameters.get(FD_ORIENTATION_BINS).setDescription(
                "\"For SIFT-descriptors, this is the number of orientation bins b per 4×4px block as described above. Tip: Keep the default value b=8 as suggested by Lowe (2004).\".  "
                        + siteRef);

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
}
