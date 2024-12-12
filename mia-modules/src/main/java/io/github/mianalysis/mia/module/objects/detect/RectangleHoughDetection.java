package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Point;
import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.RotatedRectRoi;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.process.houghtransform.transforms.RectangleTransform;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;


/**
* Detects rectangles within grayscale images using the Hough transform.  Input images can be of binary or grayscale format, but the rectangular features must be brighter than their surrounding background and have dark centres (i.e. not be solid).  For solid rectangles, a gradient filter or equivalent should be applied to the image first.  Detected rectangles are output to the workspace as solid objects.  Rectangles are detected within a user-defined width, length and orientation range and must exceed a user-defined threshold score (based on the intensity of the rectangles feartures in the input image).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RectangleHoughDetection extends AbstractHoughDetection {

	/**
	* 
	*/
    public static final String RANGE_SEPARATOR = "Parameter ranges";
    public static final String X_RANGE = "X range (px)";
    public static final String Y_RANGE = "Y range (px)";
    public static final String WIDTH_RANGE = "Width range (px)";
    public static final String LENGTH_RANGE = "Length range (px)";
    public static final String ORIENTATION_RANGE = "Orientation range (degs)";

    public RectangleHoughDetection(Modules modules) {
        super("Rectangle detection", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Detects rectangles within grayscale images using the Hough transform.  Input images can be of binary or grayscale format, but the rectangular features must be brighter than their surrounding background and have dark centres (i.e. not be solid).  For solid rectangles, a gradient filter or equivalent should be applied to the image first.  Detected rectangles are output to the workspace as solid objects.  Rectangles are detected within a user-defined width, length and orientation range and must exceed a user-defined threshold score (based on the intensity of the rectangles feartures in the input image).";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        boolean outputTransformImage = parameters.getValue(OUTPUT_TRANSFORM_IMAGE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Getting parameters
        String xRange = parameters.getValue(X_RANGE,workspace);
        String yRange = parameters.getValue(Y_RANGE,workspace);
        String widthRange = parameters.getValue(WIDTH_RANGE,workspace);
        String lengthRange = parameters.getValue(LENGTH_RANGE,workspace);
        String oriRange = parameters.getValue(ORIENTATION_RANGE,workspace);
        int samplingRate = parameters.getValue(DOWNSAMPLE_FACTOR,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);
        boolean normaliseScores = parameters.getValue(NORMALISE_SCORES,workspace);
        String detectionMode = parameters.getValue(DETECTION_MODE,workspace);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD,workspace);
        int nObjects = parameters.getValue(NUMBER_OF_OBJECTS,workspace);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS,workspace);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE,workspace);
        boolean showDetectionImage = parameters.getValue(SHOW_DETECTION_IMAGE,workspace);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE,workspace);
        int labelSize = parameters.getValue(LABEL_SIZE,workspace);

        // Storing the image calibration
        SpatCal cal = SpatCal.getFromImage(ipl);
        int nFrames = ipl.getNFrames();
        double frameInterval = ipl.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, cal, nFrames, frameInterval, TemporalUnit.getOMEUnit());

        xRange = resampleRange(xRange, samplingRate);
        yRange = resampleRange(yRange, samplingRate);
        widthRange = resampleRange(widthRange, samplingRate);
        lengthRange = resampleRange(lengthRange, samplingRate);

        int nThreads = multithread ? Prefs.getThreads() : 1;

        // Iterating over all images in the ImagePlus
        int count = 1;
        int total = ipl.getNChannels() * ipl.getNSlices() * ipl.getNFrames();

        for (int c = 0; c < ipl.getNChannels(); c++) {
            for (int z = 0; z < ipl.getNSlices(); z++) {
                for (int t = 0; t < ipl.getNFrames(); t++) {
                    ipl.setPosition(c + 1, z + 1, t + 1);

                    // Applying scaling
                    ImageProcessor ipr = ipl.getProcessor();
                    if (samplingRate != 1)
                        ipr = ipr.resize(ipr.getWidth() / samplingRate);

                    // Initialising the Hough transform
                    String[] paramRanges = new String[] { xRange, yRange, widthRange, lengthRange, oriRange };
                    RectangleTransform transform = new RectangleTransform(ipr, paramRanges);
                    transform.setnThreads(nThreads);

                    // Running the transforms
                    transform.run();

                    // Normalising scores based on the number of points in that rectangle
                    if (normaliseScores)
                        transform.normaliseScores();

                    // Getting the accumulator as an image
                    if (outputTransformImage || (showOutput && showTransformImage)) {
                        ImagePlus showIpl = new Duplicator().run(transform.getAccumulatorAsImage());

                        if (outputTransformImage) {
                            ImageI outputImage = ImageFactory.createImage(outputImageName, showIpl);
                            workspace.addImage(outputImage);
                        }
                        if (showOutput && showTransformImage) {
                            IntensityMinMax.run(showIpl, true);
                            showIpl.setTitle("Accumulator");
                            showIpl.show();
                        }
                    }

                    // Getting rectangle objects and adding to workspace
                    ArrayList<double[]> rectangles;
                    switch (detectionMode) {
                        default:
                        case DetectionModes.ALL_ABOVE_SCORE:
                            rectangles = transform.getObjects(detectionThreshold, exclusionRadius);
                            break;
                        case DetectionModes.N_HIGHEST_SCORES:
                            rectangles = transform.getNObjects(nObjects, exclusionRadius);
                            break;
                    }
                    for (double[] rectangle : rectangles) {
                        // Initialising the object
                        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);

                        // Getting rectangle parameters
                        int x = (int) Math.round(rectangle[0]) * samplingRate;
                        int y = (int) Math.round(rectangle[1]) * samplingRate;
                        int w = (int) Math.round(rectangle[2]) * samplingRate;
                        int l = (int) Math.round(rectangle[3]) * samplingRate;
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

        if (showOutput && showDetectionImage)
            showDetectionImage(inputImage, outputObjects, showHoughScore, labelSize);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
        parameters.add(new StringP(X_RANGE, this, "0-end"));
        parameters.add(new StringP(Y_RANGE, this, "0-end"));
        parameters.add(new StringP(WIDTH_RANGE, this, "10-20-1"));
        parameters.add(new StringP(LENGTH_RANGE, this, "20-30-1"));
        parameters.add(new StringP(ORIENTATION_RANGE, this, "0-360-20"));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(updateAndGetInputParameters());

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_RANGE));
        returnedParameters.add(parameters.getParameter(Y_RANGE));
        returnedParameters.add(parameters.getParameter(WIDTH_RANGE));
        returnedParameters.add(parameters.getParameter(LENGTH_RANGE));
        returnedParameters.add(parameters.getParameter(ORIENTATION_RANGE));

        returnedParameters.addAll(updateAndGetDetectionParameters());

        returnedParameters.addAll(updateAndGetVisualisationParameters());

        return returnedParameters;

    }

    void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + OUTPUT_TRANSFORM_IMAGE
                + "\" is selected, this will be the name assigned to the transform image added to the workspace.  The transform image has XY dimensions equal to the input image, an equal number of channels to the number of widths tested, Z-slices to the number of lengths tested and frames to the number of orientations tests.  Rectangular features in the input image appear as bright points, where the XYCZT location of the point corresponds to the XYWLO (i.e. X, Y, width, length and orientation) parameters for the rectangle.");

        parameters.get(X_RANGE).setDescription(
                "Range of X-position values to be tested.  X-position can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values 4,5,6 and 7).  Unlike other parameter ranges, X-position can't be specified as a range extracting every nth slice (e.g. \"4-10-2\"), instead image downsampling (\""
                        + DOWNSAMPLE_FACTOR
                        + "\" parameter) should be used.  X-position values are specified in pixel units.");

        parameters.get(Y_RANGE).setDescription(
                "Range of Y-position values to be tested.  Y-position can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values  4,5,6 and 7).  Unlike other parameter ranges, Y-position can't be specified as a range extracting every nth slice (e.g. \"4-10-2\"), instead image downsampling (\""
                        + DOWNSAMPLE_FACTOR
                        + "\" parameter) should be used.  Y-position values are specified in pixel units.");

        parameters.get(WIDTH_RANGE).setDescription(
                "Range of width values to be tested.  Widths can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" specifies values 4,6,8 and 10).  Widths are specified in pixel units.");

        parameters.get(LENGTH_RANGE).setDescription(
                "Range of length values to be tested.  Lengths can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" specifies values 4,6,8 and 10).  Lengths are specified in pixel units.");

        parameters.get(ORIENTATION_RANGE).setDescription(
                "Range of orientation values to be tested.  Orientations can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" specifies values 4,6,8 and 10).  Orientations are specified in degree units.");

    }
}
