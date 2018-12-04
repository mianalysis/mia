package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import bunwarpj.bUnwarpJ_;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import mpicbg.ij.FeatureTransform;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegisterImages extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String ROLLING_CORRECTION = "Rolling correction";
    public static final String CORRECTION_INTERVAL = "Correction interval";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String INITIAL_SIGMA = "Initial Gaussian blur (px)";
    public static final String STEPS = "Steps per scale";
    public static final String MINIMUM_IMAGE_SIZE = "Minimum image size (px)";
    public static final String MAXIMUM_IMAGE_SIZE = "Maximum image size (px)";
    public static final String FD_SIZE = "Feature descriptor size";
    public static final String FD_ORIENTATION_BINS = "Feature descriptor orientation bins";
    public static final String ROD = "Closest/next closest ratio";
    public static final String MAX_EPSILON = "Maximal alignment error (px)";
    public static final String MIN_INLIER_RATIO = "Inlier ratio";


    public interface RelativeModes {
        final String FIRST_FRAME = "First frame";
        final String PREVIOUS_FRAME = "Previous frame";
        final String SPECIFIC_IMAGE = "Specific image";

        final String[] ALL = new String[]{FIRST_FRAME,PREVIOUS_FRAME,SPECIFIC_IMAGE};

    }

    public interface RollingCorrectionModes {
        final String NONE = "None";
        final String EVERY_NTH_FRAME = "Every nth frame";

        final String[] ALL = new String[]{NONE,EVERY_NTH_FRAME};

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[]{INTERNAL,EXTERNAL};

    }

    public interface TransformationModes {
        String AFFINE = "Affine";
        String RIGID = "Rigid";
        String SIMILARITY = "Similarity";
        String TRANSLATION = "Translation";

        String[] ALL = new String[]{AFFINE,RIGID,SIMILARITY,TRANSLATION};

    }


    public void process(Image inputImage, int calculationChannel, String relativeMode, Param param, int correctionInterval, @Nullable Image reference, @Nullable Image externalSource) {
        // Creating a reference image
        Image projectedReference = null;

        // Assigning source image
        Image source = externalSource == null ? inputImage : externalSource;

        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(source, "Reference", String.valueOf(calculationChannel), "1-end", "1");
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null) return;
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int total = source.getImagePlus().getNFrames();
        for (int t = 1; t <= source.getImagePlus().getNFrames(); t++) {
            writeMessage("Processing timepoint "+(++count)+" of "+total);

            // If the reference image is the previous frame, calculate this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                // Can't process if this is the first frame
                if (t == 1) continue;

                reference = ExtractSubstack.extractSubstack(source, "Reference", String.valueOf(calculationChannel), "1-end", String.valueOf(t - 1));
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
            }

            // Getting the projected image at this time-point
            Image warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(calculationChannel), "1-end", String.valueOf(t));
            Image projectedWarped = ProjectImage.projectImageInZ(warped, "ProjectedWarped", ProjectImage.ProjectionModes.MAX);

            // Calculating the transformation for this image pair
            if (projectedReference == null) return;

//            // If we're using the previous frame we may need to crop out the boundaries
//            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
//                // Getting largest non-black rectangle
//                Rectangle rectangle = getLargestLimits(projectedReference);
//
//                // Cropping references to this rectangle
//                int top = rectangle.y;
//                int left = rectangle.x;
//                int width = rectangle.width;
//                int height = rectangle.height;
//
//                projectedReference = CropImage.cropImage(projectedReference, "ProjectedReference", top, left, width, height);
//                projectedWarped = CropImage.cropImage(projectedWarped, "ProjectedWarped", top, left, width, height);
//
//            }

            Mapping mapping = getTransformation(projectedReference,projectedWarped,param);

            int t2 = t;
            switch (relativeMode) {
                case UnwarpImages.RelativeModes.PREVIOUS_FRAME:
                    if (correctionInterval != -1 && t%correctionInterval == 0) {
                        t2 = source.getImagePlus().getNFrames();
                    }
                    break;
            }

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same transformation.
            for (int tt = t; tt <= t2; tt++) {
                for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                    warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end", String.valueOf(tt));
                    applyTransformation(warped, mapping);
                    replaceStack(inputImage, warped, c, tt);
                }
            }

            // Need to apply the warp to an external image
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME) && externalSource != null) {
                for (int tt = t; tt <= t2; tt++) {
                    for (int c = 1; c <= source.getImagePlus().getNChannels(); c++) {
                        warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(c), "1-end", String.valueOf(tt));
                        applyTransformation(warped, mapping);
                        replaceStack(source, warped, c, tt);
                    }
                }
            }

            mapping = null;
            System.gc();
        }
    }

