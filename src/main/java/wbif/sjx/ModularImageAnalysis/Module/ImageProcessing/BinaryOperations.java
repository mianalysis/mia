package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.plugin.frame.ThresholdAdjuster;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";
    public static final String SHOW_IMAGE = "Show image";
    public static final String DYNAMIC = "Dynamic (Watershed)";

    public interface OperationModes {
        String DILATE = "Dilate 2D";
        String MANHATTAN_DISTANCE_MAP_2D = "Distance map (Manhattan) 2D";
        String ERODE = "Erode 2D";
        String FILL_HOLES_2D = "Fill holes 2D";
        String WATERSHED_3D = "Watershed 3D";

        String[] ALL = new String[]{DILATE,MANHATTAN_DISTANCE_MAP_2D,ERODE,FILL_HOLES_2D,WATERSHED_3D};

    }


    @Override
    public String getTitle() {
        return "Binary operations";
    }

    @Override
    public String getHelp() {
        return "Performs 2D fill holes, dilate and erode using ImageJ functions\n" +
                "Uses MorphoLibJ to do 3D Watershed";

    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        Prefs.blackBackground = false;

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String operationMode = parameters.getValue(OPERATION_MODE);
        int dynamic = parameters.getValue(DYNAMIC);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Applying process to stack
        switch (operationMode) {
            case OperationModes.DILATE:
                int numIterations = parameters.getValue(NUM_ITERATIONS);
                if (verbose) System.out.println("["+moduleName+"] Dilate ("+numIterations+"x)");
                for (int i=0;i<numIterations;i++) {
                    IJ.run(inputImagePlus, "Dilate", "stack");
                }

                break;

            case OperationModes.ERODE:
                numIterations = parameters.getValue(NUM_ITERATIONS);
                if (verbose) System.out.println("["+moduleName+"] Erode ("+numIterations+"x)");
                for (int i=0;i<numIterations;i++) {
                    IJ.run(inputImagePlus, "Erode", "stack");
                }

                break;

            case OperationModes.FILL_HOLES_2D:
                if (verbose) System.out.println("["+moduleName+"] Filling binary holes");
                IJ.run(inputImagePlus,"Fill Holes", "stack");

                break;

            case OperationModes.WATERSHED_3D:
                if (verbose) System.out.println("["+moduleName+"] Calculating distance map");
                IJ.run(inputImagePlus,"Invert", "stack");

                // Creating a marker image
                ImagePlus markerIpl = new Duplicator().run(inputImagePlus);

                // Inverting the mask intensity
                for (int z = 1; z <= markerIpl.getNSlices(); z++) {
                    for (int c = 1; c <= markerIpl.getNChannels(); c++) {
                        for (int t = 1; t <= markerIpl.getNFrames(); t++) {
                            markerIpl.setPosition(c, z, t);
                            markerIpl.getProcessor().invert();
                        }
                    }
                }
                markerIpl.setPosition(1,1,1);

                // Calculating the distance map using MorphoLibJ
                if (verbose) System.out.println("["+moduleName+"] Calculating distance map");
                float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
                ImagePlus distIpl = new GeodesicDistanceMap3D().process(markerIpl,inputImagePlus,"Dist",weights,false);

                // Inverting the distance map, so the centres of objects have the smallest values
                for (int z = 1; z <= distIpl.getNSlices(); z++) {
                    for (int c = 1; c <= distIpl.getNChannels(); c++) {
                        for (int t = 1; t <= distIpl.getNFrames(); t++) {
                            distIpl.setPosition(c, z, t);
                            distIpl.getProcessor().invert();
                        }
                    }
                }
                distIpl.setPosition(1,1,1);

                if (verbose) System.out.println("["+moduleName+"] Applying watershed segmentation");
                inputImagePlus.setStack(ExtendedMinimaWatershed.extendedMinimaWatershed(distIpl.getImageStack(),inputImagePlus.getImageStack(),dynamic,26,false));

                IJ.setRawThreshold(inputImagePlus, 0, 0, null);
                IJ.run(inputImagePlus, "Convert to Mask", "method=Default background=Light");
                IJ.run(inputImagePlus, "Invert LUT", "");

                break;

        }

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            new Duplicator().run(inputImagePlus).show();
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OPERATION_MODE, Parameter.CHOICE_ARRAY,OperationModes.DILATE,OperationModes.ALL));
        parameters.addParameter(new Parameter(NUM_ITERATIONS, Parameter.INTEGER,1));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(DYNAMIC, Parameter.INTEGER,1));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));

        }

        returnedParameters.addParameter(parameters.getParameter(OPERATION_MODE));

        if (parameters.getValue(OPERATION_MODE).equals(OperationModes.DILATE) | parameters.getValue(OPERATION_MODE).equals(OperationModes.ERODE)) {
            returnedParameters.addParameter(parameters.getParameter(NUM_ITERATIONS));

        } else if (parameters.getValue(OPERATION_MODE).equals(OperationModes.WATERSHED_3D)) {
            returnedParameters.addParameter(parameters.getParameter(DYNAMIC));

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
