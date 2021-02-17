package wbif.sjx.MIA.Module.Deprecated;

import java.awt.geom.AffineTransform;
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
import ij.plugin.Duplicator;
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
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ManualUnwarp;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.UnwarpImages;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
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
import wbif.sjx.MIA.Process.Interactable.Interactable;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;

public class RegisterImages<T extends RealType<T> & NativeType<T>> extends Module implements Interactable {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String ALIGNMENT_MODE = "Alignment mode";
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

    private Image inputImage;
    private Image reference;

    public RegisterImages(ModuleCollection modules) {
        super("Register images", modules);
    }

    public interface AlignmentModes {
        final String AUTOMATIC = "Automatic (feature extraction)";
        final String MANUAL = "Manual (landmarks)";

        final String[] ALL = new String[] { AUTOMATIC, MANUAL };

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

    public interface Measurements {
        String TRANSLATE_X = "REGISTER // TRANSLATE_X";
        String TRANSLATE_Y = "REGISTER // TRANSLATE_Y";
        String SCALE_X = "REGISTER // SCALE_X";
        String SCALE_Y = "REGISTER // SCALE_Y";
        String SHEAR_X = "REGISTER // SHEAR_X";
        String SHEAR_Y = "REGISTER // SHEAR_Y";

    }

    public void processAutomatic(Image inputImage, int calculationChannel, String relativeMode, Param param,
            int correctionInterval, String fillMode, boolean multithread, @Nullable Image reference,
            @Nullable Image externalSource) {
        // Creating a reference image
        Image projectedReference = null;

        // Assigning source image
        Image source = externalSource == null ? inputImage : externalSource;

        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(source, "ExportableRef", String.valueOf(calculationChannel),
                        "1-end", "1");
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                        ProjectImage.ProjectionModes.MAX);
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null)
                    return;
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                        ProjectImage.ProjectionModes.MAX);
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int total = source.getImagePlus().getNFrames();
        for (int t = 1; t <= source.getImagePlus().getNFrames(); t++) {
            writeStatus("Processing timepoint " + (++count) + " of " + total);

            // If the reference image is the previous frame, calculate this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                // Can't processAutomatic if this is the first frame
                if (t == 1)
                    continue;

                reference = ExtractSubstack.extractSubstack(source, "ExportableRef", String.valueOf(calculationChannel),
                        "1-end", String.valueOf(t - 1));
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                        ProjectImage.ProjectionModes.MAX);
            }

            // Getting the projected image at this time-point
            Image warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(calculationChannel),
                    "1-end", String.valueOf(t));
            Image projectedWarped = ProjectImage.projectImageInZ(warped, "ProjectedWarped",
                    ProjectImage.ProjectionModes.MAX);

            // Calculating the transformation for this image pair
            if (projectedReference == null)
                return;

            AbstractAffineModel2D model = getAffineModel2D(projectedReference.getImagePlus().getProcessor(), projectedWarped.getImagePlus().getProcessor(), param);
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
                        applyTransformation(warped, projectedReference, mapping, fillMode, multithread);
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
                            applyTransformation(warped, projectedReference, mapping, fillMode, multithread);
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

    public void processManual(Image inputImage, String transformationMode, boolean multithread, String fillMode,
            Image reference) {
        // Creating a reference image
        Image projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                ProjectImage.ProjectionModes.MAX);

        // Creating a projection of the main image
        Image projectedWarped = ProjectImage.projectImageInZ(inputImage, "ProjectedWarped",
                ProjectImage.ProjectionModes.MAX);

        ImagePlus ipl1 = new Duplicator().run(projectedWarped.getImagePlus());
        ImagePlus ipl2 = new Duplicator().run(projectedReference.getImagePlus());
        ArrayList<PointPair> pairs = new PointPairSelector(this, true).getPointPairs(ipl1, ipl2);

        // Getting transform
        Object[] output = getLandmarkTransformation(pairs, transformationMode);
        InverseTransformMapping mapping = (InverseTransformMapping) output[0];
        AbstractAffineModel2D model = (AbstractAffineModel2D) output[1];

        // Iterate over each time-step
        int count = 0;
        int total = inputImage.getImagePlus().getNFrames();
        for (int t = 1; t <= inputImage.getImagePlus().getNFrames(); t++) {
            writeStatus("Processing timepoint " + (++count) + " of " + total);

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                Image warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end",
                        String.valueOf(t));
                try {
                    applyTransformation(warped, projectedReference, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(inputImage, warped, c, t);
            }

            mapping = null;

        }

        addManualMeasurements(inputImage, model);

    }

    private static AbstractAffineModel2D getModel(String transformationMode) {
        switch (transformationMode) {
            case TransformationModes.AFFINE:
                return new AffineModel2D();
            case TransformationModes.RIGID:
            default:
                return new RigidModel2D();
            case TransformationModes.SIMILARITY:
                return new SimilarityModel2D();
            case TransformationModes.TRANSLATION:
                return new TranslationModel2D();
        }
    }

    public static AbstractAffineModel2D getAffineModel2D(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param) {
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

    public static Object[] getLandmarkTransformation(List<PointPair> pairs, String transformationMode) {
        // Getting registration model
        AbstractAffineModel2D model = getModel(transformationMode);
        InverseTransformMapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
        final ArrayList<PointMatch> candidates = new ArrayList<PointMatch>();

        for (PointPair pair : pairs) {
            candidates.addAll(Util.pointRoisToPointMatches(pair.getPoint1(), pair.getPoint2()));
        }

        try {
            model.fit(candidates);
        } catch (NotEnoughDataPointsException | IllDefinedDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        return new Object[] { mapping, model };

    }

    public static void applyTransformation(Image inputImage, Image referenceImage, Mapping mapping, String fillMode,
            boolean multithread) throws InterruptedException {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        // Getting reference ImageProcessor. The output image will be the same size as
        // this.
        ImageProcessor referenceIpr = referenceImage.getImagePlus().getProcessor();

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        if (fillMode.equals(ManualUnwarp.FillModes.WHITE))
            InvertIntensity.process(inputImage);

        for (int c = 1; c <= nChannels; c++) {
            for (int z = 1; z <= nSlices; z++) {
                for (int t = 1; t <= nFrames; t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    Runnable task = () -> {
                        ImageProcessor slice = getSetStack(inputIpl, finalT, finalC, finalZ, null).getProcessor();
                        slice.setInterpolationMethod(ImageProcessor.BILINEAR);
                        ImageProcessor alignedSlice = slice.createProcessor(referenceIpr.getWidth(),
                                referenceIpr.getHeight());
                        alignedSlice.setMinAndMax(slice.getMin(), slice.getMax());
                        mapping.mapInterpolated(slice, alignedSlice);

                        getSetStack(inputIpl, finalT, finalC, finalZ, alignedSlice);

                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        if (fillMode.equals(ManualUnwarp.FillModes.WHITE))
            InvertIntensity.process(inputImage);

    }

    synchronized private static ImagePlus getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, int slice,
            @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice,
                    timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel, slice, timepoint);
            inputImagePlus.setProcessor(toPut);
            inputImagePlus.updateAndDraw();
            return null;
        }        
    }

    public static void replaceStack(Image inputImage, Image newStack, int channel, int timepoint) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ImagePlus newStackImagePlus = newStack.getImagePlus();

        for (int z = 1; z <= newStackImagePlus.getNSlices(); z++) {
            inputImagePlus.setPosition(channel, z, timepoint);
            newStackImagePlus.setPosition(1, z, 1);

            inputImagePlus.setProcessor(newStackImagePlus.getProcessor());

        }

        inputImagePlus.updateAndDraw();
        
    }

    static void addManualMeasurements(Image image, AbstractAffineModel2D model) {
        AffineTransform transform = model.createAffine();

        image.addMeasurement(new Measurement(Measurements.TRANSLATE_X, transform.getTranslateX()));
        image.addMeasurement(new Measurement(Measurements.TRANSLATE_Y, transform.getTranslateY()));
        image.addMeasurement(new Measurement(Measurements.SCALE_X, transform.getScaleX()));
        image.addMeasurement(new Measurement(Measurements.SCALE_Y, transform.getScaleY()));
        image.addMeasurement(new Measurement(Measurements.SHEAR_X, transform.getShearX()));
        image.addMeasurement(new Measurement(Measurements.SHEAR_Y, transform.getShearY()));

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
    public void doAction(Object[] objects) {
        writeStatus("Running test registration");

        String transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        ArrayList<PointPair> pairs = (ArrayList<PointPair>) objects[0];

        // Creating a reference image
        Image projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference",
                ProjectImage.ProjectionModes.MAX);

        // Duplicating image
        ImagePlus dupIpl = inputImage.getImagePlus().duplicate();
        Image<T> dupImage = new Image<T>("Registered", dupIpl);

        // Getting transform
        Object[] output = getLandmarkTransformation(pairs, transformationMode);
        InverseTransformMapping mapping = (InverseTransformMapping) output[0];
        AbstractAffineModel2D model = (AbstractAffineModel2D) output[1];

        // Iterate over each time-step
        int count = 0;
        int total = dupImage.getImagePlus().getNFrames();
        for (int t = 1; t <= dupImage.getImagePlus().getNFrames(); t++) {
            writeStatus("Processing timepoint " + (++count) + " of " + total);

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int c = 1; c <= dupImage.getImagePlus().getNChannels(); c++) {
                Image warped = ExtractSubstack.extractSubstack(dupImage, "Warped", String.valueOf(c), "1-end",
                        String.valueOf(t));
                try {
                    applyTransformation(warped, projectedReference, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(dupImage, warped, c, t);
            }

            mapping = null;

        }

        ArrayList<Image<T>> images = new ArrayList<>();
        images.add(reference);
        images.add(dupImage);
        ConcatenateStacks.concatenateImages(images, ConcatenateStacks.AxisModes.CHANNEL, "Registration comparison")
                .showImage();

    }


    @Override
    public Category getCategory() {
        return Categories.DEPRECATED;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: Please use separate automatic and manual modules instead."

        + "<br><br>Apply slice-by-slice (2D) affine-based image registration to a multi-dimensional stack.  Images can be aligned relative to the first frame in the stack, the previous frame or a separate image in the workspace.  The registration transform can also be calculated from a separate stack to the one that it will be applied to.  Registration is performed along the time axes and applied equally to all Z-slices.  For greater control (including registration along Z) please use separate automatic and manual modules instead."

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
        String alignmentMode = parameters.getValue(ALIGNMENT_MODE);
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

        inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());

        switch (alignmentMode) {
            case AlignmentModes.AUTOMATIC:
                reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName)
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
                        ? new Image(externalSourceName,
                                workspace.getImage(externalSourceName).getImagePlus().duplicate())
                        : null;

                processAutomatic(inputImage, calculationChannel, relativeMode, param, correctionInterval, fillMode,
                        multithread, reference, externalSource);

                if (showOutput) {
                    if (relativeMode.equals(RelativeModes.SPECIFIC_IMAGE)) {
                        createOverlay(inputImage, reference).showImage();
                    } else {
                        inputImage.showImage();
                    }
                }

                break;

            case AlignmentModes.MANUAL:
                reference = workspace.getImage(referenceImageName);
                processManual(inputImage, transformationMode, multithread, fillMode, reference);

                if (showOutput)
                    inputImage.showImage();

                break;
        }

        // Dealing with module outputs
        if (!applyToInput)
            workspace.addImage(inputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REGISTRATION_SEPARATOR, this));
        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));
        parameters.add(new ChoiceP(ALIGNMENT_MODE, this, AlignmentModes.AUTOMATIC, AlignmentModes.ALL));
        parameters.add(new ChoiceP(FILL_MODE, this, FillModes.BLACK, FillModes.ALL));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

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

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(REGISTRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(ALIGNMENT_MODE));
        returnedParameters.add(parameters.getParameter(FILL_MODE));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        switch ((String) parameters.getValue(ALIGNMENT_MODE)) {
            case AlignmentModes.AUTOMATIC:
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
                break;

            case AlignmentModes.MANUAL:
                returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        if (parameters.getValue(ALIGNMENT_MODE).equals(AlignmentModes.MANUAL)) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);

            returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.TRANSLATE_X).setImageName(outputImageName));
            returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.TRANSLATE_Y).setImageName(outputImageName));
            returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SCALE_X).setImageName(outputImageName));
            returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SCALE_Y).setImageName(outputImageName));
            returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SHEAR_X).setImageName(outputImageName));
            returnedRefs.add(imageMeasurementRefs.getOrPut(Measurements.SHEAR_Y).setImageName(outputImageName));

        }

        return returnedRefs;

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

        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply registration to.");

        parameters.get(APPLY_TO_INPUT).setDescription("When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
        + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(TRANSFORMATION_MODE).setDescription("Controls the type of registration being applied:<br><ul>"

                + "<li>\"" + TransformationModes.AFFINE + "\" Applies the full affine transformation, whereby the input image can undergo translation, rotation, reflection, scaling and shear.</li>"

                + "<li>\"" + TransformationModes.RIGID + "\" Applies only translation and rotation to the input image.  As such, all features should remain the same size.</li>"

                + "<li>\"" + TransformationModes.SIMILARITY +"\" Applies translation, rotating and linear scaling to the input image.</li>"

                +"<li>\""+TransformationModes.TRANSLATION+"\" Applies only translation (motion within the 2D plane) to the input image.</li></ul>");

        parameters.get(ALIGNMENT_MODE).setDescription("Controls whether the registration is determined automatically through SIFT feature extraction or manually, by a user selecting reference points on a pair of images.");

        parameters.get(FILL_MODE).setDescription("Controls what intensity any border pixels will have.  \"Borders\" in this case correspond to strips/wedges at the image edge corresponding to regions outside the initial image (e.g. the right-side of an output image when the input was translated to the left).   Choices are: "+String.join(", ",FillModes.ALL)+".");

        parameters.get(ENABLE_MULTITHREADING).setDescription("When selected, certain parts of the registration process will be run on multiple threads of the CPU.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

        parameters.get(RELATIVE_MODE).setDescription("Controls what reference image each image will be compared to:<br><ul>"

        +"<li>\""+RelativeModes.FIRST_FRAME+"\" All images will be compared to the first frame (or slice when in Z-axis mode).  For image sequences which continuously evolve over time (e.g. cells dividing) this can lead to reduced likelihood of successfully calculating the transform over time.</li>"

        +"<li>\""+RelativeModes.PREVIOUS_FRAME+"\" Each image will be compared to the frame (or slice when in Z-axis mode) immediately before it.  This copes better with image sequences which continuously evolve over time, but can also lead to compounding errors over time (errors in registration get propagated to all remaining slices).</li>"

        +"<li>\""+RelativeModes.SPECIFIC_IMAGE+"\" All images will be compared to a separate 2D image from the workspace.  The image to compare to is selected using the \""+REFERENCE_IMAGE+"\" parameter.</li></ul>");

        parameters.get(ROLLING_CORRECTION).setDescription("Controls whether the entire stack is moved at specific intervals.  When enabled (\""+RollingCorrectionModes.EVERY_NTH_FRAME+"\"), any remaining unregistered images will be moved to match the present transform.  This is only available when registering relative to the previous frame and is intended to prevent the difference between the previous frame (registered) and unregistered images becoming too large.  The frame interval at which this transformation is applied to the unregistered images is specified using \""+CORRECTION_INTERVAL+"\".  Note: This can lead to images becoming increasingly blurry as they are passed through multiple interpolation steps.");

        parameters.get(CORRECTION_INTERVAL).setDescription("If applying rolling correction, this is the frame interval at which the transformation will be applied to the unregistered images.");

        parameters.get(REFERENCE_IMAGE).setDescription("If \""+RELATIVE_MODE+"\" is set to \""+RelativeModes.SPECIFIC_IMAGE+"\" mode, all input images will be registered relative to this image.  This image must only have a single channel, slice and timepoint.");

        parameters.get(CALCULATION_SOURCE).setDescription("Controls whether the input image will be used to calculate the registration transform or whether it will be determined from a separate image:<br><ul>"

        +"<li>\""+CalculationSources.EXTERNAL+"\" The transform is calculated from a separate image from the workspace (specified using \""+EXTERNAL_SOURCE+"\").  This could be an image with enhanced contrast (to enable better feature extraction), but where the enhancements are not desired in the output registered image.  The external image must be the same length along the registration axis.  The non-registration axis will have a maximum intensity projection applied prior to calculation of transform.</li>"

        +"<li>\""+CalculationSources.INTERNAL+"\" The transform is calculated from the input image.</li></ul>");

        parameters.get(EXTERNAL_SOURCE).setDescription("If \""+CALCULATION_SOURCE+"\" is set to \""+CalculationSources.EXTERNAL+"\", registration transforms will be calculated using this image from the workspace.  This image will be unaffected by the process.");

        parameters.get(CALCULATION_CHANNEL).setDescription("If calculating the registration transform from a multi-channel image stack, the transform will be determined from this channel only.  Irrespectively, for multi-channel image stacks, the calculated transform will be applied equally to all channels.");

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

    public static class Param extends FloatArray2DSIFT.Param {
        /**
         *
         */
        private static final long serialVersionUID = -9039231442503621671L;
        String transformationMode = TransformationModes.RIGID;
        float rod = 0.92f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;

    }
}
