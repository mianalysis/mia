package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import bunwarpj.Param;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.PointRoi;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;

import com.drew.lang.annotations.Nullable;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.Interactable.Interactable;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector;
import wbif.sjx.MIA.Process.Interactable.PointPairSelector.PointPair;
import wbif.sjx.MIA.ThirdParty.bUnwarpJ_Mod;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ManualUnwarp extends Module implements Interactable {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String REFERENCE_IMAGE = "Ref image";
    public static final String POINT_SELECTION_MODE = "Point selection mode";
    public static final String REGISTRATION_MODE = "Registration mode";
    public static final String SUBSAMPLE_FACTOR = "Subsample factor";
    public static final String INITIAL_DEFORMATION_MODE = "Initial deformation mode";
    public static final String FINAL_DEFORMATION_MODE = "Final deformation mode";
    public static final String DIVERGENCE_WEIGHT = "Divergence weight";
    public static final String CURL_WEIGHT = "Curl weight";
    public static final String LANDMARK_WEIGHT = "Landmark weight";
    public static final String IMAGE_WEIGHT = "Image weight";
    public static final String CONSISTENCY_WEIGHT = "Consistency weight";
    public static final String STOP_THRESHOLD = "Stop threshold";
    public static final String FILL_MODE = "Fill mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    private Param param;
    private Image inputImage;
    private Image reference;


    public interface PointSelectionModes {
        String PRESELECTED = "Pre-selected points";
        String RUNTIME = "Select at runtime";

        String[] ALL = new String[]{PRESELECTED,RUNTIME};

    }

    public interface RegistrationModes {
        final String FAST = "Fast";
        final String ACCURATE = "Accurate";
        final String MONO = "Mono";

        final String[] ALL = new String[]{FAST, ACCURATE, MONO};

    }

    public interface InitialDeformationModes {
        final String VERY_COARSE = "Very Coarse";
        final String COARSE = "Coarse";
        final String FINE = "Fine";
        final String VERY_FINE = "Very Fine";

        final String[] ALL = new String[]{VERY_COARSE, COARSE, FINE, VERY_FINE};

    }

    public interface FinalDeformationModes {
        final String VERY_COARSE = "Very Coarse";
        final String COARSE = "Coarse";
        final String FINE = "Fine";
        final String VERY_FINE = "Very Fine";
        final String SUPER_FINE = "Super Fine";

        final String[] ALL = new String[]{VERY_COARSE, COARSE, FINE, VERY_FINE, SUPER_FINE};

    }

    public interface FillModes {
        String BLACK = "Black";
        String WHITE = "White";

        String[] ALL = new String[]{BLACK,WHITE};

    }


    private int getRegistrationMode(String registrationMode) {
        switch (registrationMode) {
            case RegistrationModes.FAST:
            default:
                return 0;
            case RegistrationModes.ACCURATE:
                return 1;
            case RegistrationModes.MONO:
                return 2;
        }
    }

    private int getInitialDeformationMode(String initialDeformationMode) {
        switch (initialDeformationMode) {
            case InitialDeformationModes.VERY_COARSE:
            default:
                return 0;
            case InitialDeformationModes.COARSE:
                return 1;
            case InitialDeformationModes.FINE:
                return 2;
            case InitialDeformationModes.VERY_FINE:
                return 3;
        }
    }

    private int getFinalDeformationMode(String finalDeformationMode) {
        switch (finalDeformationMode) {
            case FinalDeformationModes.VERY_COARSE:
            default:
                return 0;
            case FinalDeformationModes.COARSE:
                return 1;
            case FinalDeformationModes.FINE:
                return 2;
            case FinalDeformationModes.VERY_FINE:
                return 3;
            case FinalDeformationModes.SUPER_FINE:
                return 4;
        }
    }

    public static Transformation getTransformation(Image referenceImage, Image warpedImage, Param param) {
        ImagePlus referenceIpl = referenceImage.getImagePlus();
        ImagePlus warpedIpl = warpedImage.getImagePlus();

        return bUnwarpJ_.computeTransformationBatch(referenceIpl, warpedIpl, null, null, param);

    }

    public void applyTransformation(Image inputImage, Image outputImage, Transformation transformation, String fillMode, boolean multithread) throws InterruptedException {
        final String tempPath;
        try {
            File tempFile = File.createTempFile("unwarp", ".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            bufferedWriter.close();

            tempPath = tempFile.getAbsolutePath();
            transformation.saveDirectTransformation(tempPath);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus outputIpl = outputImage.getImagePlus();

        // Invert the intensity before applying transformation if using white fill
        if (fillMode.equals(FillModes.WHITE)) InvertIntensity.process(inputIpl);

        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();
        int bitDepth = inputIpl.getBitDepth();
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        for (int c = 1; c <= nChannels; c++) {
            for (int z = 1; z <= nSlices; z++) {
                for (int t = 1; t <= nFrames; t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    Runnable task = () -> {
                        ImagePlus slice = getSetStack(inputIpl, finalT, finalC, finalZ, null);
                        bUnwarpJ_.applyTransformToSource(tempPath, outputImage.getImagePlus(), slice);
                        ImageTypeConverter.applyConversion(slice, inputIpl.getBitDepth(), ImageTypeConverter.ScalingModes.CLIP);

                        getSetStack(outputIpl, finalT, finalC, finalZ, slice.getProcessor());

                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        if (fillMode.equals(FillModes.WHITE)) InvertIntensity.process(outputIpl);

    }

    synchronized private static ImagePlus getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, int slice, @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice, timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel, slice, timepoint);
            inputImagePlus.setProcessor(toPut);
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
    }

    public Image createEmptyTarget(Image inputImage, Image referenceImage, String outputImageName) {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();
        int bitDepth = inputIpl.getBitDepth();
        int width = referenceImage.getImagePlus().getWidth();
        int height = referenceImage.getImagePlus().getHeight();

        // Creating output image
        return new Image(outputImageName,IJ.createHyperStack("Output", width, height, nChannels, nSlices, nFrames, bitDepth));

    }

    public Image process(Image inputImage, String outputImageName, Image reference, ArrayList<PointPair> pairs, Param param, String fillMode, boolean multithread) throws InterruptedException {
        // Converting point pairs into format for bUnwarpJ
        Stack<Point> points1 = new Stack<>();
        Stack<Point> points2 = new Stack<>();
        for (PointPair pair : pairs) {
            PointRoi pointRoi1 = pair.getPoint1();
            PointRoi pointRoi2 = pair.getPoint2();

            points1.push(new Point((int) pointRoi1.getXBase(), (int) pointRoi1.getYBase()));
            points2.push(new Point((int) pointRoi2.getXBase(), (int) pointRoi2.getYBase()));

        }

        ImageProcessor ipr1 = new Duplicator().run(inputImage.getImagePlus()).getProcessor();
        ImageProcessor ipr2 = new Duplicator().run(reference.getImagePlus()).getProcessor();

        Transformation transformation = bUnwarpJ_Mod.computeTransformationBatch(ipr1, ipr2, points1, points2, param);

        // Creating an output image
        Image outputImage = createEmptyTarget(inputImage,reference,outputImageName);

        // Applying transformation to entire stack
        // Applying the transformation to the whole stack.
        // All channels should move in the same way, so are processed with the same transformation.
        applyTransformation(inputImage, outputImage, transformation, fillMode, multithread);

        return outputImage;

    }


    @Override
    public void doAction(Object[] objects) {
        writeMessage("Running test unwarp");

        // Getting objects
        ImagePlus ipl1 = new Duplicator().run(inputImage.getImagePlus());
        ImagePlus ipl2 = new Duplicator().run(reference.getImagePlus());
        ArrayList<PointPair> pairs = (ArrayList<PointPair>) objects[0];

        // Converting point pairs into format for bUnwarpJ
        Stack<Point> points1 = new Stack<>();
        Stack<Point> points2 = new Stack<>();
        for (PointPair pair : pairs) {
            PointRoi pointRoi1 = pair.getPoint1();
            PointRoi pointRoi2 = pair.getPoint2();

            points1.push(new Point((int) pointRoi1.getXBase(), (int) pointRoi1.getYBase()));
            points2.push(new Point((int) pointRoi2.getXBase(), (int) pointRoi2.getYBase()));

        }

        ImageProcessor ipr1 = new Duplicator().run(ipl1).getProcessor();
        ImageProcessor ipr2 = new Duplicator().run(ipl2).getProcessor();

        Transformation transformation = bUnwarpJ_Mod.computeTransformationBatch(ipr1, ipr2, points1, points2, param);

        // Creating an output image
        Image outputImage = createEmptyTarget(inputImage,reference,"Test");

        String fillMode = parameters.getValue(FILL_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Applying transformation to entire stack
        // Applying the transformation to the whole stack.
        // All channels should move in the same way, so are processed with the same transformation.
        try {
            applyTransformation(inputImage, outputImage, transformation, fillMode, multithread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ConcatenateStacks concatenateStacks = new ConcatenateStacks();
        concatenateStacks.concatenateImages(new Image[]{reference,outputImage},ConcatenateStacks.AxisModes.CHANNEL,"Unwarp comparison").showImage();

    }

    @Override
    public String getTitle() {
        return "Unwarp images (manual)";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        IJ.setBackgroundColor(255,255,255);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String pointSelectionMode = parameters.getValue(POINT_SELECTION_MODE);
        String registrationMode = parameters.getValue(REGISTRATION_MODE);
        String fillMode = parameters.getValue(FILL_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        reference = workspace.getImage(referenceImageName);

        // Setting up the parameters
        param = new Param();
        param.mode = getRegistrationMode(registrationMode);
        param.img_subsamp_fact = parameters.getValue(SUBSAMPLE_FACTOR);
        param.min_scale_deformation = getInitialDeformationMode(parameters.getValue(INITIAL_DEFORMATION_MODE));
        param.max_scale_deformation = getFinalDeformationMode(parameters.getValue(FINAL_DEFORMATION_MODE));
        param.divWeight = parameters.getValue(DIVERGENCE_WEIGHT);
        param.curlWeight = parameters.getValue(CURL_WEIGHT);
        param.landmarkWeight = parameters.getValue(LANDMARK_WEIGHT);
        param.imageWeight = parameters.getValue(IMAGE_WEIGHT);
        if (registrationMode.equals(RegistrationModes.MONO)) {
            param.consistencyWeight = 10.0;
        } else {
            param.consistencyWeight = parameters.getValue(CONSISTENCY_WEIGHT);
        }
        param.stopThreshold = parameters.getValue(STOP_THRESHOLD);

        // Getting point pairs
        ImagePlus ipl1 = new Duplicator().run(inputImage.getImagePlus());
        ImagePlus ipl2 = new Duplicator().run(reference.getImagePlus());

        // Adding any ROIs that may have been pre-selected on the images
        ipl1.setRoi(inputImage.getImagePlus().getRoi());
        ipl2.setRoi(reference.getImagePlus().getRoi());

        ArrayList<PointPair> pairs = null;
        switch (pointSelectionMode) {
            case PointSelectionModes.PRESELECTED:
                pairs = PointPairSelector.getPreselectedPoints(inputImage,reference);
                break;
            case PointSelectionModes.RUNTIME:
            default:
                pairs = new PointPairSelector(this,true).getPointPairs(ipl1, ipl2);
                break;
        }

        Image outputImage = null;
        try {
            outputImage = process(inputImage, outputImageName, reference, pairs, param, fillMode, multithread);

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        // Failure to unwarp the image will result in a null
        if (outputImage == null) return false;

        // Dealing with module outputs
        workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new InputImageP(REFERENCE_IMAGE,this));
        parameters.add(new ChoiceP(POINT_SELECTION_MODE,this,PointSelectionModes.RUNTIME,PointSelectionModes.ALL));
        parameters.add(new ChoiceP(REGISTRATION_MODE,this,RegistrationModes.FAST,RegistrationModes.ALL));
        parameters.add(new IntegerP(SUBSAMPLE_FACTOR,this,0));
        parameters.add(new ChoiceP(INITIAL_DEFORMATION_MODE,this,InitialDeformationModes.VERY_COARSE,InitialDeformationModes.ALL));
        parameters.add(new ChoiceP(FINAL_DEFORMATION_MODE,this,FinalDeformationModes.FINE,FinalDeformationModes.ALL));
        parameters.add(new DoubleP(DIVERGENCE_WEIGHT,this,0d));
        parameters.add(new DoubleP(CURL_WEIGHT,this,0d));
        parameters.add(new DoubleP(LANDMARK_WEIGHT,this,0d));
        parameters.add(new DoubleP(IMAGE_WEIGHT,this,1d));
        parameters.add(new DoubleP(CONSISTENCY_WEIGHT,this,10d));
        parameters.add(new DoubleP(STOP_THRESHOLD,this,0.01));
        parameters.add(new ChoiceP(FILL_MODE,this,FillModes.BLACK,FillModes.ALL));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
        returnedParameters.add(parameters.getParameter(POINT_SELECTION_MODE));
        returnedParameters.add(parameters.getParameter(REGISTRATION_MODE));
        returnedParameters.add(parameters.getParameter(SUBSAMPLE_FACTOR));
        returnedParameters.add(parameters.getParameter(INITIAL_DEFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(FINAL_DEFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(DIVERGENCE_WEIGHT));
        returnedParameters.add(parameters.getParameter(CURL_WEIGHT));
        returnedParameters.add(parameters.getParameter(LANDMARK_WEIGHT));
        returnedParameters.add(parameters.getParameter(IMAGE_WEIGHT));

        switch ((String) parameters.getValue(REGISTRATION_MODE)) {
            case RegistrationModes.ACCURATE:
            case RegistrationModes.FAST:
                returnedParameters.add(parameters.getParameter(CONSISTENCY_WEIGHT));
                break;
        }

        returnedParameters.add(parameters.getParameter(FILL_MODE));
        returnedParameters.add(parameters.getParameter(STOP_THRESHOLD));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
