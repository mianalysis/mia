package wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.drew.lang.annotations.Nullable;

import fiji.stacks.Hyperstack_rearranger;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.HyperStackConverter;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import mpicbg.ij.Mapping;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.RigidModel2D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.TranslationModel2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;

public abstract class CoreRegistrationHandler<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String REGISTRATION_AXIS = "Registration axis";
    public static final String OTHER_AXIS_MODE = "Other axis mode";
    public static final String ALIGNMENT_MODE = "Alignment mode";
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String FILL_MODE = "Fill mode";

    public CoreRegistrationHandler(String name, ModuleCollection modules) {
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

    public interface AlignmentModes {
        final String AUTOMATIC = "Automatic (feature extraction)";
        final String MANUAL = "Manual (landmarks)";

        final String[] ALL = new String[] { AUTOMATIC, MANUAL };

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

    public static void changeStackOrder(Image image) {
        ImagePlus inputIpl = image.getImagePlus();

        // InputIpl must be a HyperStack
        if (!inputIpl.isHyperStack()) {
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

    public static void applyTransformation(Image inputImage, Mapping mapping, String fillMode, boolean multithread)
            throws InterruptedException {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        if (fillMode.equals(FillModes.WHITE))
            InvertIntensity.process(inputImage);

        for (int c = 1; c <= nChannels; c++) {
            for (int z = 1; z <= nSlices; z++) {
                for (int t = 1; t <= nFrames; t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    Runnable task = () -> {
                        ImageProcessor slice = getSetStack(inputIpl, finalT, finalC, finalZ, null).getProcessor();
                        slice.setInterpolationMethod(ImageProcessor.BILINEAR);
                        ImageProcessor alignedSlice = slice.createProcessor(slice.getWidth(), slice.getHeight());
                        alignedSlice.setMinAndMax(slice.getMin(), slice.getMax());
                        mapping.mapInterpolated(slice, alignedSlice);

                        getSetStack(inputIpl, finalT, finalC, finalZ, alignedSlice);

                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        if (fillMode.equals(FillModes.WHITE))
            InvertIntensity.process(inputImage);

    }

    protected static AbstractAffineModel2D getModel(String transformationMode) {
        switch (transformationMode) {
            case TransformationModes.AFFINE:
                return new AffineModel2D();
            case TransformationModes.RIGID:
            default:
                return new RigidModel2D();
            case TransformationModes.SIMILARITY:
                return new SimilarityModel2D();
            case TransformationModes.TRANSLATION:
                return new TranslationModel2D();
        }
    }

    synchronized private static ImagePlus getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, int slice,
            @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice,
                    timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel, slice, timepoint);
            inputImagePlus.setProcessor(toPut);
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
        parameters.add(new ChoiceP(TRANSFORMATION_MODE, this, TransformationModes.RIGID, TransformationModes.ALL));
        parameters.add(new ChoiceP(ALIGNMENT_MODE, this, AlignmentModes.AUTOMATIC, AlignmentModes.ALL));
        parameters.add(new ChoiceP(FILL_MODE, this, FillModes.BLACK, FillModes.ALL));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

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
        returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(ALIGNMENT_MODE));
        returnedParameters.add(parameters.getParameter(FILL_MODE));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    protected static class Param extends FloatArray2DSIFT.Param {
        /**
         *
         */
        private static final long serialVersionUID = -9039231442503621671L;
        String transformationMode = TransformationModes.RIGID;
        float rod = 0.92f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;

    }
}
