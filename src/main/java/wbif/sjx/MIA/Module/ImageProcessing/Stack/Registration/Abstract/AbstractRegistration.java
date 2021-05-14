package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract;

import java.util.ArrayList;

import com.drew.lang.annotations.Nullable;

import fiji.stacks.Hyperstack_rearranger;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.HyperStackConverter;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;

public abstract class AbstractRegistration<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String REGISTRATION_AXIS = "Registration axis";
    public static final String OTHER_AXIS_MODE = "Other axis mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String FILL_MODE = "Fill mode";

    public static final String REFERENCE_SEPARATOR = "Reference image source";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String NUM_PREV_FRAMES = "Number of previous frames";
    public static final String PREV_FRAMES_STAT_MODE = "Previous frames statistic";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";
    
    public AbstractRegistration(String name, ModuleCollection modules) {
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

    public static void changeStackOrder(Image image) {
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

    public static boolean testReferenceValidity(Image inputImage, Image calculationImage, String otherAxisMode) {
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

    public static <T extends RealType<T> & NativeType<T>> Image createOverlay(Image<T> inputImage, Image<T> referenceImage) {
        // Only create the overlay if the two images have matching dimensions
        ImagePlus ipl1 = inputImage.getImagePlus();
        ImagePlus ipl2 = referenceImage.getImagePlus();

        if (ipl1.getNSlices() == ipl2.getNSlices() && ipl1.getNFrames() == ipl2.getNFrames()) {
            String axis = ConcatenateStacks.AxisModes.CHANNEL;
            ArrayList<Image<T>> images = new ArrayList<>();
            images.add(inputImage);
            images.add(referenceImage);
            return ConcatenateStacks.concatenateImages(images, axis, "Overlay");
        }

        return inputImage;

    }

    synchronized public static ImagePlus getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, int slice,
            @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice,
                    timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel, slice, timepoint);
            inputImagePlus.setProcessor(toPut);
            inputImagePlus.updateAndDraw();
            return null;
        }
    }

    public static void replaceStack(Image targetImage, Image sourceImage, int channel, int timepoint) {
        ImagePlus targetIpl = targetImage.getImagePlus();
        ImagePlus sourceIpl = sourceImage.getImagePlus();
        ImageStack targetIst = targetIpl.getStack();
        ImageStack sourceIst = sourceIpl.getStack();

        for (int z = 0; z < sourceIpl.getNSlices(); z++) {
            int sourceIdx = sourceIpl.getStackIndex(1, z + 1, 1);
            int targetIdx = targetIpl.getStackIndex(channel + 1, z + 1, timepoint + 1);

            targetIst.setProcessor(sourceIst.getProcessor(sourceIdx), targetIdx);

        }
        targetImage.getImagePlus().updateAndDraw();
    }

    public static void replaceSlice(Image targetImage, Image sourceSlice, int slice) {
        ImagePlus targetIpl = targetImage.getImagePlus();
        ImagePlus sourceIpl = sourceSlice.getImagePlus();
        ImageStack targetIst = targetIpl.getStack();
        ImageStack sourceIst = sourceIpl.getStack();

        for (int channel = 0; channel < targetIpl.getNChannels(); channel++) {
            for (int timepoint = 0; timepoint < targetIpl.getNFrames(); timepoint++) {
                int sourceIdx = sourceIpl.getStackIndex(channel + 1, 1, timepoint + 1);
                int targetIdx = targetIpl.getStackIndex(channel + 1, slice + 1, timepoint + 1);

                targetIst.setProcessor(sourceIst.getProcessor(sourceIdx), targetIdx);

            }
        }
        targetImage.getImagePlus().updateAndDraw();
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REGISTRATION_SEPARATOR, this));
        parameters.add(new ChoiceP(REGISTRATION_AXIS, this, RegistrationAxes.TIME, RegistrationAxes.ALL));
        parameters.add(new ChoiceP(OTHER_AXIS_MODE, this, OtherAxisModes.INDEPENDENT, OtherAxisModes.ALL));        
        parameters.add(new ChoiceP(FILL_MODE, this, FillModes.BLACK, FillModes.ALL));
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
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REGISTRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REGISTRATION_AXIS));
        returnedParameters.add(parameters.getParameter(OTHER_AXIS_MODE));        
        returnedParameters.add(parameters.getParameter(FILL_MODE));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.PREVIOUS_N_FRAMES:
                returnedParameters.add(parameters.getParameter(NUM_PREV_FRAMES));
                returnedParameters.add(parameters.getParameter(PREV_FRAMES_STAT_MODE));
                break;
            case ReferenceModes.SPECIFIC_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
        switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));
        
        return returnedParameters;

    }

    protected void addParameterDescriptions() {        
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply registration to.");

        parameters.get(APPLY_TO_INPUT).setDescription("When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
        + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(REGISTRATION_AXIS).setDescription("Controls which stack axis the registration will be applied in.  For example, when \""+RegistrationAxes.TIME+"\" is selected, all images along the time axis will be aligned.  Choices are: " + String.join(", ", RegistrationAxes.ALL) + ".");

        parameters.get(OTHER_AXIS_MODE).setDescription("For stacks with non-registration axis lengths longer than 1 (e.g. the \"Z\" axis when registering in time) the behaviour of this other axis is controlled by this parameter:<br><ul>"

        +"<li>\""+OtherAxisModes.INDEPENDENT+"\" Each non-registration axis is registered independently.  For example, applying separate Z-registrations for each timepoint of a 4D stack.</li>"

        +"<li>\""+OtherAxisModes.LINKED+"\" All elements of the non-registration axis are registered with a single transform.  For example, applying the same registration at a timepoint to all slices of a 4D stack.</li></ul>");

        parameters.get(FILL_MODE).setDescription("Controls what intensity any border pixels will have.  \"Borders\" in this case correspond to strips/wedges at the image edge corresponding to regions outside the initial image (e.g. the right-side of an output image when the input was translated to the left).   Choices are: "+String.join(", ",FillModes.ALL)+".");

        parameters.get(ENABLE_MULTITHREADING).setDescription("When selected, certain parts of the registration process will be run on multiple threads of the CPU.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
