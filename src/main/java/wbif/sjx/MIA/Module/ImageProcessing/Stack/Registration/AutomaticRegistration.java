package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.ij.SIFT;
import mpicbg.ij.util.Util;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ManualUnwarp;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.UnwarpImages;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;

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

    public void processIndependent(Image inputImage, String regAxis, int calculationChannel, String relativeMode,
            Param param, int correctionInterval, String fillMode, boolean multithread, @Nullable Image reference,
            @Nullable Image externalSource) {
        // Creating a reference image
        Image currReference = null;

        // Assigning source image
        Image source = externalSource == null ? inputImage : externalSource;

        // Getting non-registration axis length
        int nReg = getRegistrationAxisLength(inputImage, regAxis);
        int nOther = getNonRegistrationAxisLength(inputImage, regAxis);
        
        int count = 0;
        int total = source.getImagePlus().getNFrames()*source.getImagePlus().getNSlices();

        // Iterating over all non-registration axis indices, applying the sorting
        for (int i = 0; i < nOther; i++) {
            // Assigning fixed reference images
            switch (relativeMode) {
                case RelativeModes.FIRST_FRAME:
                    currReference = getReferenceImageIndependent(source, calculationChannel, regAxis, i);
                    break;
                case RelativeModes.SPECIFIC_IMAGE:
                    currReference = reference;
                    break;
            }

            for (int j = 1; j < nReg; j++) {
                writeStatus("Processing frame " + (++count) + " of " + total);

                // If the reference image is the previous frame, calculate this now
                if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                    reference = ExtractSubstack.extractSubstack(source, "ExportableRef",
                            String.valueOf(calculationChannel), "1-end", String.valueOf(t - 1));
                    currReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                            ProjectImage.ProjectionModes.MAX);
                }
            
            }
        }

        // Iterate over each time-step        
        for (int t = 1; t <= source.getImagePlus().getNFrames(); t++) {
            // If the reference image is the previous frame, calculate this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                // Can't processAutomatic if this is the first frame
                if (t == 1)
                    continue;

                reference = ExtractSubstack.extractSubstack(source, "ExportableRef", String.valueOf(calculationChannel),
                        "1-end", String.valueOf(t - 1));
                currReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                        ProjectImage.ProjectionModes.MAX);
            }

            // Getting the projected image at this time-point
            Image warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(calculationChannel),
                    "1-end", String.valueOf(t));
            Image projectedWarped = ProjectImage.projectImageInZ(warped, "ProjectedWarped",
                    ProjectImage.ProjectionModes.MAX);

            // Calculating the transformation for this image pair
            if (currReference == null)
                return;

            AbstractAffineModel2D model = getAffineModel2D(currReference.getImagePlus().getProcessor(),
                    projectedWarped.getImagePlus().getProcessor(), param);
            InverseTransformMapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);

            int t2 = t;
            switch (relativeMode) {
                case UnwarpImages.RelativeModes.PREVIOUS_FRAME:
                    if (correctionInterval != -1 && t % correctionInterval == 0) {
                        t2 = source.getImagePlus().getNFrames();
                    }
                    break;
            }

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int tt = t; tt <= t2; tt++) {
                for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                    warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end",
                            String.valueOf(tt));
                    try {
                        applyTransformation(warped, currReference, mapping, fillMode, multithread);
                    } catch (InterruptedException e) {
                        return;
                    }
                    replaceStack(inputImage, warped, c, tt);

                }
            }

            // Need to apply the warp to an external image
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME) && externalSource != null) {
                for (int tt = t; tt <= t2; tt++) {
                    for (int c = 1; c <= source.getImagePlus().getNChannels(); c++) {
                        warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(c), "1-end",
                                String.valueOf(tt));
                        try {
                            applyTransformation(warped, currReference, mapping, fillMode, multithread);
                        } catch (InterruptedException e) {
                            return;
                        }
                        replaceStack(source, warped, c, tt);
                    }
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
        return "Uses SIFT image registration toolbox";
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

        Image inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());

        Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName)
                : null;

        // If the rolling correction mode is off, set the interval to -1
        if (rollingCorrectionMode.equals(RollingCorrectionModes.NONE))
            correctionInterval = -1;

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

        // Getting external source image
        Image externalSource = calculationSource.equals(CalculationSources.EXTERNAL)
                ? new Image(externalSourceName, workspace.getImage(externalSourceName).getImagePlus().duplicate())
                : null;

        processIndependent(inputImage, regAxis, calculationChannel, relativeMode, param, correctionInterval, fillMode,
                multithread, reference, externalSource);

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
}
