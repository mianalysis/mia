// TODO: Figure out why ImageJFunctions.wrap() ImagePlus behaves badly with RunTrackMate tracking

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.Duplicator;
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

public class CropImage < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";

    public Image cropImage(Image<T> inputImage, String outputImageName, int top, int left, int width, int height) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        Img<T> img = inputImage.getImgPlus();

        long[] min = new long[img.numDimensions()];
        long[] dimsIn = new long[img.numDimensions()];
        min[0] = left;
        min[1] = top;
        dimsIn[0] = width;
        dimsIn[1] = height;
        for (int i=2;i<img.numDimensions();i++) {
            min[i] = 0;
            dimsIn[i] = img.dimension(i);
        }

        long[] dimsOut = new long[5];
        dimsOut[0] = width;
        dimsOut[1] = height;
        dimsOut[2] = inputImagePlus.getNChannels();
        dimsOut[3] = inputImagePlus.getNSlices();
        dimsOut[4] = inputImagePlus.getNFrames();

        // Creating the output image and copying over the pixel coordinates
        ArrayImgFactory<T> factory = new ArrayImgFactory<T>();
        Img<T> outputImg = factory.create(dimsOut,img.firstElement());
        Cursor<T> cropCursor = Views.offsetInterval(img,min,dimsIn).cursor();
        Cursor<T> outputCursor = outputImg.cursor();

        while (cropCursor.hasNext()) outputCursor.next().set(cropCursor.next());

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = new Duplicator().run(ImageJFunctions.wrap(outputImg,outputImageName));
        outputImagePlus.setCalibration(inputImagePlus.getCalibration());
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
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
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
            if (showOutput) showImage(inputImage);

        } else {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            workspace.addImage(outputImage);
            if (showOutput) showImage(outputImage);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(LEFT, Parameter.INTEGER,0));
        parameters.add(new Parameter(TOP, Parameter.INTEGER,0));
        parameters.add(new Parameter(WIDTH, Parameter.INTEGER,512));
        parameters.add(new Parameter(HEIGHT, Parameter.INTEGER,512));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(LEFT));
        returnedParameters.add(parameters.getParameter(TOP));
        returnedParameters.add(parameters.getParameter(WIDTH));
        returnedParameters.add(parameters.getParameter(HEIGHT));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
