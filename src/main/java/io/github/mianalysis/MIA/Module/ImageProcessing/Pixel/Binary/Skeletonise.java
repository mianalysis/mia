package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
import sc.fiji.skeletonize3D.Skeletonize3D_;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.ImageMath;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.OutputImageP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceInterfaces.BinaryLogicInterface;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;

public class Skeletonise extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String SKELETONISE_SEPARATOR = "Skeletonise controls";
    public static final String BINARY_LOGIC = "Binary logic";

    public interface BinaryLogic extends BinaryLogicInterface {
    }


    public Skeletonise(Modules modules) {
        super("Skeletonise", modules);
    }


    public static void process(Image image, boolean blackBackground) {
        if (!blackBackground)
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
        if (!blackBackground)
            InvertIntensity.process(image);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Creates an skeletonised representation of a specific binary image in the workspace.  The input and output images will be 8-bit with binary logic determined by the \"" + BINARY_LOGIC + "\" parameter.  Each minima will show the lowest local intensity region within a specific dynamic range.  Local variation greater than this dynamic will result in the creation of more minima.  Uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";
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
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImage = new Image(outputImageName, inputImagePlus.duplicate());

        process(inputImage,blackBackground);

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

        parameters.add(new SeparatorP(SKELETONISE_SEPARATOR, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        
        returnedParameters.add(parameters.getParameter(SKELETONISE_SEPARATOR));
            returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

        return returnedParameters;

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

    void addParameterDescriptions() {
      parameters.get(INPUT_IMAGE).setDescription(
              "Image from workspace to apply 3D skeletonisation operation to.  This image will be 8-bit with binary logic determined by the \"" + BINARY_LOGIC + "\" parameter.");

      parameters.get(APPLY_TO_INPUT).setDescription(
              "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

      parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
              + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

              parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());
              
    }
}
