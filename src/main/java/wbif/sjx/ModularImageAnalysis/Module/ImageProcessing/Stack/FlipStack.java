package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.TypedAxis;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

public class FlipStack<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String AXIS_MODE = "Axis mode";

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
        ImgPlus img = inputImage.getImgPlus();

        // The output image has the same dimensions as the input image
        long[] dims = new long[img.numDimensions()];
        for (int i=0;i<img.numDimensions();i++) {
            dims[i] = img.dimension(i);
        }

        // Creating the new Img
        ArrayImgFactory<T> factory = new ArrayImgFactory<T>((T) img .firstElement());
        Img<T> outputImg = factory.create(dims);

        // Determining the axis index
        int axisIndex = getAxesIndex(img, axis);
        if (axisIndex == -1) {
            System.err.println("[FlipStack] Specified axis for image flipping doesn't exist.");
            return null;
        }

        Cursor<T> inputCursor = Views.invertAxis(img,axisIndex).cursor();
        Cursor<T> outputCursor = outputImg.cursor();

        while (inputCursor.hasNext()) outputCursor.next().set(inputCursor.next());

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = new Duplicator().run(ImageJFunctions.wrap(outputImg,outputImageName));
        outputImagePlus.setCalibration(inputImage.getImagePlus().getCalibration());

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
    protected boolean run(Workspace workspace) {
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
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(AXIS_MODE, this,AxisModes.X,AxisModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(AXIS_MODE));

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
}
