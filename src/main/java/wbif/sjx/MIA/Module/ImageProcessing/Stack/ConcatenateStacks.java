package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultLinearAxis;
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
import wbif.sjx.common.Process.ImgPlusTools;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.LinkedHashSet;

public class ConcatenateStacks <T extends RealType<T> & NativeType<T>> extends Module {
    public static final String ADD_INPUT_IMAGE = "Add image";
    public static final String INPUT_IMAGE = "Input image";
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

    long getCombinedAxisLength(ImgPlus<T> img1, ImgPlus<T> img2, AxisType axis) {
        long lengthIn1 = getAxisLength(img1,axis);
        long lengthIn2 = getAxisLength(img2,axis);

        return lengthIn1 + lengthIn2;

    }

    boolean checkAxisEquality(ImgPlus<T> img1, ImgPlus<T> img2, AxisType axis) {
        long lengthIn1 = getAxisLength(img1,axis);
        long lengthIn2 = getAxisLength(img2,axis);

        return lengthIn1 == lengthIn2;

    }

    long getAxisLength(ImgPlus<T> img, AxisType axis) {
        int idxIn = img.dimensionIndex(axis);
        return idxIn == -1 ? 1 : img.dimension(idxIn);

    }

    void copyPixels(ImgPlus<T> sourceImg, ImgPlus targetImg, long[] offset, long[] dims) {
        int xIdxIn1 = sourceImg.dimensionIndex(Axes.X);
        int yIdxIn1 = sourceImg.dimensionIndex(Axes.Y);
        int cIdxIn1 = sourceImg.dimensionIndex(Axes.CHANNEL);
        int zIdxIn1 = sourceImg.dimensionIndex(Axes.Z);
        int tIdxIn1 = sourceImg.dimensionIndex(Axes.TIME);

        // Adding the first image to the output
        Cursor<T> cursor1 = sourceImg.localizingCursor();
        RandomAccess<T> randomAccess1 = Views.offsetInterval(targetImg,offset,dims).randomAccess();
        while (cursor1.hasNext()) {
            cursor1.fwd();

            // Getting position
            long[] posIn = new long[sourceImg.numDimensions()];
            cursor1.localize(posIn);

            // Assigning position
            if (xIdxIn1 == -1) randomAccess1.setPosition(0,0);
            else randomAccess1.setPosition(posIn[xIdxIn1],0);

            if (yIdxIn1 == -1) randomAccess1.setPosition(0,1);
            else randomAccess1.setPosition(posIn[yIdxIn1],1);

            if (cIdxIn1 == -1) randomAccess1.setPosition(0,2);
            else randomAccess1.setPosition(posIn[cIdxIn1],2);

            if (zIdxIn1 == -1) randomAccess1.setPosition(0,3);
            else randomAccess1.setPosition(posIn[zIdxIn1],3);

            if (tIdxIn1 == -1) randomAccess1.setPosition(0,4);
            else randomAccess1.setPosition(posIn[tIdxIn1],4);

            randomAccess1.get().set(cursor1.get());

        }
    }

