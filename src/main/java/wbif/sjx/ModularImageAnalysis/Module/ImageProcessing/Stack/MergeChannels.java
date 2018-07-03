package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import fiji.stacks.Hyperstack_rearranger;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.autoscale.DefaultAutoscaleMethod;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 22/02/2018.
 */
public class MergeChannels< T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE1 = "Input image 1";
    public static final String INPUT_IMAGE2 = "Input image 2";
    public static final String OUTPUT_IMAGE = "Output image";

    private Img<T> combineImages(Image inputImage1, Image inputImage2) {
        Img<T> img1 = inputImage1.getImg();
        Img<T> img2 = inputImage2.getImg();

        int nDimsFinal = img1.numDimensions();
        if (!inputImage1.getImagePlus().isHyperStack()) nDimsFinal++;

        long[] dims = new long[nDimsFinal];
        long[] offset = new long[nDimsFinal];

        for (int i=0;i<img1.numDimensions();i++) {
            offset[i] = 0;
            dims[i] = img1.dimension(i);
        }

        if (inputImage1.getImagePlus().isHyperStack()) {
            dims[2] = img1.dimension(2) + img2.dimension(2);
        } else {
            dims[2] = 2;
            dims[3] = img1.dimension(2);
        }

        // Creating the composite image
        T type = img1.firstElement();
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        Img<T> mergedImg = factory.create(dims, type);

        // Adding values from image 1
        if (!inputImage1.getImagePlus().isHyperStack()) dims[2] = 1;
        Cursor<T> cursor1 = img1.cursor();
        Cursor<T> cursorMerge = Views.offsetInterval(mergedImg, offset, dims).cursor();
        while (cursor1.hasNext()) cursorMerge.next().set(cursor1.next());

        if (!inputImage1.getImagePlus().isHyperStack())  offset[2] = 1;
        Cursor<T> cursor2 = img2.cursor();
        cursorMerge = Views.offsetInterval(mergedImg, offset, dims).cursor();
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
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting parameters
        String inputImage1Name = parameters.getValue(INPUT_IMAGE1);
        Image inputImage1 = workspace.getImage(inputImage1Name);

        String inputImage2Name = parameters.getValue(INPUT_IMAGE2);
        Image inputImage2 = workspace.getImage(inputImage2Name);

        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        Img<T> mergedImage = combineImages(inputImage1,inputImage2);
        ImagePlus ipl;
        if (mergedImage.firstElement().getClass().isInstance(new UnsignedByteType())) {
            ipl = ImageJFunctions.wrapUnsignedByte(mergedImage,outputImageName);
        } else if (mergedImage.firstElement().getClass().isInstance(new UnsignedShortType())) {
            ipl = ImageJFunctions.wrapUnsignedShort(mergedImage,outputImageName);
        } else {
            ipl = ImageJFunctions.wrapFloat(mergedImage,outputImageName);
        }

        CompositeImage compositeImage = new CompositeImage(ipl,CompositeImage.COMPOSITE);
        compositeImage.setMode(IJ.COMPOSITE);
        compositeImage.setCalibration(inputImage1.getImagePlus().getCalibration());
        Image outputImage = new Image(outputImageName,compositeImage);

        workspace.addImage(outputImage);
        if (showOutput) {
            ImagePlus showIpl = new Duplicator().run(compositeImage);
            showIpl.setTitle(outputImageName);
            showIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE1,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_IMAGE2,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
