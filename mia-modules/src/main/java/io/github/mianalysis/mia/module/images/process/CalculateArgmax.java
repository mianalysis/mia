package io.github.mianalysis.mia.module.images.process;

import org.checkerframework.checker.units.qual.h;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetDisplayRange;
import io.github.mianalysis.mia.module.images.configure.SetLookupTable;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CalculateArgmax extends Module {

    public static final String INPUT_SEPARATOR = "Image input";

    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_SEPARATOR = "Image output";

    public static final String OUTPUT_IMAGE = "Output image";

    public CalculateArgmax(Modules modules) {
        super("Calculate argmax", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Takes a multi-channel probability image and uses the argmax to determine which class each pixel belongs to";
    }

    public static Image process(Image inputImage, String outputImageName) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        
        int width = inputIpl.getWidth();
        int height = inputIpl.getHeight();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        ImagePlus outputIpl = IJ.createHyperStack(outputImageName, width, height, nChannels, nSlices, nFrames, 8);
        Image outputImage = ImageFactory.createImage(outputImageName, outputIpl);

        // T and Z starting at 1 due to indexing in getStackIndex function
        int count = 0;
        int total = nFrames * nSlices;
        for (int t = 1; t <= nFrames; t++) {
            for (int z = 1; z <= nSlices; z++) {
                int outputIdx = inputIpl.getStackIndex(1, z, t);
                ImageProcessor outputIpr = outputIpl.getStack().getProcessor(outputIdx);
                
                float[][][] channels = new float[nChannels][width][height];
                for (int c = 0; c < inputIpl.getNChannels(); c++) {
                    int inputIdx = inputIpl.getStackIndex(c+1, z, t);
                    channels[c] = inputIpl.getStack().getProcessor(inputIdx).getFloatArray();
                }

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int maxIdx = 0;
                        double maxVal = -Double.MAX_VALUE;
                        for (int c = 0; c < nChannels; c++) {
                            double currVal = channels[c][x][y];
                            if (currVal > maxVal) {
                                maxIdx = c;
                                maxVal = currVal;
                            }
                        }

                        outputIpr.set(x, y, maxIdx);

                    }
                }

                writeProgressStatus(++count, total, "slices", "Calculate argmax");

            }
        }

        return outputImage;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        // Getting input image
        Image inputImage = workspace.getImage(inputImageName);

        // Creating output image
        Image outputImage = process(inputImage, outputImageName);
        SetLookupTable.setLUT(outputImage, LUTs.Random(true), SetLookupTable.ChannelModes.ALL_CHANNELS, 1);
        SetDisplayRange.setDisplayRangeManual(outputImage, new double[]{0, 255});

        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

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
