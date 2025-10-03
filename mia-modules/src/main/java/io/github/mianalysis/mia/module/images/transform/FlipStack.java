package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImgPlusTools;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


/**
* Flips the order of slices in stack.  This operation can be performed on the channel, time or Z axis.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FlipStack<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image to process.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* If selected, the flipped image will replace the input image in the workspace.  All measurements associated with the input image will be transferred to the flipped image.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* Name of the output flipped image.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String FLIP_SEPARATOR = "Stack flip controls";

	/**
	* Axis along which to flip the image.
	*/
    public static final String AXIS_MODE = "Axis mode";

    public FlipStack(Modules modules) {
        super("Flip stack",modules);
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

    public ImageI applyFlip(ImageI inputImage, String axis, String outputImageName) {
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        long[] dims = new long[inputImg.numDimensions()];
        for (int i=0;i<inputImg.numDimensions();i++) dims[i] = inputImg.dimension(i);

        // Creating the new Img
        DiskCachedCellImgFactory<T> factory = new DiskCachedCellImgFactory<T>((T) inputImg .firstElement());
        DiskCachedCellImg dcImage = factory.create(dims);
        ImgPlus<T> outputImg = new ImgPlus<T>(dcImage);
        ImgPlusTools.copyAxes(inputImg,outputImg);
 
        // Determining the axis index
        int axisIndex = getAxesIndex(inputImg, axis);
        if (axisIndex == -1)
            return ImageFactory.createImage(outputImageName, inputImage.getImagePlus());
        
        long[] offsetIn = new long[inputImg.numDimensions()];
        long[] offsetOut = new long[outputImg.numDimensions()];
        offsetOut[axisIndex] = -dims[axisIndex] + 1;

        IntervalView<T> source = Views.offsetInterval(inputImg,offsetIn,dims);
        IntervalView<T> target = Views.offsetInterval(Views.invertAxis(outputImg,axisIndex),offsetOut,dims);
        LoopBuilder.setImages(source,target).forEachPixel((s,t) -> t.set(s));

        // Cursor<T> targetCursor = Views.offsetInterval(Views.invertAxis(outputImg,axisIndex),offsetOut,dims).localizingCursor();
        // RandomAccess<T> sourceRandomAccess = Views.offsetInterval(inputImg,offsetIn,dims).randomAccess();
        // while (targetCursor.hasNext()) {
        //     targetCursor.fwd();
        //     sourceRandomAccess.setPosition(targetCursor);
        //     targetCursor.get().set(sourceRandomAccess.get());
        // }

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg,outputImageName);
        outputImagePlus.setCalibration(inputImage.getImagePlus().getCalibration());
        ImgPlusTools.applyDimensions(outputImg,outputImagePlus);

        dcImage.shutdown();
        
        return ImageFactory.createImage(outputImageName,outputImagePlus);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Flips the order of slices in stack.  This operation can be performed on the channel, time or Z axis.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String axisMode = parameters.getValue(AXIS_MODE,workspace);

        // If applying to a new image, the input image is duplicated
        if (applyToInput) outputImageName = inputImageName;

        // Applying flip
        ImageI outputImage = applyFlip(inputImage, axisMode, outputImageName);
        if (outputImage == null) return Status.FAIL;

        if (showOutput) outputImage.showAsIs();
        if (applyToInput) {
            inputImage.setImagePlus(outputImage.getImagePlus());
        } else {
            workspace.addImage(outputImage);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to process."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true, "If selected, the flipped image will replace the input image in the workspace.  All measurements associated with the input image will be transferred to the flipped image."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output flipped image."));

        parameters.add(new SeparatorP(FLIP_SEPARATOR,this));
        parameters.add(new ChoiceP(AXIS_MODE, this,AxisModes.X,AxisModes.ALL,"Axis along which to flip the image."));

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(FLIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(AXIS_MODE));

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
}
