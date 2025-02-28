package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Point;
import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.process.houghtransform.transforms.LineTransform;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;

/**
 * Detects rectangles within grayscale images using the Hough transform. Input
 * images can be of binary or grayscale format, but the rectangular features
 * must be brighter than their surrounding background and have dark centres
 * (i.e. not be solid). For solid rectangles, a gradient filter or equivalent
 * should be applied to the image first. Detected rectangles are output to the
 * workspace as solid objects. Rectangles are detected within a user-defined
 * width, length and orientation range and must exceed a user-defined threshold
 * score (based on the intensity of the rectangles feartures in the input
 * image).
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class LineHoughDetection extends AbstractHoughDetection {

    /**
    * 
    */
    public static final String RANGE_SEPARATOR = "Parameter ranges";
    public static final String THETA_RANGE = "Angle range (degs)";

    public interface Measurements extends AbstractHoughDetection.Measurements {
        String ORIENTATION = "HOUGH_DETECTION // ORIENTATION_(DEGS)";

    }

    public LineHoughDetection(Modules modules) {
        super("Line detection", modules);
        deprecated = true; // This isn't ready for full-scale release
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
        return "Detects lines within grayscale images using the Hough transform.  Input images can be of binary or grayscale format, but the line features must be brighter than their surrounding background and have dark centres (i.e. not be solid).";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean outputTransformImage = parameters.getValue(OUTPUT_TRANSFORM_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String thetaRange = parameters.getValue(THETA_RANGE, workspace);
        int samplingRate = parameters.getValue(DOWNSAMPLE_FACTOR, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);
        boolean normaliseScores = parameters.getValue(NORMALISE_SCORES, workspace);
        String detectionMode = parameters.getValue(DETECTION_MODE, workspace);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD, workspace);
        int nObjects = parameters.getValue(NUMBER_OF_OBJECTS, workspace);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS, workspace);
        boolean showTransformImage = parameters.getValue(SHOW_TRANSFORM_IMAGE, workspace);
        boolean showDetectionImage = parameters.getValue(SHOW_DETECTION_IMAGE, workspace);
        boolean showHoughScore = parameters.getValue(SHOW_HOUGH_SCORE, workspace);
        int labelSize = parameters.getValue(LABEL_SIZE, workspace);

        // Getting image
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Storing the image calibration
        SpatCal cal = SpatCal.getFromImage(ipl);
        int nFrames = ipl.getNFrames();
        double frameInterval = ipl.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, cal, nFrames, frameInterval, TemporalUnit.getOMEUnit());

        String rRange = resampleRange("-end-end", samplingRate);

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
                    String[] paramRanges = new String[] { rRange, thetaRange };
                    LineTransform transform = new LineTransform(ipr, paramRanges);
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
                            Image outputImage = ImageFactory.createImage(outputImageName, showIpl);
                            workspace.addImage(outputImage);
                        }
                        if (showOutput && showTransformImage) {
                            IntensityMinMax.run(showIpl, true);
                            showIpl.setTitle("Accumulator");
                            showIpl.show();
                        }
                    }

                    // Getting rectangle objects and adding to workspace
                    ArrayList<double[]> lines;
                    switch (detectionMode) {
                        default:
                        case DetectionModes.ALL_ABOVE_SCORE:
                            lines = transform.getObjects(detectionThreshold, exclusionRadius);
                            break;
                        case DetectionModes.N_HIGHEST_SCORES:
                            lines = transform.getNObjects(nObjects, exclusionRadius);
                            break;
                    }
                    for (double[] line : lines) {
                        // Initialising the object
                        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);

                        // Getting rectangle parameters
                        int r = (int) Math.round(line[0]) * samplingRate;
                        int thetaD = (int) Math.round(line[1]);
                        double score = line[2];

                        double thetaR = Math.toRadians(thetaD);
                        int y0 = (int) Math.round(r / (Math.sin(thetaR)));
                        int y1 = (int) Math.round((r - ipl.getWidth() * Math.cos(thetaR)) / (Math.sin(thetaR)));

                        Line roi = new Line(0, y0, ipl.getWidth(), y1);

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
                        outputObject.addMeasurement(new Measurement(Measurements.ORIENTATION, thetaD));

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
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
        parameters.add(new StringP(THETA_RANGE, this, "-90-90"));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(updateAndGetInputParameters());

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THETA_RANGE));

        returnedParameters.addAll(updateAndGetDetectionParameters());

        returnedParameters.addAll(updateAndGetVisualisationParameters());

        return returnedParameters;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = super.updateAndGetObjectMeasurementRefs();

        ObjMeasurementRef orientation = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION);
        orientation.setObjectsName(parameters.getValue(OUTPUT_OBJECTS, workspace));
        returnedRefs.add(orientation);

        return returnedRefs;

    }
}
