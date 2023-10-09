package io.github.mianalysis.mia.module.images.process;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import de.biomedical_imaging.ij.steger.Convol;
import de.biomedical_imaging.ij.steger.LinesUtil;
import de.biomedical_imaging.ij.steger.Position;
import fiji.stacks.Hyperstack_rearranger;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.Filters3D;
import ij.plugin.GaussianBlur3D;
import ij.plugin.ImageCalculator;
import ij.plugin.SubHyperstackMaker;
import ij.plugin.ZProjector;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.strel.DiskStrel;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImagePlusImage;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;

/**
 * Created by Stephen on 30/05/2017.
 */

/**
 * Apply intensity filters to an image (or image stack) in the workspace.
 * Filters are applied to each Z-stack independently (i.e. channels and
 * timepoints do not interact with each other).
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterImage extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
     * Image to apply filter to.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Select if the filter should be applied directly to the input image, or if it
     * should be applied to a duplicate, then stored as a different image in the
     * workspace.
     */
    public static final String APPLY_TO_INPUT = "Apply to input image";

    /**
     * Name of the output image created during the filtering process. This image
     * will be added to the workspace.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String FILTER_SEPARATOR = "Filter controls";

    /**
     * Filter to be applied to the image.<br>
     * <ul>
     * <li>"Difference of Gaussian 2D" Difference of Gaussian filter (2D) Used to
     * enhance spot-like features of sizes similar to the setting for "Filter
     * radius".</li>
     * <li>"Gaussian 2D"</li>
     * <li>"Gaussian 3D"</li>
     * <li>"Gradient 2D"</li>
     * <li>"Maximum 2D"</li>
     * <li>"Maximum 3D"</li>
     * <li>"Mean 2D"</li>
     * <li>"Mean 3D"</li>
     * <li>"Median 2D"</li>
     * <li>"Median 3D"</li>
     * <li>"Minimum 2D"</li>
     * <li>"Minimum 3D"</li>
     * <li>"Ridge enhancement 2D" Uses initial image processing steps from "Ridge
     * Detection" plugin to enhance ridge-like structures.</li>
     * <li>"Rolling frame" Filters the image at each frame based on frames before
     * and/after. The frame window over which the statistics are calculated is
     * user-controllable</li>
     * <li>"Variance 2D"</li>
     * <li>"Variance 3D"</li>
     * </ul>
     */
    public static final String FILTER_MODE = "Filter mode";

    /**
     * Range the filter is calculated over. Often also referred to as "sigma". Value
     * specified in pixel units, unless "calibrated units" is enabled.
     */
    public static final String FILTER_RADIUS = "Filter radius";

    /**
     * Second filter value for when using DoG (difference of Gaussian) filter. Value
     * specified in pixel units, unless "calibrated units" is enabled.
     */
    public static final String FILTER_RADIUS_2 = "Filter radius 2";

    /**
     * Choose if filter radius is specified in pixel (set to "false") or calibrated
     * (set to "true") units. What units are used are controlled from "Input
     * control".
     */
    public static final String CALIBRATED_UNITS = "Calibrated units";

    /**
     * Statistic to apply for rolling frame filtering.
     */
    public static final String ROLLING_METHOD = "Rolling filter method";

    /**
     * When "Filter mode" is set to "Rolling frame", the rolling frame statistic
     * will be calculated for each frame using these relative frames (i.e. with
     * indices set to "-1,1" the statistic would be calculated based on the frames
     * immediately before and after).
     */
    public static final String WINDOW_INDICES = "Window indices";

    /**
     * When "Filter mode" is set to "Ridge enhancement 2D", this parameter controls
     * whether the ridges to be enhanced are bright (brighter than the background)
     * or dark (darker than the background).
     */
    public static final String CONTOUR_CONTRAST = "Contour contrast";

    public FilterImage(Modules modules) {
        super("Filter image", modules);
    }

    public interface FilterModes {
        String DOG2D = "DoG 2D";
        String GAUSSIAN2D = "Gaussian 2D"; // Tested
        String GAUSSIAN3D = "Gaussian 3D"; // Tested
        String GRADIENT2D = "Gradient 2D";
        String LOG2DAPPROX = "LoG 2D (approximation)";
        String MAXIMUM2D = "Maximum 2D";
        String MAXIMUM3D = "Maximum 3D";
        String MEAN2D = "Mean 2D";
        String MEAN3D = "Mean 3D";
        String MEDIAN2D = "Median 2D";
        String MEDIAN3D = "Median 3D";
        String MINIMUM2D = "Minimum 2D";
        String MINIMUM3D = "Minimum 3D";
        String RIDGE_ENHANCEMENT = "Ridge enhancement 2D";
        String ROLLING_FRAME = "Rolling frame";
        String VARIANCE2D = "Variance 2D";
        String VARIANCE3D = "Variance 3D";

        String[] ALL = new String[] { DOG2D, GAUSSIAN2D, GAUSSIAN3D, GRADIENT2D, LOG2DAPPROX, MAXIMUM2D, MAXIMUM3D,
                MEAN2D, MEAN3D,
                MEDIAN2D, MEDIAN3D, MINIMUM2D, MINIMUM3D, RIDGE_ENHANCEMENT, ROLLING_FRAME, VARIANCE2D, VARIANCE3D };

    }

    public interface RollingMethods {
        String AVERAGE = "Average";
        String MEDIAN = "Median";
        String MINIMUM = "Minimum";
        String MAXIMUM = "Maximum";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[] { AVERAGE, MEDIAN, MINIMUM, MAXIMUM, STDEV, SUM };

    }

    public interface WindowModes {
        String BOTH_SIDES = "Both sides";
        String PREVIOUS = "Previous only";
        String FUTURE = "Future only";

        String[] ALL = new String[] { BOTH_SIDES, PREVIOUS, FUTURE };

    }

    public interface ContourContrast {
        String DARK_LINE = "Dark line";
        String LIGHT_LINE = "Light line";

        String[] ALL = new String[] { DARK_LINE, LIGHT_LINE };

    }

    public static void apply2DFilter(ImagePlus inputImagePlus, String filterMode, double filterRadius) {
        String moduleName = new FilterImage(null).getName();

        // Determining which rank filter ID to use
        int rankFilter = 0;
        switch (filterMode) {
            case FilterModes.MAXIMUM2D:
                rankFilter = RankFilters.MAX;
                break;
            case FilterModes.MEAN2D:
                rankFilter = RankFilters.MEAN;
                break;
            case FilterModes.MEDIAN2D:
                rankFilter = RankFilters.MEDIAN;
                break;
            case FilterModes.MINIMUM2D:
                rankFilter = RankFilters.MIN;
                break;
            case FilterModes.VARIANCE2D:
                rankFilter = RankFilters.VARIANCE;
                break;
        }

        int count = 0;
        int total = inputImagePlus.getStack().size();
        RankFilters filter = new RankFilters();
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    filter.rank(inputImagePlus.getProcessor(), filterRadius, rankFilter);

                    writeProgressStatus(count++, total, "images", moduleName);

                }
            }
        }
        inputImagePlus.setPosition(1, 1, 1);
    }

    public static void apply3DFilter(ImagePlus inputImagePlus, String filterMode, float filterRadius) {
        String moduleName = new FilterImage(null).getName();

        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();

        int filter = 0;
        switch (filterMode) {
            case FilterModes.MAXIMUM3D:
                filter = Filters3D.MAX;
                break;
            case FilterModes.MEAN3D:
                filter = Filters3D.MEAN;
                break;
            case FilterModes.MEDIAN3D:
                filter = Filters3D.MEDIAN;
                break;
            case FilterModes.MINIMUM3D:
                filter = Filters3D.MIN;
                break;
            case FilterModes.VARIANCE3D:
                filter = Filters3D.VAR;
                break;

        }

        // Variance 3D will output a 32-bit image
        if (filterMode.equals(FilterModes.VARIANCE3D))
            ImageTypeConverter.process(inputImagePlus, 32, ImageTypeConverter.ScalingModes.CLIP);

        int count = 0;
        int total = inputImagePlus.getNChannels() * inputImagePlus.getNFrames();
        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(inputImagePlus, c + "-" + c, "1-" + nSlices,
                        t + "-" + t);

                if (filterMode.equals(FilterModes.GAUSSIAN3D)) {
                    GaussianBlur3D.blur(iplOrig, filterRadius, filterRadius,
                            filterRadius);
                    ImagePlusImage.getSetStack(inputImagePlus, t, c, iplOrig.getStack());

                } else {
                    ImageStack istFilt = Filters3D.filter(iplOrig.getStack(), filter, filterRadius, filterRadius,
                            filterRadius);
                    ImagePlusImage.getSetStack(inputImagePlus, t, c, istFilt);

                }

                writeProgressStatus(count++, total, "images", moduleName);

            }
        }

        inputImagePlus.setPosition(1, 1, 1);
        inputImagePlus.updateChannelAndDraw();

    }

    public static void runDoG2DFilter(ImagePlus imagePlus, double sigma1, double sigma2) {
        // We want to output a 32-bit image
        ImageTypeConverter.process(imagePlus, 32, ImageTypeConverter.ScalingModes.CLIP);

        for (int z = 1; z <= imagePlus.getNSlices(); z++) {
            for (int c = 1; c <= imagePlus.getNChannels(); c++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    imagePlus.setPosition(c, z, t);
                    ImagePlus ipl1 = new ImagePlus("1", imagePlus.getProcessor().duplicate());
                    ImagePlus ipl2 = new ImagePlus("2", imagePlus.getProcessor().duplicate());

                    runGaussian2DFilter(ipl1, sigma1);
                    runGaussian2DFilter(ipl2, sigma2);

                    imagePlus.setProcessor(ImageCalculator.run(ipl1, ipl2, "Subtract").getProcessor());

                }
            }
        }
        imagePlus.setPosition(1, 1, 1);
    }

    public static void runGaussian2DFilter(ImagePlus imagePlus, double sigma) {
        for (int z = 1; z <= imagePlus.getNSlices(); z++) {
            for (int c = 1; c <= imagePlus.getNChannels(); c++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    imagePlus.setPosition(c, z, t);
                    imagePlus.getProcessor().blurGaussian(sigma);
                }
            }
        }
        imagePlus.setPosition(1, 1, 1);
    }

    public static void runGradient2DFilter(ImagePlus ipl, double sigma) {
        DiskStrel strel = DiskStrel.fromRadius((int) Math.round(sigma));
        for (int z = 1; z <= ipl.getNSlices(); z++) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    ipl.setPosition(c, z, t);
                    ipl.setProcessor(Morphology.gradient(ipl.getProcessor(), strel));
                }
            }
        }
        ipl.setPosition(1, 1, 1);
        ipl.updateAndDraw();
    }

    public static void runRidgeEnhancement2DFilter(ImagePlus ipl, double sigma, String contourContrast) {
        int width = ipl.getWidth();
        int height = ipl.getHeight();

        Convol convol = new Convol();
        Position position = new Position();

        int mult = contourContrast.equals(ContourContrast.LIGHT_LINE) ? -1 : 1;

        ImageTypeConverter.process(ipl, 32, ImageTypeConverter.ScalingModes.CLIP);

        for (int z = 1; z <= ipl.getNSlices(); z++) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    int idx = ipl.getStackIndex(c, z, t);
                    ImageProcessor ipr = ipl.getStack().getProcessor(idx);
                    float[] image = (float[]) ipr.getPixels();

                    float[] kRR = new float[width * height];
                    float[] kRC = new float[width * height];
                    float[] kCC = new float[width * height];

                    double[] eigval = new double[2];
                    double[][] eigvec = new double[2][2];

                    convol.convolve_gauss(image, kRR, width, height, sigma, LinesUtil.DERIV_RR);
                    convol.convolve_gauss(image, kRC, width, height, sigma, LinesUtil.DERIV_RC);
                    convol.convolve_gauss(image, kCC, width, height, sigma, LinesUtil.DERIV_CC);

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int l = LinesUtil.LINCOOR(y, x, width);

                            position.compute_eigenvals(kRR[l], kRC[l], kCC[l], eigval, eigvec);

                            double val = eigval[0] * mult;
                            if (val > 0.0)
                                ipr.setf(x, y, (float) val);
                            else
                                ipr.setf(x, y, 0);
                        }
                    }
                }
            }
        }
        ipl.updateAndDraw();

    }

    public static void runRollingFrameFilter(ImagePlus inputImagePlus, String windowIndices, String rollingMethod) {
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();
        ArrayList<Integer> channels = new ArrayList<>();
        ArrayList<Integer> slices = new ArrayList<>();
        for (int channel = 1; channel <= nChannels; channel++)
            channels.add(channel);
        for (int slice = 1; slice <= nSlices; slice++)
            slices.add(slice);

        ImagePlus tempImagePlus = new Duplicator().run(inputImagePlus);

        // Getting list of frames
        int[] offsets = CommaSeparatedStringInterpreter.interpretIntegers(windowIndices, true, 0);

        // Running through each frame, calculating the local average
        for (int f = 1; f <= inputImagePlus.getNFrames(); f++) {
            ArrayList<Integer> frames = new ArrayList<>();
            for (int offset : offsets) {
                int frame = f + offset;
                if (frame > 0 && frame <= nFrames)
                    frames.add(frame);
            }

            // Creating a local substack and switching T and Z, so time (not Z) is averaged
            ImagePlus currentSubstack = SubHyperstackMaker.makeSubhyperstack(tempImagePlus, channels, slices, frames);
            currentSubstack = Hyperstack_rearranger.reorderHyperstack(currentSubstack, "CTZ", true, false);

            // Applying average filter
            ZProjector zProjector = new ZProjector(currentSubstack);
            switch (rollingMethod) {
                case RollingMethods.AVERAGE:
                    zProjector.setMethod(ZProjector.AVG_METHOD);
                    break;
                case RollingMethods.MEDIAN:
                    zProjector.setMethod(ZProjector.MEDIAN_METHOD);
                    break;
                case RollingMethods.MINIMUM:
                    zProjector.setMethod(ZProjector.MIN_METHOD);
                    break;
                case RollingMethods.MAXIMUM:
                    zProjector.setMethod(ZProjector.MAX_METHOD);
                    break;
                case RollingMethods.STDEV:
                    zProjector.setMethod(ZProjector.SD_METHOD);
                    ImageTypeConverter.process(inputImagePlus, 32, ImageTypeConverter.ScalingModes.CLIP);
                    break;
                case RollingMethods.SUM:
                    zProjector.setMethod(ZProjector.SUM_METHOD);
                    break;
            }

            zProjector.setStartSlice(1);
            zProjector.setStopSlice(currentSubstack.getNSlices());
            zProjector.doHyperStackProjection(true);
            ImagePlus iplOut = zProjector.getProjection();

            // Switching T and Z back
            iplOut = Hyperstack_rearranger.reorderHyperstack(iplOut, "CTZ", true, false);

            // Adding the new image into outputImagePlus
            for (int z = 1; z <= iplOut.getNSlices(); z++) {
                for (int c = 1; c <= iplOut.getNChannels(); c++) {
                    inputImagePlus.setPosition(c, z, f);
                    iplOut.setPosition(c, z, 1);

                    inputImagePlus.setProcessor(iplOut.getProcessor());

                }
            }
        }

        inputImagePlus.setPosition(1, 1, 1);
        inputImagePlus.updateAndDraw();

    }

    public static void runRollingFrameFilter(ImagePlus inputImagePlus, int windowHalfWidth, String rollingMethod,
            String windowMode) {
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();

        ImagePlus tempImagePlus = new Duplicator().run(inputImagePlus);

        // Running through each frame, calculating the local average
        for (int f = 1; f <= inputImagePlus.getNFrames(); f++) {
            int firstFrame = 0;
            int lastFrame = 0;

            switch (windowMode) {
                case WindowModes.BOTH_SIDES:
                    firstFrame = Math.max(1, f - windowHalfWidth);
                    lastFrame = Math.min(nFrames, f + windowHalfWidth);
                    break;

                case WindowModes.PREVIOUS:
                    firstFrame = Math.max(1, f - windowHalfWidth);
                    lastFrame = Math.min(nFrames, f);
                    break;

                case WindowModes.FUTURE:
                    firstFrame = Math.max(1, f);
                    lastFrame = Math.min(nFrames, f + windowHalfWidth);
                    break;
            }

            // Creating a local substack
            ImagePlus currentSubstack = SubHyperstackMaker.makeSubhyperstack(tempImagePlus, "1-" + nChannels,
                    "1-" + nSlices, firstFrame + "-" + lastFrame);

            // Switching T and Z, so time (not Z) is averaged
            currentSubstack = Hyperstack_rearranger.reorderHyperstack(currentSubstack, "CTZ", true, false);

            // Applying average filter
            ZProjector zProjector = new ZProjector(currentSubstack);
            switch (rollingMethod) {
                case RollingMethods.AVERAGE:
                    zProjector.setMethod(ZProjector.AVG_METHOD);
                    break;
                case RollingMethods.MEDIAN:
                    zProjector.setMethod(ZProjector.MEDIAN_METHOD);
                    break;
                case RollingMethods.MINIMUM:
                    zProjector.setMethod(ZProjector.MIN_METHOD);
                    break;
                case RollingMethods.MAXIMUM:
                    zProjector.setMethod(ZProjector.MAX_METHOD);
                    break;
                case RollingMethods.STDEV:
                    zProjector.setMethod(ZProjector.SD_METHOD);
                    ImageTypeConverter.process(inputImagePlus, 32, ImageTypeConverter.ScalingModes.CLIP);
                    break;
                case RollingMethods.SUM:
                    zProjector.setMethod(ZProjector.SUM_METHOD);
                    break;
            }

            zProjector.setStartSlice(1);
            zProjector.setStopSlice(currentSubstack.getNSlices());
            zProjector.doHyperStackProjection(true);
            ImagePlus iplOut = zProjector.getProjection();

            // Switching T and Z back
            iplOut = Hyperstack_rearranger.reorderHyperstack(iplOut, "CTZ", true, false);

            // Adding the new image into outputImagePlus
            for (int z = 1; z <= iplOut.getNSlices(); z++) {
                for (int c = 1; c <= iplOut.getNChannels(); c++) {
                    inputImagePlus.setPosition(c, z, f);
                    iplOut.setPosition(c, z, 1);

                    inputImagePlus.setProcessor(iplOut.getProcessor());

                }
            }
        }

        inputImagePlus.setPosition(1, 1, 1);
        inputImagePlus.updateAndDraw();

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Apply intensity filters to an image (or image stack) in the workspace.  Filters are applied to each Z-stack independently (i.e. channels and timepoints do not interact with each other).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        double filterRadius = parameters.getValue(FILTER_RADIUS, workspace);
        double filterRadius2 = parameters.getValue(FILTER_RADIUS_2, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        String rollingMethod = parameters.getValue(ROLLING_METHOD, workspace);
        String windowIndices = parameters.getValue(WINDOW_INDICES, workspace);
        String contourContrast = parameters.getValue(CONTOUR_CONTRAST, workspace);

        if (calibratedUnits) {
            filterRadius = inputImagePlus.getCalibration().getRawX(filterRadius);
            filterRadius2 = inputImagePlus.getCalibration().getRawX(filterRadius2);
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = inputImagePlus.duplicate();

        // Applying smoothing filter
        switch (filterMode) {
            case FilterModes.MAXIMUM2D:
            case FilterModes.MEAN2D:
            case FilterModes.MEDIAN2D:
            case FilterModes.MINIMUM2D:
            case FilterModes.VARIANCE2D:
                writeStatus("Applying " + filterMode + " filter");
                apply2DFilter(inputImagePlus, filterMode, filterRadius);
                break;

            case FilterModes.GAUSSIAN3D:
            case FilterModes.MAXIMUM3D:
            case FilterModes.MEAN3D:
            case FilterModes.MEDIAN3D:
            case FilterModes.MINIMUM3D:
            case FilterModes.VARIANCE3D:
                writeStatus("Applying " + filterMode + " filter");
                apply3DFilter(inputImagePlus, filterMode, (float) filterRadius);
                break;

            case FilterModes.DOG2D:
                writeStatus("Applying " + filterMode + " filter");
                runDoG2DFilter(inputImagePlus, filterRadius, filterRadius2);
                break;

            case FilterModes.GAUSSIAN2D:
                writeStatus("Applying " + filterMode + " filter");
                runGaussian2DFilter(inputImagePlus, filterRadius);
                break;

            case FilterModes.GRADIENT2D:
                writeStatus("Applying " + filterMode + " filter");
                runGradient2DFilter(inputImagePlus, filterRadius);
                break;

            case FilterModes.LOG2DAPPROX:
                writeStatus("Applying " + filterMode + " filter");
                runDoG2DFilter(inputImagePlus, filterRadius, filterRadius * 1.6);
                break;

            case FilterModes.RIDGE_ENHANCEMENT:
                writeStatus("Applying 2D ridge enhancement filter");
                runRidgeEnhancement2DFilter(inputImagePlus, filterRadius, contourContrast);
                break;

            case FilterModes.ROLLING_FRAME:
                writeStatus("Applying rolling frame filter");
                runRollingFrameFilter(inputImagePlus, windowIndices, rollingMethod);
                break;

        }

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            // Reapplying the image in case it was an ImgLib2
            inputImage.setImagePlus(inputImagePlus);
            if (showOutput)
                inputImage.show();
        } else {
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.LOG2DAPPROX, FilterModes.ALL));
        parameters.add(new DoubleP(FILTER_RADIUS, this, 2d));
        parameters.add(new DoubleP(FILTER_RADIUS_2, this, 3d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new ChoiceP(ROLLING_METHOD, this, RollingMethods.AVERAGE, RollingMethods.ALL));
        parameters.add(new StringP(WINDOW_INDICES, this, "-1-1"));
        parameters.add(new ChoiceP(CONTOUR_CONTRAST, this, ContourContrast.DARK_LINE, ContourContrast.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (!parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.ROLLING_FRAME)) {
            returnedParameters.add(parameters.getParameter(FILTER_RADIUS));
            if (((String) parameters.getValue(FILTER_MODE, workspace)).equals(FilterModes.DOG2D))
                returnedParameters.add(parameters.getParameter(FILTER_RADIUS_2));
            returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        } else {
            returnedParameters.add(parameters.getParameter(ROLLING_METHOD));
            returnedParameters.add(parameters.getParameter(WINDOW_INDICES));

        }

        if (parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.RIDGE_ENHANCEMENT))
            returnedParameters.add(parameters.getParameter(CONTOUR_CONTRAST));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
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

    protected void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to apply filter to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the filter should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during the filtering process.  This image will be added to the workspace.");

        parameters.get(FILTER_MODE).setDescription("Filter to be applied to the image.<br><ul>"

                + "<li>\"" + FilterModes.DOG2D
                + "\" Difference of Gaussian filter in 2D.</li>"

                + "<li>\"" + FilterModes.GAUSSIAN2D + "\" </li>"

                + "<li>\"" + FilterModes.GAUSSIAN3D + "\" </li>"

                + "<li>\"" + FilterModes.GRADIENT2D + "\" </li>"

                + "<li>\"" + FilterModes.LOG2DAPPROX
                + "\" Approximation of a Laplacian of Gaussian filter in 2D.  This is used to enhance spot-like features of sizes similar to the setting for \""
                + FILTER_RADIUS + "\".</li>"

                + "<li>\"" + FilterModes.MAXIMUM2D + "\" </li>"

                + "<li>\"" + FilterModes.MAXIMUM3D + "\" </li>"

                + "<li>\"" + FilterModes.MEAN2D + "\" </li>"

                + "<li>\"" + FilterModes.MEAN3D + "\" </li>"

                + "<li>\"" + FilterModes.MEDIAN2D + "\" </li>"

                + "<li>\"" + FilterModes.MEDIAN3D + "\" </li>"

                + "<li>\"" + FilterModes.MINIMUM2D + "\" </li>"

                + "<li>\"" + FilterModes.MINIMUM3D + "\" </li>"

                + "<li>\"" + FilterModes.RIDGE_ENHANCEMENT
                + "\" Uses initial image processing steps from \"Ridge Detection\" plugin to enhance ridge-like structures.</li>"

                + "<li>\"" + FilterModes.ROLLING_FRAME
                + "\" Filters the image at each frame based on frames before and/after.  The frame window over which the statistics are calculated is user-controllable</li>"

                + "<li>\"" + FilterModes.VARIANCE2D + "\" </li>"

                + "<li>\"" + FilterModes.VARIANCE3D + "\" </li></ul>");

        parameters.get(FILTER_RADIUS).setDescription(
                "Range the filter is calculated over.  Often also referred to as \"sigma\".  Value specified in pixel units, unless \"calibrated units\" is enabled.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "Choose if filter radius is specified in pixel (set to \"false\") or calibrated (set to \"true\") units.  What units are used are controlled from \"Input control\".");

        parameters.get(ROLLING_METHOD).setDescription("Statistic to apply for rolling frame filtering.");

        parameters.get(WINDOW_INDICES).setDescription("When \"" + FILTER_MODE + "\" is set to \""
                + FilterModes.ROLLING_FRAME
                + "\", the rolling frame statistic will be calculated for each frame using these relative frames (i.e. with indices set to \"-1,1\" the statistic would be calculated based on the frames immediately before and after).");

        parameters.get(CONTOUR_CONTRAST).setDescription("When \"" + FILTER_MODE + "\" is set to \""
                + FilterModes.RIDGE_ENHANCEMENT
                + "\", this parameter controls whether the ridges to be enhanced are bright (brighter than the background) or dark (darker than the background).");

    }
}
