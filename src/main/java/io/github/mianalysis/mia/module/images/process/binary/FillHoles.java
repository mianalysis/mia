package io.github.mianalysis.mia.module.images.process.binary;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Reconstruction3D;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Performs a 3D fill holes operation on an input binary image.  This operation will change all background pixels in a region which is fully enclosed by foreground pixels to foreground.  This image will be 8-bit with binary logic determined by the "Binary logic" parameter.  Uses the plugin "<a href="https://github.com/ijpb/MorphoLibJ">MorphoLibJ</a>".
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FillHoles extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from workspace to apply fill holes operation to.  This image will be 8-bit with binary logic determined by the "Binary logic" parameter.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the "Output image" parameter.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If "Apply to input image" is not selected, the post-operation image will be saved to the workspace with this name.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String FILL_HOLES_SEPARATOR = "Fill holes controls";

	/**
	* Controls whether objects are considered to be white (255 intensity) on a black (0 intensity) background, or black on a white background.
	*/
    public static final String BINARY_LOGIC = "Binary logic";

    public interface BinaryLogic extends BinaryLogicInterface {
    }
    

    public FillHoles(Modules modules) {
        super("Fill holes", modules);
    }   

    public static void process(ImagePlus ipl, boolean blackBackground) {
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        // MorphoLibJ takes objects as being white
        if (!blackBackground)
            InvertIntensity.process(ipl);

        for (int c=1;c<=nChannels;c++) {
            for (int t = 1; t <= nFrames; t++) {
                ImagePlus iplOrig = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                ImageStack iplFill = Reconstruction3D.fillHoles(iplOrig.getImageStack());

                for (int z = 1; z <= iplFill.getSize(); z++) {
                    ipl.setPosition(c, z, t);
                    ImageProcessor iprOrig = ipl.getProcessor();
                    ImageProcessor iprFilt = iplFill.getProcessor(z);

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            iprOrig.setf(x, y, iprFilt.getf(x, y));
                        }
                    }
                }
            }
        }

        if (!blackBackground)
            InvertIntensity.process(ipl);

    }



    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_BINARY;
    }

    @Override
    public String getDescription() {
        return "Performs a 3D fill holes operation on an input binary image.  This operation will change all background pixels in a region which is fully enclosed by foreground pixels to foreground.  This image will be 8-bit with binary logic determined by the \"" + BINARY_LOGIC + "\" parameter.  Uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC,workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        process(inputImagePlus, blackBackground);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image ("+outputImageName+") to workspace");
            Image outputImage = ImageFactory.createImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.show();

        } else {
            if (showOutput) inputImage.show();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(FILL_HOLES_SEPARATOR, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        
        returnedParameters.add(parameters.getParameter(FILL_HOLES_SEPARATOR));
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
              "Image from workspace to apply fill holes operation to.  This image will be 8-bit with binary logic determined by the \"" + BINARY_LOGIC + "\" parameter.");

      parameters.get(APPLY_TO_INPUT).setDescription(
              "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

      parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
              + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

      parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());
              
    }
}