//    public static Rectangle getLargestLimits(Image image) {
//        ImageProcessor ipr = image.getImagePlus().getProcessor();
//
//        int top = 0;
//        int left = 0;
//        int width = 0;
//        int height = 0;
//        int imWidth = ipr.getWidth();
//        int imHeight = ipr.getHeight();
//        int largestArea = 0;
//
//        for (int x=0;x<imWidth;x++) {
//            for (int y=0;y<imHeight;y++) {
//                for (int xx=(imWidth-1);xx>x;xx--) {
//                    for (int yy=(imHeight-1);yy>y;yy--) {
//                        // If this rectangle has a smaller area than the previous best, skip the rest of this loop
//                        int area = (xx-x)*(yy-y);
//                        if (area <= largestArea) break;
//
//                        // Checking that the corners are non-zero
//                        if (ipr.get(x,y) == 0) continue;
//                        if (ipr.get(x,yy) == 0) continue;
//                        if (ipr.get(xx,y) == 0) continue;
//                        if (ipr.get(xx,yy) == 0) continue;
//
//                        // If we got here, we have the largest current rectangle
//                        top = y;
//                        left = x;
//                        width = (xx-x);
//                        height = (yy-y);
//                        largestArea = area;
//                        break;
//
//                    }
//                }
//            }
//        }
//
//        return new Rectangle(left,top,width,height);
//
//    }

    public static Mapping getTransformation(Image referenceImage, Image warpedImage, Param param) {
        ImagePlus referenceIpl = referenceImage.getImagePlus();
        ImagePlus warpedIpl = warpedImage.getImagePlus();

        // Initialising SIFT feature extractor
        FloatArray2DSIFT sift = new FloatArray2DSIFT(param);
        SIFT ijSIFT = new SIFT(sift);

        // Extracting features
        ArrayList<Feature> featureList1 = new ArrayList<Feature>();
        ijSIFT.extractFeatures(referenceIpl.getProcessor(),featureList1);
        ArrayList<Feature> featureList2 = new ArrayList<Feature>();
        ijSIFT.extractFeatures(warpedIpl.getProcessor(),featureList2);

        // Running registration
        AbstractAffineModel2D model;
        AbstractAffineModel2D< ? > currentModel;
        switch (param.transformationMode) {
            case TransformationModes.AFFINE:
                model = new AffineModel2D();
                currentModel = new AffineModel2D();
                break;
            case TransformationModes.RIGID:
            default:
                model = new RigidModel2D();
                currentModel = new RigidModel2D();
                break;
            case TransformationModes.SIMILARITY:
                model = new SimilarityModel2D();
                currentModel = new SimilarityModel2D();
                break;
            case TransformationModes.TRANSLATION:
                model = new TranslationModel2D();
                currentModel = new TranslationModel2D();
                break;
        }

        Mapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
        Vector<PointMatch> candidates = FloatArray2DSIFT.createMatches(featureList2,featureList1,1.5f,null, Float.MAX_VALUE,param.rod);
        Vector<PointMatch> inliers = new Vector<PointMatch>();

        try {
            currentModel.filterRansac(candidates,inliers,1000,param.maxEpsilon,param.minInlierRatio);
        } catch (NotEnoughDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        model.concatenate(currentModel);

        return mapping;

    }

    public static void applyTransformation(Image inputImage, Mapping mapping) {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Setting up the ExecutorService, which will manage the threads
        int nThreads = Prefs.getThreads();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        for (int c=1;c<=inputIpl.getNChannels();c++) {
            for (int z=1;z<=inputIpl.getNSlices();z++) {
                for (int t=1;t<=inputIpl.getNFrames();t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;
                    Runnable task = () -> {
                        ImageProcessor originalSlice = getOrReplaceSlice(inputIpl,null,finalC,finalZ,finalT,false);
                        if (originalSlice == null) return;

                        originalSlice.setInterpolationMethod(ImageProcessor.BILINEAR);
                        ImageProcessor alignedSlice = originalSlice.createProcessor(originalSlice.getWidth(), originalSlice.getHeight());
                        alignedSlice.setMinAndMax(originalSlice.getMin(), originalSlice.getMax());
                        mapping.mapInterpolated(originalSlice, alignedSlice);

                        getOrReplaceSlice(inputIpl,alignedSlice,finalC,finalZ,finalT,true);

                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }

        for (int c=1;c<=inputIpl.getNChannels();c++) {
            for (int z=1;z<=inputIpl.getNSlices();z++) {
                for (int t=1;t<=inputIpl.getNFrames();t++) {
                    inputIpl.setPosition(c, z, t);



                }
            }
        }
    }

    private static synchronized ImageProcessor getOrReplaceSlice(ImagePlus inputIpl, @Nullable ImageProcessor slice, int c, int z, int t, boolean add) {
        inputIpl.setPosition(c, z, t);
        if (add) {
            inputIpl.setProcessor(slice);
            return null;
        } else {
            return inputIpl.getProcessor();
        }
    }

    public static void replaceStack(Image inputImage, Image newStack, int channel, int timepoint) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ImagePlus newStackImagePlus = newStack.getImagePlus();

        for (int z=1;z<=newStackImagePlus.getNSlices();z++) {
            inputImagePlus.setPosition(channel,z,timepoint);
            newStackImagePlus.setPosition(1,z,1);

            inputImagePlus.setProcessor(newStackImagePlus.getProcessor());

        }
    }

    @Override
    public String getTitle() {
        return "Register images";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "Uses SIFT image registration toolbox";
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String relativeMode = parameters.getValue(RELATIVE_MODE);
        String rollingCorrectionMode = parameters.getValue(ROLLING_CORRECTION);
        int correctionInterval = parameters.getValue(CORRECTION_INTERVAL);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        double initialSigma = parameters.getValue(INITIAL_SIGMA);
        double rod = parameters.getValue(ROD);
        double maxEpsilon = parameters.getValue(MAX_EPSILON);
        double minInlierRatio = parameters.getValue(MIN_INLIER_RATIO);

        if (!applyToInput) inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());

        // If the rolling correction mode is off, set the interval to -1
        if (rollingCorrectionMode.equals(RollingCorrectionModes.NONE)) correctionInterval = -1;

        // Setting up the parameters

        Param param = new Param();
        param.transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        param.initialSigma = (float) initialSigma;
        param.steps = parameters.getValue(STEPS);
        param.minOctaveSize = parameters.getValue(MINIMUM_IMAGE_SIZE);
        param.maxOctaveSize = parameters.getValue(MAXIMUM_IMAGE_SIZE);
        param.fdSize = parameters.getValue(FD_SIZE);
        param.fdBins = parameters.getValue(FD_ORIENTATION_BINS);
        param.rod = (float) rod;
        param.maxEpsilon = (float) maxEpsilon;
        param.minInlierRatio = (float) minInlierRatio;

        Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName) : null;
        Image externalSource = calculationSource.equals(CalculationSources.EXTERNAL) ? workspace.getImage(externalSourceName) : null;
        process(inputImage,calculationChannel,relativeMode,param,correctionInterval,reference,externalSource);

        // Dealing with module outputs
        if (!applyToInput) workspace.addImage(inputImage);
        if (showOutput) showImage(inputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(RELATIVE_MODE,Parameter.CHOICE_ARRAY,RelativeModes.FIRST_FRAME,RelativeModes.ALL));
        parameters.add(new Parameter(ROLLING_CORRECTION,Parameter.CHOICE_ARRAY,RollingCorrectionModes.NONE,RollingCorrectionModes.ALL));
        parameters.add(new Parameter(CORRECTION_INTERVAL, Parameter.INTEGER,1));
        parameters.add(new Parameter(REFERENCE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(CALCULATION_SOURCE,Parameter.CHOICE_ARRAY,CalculationSources.INTERNAL,CalculationSources.ALL));
        parameters.add(new Parameter(EXTERNAL_SOURCE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(CALCULATION_CHANNEL, Parameter.INTEGER,1));
        parameters.add(new Parameter(TRANSFORMATION_MODE,Parameter.CHOICE_ARRAY,TransformationModes.RIGID,TransformationModes.ALL));
        parameters.add(new Parameter(INITIAL_SIGMA, Parameter.DOUBLE,1.6));
        parameters.add(new Parameter(STEPS, Parameter.INTEGER,3));
        parameters.add(new Parameter(MINIMUM_IMAGE_SIZE, Parameter.INTEGER,64));
        parameters.add(new Parameter(MAXIMUM_IMAGE_SIZE, Parameter.INTEGER,1024));
        parameters.add(new Parameter(FD_SIZE, Parameter.INTEGER,4));
        parameters.add(new Parameter(FD_ORIENTATION_BINS, Parameter.INTEGER,8));
        parameters.add(new Parameter(ROD, Parameter.DOUBLE,0.92));
        parameters.add(new Parameter(MAX_EPSILON, Parameter.DOUBLE,25.0));
        parameters.add(new Parameter(MIN_INLIER_RATIO, Parameter.DOUBLE,0.05));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

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
        returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));
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
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }


    private class Param extends FloatArray2DSIFT.Param {
        String transformationMode = TransformationModes.RIGID;
        float rod = 0.92f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;

    }

}
