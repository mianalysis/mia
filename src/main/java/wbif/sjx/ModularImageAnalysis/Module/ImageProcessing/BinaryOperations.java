package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";
    public static final String DYNAMIC = "Dynamic (Watershed)";
    public static final String SHOW_IMAGE = "Show image";

    public interface OperationModes {
        String DILATE_2D = "Dilate 2D";
        String MANHATTAN_DISTANCE_MAP_2D = "Distance map (Manhattan) 2D";
        String ERODE_2D = "Erode 2D";
        String FILL_HOLES_2D = "Fill holes 2D";
        String SKELETONISE_2D = "Skeletonise 2D";
        String WATERSHED_2D = "Watershed 2D";
        String WATERSHED_3D = "Watershed 3D";

        String[] ALL = new String[]{DILATE_2D,MANHATTAN_DISTANCE_MAP_2D,ERODE_2D,FILL_HOLES_2D,SKELETONISE_2D,WATERSHED_2D,WATERSHED_3D};

    }

    public static void applyBinaryTransform(ImagePlus ipl, String operationMode, int numIterations, int dynamic) {
        // Applying process to stack
        switch (operationMode) {
            case OperationModes.DILATE_2D:
                for (int i=0;i<numIterations;i++) {
                    IJ.run(ipl, "Dilate", "stack");
                }
                break;

            case OperationModes.ERODE_2D:
                for (int i=0;i<numIterations;i++) {
                    IJ.run(ipl, "Erode", "stack");
                }
                break;

            case OperationModes.FILL_HOLES_2D:
                IJ.run(ipl,"Fill Holes", "stack");
                break;

            case OperationModes.SKELETONISE_2D:
                IJ.run(ipl,"Skeletonize", "Stack");
                break;

            case OperationModes.WATERSHED_2D:
                IJ.run(ipl,"Watershed", "Stack");
                break;

            case OperationModes.WATERSHED_3D:
                IJ.run(ipl,"Invert", "stack");

                // Creating a marker image
                ImagePlus markerIpl = new Duplicator().run(ipl);

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
                float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
                ImagePlus distIpl = new GeodesicDistanceMap3D().process(markerIpl,ipl,"Dist",weights,false);

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

                ipl.setStack(ExtendedMinimaWatershed.extendedMinimaWatershed(distIpl.getImageStack(),ipl.getImageStack(),dynamic,26,false));

                IJ.setRawThreshold(ipl, 0, 0, null);
                IJ.run(ipl, "Convert to Mask", "method=Default background=Light");
                IJ.run(ipl, "Invert LUT", "");

                break;

        }
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
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String operationMode = parameters.getValue(OPERATION_MODE);
        int numIterations = parameters.getValue(NUM_ITERATIONS);
        int dynamic = parameters.getValue(DYNAMIC);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        applyBinaryTransform(inputImagePlus,operationMode,numIterations,dynamic);

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.show();
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
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(
                new Parameter(OPERATION_MODE, Parameter.CHOICE_ARRAY,OperationModes.DILATE_2D,OperationModes.ALL));
        parameters.add(new Parameter(NUM_ITERATIONS, Parameter.INTEGER,1));
        parameters.add(new Parameter(DYNAMIC, Parameter.INTEGER,1));
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

        returnedParameters.add(parameters.getParameter(OPERATION_MODE));

        if (parameters.getValue(OPERATION_MODE).equals(OperationModes.DILATE_2D)
                | parameters.getValue(OPERATION_MODE).equals(OperationModes.ERODE_2D)) {
            returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));

        } else if (parameters.getValue(OPERATION_MODE).equals(OperationModes.WATERSHED_3D)) {
            returnedParameters.add(parameters.getParameter(DYNAMIC));

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
