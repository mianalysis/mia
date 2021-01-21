package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
import sc.fiji.skeletonize3D.Skeletonize3D_;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageMath;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class Skeletonise extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public Skeletonise(ModuleCollection modules) {
        super("Skeletonise", modules);
    }

    public static void process(Image image) {
        // Inverting image intensity
        InvertIntensity.process(image);

        // Iterating over all channels and timepoints
        ImagePlus ipl = image.getImagePlus();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        Skeletonize3D_ skeletonize3d = new Skeletonize3D_();

        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);

                // Running skeletonisation
                skeletonize3d.setup("arg", iplOrig);
                skeletonize3d.run(iplOrig.getProcessor());

            }
        }

        // Multiplying back to the range 0-255
        ImageMath.process(ipl, ImageMath.CalculationTypes.MULTIPLY, 255);

        // Inverting back to original logic
        InvertIntensity.process(image);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Creates an skeletonised representation of a specific binary image in the workspace.  The output 8-bit image will show black minima (intensity 0) on a white background (intensity 255).  Each minima will show the lowest local intensity region within a specific dynamic range.  Local variation greater than this dynamic will result in the creation of more minima.  Uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImagePlus.duplicate());

        process(inputImage);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            workspace.addImage(inputImage);
        }

        if (showOutput)
            inputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        return returnedParameters;

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

    void addParameterDescriptions() {
      parameters.get(INPUT_IMAGE).setDescription(
              "Image from workspace to apply 3D skeletonisation operation to.  This must be an 8-bit binary image (255 = background, 0 = foreground).");

      parameters.get(APPLY_TO_INPUT).setDescription(
              "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

      parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
              + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

    }
}
