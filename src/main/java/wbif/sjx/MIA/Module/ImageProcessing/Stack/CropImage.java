// TODO: Figure out why ImageJFunctions.wrap() ImagePlus behaves badly with RunTrackMate tracking

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
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Process.ImgPlusTools;

public class CropImage < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CROP_SEPARATOR = "Crop selection";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";

    public CropImage(ModuleCollection modules) {
        super(modules);
    }

    public static <T extends RealType< T > & NativeType< T >> Image cropImage(Image<T> inputImage, String outputImageName, int top, int left, int width, int height) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        int xIdx = inputImg.dimensionIndex(Axes.X);
        int yIdx = inputImg.dimensionIndex(Axes.Y);

        long[] offsetIn = new long[inputImg.numDimensions()];
        long[] dimsIn = new long[inputImg.numDimensions()];
        for (int i=0;i<inputImg.numDimensions();i++) dimsIn[i] = inputImg.dimension(i);
        offsetIn[xIdx] = left;
        offsetIn[yIdx] = top;
        dimsIn[xIdx] = width;
        dimsIn[yIdx] = height;

        long[] offsetOut = new long[inputImg.numDimensions()];
        long[] dimsOut = new long[inputImg.numDimensions()];
        for (int i=0;i<inputImg.numDimensions();i++) dimsOut[i] = inputImg.dimension(i);
        dimsOut[xIdx] = width;
        dimsOut[yIdx] = height;

        // Creating the output image and copying over the pixel coordinates
        CellImgFactory<T> factory = new CellImgFactory<>(inputImg.firstElement());
        ImgPlus<T> outputImg = new ImgPlus<>(factory.create(dimsOut));
        ImgPlusTools.copyAxes(inputImg,outputImg);

        RandomAccess<T> randomAccessIn = Views.offsetInterval(inputImg,offsetIn,dimsIn).randomAccess();
        Cursor<T> cursorOut = outputImg.localizingCursor();

        while (cursorOut.hasNext()) {
            cursorOut.fwd();
            randomAccessIn.setPosition(cursorOut);
            cursorOut.get().set(randomAccessIn.get());
        }

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg,outputImageName);
        outputImagePlus.setCalibration(inputImagePlus.getCalibration());
        ImgPlusTools.applyAxes(outputImg,outputImagePlus);

        return new Image(outputImageName,outputImagePlus);

    }

    @Override
    public String getTitle() {
        return "Crop image";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int left = parameters.getValue(LEFT);
        int top = parameters.getValue(TOP);
        int width = parameters.getValue(WIDTH);
        int height = parameters.getValue(HEIGHT);

        Image outputImage = cropImage(inputImage,outputImageName,top,left,width,height);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.setImagePlus(outputImage.getImagePlus());
            if (showOutput) inputImage.showImage();

        } else {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ParamSeparatorP(CROP_SEPARATOR,this));
        parameters.add(new IntegerP(LEFT,this,0));
        parameters.add(new IntegerP(TOP,this,0));
        parameters.add(new IntegerP(WIDTH,this,512));
        parameters.add(new IntegerP(HEIGHT,this,512));
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

        returnedParameters.add(parameters.getParameter(CROP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LEFT));
        returnedParameters.add(parameters.getParameter(TOP));
        returnedParameters.add(parameters.getParameter(WIDTH));
        returnedParameters.add(parameters.getParameter(HEIGHT));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
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
