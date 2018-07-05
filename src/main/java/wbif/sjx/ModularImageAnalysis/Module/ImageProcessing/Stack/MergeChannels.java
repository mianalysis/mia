package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import fiji.stacks.Hyperstack_rearranger;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.ImgPlus;
import net.imagej.autoscale.DefaultAutoscaleMethod;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.IdentityAxis;
import net.imagej.interval.DefaultCalibratedRealInterval;
import net.imglib2.Cursor;
import net.imglib2.RealInterval;
import net.imglib2.img.ImagePlusAdapter;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sc13967 on 22/02/2018.
 */
public class MergeChannels< T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE1 = "Input image 1";
    public static final String INPUT_IMAGE2 = "Input image 2";
    public static final String OUTPUT_IMAGE = "Output image";

    private ImgPlus<T> combineImages(Image inputImage1, Image inputImage2) {
        ImgPlus<T> img1 = inputImage1.getImgPlus();
        ImgPlus<T> img2 = inputImage2.getImgPlus();

        int nDimsFinal = img1.numDimensions();
        if (!inputImage1.getImagePlus().isHyperStack()) nDimsFinal++;

        int xDim1 = img1.dimensionIndex(Axes.X);
        int yDim1 = img1.dimensionIndex(Axes.Y);
        int cDim1 = img1.dimensionIndex(Axes.CHANNEL);
        int zDim1 = img1.dimensionIndex(Axes.Z);
        int tDim1 = img1.dimensionIndex(Axes.TIME);
        int xDim2 = img2.dimensionIndex(Axes.X);
        int yDim2 = img2.dimensionIndex(Axes.Y);
        int cDim2 = img2.dimensionIndex(Axes.CHANNEL);
        int zDim2 = img2.dimensionIndex(Axes.Z);
        int tDim2 = img2.dimensionIndex(Axes.TIME);

        long[] dimsIn = new long[5];
        long[] dimsOut = new long[5];
        long[] offsetIn = new long[5];
        long[] offsetOut = new long[5];

        dimsIn[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsIn[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsIn[2] = cDim1 == -1 ? 1 : img1.dimension(cDim1);
        dimsIn[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsIn[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        dimsOut[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsOut[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsOut[2] = (cDim1 == -1 ? 1 : img1.dimension(cDim1)) + (cDim2 == -1 ? 1 : img2.dimension(cDim2));
        dimsOut[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsOut[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        offsetIn[0] = 0;
        offsetIn[1] = 0;
        offsetIn[2] = 0;
        offsetIn[3] = 0;
        offsetIn[4] = 0;

        for (int i=0;i<dimsOut.length;i++) System.out.println("DO "+dimsOut[i]);

//        for (int i=0;i<img1.numDimensions();i++) {
//            offset[i] = 0;
//            dims[i] = img1.dimension(i);
//        }
//
//        if (inputImage1.getImagePlus().isHyperStack() && inputImage2.getImagePlus().isHyperStack()) {
//            dims[2] = img1.dimension(2) + img2.dimension(2);
//        } else if(inputImage1.getImagePlus().isHyperStack() &! inputImage2.getImagePlus().isHyperStack()) {
//            dims[2] = img1.dimension(2) + 1;
//        } else {
//            dims[2] = 2;
//            dims[3] = img1.dimension(2);
//        }

        // Creating the composite image
        T type = img1.firstElement();
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        ImgPlus<T> mergedImg = new ImgPlus<>(factory.create(dimsOut, type));

        if (cDim1 == -1) {
            mergedImg.setAxis(new IdentityAxis(Axes.CHANNEL),2);
        } else {
            mergedImg.setAxis(img1.axis(cDim1),2);
        }

        if (zDim1 == -1) {
            mergedImg.setAxis(new IdentityAxis(Axes.Z),3);
        } else {
            mergedImg.setAxis(img1.axis(zDim1),3);
        }

        if (tDim1 == -1) {
            mergedImg.setAxis(new IdentityAxis(Axes.TIME),4);
        } else {
            mergedImg.setAxis(img1.axis(tDim1),4);
        }

//        // Adding values from image 1
//        if (inputImage1.getImagePlus().isHyperStack() && inputImage2.getImagePlus().isHyperStack()) {
//            dims[2] = img1.dimension(2);
//        } else if(inputImage1.getImagePlus().isHyperStack() &! inputImage2.getImagePlus().isHyperStack()) {
//            dims[2] = img1.dimension(2);
//        } else {
//            dims[2] = 1;
//        }

        Cursor<T> cursorIn = img1.cursor();
        Cursor<T> cursorOut = Views.offsetInterval(mergedImg, offsetIn, dimsIn).cursor();
        while (cursorIn.hasNext()) cursorOut.next().set(cursorIn.next());

//        if (inputImage1.getImagePlus().isHyperStack() && inputImage2.getImagePlus().isHyperStack()) {
//            offset[2] = img1.dimension(2);
//            dims[2] = img2.dimension(2);
//        } else if(inputImage1.getImagePlus().isHyperStack() &! inputImage2.getImagePlus().isHyperStack()) {
//            dims[2] = 1;
//            offset[2] = img1.dimension(2);
//        } else {
//            dims[2] = 1;
//            offset[2] = 1;
//        }

        Cursor<T> cursor2 = img2.cursor();
        cursorOut = Views.offsetInterval(mergedImg, offset2, dims2).cursor();
        while (cursor2.hasNext()) cursorOut.next().set(cursor2.next());
//
        return mergedImg;

    }

    private Img<T> createComposite(Image inputImageRed, Image inputImageGreen, Image inputImageBlue) {
        long dimX = 0;
        long dimY = 0;
        long dimZ = 0;
        T type = null;

        Img<T> redImg = null;
        if (inputImageRed != null) {
            redImg = inputImageRed.getImgPlus();
            dimX = redImg.dimension(0);
            dimY = redImg.dimension(1);
            dimZ = redImg.dimension(2);
            type = redImg.firstElement();
        }

        Img<T> greenImg = null;
        if (inputImageGreen != null) {
            greenImg = inputImageGreen.getImgPlus();
            dimX = greenImg.dimension(0);
            dimY = greenImg.dimension(1);
            dimZ = greenImg.dimension(2);
            type = greenImg.firstElement();
        }

        Img<T> blueImg = null;
        if (inputImageBlue != null) {
            blueImg = inputImageBlue.getImgPlus();
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
        IntensityMinMax.run(compositeImage,true,0.001);
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
