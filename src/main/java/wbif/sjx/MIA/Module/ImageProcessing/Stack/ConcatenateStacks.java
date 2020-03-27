package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.annotation.Nonnull;

import ij.ImagePlus;
import ij.plugin.HyperStackConverter;
import ij.process.LUT;
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
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Visualisation.ImageRendering.SetLookupTable;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.common.Process.ImgPlusTools;

public class ConcatenateStacks <T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String ADD_INPUT_IMAGE = "Add image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String ALLOW_MISSING_IMAGES = "Allow missing images";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CONCAT_SEPARATOR = "Stack concatenation";
    public static final String AXIS_MODE = "Axis mode";


    public ConcatenateStacks(ModuleCollection modules) {
        super("Concatenate stacks",modules);
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

    static <T extends RealType<T> & NativeType<T>> ArrayList<Image<T>> getAvailableImages(Workspace workspace, LinkedHashSet<ParameterCollection> collections) {
        ArrayList<Image<T>> available = new ArrayList<>();

        for (ParameterCollection collection:collections) {
            Image image = workspace.getImage(collection.getValue(INPUT_IMAGE));
            if (image != null) available.add(image);
        }

        return available;

    }

    static <T extends RealType<T> & NativeType<T>> long getCombinedAxisLength(ImgPlus<T> img1, ImgPlus<T> img2, AxisType axis) {
        long lengthIn1 = getAxisLength(img1,axis);
        long lengthIn2 = getAxisLength(img2,axis);

        return lengthIn1 + lengthIn2;

    }

    static <T extends RealType<T> & NativeType<T>> boolean checkAxisEquality(ImgPlus<T> img1, ImgPlus<T> img2, AxisType axis) {
        long lengthIn1 = getAxisLength(img1,axis);
        long lengthIn2 = getAxisLength(img2,axis);

        return lengthIn1 == lengthIn2;

    }

    static <T extends RealType<T> & NativeType<T>> long getAxisLength(ImgPlus<T> img, AxisType axis) {
        int idxIn = img.dimensionIndex(axis);
        return idxIn == -1 ? 1 : img.dimension(idxIn);

    }

    static <T extends RealType<T> & NativeType<T>> void copyPixels(ImgPlus<T> sourceImg, ImgPlus targetImg, long[] offset, long[] dims) {
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

    public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> concatenateImages(ImgPlus<T> img1, ImgPlus<T> img2, String axis) {
        long[] dimsOutCombined = new long[5];
        long[] offsetOut1 = new long[5];
        long[] offsetOut2 = new long[5];
        long[] dimsOut1 = ImgPlusTools.getDimensionsXYCZT(img1);
        long[] dimsOut2 = ImgPlusTools.getDimensionsXYCZT(img2);

        if (axis.equals(AxisModes.X)) {
            dimsOutCombined[0] = getCombinedAxisLength(img1, img2, Axes.X);
            offsetOut2[0] = getAxisLength(img1, Axes.X);
        } else {
            if (!checkAxisEquality(img1,img2,Axes.X)) {
                MIA.log.writeWarning("Axes not equal along X axis");
                return null;
            }
            dimsOutCombined[0] = getAxisLength(img1, Axes.X);
        }

        if (axis.equals(AxisModes.Y)) {
            dimsOutCombined[1] = getCombinedAxisLength(img1, img2, Axes.Y);
            offsetOut2[1] = getAxisLength(img1, Axes.Y);
        } else {
            if (!checkAxisEquality(img1,img2,Axes.Y)) {
                MIA.log.writeWarning("Axes not equal along Y axis");
                return null;
            }
            dimsOutCombined[1] = getAxisLength(img1, Axes.Y);
        }

        if (axis.equals(AxisModes.CHANNEL)) {
            dimsOutCombined[2] = getCombinedAxisLength(img1, img2, Axes.CHANNEL);
            offsetOut2[2] = getAxisLength(img1, Axes.CHANNEL);
        } else {
            if (!checkAxisEquality(img1,img2,Axes.CHANNEL)) {
                MIA.log.writeWarning("Axes not equal along channel axis");
                return null;
            }
            dimsOutCombined[2] = getAxisLength(img1, Axes.CHANNEL);
        }

        if (axis.equals(AxisModes.Z)) {
            dimsOutCombined[3] = getCombinedAxisLength(img1, img2, Axes.Z);
            offsetOut2[3] = getAxisLength(img1, Axes.Z);
        } else {
            if (!checkAxisEquality(img1,img2,Axes.Z)) {
                MIA.log.writeWarning("Axes not equal along Z axis");
                return null;
            }
            dimsOutCombined[3] = getAxisLength(img1, Axes.Z);
        }

        if (axis.equals(AxisModes.TIME)) {
            dimsOutCombined[4] = getCombinedAxisLength(img1, img2, Axes.TIME);
            offsetOut2[4] = getAxisLength(img1, Axes.TIME);
        } else {
            if (!checkAxisEquality(img1,img2,Axes.TIME)) {
                MIA.log.writeWarning("Axes not equal along time axis");
                return null;
            }
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

    public static <T extends RealType<T> & NativeType<T>> Image<T> concatenateImages(ArrayList<Image<T>> inputImages, String axis, String outputImageName) {
        // Processing first two images
        ImgPlus<T> imgOut = concatenateImages(inputImages.get(0).getImgPlus(),inputImages.get(1).getImgPlus(),axis);

        // Appending any additional images
        for (int i=2;i<inputImages.size();i++) {
            imgOut = concatenateImages(imgOut,inputImages.get(i).getImgPlus(),axis);
        }

        // If concatenation failed (for example, if the dimensions were inconsistent) it returns null
        if (imgOut == null) return null;

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(imgOut,outputImageName).duplicate();
        outputImagePlus.setCalibration(inputImages.get(0).getImagePlus().getCalibration());
        ImgPlusTools.applyAxes(imgOut,outputImagePlus);

        return new Image<T>(outputImageName,outputImagePlus);

    }

    static LUT[] getLUTs(Image[] images){
        int count = 0;
        for (int i=0;i<images.length;i++) {
            count = count + images[i].getImagePlus().getNChannels();
        }

        LUT[] luts = new LUT[count];
        count = 0;
        for (int i=0;i<images.length;i++) {
            ImagePlus currIpl = images[i].getImagePlus();
            for (int c=0;c<currIpl.getNChannels();c++) {
                currIpl.setPosition(c+1,1,1);
                luts[count++] = currIpl.getProcessor().getLut();
            }
        }

        return luts;

    }

    public static <T extends RealType<T> & NativeType<T>> void convertToColour(Image image, ArrayList<Image<T>> inputImages) {
        ImagePlus ipl = image.getImagePlus();

        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();
        if (nChannels > 1) ipl = HyperStackConverter.toHyperStack(ipl, nChannels, nSlices, nFrames, "xyczt", "color");

        image.setImagePlus(ipl);

        // Set LUTs
        int count = 1;
        for (int i=0;i<inputImages.size();i++) {
            ImagePlus currIpl = inputImages.get(i).getImagePlus();
            for (int c=0;c<currIpl.getNChannels();c++) {
                currIpl.setPosition(c+1,1,1);
                LUT lut = currIpl.getProcessor().getLut();
                SetLookupTable.setLUT(image,lut,SetLookupTable.ChannelModes.SPECIFIC_CHANNELS,count++);
            }
        }
    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Combine two or more image stacks into a single stack.  This module allows images to be combined along any of the axes X,Y,C,Z or T.<br>" +
                "<br>Note: Image stack dimensions and bit-depths must be compatible.";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        boolean allowMissingImages = parameters.getValue(ALLOW_MISSING_IMAGES);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String axisMode = parameters.getValue(AXIS_MODE);

        // Creating a collection of images
        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_INPUT_IMAGE);
        ArrayList<Image<T>> inputImages = getAvailableImages(workspace,collections);

        if (!allowMissingImages && collections.size() != inputImages.size()) {
            MIA.log.writeError("Input images missing.");
            return false;
        }

        // If only one image was specified, simply create a duplicate of the input, otherwise do concatenation.
        Image outputImage;
        if (inputImages.size() == 1) {
            outputImage = new Image(outputImageName,inputImages.get(0).getImagePlus());
        } else {
            outputImage = concatenateImages(inputImages, axisMode, outputImageName);
        }

        if (outputImage == null) return false;
        if (axisMode.equals(AxisModes.CHANNEL)) convertToColour(outputImage, inputImages);
        if (showOutput) outputImage.showImage();
        workspace.addImage(outputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        ParameterCollection collection = new ParameterCollection();
        collection.add(new CustomInputImageP(INPUT_IMAGE,this,"","Image for concatenation."));
        parameters.add(new ParameterGroup(ADD_INPUT_IMAGE,this,collection,2,"Add another image for concatenation."));
        parameters.add(new BooleanP(ALLOW_MISSING_IMAGES,this,false,"If enabled, the moduule can ignore any images specified for inclusion that aren't present in the workspace.  This is useful if an image's existence is dependent on optional modules."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this,"","The resultant image of concatenation to be added to the workspace."));

        parameters.add(new ParamSeparatorP(CONCAT_SEPARATOR,this));
        parameters.add(new ChoiceP(AXIS_MODE,this,AxisModes.X,AxisModes.ALL,"Axis along which to concatenate input images."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        boolean allowMissingImages = parameters.getValue(ALLOW_MISSING_IMAGES);

        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_INPUT_IMAGE);
        for (ParameterCollection collection:collections) {
            CustomInputImageP parameter = collection.getParameter(INPUT_IMAGE);
            parameter.setAllowMissingImages(allowMissingImages);
        }

        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    // Creating a custom class for this module, which always returns true.  This way channels can go missing and
    // this will still work.
    class CustomInputImageP extends InputImageP {
        private boolean allowMissingImages = false;

        private CustomInputImageP(String name, Module module) {
            super(name, module);

        }

        public CustomInputImageP(String name, Module module, @Nonnull String imageName) {
            super(name, module, imageName);
        }

        public CustomInputImageP(String name, Module module, @Nonnull String imageName, String description) {
            super(name, module, imageName, description);
        }

        @Override
        public boolean verify() {
            if (allowMissingImages) return true;
            else return super.verify();
        }

        @Override
        public boolean isValid() {
            if (allowMissingImages) return true;
            else return super.isValid();
        }

        @Override
        public <T extends Parameter> T duplicate(Module newModule) {
            CustomInputImageP newParameter = new CustomInputImageP(name,module,getImageName(),getDescription());

            newParameter.setNickname(getNickname());
            newParameter.setVisible(isVisible());
            newParameter.setExported(isExported());
            newParameter.setAllowMissingImages(allowMissingImages);

            return (T) newParameter;

        }

        public boolean isAllowMissingImages() {
            return allowMissingImages;
        }

        public void setAllowMissingImages(boolean allowMissingImages) {
            this.allowMissingImages = allowMissingImages;
        }
    }
}
