package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

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
    public static final String MERGE_MODE = "Merging mode";
    public static final String USE_RED = "Use red channel";
    public static final String INPUT_IMAGE_RED = "Input image (red)";
    public static final String USE_GREEN = "Use green channel";
    public static final String INPUT_IMAGE_GREEN = "Input image (green)";
    public static final String USE_BLUE = "Use blue channel";
    public static final String INPUT_IMAGE_BLUE = "Input image (blue)";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    interface MergingMode {
        String COMPSITE = "Composite";
        String RGB = "RGB";

        String[] ALL = new String[]{COMPSITE,RGB};

    }

    private Img<ARGBType> createRGB(Image inputImageRed, Image inputImageGreen, Image inputImageBlue) {
        long dimX = 0;
        long dimY = 0;

        Img<T> redImg = null;
        if (inputImageRed != null) {
            redImg = inputImageRed.getImg();
            dimX = redImg.dimension(0);
            dimY = redImg.dimension(1);
        }

        Img<T> greenImg = null;
        if (inputImageGreen != null) {
            greenImg = inputImageGreen.getImg();
            dimX = greenImg.dimension(0);
            dimY = greenImg.dimension(1);
        }

        Img<T> blueImg = null;
        if (inputImageBlue != null) {
            blueImg = inputImageBlue.getImg();
            dimX = blueImg.dimension(0);
            dimY = blueImg.dimension(1);
        }

        // Creating the RGB image
        long[] dimensions = new long[]{dimX,dimY};
        final ImgFactory<ARGBType> factory = new ArrayImgFactory<>();
        Img<ARGBType> rgbImg = factory.create(dimensions, new ARGBType());

        Cursor<ARGBType> cursorSingle = rgbImg.cursor();
        Cursor<T> cursorRed = inputImageRed != null ? redImg.cursor() : null;
        Cursor<T> cursorGreen = inputImageGreen != null? greenImg.cursor() : null;
        Cursor<T> cursorBlue = inputImageBlue != null ? blueImg.cursor() : null;

        while (cursorSingle.hasNext()) {
            double r = inputImageRed != null ? cursorRed.next().getRealDouble() : 0;
            double g = inputImageGreen != null ? cursorGreen.next().getRealDouble() : 0;
            double b = inputImageBlue != null ? cursorBlue.next().getRealDouble() : 0;

            cursorSingle.next().set(ARGBType.rgba(r,g,b,1));

        }

        return rgbImg;

    }

    private Img<T> createComposite(Image inputImageRed, Image inputImageGreen, Image inputImageBlue) {
        long dimX = 0;
        long dimY = 0;
        T type = null;

        Img<T> redImg = null;
        if (inputImageRed != null) {
            redImg = inputImageRed.getImg();
            dimX = redImg.dimension(0);
            dimY = redImg.dimension(1);
            type = redImg.firstElement();
        }

        Img<T> greenImg = null;
        if (inputImageGreen != null) {
            greenImg = inputImageGreen.getImg();
            dimX = greenImg.dimension(0);
            dimY = greenImg.dimension(1);
            type = greenImg.firstElement();
        }

        Img<T> blueImg = null;
        if (inputImageBlue != null) {
            blueImg = inputImageBlue.getImg();
            dimX = blueImg.dimension(0);
            dimY = blueImg.dimension(1);
            type = blueImg.firstElement();
        }

        // Creating the RGB image
        long[] dimensions = new long[]{dimX,dimY,3};
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        Img<T> rgbImg = factory.create(dimensions, type);

        // Adding values view
        if (inputImageRed != null) {
            Cursor<T> cursorSingle = redImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 0}, new long[]{dimX,dimY, 1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageGreen != null) {
            Cursor<T> cursorSingle = greenImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 1}, new long[]{dimX, dimY, 1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageBlue != null) {
            Cursor<T> cursorSingle = blueImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 2}, new long[]{dimX, dimY, 1}).cursor();
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
        String mergeMode = parameters.getValue(MERGE_MODE);

        String inputImageRedName = parameters.getValue(INPUT_IMAGE_RED);
        Image inputImageRed = workspace.getImage(inputImageRedName);

        String inputImageGreenName = parameters.getValue(INPUT_IMAGE_GREEN);
        Image inputImageGreen = workspace.getImage(inputImageGreenName);

        String inputImageBlueName = parameters.getValue(INPUT_IMAGE_BLUE);
        Image inputImageBlue = workspace.getImage(inputImageBlueName);

        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

        switch (mergeMode) {
            case MergingMode.COMPSITE:
                Img<T> compositeImage = createComposite(inputImageRed,inputImageGreen,inputImageBlue);
                Image outputImage = new Image(outputImageName,compositeImage);
                workspace.addImage(outputImage);

                if (showImage) ImageJFunctions.show(compositeImage);

                break;

            case MergingMode.RGB:
                Img<ARGBType> rgbImage = createRGB(inputImageRed, inputImageGreen, inputImageBlue);
                outputImage = new Image(outputImageName,rgbImage);
                workspace.addImage(outputImage);

                if (showImage) ImageJFunctions.show(rgbImage);

                break;

        }


    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(MERGE_MODE,Parameter.CHOICE_ARRAY,MergingMode.COMPSITE,MergingMode.ALL));
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

        returnedParameters.add(parameters.getParameter(MERGE_MODE));
        returnedParameters.add(parameters.getParameter(USE_RED));
        if (parameters.getValue(USE_RED)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_RED));

        returnedParameters.add(parameters.getParameter(USE_GREEN));
        if (parameters.getValue(USE_GREEN)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_GREEN));

        returnedParameters.add(parameters.getParameter(USE_BLUE));
        if (parameters.getValue(USE_BLUE)) returnedParameters.add(parameters.getParameter(INPUT_IMAGE_BLUE));

        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
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
