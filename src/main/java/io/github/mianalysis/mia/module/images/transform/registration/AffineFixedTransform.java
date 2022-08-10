package io.github.mianalysis.mia.module.images.transform.registration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.drew.lang.annotations.Nullable;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AffineModel2D;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AffineFixedTransform extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGISTRATION_SEPARATOR = "Registration controls";
    public static final String TRANSFORMATION = "Transformation";
    public static final String FILL_MODE = "Fill mode";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AffineFixedTransform(Modules modules) {
        super("Affine (fixed transform)", modules);
    }

    public interface FillModes {
        String BLACK = "Black";
        String WHITE = "White";

        String[] ALL = new String[] { BLACK, WHITE };

    }

    public static InverseTransformMapping<AbstractAffineModel2D<?>> getMapping(String transform) {
        String[] transformValues = transform.split(",");
        double m00 = Double.parseDouble(transformValues[0]);
        double m10 = Double.parseDouble(transformValues[1]);
        double m01 = Double.parseDouble(transformValues[2]);
        double m11 = Double.parseDouble(transformValues[3]);
        double m02 = Double.parseDouble(transformValues[4]);
        double m12 = Double.parseDouble(transformValues[5]);

        AffineModel2D model = new AffineModel2D();
        model.set(m00, m10, m01, m11, m02, m12);
                
        return new InverseTransformMapping<AbstractAffineModel2D<?>>(model);

    }

    public static ImageProcessor applyTransform(ImageProcessor inputIpr,
            InverseTransformMapping<AbstractAffineModel2D<?>> mapping) {
        inputIpr.setInterpolationMethod(ImageProcessor.BILINEAR);
        ImageProcessor outputIpr = inputIpr.createProcessor(inputIpr.getWidth(), inputIpr.getHeight());

        mapping.mapInterpolated(inputIpr, outputIpr);

        return outputIpr;

    }

    public void applyTransformation(Image inputImage, String transform, String fillMode, boolean multithread)
            throws InterruptedException {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        InverseTransformMapping<AbstractAffineModel2D<?>> mapping = getMapping(transform);

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

                        ImageProcessor alignedSlice = applyTransform(slice, mapping);
                        alignedSlice.setMinAndMax(slice.getMin(), slice.getMax());

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

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM_REGISTRATION;
    }

    @Override
    public Status process(Workspace workspace) {
        IJ.setBackgroundColor(255, 255, 255);

        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String transform = parameters.getValue(TRANSFORMATION,workspace);
        String fillMode = parameters.getValue(FILL_MODE,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        // Getting the input image and duplicating if the output will be stored
        // separately
        Image inputImage = workspace.getImage(inputImageName);
        if (!applyToInput)
            inputImage = ImageFactory.createImage(outputImageName, inputImage.getImagePlus().duplicate());

        // Applying the transformation
        try {
            applyTransformation(inputImage, transform, fillMode, multithread);
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return Status.FAIL;
        }

        if (showOutput)
            inputImage.showImage();

        // Dealing with module outputs
        if (!applyToInput)
            workspace.addImage(inputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REGISTRATION_SEPARATOR, this));
        parameters.add(new StringP(TRANSFORMATION, this));
        parameters.add(new ChoiceP(FILL_MODE, this, FillModes.BLACK, FillModes.ALL));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REGISTRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(TRANSFORMATION));
        returnedParameters.add(parameters.getParameter(FILL_MODE));
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

    protected void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply registration to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(FILL_MODE).setDescription(
                "Controls what intensity any border pixels will have.  \"Borders\" in this case correspond to strips/wedges at the image edge corresponding to regions outside the initial image (e.g. the right-side of an output image when the input was translated to the left).   Choices are: "
                        + String.join(", ", FillModes.ALL) + ".");

        parameters.get(TRANSFORMATION).setDescription("M00,M10,M10,M11,M02,M12)");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "When selected, certain parts of the registration process will be run on multiple threads of the CPU.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
