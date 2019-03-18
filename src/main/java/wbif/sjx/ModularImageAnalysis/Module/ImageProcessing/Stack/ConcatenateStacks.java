package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.common.Process.ImgPlusTools;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.Arrays;
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

//    public ImgPlus<T> concatenateImages(ImgPlus<T> img1, ImgPlus<T> img2, String axis) {
//        // Getting dimensions for the input images
//        long[] offset1 = new long[]{0,0,0,0,0};
//        long[] dims1 = ImgPlusTools.getDimensionsXYCZT(img1);
//
//        long[] offset2 = new long[]{0,0,0,0,0};
//        long[] dims2 = ImgPlusTools.getDimensionsXYCZT(img2);
//
//        long[] dimsOut = new long[5];
//        if (axis.equals(AxisModes.X)) {
//            dimsOut[0] = dims1[0] + dims2[0];
//            offset2[0] = dims1[0];
//        } else if (dims1[0] != dims2[0]) {
//            System.err.println("[Concatenate stacks] X-axis lengths not equal.");
//            return null;
//        } else {
//            dimsOut[0] = dims1[0];
//        }
//
//        if (axis.equals(AxisModes.Y)) {
//            dimsOut[1] = dims1[1] + dims2[1];
//            offset2[1] = dims1[1];
//        } else if (dims1[1] != dims2[1]) {
//            System.err.println("[Concatenate stacks] Y-axis lengths not equal.");
//            return null;
//        } else {
//            dimsOut[1] = dims1[1];
//        }
//
//        if (axis.equals(AxisModes.CHANNEL)) {
//            dimsOut[2] = dims1[2] + dims2[2];
//            offset2[2] = dims1[2];
//        } else if (dims1[2] != dims2[2]) {
//            System.err.println("[Concatenate stacks] channel-axis lengths not equal.");
//            return null;
//        } else {
//            dimsOut[2] = dims1[2];
//        }
//
//        if (axis.equals(AxisModes.Z)) {
//            dimsOut[3] = dims1[3] + dims2[3];
//            offset2[3] = dims1[3];
//        } else if (dims1[3] != dims2[3]) {
//            System.err.println("[Concatenate stacks] Z-axis lengths not equal.");
//            return null;
//        } else {
//            dimsOut[3] = dims1[3];
//        }
//
//        if (axis.equals(AxisModes.TIME)) {
//            dimsOut[4] = dims1[4] + dims2[4];
//            offset2[4] = dims1[4];
//        } else if (dims1[4] != dims2[4]) {
//            System.err.println("[Concatenate stacks] Time-axis lengths not equal.");
//            return null;
//        } else {
//            dimsOut[4] = dims1[4];
//        }
//
//        // Creating the new Img
//        CellImgFactory<T> factory = new CellImgFactory<>((T) img1.firstElement());
//        ImgPlus<T> imgOut = new ImgPlus<T>(factory.create(dimsOut));
//
//        // Adding the first image to the output
//        Cursor<T> cursor1 = img1.cursor();
//        RandomAccess<T> randomAccess1 = Views.offsetInterval(imgOut, offset1, dims1).randomAccess();
//        while (cursor1.hasNext()) {
//            cursor1.fwd();
//            randomAccess1.setPosition(cursor1);
//            randomAccess1.get().set(cursor1.get());
//        }
//
//        // Adding the second image to the output
//        Cursor<T> cursor2 = img2.cursor();
//        RandomAccess<T> randomAccess2 = Views.offsetInterval(imgOut,offset2,dims2).randomAccess();
//        while (cursor2.hasNext()) {
//            cursor2.fwd();
//            randomAccess2.setPosition(cursor2);
//            randomAccess2.get().set(cursor2.get());
//        }
//
//        // Applying calibration from img1 to imgOut
//        ImgPlusTools.applyCalibrationXYCZT(img1,imgOut);
//
//        // Ensuring the extended axis has correct dimensions
//        switch (axis) {
//            case AxisModes.X:
//                if (imgOut.dimensionIndex(Axes.X) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.X,1),0);
//            break;
//            case AxisModes.Y:
//                if (imgOut.dimensionIndex(Axes.Y) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.Y,1),1);
//                break;
//            case AxisModes.CHANNEL:
//                if (imgOut.dimensionIndex(Axes.CHANNEL) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.CHANNEL,1),2);
//                break;
//            case AxisModes.Z:
//                if (imgOut.dimensionIndex(Axes.Z) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.Z,1),3);
//                break;
//            case AxisModes.TIME:
//                if (imgOut.dimensionIndex(Axes.TIME) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.TIME,1),4);
//                break;
//        }
//
//        return imgOut;
//
//    }

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

    public ImgPlus<T> concatenateImages(ImgPlus<T> img1, ImgPlus<T> img2, String axis) {
        // Getting dimensions for the input images
        long[] offsetIn1 = new long[img1.numDimensions()];
        long[] dimsIn1= new long[img1.numDimensions()];
        for (int i=0;i<dimsIn1.length;i++) dimsIn1[i] = img1.dimension(i);

        long[] offsetIn2 = new long[img2.numDimensions()];
        long[] dimsIn2= new long[img2.numDimensions()];
        for (int i=0;i<dimsIn2.length;i++) dimsIn2[i] = img2.dimension(i);

        int idxCount = 0;
        int xOutIdx = (img1.dimensionIndex(Axes.X) != -1 || img2.dimensionIndex(Axes.X) != -1 || axis.equals(AxisModes.X)) ? idxCount++ : -1;
        int yOutIdx = (img1.dimensionIndex(Axes.Y) != -1 || img2.dimensionIndex(Axes.Y) != -1 || axis.equals(AxisModes.Y)) ? idxCount++ : -1;
        int cOutIdx = (img1.dimensionIndex(Axes.CHANNEL) != -1 || img2.dimensionIndex(Axes.CHANNEL) != -1 || axis.equals(AxisModes.CHANNEL)) ? idxCount++ : -1;
        int zOutIdx = (img1.dimensionIndex(Axes.Z) != -1 || img2.dimensionIndex(Axes.Z) != -1 || axis.equals(AxisModes.Z)) ? idxCount++ : -1;
        int tOutIdx = (img1.dimensionIndex(Axes.TIME) != -1 || img2.dimensionIndex(Axes.TIME) != -1 || axis.equals(AxisModes.TIME)) ? idxCount++ : -1;

        long[] dimsOutCombined = new long[idxCount];
        long[] offsetOut1 = new long[idxCount];
        long[] offsetOut2 = new long[idxCount];
        long[] dimsOut1 = new long[idxCount];
        long[] dimsOut2 = new long[idxCount];

        if (xOutIdx != -1) {
            if (axis.equals(AxisModes.X)) {
                dimsOutCombined[xOutIdx] = getCombinedAxisLength(img1, img2, Axes.X);
                offsetOut2[xOutIdx] = getAxisLength(img1, Axes.X);
                dimsOut1[xOutIdx] = getAxisLength(img1, Axes.X);
                dimsOut2[xOutIdx] = getAxisLength(img2, Axes.X);
//        } else if (!checkAxisEquality(img1,img2,Axes.X)) {
//            System.err.println("[Concatenate stacks] X-axis lengths not equal.");
//            return null;
            } else {
                dimsOutCombined[xOutIdx] = getAxisLength(img1, Axes.X);
                dimsOut1[xOutIdx] = getAxisLength(img1, Axes.X);
                dimsOut2[xOutIdx] = getAxisLength(img2, Axes.X);
            }
        }

        if (yOutIdx != -1) {
            if (axis.equals(AxisModes.Y)) {
                dimsOutCombined[yOutIdx] = getCombinedAxisLength(img1, img2, Axes.Y);
                offsetOut2[yOutIdx] = getAxisLength(img1, Axes.Y);
                dimsOut1[yOutIdx] = getAxisLength(img1, Axes.Y);
                dimsOut2[yOutIdx] = getAxisLength(img2, Axes.Y);
//        } else if (!checkAxisEquality(img1,img2,Axes.Y)) {
//            System.err.println("[Concatenate stacks] y-axis lengths not equal.");
//            return null;
            } else {
                dimsOutCombined[yOutIdx] = getAxisLength(img1, Axes.Y);
                dimsOut1[yOutIdx] = getAxisLength(img1, Axes.Y);
                dimsOut2[yOutIdx] = getAxisLength(img2, Axes.Y);
            }
        }

        if (cOutIdx != -1) {
            if (axis.equals(AxisModes.CHANNEL)) {
                dimsOutCombined[cOutIdx] = getCombinedAxisLength(img1, img2, Axes.CHANNEL);
                offsetOut2[cOutIdx] = getAxisLength(img1, Axes.CHANNEL);
                dimsOut1[cOutIdx] = getAxisLength(img1, Axes.CHANNEL);
                dimsOut2[cOutIdx] = getAxisLength(img2, Axes.CHANNEL);
//        } else if (!checkAxisEquality(img1,img2,Axes.CHANNEL)) {
//            System.err.println("[Concatenate stacks] channel-axis lengths not equal.");
//            return null;
            } else {
                dimsOutCombined[cOutIdx] = getAxisLength(img1, Axes.CHANNEL);
                dimsOut1[cOutIdx] = getAxisLength(img1, Axes.CHANNEL);
                dimsOut2[cOutIdx] = getAxisLength(img2, Axes.CHANNEL);
            }
        }

        if (zOutIdx != -1) {
            if (axis.equals(AxisModes.Z)) {
                dimsOutCombined[zOutIdx] = getCombinedAxisLength(img1, img2, Axes.Z);
                offsetOut2[zOutIdx] = getAxisLength(img1, Axes.Z);
                dimsOut1[zOutIdx] = getAxisLength(img1, Axes.Z);
                dimsOut2[zOutIdx] = getAxisLength(img2, Axes.Z);
//        } else if (!checkAxisEquality(img1,img2,Axes.Z)) {
//            System.err.println("[Concatenate stacks] Z-axis lengths not equal.");
//            return null;
            } else {
                dimsOutCombined[zOutIdx] = getAxisLength(img1, Axes.Z);
                dimsOut1[zOutIdx] = getAxisLength(img1, Axes.Z);
                dimsOut2[zOutIdx] = getAxisLength(img2, Axes.Z);
            }
        }

        if (tOutIdx != -1) {
            if (axis.equals(AxisModes.TIME)) {
                dimsOutCombined[tOutIdx] = getCombinedAxisLength(img1, img2, Axes.TIME);
                offsetOut2[tOutIdx] = getAxisLength(img1, Axes.TIME);
                dimsOut1[tOutIdx] = getAxisLength(img1, Axes.TIME);
                dimsOut2[tOutIdx] = getAxisLength(img2, Axes.TIME);
//        } else if (!checkAxisEquality(img1,img2,Axes.TIME)) {
//            System.err.println("[Concatenate stacks] time-axis lengths not equal.");
//            return null;
            } else {
                dimsOutCombined[tOutIdx] = getAxisLength(img1, Axes.TIME);
                dimsOut1[tOutIdx] = getAxisLength(img1, Axes.TIME);
                dimsOut2[tOutIdx] = getAxisLength(img2, Axes.TIME);
            }
        }

        // Creating the new Img
        CellImgFactory<T> factory = new CellImgFactory<>((T) img1.firstElement());
        ImgPlus<T> imgOut = new ImgPlus<T>(factory.create(dimsOutCombined));

        if (xOutIdx != -1) imgOut.setAxis(new DefaultLinearAxis(Axes.X,1),xOutIdx);
        if (yOutIdx != -1) imgOut.setAxis(new DefaultLinearAxis(Axes.Y,1),yOutIdx);
        if (cOutIdx != -1) imgOut.setAxis(new DefaultLinearAxis(Axes.CHANNEL,1),cOutIdx);
        if (zOutIdx != -1) imgOut.setAxis(new DefaultLinearAxis(Axes.Z,1),zOutIdx);
        if (tOutIdx != -1) imgOut.setAxis(new DefaultLinearAxis(Axes.TIME,1),tOutIdx);

        // Adding the first image to the output
        Cursor<T> cursor1 = img1.localizingCursor();
        RandomAccess<T> randomAccess1 = Views.offsetInterval(imgOut,offsetOut1,dimsOut1).randomAccess();
        while (cursor1.hasNext()) {
            cursor1.fwd();

            // Getting position
            long[] posIn = new long[img1.numDimensions()];
            cursor1.localize(posIn);

            // Assigning position
            int xIdxIn1 = img1.dimensionIndex(Axes.X);
            if (xIdxIn1 == -1) {
                if (xOutIdx != -1) randomAccess1.setPosition(0,xOutIdx);
            } else {
                randomAccess1.setPosition(posIn[xIdxIn1],xOutIdx);
            }

            int yIdxIn1 = img1.dimensionIndex(Axes.Y);
            if (yIdxIn1 == -1) {
                if (yOutIdx != -1) randomAccess1.setPosition(0,yOutIdx);
            } else {
                randomAccess1.setPosition(posIn[yIdxIn1],yOutIdx);
            }

            int cIdxIn1 = img1.dimensionIndex(Axes.CHANNEL);
            if (cIdxIn1 == -1) {
                if (cOutIdx != -1) randomAccess1.setPosition(0,cOutIdx);
            } else {
                randomAccess1.setPosition(posIn[cIdxIn1],cOutIdx);
            }

            int zIdxIn1 = img1.dimensionIndex(Axes.Z);
            if (zIdxIn1 == -1) {
                if (zOutIdx != -1) randomAccess1.setPosition(0,zOutIdx);
            } else {
                randomAccess1.setPosition(posIn[zIdxIn1],zOutIdx);
            }

            int tIdxIn1 = img1.dimensionIndex(Axes.TIME);
            if (tIdxIn1 == -1) {
                if (tOutIdx != -1) randomAccess1.setPosition(0,tOutIdx);
            } else {
                randomAccess1.setPosition(posIn[tIdxIn1],tOutIdx);
            }

            randomAccess1.get().set(cursor1.get());
        }

        // Adding the second image to the output
        // Adding the first image to the output
        Cursor<T> cursor2 = img2.localizingCursor();
        RandomAccess<T> randomAccess2 = Views.offsetInterval(imgOut,offsetOut2,dimsOut2).randomAccess();
        while (cursor2.hasNext()) {
            cursor2.fwd();

            // Getting position
            long[] posIn = new long[img2.numDimensions()];
            cursor2.localize(posIn);

            // Assigning position
            int xIdxIn2 = img2.dimensionIndex(Axes.X);
            if (xIdxIn2 == -1) {
                if (xOutIdx != -1) randomAccess2.setPosition(0,xOutIdx);
            } else {
                randomAccess2.setPosition(posIn[xIdxIn2],xOutIdx);
            }

            int yIdxIn2 = img2.dimensionIndex(Axes.Y);
            if (yIdxIn2 == -1) {
                if (yOutIdx != -1) randomAccess2.setPosition(0,yOutIdx);
            } else {
                randomAccess2.setPosition(posIn[yIdxIn2],yOutIdx);
            }

            int cIdxIn2 = img2.dimensionIndex(Axes.CHANNEL);
            if (cIdxIn2 == -1) {
                if (cOutIdx != -1) randomAccess2.setPosition(0,cOutIdx);
            } else {
                randomAccess2.setPosition(posIn[cIdxIn2],cOutIdx);
            }

            int zIdxIn2 = img2.dimensionIndex(Axes.Z);
            if (zIdxIn2 == -1) {
                if (zOutIdx != -1) randomAccess2.setPosition(0,zOutIdx);
            } else {
                randomAccess2.setPosition(posIn[zIdxIn2],zOutIdx);
            }

            int tIdxIn2 = img2.dimensionIndex(Axes.TIME);
            if (tIdxIn2 == -1) {
                if (tOutIdx != -1) randomAccess2.setPosition(0,tOutIdx);
            } else {
                randomAccess2.setPosition(posIn[tIdxIn2],tOutIdx);
            }

            randomAccess2.get().set(cursor2.get());
        }

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
    protected boolean run(Workspace workspace) {
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
