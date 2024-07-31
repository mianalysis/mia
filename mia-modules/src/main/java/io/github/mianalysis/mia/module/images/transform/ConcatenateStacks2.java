package io.github.mianalysis.mia.module.images.transform;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.NotNull;

import ij.ImagePlus;
import ij.plugin.HyperStackConverter;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetLookupTable;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.image.ImgPlusTools;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.RemovableInputImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Combine two or more image stacks into a single stack. This module allows
 * images to be combined along any of the axes X,Y,C,Z or T.<br>
 * <br>
 * Note: Image stack dimensions and bit-depths must be compatible.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ConcatenateStacks2<T extends RealType<T> & NativeType<T>> extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input";

    /**
     * Add another image for concatenation.
     */
    public static final String ADD_INPUT_IMAGE = "Add image";
    public static final String INPUT_IMAGE = "Input image";

    /**
     * If enabled, the moduule can ignore any images specified for inclusion that
     * aren't present in the workspace. This is useful if an image's existence is
     * dependent on optional modules.
     */
    public static final String ALLOW_MISSING_IMAGES = "Allow missing images";

    public static final String REMOVE_INPUT_IMAGES = "Remove input images";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Image output";

    /**
     * The resultant image of concatenation to be added to the workspace.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
     * Axis along which to concatenate input images.
     */
    public static final String AXIS_MODE = "Axis mode";

    public ConcatenateStacks2(Modules modules) {
        super("Concatenate stacks 2", modules);
        deprecated = true; // Marked as deprecated to hide until ready to replace main ConcatenateStacks
                           // code
    }

    public interface AxisModes {
        String X = "X";
        String Y = "Y";
        String Z = "Z";
        String CHANNEL = "Channel";
        String TIME = "Time";

        String[] ALL = new String[] { X, Y, Z, CHANNEL, TIME };

    }

    static <T extends RealType<T> & NativeType<T>> ArrayList<Image<T>> getAvailableImages(Workspace workspace,
            LinkedHashMap<Integer, Parameters> collections) {
        ArrayList<Image<T>> available = new ArrayList<>();

        for (Parameters collection : collections.values()) {
            Image<T> image = workspace.getImage(collection.getValue(INPUT_IMAGE, workspace));
            if (image != null)
                available.add(image);
        }

        return available;

    }

    public static int getAxisIndex(String axis) {
        switch (axis) {
            default:
            case AxisModes.X:
                return 0;

            case AxisModes.Y:
                return 1;

            case AxisModes.CHANNEL:
                return 2;

            case AxisModes.Z:
                return 3;

            case AxisModes.TIME:
                return 4;

        }
    }

    public static <T extends RealType<T> & NativeType<T>> Image<T> process(ArrayList<Image<T>> inputImages,
            String axis, String outputImageName) {
        long[] dimsOut = getOutputImageDimensions(inputImages, axis);
        long[] offsetOut = new long[5];
        int axisIdx = getAxisIndex(axis);

        ImgPlus<T> inputImgRef = inputImages.get(0).getImgPlus();

        // Creating the new Img
        DiskCachedCellImgFactory<T> factory = new DiskCachedCellImgFactory<>((T) inputImgRef.firstElement());
        DiskCachedCellImg dcImage = factory.create(dimsOut);
        ImgPlus<T> imgOut = new ImgPlus<T>(dcImage);
        imgOut.setAxis(new DefaultLinearAxis(Axes.X, 1), 0);
        imgOut.setAxis(new DefaultLinearAxis(Axes.Y, 1), 1);
        imgOut.setAxis(new DefaultLinearAxis(Axes.CHANNEL, 1), 2);
        imgOut.setAxis(new DefaultLinearAxis(Axes.Z, 1), 3);
        imgOut.setAxis(new DefaultLinearAxis(Axes.TIME, 1), 4);

        for (int i = 0; i < inputImages.size(); i++) {
            ImgPlus<T> inputImg = inputImages.get(i).getImgPlus();
            long[] dimsIn = ImgPlusTools.getDimensionsXYCZT(inputImg);

            // Copying pixels from input to output
            IntervalView<T> target = Views.offsetInterval(imgOut, offsetOut, dimsIn);
            RandomAccessibleInterval<T> source = ImgPlusTools.forceImgPlusToXYCZT(inputImg);
            LoopBuilder.setImages(source, target).forEachPixel((s, t) -> t.set(s));

            // Updating the output offset
            offsetOut[axisIdx] = offsetOut[axisIdx] + dimsIn[axisIdx];

            writeProgressStatus(i + 1, inputImages.size(), "stacks", "Concatenate stacks");

        }

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves
        // strangely, but this can be remedied by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(imgOut, outputImageName).duplicate();
        outputImagePlus.setCalibration(inputImages.get(0).getImagePlus().getCalibration());
        ImgPlusTools.applyDimensions(imgOut, outputImagePlus);

        dcImage.shutdown();
        
        return ImageFactory.createImage(outputImageName, outputImagePlus);

    }

    public static <T extends RealType<T> & NativeType<T>> long[] getOutputImageDimensions(
            ArrayList<Image<T>> inputImages,
            String axis) {
        long[] dimsOut = new long[5];

        ImgPlus<T> inputImgRef = inputImages.get(0).getImgPlus();
        long[] dimsRef = ImgPlusTools.getDimensionsXYCZT(inputImgRef);

        int axisIdx = getAxisIndex(axis);

        for (int i = 0; i < inputImages.size(); i++) {
            ImgPlus<T> inputImg = inputImages.get(i).getImgPlus();
            long[] dimsIn = ImgPlusTools.getDimensionsXYCZT(inputImg);

            // Checking bit depths
            if (inputImgRef.firstElement().getBitsPerPixel() != inputImg.firstElement().getBitsPerPixel()) {
                MIA.log.writeWarning("Concatenate stacks: Image bit depths not the same");
                return null;
            }

            // Adding current dimension length
            for (int dim = 0; dim < 5; dim++) {
                if (dim == axisIdx) {
                    dimsOut[dim] = dimsOut[dim] + dimsIn[dim];
                } else {
                    if (dimsRef[dim] != dimsIn[dim]) {
                        MIA.log.writeWarning("Concatenate stacks: Axes not equal along " + axis + " axis");
                        return null;
                    }
                    dimsOut[dim] = dimsRef[dim];
                }
            }
        }

        return dimsOut;

    }

    // static <T extends RealType<T> & NativeType<T>> LUT[] getLUTs(Image<T>[]
    // images) {
    // int count = 0;
    // for (int i = 0; i < images.length; i++) {
    // count = count + images[i].getImagePlus().getNChannels();
    // }

    // LUT[] luts = new LUT[count];
    // count = 0;
    // for (int i = 0; i < images.length; i++) {
    // ImagePlus currIpl = images[i].getImagePlus();
    // for (int c = 0; c < currIpl.getNChannels(); c++) {
    // currIpl.setPosition(c + 1, 1, 1);
    // luts[count++] = currIpl.getProcessor().getLut();
    // }
    // }

    // return luts;

    // }

    public static <T extends RealType<T> & NativeType<T>> void convertToColour(Image<T> image,
            ArrayList<Image<T>> inputImages) {
        ImagePlus ipl = image.getImagePlus();

        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();
        if (nChannels > 1)
            ipl = HyperStackConverter.toHyperStack(ipl, nChannels, nSlices, nFrames, "xyczt", "color");

        image.setImagePlus(ipl);

        // Set LUTs
        int count = 1;
        for (int i = 0; i < inputImages.size(); i++) {
            ImagePlus currIpl = inputImages.get(i).getImagePlus();
            for (int c = 0; c < currIpl.getNChannels(); c++) {
                currIpl.setPosition(c + 1, 1, 1);
                LUT lut = currIpl.getProcessor().getLut();
                SetLookupTable.setLUT(image, lut, SetLookupTable.ChannelModes.SPECIFIC_CHANNELS, count++);
            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "This is a candidate replacement for the original ConcatenateStacks.  For now, it's marked as \"deprecated\" to keep it hidden.<br>"
                +

                "Combine two or more image stacks into a single stack.  This module allows images to be combined along any of the axes X,Y,C,Z or T.<br>"
                +
                "<br>Note: Image stack dimensions and bit-depths must be compatible.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean allowMissingImages = parameters.getValue(ALLOW_MISSING_IMAGES, workspace);
        boolean removeInputImages = parameters.getValue(REMOVE_INPUT_IMAGES, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String axisMode = parameters.getValue(AXIS_MODE, workspace);
        int axisIdx = getAxisIndex(axisMode);

        // Creating a collection of images
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_INPUT_IMAGE, workspace);
        ArrayList<Image<T>> inputImages = getAvailableImages(workspace, collections);

        if (!allowMissingImages && collections.size() != inputImages.size()) {
            MIA.log.writeError("Input images missing.");
            return Status.FAIL;
        }

        // If only one image was specified, simply create a duplicate of the input,
        // otherwise do concatenation.
        Image outputImage;
        if (inputImages.size() == 1)
            outputImage = ImageFactory.createImage(outputImageName, inputImages.get(0).getImagePlus());
        else {
            if (removeInputImages) {
                ArrayList<RandomAccessibleInterval<T>> inputImgs = new ArrayList<>();
                for (Image<T> inputImage : inputImages)
                    inputImgs.add(ImgPlusTools.forceImgPlusToXYCZT(inputImage.getImgPlus()));

                ConvertService convertService = MIA.getIJService().getContext().getService(ConvertService.class);
                RandomAccessibleInterval<T> outputRAI = Views.concatenate(axisIdx, inputImgs);
                ImgPlus<T> outputImgPlus = convertService.convert(outputRAI, ImgPlus.class);
                ImgPlusTools.initialiseXYCZTAxes(outputImgPlus);
                ImgPlusTools.applyAxes(inputImages.get(0).getImgPlus(), outputImgPlus);
                outputImage = ImageFactory.createImage(outputImageName, outputImgPlus, ImageType.IMGLIB2);

            } else {
                outputImage = process(inputImages, axisMode, outputImageName);
            }
        }

        if (outputImage == null)
            return Status.FAIL;
        if (axisMode.equals(AxisModes.CHANNEL))
            convertToColour(outputImage, inputImages);

        if (showOutput)
            outputImage.show();

        workspace.addImage(outputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new CustomInputImageP(INPUT_IMAGE, this, "", "Image for concatenation."));
        parameters
                .add(new ParameterGroup(ADD_INPUT_IMAGE, this, collection, 2, "Add another image for concatenation."));
        parameters.add(new BooleanP(ALLOW_MISSING_IMAGES, this, false,
                "If enabled, the moduule can ignore any images specified for inclusion that aren't present in the workspace.  This is useful if an image's existence is dependent on optional modules."));
        parameters.add(new BooleanP(REMOVE_INPUT_IMAGES, this, false));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "",
                "The resultant image of concatenation to be added to the workspace."));
        parameters.add(new ChoiceP(AXIS_MODE, this, AxisModes.X, AxisModes.ALL,
                "Axis along which to concatenate input images."));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        boolean allowMissingImages = parameters.getValue(ALLOW_MISSING_IMAGES, workspace);
        boolean removeInputImages = parameters.getValue(REMOVE_INPUT_IMAGES, workspace);

        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_INPUT_IMAGE, workspace);
        for (Parameters collection : collections.values()) {
            CustomInputImageP parameter = collection.getParameter(INPUT_IMAGE);
            parameter.setAllowMissingImages(allowMissingImages);
            parameter.setRemoveInputImages(removeInputImages);
        }

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    // Creating a custom class for this module, which always returns true. This way
    // channels can go missing and
    // this will still work.
    class CustomInputImageP extends RemovableInputImageP {
        private boolean allowMissingImages = false;

        private CustomInputImageP(String name, Module module) {
            super(name, module);

        }

        public CustomInputImageP(String name, Module module, @NotNull String imageName) {
            super(name, module, imageName);
        }

        public CustomInputImageP(String name, Module module, @NotNull String imageName, String description) {
            super(name, module, imageName, description);
        }

        @Override
        public boolean verify() {
            if (allowMissingImages)
                return true;
            else
                return super.verify();
        }

        @Override
        public boolean isValid() {
            if (allowMissingImages)
                return true;
            else
                return super.isValid();
        }

        @Override
        public <T extends Parameter> T duplicate(Module newModule) {
            CustomInputImageP newParameter = new CustomInputImageP(name, module, getImageName(), getDescription());

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