    public ImgPlus<T> concatenateImages(ImgPlus<T> img1, ImgPlus<T> img2, String axis) {
        long[] dimsOutCombined = new long[5];
        long[] offsetOut1 = new long[5];
        long[] offsetOut2 = new long[5];
        long[] dimsOut1 = ImgPlusTools.getDimensionsXYCZT(img1);
        long[] dimsOut2 = ImgPlusTools.getDimensionsXYCZT(img2);

        if (axis.equals(AxisModes.X)) {
            dimsOutCombined[0] = getCombinedAxisLength(img1, img2, Axes.X);
            offsetOut2[0] = getAxisLength(img1, Axes.X);
//        } else if (!checkAxisEquality(img1,img2,Axes.X)) {
//            System.err.println("[Concatenate stacks] X-axis lengths not equal.");
//            return null;
        } else {
            dimsOutCombined[0] = getAxisLength(img1, Axes.X);
        }

        if (axis.equals(AxisModes.Y)) {
            dimsOutCombined[1] = getCombinedAxisLength(img1, img2, Axes.Y);
            offsetOut2[1] = getAxisLength(img1, Axes.Y);
//        } else if (!checkAxisEquality(img1,img2,Axes.Y)) {
//            System.err.println("[Concatenate stacks] y-axis lengths not equal.");
//            return null;
        } else {
            dimsOutCombined[1] = getAxisLength(img1, Axes.Y);
        }

        if (axis.equals(AxisModes.CHANNEL)) {
            dimsOutCombined[2] = getCombinedAxisLength(img1, img2, Axes.CHANNEL);
            offsetOut2[2] = getAxisLength(img1, Axes.CHANNEL);
//        } else if (!checkAxisEquality(img1,img2,Axes.CHANNEL)) {
//            System.err.println("[Concatenate stacks] channel-axis lengths not equal.");
//            return null;
        } else {
            dimsOutCombined[2] = getAxisLength(img1, Axes.CHANNEL);
        }

        if (axis.equals(AxisModes.Z)) {
            dimsOutCombined[3] = getCombinedAxisLength(img1, img2, Axes.Z);
            offsetOut2[3] = getAxisLength(img1, Axes.Z);
//        } else if (!checkAxisEquality(img1,img2,Axes.Z)) {
//            System.err.println("[Concatenate stacks] Z-axis lengths not equal.");
//            return null;
        } else {
            dimsOutCombined[3] = getAxisLength(img1, Axes.Z);
        }

        if (axis.equals(AxisModes.TIME)) {
            dimsOutCombined[4] = getCombinedAxisLength(img1, img2, Axes.TIME);
            offsetOut2[4] = getAxisLength(img1, Axes.TIME);
//        } else if (!checkAxisEquality(img1,img2,Axes.TIME)) {
//            System.err.println("[Concatenate stacks] time-axis lengths not equal.");
//            return null;
        } else {
            dimsOutCombined[4] = getAxisLength(img1, Axes.TIME);
        }

        // Creating the new Img
        CellImgFactory<T> factory = new CellImgFactory<>((T) img1.firstElement());
        ImgPlus<T> imgOut = new ImgPlus<T>(factory.create(dimsOutCombined));
        imgOut.setAxis(new DefaultLinearAxis(Axes.X,1),0);
        imgOut.setAxis(new DefaultLinearAxis(Axes.Y,1),1);
        imgOut.setAxis(new DefaultLinearAxis(Axes.CHANNEL,1),2);
        imgOut.setAxis(new DefaultLinearAxis(Axes.Z,1),3);
        imgOut.setAxis(new DefaultLinearAxis(Axes.TIME,1),4);

        copyPixels(img1,imgOut,offsetOut1,dimsOut1);
        copyPixels(img2,imgOut,offsetOut2,dimsOut2);

        return imgOut;

    }

    public Image<T> concatenateImages(Image<T>[] inputImages, String axis, String outputImageName) {
        // Processing first two images
        ImgPlus<T> imgOut = concatenateImages(inputImages[0].getImgPlus(),inputImages[1].getImgPlus(),axis);

        // Appending any additional images
        for (int i=2;i<inputImages.length;i++) {
            imgOut = concatenateImages(imgOut,inputImages[i].getImgPlus(),axis);
        }

        // If concatenation failed (for example, if the dimensions were inconsistent) it returns null
        if (imgOut == null) return null;

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(imgOut,outputImageName);
        outputImagePlus.setCalibration(inputImages[0].getImagePlus().getCalibration());
        ImgPlusTools.applyAxes(imgOut,outputImagePlus);

        return new Image<T>(outputImageName,outputImagePlus);

    }

    private void convertToComposite(Image<T> image) {
        ImagePlus ipl = image.getImagePlus();

        ipl = new Duplicator().run(HyperStackConverter.toHyperStack(ipl,ipl.getNChannels(),ipl.getNSlices(),ipl.getNFrames(),"xyczt","Composite"));

        // Updating the display range to help show all the colours
        IntensityMinMax.run(ipl,true,0.001,IntensityMinMax.PROCESS_FAST);

        image.setImagePlus(ipl);

    }

    @Override
    public String getTitle() {
        return "Concatenate stacks";
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
    protected boolean process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String axisMode = parameters.getValue(AXIS_MODE);

        // Creating a collection of images
        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_INPUT_IMAGE);
        Image[] inputImages = new Image[collections.size()];
        int i=0;
        for (ParameterCollection collection:collections) {
            inputImages[i++] = workspace.getImage(collection.getValue(INPUT_IMAGE));
        }

        // Applying concatenation
        Image outputImage = concatenateImages(inputImages, axisMode, outputImageName);
        if (outputImage == null) return false;

        if (axisMode.equals(AxisModes.CHANNEL)) {
            convertToComposite(outputImage);
        }

        if (showOutput) outputImage.showImage();
        workspace.addImage(outputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        ParameterCollection collection = new ParameterCollection();
        collection.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new ParameterGroup(ADD_INPUT_IMAGE,this,collection,2));

        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(AXIS_MODE,this,AxisModes.X,AxisModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

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
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }
}
