// TODO: Could move rolling frame filter to Common library's filters package
// TODO: MedianFilter3D isn't currently working for multiple channels

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.*;
import ij.plugin.filter.RankFilters;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.strel.DiskStrel;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Filters.DoG;
import wbif.sjx.common.Filters.RidgeEnhancement;
import wbif.sjx.common.Process.IntensityMinMax;


/**
 * Created by sc13967 on 30/05/2017.
 */
public class FilterImage extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String WINDOW_HALF_WIDTH = "Window half width (frames)";
    public static final String SHOW_IMAGE = "Show image";

    public interface FilterModes {
        String DOG2D = "Difference of Gaussian 2D";
        String GAUSSIAN2D = "Gaussian 2D"; // Tested
        String GAUSSIAN3D = "Gaussian 3D"; // Tested
        String GRADIENT2D = "Gradient 2D";
        String MEDIAN2D = "Median 2D";
        String MEDIAN3D = "Median 3D";
        String RIDGE_ENHANCEMENT = "Ridge enhancement 2D";
        String ROLLING_FRAME = "Rolling frame";
        String VARIANCE2D = "Variance 2D";

        String[] ALL = new String[]{
                DOG2D,GAUSSIAN2D,GAUSSIAN3D,GRADIENT2D,MEDIAN2D,MEDIAN3D,RIDGE_ENHANCEMENT, ROLLING_FRAME,VARIANCE2D};

    }

    public void applyRankFilterToStack(ImagePlus inputImagePlus, int rankFilter, double filterRadius) {
        RankFilters filter = new RankFilters();

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

    public static void runGaussian2DFilter(ImagePlus imagePlus, double sigma) {
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

    /**
     * Fiji's Median3D filter doesn't currently support 5D hyperstacks; therefore, it is necessary to split the
     * channels, then recombine them.
     */
    public static void runMedian3DFilter(ImagePlus inputImagePlus, float filterRadius) {
//        ImagePlus[] ipls = ChannelSplitter.split(inputImagePlus);
//
//        // Running Median3D on each channel
//        for (int i=0;i<inputImagePlus.getNChannels();i++) {
//            new ImagePlus("fdf",ChannelSplitter.getChannel(inputImagePlus,i+1)).show();
//            ImageStack ist = Filters3D.filter(ChannelSplitter.getChannel(inputImagePlus,i+1),Filters3D.MEDIAN,
//                    filterRadius, filterRadius, filterRadius);
//            ipls[i].setStack(ist);
//        }
//
//        // Re-combining the channels
//        inputImagePlus.setProcessor(RGBStackMerge.mergeChannels(inputImagePlus,false).getProcessor());

//        inputImagePlus.setPosition(1,1,1);

        inputImagePlus.setStack(Filters3D.filter(inputImagePlus.getImageStack(), Filters3D.MEDIAN,
                filterRadius,filterRadius, filterRadius));


    }

    public static ImagePlus runRollingFrameFilter(ImagePlus inputImagePlus, int windowHalfWidth) {
        // Creating new hyperstack
        String type = "8-bit";
        switch (inputImagePlus.getBitDepth()) {
            case 8:
                type = "8-bit";
                break;

            case 16:
                type = "16-bit";
                break;

            case 32:
                type = "32-bit";
                break;
        }

        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();

        ImagePlus outputImagePlus = IJ.createImage(inputImagePlus.getTitle(),type,width,height,nChannels,nSlices,nFrames);

        // Running through each frame, calculating the local average
        for (int f=1;f<=nFrames;f++) {
            int firstFrame = Math.max(1,f-windowHalfWidth);
            int lastFrame = Math.min(nFrames,f+windowHalfWidth);

            // Creating a local substack
            ImagePlus currentSubstack = SubHyperstackMaker.makeSubhyperstack(inputImagePlus,"1-"+nChannels,
                    "1-"+nSlices,firstFrame+"-"+lastFrame);

            // Switching T and Z, so time (not Z) is averaged
            currentSubstack = Hyperstack_rearranger.reorderHyperstack(currentSubstack,"CTZ",true,false);

            // Applying average filter
            ZProjector zProjector = new ZProjector(currentSubstack);
            zProjector.setMethod(ZProjector.AVG_METHOD);
            zProjector.setStartSlice(1);
            zProjector.setStopSlice(currentSubstack.getNSlices());
            zProjector.doHyperStackProjection(true);
            ImagePlus iplOut = zProjector.getProjection();

            // Switching T and Z back
            iplOut = Hyperstack_rearranger.reorderHyperstack(iplOut,"CTZ",true,false);

            // Adding the new image into outputImagePlus
            for (int z = 1; z <= iplOut.getNSlices(); z++) {
                for (int c = 1; c <= iplOut.getNChannels(); c++) {
                    outputImagePlus.setPosition(c,z,f);
                    iplOut.setPosition(c,z,1);

                    outputImagePlus.setProcessor(iplOut.getProcessor());

                }
            }
        }

        outputImagePlus.setPosition(1,1,1);

        return outputImagePlus;

    }

    @Override
    public String getTitle() {
        return "Filter image";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
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
        int windowHalfWidth = parameters.getValue(WINDOW_HALF_WIDTH);

        if (calibratedUnits) {
            filterRadius = inputImagePlus.getCalibration().getRawX(filterRadius);
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Applying smoothing filter
        switch (filterMode) {
            case FilterModes.DOG2D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 2D difference of Gaussian filter (radius = " + filterRadius + " px)");
                DoG.run(inputImagePlus,filterRadius,true);
                break;

            case FilterModes.GAUSSIAN2D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 2D Gaussian filter (radius = " + filterRadius + " px)");
                runGaussian2DFilter(inputImagePlus,filterRadius);
                break;

            case FilterModes.GAUSSIAN3D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 3D Gaussian filter (radius = " + filterRadius + " px)");
                GaussianBlur3D.blur(inputImagePlus,filterRadius,filterRadius,filterRadius);
                break;

            case FilterModes.GRADIENT2D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 2D Gradient filter (radius = " + filterRadius + " px)");
                    runGradient2DFilter(inputImagePlus,filterRadius);
                break;

            case FilterModes.MEDIAN2D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 2D median filter (radius = " + filterRadius + " px)");
                applyRankFilterToStack(inputImagePlus,RankFilters.MEDIAN,filterRadius);
                break;

            case FilterModes.MEDIAN3D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 3D median filter (radius = " + filterRadius + " px)");
                runMedian3DFilter(inputImagePlus,(float) filterRadius);
                break;

            case FilterModes.RIDGE_ENHANCEMENT:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 3D median filter (radius = " + filterRadius + " px)");
                RidgeEnhancement.run(inputImagePlus,(float) filterRadius, true);
                break;

            case FilterModes.ROLLING_FRAME:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying rolling frame filter (window half width = "+windowHalfWidth+" frames)");
                inputImagePlus = runRollingFrameFilter(inputImagePlus,windowHalfWidth);
                break;

            case FilterModes.VARIANCE2D:
                if (verbose) System.out.println("[" + moduleName + "] " +
                        "Applying 2D variance filter (radius = " + filterRadius + " px)");
                applyRankFilterToStack(inputImagePlus,RankFilters.VARIANCE,filterRadius);
                break;

        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            outputImage.getImagePlus().setPosition(1,1,1);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                ImagePlus dispIpl = new Duplicator().run(outputImage.getImagePlus());
                IntensityMinMax.run(dispIpl,true);
                dispIpl.show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
                IntensityMinMax.run(dispIpl,true);
                dispIpl.show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(FILTER_MODE, Parameter.CHOICE_ARRAY,FilterModes.DOG2D,FilterModes.ALL));
        parameters.add(new Parameter(FILTER_RADIUS, Parameter.DOUBLE,2d));
        parameters.add(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(WINDOW_HALF_WIDTH,Parameter.INTEGER,1));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (!parameters.getValue(FILTER_MODE).equals(FilterModes.ROLLING_FRAME)) {
            returnedParameters.add(parameters.getParameter(FILTER_RADIUS));
            returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        } else {
            returnedParameters.add(parameters.getParameter(WINDOW_HALF_WIDTH));

        }

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
