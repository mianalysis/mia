package io.github.mianalysis.mia.module.imageprocessing.pixel.binary;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.imageprocessing.pixel.InvertIntensity;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
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

public class DilateErode extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String OPERATION_SEPARATOR = "Operation controls";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";
    public static final String BINARY_LOGIC = "Binary logic";

    public DilateErode(Modules modules) {
        super("Dilate and erode", modules);
    }

    public interface OperationModes {
        String DILATE_2D = "Dilate 2D";
        String DILATE_3D = "Dilate 3D";
        String ERODE_2D = "Erode 2D";
        String ERODE_3D = "Erode 3D";

        String[] ALL = new String[] { DILATE_2D, DILATE_3D, ERODE_2D, ERODE_3D };

    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public static void process(ImagePlus ipl, String operationMode, boolean blackBackground, int numIterations) {
        process(ipl, operationMode, blackBackground, numIterations, false);
    }

    public static void process(ImagePlus ipl, String operationMode, boolean blackBackground, int numIterations,
            boolean verbose) {
        String moduleName = new DilateErode(null).getName();

        if (!blackBackground)
            InvertIntensity.process(ipl);

        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        double dppXY = ipl.getCalibration().pixelWidth;
        double dppZ = ipl.getCalibration().pixelDepth;
        double ratio = dppXY / dppZ;

        Strel3D ballStrel = null;
        Strel diskStrel = null;

        int count = 0;
        int total = nChannels * nFrames;
        switch (operationMode) {
            case OperationModes.DILATE_2D:
            case OperationModes.ERODE_2D:
                diskStrel = Strel.Shape.DISK.fromRadius(numIterations);
                break;
            case OperationModes.DILATE_3D:
            case OperationModes.ERODE_3D:
                ballStrel = Strel3D.Shape.BALL.fromRadiusList(numIterations, numIterations,
                        (int) (numIterations * ratio));
                break;
        }

        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                ImageStack istFilt = null;

                switch (operationMode) {
                    case OperationModes.DILATE_2D:
                        istFilt = Morphology.dilation(iplOrig.getImageStack(), diskStrel);
                        break;
                    case OperationModes.DILATE_3D:
                        istFilt = Morphology.dilation(iplOrig.getImageStack(), ballStrel);
                        break;
                    case OperationModes.ERODE_2D:
                        istFilt = Morphology.erosion(iplOrig.getImageStack(), diskStrel);
                        break;
                    case OperationModes.ERODE_3D:
                        istFilt = Morphology.erosion(iplOrig.getImageStack(), ballStrel);
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

                if (verbose)
                    writeProgressStatus(count++, total, "images", moduleName);

            }
        }

        if (!blackBackground)
            InvertIntensity.process(ipl);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Applies binary dilate or erode operations to an image in the workspace.  Dilate will expand all foreground-labelled regions by a specified number of pixels, while erode will shrink all foreground-labelled regions by the same ammount."

                + "<br><br>This image will be 8-bit with binary logic determined by the \""
                + BINARY_LOGIC + "\" parameter.  If 2D operations are applied on higher dimensionality images the operations will be performed in a slice-by-slice manner.  All operations (both 2D and 3D) use the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String operationMode = parameters.getValue(OPERATION_MODE);
        int numIterations = parameters.getValue(NUM_ITERATIONS);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        process(inputImagePlus, operationMode, blackBackground, numIterations);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName, inputImagePlus);
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

        parameters.add(new SeparatorP(OPERATION_SEPARATOR, this));
        parameters.add(new ChoiceP(OPERATION_MODE, this, OperationModes.DILATE_3D, OperationModes.ALL));
        parameters.add(new IntegerP(NUM_ITERATIONS, this, 1));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(OPERATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OPERATION_MODE));
        returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));
        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

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
                "Image from workspace to apply dilate or erode operation to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.  This image will be 8-bit with binary logic determined by the \""
                + BINARY_LOGIC + "\" parameter.");

        parameters.get(OPERATION_MODE).setDescription(
                "Controls what sort of dilate or erode operation is performed on the input image:<br><ul>"

                        + "<li>\"" + OperationModes.DILATE_2D
                        + "\" Change any foreground-connected background pixels to foreground.  This effectively expands objects by one pixel.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.DILATE_3D
                        + "\" Change any foreground-connected background pixels to foreground.  This effectively expands objects by one pixel.  Uses MorphoLibJ implementation.</li>"

                        + "<li>\"" + OperationModes.ERODE_2D
                        + "\" Change any background-connected foreground pixels to background.  This effectively shrinks objects by one pixel.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + OperationModes.ERODE_3D
                        + "\" Change any background-connected foreground pixels to background.  This effectively shrinks objects by one pixel.  Uses MorphoLibJ implementation.</li></ul>");

        parameters.get(NUM_ITERATIONS).setDescription(
                "Number of times the operation will be run on a single image.  Effectively, this allows objects to be dilated or eroded by a specific number of pixels.");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

    }
}
