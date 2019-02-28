package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.Axis;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Process.ImgPlusTools;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;
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

    public ImgPlus<T> concatenateImages(ImgPlus<T> img1, ImgPlus<T> img2, String axis) {
        // Getting dimensions for the input images
        long[] offset1 = new long[]{0,0,0,0,0};
        long[] dims1 = ImgPlusTools.getDimensionsXYCZT(img1);

        long[] offset2 = new long[]{0,0,0,0,0};
        long[] dims2 = ImgPlusTools.getDimensionsXYCZT(img2);

        long[] dimsOut = new long[5];
        if (axis.equals(AxisModes.X)) {
            dimsOut[0] = dims1[0] + dims2[0];
            offset2[0] = dims1[0];
        } else if (dims1[0] != dims2[0]) {
            System.err.println("[Concatenate stacks] X-axis lengths not equal.");
            return null;
        } else {
            dimsOut[0] = dims1[0];
        }

        if (axis.equals(AxisModes.Y)) {
            dimsOut[1] = dims1[1] + dims2[1];
            offset2[1] = dims1[1];
        } else if (dims1[1] != dims2[1]) {
            System.err.println("[Concatenate stacks] Y-axis lengths not equal.");
            return null;
        } else {
            dimsOut[1] = dims1[1];
        }

        if (axis.equals(AxisModes.CHANNEL)) {
            dimsOut[2] = dims1[2] + dims2[2];
            offset2[2] = dims1[2];
        } else if (dims1[2] != dims2[2]) {
            System.err.println("[Concatenate stacks] channel-axis lengths not equal.");
            return null;
        } else {
            dimsOut[2] = dims1[2];
        }

        if (axis.equals(AxisModes.Z)) {
            dimsOut[3] = dims1[3] + dims2[3];
            offset2[3] = dims1[3];
        } else if (dims1[3] != dims2[3]) {
            System.err.println("[Concatenate stacks] Z-axis lengths not equal.");
            return null;
        } else {
            dimsOut[3] = dims1[3];
        }

        if (axis.equals(AxisModes.TIME)) {
            dimsOut[4] = dims1[4] + dims2[4];
            offset2[4] = dims1[4];
        } else if (dims1[4] != dims2[4]) {
            System.err.println("[Concatenate stacks] Time-axis lengths not equal.");
            return null;
        } else {
            dimsOut[4] = dims1[4];
        }

        // Creating the new Img
        ArrayImgFactory<T> factory = new ArrayImgFactory<T>((T) img1.firstElement());
        ImgPlus<T> imgOut = new ImgPlus<T>(factory.create(dimsOut));

        // Adding the first image to the output
        Cursor<T> cursor1 = img1.cursor();
        Cursor<T> cursorOut = Views.offsetInterval(imgOut, offset1, dims1).cursor();
        while (cursor1.hasNext()) cursorOut.next().set(cursor1.next());

        // Adding the first image to the output
        Cursor<T> cursor2 = img2.cursor();
        cursorOut = Views.offsetInterval(imgOut,offset2,dims2).cursor();
        while (cursor2.hasNext()) cursorOut.next().set(cursor2.next());

        // Applying calibration from img1 to imgOut
        ImgPlusTools.applyCalibrationXYCZT(img1,imgOut);

        // Ensuring the extended axis has correct dimensions
        switch (axis) {
            case AxisModes.X:
                if (imgOut.dimensionIndex(Axes.X) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.X,1),0);
            break;
            case AxisModes.Y:
                if (imgOut.dimensionIndex(Axes.Y) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.Y,1),1);
                break;
            case AxisModes.CHANNEL:
                if (imgOut.dimensionIndex(Axes.CHANNEL) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.CHANNEL,1),2);
                break;
            case AxisModes.Z:
                if (imgOut.dimensionIndex(Axes.Z) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.Z,1),3);
                break;
            case AxisModes.TIME:
                if (imgOut.dimensionIndex(Axes.TIME) == -1) imgOut.setAxis(new DefaultLinearAxis(Axes.TIME,1),4);
                break;
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
        ImagePlus outputImagePlus = new Duplicator().run(ImageJFunctions.wrap(imgOut,outputImageName));
        outputImagePlus.setCalibration(inputImages[0].getImagePlus().getCalibration());

        return new Image<T>(outputImageName,imgOut);

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
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String axisMode = parameters.getValue(AXIS_MODE);

        // Creating a collection of images
        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_INPUT_IMAGE);
        Image<T>[] inputImages = new Image[collections.size()];
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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
