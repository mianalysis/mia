package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Arrow;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;


/**
* Removes any overlay elements from specified image.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ClearOverlay extends AbstractOverlay {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* Image for which overlay will be cleared
	*/
    public static final String INPUT_IMAGE = "Input image";
    

	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Image output";

	/**
	* Determines if the modifications made to the input image (removed overlay) will be applied to that image or directed to a new image.  When selected, the input image will be updated.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.
	*/
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";

	/**
	* The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).
	*/
    public static final String OUTPUT_IMAGE = "Output image";
    
    
    public ClearOverlay(Modules modules) {
        super("Clear overlay", modules);
    }
    
    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }
    
    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Removes any overlay elements from specified image.";
        
    }
    
    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();
        
        // Only add output to workspace if not applying to input
        if (applyToInput)
        addOutputToWorkspace = false;
        
        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
        ipl = new Duplicator().run(ipl);
        
        ipl.getOverlay().clear();
        
        Image outputImage = ImageFactory.createImage(outputImageName, ipl);
        
        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
        workspace.addImage(outputImage);
        if (showOutput)
        outputImage.show();
        
        return Status.PASS;
        
    }
    
    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        
        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        
        addParameterDescriptions();
        
    }
    
    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        
        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));
            
            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                
            }
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
    
    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.get(INPUT_IMAGE)
        .setDescription("Image for which overlay will be cleared");
                
        parameters.get(APPLY_TO_INPUT).setDescription(
        "Determines if the modifications made to the input image (removed overlay) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");
        
        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
        "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");
        parameters.get(OUTPUT_IMAGE).setDescription(
        "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");
        
    }
}
