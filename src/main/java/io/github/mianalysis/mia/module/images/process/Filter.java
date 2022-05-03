package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImgPlusTools;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Created by Stephen on 30/05/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class Filter<T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_32BIT = "Output 32-bit image";
    public static final String FILTER_SEPARATOR = "Filter controls";
    public static final String APPLICATION_MODE = "Application mode";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    // public static final String ROLLING_METHOD = "Rolling filter method";
    // public static final String WINDOW_INDICES = "Window indices";
    // public static final String CONTOUR_CONTRAST = "Contour contrast";

    public Filter(Modules modules) {
        super("Filter", modules);
        il2Support = IL2Support.PARTIAL;

    }

    public interface ApplicationModes {
        String TWO_D = "2D (slice-by-slice)";
        String THREE_D = "3D";

        String[] ALL = new String[] { TWO_D, THREE_D };

    }

    public interface FilterModes {
        String DOG = "Difference of Gaussian";
        String GAUSSIAN = "Gaussian";
        // String GRADIENT = "Gradient";
        String MAXIMUM = "Maximum";
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String MINIMUM = "Minimum";
        // String RIDGE_ENHANCEMENT = "Ridge enhancement";
        // String ROLLING_FRAME = "Rolling frame";
        String VARIANCE = "Variance";

        // String[] ALL = new String[] { DOG, GAUSSIAN, GRADIENT, MAXIMUM, MEAN, MEDIAN,
        // MINIMUM, RIDGE_ENHANCEMENT,
        // ROLLING_FRAME, VARIANCE };

        String[] ALL = new String[] { DOG, GAUSSIAN, MAXIMUM, MEAN, MEDIAN, MINIMUM, VARIANCE };

    }

    // public interface RollingMethods {
    // String AVERAGE = "Average";
    // String MEDIAN = "Median";
    // String MINIMUM = "Minimum";
    // String MAXIMUM = "Maximum";
    // String STDEV = "Standard deviation";
    // String SUM = "Sum";

    // String[] ALL = new String[] { AVERAGE, MEDIAN, MINIMUM, MAXIMUM, STDEV, SUM
    // };

    // }

    // public interface WindowModes {
    // String BOTH_SIDES = "Both sides";
    // String PREVIOUS = "Previous only";
    // String FUTURE = "Future only";

    // String[] ALL = new String[] { BOTH_SIDES, PREVIOUS, FUTURE };

    // }

    // public interface ContourContrast {
    // String DARK_LINE = "Dark line";
    // String LIGHT_LINE = "Light line";

    // String[] ALL = new String[] { DARK_LINE, LIGHT_LINE };

    // }

    public static Class<? extends Op> getFilterClass(String filter) {
        switch (filter) {
            case FilterModes.DOG:
                return Ops.Filter.DoG.class;
            case FilterModes.GAUSSIAN:
                return Ops.Filter.Gauss.class;
            case FilterModes.MAXIMUM:
                return Ops.Filter.Max.class;
            case FilterModes.MEAN:
                return Ops.Filter.Mean.class;
            case FilterModes.MEDIAN:
                return Ops.Filter.Median.class;
            case FilterModes.MINIMUM:
                return Ops.Filter.Min.class;
            case FilterModes.VARIANCE:
                return Ops.Filter.Variance.class;
        }

        MIA.log.writeError("Filter not found");
        return null;

    }

    public static <T extends RealType<T> & NativeType<T>> void applyFilterInSitu(Image<T> image,
            String applicationMode, String filterMode,
            long filterRadius) {
        OpService ops = MIA.ijService.getContext().getService(OpService.class);

        Shape shape = new HyperSphereShape(filterRadius);
        Class<? extends Op> filterClass = getFilterClass(filterMode);
        if (filterClass == null)
            return;

        ImgPlus<T> imgIn = image.getImgPlus();
        long[] dims = ImgPlusTools.getDimensionsXYCZT(imgIn);

        // We only need create the filter substack once, as it'll be overwritten each
        // iteration
        int nChannels = imgIn.dimensionIndex(Axes.CHANNEL) == -1 ? 0 : 1;
        int nFrames = imgIn.dimensionIndex(Axes.TIME) == -1 ? 0 : 1;
        int nSlices;
        if (applicationMode.equals(ApplicationModes.TWO_D)) {
            nSlices = imgIn.dimensionIndex(Axes.Z) == -1 ? 0 : 1;
        } else {
            nSlices = imgIn.dimensionIndex(Axes.Z) == -1 ? 0 : (int) dims[3];
            dims[3] = 1;
        }

        ImgPlus<T> substackFiltered = ImgPlusTools.createNewImgPlus(imgIn, nChannels, nSlices, nFrames,
                (T) imgIn.firstElement());

        for (int c = 0; c < dims[2]; c++) {
            int[] cRange = new int[] { c, c };

            for (int t = 0; t < dims[4]; t++) {
                int[] tRange = new int[] { t, t };

                for (int z = 0; z < dims[3]; z++) {
                    int[] zRange;
                    switch (applicationMode) {
                        case ApplicationModes.TWO_D:
                        default:
                            zRange = new int[] { z, z };
                            break;
                        case ApplicationModes.THREE_D:
                            zRange = new int[] { 0, -1 };
                            break;
                    }

                    long[][] interval = ImgPlusTools.getSubHyperstackInterval(imgIn, cRange, zRange, tRange);

                    RandomAccessibleInterval<T> substackIn = Views.interval(imgIn, interval[0], interval[1]);

                    // Initialising filter
                    UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> filter;
                    switch (filterMode) {
                        case FilterModes.GAUSSIAN:
                        default:
                            filter = Computers.unary(ops, filterClass, substackFiltered, substackIn, filterRadius);
                            break;
                        case FilterModes.DOG:
                            filter = Computers.unary(ops, filterClass, substackFiltered, substackIn, filterRadius,
                                    filterRadius * 1.6);
                            break;
                        case FilterModes.MAXIMUM:
                        case FilterModes.MEAN:
                        case FilterModes.MEDIAN:
                        case FilterModes.MINIMUM:
                        case FilterModes.VARIANCE:
                            filter = Computers.unary(ops, filterClass, substackFiltered, substackIn, shape);
                            break;
                    }

                    // Applying filter
                    ops.slice(substackFiltered, substackIn, filter, 0, 1, 2);

                    // Overwriting input
                    LoopBuilder.setImages(substackIn, substackFiltered)
                            .forEachPixel((i, f) -> i.setReal(f.getRealDouble()));

                }
            }
        }
    }

    public static <T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> Image<R> apply2DFilterCreate(
            Image<T> image,
            String applicationMode, String filterMode, long filterRadius, String outputImageName, boolean output32Bit) {
        OpService ops = MIA.ijService.getContext().getService(OpService.class);

        Shape shape = new HyperSphereShape(filterRadius);
        Class<? extends Op> filterClass = getFilterClass(filterMode);
        if (filterClass == null)
            return null;

        ImgPlus<T> imgIn = image.getImgPlus();
        long[] dims = ImgPlusTools.getDimensionsXYCZT(imgIn);

        // Creating duplicate image
        R typeOut = output32Bit ? (R) new FloatType() : (R) imgIn.firstElement();
        ImgPlus<R> imgOut = ImgPlusTools.createNewImgPlus(imgIn, typeOut);
        Image<R> imageOut = ImageFactory.createImage(outputImageName, imgOut);

        if (applicationMode.equals(ApplicationModes.THREE_D))
            dims[3] = 1;

        for (int c = 0; c < dims[2]; c++) {
            int[] cRange = new int[] { c, c };

            for (int t = 0; t < dims[4]; t++) {
                int[] tRange = new int[] { t, t };

                for (int z = 0; z < dims[3]; z++) {
                    int[] zRange;
                    switch (applicationMode) {
                        case ApplicationModes.TWO_D:
                        default:
                            zRange = new int[] { z, z };
                            break;
                        case ApplicationModes.THREE_D:
                            zRange = new int[] { 0, -1 };
                            break;
                    }

                    long[][] interval = ImgPlusTools.getSubHyperstackInterval(imgIn, cRange, zRange, tRange);

                    // If the output is 32 bit, ensuring the input is also 32 bit. Otherwise, the
                    // output should already match the input.
                    Converter<T, R> converter = (i, o) -> o.setReal(i.getRealDouble());
                    RandomAccessibleInterval<R> substackIn;
                    if (output32Bit && imgIn.firstElement() != new FloatType())
                        substackIn = Converters.convert(
                                (RandomAccessibleInterval<T>) Views.interval(imgIn, interval[0], interval[1]),
                                converter, typeOut);
                    else
                        substackIn = (RandomAccessibleInterval<R>) Views.interval(imgIn, interval[0], interval[1]);
                    RandomAccessibleInterval<R> substackOut = Views.interval(imgOut, interval[0], interval[1]);

                    // Initialising filter
                    UnaryComputerOp<RandomAccessibleInterval<R>, RandomAccessibleInterval<R>> filter;
                    switch (filterMode) {
                        case FilterModes.GAUSSIAN:
                        default:
                            filter = Computers.unary(ops, filterClass, substackOut, substackIn, filterRadius);
                            break;
                        case FilterModes.DOG:
                            filter = Computers.unary(ops, filterClass, substackOut, substackIn, (double) filterRadius,
                                    (double) filterRadius * 1.6);
                            break;
                        case FilterModes.MAXIMUM:
                        case FilterModes.MEAN:
                        case FilterModes.MEDIAN:
                        case FilterModes.MINIMUM:
                        case FilterModes.VARIANCE:
                            filter = Computers.unary(ops, filterClass, substackOut, substackIn, shape);
                            break;
                    }

                    // Applying filter
                    ops.slice(substackOut, substackIn, filter, 0, 1, 2);

                }
            }
        }

        return imageOut;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Apply intensity filters to an image in the workspace."
                + "<br>Note: 3D median filter is currently incompatible with 5D hyperstacks";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image<T> inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean output32Bit = parameters.getValue(OUTPUT_32BIT);
        String applicationMode = parameters.getValue(APPLICATION_MODE);
        String filterMode = parameters.getValue(FILTER_MODE);
        double filterRadius = parameters.getValue(FILTER_RADIUS);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        // String rollingMethod = parameters.getValue(ROLLING_METHOD);
        // String windowIndices = parameters.getValue(WINDOW_INDICES);
        // String contourContrast = parameters.getValue(CONTOUR_CONTRAST);

        if (calibratedUnits)
            filterRadius = inputImagePlus.getCalibration().getRawX(filterRadius);

        long filterRadiusL = (long) Math.round(filterRadius);

        if (applyToInput) {
            applyFilterInSitu(inputImage, applicationMode, filterMode, filterRadiusL);

            if (showOutput)
                inputImage.showImage();

        } else {
            Image<R> outputImage = apply2DFilterCreate(inputImage, applicationMode, filterMode, filterRadiusL,
                    outputImageName, output32Bit);
            MIA.log.writeDebug(outputImage.getImgPlus().firstElement().getClass());

            if (showOutput)
                outputImage.showImage();

            workspace.addImage(outputImage);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new BooleanP(OUTPUT_32BIT, this, false));
        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(APPLICATION_MODE, this, ApplicationModes.TWO_D, ApplicationModes.ALL));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.MEDIAN, FilterModes.ALL));
        parameters.add(new DoubleP(FILTER_RADIUS, this, 2d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        // parameters.add(new ChoiceP(ROLLING_METHOD, this, RollingMethods.AVERAGE,
        // RollingMethods.ALL));
        // parameters.add(new StringP(WINDOW_INDICES, this, "-1-1"));
        // parameters.add(new ChoiceP(CONTOUR_CONTRAST, this, ContourContrast.DARK_LINE,
        // ContourContrast.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            returnedParameters.add(parameters.getParameter(OUTPUT_32BIT));
        }

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLICATION_MODE));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        // if (!parameters.getValue(FILTER_MODE).equals(FilterModes.ROLLING_FRAME)) {
        returnedParameters.add(parameters.getParameter(FILTER_RADIUS));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        // } else {
        // returnedParameters.add(parameters.getParameter(ROLLING_METHOD));
        // returnedParameters.add(parameters.getParameter(WINDOW_INDICES));

        // }

        // if (parameters.getValue(FILTER_MODE).equals(FilterModes.RIDGE_ENHANCEMENT))
        // returnedParameters.add(parameters.getParameter(CONTOUR_CONTRAST));

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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    protected void addParameterDescriptions() {
        // parameters.get(INPUT_IMAGE).setDescription("Image to apply filter to.");

        // parameters.get(APPLY_TO_INPUT).setDescription(
        // "Select if the filter should be applied directly to the input image, or if it
        // should be applied to a duplicate, then stored as a different image in the
        // workspace.");

        // parameters.get(OUTPUT_IMAGE).setDescription(
        // "Name of the output image created during the filtering process. This image
        // will be added to the workspace.");

        // parameters.get(FILTER_MODE).setDescription("Filter to be applied to the
        // image.<br><ul>"

        // + "<li>\"" + FilterModes.DOG
        // + "\" Difference of Gaussian filter (2D) Used to enhance spot-like features
        // of sizes similar to the setting for \""
        // + FILTER_RADIUS + "\".</li>"

        // + "<li>\"" + FilterModes.GAUSSIAN + "\" </li>"

        // + "<li>\"" + FilterModes.GAUSSIAN3D + "\" </li>"

        // + "<li>\"" + FilterModes.GRADIENT + "\" </li>"

        // + "<li>\"" + FilterModes.MAXIMUM + "\" </li>"

        // + "<li>\"" + FilterModes.MAXIMUM3D + "\" </li>"

        // + "<li>\"" + FilterModes.MEAN + "\" </li>"

        // + "<li>\"" + FilterModes.MEAN3D + "\" </li>"

        // + "<li>\"" + FilterModes.MEDIAN + "\" </li>"

        // + "<li>\"" + FilterModes.MEDIAN3D + "\" </li>"

        // + "<li>\"" + FilterModes.MINIMUM + "\" </li>"

        // + "<li>\"" + FilterModes.MINIMUM3D + "\" </li>"

        // + "<li>\"" + FilterModes.RIDGE_ENHANCEMENT
        // + "\" Uses initial image processing steps from \"Ridge Detection\" plugin to
        // enhance ridge-like structures.</li>"

        // + "<li>\"" + FilterModes.ROLLING_FRAME
        // + "\" Filters the image at each frame based on frames before and/after. The
        // frame window over which the statistics are calculated is
        // user-controllable</li>"

        // + "<li>\"" + FilterModes.VARIANCE + "\" </li>"

        // + "<li>\"" + FilterModes.VARIANCE3D + "\" </li></ul>");

        // parameters.get(FILTER_RADIUS).setDescription(
        // "Range the filter is calculated over. Often also referred to as \"sigma\".
        // Value specified in pixel units, unless \"calibrated units\" is enabled.");

        // parameters.get(CALIBRATED_UNITS).setDescription(
        // "Choose if filter radius is specified in pixel (set to \"false\") or
        // calibrated (set to \"true\") units. What units are used are controlled from
        // \"Input control\".");

        // parameters.get(ROLLING_METHOD).setDescription("Statistic to apply for rolling
        // frame filtering.");

        // parameters.get(WINDOW_INDICES).setDescription("When \"" + FILTER_MODE + "\"
        // is set to \""
        // + FilterModes.ROLLING_FRAME
        // + "\", the rolling frame statistic will be calculated for each frame using
        // these relative frames (i.e. with indices set to \"-1,1\" the statistic would
        // be calculated based on the frames immediately before and after).");

        // parameters.get(CONTOUR_CONTRAST).setDescription("When \"" + FILTER_MODE + "\"
        // is set to \""
        // + FilterModes.RIDGE_ENHANCEMENT
        // + "\", this parameter controls whether the ridges to be enhanced are bright
        // (brighter than the background) or dark (darker than the background).");

    }
}
