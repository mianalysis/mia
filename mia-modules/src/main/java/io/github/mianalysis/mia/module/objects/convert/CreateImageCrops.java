// TODO: Normalised distance from centre to edge.  Will need to calculate line between the two and assign points on that line

package io.github.mianalysis.mia.module.objects.convert;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.CanvasResizer;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.CropImage;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateImageCrops extends Module {

    public static final String INPUT_SEPARATOR = "Image/object input";

    public static final String INPUT_OBJECTS = "Input objects";

    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_SEPARATOR = "Image output";

    public static final String OUTPUT_IMAGE = "Output image";

    public static final String OUTPUT_WIDTH_PX = "Output width (px)";

    public static final String OUTPUT_HEIGHT_PX = "Output height (px)";

    public static final String OUTPUT_NUM_SLICES_PX = "Output number of slices";

    public CreateImageCrops(Modules modules) {
        super("Create image crops", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_CONVERT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static Image process(Objs inputObjects, Image inputImage, String outputImageName, int outputWidth,
            int outputHeight, int outputNumSlices) {
        ImagePlus inputIpl = inputImage.getImagePlus().duplicate();
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName, outputWidth, outputHeight, inputIpl.getNChannels(),
                outputNumSlices, inputObjects.size(), inputIpl.getBitDepth());

        // Exoanding input image in XY to ensure cropping works
        ImageStack inputIst = inputImage.getImagePlus().getStack();
        ImageStack expandedIst = new CanvasResizer().expandStack(inputIst, inputIst.getWidth() + outputWidth * 2,
                inputIst.getHeight() + outputHeight * 2, outputWidth, outputHeight);
        ImagePlus expandedIpl = IJ.createHyperStack("Expanded", expandedIst.getWidth(), expandedIst.getHeight(),
                inputIpl.getNChannels(), inputIpl.getNSlices(), inputIpl.getNFrames(), inputIpl.getBitDepth());
        expandedIpl.setStack(expandedIst);
        Image expandedImage = ImageFactory.createImage("Expanded",expandedIpl);

        // Iterating over each object, extracting the crop, then inserting it into the
        // output image
        int idx = 1;
        for (Obj inputObject : inputObjects.values()) {
            int xCent = (int) Math.round(inputObject.getXMean(true))+outputWidth;
            int yCent = (int) Math.round(inputObject.getYMean(true))+outputHeight;
            int zCent = (int) Math.round(inputObject.getZMean(true, false));

            // Cropping in the XY plane
            int left = xCent - (int) Math.round(outputWidth / 2);
            int top = yCent - (int) Math.round(outputHeight / 2);
            Image currCrop = CropImage.cropImage(expandedImage, outputImageName, left, top, outputWidth, outputHeight);

            // Cropping along the Z axis
            int z1 = zCent - (int) Math.round(outputNumSlices / 2);
            int z2 = z1 + outputNumSlices - 1;
            int appliedZ1 = Math.max(0, z1);
            int appliedZ2 = Math.min(inputIpl.getNSlices() - 1, z2);
            int offsetZ = appliedZ1 - z1;
            int t = inputObject.getT();
            Image currCropZ = ExtractSubstack.extractSubstack(currCrop, outputImageName, "1-end",
                    String.valueOf(appliedZ1 + 1) + "-" + String.valueOf(appliedZ2 + 1), String.valueOf(t + 1));
            
            ImagePlus currCropZIpl = currCropZ.getImagePlus();
            for (int z = 0; z < currCropZIpl.getNSlices(); z++) {
                for (int c = 0; c < currCropZIpl.getNChannels(); c++) {
                    outputIpl.setPosition(c + 1, z + offsetZ + 1, idx);
                    currCropZIpl.setPosition(c + 1, z + 1, 1);
                    outputIpl.setProcessor(currCropZIpl.getProcessor());
                }
            }

            idx++;
            
        }

        return ImageFactory.createImage(outputImageName, outputIpl);

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        int outputWidth = parameters.getValue(OUTPUT_WIDTH_PX, workspace);
        int outputHeight = parameters.getValue(OUTPUT_HEIGHT_PX, workspace);
        int outputNumSlices = parameters.getValue(OUTPUT_NUM_SLICES_PX, workspace);

        // Getting input image and objects
        Image inputImage = workspace.getImage(inputImageName);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        Image outputImage = process(inputObjects, inputImage, outputImageName, outputWidth, outputHeight,
                outputNumSlices);

        workspace.addImage(outputImage);
        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new IntegerP(OUTPUT_WIDTH_PX, this, 256));
        parameters.add(new IntegerP(OUTPUT_HEIGHT_PX, this, 256));
        parameters.add(new IntegerP(OUTPUT_NUM_SLICES_PX, this, 1));

    }

    @Override
    public Parameters updateAndGetParameters() {
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
}
