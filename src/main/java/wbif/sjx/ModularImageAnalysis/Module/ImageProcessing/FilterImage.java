// TODO: Could move rolling frame filter to Common library's filters package

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.*;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Filters.DoG;


/**
 * Created by sc13967 on 30/05/2017.
 */
public class FilterImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius (px)";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String WINDOW_HALF_WIDTH = "Window half width (frames)";
    public static final String SHOW_IMAGE = "Show image";

    public interface FilterModes {
        String DOG2D = "Difference of Gaussian 2D";
        String GAUSSIAN2D = "Gaussian 2D";
        String GAUSSIAN3D = "Gaussian 3D";
        String MEDIAN3D = "Median 3D";
        String ROLLING_FRAME = "Rolling frame";

        String[] ALL = new String[]{DOG2D,GAUSSIAN2D,GAUSSIAN3D,MEDIAN3D,ROLLING_FRAME};

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
            ImagePlus currentSubstack = SubHyperstackMaker.makeSubhyperstack(inputImagePlus,"1-"+nChannels,"1-"+nSlices,firstFrame+"-"+lastFrame);

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
        return "+++INCOMPLETE+++";
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
                if (verbose) System.out.println("[" + moduleName + "] Applying 2D difference of Gaussian filter (radius = " + filterRadius + " px)");
                DoG.run(inputImagePlus,filterRadius,true);
                break;

            case FilterModes.GAUSSIAN2D:
                if (verbose) System.out.println("[" + moduleName + "] Applying 2D Gaussian filter (radius = " + filterRadius + " px)");
                runGaussian2DFilter(inputImagePlus,filterRadius);
                break;

            case FilterModes.GAUSSIAN3D:
                if (verbose) System.out.println("[" + moduleName + "] Applying 3D Gaussian filter (radius = " + filterRadius + " px)");
                GaussianBlur3D.blur(inputImagePlus,filterRadius,filterRadius,filterRadius);
                break;

            case FilterModes.MEDIAN3D:
                if (verbose) System.out.println("[" + moduleName + "] Applying 3D median filter (radius = " + filterRadius + " px)");
                inputImagePlus.setStack(Filters3D.filter(inputImagePlus.getImageStack(), Filters3D.MEDIAN, (float) filterRadius, (float) filterRadius, (float) filterRadius));
                break;

            case FilterModes.ROLLING_FRAME:
                if (verbose) System.out.println("[" + moduleName + "] Applying rolling frame filter (window half width = "+windowHalfWidth+" frames)");
                inputImagePlus = runRollingFrameFilter(inputImagePlus,windowHalfWidth);
                break;

        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(outputImage.getImagePlus()).show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(inputImagePlus).show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));

        parameters.addParameter(new Parameter(FILTER_MODE, Parameter.CHOICE_ARRAY,FilterModes.DOG2D,FilterModes.ALL));
        parameters.addParameter(new Parameter(FILTER_RADIUS, Parameter.DOUBLE,2d));
        parameters.addParameter(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(WINDOW_HALF_WIDTH,Parameter.INTEGER,1));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(FILTER_MODE));
        if (!parameters.getValue(FILTER_MODE).equals(FilterModes.ROLLING_FRAME)) {
            returnedParameters.addParameter(parameters.getParameter(FILTER_RADIUS));
            returnedParameters.addParameter(parameters.getParameter(CALIBRATED_UNITS));

        } else {
            returnedParameters.addParameter(parameters.getParameter(WINDOW_HALF_WIDTH));

        }

        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
