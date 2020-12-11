package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.util.ArrayList;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.models.AbstractAffineModel2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConcatenateStacks;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Convert3DStack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.UnwarpImages;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public abstract class AutomaticRegistration<T extends RealType<T> & NativeType<T>> extends AbstractRegistrationHandler<T> {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String REGISTRATION_AXIS = "Registration axis";
    public static final String OTHER_AXIS_MODE = "Other axis mode";
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String FILL_MODE = "Fill mode";

    public static final String REFERENCE_SEPARATOR = "Reference image source";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String NUM_PREV_FRAMES = "Number of previous frames";
    public static final String PREV_FRAMES_STAT_MODE = "Previous frames statistic";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";


    public AutomaticRegistration(String name, ModuleCollection modules) {
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

    public interface RelativeModes {
        String FIRST_FRAME = "First frame";
        String PREVIOUS_N_FRAMES = "Previous N frames";
        String SPECIFIC_IMAGE = "Specific image";

        String[] ALL = new String[] { FIRST_FRAME, PREVIOUS_N_FRAMES, SPECIFIC_IMAGE };

    }

    public interface PrevFramesStatModes extends ProjectImage.ProjectionModes {}

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public interface TransformationModes {
        String AFFINE = "Affine";
        String RIGID = "Rigid";
        String SIMILARITY = "Similarity";
        String TRANSLATION = "Translation";

        String[] ALL = new String[] { AFFINE, RIGID, SIMILARITY, TRANSLATION };

    }

    public interface FillModes {
        String BLACK = "Black";
        String WHITE = "White";

        String[] ALL = new String[] { BLACK, WHITE };

    }


    boolean testReferenceValidity(Image inputImage, Image calculationImage, String otherAxisMode) {
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

    public void processIndependent(Image inputImage, Image calculationImage, String relativeMode, int numPrevFrames, String prevFrameStatMode, Param param, String fillMode, boolean multithread, @Nullable Image reference) {
        // This works in a very similar manner to processLinked, except it's performed
        // one slice at a time
        for (int z = 0; z < inputImage.getImagePlus().getNSlices(); z++) {
            // Getting the current slices (input and calculation)
            Image currInputImage = ExtractSubstack.extractSubstack(inputImage, inputImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");
            Image currCalcImage = ExtractSubstack.extractSubstack(calculationImage, calculationImage.getName(), "1-end",
                    String.valueOf(z + 1), "1-end");

            // Performing the registration on this slice
            processLinked(currInputImage, currCalcImage, relativeMode, numPrevFrames, prevFrameStatMode, param, fillMode, multithread, reference);

            // Replacing all images in this slice of the input with the registered images
            replaceSlice(inputImage, currInputImage, z);

        }
    }

    public void processLinked(Image inputImage, Image calculationImage, String relativeMode, int numPrevFrames, String prevFrameStatMode,  Param param,
            String fillMode, boolean multithread, @Nullable Image reference) {
        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1", "1");
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null)
                    return;
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int nFrames = calculationImage.getImagePlus().getNFrames();

        for (int t = 0; t < nFrames; t++) {
            MIA.log.writeDebug(t);
            writeStatus("Processing frame " + (++count) + " of " + nFrames);

            // If the reference image is the previous frame, get this now
            switch (relativeMode) {
                case RelativeModes.PREVIOUS_N_FRAMES:
                 if (t == 0)
                    continue;

                int minT = Math.max(1, t - numPrevFrames+1);
                Image referenceStack = ExtractSubstack.extractSubstack(calculationImage, "Reference", "1", "1",
                        minT + "-" + t);
                Convert3DStack.process(referenceStack.getImagePlus(), Convert3DStack.Modes.OUTPUT_Z_STACK);
                reference = ProjectImage.projectImageInZ(referenceStack, "Reference", prevFrameStatMode);

                break;
            }

            // Getting the calculation image at this time-point
            Image warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1", String.valueOf(t + 1));

            // Calculating the transformation for this image pair
            AbstractAffineModel2D model = getAffineModel2D(reference.getImagePlus().getProcessor(),
                    warped.getImagePlus().getProcessor(), param);

            if (model == null)
                MIA.log.writeDebug("Model null");

            InverseTransformMapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
            MIA.log.writeDebug(mapping.getTransform());
            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same
            // transformation.
            for (int c = 0; c < inputImage.getImagePlus().getNChannels(); c++) {
                warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c + 1), "1-end",
                        String.valueOf(t + 1));

                try {
                    applyTransformation(warped, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }

                replaceStack(inputImage, warped, c, t);

            }

            // Need to apply the warp to an external image
            if (relativeMode.equals(RelativeModes.PREVIOUS_N_FRAMES)) {
                warped = ExtractSubstack.extractSubstack(calculationImage, "Warped", "1", "1", String.valueOf(t + 1));
                try {
                    applyTransformation(warped, mapping, fillMode, multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(calculationImage, warped, 0, t);
            }

            mapping = null;

        }
    }

    public abstract AbstractAffineModel2D getAffineModel2D(ImageProcessor referenceIpr, ImageProcessor warpedIpr, Param param);

    static <T extends RealType<T> & NativeType<T>> Image createOverlay(Image<T> inputImage, Image<T> referenceImage) {
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


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK_REGISTRATION;
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new ChoiceP(RELATIVE_MODE, this, RelativeModes.FIRST_FRAME, RelativeModes.ALL));
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

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RELATIVE_MODE));
        switch ((String) parameters.getValue(RELATIVE_MODE)) {
            case RelativeModes.PREVIOUS_N_FRAMES:
                returnedParameters.add(parameters.getParameter(NUM_PREV_FRAMES));
                returnedParameters.add(parameters.getParameter(PREV_FRAMES_STAT_MODE));
                break;
            case RelativeModes.SPECIFIC_IMAGE:
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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(RELATIVE_MODE)
                .setDescription("Controls what reference image each image will be compared to:<br><ul>"

                        + "<li>\"" + RelativeModes.FIRST_FRAME
                        + "\" All images will be compared to the first frame (or slice when in Z-axis mode).  For image sequences which continuously evolve over time (e.g. cells dividing) this can lead to reduced likelihood of successfully calculating the transform over time.</li>"

                        + "<li>\"" + RelativeModes.PREVIOUS_N_FRAMES
                        + "\" Each image will be compared to the N frames (or slice when in Z-axis mode) immediately before it (number of frames specified by \""+NUM_PREV_FRAMES+"\").  These reference frames are consolidated into a single reference image using a projection based on the statistic specified by \""+PREV_FRAMES_STAT_MODE+"\".  This mode copes better with image sequences which continuously evolve over time, but can also lead to compounding errors over time (errors in registration get propagated to all remaining slices).</li>"

                        + "<li>\"" + RelativeModes.SPECIFIC_IMAGE
                        + "\" All images will be compared to a separate 2D image from the workspace.  The image to compare to is selected using the \""
                        + REFERENCE_IMAGE + "\" parameter.</li></ul>");

        parameters.get(REFERENCE_IMAGE).setDescription("If \"" + RELATIVE_MODE + "\" is set to \""
                + RelativeModes.SPECIFIC_IMAGE
                + "\" mode, all input images will be registered relative to this image.  This image must only have a single channel, slice and timepoint.");

        parameters.get(NUM_PREV_FRAMES).setDescription("Number of previous frames (or slices) to use as reference image when \""+RELATIVE_MODE+"\" is set to \""+RelativeModes.PREVIOUS_N_FRAMES+"\".  If there are insufficient previous frames (e.g. towards the beginning of the stack) the maximum available frames will be used.  Irrespective of the number of frames used, the images will be projected into a single reference image using the statistic specified by \""+PREV_FRAMES_STAT_MODE+"\".");

        parameters.get(PREV_FRAMES_STAT_MODE).setDescription("Statistic to use when combining multiple previous frames as a reference (\""+RELATIVE_MODE+"\" set to \""+RelativeModes.PREVIOUS_N_FRAMES+"\").");

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

    }

    public abstract class Param {
        String transformationMode = TransformationModes.RIGID;

    }
}
