package io.github.mianalysis.mia.module.images.transform.registration.abstrakt;

import java.awt.Color;
import java.util.ArrayList;

import com.drew.lang.annotations.Nullable;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.CanvasResizer;
import ij.plugin.HyperStackConverter;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.images.transform.ConcatenateStacks2;
import io.github.mianalysis.mia.module.images.transform.Convert3DStack;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.images.transform.ProjectImage;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectCentroid;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.PointPair;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImagePlusImage;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public abstract class AbstractRegistration<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String REGISTRATION_AXIS = "Registration axis";
    public static final String OTHER_AXIS_MODE = "Other axis mode";
    public static final String FILL_MODE = "Fill mode";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String SHOW_DETECTED_POINTS = "Show detected points";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public static final String REFERENCE_SEPARATOR = "Reference ImageI source";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String NUM_PREV_FRAMES = "Number of previous frames";
    public static final String PREV_FRAMES_STAT_MODE = "Previous frames statistic";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";

    public AbstractRegistration(String name, ModulesI modules) {
        super(name, modules);
    }

    public interface RegistrationAxes {
        String TIME = "Time";
        String Z = "Z";

        String[] ALL = new String[] { TIME, Z };

    }

    public interface OtherAxisModes {
        String INDEPENDENT = "Independent";
        String LINKED = "Linked";

        String[] ALL = new String[] { INDEPENDENT, LINKED };

    }

    public interface FillModes {
        String BLACK = "Black";
        String WHITE = "White";

        String[] ALL = new String[] { BLACK, WHITE };

    }

    public interface ReferenceModes {
        String FIRST_FRAME = "First frame";
        String PREVIOUS_N_FRAMES = "Previous N frames";
        String SPECIFIC_IMAGE = "Specific image";

        String[] ALL = new String[] { FIRST_FRAME, PREVIOUS_N_FRAMES, SPECIFIC_IMAGE };

    }

    public interface PrevFramesStatModes extends ProjectImage.ProjectionModes {
    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public abstract Param createParameterSet();

    public abstract Transform getTransform(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param,
            boolean showDetectedPoints);

    public abstract ImageProcessor applyTransform(ImageProcessor inputIpl, Transform transform, int fillValue);

    public static int getFillValue(String fillMode, ImageProcessor ipr) {
        switch (fillMode) {
            case FillModes.BLACK:
            default:
                return 0;
            case FillModes.WHITE:
                return (int) Math.round(Math.pow(2, ipr.getBitDepth())) - 1;
        }
    }

    public void processIndependent(ImageI inputImage, ImageI calculationImage, String referenceMode, int numPrevFrames,
            String prevFrameStatMode, Param param, String fillMode, boolean showDetectedPoints, boolean multithread,
            @Nullable ImageI reference) {
        // This works in a very similar manner to processLinked, except it's performed
        // one slice at a time
        for (int z = 0; z < inputImage.getImagePlus().getNSlices(); z++) {
            // Getting the current slices (input and calculation)
            ImageI currInputImage = ExtractSubstack.extractSubstack(inputImage, inputImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");
            ImageI currCalcImage = ExtractSubstack.extractSubstack(calculationImage, calculationImage.getName(),
                    "1-end",
                    String.valueOf(z + 1), "1-end");

            // Performing the registration on this slice
            processLinked(currInputImage, currCalcImage, referenceMode, numPrevFrames, prevFrameStatMode, param,
                    fillMode, showDetectedPoints, multithread, reference);

            // Replacing all images in this slice of the input with the registered images
            replaceSlice(inputImage, currInputImage, z);

        }
    }

    public void processLinked(ImageI inputImage, ImageI calculationImage, String referenceMode, int numPrevFrames,
            String prevFrameStatMode, Param param, String fillMode, boolean showDetectedPoints, boolean multithread,
            @Nullable ImageI reference) {
        // Assigning fixed reference images
        switch (referenceMode) {
            case ReferenceModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1", "1");
                break;

            case ReferenceModes.SPECIFIC_IMAGE:
                if (reference == null)
                    return;
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int nFrames = calculationImage.getImagePlus().getNFrames();

        for (int t = 0; t < nFrames; t++) {
            count++;

            // Setting the current timepoint
            param.t = t;

            // If the reference image is the previous frame, get this now
            switch (referenceMode) {
                case ReferenceModes.FIRST_FRAME:
                    // We don't need to align the first image if comparing to the first image
                    if (t == 0)
                        continue;
                    break;

                case ReferenceModes.PREVIOUS_N_FRAMES:
                    if (t == 0)
                        continue;

                    int minT = Math.max(1, t - numPrevFrames + 1);
                    ImageI referenceStack = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1",
                            minT + "-" + t);
                    Convert3DStack.process(referenceStack.getImagePlus(), Convert3DStack.Modes.OUTPUT_Z_STACK);
                    reference = ProjectImage.projectImageInZ(referenceStack, "Reference", prevFrameStatMode);

                    break;
            }

            // Getting the calculation image at this time-point
            ImageI warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1",
                    String.valueOf(t + 1));

            // Calculating the transformation for this image pair
            Transform transform = getTransform(reference.getImagePlus().getProcessor(),
                    warped.getImagePlus().getProcessor(), param, showDetectedPoints);

            if (transform == null) {
                MIA.log.writeWarning("Unable to align images at position " + (t + 1));
                continue;
            }

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int c = 0; c < inputImage.getImagePlus().getNChannels(); c++) {
                ImageI warpedChannel = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c + 1),
                        "1-end", String.valueOf(t + 1));

                try {
                    applyTransformation(warpedChannel, transform, fillMode, multithread);
                } catch (InterruptedException e) {
                }

                replaceStack(inputImage, warpedChannel, c + 1, t + 1);

            }

            // Need to apply the warp to an external image
            if (referenceMode.equals(ReferenceModes.PREVIOUS_N_FRAMES)) {
                warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1", String.valueOf(t + 1));
                try {
                    applyTransformation(warped, transform, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(calculationImage, warped, 1, t + 1);
            }

            transform = null;

            writeProgressStatus(count, nFrames, "frames");

        }
    }

    public static void showDetectedPoints(ImageProcessor referenceIpr, ImageProcessor warpedIpr,
            ArrayList<PointPair> pairs) {
        ObjsI oc = ObjsFactories.getDefaultFactory().createObjs("Im", referenceIpr.getWidth(),
                referenceIpr.getHeight(), 2, 1, 1, "um", 1, 1d, TemporalUnit.getOMEUnit());

        ImagePlus showIpl = IJ.createImage("Detected points", referenceIpr.getWidth(), referenceIpr.getHeight(), 2,
                referenceIpr.getBitDepth());

        showIpl.getStack().setProcessor(referenceIpr, 1);
        for (PointPair pair : pairs) {
            ObjI obj = oc.createAndAddNewObject(new PointListFactory());
            try {
                obj.addCoord((int) Math.round(pair.getPoint2().getXBase()),
                        (int) Math.round(pair.getPoint2().getYBase()),
                        0);
            } catch (PointOutOfRangeException e) {
            }
            AddObjectCentroid.addOverlay(obj, showIpl, Color.RED, AddObjectCentroid.PointSizes.MEDIUM,
                    AddObjectCentroid.PointTypes.DOT, false);
        }

        showIpl.getStack().setProcessor(warpedIpr, 2);
        for (PointPair pair : pairs) {
            ObjI obj = oc.createAndAddNewObject(new PointListFactory());
            try {
                obj.addCoord((int) Math.round(pair.getPoint1().getXBase()),
                        (int) Math.round(pair.getPoint1().getYBase()),
                        1);
            } catch (PointOutOfRangeException e) {
            }
            AddObjectCentroid.addOverlay(obj, showIpl, Color.BLUE, AddObjectCentroid.PointSizes.MEDIUM,
                    AddObjectCentroid.PointTypes.DOT, false);
        }

        showIpl.duplicate().show();

    }

    public void applyTransformation(ImageI inputImage, Transform transform, String fillMode, boolean multithread)
            throws InterruptedException {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        // int nThreads = multithread ? Prefs.getThreads() : 1;
        // ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L,
        // TimeUnit.MILLISECONDS,
        // new LinkedBlockingQueue<>());

        for (int c = 1; c <= nChannels; c++) {
            for (int z = 1; z <= nSlices; z++) {
                for (int t = 1; t <= nFrames; t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    // Runnable task = () -> {
                    ImageProcessor slice = getSetSlice(inputIpl, finalT, finalC, finalZ, null).getProcessor();
                    int fillValue = getFillValue(fillMode, slice);
                    ImageProcessor alignedSlice = applyTransform(slice, transform, fillValue);
                    alignedSlice.setMinAndMax(slice.getMin(), slice.getMax());
                    getSetSlice(inputIpl, finalT, finalC, finalZ, alignedSlice);
                    // };
                    // pool.submit(task);
                }
            }
        }

        // pool.shutdown();
        // pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never
        // terminate early

    }

    public static boolean testReferenceValidity(ImageI inputImage, ImageI calculationImage, String otherAxisMode) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus calculationIpl = calculationImage.getImagePlus();

        // Stacks should already be ordered such that the registration axis is along the
        // time axis. Calculation image should have equal number of frames as the input.
        // Also, if "slices" are linked, it should only have one slice. If "slices" are
        // independent, it should have the same number of slices as the input.
        switch (otherAxisMode) {
            case OtherAxisModes.INDEPENDENT:
                if (calculationIpl.getNSlices() != inputIpl.getNSlices()) {
                    MIA.log.writeWarning(
                            "Non-registration axis of calculation image stack is different length to input image stack.  Calculation stack has "
                                    + calculationIpl.getNSlices() + " images, input stack has " + inputIpl.getNSlices()
                                    + " images.  In \"" + OtherAxisModes.INDEPENDENT
                                    + "\" mode, calculation and input stacks should be the same length along non-registration axis.");
                    return false;
                }
                break;

            case OtherAxisModes.LINKED:
                if (calculationIpl.getNSlices() > 1) {
                    MIA.log.writeWarning("Non-registration axis of calculation image stack is too large ("
                            + calculationIpl.getNSlices() + ").  In \"" + OtherAxisModes.LINKED
                            + "\" mode, calculation stack should have a single image along non-registration axis.");
                    return false;
                }
                break;
        }

        // Irrespective of other axis mode, "time" axis should be the same length
        if (calculationIpl.getNFrames() != inputIpl.getNFrames()) {
            MIA.log.writeWarning("Calculation image stack has different length to input image.  Calculation stack has "
                    + calculationIpl.getNFrames() + " images and, input stack has " + inputIpl.getNFrames()
                    + " images.");
            return false;
        }

        // Reference stack is valid
        return true;

    }

    public static <T extends RealType<T> & NativeType<T>> ImageI<T> createOverlay(ImageI<T> inputImage,
            ImageI<T> referenceImage) {
        // Only create the overlay if the two images have matching dimensions
        ImagePlus ipl1 = inputImage.getImagePlus();
        ImagePlus ipl2 = referenceImage.getImagePlus();

        if (ipl1.getNSlices() == ipl2.getNSlices() && ipl1.getNFrames() == ipl2.getNFrames()) {
            String axis = ConcatenateStacks2.AxisModes.CHANNEL;
            ArrayList<ImageI<T>> images = new ArrayList<>();
            images.add(inputImage);
            images.add(referenceImage);

            return ConcatenateStacks2.process(images, axis, "Overlay");

        }

        return inputImage;

    }

    public static void changeStackOrder(ImageI image) {
        ImagePlus inputIpl = image.getImagePlus();

        // InputIpl must be a HyperStack (but at least be a stack)
        if (!inputIpl.isHyperStack() && (inputIpl.getNFrames() > 1 || inputIpl.getNSlices() > 1)) {
            int nChannels = inputIpl.getNChannels();
            int nSlices = inputIpl.getNSlices();
            int nFrames = inputIpl.getNFrames();

            // Converting to a HyperStack
            inputIpl = HyperStackConverter.toHyperStack(inputIpl, nChannels, nSlices, nFrames);

        }

        // Applying stack reordering
        inputIpl = Hyperstack_rearranger.reorderHyperstack(inputIpl, "CTZ", true, false);

        // Updating image of inputIpl
        image.setImagePlus(inputIpl);

    }

    synchronized public static ImagePlus getSetSlice(ImagePlus inputImagePlus, int timepoint, int channel, int slice,
            @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice,
                    timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel, slice, timepoint);
            inputImagePlus.setProcessor(toPut);
            inputImagePlus.updateAndDraw();
            return null;
        }
    }

    public static void replaceStack(ImageI targetImage, ImageI sourceImage, int channel, int timepoint) {
        ImagePlus targetIpl = targetImage.getImagePlus();
        ImageStack sourceIst = sourceImage.getImagePlus().getStack();

        ImagePlusImage.getSetStack(targetIpl, timepoint, channel, sourceIst);

    }

    public static void replaceSlice(ImageI targetImage, ImageI sourceSlice, int slice) {
        ImagePlus targetIpl = targetImage.getImagePlus();
        ImagePlus sourceIpl = sourceSlice.getImagePlus();

        for (int channel = 0; channel < targetIpl.getNChannels(); channel++) {
            for (int timepoint = 0; timepoint < targetIpl.getNFrames(); timepoint++) {
                int sourceIdx = sourceIpl.getStackIndex(channel + 1, 1, timepoint + 1);
                int targetIdx = targetIpl.getStackIndex(channel + 1, slice + 1, timepoint + 1);

                targetIpl.getStack().setProcessor(sourceIpl.getStack().getProcessor(sourceIdx), targetIdx);

            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM_REGISTRATION;
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String regAxis = parameters.getValue(REGISTRATION_AXIS, workspace);
        String otherAxisMode = parameters.getValue(OTHER_AXIS_MODE, workspace);
        String fillMode = parameters.getValue(FILL_MODE, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        int numPrevFrames = parameters.getValue(NUM_PREV_FRAMES, workspace);
        String prevFramesStatMode = parameters.getValue(PREV_FRAMES_STAT_MODE, workspace);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE, workspace);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE, workspace);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL, workspace);
        boolean showDetectedPoints = parameters.getValue(SHOW_DETECTED_POINTS, workspace);

        // Getting the input image and duplicating if the output will be stored
        // separately
        ImageI inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = ImageFactories.getDefaultFactory().create(outputImageName, inputImage.getImagePlus().duplicate());

        // If comparing to a fixed image, get this now
        ImageI reference = referenceMode.equals(ReferenceModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName)
                : null;

        // Getting the image the registration will be calculated from.
        String calcC = String.valueOf(calculationChannel);
        ImageI calculationImage = null;
        switch (calculationSource) {
            case CalculationSources.EXTERNAL:
                ImageI externalImage = workspace.getImage(externalSourceName);
                calculationImage = ExtractSubstack.extractSubstack(externalImage, "CalcIm", calcC, "1-end", "1-end");
                break;

            case CalculationSources.INTERNAL:
                calculationImage = ExtractSubstack.extractSubstack(inputImage, "CalcIm", calcC, "1-end", "1-end");
                break;
        }

        ImagePlus inputIpl = inputImage.getImagePlus();
        if (reference != null) {
            ImagePlus referenceIpl = reference.getImagePlus();
            ImagePlus calculationIpl = calculationImage.getImagePlus();
            int outW = Math.max(Math.max(inputIpl.getWidth(), referenceIpl.getWidth()), calculationIpl.getWidth());
            int outH = Math.max(Math.max(inputIpl.getHeight(), referenceIpl.getHeight()), calculationIpl.getHeight());
            inputIpl.setStack(new CanvasResizer().expandStack(inputIpl.getImageStack(), outW, outH, 0, 0));
            calculationIpl.setStack(new CanvasResizer().expandStack(calculationIpl.getImageStack(), outW, outH, 0, 0));

            reference = ImageFactories.getDefaultFactory().create(reference.getName(), reference.getImagePlus().duplicate());
            referenceIpl = reference.getImagePlus();
            referenceIpl.setStack(new CanvasResizer().expandStack(referenceIpl.getImageStack(), outW, outH, 0, 0));
        }

        // Registration will be performed in time, so ensure actual axis to be
        // registered is reordered to be in time axis
        switch (regAxis) {
            case RegistrationAxes.Z:
                changeStackOrder(inputImage);
                changeStackOrder(calculationImage);
                break;
        }

        // If non-registration dimension is "linked", calculation image potentially
        // needs to be projected. Since the images have been transformed such that the
        // registration dimension is always "Time", then this is a Z projection. A
        // maximum intensity projection is used. It only needs be performed if there is
        // at least one Z-slice.
        if (calculationImage.getImagePlus().getNSlices() > 1) {
            switch (otherAxisMode) {
                case OtherAxisModes.LINKED:
                    calculationImage = ProjectImage.projectImageInZ(calculationImage, "CalcIm",
                            ProjectImage.ProjectionModes.MAX);
                    break;
            }
        }

        // Ensuring calculation image has the correct dimensions
        if (testReferenceValidity(inputImage, calculationImage, otherAxisMode)) {
            // Setting up the parameters specific to this module
            Param param = createParameterSet();
            getParameters(param, workspace);

            switch (otherAxisMode) {
                case OtherAxisModes.INDEPENDENT:
                    processIndependent(inputImage, calculationImage, referenceMode, numPrevFrames, prevFramesStatMode,
                            param, fillMode, showDetectedPoints, multithread, reference);
                    break;

                case OtherAxisModes.LINKED:
                    processLinked(inputImage, calculationImage, referenceMode, numPrevFrames, prevFramesStatMode, param,
                            fillMode, showDetectedPoints, multithread, reference);
                    break;
            }

            // If stack order was adjusted, now swap it back
            switch (regAxis) {
                case RegistrationAxes.Z:
                    changeStackOrder(inputImage);
                    changeStackOrder(calculationImage);
                    break;
            }

        } else {
            MIA.log.writeWarning("Input stack has not been registered");
        }

        if (showOutput) {
            if (referenceMode.equals(ReferenceModes.SPECIFIC_IMAGE)) {
                createOverlay(inputImage, reference).showAsIs();
            } else {
                inputImage.showAsIs();
            }
        }

        // Dealing with module outputs
        if (!applyToInput)
            workspace.addImage(inputImage);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REGISTRATION_SEPARATOR, this));
        parameters.add(new ChoiceP(REGISTRATION_AXIS, this, RegistrationAxes.TIME, RegistrationAxes.ALL));
        parameters.add(new ChoiceP(OTHER_AXIS_MODE, this, OtherAxisModes.INDEPENDENT, OtherAxisModes.ALL));
        parameters.add(new ChoiceP(FILL_MODE, this, FillModes.BLACK, FillModes.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_DETECTED_POINTS, this, false));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.FIRST_FRAME, ReferenceModes.ALL));
        parameters.add(new IntegerP(NUM_PREV_FRAMES, this, 1));
        parameters.add(new ChoiceP(PREV_FRAMES_STAT_MODE, this, PrevFramesStatModes.MAX, PrevFramesStatModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));
        parameters.add(new IntegerP(CALCULATION_CHANNEL, this, 1));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REGISTRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REGISTRATION_AXIS));
        returnedParameters.add(parameters.getParameter(OTHER_AXIS_MODE));
        returnedParameters.add(parameters.getParameter(FILL_MODE));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_DETECTED_POINTS));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE, workspace)) {
            case ReferenceModes.PREVIOUS_N_FRAMES:
                returnedParameters.add(parameters.getParameter(NUM_PREV_FRAMES));
                returnedParameters.add(parameters.getParameter(PREV_FRAMES_STAT_MODE));
                break;
            case ReferenceModes.SPECIFIC_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
        }
        returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
        switch ((String) parameters.getValue(CALCULATION_SOURCE, workspace)) {
            case CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
        }
        returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));

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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply registration to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(REGISTRATION_AXIS)
                .setDescription("Controls which stack axis the registration will be applied in.  For example, when \""
                        + RegistrationAxes.TIME
                        + "\" is selected, all images along the time axis will be aligned.  Choices are: "
                        + String.join(", ", RegistrationAxes.ALL) + ".");

        parameters.get(OTHER_AXIS_MODE).setDescription(
                "For stacks with non-registration axis lengths longer than 1 (e.g. the \"Z\" axis when registering in time) the behaviour of this other axis is controlled by this parameter:<br><ul>"

                        + "<li>\"" + OtherAxisModes.INDEPENDENT
                        + "\" Each non-registration axis is registered independently.  For example, applying separate Z-registrations for each timepoint of a 4D stack.</li>"

                        + "<li>\"" + OtherAxisModes.LINKED
                        + "\" All elements of the non-registration axis are registered with a single transform.  For example, applying the same registration at a timepoint to all slices of a 4D stack.</li></ul>");

        parameters.get(FILL_MODE).setDescription(
                "Controls what intensity any border pixels will have.  \"Borders\" in this case correspond to strips/wedges at the image edge corresponding to regions outside the initial image (e.g. the right-side of an output image when the input was translated to the left).   Choices are: "
                        + String.join(", ", FillModes.ALL) + ".");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "When selected, certain parts of the registration process will be run on multiple threads of the CPU.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

        parameters.get(REFERENCE_MODE)
                .setDescription("Controls what reference image each image will be compared to:<br><ul>"

                        + "<li>\"" + ReferenceModes.FIRST_FRAME
                        + "\" All images will be compared to the first frame (or slice when in Z-axis mode).  For image sequences which continuously evolve over time (e.g. cells dividing) this can lead to reduced likelihood of successfully calculating the transform over time.</li>"

                        + "<li>\"" + ReferenceModes.PREVIOUS_N_FRAMES
                        + "\" Each image will be compared to the N frames (or slice when in Z-axis mode) immediately before it (number of frames specified by \""
                        + NUM_PREV_FRAMES
                        + "\").  These reference frames are consolidated into a single reference image using a projection based on the statistic specified by \""
                        + PREV_FRAMES_STAT_MODE
                        + "\".  This mode copes better with image sequences which continuously evolve over time, but can also lead to compounding errors over time (errors in registration get propagated to all remaining slices).</li>"

                        + "<li>\"" + ReferenceModes.SPECIFIC_IMAGE
                        + "\" All images will be compared to a separate 2D image from the workspace.  The image to compare to is selected using the \""
                        + REFERENCE_IMAGE + "\" parameter.</li></ul>");

        parameters.get(REFERENCE_IMAGE).setDescription("If \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.SPECIFIC_IMAGE
                + "\" mode, all input images will be registered relative to this image.  This image must only have a single channel, slice and timepoint.");

        parameters.get(NUM_PREV_FRAMES)
                .setDescription("Number of previous frames (or slices) to use as reference image when \""
                        + REFERENCE_MODE + "\" is set to \"" + ReferenceModes.PREVIOUS_N_FRAMES
                        + "\".  If there are insufficient previous frames (e.g. towards the beginning of the stack) the maximum available frames will be used.  Irrespective of the number of frames used, the images will be projected into a single reference image using the statistic specified by \""
                        + PREV_FRAMES_STAT_MODE + "\".");

        parameters.get(PREV_FRAMES_STAT_MODE)
                .setDescription("Statistic to use when combining multiple previous frames as a reference (\""
                        + REFERENCE_MODE + "\" set to \"" + ReferenceModes.PREVIOUS_N_FRAMES + "\").");

        parameters.get(CALCULATION_SOURCE).setDescription(
                "Controls whether the input image will be used to calculate the registration transform or whether it will be determined from a separate image:<br><ul>"

                        + "<li>\"" + CalculationSources.EXTERNAL
                        + "\" The transform is calculated from a separate image from the workspace (specified using \""
                        + EXTERNAL_SOURCE
                        + "\").  This could be an image with enhanced contrast (to enable better feature extraction), but where the enhancements are not desired in the output registered image.  When \""
                        + OTHER_AXIS_MODE + "\" is set to \"" + OtherAxisModes.LINKED
                        + "\", the external image must be the same length along the registration axis and have single-valued length along the non-registration axis.  However, when set to \""
                        + OtherAxisModes.INDEPENDENT
                        + "\", the external image must have the same axis lengths for both the registration and non-registration axes.</li>"

                        + "<li>\"" + CalculationSources.INTERNAL
                        + "\" The transform is calculated from the input image.</li></ul>");

        parameters.get(EXTERNAL_SOURCE).setDescription("If \"" + CALCULATION_SOURCE + "\" is set to \""
                + CalculationSources.EXTERNAL
                + "\", registration transforms will be calculated using this image from the workspace.  This image will be unaffected by the process.");

        parameters.get(CALCULATION_CHANNEL).setDescription(
                "If calculating the registration transform from a multi-channel image stack, the transform will be determined from this channel only.  Irrespectively, for multi-channel image stacks, the calculated transform will be applied equally to all channels.");

        parameters.get(SHOW_DETECTED_POINTS).setDescription(
                "When enabled, the points used for calculation of the registration will be added as an overlay to the input image and displayed.");

    }

    public void getParameters(Param param, WorkspaceI workspace) {
        param.fillMode = parameters.getValue(FILL_MODE, workspace);
    }

    public class Param {
        public int t = 0;
        public String fillMode = FillModes.BLACK;
    }

    public class Transform {
    }
}
