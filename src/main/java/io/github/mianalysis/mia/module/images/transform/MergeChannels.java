package io.github.mianalysis.mia.module.images.transform;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.sjcommon.process.IntensityMinMax;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.IdentityAxis;
import net.imglib2.Cursor;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Created by sc13967 on 22/02/2018.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MergeChannels<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String ADD_INPUT_IMAGE = "Add image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OVERWRITE_MODE = "Overwrite mode";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String IMAGE_INDEX_TO_OVERWRITE = "Image index to overwrite (>= 1)";

    public MergeChannels(Modules modules) {
        super("Merge channels", modules);
        deprecated = true;
    }

    public interface OverwriteModes {
        String CREATE_NEW = "Create new image";
        String OVERWRITE_IMAGE = "Overwrite image";

        String[] ALL = new String[] { CREATE_NEW, OVERWRITE_IMAGE };

    }

    public Image combineImages(Image[] inputImages, String outputImageName) {
        // Processing first two images
        Image outputImage = combineImages(inputImages[0], inputImages[1], outputImageName);

        // Appending any additional images
        for (int i = 2; i < inputImages.length; i++) {
            outputImage = combineImages(outputImage, inputImages[i], outputImageName);
        }

        return outputImage;

    }

    public Image combineImages(Image inputImage1, Image inputImage2, String outputImageName) {
        ImgPlus<T> img1 = inputImage1.getImgPlus();
        ImgPlus<T> img2 = inputImage2.getImgPlus();

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

        long[] dimsIn1 = new long[5];
        long[] dimsIn2 = new long[5];
        long[] dimsOut = new long[5];
        long[] offsetOut1 = new long[5];
        long[] offsetOut2 = new long[5];

        dimsIn1[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsIn1[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsIn1[2] = cDim1 == -1 ? 1 : img1.dimension(cDim1);
        dimsIn1[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsIn1[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        dimsIn2[0] = xDim2 == -1 ? 1 : img2.dimension(xDim2);
        dimsIn2[1] = yDim2 == -1 ? 1 : img2.dimension(yDim2);
        dimsIn2[2] = cDim2 == -1 ? 1 : img2.dimension(cDim2);
        dimsIn2[3] = zDim2 == -1 ? 1 : img2.dimension(zDim2);
        dimsIn2[4] = tDim2 == -1 ? 1 : img2.dimension(tDim2);

        dimsOut[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsOut[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsOut[2] = (cDim1 == -1 ? 1 : img1.dimension(cDim1)) + (cDim2 == -1 ? 1 : img2.dimension(cDim2));
        dimsOut[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsOut[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        Arrays.fill(offsetOut1, 0);
        Arrays.fill(offsetOut2, 0);
        offsetOut2[2] = dimsIn1[2];

        // Creating the composite image
        T type = img1.firstElement();
        CellImgFactory<T> factory = new CellImgFactory<T>(type);
        ImgPlus<T> mergedImg = new ImgPlus<>(factory.create(dimsOut));

        // Assigning the relevant dimensions
        CalibratedAxis xAxis = xDim1 == -1 ? new IdentityAxis(Axes.X) : img1.axis(xDim1);
        mergedImg.setAxis(xAxis, 0);
        CalibratedAxis yAxis = yDim1 == -1 ? new IdentityAxis(Axes.Y) : img1.axis(yDim1);
        mergedImg.setAxis(yAxis, 1);
        CalibratedAxis cAxis = cDim1 == -1 ? new IdentityAxis(Axes.CHANNEL) : img1.axis(cDim1);
        mergedImg.setAxis(cAxis, 2);
        CalibratedAxis zAxis = zDim1 == -1 ? new IdentityAxis(Axes.Z) : img1.axis(zDim1);
        mergedImg.setAxis(zAxis, 3);
        CalibratedAxis tAxis = tDim1 == -1 ? new IdentityAxis(Axes.TIME) : img1.axis(tDim1);
        mergedImg.setAxis(tAxis, 4);

        Cursor<T> cursorIn = img1.cursor();
        Cursor<T> cursorOut = Views.offsetInterval(mergedImg, offsetOut1, dimsIn1).cursor();
        while (cursorIn.hasNext())
            cursorOut.next().set(cursorIn.next());

        cursorIn = img2.cursor();
        cursorOut = Views.offsetInterval(mergedImg, offsetOut2, dimsIn2).cursor();
        while (cursorIn.hasNext())
            cursorOut.next().set(cursorIn.next());

        ImagePlus ipl = ImageJFunctions.wrap(mergedImg, outputImageName);
        ipl = new Duplicator().run(HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(),
                ipl.getNFrames(), "xyczt", "Composite"));

        // Updating the display range to help show all the colours
        IntensityMinMax.run(ipl, true, 0.001, 0.001, IntensityMinMax.PROCESS_FAST);

        // Spatial calibration has to be reapplied, as it's lost in the translation
        // between ImagePlus and ImgPlus
        ipl.setCalibration(inputImage1.getImagePlus().getCalibration());

        ipl.setPosition(1, 1, 1);
        ipl.updateChannelAndDraw();

        return new Image(outputImageName, ipl);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: This Module has been superseeded by the more generalised \"Concatenate stacks\" Module.  It will "
                + "be removed in a future release.<br><br>"
                + "Combines image stacks as different channels.  Output is automatically converted to a composite image.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String overwriteMode = parameters.getValue(OVERWRITE_MODE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Creating a collection of images
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_INPUT_IMAGE,workspace);
        Image[] inputImages = new Image[collections.size()];
        int i = 0;
        for (Parameters collection : collections.values()) {
            inputImages[i++] = workspace.getImage(collection.getValue(INPUT_IMAGE,workspace));
        }

        Image mergedImage = combineImages(inputImages, outputImageName);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                Image outputImage = new Image(outputImageName, mergedImage.getImagePlus());
                workspace.addImage(outputImage);
                break;

            case OverwriteModes.OVERWRITE_IMAGE:
                inputImages[i - 1].setImagePlus(mergedImage.getImagePlus());
                break;

        }

        if (showOutput)
            mergedImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        Parameters collection = new Parameters();
        collection.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ParameterGroup(ADD_INPUT_IMAGE, this, collection, 2));

        parameters.add(new ChoiceP(OVERWRITE_MODE, this, OverwriteModes.CREATE_NEW, OverwriteModes.ALL));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new IntegerP(IMAGE_INDEX_TO_OVERWRITE, this, 1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(ADD_INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(OVERWRITE_MODE));
        switch ((String) parameters.getValue(OVERWRITE_MODE,workspace)) {
            case OverwriteModes.CREATE_NEW:
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                break;
            case OverwriteModes.OVERWRITE_IMAGE:
                returnedParameters.add(parameters.getParameter(IMAGE_INDEX_TO_OVERWRITE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        Parameters collection = ((ParameterGroup) parameters.get(ADD_INPUT_IMAGE)).getTemplateParameters();

        collection.get(INPUT_IMAGE).setDescription("Image from workspace to add to output merged image.");

        parameters.get(ADD_INPUT_IMAGE).setDescription(
                "Add another image to be included in output merged image.  All added images must have the same X,Y,Z and T dimensions.");

        parameters.get(OVERWRITE_MODE).setDescription("Controls where the output image is stored:<br><ul>"

                + "<li>\"" + OverwriteModes.CREATE_NEW
                + "\" Stores the merged image as a new image in the workspace with the name specified by \""
                + OUTPUT_IMAGE + "\".</li>"

                + "<li>\"" + OverwriteModes.OVERWRITE_IMAGE + "\" Overwrite the image specified by the index \""
                + IMAGE_INDEX_TO_OVERWRITE + "\" in the workspace with the merged image.");

        parameters.get(OUTPUT_IMAGE)
                .setDescription("Name for the output merged image to be stored in the workspace with.");

        parameters.get(IMAGE_INDEX_TO_OVERWRITE).setDescription(
                "If overwriting one of the input images, the image specified by this index (numbering starting at 1) will be overwritten.");

    }
}
