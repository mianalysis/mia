package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Process.ImgPlusTools;

public class FlipStack<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String FLIP_SEPARATOR = "Stack flip controls";
    public static final String AXIS_MODE = "Axis mode";

    public FlipStack(ModuleCollection modules) {
        super(modules);
    }


    public interface AxisModes {
        String X = "X";
        String Y = "Y";
        String Z = "Z";
        String CHANNEL = "Channel";
        String TIME = "Time";

        String[] ALL = new String[]{X,Y,Z,CHANNEL,TIME};

    }


    private int getAxesIndex(ImgPlus<T> img, String axis) {
        switch (axis) {
            case AxisModes.X:
                default:
                return img.dimensionIndex(Axes.X);
            case AxisModes.Y:
                return img.dimensionIndex(Axes.Y);
            case AxisModes.Z:
                return img.dimensionIndex(Axes.Z);
            case AxisModes.CHANNEL:
                return img.dimensionIndex(Axes.CHANNEL);
            case AxisModes.TIME:
                return img.dimensionIndex(Axes.TIME);
        }
    }

    public Image<T> applyFlip(Image<T> inputImage, String axis, String outputImageName) {
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        // Creating the new Img
        CellImgFactory<T> factory = new CellImgFactory<T>((T) inputImg .firstElement());
        long[] dims = new long[inputImg.numDimensions()];
        for (int i=0;i<inputImg.numDimensions();i++) dims[i] = inputImg.dimension(i);
        ImgPlus<T> outputImg = new ImgPlus<T>(factory.create(dims));
        ImgPlusTools.copyAxes(inputImg,outputImg);

        // Determining the axis index
        int axisIndex = getAxesIndex(inputImg, axis);
        if (axisIndex == -1) {
            System.err.println("[FlipStack] Specified axis for image flipping doesn't exist.");
            return null;
        }

        long[] offsetIn = new long[inputImg.numDimensions()];
        long[] offsetOut = new long[outputImg.numDimensions()];
        offsetOut[axisIndex] = -dims[axisIndex] + 1;

        Cursor<T> targetCursor = Views.offsetInterval(Views.invertAxis(outputImg,axisIndex),offsetOut,dims).localizingCursor();
        RandomAccess<T> sourceRandomAccess = Views.offsetInterval(inputImg,offsetIn,dims).randomAccess();

        while (targetCursor.hasNext()) {
            targetCursor.fwd();
            sourceRandomAccess.setPosition(targetCursor);
            targetCursor.get().set(sourceRandomAccess.get());
        }

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg,outputImageName);
        outputImagePlus.setCalibration(inputImage.getImagePlus().getCalibration());
        ImgPlusTools.applyAxes(outputImg,outputImagePlus);

        return new Image(outputImageName,outputImagePlus);

    }

    @Override
    public String getTitle() {
        return "Flip stack";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "Flips the order of slices in stack.  This operation can be performed on the channel, time or Z axis.";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String axisMode = parameters.getValue(AXIS_MODE);

        // If applying to a new image, the input image is duplicated
        if (applyToInput) outputImageName = inputImageName;

        // Applying flip
        Image outputImage = applyFlip(inputImage, axisMode, outputImageName);
        if (outputImage == null) return false;

        if (showOutput) outputImage.showImage();
        if (applyToInput) {
            inputImage.setImagePlus(outputImage.getImagePlus());
        } else {
            workspace.addImage(outputImage);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(FLIP_SEPARATOR,this));
        parameters.add(new ChoiceP(AXIS_MODE, this,AxisModes.X,AxisModes.ALL));

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

        returnedParameters.add(parameters.getParameter(FLIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(AXIS_MODE));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
