package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.Scaler;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.InterpolateZAxis;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddLabels;
import wbif.sjx.MIA.Module.Visualisation.Overlays.AddObjectOutline;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Object.Units.TemporalUnit;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.MIA.Process.LabelFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.MathFunc.VoxelSphere;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.VolumeType;
import wbif.sjx.common.Process.IntensityMinMax;
import wbif.sjx.common.Process.HoughTransform.Transforms.SphereHoughTransform;

/**
 * Created by sc13967 on 15/01/2018.
 */
public class HoughSphereDetection extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String OUTPUT_TRANSFORM_IMAGE = "Output transform image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String DETECTION_SEPARATOR = "Hough-based circle detection";
    public static final String MIN_RADIUS = "Minimum radius (px)";
    public static final String MAX_RADIUS = "Maximum radius (px)";
    public static final String DETECTION_THRESHOLD = "Detection threshold";
    public static final String EXCLUSION_RADIUS = "Exclusion radius (px)";
    public static final String DOWNSAMPLE_FACTOR = "Downsample factor";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public static final String POST_PROCESSING_SEPARATOR = "Object post processing";
    public static final String RADIUS_RESIZE = "Output radius resize (px)";

    public static final String VISUALISATION_SEPARATOR = "Visualisation controls";
    public static final String SHOW_TRANSFORM_IMAGE = "Show transform image";
    public static final String SHOW_DETECTION_IMAGE = "Show detection image";
    public static final String SHOW_HOUGH_SCORE = "Show detection score";
    public static final String LABEL_SIZE = "Label size";

    public HoughSphereDetection(ModuleCollection modules) {
        super("Sphere detection", modules);
    }

    private interface Measurements {
        String SCORE = "HOUGH_DETECTION//SCORE";

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Detects spheres within grayscale images using the Hough transform.  Input images can be of binary or grayscale format, but the circle features must be brighter than their surrounding background and have dark centres (i.e. be shells).  For solid spheres, a gradient filter or equivalent should be applied to the image first.  Detected spheres are output to the workspace as solid objects.  Spheres are detected within a user-defined radius range and must exceed a user-defined threshold score (based on the intensity of the spherical feartures in the input image and the feature sphericity.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean outputTransformImage = parameters.getValue(OUTPUT_TRANSFORM_IMAGE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting parameters
        int minR = parameters.getValue(MIN_RADIUS);
        int maxR = parameters.getValue(MAX_RADIUS);
        int samplingRate = parameters.getValue(DOWNSAMPLE_FACTOR);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS);
        int radiusResize = parameters.getValue(RADIUS_RESIZE);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE);
        boolean showDetectionImage = parameters.getValue(SHOW_DETECTION_IMAGE);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE);
        int labelSize = parameters.getValue(LABEL_SIZE);

        // Storing the image calibration
        SpatCal cal = SpatCal.getFromImage(ipl);
        int nFrames = ipl.getNFrames();
        double frameInterval = ipl.getCalibration().frameInterval;
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, cal, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

        int nThreads = multithread ? Prefs.getThreads() : 1;

        minR = (int) ((double) minR / (double) samplingRate);
        maxR = (int) ((double) maxR / (double) samplingRate);

        // Iterating over all images in the ImagePlus
        int count = 1;
        int total = ipl.getNChannels() * ipl.getNFrames();

        for (int c = 0; c < ipl.getNChannels(); c++) {
            for (int t = 0; t < ipl.getNFrames(); t++) {
                // Getting current image stack
                Image substack = ExtractSubstack.extractSubstack(inputImage, "Substack", String.valueOf(c + 1), "1-end",
                        String.valueOf(t + 1));
                ImagePlus substackIpl = substack.getImagePlus();

                // Interpolating Z axis, so the image is equal in all dimensions
                substackIpl = InterpolateZAxis.matchZToXY(substackIpl);

                // Applying downsample
                if (samplingRate != 1) {
                    int rescaleW = substackIpl.getWidth() / samplingRate;
                    int rescaleH = substackIpl.getHeight() / samplingRate;
                    int rescaleD = substackIpl.getNSlices() / samplingRate;

                    substackIpl = Scaler.resize(substackIpl, rescaleW, rescaleH, rescaleD, "bilinear");

                    Calibration inputCal = inputImage.getImagePlus().getCalibration();
                    Calibration outputCal = substackIpl.getCalibration();
                    outputCal.pixelHeight = inputCal.pixelHeight * samplingRate;
                    outputCal.pixelWidth = inputCal.pixelWidth * samplingRate;
                    outputCal.pixelDepth = inputCal.pixelDepth * samplingRate;

                }

                ImageStack ist = substackIpl.getStack();

                // Initialising the Hough transform
                int[][] paramRanges = new int[][] { { 0, ist.getWidth() - 1 }, { 0, ist.getHeight() - 1 },
                        { 0, ipl.getNSlices() - 1 }, { minR, maxR } };
                SphereHoughTransform transform = new SphereHoughTransform(ist, paramRanges);
                transform.setnThreads(nThreads);

                // Running the transforms
                transform.run();

                // Normalising scores based on the number of points in that circle
                transform.normaliseScores();

                // Getting the accumulator as an image
                if (outputTransformImage || (showOutput && showTransformImage)) {
                    ImagePlus showIpl = new Duplicator().run(transform.getAccumulatorAsImage());

                    if (outputTransformImage) {
                        Image outputImage = new Image(outputImageName, showIpl);
                        workspace.addImage(outputImage);
                    }
                    if (showOutput && showTransformImage) {
                        IntensityMinMax.run(showIpl, true);
                        showIpl.setTitle("Accumulator");
                        showIpl.show();
                    }
                }

                // Getting circle objects and adding to workspace
                ArrayList<double[]> spheres = transform.getObjects(detectionThreshold, exclusionRadius);
                Indexer indexer = new Indexer(ist.getWidth(), ist.getHeight(), ist.size());
                for (double[] sphere : spheres) {
                    // Initialising the object
                    Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);

                    // Getting circle parameters
                    double conv = inputImage.getImagePlus().getCalibration().pixelWidth
                            / inputImage.getImagePlus().getCalibration().pixelDepth;
                    int x = (int) Math.round(sphere[0]) * samplingRate;
                    int y = (int) Math.round(sphere[1]) * samplingRate;
                    int z = (int) Math.round(sphere[2] * samplingRate * conv);
                    int r = (int) Math.round(sphere[3]) * samplingRate + radiusResize;
                    double score = sphere[4];

                    // Getting coordinates corresponding to circle
                    VoxelSphere voxelSphere = new VoxelSphere(r);
                    int[] xx = voxelSphere.getXSphereFill();
                    int[] yy = voxelSphere.getYSphereFill();
                    int[] zz = voxelSphere.getZSphereFill();

                    for (int i = 0; i < xx.length; i++) {
                        int idx = indexer.getIndex(new int[] { xx[i] + x, yy[i] + y, zz[i] + z });
                        if (idx == -1)
                            continue;

                        try {
                            try {
                                outputObject.add(xx[i] + x, yy[i] + y, zz[i] + z);
                            } catch (PointOutOfRangeException e) {
                            }
                        } catch (IntegerOverflowException e) {
                            return Status.FAIL;
                        }
                    }

                    // Adding measurements
                    outputObject.setT(t);
                    outputObject.addMeasurement(new Measurement(Measurements.SCORE, score));

                }

                writeProgressStatus(count++, total, "images");

            }
        }

        ipl.setPosition(1, 1, 1);
        workspace.addObjects(outputObjects);

        if (showOutput && showDetectionImage) {
            ImagePlus dispIpl = new Duplicator().run(ipl);
            IntensityMinMax.run(dispIpl, true);

            HashMap<Integer, Float> hues = ColourFactory.getRandomHues(outputObjects);

            HashMap<Integer, String> IDs = null;
            if (showHoughScore) {
                DecimalFormat df = LabelFactory.getDecimalFormat(0, true);
                IDs = LabelFactory.getMeasurementLabels(outputObjects, Measurements.SCORE, df);
                AddLabels.addOverlay(dispIpl, outputObjects, AddLabels.LabelPositions.CENTRE, IDs, labelSize, 0, 0,
                        hues, 100, false, false, true);
            }

            AddObjectOutline.addOverlay(dispIpl, outputObjects, 1, 1, hues, 100, false, true);

            dispIpl.setPosition(1, 1, 1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new BooleanP(OUTPUT_TRANSFORM_IMAGE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(DETECTION_SEPARATOR, this));
        parameters.add(new IntegerP(MIN_RADIUS, this, 10));
        parameters.add(new IntegerP(MAX_RADIUS, this, 20));
        parameters.add(new IntegerP(DOWNSAMPLE_FACTOR, this, 1));
        parameters.add(new DoubleP(DETECTION_THRESHOLD, this, 1.0));
        parameters.add(new IntegerP(EXCLUSION_RADIUS, this, 10));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        parameters.add(new SeparatorP(POST_PROCESSING_SEPARATOR, this));
        parameters.add(new IntegerP(RADIUS_RESIZE, this, 0));

        parameters.add(new SeparatorP(VISUALISATION_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_TRANSFORM_IMAGE, this, true));
        parameters.add(new BooleanP(SHOW_DETECTION_IMAGE, this, true));
        parameters.add(new BooleanP(SHOW_HOUGH_SCORE, this, false));
        parameters.add(new IntegerP(LABEL_SIZE, this, 12));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_TRANSFORM_IMAGE));
        if ((boolean) parameters.getValue(OUTPUT_TRANSFORM_IMAGE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(DETECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MIN_RADIUS));
        returnedParameters.add(parameters.getParameter(MAX_RADIUS));
        returnedParameters.add(parameters.getParameter(DETECTION_THRESHOLD));
        returnedParameters.add(parameters.getParameter(EXCLUSION_RADIUS));
        returnedParameters.add(parameters.getParameter(DOWNSAMPLE_FACTOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        returnedParameters.add(parameters.getParameter(POST_PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RADIUS_RESIZE));

        returnedParameters.add(parameters.getParameter(VISUALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_TRANSFORM_IMAGE));
        returnedParameters.add(parameters.getParameter(SHOW_DETECTION_IMAGE));
        if ((boolean) parameters.getValue(SHOW_DETECTION_IMAGE)) {
            returnedParameters.add(parameters.getParameter(SHOW_HOUGH_SCORE));
            if ((boolean) parameters.getValue(SHOW_HOUGH_SCORE)) {
                returnedParameters.add(parameters.getParameter(LABEL_SIZE));
            }
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        ObjMeasurementRef score = objectMeasurementRefs.getOrPut(Measurements.SCORE);
        score.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        returnedRefs.add(score);

        return returnedRefs;

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
        parameters.get(INPUT_IMAGE).setDescription("Input image from which circles will be detected.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output circle objects to be added to the workspace.  Irrespective of the form of the input circle features, output circles are always solid.");

        parameters.get(OUTPUT_TRANSFORM_IMAGE).setDescription(
                "When selected, the Hough-transform image will be output to the workspace with the name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + OUTPUT_TRANSFORM_IMAGE
                + "\" is selected, this will be the name assigned to the transform image added to the workspace.  The transform image has XY dimensions equal to the input image and an equal number of Z-slices to the number of radii tested.  Circluar features in the input image appear as bright points, where the XYZ location of the point corresponds to the XYR (i.e. X, Y, radius) parameters for the circle.");

        parameters.get(MIN_RADIUS)
                .setDescription("The minimum radius to detect circles for.  Specified in pixel units.");

        parameters.get(MAX_RADIUS)
                .setDescription("The maximum radius to detect circles for.  Specified in pixel units.");

        parameters.get(DETECTION_THRESHOLD).setDescription(
                "The minimum score a detected circle must have to be stored.  Scores are the sum of all pixel intensities lying on the perimeter of the circle.  As such, higher scores correspond to brighter circles, circles with high circularity (where all points lie on the perimeter of the detected circle) and circles with continuous intensity along their perimeter (no gaps).");

        parameters.get(EXCLUSION_RADIUS).setDescription(
                "The minimum distance between adjacent circles.  For multiple candidate points within this range, the circle with the highest score will be retained.  Specified in pixel units.");

        parameters.get(DOWNSAMPLE_FACTOR).setDescription(
                "To speed up the detection process, the image can be downsampled.  For example, a downsample factor of 2 will downsize the image in X and Y by a factor of 2 prior to detection of circles.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple radii simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

        parameters.get(RADIUS_RESIZE).setDescription(
                "Radius of output objects will be adjusted by this value.  For example, a detected circle of radius 5 with a \"radius resize\" of 2 will have an output radius of 7.  Similarly, setting \"radius resize\" to -3 would produce a circle of radius 2.");

        parameters.get(SHOW_TRANSFORM_IMAGE).setDescription(
                "When selected, the transform image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_DETECTION_IMAGE).setDescription(
                "When selected, the detection image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_HOUGH_SCORE).setDescription(
                "When selected, the detection image will also show the score associated with each detected circle.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the detection score text label.");

    }
}
