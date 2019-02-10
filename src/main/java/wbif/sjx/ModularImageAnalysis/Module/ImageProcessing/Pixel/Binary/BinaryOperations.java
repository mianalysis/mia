// TODO: What happens when 3D distance map is run on 4D or 5D image hyperstack?

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.macro.MacroExtension;
import ij.plugin.*;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.morphology.GeodesicReconstruction3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import inra.ijpb.watershed.Watershed;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.InterpolateZAxis;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";
    public static final String USE_MARKERS = "Use markers";
    public static final String MARKER_IMAGE = "Input marker image";
    public static final String INTENSITY_MODE = "Intensity mode";
    public static final String INTENSITY_IMAGE = "Intensity image";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY_3D = "Connectivity (3D)";
    public static final String MATCH_Z_TO_X= "Match Z to XY";

    public interface OperationModes {
        String DILATE_2D = "Dilate 2D";
        String DILATE_3D = "Dilate 3D";
        String DISTANCE_MAP_3D = "Distance map 3D";
        String ERODE_2D = "Erode 2D";
        String ERODE_3D = "Erode 3D";
        String FILL_HOLES_2D = "Fill holes 2D";
        String FILL_HOLES_3D = "Fill holes 3D";
        String OUTLINE_2D = "Outline 2D";
        String SKELETONISE_2D = "Skeletonise 2D";
        String WATERSHED_2D = "Watershed 2D";
        String WATERSHED_3D = "Watershed 3D";

        String[] ALL = new String[]{DILATE_2D,DILATE_3D,DISTANCE_MAP_3D,ERODE_2D,ERODE_3D,FILL_HOLES_2D,FILL_HOLES_3D,
                OUTLINE_2D,SKELETONISE_2D,WATERSHED_2D,WATERSHED_3D};

    }

    public interface IntensityModes {
        String DISTANCE = "Distance";
        String INPUT_IMAGE = "Input image intensity";

        String[] ALL = new String[]{DISTANCE,INPUT_IMAGE};

    }

    public interface Connectivity3D {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }


    public static void applyStockBinaryTransform(ImagePlus ipl, String operationMode, int numIterations) {
        // Applying processAutomatic to stack
        switch (operationMode) {
            case OperationModes.DILATE_2D:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=Dilate stack");
                break;

            case OperationModes.ERODE_2D:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=Erode stack");
                break;

            case OperationModes.FILL_HOLES_2D:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=[Fill Holes] stack");
                break;

            case OperationModes.OUTLINE_2D:
                IJ.run(ipl,"Outline", "stack");
                break;

            case OperationModes.SKELETONISE_2D:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=Skeletonize stack");
                break;

            case OperationModes.WATERSHED_2D:
                IJ.run(ipl,"Watershed", "stack");
                break;

        }
    }

    public static void applyDilateErode3D(ImagePlus ipl, String operationMode, int numIterations) {
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        double dppXY = ipl.getCalibration().pixelWidth;
        double dppZ = ipl.getCalibration().pixelDepth;
        double ratio = dppXY/dppZ;

        Strel3D ballStrel = Strel3D.Shape.BALL.fromRadiusList(numIterations,(int) (numIterations*ratio),2);

        // MorphoLibJ takes objects as being white
        InvertIntensity.process(ipl);

        for (int c=1;c<=nChannels;c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                ImageStack istFilt = null;

                switch (operationMode) {
                    case OperationModes.DILATE_3D:
                        istFilt = Morphology.dilation(iplOrig.getImageStack(),ballStrel);
                        break;
                    case OperationModes.ERODE_3D:
                        istFilt = Morphology.erosion(iplOrig.getImageStack(),ballStrel);
                        break;
                }

                for (int z = 1; z <= istFilt.getSize(); z++) {
                    ipl.setPosition(c, z, t);
                    ImageProcessor iprOrig = ipl.getProcessor();
                    ImageProcessor iprFilt = istFilt.getProcessor(z);

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            iprOrig.setf(x, y, iprFilt.getf(x, y));
                        }
                    }
                }
            }
        }

        // Flipping the intensities back
        InvertIntensity.process(ipl);

    }

    public static ImagePlus getDistanceMap3D(ImagePlus ipl, boolean matchZToXY) {
        int nSlices = ipl.getNSlices();

        // If necessary, interpolating the image in Z to match the XY spacing
        if (matchZToXY && nSlices > 1) ipl = InterpolateZAxis.matchZToXY(ipl);

        // Calculating the distance map using MorphoLibJ
        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();

        // Creating duplicates of the input image
        ipl = new Duplicator().run(ipl);
        ImagePlus maskIpl = new Duplicator().run(ipl);

        IJ.run(maskIpl,"Invert","stack");
        ipl.setStack(new GeodesicDistanceMap3D().process(ipl,maskIpl,"Dist",weights,true).getStack());

        // If the input image as interpolated, it now needs to be returned to the original scaling
        if (matchZToXY && nSlices > 1) {
            Resizer resizer = new Resizer();
            resizer.setAverageWhenDownsizing(true);
            ipl = resizer.zScale(ipl, nSlices, Resizer.IN_PLACE);
        }

        return ipl;

    }

    public static void applyFillHoles3D(ImagePlus ipl) {
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        // MorphoLibJ takes objects as being white
        InvertIntensity.process(ipl);

        for (int c=1;c<=nChannels;c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                ImageStack iplFill = GeodesicReconstruction3D.fillHoles(iplOrig.getImageStack());

                for (int z = 1; z <= iplFill.getSize(); z++) {
                    ipl.setPosition(c, z, t);
                    ImageProcessor iprOrig = ipl.getProcessor();
                    ImageProcessor iprFilt = iplFill.getProcessor(z);

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            iprOrig.setf(x, y, iprFilt.getf(x, y));
                        }
                    }
                }
            }
        }
    }

    public void applyWatershed3D(ImagePlus intensityIpl, ImagePlus markerIpl, ImagePlus maskIpl, int dynamic, int connectivity) {
        // Expected inputs for binary images (marker and mask) are black objects on a white background.  These need to
        // be inverted before using as MorphoLibJ uses the opposite convention.
        IJ.run(maskIpl,"Invert","stack");
        if (markerIpl != null) {
            markerIpl = new Duplicator().run(markerIpl);
            IJ.run(markerIpl, "Invert", "stack");
        }

        int nFrames = maskIpl.getNFrames();
        for (int t = 1; t <= nFrames; t++) {
            // Getting maskIpl for this timepoint
            ImagePlus timepointMaskIpl = getTimepoint(maskIpl,t);
            ImagePlus timepointIntensityIpl = getTimepoint(intensityIpl,t);

            if (markerIpl == null) {
                timepointMaskIpl.setStack(ExtendedMinimaWatershed.extendedMinimaWatershed(timepointIntensityIpl.getStack(), timepointMaskIpl.getStack(), dynamic, connectivity, false));

            } else {
                ImagePlus timepointMarkerIpl = getTimepoint(markerIpl,t);
                timepointMarkerIpl = BinaryImages.componentsLabeling(timepointMarkerIpl, connectivity, 32);
                timepointMaskIpl.setStack(Watershed.computeWatershed(timepointIntensityIpl, timepointMarkerIpl, timepointMaskIpl, connectivity, true, false).getStack());

            }

            // The image produced by MorphoLibJ's watershed function is labelled.  Converting to binary and back to 8-bit.
            IJ.setRawThreshold(timepointMaskIpl, 0, 0, null);
            IJ.run(timepointMaskIpl, "Convert to Mask", "method=Default background=Light");
            IJ.run(timepointMaskIpl, "Invert LUT", "");
            IJ.run(timepointMaskIpl, "8-bit", null);

            //  Replacing the maskIpl intensity
            overwriteTimepoint(maskIpl,timepointMaskIpl,t);

            writeMessage("Processed "+t+" of "+nFrames+" frames");

        }
    }

    private static ImagePlus getTimepoint(ImagePlus inputImagePlus, int timepoint) {
        ImagePlus outputImagePlus = IJ.createImage("Output",inputImagePlus.getWidth(),inputImagePlus.getHeight(),inputImagePlus.getNSlices(),inputImagePlus.getBitDepth());

        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            inputImagePlus.setPosition(1,z,timepoint);
            outputImagePlus.setPosition(z);

            ImageProcessor inputImageProcessor = inputImagePlus.getProcessor();
            ImageProcessor outputImageProcessor = outputImagePlus.getProcessor();

            for (int y=0;y<inputImagePlus.getHeight();y++) {
                for (int x = 0; x < inputImagePlus.getWidth(); x++) {
                    outputImageProcessor.set(x,y,inputImageProcessor.get(x,y));
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);
        outputImagePlus.setPosition(1);

        return outputImagePlus;

    }

    private static  void overwriteTimepoint(ImagePlus inputImagePlus, ImagePlus timepointImagePlus, int timepoint) {
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            inputImagePlus.setPosition(1,z,timepoint);
            timepointImagePlus.setPosition(z);

            ImageProcessor inputImageProcessor = inputImagePlus.getProcessor();
            ImageProcessor timepointImageProcessor = timepointImagePlus.getProcessor();

            for (int y=0;y<inputImagePlus.getHeight();y++) {
                for (int x = 0; x < inputImagePlus.getWidth(); x++) {
                    inputImageProcessor.set(x,y,timepointImageProcessor.get(x,y));
                }
            }

        }

        inputImagePlus.setPosition(1,1,1);
        timepointImagePlus.setPosition(1);

    }

    @Override
    public String getTitle() {
        return "Binary operations (legacy)";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return "***DEPRECATED***" +
                "\n This module will be removed soon.  Please use individual binary modules.";

    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String operationMode = parameters.getValue(OPERATION_MODE);
        int numIterations = parameters.getValue(NUM_ITERATIONS);
        boolean useMarkers = parameters.getValue(USE_MARKERS);
        String markerImageName = parameters.getValue(MARKER_IMAGE);
        String intensityMode = parameters.getValue(INTENSITY_MODE);
        String intensityImageName = parameters.getValue(INTENSITY_IMAGE);
        int dynamic = parameters.getValue(DYNAMIC);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY_3D));
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        switch (operationMode) {
            case (OperationModes.DILATE_2D):
            case (OperationModes.ERODE_2D):
            case (OperationModes.FILL_HOLES_2D):
            case (OperationModes.OUTLINE_2D):
            case (OperationModes.SKELETONISE_2D):
            case (OperationModes.WATERSHED_2D):
                applyStockBinaryTransform(inputImagePlus,operationMode,numIterations);
                break;

            case (OperationModes.DILATE_3D):
            case (OperationModes.ERODE_3D):
                applyDilateErode3D(inputImagePlus,operationMode,numIterations);
                break;

            case (OperationModes.DISTANCE_MAP_3D):
                inputImagePlus = getDistanceMap3D(inputImagePlus,matchZToXY);
                break;

            case (OperationModes.FILL_HOLES_3D):
                applyFillHoles3D(inputImagePlus);
                break;

            case (OperationModes.WATERSHED_3D):
                ImagePlus markerIpl = null;
                if (useMarkers) markerIpl = workspace.getImage(markerImageName).getImagePlus();

                ImagePlus intensityIpl = null;
                switch (intensityMode) {
                    case IntensityModes.DISTANCE:
                        intensityIpl = new Duplicator().run(inputImagePlus);
                        intensityIpl = getDistanceMap3D(intensityIpl,matchZToXY);
                        IJ.run(intensityIpl,"Invert","stack");
                        break;

                    case IntensityModes.INPUT_IMAGE:
                        intensityIpl = workspace.getImage(intensityImageName).getImagePlus();
                        break;

                }

                applyWatershed3D(intensityIpl,markerIpl,inputImagePlus,dynamic,connectivity);

                break;

        }

        // If selected, displaying the image
        if (showOutput) {
            ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
            dispIpl.setTitle(inputImageName);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.show();
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(OPERATION_MODE,this,OperationModes.DILATE_2D,OperationModes.ALL));
        parameters.add(new IntegerP(NUM_ITERATIONS,this,1));
        parameters.add(new BooleanP(USE_MARKERS,this,false));
        parameters.add(new InputImageP(MARKER_IMAGE,this));
        parameters.add(new ChoiceP(INTENSITY_MODE,this,IntensityModes.DISTANCE,IntensityModes.ALL));
        parameters.add(new InputImageP(INTENSITY_IMAGE,this));
        parameters.add(new IntegerP(DYNAMIC,this,1));
        parameters.add(new ChoiceP(CONNECTIVITY_3D,this,Connectivity3D.SIX,Connectivity3D.ALL));
        parameters.add(new BooleanP(MATCH_Z_TO_X,this,true));

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
        switch ((String) parameters.getValue(OPERATION_MODE)) {
            case OperationModes.DILATE_2D:
            case OperationModes.DILATE_3D:
            case OperationModes.ERODE_2D:
            case OperationModes.ERODE_3D:
                returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));
                break;

            case OperationModes.DISTANCE_MAP_3D:
                returnedParameters.add(parameters.getParameter(MATCH_Z_TO_X));
                break;

            case OperationModes.WATERSHED_3D:
                returnedParameters.add(parameters.getParameter(USE_MARKERS));
                if (parameters.getValue(USE_MARKERS)) {
                    returnedParameters.add(parameters.getParameter(MARKER_IMAGE));
                } else {
                    returnedParameters.add(parameters.getParameter(DYNAMIC));
                }

                returnedParameters.add(parameters.getParameter(INTENSITY_MODE));
                switch ((String) parameters.getValue(INTENSITY_MODE)) {
                    case IntensityModes.DISTANCE:
                        returnedParameters.add(parameters.getParameter(MATCH_Z_TO_X));
                        break;

                    case IntensityModes.INPUT_IMAGE:
                        returnedParameters.add(parameters.getParameter(INTENSITY_IMAGE));
                        break;
                }
                returnedParameters.add(parameters.getParameter(CONNECTIVITY_3D));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

    @Override
    public ArrayList<MacroOperation> getMacroOperations(MacroExtension handler) {
        return null;
    }
}
