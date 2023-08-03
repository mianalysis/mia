package io.github.mianalysis.mia.module.workflow;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 09/02/2018.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AddPause extends Module {

	/**
	* 
	*/
    public static final String PAUSE_SEPARATOR = "Pause controls";

	/**
	* When selected, an image from the workspace can be automatically displayed when this module executes.
	*/
    public static final String SHOW_IMAGE = "Show image";

	/**
	* If "Show image" is selected, this image will be displayed when the module executes.
	*/
    public static final String INPUT_IMAGE = "Input image";

    private static final String CONTINUE = "Continue";
    private static final String TERMINATE = "Terminate";

    public AddPause(Modules modules) {
        super("Add pause", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.WORKFLOW;
    }

    @Override
    public String getDescription() {
        return "Pauses workflow execution and displays an option dialog to continue or quit.  Optionally, an image from the workspace can also be displayed.  An example usage would be during parameter optimisation, where subsequent elements of the analysis only want executing if the first steps are deemed successful (and this can't be automatically determined).";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean showImage = parameters.getValue(SHOW_IMAGE,workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);

        if (showImage) {
            Image inputImage = workspace.getImage(inputImageName);
            ImagePlus showIpl = new Duplicator().run(inputImage.getImagePlus());
            showIpl.setTitle(inputImageName);
            showIpl.show();
        }

        String[] options = {CONTINUE,TERMINATE};
        JOptionPane optionPane = new JOptionPane("Execution paused.  What would you like to do?",JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,options);
        JDialog dialog = optionPane.createDialog(null, "Execution paused");
        dialog.setModal(false);
        dialog.setVisible(true);

        writeStatus("Execution paused");

        while (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Do nothing as the user has selected this
            }
        }

        switch ((String) optionPane.getValue()) {
            case CONTINUE:
                return Status.PASS;

            case TERMINATE:
                return Status.TERMINATE;

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(PAUSE_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        if ((boolean) parameters.getValue(SHOW_IMAGE,workspace)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
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

    protected void addParameterDescriptions() {
        parameters.get(SHOW_IMAGE).setDescription(
                "When selected, an image from the workspace can be automatically displayed when this module executes.");

        parameters.get(INPUT_IMAGE).setDescription(
                "If \"" + SHOW_IMAGE + "\" is selected, this image will be displayed when the module executes.");

    }
}
