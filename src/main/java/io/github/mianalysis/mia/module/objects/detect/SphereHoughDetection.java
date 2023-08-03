package io.github.mianalysis.mia.module.objects.detect;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.Scaler;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.images.transform.InterpolateZAxis;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.sjcommon.exceptions.IntegerOverflowException;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.VolumeType;
import io.github.sjcross.sjcommon.object.voxels.SphereSolid;
import io.github.sjcross.sjcommon.process.IntensityMinMax;
import io.github.sjcross.sjcommon.process.houghtransform.transforms.SphereTransform;

/**
 * Created by sc13967 on 15/01/2018.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SphereHoughDetection extends AbstractHoughDetection {

	/**
	* 
	*/
    public static final String RANGE_SEPARATOR = "Parameter ranges";
    public static final String X_RANGE = "X range (px)";
    public static final String Y_RANGE = "Y range (px)";
    public static final String Z_RANGE = "Z range (slices)";
    public static final String RADIUS_RANGE = "Radius range (px)";


	/**
	* 
	*/
    public static final String POST_PROCESSING_SEPARATOR = "Object post processing";
    public static final String RADIUS_RESIZE = "Output radius resize (px)";

    public SphereHoughDetection(Modules modules) {
        super("Sphere detection", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getDescription() {
        return "Detects spheres within grayscale images using the Hough transform.  Input images can be of binary or grayscale format, but the sphere features must be brighter than their surrounding background and have dark centres (i.e. be shells).  For solid spheres, a gradient filter or equivalent should be applied to the image first.  Detected spheres are output to the workspace as solid objects.  Spheres are detected within a user-defined radius range and must exceed a user-defined threshold score (based on the intensity of the spherical feartures in the input image and the feature sphericity.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        boolean outputTransformImage = parameters.getValue(OUTPUT_TRANSFORM_IMAGE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Getting parameters
        String xRange = parameters.getValue(X_RANGE,workspace);
        String yRange = parameters.getValue(Y_RANGE,workspace);
        String zRange = parameters.getValue(Z_RANGE,workspace);
        String radiusRange = parameters.getValue(RADIUS_RANGE,workspace);
        int samplingRate = parameters.getValue(DOWNSAMPLE_FACTOR,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);
        boolean normaliseScores = parameters.getValue(NORMALISE_SCORES,workspace);
        String detectionMode = parameters.getValue(DETECTION_MODE,workspace);
        double detectionThreshold = parameters.getValue(DETECTION_THRESHOLD,workspace);
        int nObjects = parameters.getValue(NUMBER_OF_OBJECTS,workspace);
        int exclusionRadius = parameters.getValue(EXCLUSION_RADIUS,workspace);
        int radiusResize = parameters.getValue(RADIUS_RESIZE,workspace);
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
        zRange = resampleRange(zRange, samplingRate * outputObjects.getDppXY() / outputObjects.getDppZ());

        radiusRange = resampleRange(radiusRange, samplingRate);

        int nThreads = multithread ? Prefs.getThreads() : 1;

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
                substackIpl = InterpolateZAxis.matchZToXY(substackIpl, InterpolateZAxis.InterpolationModes.BILINEAR);

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
                String[] paramRanges = new String[] { xRange, yRange, zRange, radiusRange };
                SphereTransform transform = new SphereTransform(ist, paramRanges);
                transform.setnThreads(nThreads);

                // Running the transforms
                transform.run();

                // Normalising scores based on the number of points in that sphere
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

                // Getting sphere objects and adding to workspace
                ArrayList<double[]> spheres;
                switch (detectionMode) {
                    default:
                    case DetectionModes.ALL_ABOVE_SCORE:
                        spheres = transform.getObjects(detectionThreshold, exclusionRadius);
                        break;
                    case DetectionModes.N_HIGHEST_SCORES:
                        spheres = transform.getNObjects(nObjects, exclusionRadius);
                        break;
                }

                for (double[] sphere : spheres) {
                    // Initialising the object
                    Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);

                    // Getting sphere parameters
                    int x = (int) Math.round(sphere[0]) * samplingRate;
                    int y = (int) Math.round(sphere[1]) * samplingRate;
                    int z = (int) Math.round(sphere[2] * samplingRate * cal.dppXY / cal.dppZ);
                    int r = (int) Math.round(sphere[3]) * samplingRate + radiusResize;
                    double score = sphere[4];

                    // Getting coordinates corresponding to sphere
                    SphereSolid voxelSphere = new SphereSolid(r);
                    int[] xx = voxelSphere.getX();
                    int[] yy = voxelSphere.getY();
                    int[] zz = voxelSphere.getZ();

                    for (int i = 0; i < xx.length; i++) {
                        try {
                            try {
                                outputObject.add(xx[i] + x, yy[i] + y,
                                        (int) Math.round(zz[i] * cal.dppXY / cal.dppZ + z));
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

        if (showOutput && showDetectionImage)
            showDetectionImage(inputImage, outputObjects, showHoughScore, labelSize);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
        parameters.add(new StringP(X_RANGE, this, "0-end"));
        parameters.add(new StringP(Y_RANGE, this, "0-end"));
        parameters.add(new StringP(Z_RANGE, this, "0-end"));
        parameters.add(new StringP(RADIUS_RANGE, this, "10-20-1"));

        parameters.add(new SeparatorP(POST_PROCESSING_SEPARATOR, this));
        parameters.add(new IntegerP(RADIUS_RESIZE, this, 0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(updateAndGetInputParameters());

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_RANGE));
        returnedParameters.add(parameters.getParameter(Y_RANGE));
        returnedParameters.add(parameters.getParameter(Z_RANGE));
        returnedParameters.add(parameters.getParameter(RADIUS_RANGE));

        returnedParameters.addAll(updateAndGetDetectionParameters());

        returnedParameters.add(parameters.getParameter(POST_PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RADIUS_RESIZE));

        returnedParameters.addAll(updateAndGetVisualisationParameters());

        return returnedParameters;

    }

    void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + OUTPUT_TRANSFORM_IMAGE
                + "\" is selected, this will be the name assigned to the transform image added to the workspace.  The transform image has XY dimensions equal to the input image and an equal number of Z-slices to the number of radii tested.  Circluar features in the input image appear as bright points, where the XYZ location of the point corresponds to the XYR (i.e. X, Y, radius) parameters for the sphere.");

        parameters.get(X_RANGE).setDescription(
                "Range of X-position values to be tested.  X-position can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values 4,5,6 and 7).  Unlike other parameter ranges, X-position can't be specified as a range extracting every nth slice (e.g. \"4-10-2\"), instead image downsampling (\""
                        + DOWNSAMPLE_FACTOR
                        + "\" parameter) should be used.  X-position values are specified in pixel units.");

        parameters.get(Y_RANGE).setDescription(
                "Range of Y-position values to be tested.  Y-position can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values  4,5,6 and 7).  Unlike other parameter ranges, Y-position can't be specified as a range extracting every nth slice (e.g. \"4-10-2\"), instead image downsampling (\""
                        + DOWNSAMPLE_FACTOR
                        + "\" parameter) should be used.  Y-position values are specified in pixel units.");

        parameters.get(Z_RANGE).setDescription(
                "Range of Z-slices values to be tested.  Z-slices can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values  4,5,6 and 7).  Unlike other parameter ranges, Z-slices can't be specified as a range extracting every nth slice (e.g. \"4-10-2\"), instead image downsampling (\""
                        + DOWNSAMPLE_FACTOR
                        + "\" parameter) should be used.  Z-slices values are specified in slice units.");

        parameters.get(RADIUS_RANGE).setDescription(
                "Range of radius values to be tested.  Radii can be specified as a comma-separated list, using a range (e.g. \"4-7\" specifies values 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" specifies values 4,6,8 and 10).  Radii are specified in pixel units.");

        parameters.get(RADIUS_RESIZE).setDescription(
                "Radius of output objects will be adjusted by this value.  For example, a detected sphere of radius 5 with a \"radius resize\" of 2 will have an output radius of 7.  Similarly, setting \"radius resize\" to -3 would produce a sphere of radius 2.");

    }
}
