package io.github.mianalysis.mia.module.workflow;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ModuleP;
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
 * Created by Stephen Cross on 23/11/2018.
 */

/**
* Implement workflow handling outcome based on whether another module is enabled or disabled.  Outcomes can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis to skipped.<br><br>Note: This only applies to modules explictly enabled/disabled by the user.  It does not apply to modules that are inactive due to invalid parameters (modules highlighted in red in the module list).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ModuleIsEnabled extends AbstractWorkspaceHandler {

	/**
	* 
	*/
    public static final String CONDITION_SEPARATOR = "Condition";

	/**
	* Controls whether the specified workflow handling outcome is applied if another module is enabled or disabled:<br><ul><li>"Module is enabled" Execute specified outcome if another module is enabled.</li><li>"Module is not enabled" Execute specified outcome if another module is not enabled.</li></ul>
	*/
    public static final String TEST_MODE = "Test mode";

	/**
	* Module to test the enabled/disabled state of.  This doesn't necessarily need to be a module that would execute before the current module.
	*/
    public static final String TEST_MODULE = "Test module";


	/**
	* 
	*/
    public static final String RESULT_SEPARATOR = "Result";

    public ModuleIsEnabled(Modules modules) {
        super("Module is enabled condition", modules);
    }

    public interface TestModes {
        String MODULE_IS_ENABLED = "Module is enabled";
        String MODULE_IS_NOT_ENABLED = "Module is not enabled";

        String[] ALL = new String[] { MODULE_IS_ENABLED, MODULE_IS_NOT_ENABLED };

    }

    public boolean testDoRedirect(Workspace workspace) {
        String testMode = parameters.getValue(TEST_MODE,workspace);
        Module testModule = modules.getModuleByID(parameters.getValue(TEST_MODULE,workspace));

        if (testModule == null)
            return false;

        boolean terminate = false;
        switch (testMode) {
            case TestModes.MODULE_IS_ENABLED:
                terminate = testModule.isEnabled();
                break;
            case TestModes.MODULE_IS_NOT_ENABLED:
                terminate = !testModule.isEnabled();
                break;
        }

        return terminate;

    }

    @Override
    public Category getCategory() {
        return Categories.WORKFLOW;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Implement workflow handling outcome based on whether another module is enabled or disabled.  Outcomes can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis to skipped.<br><br>"

                + "Note: This only applies to modules explictly enabled/disabled by the user.  It does not apply to modules that are inactive due to invalid parameters (modules highlighted in red in the module list).";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE,workspace);

        if (testDoRedirect(workspace))
            return processTermination(parameters, workspace, showRedirectMessage);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ChoiceP(TEST_MODE, this, TestModes.MODULE_IS_ENABLED, TestModes.ALL));
        parameters.add(new ModuleP(TEST_MODULE, this, true));

        parameters.add(new SeparatorP(RESULT_SEPARATOR, this));
        parameters.addAll(super.updateAndGetParameters());

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(TEST_MODE));
        returnedParameters.add(parameters.getParameter(TEST_MODULE));

        returnedParameters.add(parameters.getParameter(RESULT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CONTINUATION_MODE));
        switch ((String) parameters.getValue(CONTINUATION_MODE,workspace)) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                returnedParameters.add(parameters.getParameter(REDIRECT_MODULE));
                redirectModuleID = parameters.getValue(REDIRECT_MODULE,workspace);
                returnedParameters.add(parameters.getParameter(SHOW_REDIRECT_MESSAGE));
                if ((boolean) parameters.getValue(SHOW_REDIRECT_MESSAGE,workspace)) {
                    returnedParameters.add(parameters.getParameter(REDIRECT_MESSAGE));
                }
                break;
            case ContinuationModes.TERMINATE:
                returnedParameters.add(parameters.getParameter(SHOW_TERMINATION_WARNING));
                returnedParameters.add(parameters.getParameter(EXPORT_WORKSPACE));
                returnedParameters.add(parameters.getParameter(REMOVE_IMAGES));
                returnedParameters.add(parameters.getParameter(REMOVE_OBJECTS));
                redirectModuleID = null;
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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(TEST_MODE).setDescription(
                "Controls whether the specified workflow handling outcome is applied if another module is enabled or disabled:<br><ul>"
                        + "<li>\"" + TestModes.MODULE_IS_ENABLED
                        + "\" Execute specified outcome if another module is enabled.</li>"

                        + "<li>\"" + TestModes.MODULE_IS_NOT_ENABLED
                        + "\" Execute specified outcome if another module is not enabled.</li></ul>");

        parameters.get(TEST_MODULE).setDescription(
                "Module to test the enabled/disabled state of.  This doesn't necessarily need to be a module that would execute before the current module.");

    }
}
