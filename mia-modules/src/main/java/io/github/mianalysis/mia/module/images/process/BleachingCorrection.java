package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import emblcmci.BleachCorrection_ExpoFit;
import emblcmci.BleachCorrection_MH;
import emblcmci.BleachCorrection_SimpleRatio;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
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

/**
 * Created by sc13967 on 30/11/2017.
 */

/**
* Apply bleaching correction to a specified image.  This adjusts intensities in all frames (after the first) to match the histogram distribution of the first frame.  It is intended to account for any fluorophore bleaching that occurs during acquisition of a timecourse.<br><br>This macro runs the Fiji bleaching correction plugin, "<a href="https://imagej.net/Bleach_Correction">Bleach Correction</a>".
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class BleachingCorrection extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from workspace to apply bleaching correction process to.
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
    public static final String CORRECTION_SEPARATOR = "Correction controls";

	/**
	* Controls the bleach correction algorithm to use:<br><ul><li>"Exponential fit" Assumes the bleaching process is controlled by a mono-exponential decay.  Will fail if the signal does not decay over time.  Calculation can be performed using a single ROI for all frames.</li><li>"Histogram matching" Adjusts image intensities so that the histograms match that from the first frame.</li><li>"Simple ratio" Normalises images to have the same mean intensity.  Calculation can be performed using a single ROI for all frames.</li></ul>
	*/
    public static final String CORRECTION_MODE = "Correction mode";

	/**
	* When selected, the bleaching and associated intensity correction will be calculated based on the pixels within a region of interest (specified as the objects of collection "ROI objects").  A single ROI is used for all frames (i.e. the region can't be different from frame to frame).
	*/
    public static final String USE_ROI_OBJECTS = "Use ROI objects";

	/**
	* If "Use ROI objects" is selected, this is the object collection which will act as the region of interest for calculating the bleaching.  Since only a single ROI can be used, all objects in this collection are reduced down into a single frame and timepoint.
	*/
    public static final String ROI_OBJECTS = "ROI objects";

    public interface CorrectionModes {
        public String EXPONENTIAL_FIT = "Exponential fit";
        public String HISTOGRAM_MATCHING = "Histogram matching";
        public String SIMPLE_RATIO = "Simple ratio";

        public String[] ALL = new String[] { EXPONENTIAL_FIT, HISTOGRAM_MATCHING, SIMPLE_RATIO };

    }

    public BleachingCorrection(Modules modules) {
        super("Bleaching correction", modules);
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
        return "Apply bleaching correction to a specified image.  This adjusts intensities in all frames (after the first) to match the histogram distribution of the first frame.  It is intended to account for any fluorophore bleaching that occurs during acquisition of a timecourse.<br><br>This macro runs the Fiji bleaching correction plugin, \"<a href=\"https://imagej.net/Bleach_Correction\">Bleach Correction</a>\".";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String correctionMode = parameters.getValue(CORRECTION_MODE,workspace);
        boolean useRoiObjects = parameters.getValue(USE_ROI_OBJECTS,workspace);
        String roiObjectsName = parameters.getValue(ROI_OBJECTS,workspace);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        // Although histogram matching has a ROI-compatible constructor the ROI is
        // unused
        if (correctionMode.equals(CorrectionModes.HISTOGRAM_MATCHING))
            useRoiObjects = false;

        Roi roi = null;
        if (useRoiObjects) {
            ObjsI roiObjects = workspace.getObjects(roiObjectsName);
            ObjI roiObject = roiObjects.getAsSingleObject();
            roiObject.setCoordinateSet(roiObject.getProjected().getCoordinateSet());
            roi = roiObject.getRoi(0);
        }

        switch (correctionMode) {
            case CorrectionModes.EXPONENTIAL_FIT:
                try {
                    BleachCorrection_ExpoFit bleachCorrectionExpoFit = new BleachCorrection_ExpoFit(inputImagePlus, roi);
                    bleachCorrectionExpoFit.setHeadlessProcessing(true);
                    bleachCorrectionExpoFit.core();
                } catch (NullPointerException e) {
                    MIA.log.writeWarning("Bleach correction failed (possible lack of exponential decay in signal)");
                }
                break;
            case CorrectionModes.HISTOGRAM_MATCHING:
                new BleachCorrection_MH(inputImagePlus).doCorrection();
                break;
            case CorrectionModes.SIMPLE_RATIO:
                inputImagePlus = new BleachCorrection_SimpleRatio(inputImagePlus, roi).correctBleach();
                break;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImagePlus.setPosition(1, 1, 1);
            inputImagePlus.updateChannelAndDraw();
            inputImage.setImagePlus(inputImagePlus);
            if (showOutput)
                inputImage.showAsIs();

        } else {
            ImageI outputImage = ImageFactories.getDefaultFactory().create(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showAsIs();

        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(CORRECTION_SEPARATOR, this));
        parameters.add(new ChoiceP(CORRECTION_MODE, this, CorrectionModes.HISTOGRAM_MATCHING, CorrectionModes.ALL));
        parameters.add(new BooleanP(USE_ROI_OBJECTS, this, false));
        parameters.add(new InputObjectsP(ROI_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CORRECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CORRECTION_MODE));
        switch ((String) parameters.getValue(CORRECTION_MODE,workspace)) {
            case CorrectionModes.EXPONENTIAL_FIT:
            case CorrectionModes.SIMPLE_RATIO:
                returnedParameters.add(parameters.getParameter(USE_ROI_OBJECTS));
                if ((boolean) parameters.getValue(USE_ROI_OBJECTS,workspace))
                    returnedParameters.add(parameters.getParameter(ROI_OBJECTS));
                break;
        }

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply bleaching correction process to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(CORRECTION_MODE).setDescription("Controls the bleach correction algorithm to use:<br><ul>" +

                "<li>\"" + CorrectionModes.EXPONENTIAL_FIT
                + "\" Assumes the bleaching process is controlled by a mono-exponential decay.  Will fail if the signal does not decay over time.  Calculation can be performed using a single ROI for all frames.</li>"
                +

                "<li>\"" + CorrectionModes.HISTOGRAM_MATCHING
                + "\" Adjusts image intensities so that the histograms match that from the first frame.</li>" +

                "<li>\"" + CorrectionModes.SIMPLE_RATIO
                + "\" Normalises images to have the same mean intensity.  Calculation can be performed using a single ROI for all frames.</li></ul>");

        parameters.get(USE_ROI_OBJECTS).setDescription(
                "When selected, the bleaching and associated intensity correction will be calculated based on the pixels within a region of interest (specified as the objects of collection \""
                        + ROI_OBJECTS
                        + "\").  A single ROI is used for all frames (i.e. the region can't be different from frame to frame).");
                
        parameters.get(ROI_OBJECTS).setDescription(
                "If \""+USE_ROI_OBJECTS+"\" is selected, this is the object collection which will act as the region of interest for calculating the bleaching.  Since only a single ROI can be used, all objects in this collection are reduced down into a single frame and timepoint.");
                        
    }
}
