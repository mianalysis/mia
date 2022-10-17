package io.github.mianalysis.mia.module.images.process.binary;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Duplicator;
import inra.ijpb.binary.BinaryImages;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
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
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class Watershed extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String WATERSHED_SEPARATOR = "Watershed controls";
    public static final String USE_MARKERS = "Use markers";
    public static final String MARKER_IMAGE = "Input marker image";
    public static final String INTENSITY_MODE = "Intensity mode";
    public static final String INTENSITY_IMAGE = "Intensity image";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String MATCH_Z_TO_X = "Match Z to XY";
    public static final String BINARY_LOGIC = "Binary logic";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public Watershed(Modules modules) {
        super("Watershed transform", modules);
    }

    public interface IntensityModes {
        String DISTANCE = "Distance";
        String INPUT_IMAGE = "Input image intensity";

        String[] ALL = new String[] { DISTANCE, INPUT_IMAGE };

    }

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[] { SIX, TWENTYSIX };

    }

    public static void process(ImagePlus intensityIpl, ImagePlus markerIpl, ImagePlus maskIpl, boolean blackBackground,
            int dynamic, int connectivity, boolean multithread) throws InterruptedException {
        String name = new Watershed(null).getName();

        // Expected inputs for binary images (marker and mask) are black objects on a
        // white background. These need to
        // be inverted before using as MorphoLibJ uses the opposite convention.
        if (!blackBackground)
            IJ.run(maskIpl, "Invert", "stack");

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int nChannels = maskIpl.getNChannels();
        int nFrames = maskIpl.getNFrames();
        int nTotal = nChannels * nFrames;
        AtomicInteger count = new AtomicInteger();

        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                int finalT = t;
                int finalC = c;

                Runnable task = () -> {
                    // Getting maskIpl for this timepoint
                    ImageStack timepointMask = ImagePlusImage.getSetStack(maskIpl, finalT, finalC, null);
                    ImageStack timepointIntensity = ImagePlusImage.getSetStack(intensityIpl, finalT, finalC, null);
                    ImageStack timepointMarker = ImagePlusImage.getSetStack(markerIpl, finalT, finalC, null);
                    timepointMarker = BinaryImages.componentsLabeling(timepointMarker, connectivity, 32);

                    timepointMask = inra.ijpb.watershed.Watershed.computeWatershed(timepointIntensity, timepointMarker,
                            timepointMask, connectivity, true, false);

                    // The image produced by MorphoLibJ's watershed function is labelled. Converting
                    // to binary and back to 8-bit.
                    ImagePlus timepointMaskIpl = new ImagePlus("Timepoint mask", timepointMask);
                    IJ.setRawThreshold(timepointMaskIpl, 0, 0, null);
                    IJ.run(timepointMaskIpl, "Convert to Mask", "method=Default background=Light");
                    if (blackBackground)
                        IJ.run(timepointMaskIpl, "Invert", "stack");                        
                    
                    IJ.run(timepointMaskIpl, "8-bit", null);

                    if (timepointMaskIpl.isInvertedLut())
                        IJ.run(timepointMaskIpl, "Invert LUT", "");

                    // Replacing the maskIpl intensity
                    ImagePlusImage.getSetStack(maskIpl, finalT, finalC, timepointMaskIpl.getStack());

                    writeProgressStatus(count.incrementAndGet(), nTotal, "stacks", name);

                };
                pool.submit(task);
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_BINARY;
    }

    @Override
    public String getDescription() {
        return "Peforms a watershed transform on a specified input image.  This process is able to split separate regions of a single connected foreground region as long as the sub-regions are connected by narrow necks (e.g. snowman shape).  Background lines are drawn between each sub-region such that they are no longer connected.  This can use specific markers and be run in either distance or intensity-based modes.  Uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus maskIpl = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        boolean useMarkers = parameters.getValue(USE_MARKERS,workspace);
        String markerImageName = parameters.getValue(MARKER_IMAGE,workspace);
        String intensityMode = parameters.getValue(INTENSITY_MODE,workspace);
        String intensityImageName = parameters.getValue(INTENSITY_IMAGE,workspace);
        int dynamic = parameters.getValue(DYNAMIC,workspace);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY,workspace));
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X,workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC,workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            maskIpl = new Duplicator().run(maskIpl);

        ImagePlus intensityIpl = null;
        switch (intensityMode) {
            case IntensityModes.DISTANCE:
                intensityIpl = DistanceMap.process(maskIpl, "Distance", blackBackground,
                        DistanceMap.WeightModes.WEIGHTS_3_4_5_7, matchZToXY, false);
                InvertIntensity.process(intensityIpl);
                break;

            case IntensityModes.INPUT_IMAGE:
                intensityIpl = workspace.getImage(intensityImageName).getImagePlus();
                break;
        }

        try {
            ImagePlus markerIpl = null;
            if (useMarkers) {
                markerIpl = workspace.getImage(markerImageName).getImagePlus();
            } else {
                Image tempIntensity = ImageFactory.createImage("TempIntensity", intensityIpl);
                String mode = ExtendedMinima.MinimaMaximaModes.MINIMA;
                markerIpl = ExtendedMinima
                        .process(tempIntensity, "Marker", mode, blackBackground, dynamic, connectivity, multithread)
                        .getImagePlus();
            }

            process(intensityIpl, markerIpl, maskIpl, blackBackground, dynamic, connectivity, multithread);

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            Image outputImage = ImageFactory.createImage(outputImageName, maskIpl);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage();

        } else {
            if (showOutput)
                inputImage.showImage();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(WATERSHED_SEPARATOR, this));
        parameters.add(new BooleanP(USE_MARKERS, this, false));
        parameters.add(new InputImageP(MARKER_IMAGE, this));
        parameters.add(new ChoiceP(INTENSITY_MODE, this, IntensityModes.DISTANCE, IntensityModes.ALL));
        parameters.add(new InputImageP(INTENSITY_IMAGE, this));
        parameters.add(new IntegerP(DYNAMIC, this, 1));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new BooleanP(MATCH_Z_TO_X, this, true));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        
        returnedParameters.add(parameters.getParameter(WATERSHED_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_MARKERS));
        if ((boolean) parameters.getValue(USE_MARKERS,workspace))
            returnedParameters.add(parameters.getParameter(MARKER_IMAGE));
        else
            returnedParameters.add(parameters.getParameter(DYNAMIC));
        
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
        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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
                "Image from workspace to apply watershed transform to.  This image will be 8-bit with binary logic determined by the \""
                        + BINARY_LOGIC + "\" parameter.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(USE_MARKERS).setDescription(
                "When selected, this option allows the use of markers to define the starting point of each region.  The marker image to use is specified using the \""
                        + MARKER_IMAGE
                        + "\" parameter.  If not selected, a distance map will be generated for the input binary image and extended minima created according to the dynamic specified by \""
                        + DYNAMIC + "\".");

        parameters.get(MARKER_IMAGE).setDescription("Marker image to be used if \"" + USE_MARKERS
                + "\" is selected.  This image must be of equal dimensions to the input image (to which the transform will be applied).  This image will be 8-bit with binary logic determined by the \""
                + BINARY_LOGIC + "\" parameter.");

        parameters.get(INTENSITY_MODE).setDescription(
                "Controls the source for the intensity image against which the watershed transform will be computed.  Irrespective of mode, the image (raw image or object distance map) will act as a surface that the starting points will evolve up until adjacent regions come into contact (at which point creating a dividing line between the two):<br><ul>"

                        + "<li>\"" + IntensityModes.DISTANCE
                        + "\" A distance map will be created from the input binary image and used as the surface against which the watershed regions will evolve.</li>"

                        + "<li>\"" + IntensityModes.INPUT_IMAGE
                        + "\" The watershed regions will evolve against an image from the workspace.  This image will be unaffected by this process.  The image should have lower intensity coincident with the markers, rising to higher intensity along the boundaries between regions. </li></ul>");

        parameters.get(INTENSITY_IMAGE).setDescription("If \"" + INTENSITY_MODE + "\" is set to \""
                + IntensityModes.INPUT_IMAGE
                + "\", this is the image from the workspace against which the watershed regions will evolve.  The image should have lower intensity coincident with the markers, rising to higher intensity along the boundaries between regions.");

        parameters.get(DYNAMIC).setDescription("If \"" + USE_MARKERS
                + "\" is not selected, the initial region markers will be created by generating a distance map for the input binary image and calculating the extended minima.  This parameter specifies the maximum permitted pixel intensity difference for a single marker.  Local intensity differences greater than this will result in creation of more markers.  The smaller the dynamic value is, the more the watershed transform will split the image.");

        parameters.get(CONNECTIVITY).setDescription("Controls which adjacent pixels are considered:<br><ul>"

                + "<li>\"" + Connectivity.SIX
                + "\" Only pixels immediately next to the active pixel are considered.  These are the pixels on the four \"cardinal\" directions plus the pixels immediately above and below the current pixel.  If working in 2D, 4-way connectivity is used.</li>"

                + "<li>\"" + Connectivity.TWENTYSIX
                + "\" In addition to the core 6-pixels, all immediately diagonal pixels are used.  If working in 2D, 8-way connectivity is used.</li>");

        parameters.get(MATCH_Z_TO_X).setDescription(
                "When selected, an image is interpolated in Z (so that all pixels are isotropic) prior to calculation of a distance map.  This prevents warping of the distance map along the Z-axis if XY and Z sampling aren't equal.");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple 3D stacks simultaneously.  Since the watershed transform is applied on a single 3D stack at a time, multithreading only works for images with multiple channels or timepoints (other stacks will still work, but won't see a speed improvement).  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
