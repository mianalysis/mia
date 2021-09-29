package io.github.mianalysis.mia.module.objectprocessing.identification;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.RotatedRectRoi;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualisation.overlays.AddLabels;
import io.github.mianalysis.mia.module.visualisation.overlays.AddObjectOutline;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.LabelFactory;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;
import io.github.sjcross.common.process.IntensityMinMax;
import io.github.sjcross.common.process.houghtransform.transforms.RectangleTransform;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RectangleHoughDetection extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String OUTPUT_TRANSFORM_IMAGE = "Output transform image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String DETECTION_SEPARATOR = "Hough-based rectangle detection";
    public static final String X_RANGE = "X range (px)";
    public static final String Y_RANGE = "Y range (px)";
    public static final String WIDTH_RANGE = "Width range (px)";
    public static final String LENGTH_RANGE = "Length range (px)";
    public static final String ORIENTATION_RANGE = "Orientation range (degs)";
    public static final String DOWNSAMPLE_FACTOR = "Downsample factor";
    public static final String DETECTION_THRESHOLD = "Detection threshold";
    public static final String EXCLUSION_RADIUS = "Exclusion radius (px)";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public static final String VISUALISATION_SEPARATOR = "Visualisation controls";
    public static final String SHOW_TRANSFORM_IMAGE = "Show transform image";
    public static final String SHOW_DETECTION_IMAGE = "Show detection image";
    public static final String SHOW_HOUGH_SCORE = "Show detection score";
    public static final String LABEL_SIZE = "Label size";

    public RectangleHoughDetection(Modules modules) {
        super("Rectangle detection", modules);
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
        return "Detects rectangles within grayscale images using the Hough transform.  Input images can be of binary or grayscale format, but the rectangular features must be brighter than their surrounding background and have dark centres (i.e. not be solid).  For solid rectangles, a gradient filter or equivalent should be applied to the image first.  Detected rectangles are output to the workspace as solid objects.  Rectangles are detected within a user-defined width, length and orientation range and must exceed a user-defined threshold score (based on the intensity of the rectangles feartures in the input image).";

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
        String xRange = parameters.getValue(X_RANGE);
        String yRange = parameters.getValue(Y_RANGE);
        String widthRange = parameters.getValue(WIDTH_RANGE);
        String lengthRange = parameters.getValue(LENGTH_RANGE);
        String oriRange = parameters.getValue(ORIENTATION_RANGE);
        int samplingRate = parameters.getValue(DOWNSAMPLE_FACTOR);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE);
        boolean showDetectionImage = parameters.getValue(SHOW_DETECTION_IMAGE);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE);
        int labelSize = parameters.getValue(LABEL_SIZE);

        // Storing the image calibration
        SpatCal cal = SpatCal.getFromImage(ipl);
        int nFrames = ipl.getNFrames();
        double frameInterval = ipl.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, cal, nFrames, frameInterval, TemporalUnit.getOMEUnit());

        int nThreads = multithread ? Prefs.getThreads() : 1;

        // Iterating over all images in the ImagePlus
        int count = 1;
        int total = ipl.getNChannels() * ipl.getNSlices() * ipl.getNFrames();

        for (int c = 0; c < ipl.getNChannels(); c++) {
            for (int z = 0; z < ipl.getNSlices(); z++) {
                for (int t = 0; t < ipl.getNFrames(); t++) {
                    ipl.setPosition(c + 1, z + 1, t + 1);

                    // Initialising the Hough transform
                    String[] paramRanges = new String[] { xRange, yRange, widthRange, lengthRange, oriRange };
                    RectangleTransform transform = new RectangleTransform(ipl.getProcessor(), paramRanges);
                    transform.setnThreads(nThreads);

                    // Running the transforms
                    transform.run();

                    // Normalising scores based on the number of points in that rectangle
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

                    // Getting rectangle objects and adding to workspace
                    ArrayList<double[]> rectangles = transform.getObjects(detectionThreshold, exclusionRadius);
                    for (double[] rectangle : rectangles) {
                        // Initialising the object
                        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);

                        // Getting rectangle parameters
                        int x = (int) Math.round(rectangle[0]);
                        int y = (int) Math.round(rectangle[1]);
                        int w = (int) Math.round(rectangle[2]);
                        int l = (int) Math.round(rectangle[3]);
                        int ori = (int) Math.round(rectangle[4]);
                        double score = rectangle[5];

                        double thetaRads = -Math.toRadians((double) ori);
                        int x1 = (int) Math.round((-l / 2) * Math.cos(thetaRads));
                        int y1 = (int) Math.round(-(-l / 2) * Math.sin(thetaRads));

                        RotatedRectRoi roi = new RotatedRectRoi(x + x1, y + y1, x - x1, y - y1, w);

                        for (Point point : roi.getContainedPoints()) {
                            try {
                                try {
                                    outputObject.add(point.x, point.y, z);
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
        parameters.add(new StringP(X_RANGE, this, "0-end"));
        parameters.add(new StringP(Y_RANGE, this, "0-end"));
        parameters.add(new StringP(WIDTH_RANGE, this, "10-20-1"));
        parameters.add(new StringP(LENGTH_RANGE, this, "20-30-1"));
        parameters.add(new StringP(ORIENTATION_RANGE, this, "0-360-20"));
        parameters.add(new DoubleP(DETECTION_THRESHOLD, this, 1.0));
        parameters.add(new IntegerP(EXCLUSION_RADIUS, this, 10));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        parameters.add(new SeparatorP(VISUALISATION_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_TRANSFORM_IMAGE, this, true));
        parameters.add(new BooleanP(SHOW_DETECTION_IMAGE, this, true));
        parameters.add(new BooleanP(SHOW_HOUGH_SCORE, this, false));
        parameters.add(new IntegerP(LABEL_SIZE, this, 12));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_TRANSFORM_IMAGE));
        if ((boolean) parameters.getValue(OUTPUT_TRANSFORM_IMAGE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(DETECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_RANGE));
        returnedParameters.add(parameters.getParameter(Y_RANGE));
        returnedParameters.add(parameters.getParameter(WIDTH_RANGE));
        returnedParameters.add(parameters.getParameter(LENGTH_RANGE));
        returnedParameters.add(parameters.getParameter(ORIENTATION_RANGE));
        returnedParameters.add(parameters.getParameter(DETECTION_THRESHOLD));
        returnedParameters.add(parameters.getParameter(EXCLUSION_RADIUS));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        ObjMeasurementRef score = objectMeasurementRefs.getOrPut(Measurements.SCORE);
        score.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        returnedRefs.add(score);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Input image from which rectangles will be detected.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output rectangle objects to be added to the workspace.  Irrespective of the form of the input rectangle features, output rectangles are always solid.");

        parameters.get(OUTPUT_TRANSFORM_IMAGE).setDescription(
                "When selected, the Hough-transform image will be output to the workspace with the name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + OUTPUT_TRANSFORM_IMAGE
                + "\" is selected, this will be the name assigned to the transform image added to the workspace.  The transform image has XY dimensions equal to the input image and an equal number of Z-slices to the number of radii tested.  Circluar features in the input image appear as bright points, where the XYZ location of the point corresponds to the XYR (i.e. X, Y, radius) parameters for the rectangle.");

        parameters.get(WIDTH_RANGE).setDescription(
                "Range of width values to be tested.  Widths can be specified as a comma-separated list, using a range (e.g. \"4-7\" will extract relative indices 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" will extract slices 4,6,8 and 10).  Widths are specified in pixel units.");

        parameters.get(LENGTH_RANGE).setDescription(
                "Range of length values to be tested.  Lengths can be specified as a comma-separated list, using a range (e.g. \"4-7\" will extract relative indices 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" will extract slices 4,6,8 and 10).  Lengths are specified in pixel units.");

        parameters.get(ORIENTATION_RANGE).setDescription(
                "Range of orientation values to be tested.  Orientations can be specified as a comma-separated list, using a range (e.g. \"4-7\" will extract relative indices 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" will extract slices 4,6,8 and 10).  Orientations are specified in degree units.");

        parameters.get(DETECTION_THRESHOLD).setDescription(
                "The minimum score a detected rectangle must have to be stored.  Scores are the sum of all pixel intensities lying on the perimeter of the rectangle.  As such, higher scores correspond to brighter rectangles, rectangles with high circularity (where all points lie on the perimeter of the detected rectangle) and rectangles with continuous intensity along their perimeter (no gaps).");

        parameters.get(EXCLUSION_RADIUS).setDescription(
                "The minimum distance between adjacent rectangles.  For multiple candidate points within this range, the rectangle with the highest score will be retained.  Specified in pixel units.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple radii simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

        parameters.get(SHOW_TRANSFORM_IMAGE).setDescription(
                "When selected, the transform image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_DETECTION_IMAGE).setDescription(
                "When selected, the detection image will be displayed (as long as the module is currently set to show its output).");

        parameters.get(SHOW_HOUGH_SCORE).setDescription(
                "When selected, the detection image will also show the score associated with each detected rectangle.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the detection score text label.");

    }
}
