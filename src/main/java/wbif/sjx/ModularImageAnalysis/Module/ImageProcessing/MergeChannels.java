package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 22/02/2018.
 */
public class MergeChannels< T extends RealType< T > & NativeType< T >> extends Module {
    public static final String USE_RED = "Use red channel";
    public static final String INPUT_IMAGE_RED = "Input image (red)";
    public static final String USE_GREEN = "Use green channel";
    public static final String INPUT_IMAGE_GREEN = "Input image (green)";
    public static final String USE_BLUE = "Use blue channel";
    public static final String INPUT_IMAGE_BLUE = "Input image (blue)";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    private Img<T> combineImages(Image inputImage1, Image inputImage2) {
        Img<T> img1 = inputImage1.getImg();
        Img<T> img2 = inputImage2.getImg();

        T type = img1.firstElement();

        long[] dims1 = new long[img1.numDimensions()];
        long[] dims2 = new long[img2.numDimensions()];
        long[] dims = new long[img1.numDimensions()];

        long[] offset1 = new long[img1.numDimensions()];
        long[] offset2 = new long[img2.numDimensions()];

        for (int i=0;i<img1.numDimensions();i++) {
            System.out.println("Im1 "+img1.dimension(i));
            dims1[i] = img1.dimension(i);
            offset1[i] = 0;
        }

        for (int i=0;i<img2.numDimensions();i++) {
            System.out.println("Im2 "+img2.dimension(i));
            dims2[i] = img2.dimension(i);
            offset2[i] = 0;
        }
        offset2[2] = dims1[2];

        for (int i=0;i<img1.numDimensions();i++) {
            System.out.println("Im "+img1.dimension(i));
            dims[i] = img1.dimension(i);
        }
        dims[2] = dims1[2]+dims2[2];

        // Creating the composite image
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        Img<T> mergedImg = factory.create(dims, type);

        // Adding values from image 1
        Cursor<T> cursor1 = img1.cursor();
        Cursor<T> cursorMerge = Views.offsetInterval(mergedImg, offset1, dims1).cursor();
        while (cursor1.hasNext()) cursorMerge.next().set(cursor1.next());

        Cursor<T> cursor2 = img2.cursor();
        cursorMerge = Views.offsetInterval(mergedImg, offset2, dims2).cursor();
        while (cursor2.hasNext()) cursorMerge.next().set(cursor2.next());

        return mergedImg;

    }

    private Img<T> createComposite(Image inputImageRed, Image inputImageGreen, Image inputImageBlue) {
        long dimX = 0;
        long dimY = 0;
        long dimZ = 0;
        T type = null;

        Img<T> redImg = null;
        if (inputImageRed != null) {
            redImg = inputImageRed.getImg();
            dimX = redImg.dimension(0);
            dimY = redImg.dimension(1);
            dimZ = redImg.dimension(2);
            type = redImg.firstElement();
        }

        Img<T> greenImg = null;
        if (inputImageGreen != null) {
            greenImg = inputImageGreen.getImg();
            dimX = greenImg.dimension(0);
            dimY = greenImg.dimension(1);
            dimZ = greenImg.dimension(2);
            type = greenImg.firstElement();
        }

        Img<T> blueImg = null;
        if (inputImageBlue != null) {
            blueImg = inputImageBlue.getImg();
            dimX = blueImg.dimension(0);
            dimY = blueImg.dimension(1);
            dimZ = blueImg.dimension(2);
            type = blueImg.firstElement();
        }

        // Creating the composite image
        long[] dimensions = new long[]{dimX,dimY,3, dimZ,1};
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        Img<T> rgbImg = factory.create(dimensions, type);

        // Adding values view
        if (inputImageRed != null) {
            Cursor<T> cursorSingle = redImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 0, 0,0}, new long[]{dimX,dimY,1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageGreen != null) {
            Cursor<T> cursorSingle = greenImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 1, 0,0}, new long[]{dimX, dimY, 1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageBlue != null) {
            Cursor<T> cursorSingle = blueImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 2, 0,0}, new long[]{dimX, dimY, 1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        return rgbImg;

    }

    @Override
    public String getTitle() {
        return "Merge channels";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting parameters
        String inputImageRedName = parameters.getValue(INPUT_IMAGE_RED);
        Image inputImageRed = workspace.getImage(inputImageRedName);

        String inputImageGreenName = parameters.getValue(INPUT_IMAGE_GREEN);
        Image inputImageGreen = workspace.getImage(inputImageGreenName);

        String inputImageBlueName = parameters.getValue(INPUT_IMAGE_BLUE);
        Image inputImageBlue = workspace.getImage(inputImageBlueName);

        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

//        Img<T> mergedImage = combineImages(inputImageGreen,inputImageBlue);
//        Image outputImage = new Image(outputImageName,mergedImage);
//        workspace.addImage(outputImage);
//
//        if (showImage) ImageJFunctions.show(mergedImage,"Merged");

        Img<T> compositeImage = createComposite(inputImageRed,inputImageGreen,inputImageBlue);
        Image outputImage = new Image(outputImageName,compositeImage);
        workspace.addImage(outputImage);

        if (showImage) ImageJFunctions.show(compositeImage,"Composite");

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(USE_RED,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE_RED,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(USE_GREEN,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE_GREEN,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(USE_BLUE,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(INPUT_IMAGE_BLUE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(SHOW_IMAGE,Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(USE_RED));
        if (parameters.getValue(USE_RED)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_RED));

        returnedParameters.add(parameters.getParameter(USE_GREEN));
        if (parameters.getValue(USE_GREEN)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_GREEN));

        returnedParameters.add(parameters.getParameter(USE_BLUE));
        if (parameters.getValue(USE_BLUE)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_BLUE));

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
