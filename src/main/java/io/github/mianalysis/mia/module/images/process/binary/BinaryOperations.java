// TODO: What happens when 3D distance map is generateModuleList on 4D or 5D image hyperstack?

package io.github.mianalysis.mia.module.images.process.binary;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.Resizer;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Reconstruction3D;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import inra.ijpb.watershed.Watershed;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.transform.InterpolateZAxis;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.ConnectivityInterface;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.process.IntensityMinMax;

/**
 * Created by sc13967 on 06/06/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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
    public static final String CONNECTIVITY = "Connectivity";
    public static final String MATCH_Z_TO_X= "Match Z to XY";

    public BinaryOperations(Modules modules) {
        super("Binary operations (legacy)", modules);
        deprecated = true;
    }

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

    public interface Connectivity extends ConnectivityInterface {
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

        Strel3D ballStrel = Strel3D.Shape.BALL.fromRadiusList(numIterations,numIterations,(int) (numIterations*ratio));

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
        if (matchZToXY && nSlices > 1) ipl = InterpolateZAxis.matchZToXY(ipl,InterpolateZAxis.InterpolationModes.NONE);

        // Creating duplicates of the input image
        ipl = new Duplicator().run(ipl);
        ImagePlus maskIpl = new Duplicator().run(ipl);
        ImageMath.process(maskIpl, ImageMath.CalculationModes.MULTIPLY, 0);
        ImageMath.process(maskIpl, ImageMath.CalculationModes.ADD, 255);

        ipl.setStack(new GeodesicDistanceMap3D().process(ipl,maskIpl,"Dist",ChamferMask3D.SVENSSON_3_4_5_7,true).getStack());

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
                ImageStack iplFill = Reconstruction3D.fillHoles(iplOrig.getImageStack());

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

            writeProgressStatus(t, nFrames, "frames");

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
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_BINARY;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: This Module has been superseeded by separate Modules for 2D and 3D binary operations.  It will " +
                "be removed in a future release.<br><br>"

                + "Applies stock binary operations to an image in the workspace.  This image must be 8-bit and have the logic black foreground (intensity 0) and white background (intensity 255).  Operations labelled \"2D\" are performed using the stock ImageJ implementations, while those labelled \"3D\" use the MorphoLibJ implementations.  If 2D operations are applied on higher dimensionality images the operations will be performed in a slice-by-slice manner.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String operationMode = parameters.getValue(OPERATION_MODE,workspace);
        int numIterations = parameters.getValue(NUM_ITERATIONS,workspace);
        boolean useMarkers = parameters.getValue(USE_MARKERS,workspace);
        String markerImageName = parameters.getValue(MARKER_IMAGE,workspace);
        String intensityMode = parameters.getValue(INTENSITY_MODE,workspace);
        String intensityImageName = parameters.getValue(INTENSITY_IMAGE,workspace);
        int dynamic = parameters.getValue(DYNAMIC,workspace);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY,workspace));
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X,workspace);

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
            writeStatus("Adding image ("+outputImageName+") to workspace");
            Image outputImage = ImageFactory.createImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }

        return Status.PASS;

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
        parameters.add(new ChoiceP(CONNECTIVITY,this,Connectivity.SIX,Connectivity.ALL));
        parameters.add(new BooleanP(MATCH_Z_TO_X,this,true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(OPERATION_MODE));
        switch ((String) parameters.getValue(OPERATION_MODE,workspace)) {
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
                if ((boolean) parameters.getValue(USE_MARKERS,workspace)) {
                    returnedParameters.add(parameters.getParameter(MARKER_IMAGE));
                } else {
                    returnedParameters.add(parameters.getParameter(DYNAMIC));
                }

                returnedParameters.add(parameters.getParameter(INTENSITY_MODE));
                switch ((String) parameters.getValue(INTENSITY_MODE,workspace)) {
                    case IntensityModes.DISTANCE:
                        returnedParameters.add(parameters.getParameter(MATCH_Z_TO_X));
                        break;

                    case IntensityModes.INPUT_IMAGE:
                        returnedParameters.add(parameters.getParameter(INTENSITY_IMAGE));
                        break;
                }
                returnedParameters.add(parameters.getParameter(CONNECTIVITY));
                break;
        }

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from workspace to apply binary operation to.  This must be an 8-bit binary image (255 = background, 0 = foreground).");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(OPERATION_MODE).setDescription(
                "Controls which binary operation will be applied.  All operations assume the default ImageJ logic of black objects on a white background.  The 2D operations are described in full at <a href=\"https://imagej.nih.gov/ij/docs/guide/146-29.html\">https://imagej.nih.gov/ij/docs/guide/146-29.html</a>:<br><ul>"

                        + "<li>\"" + OperationModes.DILATE_2D
                        + "\" Change any foreground-connected background pixels to foreground.  This effectively expands objects by one pixel.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.DILATE_3D
                        + "\" Change any foreground-connected background pixels to foreground.  This effectively expands objects by one pixel.  Uses MorphoLibJ implementation.</li>"

                        + "<li>\"" + OperationModes.DISTANCE_MAP_3D
                        + "\" Create a 32-bit greyscale image where the value of each foreground pixel is equal to its Euclidean distance to the nearest background pixel.  Uses MorphoLibJ implementation.</li>"

                        + "<li>\"" + OperationModes.ERODE_2D
                        + "\" Change any background-connected foreground pixels to background.  This effectively shrinks objects by one pixel.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.ERODE_3D
                        + "\" Change any background-connected foreground pixels to background.  This effectively shrinks objects by one pixel.  Uses MorphoLibJ implementation.</li>"

                        + "<li>\"" + OperationModes.FILL_HOLES_2D
                        + "\" Change all background pixels in a region which is fully enclosed by foreground pixels to foreground.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.FILL_HOLES_3D
                        + "\" Change all background pixels in a region which is fully enclosed by foreground pixels to foreground.  Uses MorphoLibJ implementation.</li>"

                        + "<li>\"" + OperationModes.OUTLINE_2D
                        + "\" Convert all non-background-connected foreground pixels to background.  This effectively creates a fully-background image, except for the outer band of foreground pixels.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.SKELETONISE_2D
                        + "\" Repeatedly applies the erode process until each foreground region is a single pixel wide.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.WATERSHED_2D
                        + "\" Peforms a distance-based watershed transform on the image.  This process is able to split separate regions of a single connected foreground region as long as the sub-regions are connected by narrow necks (e.g. snowman shape).  Background lines are drawn between each sub-region such that they are no longer connected.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.WATERSHED_3D
                        + "\" Peforms a watershed transform on the image.  This process is able to split separate regions of a single connected foreground region as long as the sub-regions are connected by narrow necks (e.g. snowman shape).  Background lines are drawn between each sub-region such that they are no longer connected.  Unlike the 2D ImageJ implementation, this version can use specific markers and be run in either distance or intensity-based modes.  Uses MorphoLibJ implementation.</li></ul>");

        parameters.get(NUM_ITERATIONS).setDescription(
                "Number of times the operation will be run on a single image.  For example, this allows objects to be eroded further than one pixel in a single step.");

        parameters.get(USE_MARKERS).setDescription("(3D watershed only) When selected, this option allows the use of markers to define the starting point of each region.  The marker image to use is specified using the \""+MARKER_IMAGE+"\" parameter.  If not selected, a distance map will be generated for the input binary image and extended minima created according to the dynamic specified by \""+DYNAMIC+"\".");

        parameters.get(MARKER_IMAGE).setDescription("(3D watershed only) Marker image to be used if \""+USE_MARKERS+"\" is selected.  This image must be of equal dimensions to the input image (to which the transform will be applied).  The image must be 8-bit binary with markers in black (intensity 0) on a white background (intensity 255).");

        parameters.get(INTENSITY_MODE).setDescription("(3D watershed only) Controls the source for the intensity image against which the watershed transform will be computed.  Irrespective of mode, the image (raw image or object distance map) will act as a surface that the starting points will evolve up until adjacent regions come into contact (at which point creating a dividing line between the two):<br><ul>"

        +"<li>\""+IntensityModes.DISTANCE+"\" A distance map will be created from the input binary image and used as the surface against which the watershed regions will evolve.</li>"

        +"<li>\""+IntensityModes.INPUT_IMAGE+"\" The watershed regions will evolve against an image from the workspace.  This image will be unaffected by this process.  The image should have lower intensity coincident with the markers, rising to higher intensity along the boundaries between regions. </li></ul>");

        parameters.get(INTENSITY_IMAGE).setDescription("(3D watershed only) If \""+INTENSITY_MODE+"\" is set to \""+IntensityModes.INPUT_IMAGE+"\", this is the image from the workspace against which the watershed regions will evolve.  The image should have lower intensity coincident with the markers, rising to higher intensity along the boundaries between regions.");

        parameters.get(DYNAMIC).setDescription("(3D watershed only) If \""+USE_MARKERS+"\" is not selected, the initial region markers will be created by generating a distance map for the input binary image and calculating the extended minima.  This parameter specifies the maximum permitted pixel intensity difference for a single marker.  Local intensity differences greater than this will result in creation of more markers.  The smaller the dynamic value is, the more the watershed transform will split the image.");

        parameters.get(CONNECTIVITY).setDescription("(3D watershed only) Controls which adjacent pixels are considered:<br><ul>"

        +"<li>\""+Connectivity.SIX+"\" Only pixels immediately next to the active pixel are considered.  These are the pixels on the four \"cardinal\" directions plus the pixels immediately above and below the current pixel.  If working in 2D, 4-way connectivity is used.</li>"

        +"<li>\""+Connectivity.TWENTYSIX+"\" In addition to the core 6-pixels, all immediately diagonal pixels are used.  If working in 2D, 8-way connectivity is used.</li>");

        parameters.get(MATCH_Z_TO_X).setDescription("When selected, an image is interpolated in Z (so that all pixels are isotropic) prior to calculation of a distance map.  This prevents warping of the distance map along the Z-axis if XY and Z sampling aren't equal.");

    }
}
