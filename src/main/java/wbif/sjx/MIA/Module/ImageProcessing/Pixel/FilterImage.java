// TODO: Could move rolling frame filter to Common library's filters package
// TODO: MedianFilter3D isn't currently working for multiple channels

package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import fiji.stacks.Hyperstack_rearranger;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.*;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.strel.DiskStrel;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Filters.DoG;
import wbif.sjx.common.Filters.RidgeEnhancement;


/**
 * Created by sc13967 on 30/05/2017.
 */
public class FilterImage extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_SEPARATOR = "Filter controls";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String ROLLING_METHOD = "Rolling filter method";
    public static final String WINDOW_MODE = "Window mode";
    public static final String WINDOW_HALF_WIDTH = "Window half width (frames)";

    public FilterImage(ModuleCollection modules) {
        super("Filter image",modules);
    }


    public interface FilterModes {
        String DOG2D = "Difference of Gaussian 2D";
        String GAUSSIAN2D = "Gaussian 2D"; // Tested
        String GAUSSIAN3D = "Gaussian 3D"; // Tested
        String GRADIENT2D = "Gradient 2D";
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

        String[] ALL = new String[]{
                DOG2D,GAUSSIAN2D,GAUSSIAN3D,GRADIENT2D,MAXIMUM2D,MAXIMUM3D,MEAN2D,MEAN3D,MEDIAN2D,MEDIAN3D,
                MINIMUM2D,MINIMUM3D,RIDGE_ENHANCEMENT,ROLLING_FRAME,VARIANCE2D,VARIANCE3D};

    }

    public interface RollingMethods {
        String AVERAGE = "Average";
        String MINIMUM = "Minimum";
        String MAXIMUM = "Maximum";

        String[] ALL = new String[]{AVERAGE,MINIMUM,MAXIMUM};

    }

    public interface WindowModes {
        String BOTH_SIDES = "Both sides";
        String PREVIOUS = "Previous only";
        String FUTURE = "Future only";

        String[] ALL = new String[]{BOTH_SIDES,PREVIOUS,FUTURE};

    }


    public static void apply2DFilter(ImagePlus inputImagePlus, String filterMode, double filterRadius) {
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

        RankFilters filter = new RankFilters();
        int count = 0;
        int total = inputImagePlus.getNChannels()*inputImagePlus.getNSlices()*inputImagePlus.getNFrames();
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    filter.rank(inputImagePlus.getProcessor(),filterRadius,rankFilter);

                }
            }
        }
        inputImagePlus.setPosition(1,1,1);
    }

    public static void apply3DFilter(ImagePlus inputImagePlus, String filterMode, float filterRadius) {
        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
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
        if (filterMode.equals(FilterModes.VARIANCE3D)) {
            ImageTypeConverter.applyConversion(inputImagePlus,32,ImageTypeConverter.ScalingModes.CLIP);
        }

        int count = 0;
        int total = inputImagePlus.getNChannels()*inputImagePlus.getNSlices()*inputImagePlus.getNFrames();
        for (int c=1;c<=nChannels;c++) {
            for (int t = 1; t <=nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(inputImagePlus, c+"-"+c, "1-"+nSlices, t+"-"+t);
                ImageStack istFilt = Filters3D.filter(iplOrig.getStack(), filter, filterRadius, filterRadius, filterRadius);

                for (int z = 1; z <= istFilt.getSize(); z++) {
                    inputImagePlus.setPosition(c,z,t);
                    ImageProcessor iprOrig = inputImagePlus.getProcessor();
                    ImageProcessor iprFilt = istFilt.getProcessor(z);

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            iprOrig.setf(x, y, iprFilt.getf(x, y));
                        }
                    }
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);
        inputImagePlus.updateChannelAndDraw();

    }

    public static void runGaussian2DFilter(ImagePlus imagePlus, double sigma) {
        int count = 0;
        int total = imagePlus.getNChannels()*imagePlus.getNSlices()*imagePlus.getNFrames();
        for (int z = 1; z <= imagePlus.getNSlices(); z++) {
            for (int c = 1; c <= imagePlus.getNChannels(); c++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    imagePlus.setPosition(c, z, t);
                    imagePlus.getProcessor().blurGaussian(sigma);
                }
            }
        }
        imagePlus.setPosition(1,1,1);
    }

    public static void runGradient2DFilter(ImagePlus imagePlus, double sigma) {
        DiskStrel strel = DiskStrel.fromRadius((int) Math.round(sigma));
        int count = 0;
        int total = imagePlus.getNChannels()*imagePlus.getNSlices()*imagePlus.getNFrames();
        for (int z = 1; z <= imagePlus.getNSlices(); z++) {
            for (int c = 1; c <= imagePlus.getNChannels(); c++) {
                for (int t = 1; t <= imagePlus.getNFrames(); t++) {
                    imagePlus.setPosition(c, z, t);
                    imagePlus.setProcessor(Morphology.gradient(imagePlus.getProcessor(),strel));
                }
            }
        }
        imagePlus.setPosition(1,1,1);
    }

    public static void runRollingFrameFilter(ImagePlus inputImagePlus, int windowHalfWidth, String rollingMethod, String windowMode) {
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();

        ImagePlus tempImagePlus = new Duplicator().run(inputImagePlus);

        // Running through each frame, calculating the local average
        int count = 0;
        for (int f=1;f<=inputImagePlus.getNFrames();f++) {
            int firstFrame = 0;
            int lastFrame = 0;

            switch (windowMode) {
                case WindowModes.BOTH_SIDES:
                    firstFrame = Math.max(1,f-windowHalfWidth);
                    lastFrame = Math.min(nFrames,f+windowHalfWidth);
                    break;

                case WindowModes.PREVIOUS:
                    firstFrame = Math.max(1,f-windowHalfWidth);
                    lastFrame = Math.min(nFrames,f);
                    break;

                case WindowModes.FUTURE:
                    firstFrame = Math.max(1,f);
                    lastFrame = Math.min(nFrames,f+windowHalfWidth);
                    break;
            }

            // Creating a local substack
            ImagePlus currentSubstack = SubHyperstackMaker.makeSubhyperstack(tempImagePlus,"1-"+nChannels,
                    "1-"+nSlices,firstFrame+"-"+lastFrame);

            // Switching T and Z, so time (not Z) is averaged
            currentSubstack = Hyperstack_rearranger.reorderHyperstack(currentSubstack,"CTZ",true,false);

            // Applying average filter
            ZProjector zProjector = new ZProjector(currentSubstack);
            switch (rollingMethod) {
                case RollingMethods.AVERAGE:
                    zProjector.setMethod(ZProjector.AVG_METHOD);
                    break;

                case RollingMethods.MINIMUM:
                    zProjector.setMethod(ZProjector.MIN_METHOD);
                    break;

                case RollingMethods.MAXIMUM:
                    zProjector.setMethod(ZProjector.MAX_METHOD);
                    break;
            }

            zProjector.setStartSlice(1);
            zProjector.setStopSlice(currentSubstack.getNSlices());
            zProjector.doHyperStackProjection(true);
            ImagePlus iplOut = zProjector.getProjection();

            // Switching T and Z back
            iplOut = Hyperstack_rearranger.reorderHyperstack(iplOut,"CTZ",true,false);

            // Adding the new image into outputImagePlus
            for (int z = 1; z <= iplOut.getNSlices(); z++) {
                for (int c = 1; c <= iplOut.getNChannels(); c++) {
                    inputImagePlus.setPosition(c,z,f);
                    iplOut.setPosition(c,z,1);

                    inputImagePlus.setProcessor(iplOut.getProcessor());

                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "3D median filter currently incompatible with 5D hyperstacks";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String filterMode = parameters.getValue(FILTER_MODE);
        double filterRadius = parameters.getValue(FILTER_RADIUS);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        String rollingMethod = parameters.getValue(ROLLING_METHOD);
        int windowHalfWidth = parameters.getValue(WINDOW_HALF_WIDTH);
        String windowMode = parameters.getValue(WINDOW_MODE);

        if (calibratedUnits) {
            filterRadius = inputImagePlus.getCalibration().getRawX(filterRadius);
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = inputImagePlus.duplicate();}

        // Applying smoothing filter
        switch (filterMode) {
            case FilterModes.MAXIMUM2D:
            case FilterModes.MEAN2D:
            case FilterModes.MEDIAN2D:
            case FilterModes.MINIMUM2D:
            case FilterModes.VARIANCE2D:
                writeMessage("Applying "+filterMode+" filter");
                apply2DFilter(inputImagePlus,filterMode,filterRadius);
                break;

            case FilterModes.MAXIMUM3D:
            case FilterModes.MEAN3D:
            case FilterModes.MEDIAN3D:
            case FilterModes.MINIMUM3D:
            case FilterModes.VARIANCE3D:
                writeMessage("Applying "+filterMode+" filter");
                apply3DFilter(inputImagePlus,filterMode,(float) filterRadius);
                break;

            case FilterModes.DOG2D:
                writeMessage("Applying "+filterMode+" filter");
                DoG.run(inputImagePlus,filterRadius,true);
                break;

            case FilterModes.GAUSSIAN2D:
                writeMessage("Applying "+filterMode+" filter");
                runGaussian2DFilter(inputImagePlus,filterRadius);
                break;

            case FilterModes.GAUSSIAN3D:
                writeMessage("Applying "+filterMode+" filter");
                GaussianBlur3D.blur(inputImagePlus,filterRadius,filterRadius,filterRadius);
                break;

            case FilterModes.GRADIENT2D:
                writeMessage("Applying "+filterMode+" filter");
                runGradient2DFilter(inputImagePlus,filterRadius);
                break;

            case FilterModes.RIDGE_ENHANCEMENT:
                writeMessage("Applying 3D median filter");
                RidgeEnhancement.run(inputImagePlus,(float) filterRadius, true);
                break;

            case FilterModes.ROLLING_FRAME:
                writeMessage("Applying rolling frame filter");
                runRollingFrameFilter(inputImagePlus,windowHalfWidth,rollingMethod,windowMode);
                break;

        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

        } else {
            if (showOutput) inputImage.showImage();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply filter to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true, "Select if the filter should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output image created during the filtering process.  This image will be added to the workspace."));
        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));
        parameters.add(new ChoiceP(FILTER_MODE, this,FilterModes.DOG2D,FilterModes.ALL, "Filter to be applied to the image.  Some filters have separate 2D and 3D variants."));
        parameters.add(new DoubleP(FILTER_RADIUS, this, 2d, "Range the filter is calculated over.  Often also referred to as \"sigma\".  Value specified in pixel units, unless \"calibrated units\" is enabled."));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this,false, "Choose if filter radius is specified in pixel (set to \"false\") or calibrated (set to \"true\" units.  What units are used are controlled from \"Input control\"."));
        parameters.add(new ChoiceP(ROLLING_METHOD, this,RollingMethods.AVERAGE,RollingMethods.ALL));
        parameters.add(new IntegerP(WINDOW_HALF_WIDTH,this,1));
        parameters.add(new ChoiceP(WINDOW_MODE,this,WindowModes.BOTH_SIDES,WindowModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (!parameters.getValue(FILTER_MODE).equals(FilterModes.ROLLING_FRAME)) {
            returnedParameters.add(parameters.getParameter(FILTER_RADIUS));
            returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        } else {
            returnedParameters.add(parameters.getParameter(ROLLING_METHOD));
            returnedParameters.add(parameters.getParameter(WINDOW_HALF_WIDTH));
            returnedParameters.add(parameters.getParameter(WINDOW_MODE));

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
