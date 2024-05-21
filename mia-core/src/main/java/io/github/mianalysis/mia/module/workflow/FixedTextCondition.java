package io.github.mianalysis.mia.module.workflow;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
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
* Implement variable workflow handling outcomes based on comparison of a fixed text value against a series of fixed conditions.  If the text test value matches any of the conditions the workflow handling outcome associated with that condition will be implemented.  Outcomes can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis to be looped, or sections of the workflow to be skipped.<br><br>An example usage case for fixed text conditions is implementing the same behaviour at multiple parts of the workflow without having to control them individually.  This can be achieved using a global variable (see "Global variables" module).  The global variable could be specified once, early on in the analysis, then used as "Test value" in this module.  As such, it's possible to only specify the value once, but refer to it in multiple "Fixed text condition" modules.  Note: The global variables module allows variables to be user-selected from a drop-down list, negating risk of mis-typing parameter names that will be compared in this module.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FixedTextCondition extends AbstractWorkspaceHandler {

	/**
	* 
	*/
    public static final String CONDITION_SEPARATOR = "Condition";

	/**
	* Text value that will tested.  If this matches any of the reference values listed within this module the relevant workflow operation (e.g. termination/redirection) will be implemented.  This text value could be a global variable.
	*/
    public static final String TEST_VALUE = "Test value";

	/**
	* Add another condition that "Test value" can be compared against.  Each condition can have its own handling outcome (e.g. termination/redirection).
	*/
    public static final String ADD_CONDITION = "Add condition";
    public static final String REFERENCE_VALUE = "Reference value";


	/**
	* 
	*/
    public static final String RESULT_SEPARATOR = "Result";

    public FixedTextCondition(Modules modules) {
        super("Fixed text condition", modules);
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
        return "Implement variable workflow handling outcomes based on comparison of a fixed text value against a series of fixed conditions.  If the text test value matches any of the conditions the workflow handling outcome associated with that condition will be implemented.  Outcomes can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis to be looped, or sections of the workflow to be skipped.<br><br>"

                + "An example usage case for fixed text conditions is implementing the same behaviour at multiple parts of the workflow without having to control them individually.  This can be achieved using a global variable (see \""
                + new GlobalVariables(null).getName()
                + "\" module).  The global variable could be specified once, early on in the analysis, then used as \""
                + TEST_VALUE
                + "\" in this module.  As such, it's possible to only specify the value once, but refer to it in multiple \""
                + getName()
                + "\" modules.  Note: The global variables module allows variables to be user-selected from a drop-down list, negating risk of mis-typing parameter names that will be compared in this module.";

    }

    @Override
    public Module getRedirectModule(Workspace workspace) {
        // Default redirect module is the next one in the sequence
        int idx = modules.indexOf(this) + 1;
        if (idx >= modules.size())
            redirectModule = null;
        else
            redirectModule = modules.get(idx);

        String testValue = parameters.getValue(TEST_VALUE,workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_CONDITION,workspace);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(REFERENCE_VALUE,workspace).equals(testValue)) {
                switch ((String) collection.getValue(CONTINUATION_MODE,workspace)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        redirectModule = collection.getValue(REDIRECT_MODULE,workspace);
                        break;
                    case ContinuationModes.TERMINATE:
                        redirectModule = null;
                        break;
                }
            }
        }

        return redirectModule;

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String testValue = parameters.getValue(TEST_VALUE,workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_CONDITION,workspace);
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE,workspace);

        // Getting choice parameters
        for (Parameters collection : collections.values()) {
            if (collection.getValue(REFERENCE_VALUE,workspace).equals(testValue)) {
                return processTermination(collection, workspace, showRedirectMessage);
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        Parameters collection = new Parameters();
        collection.add(new StringP(REFERENCE_VALUE, this, ""));
        collection.addAll(super.updateAndGetParameters());

        parameters.add(new StringP(TEST_VALUE, this));
        parameters.add(new SeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ParameterGroup(ADD_CONDITION, this, collection, 1, getUpdaterAndGetter()));

        parameters.add(new SeparatorP(RESULT_SEPARATOR, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(TEST_VALUE));
        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_CONDITION));

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

        parameters.get(TEST_VALUE).setDescription(
                "Text value that will tested.  If this matches any of the reference values listed within this module the relevant workflow operation (e.g. termination/redirection) will be implemented.  This text value could be a global variable.");

        parameters.get(ADD_CONDITION).setDescription("Add another condition that \"" + TEST_VALUE
                + "\" can be compared against.  Each condition can have its own handling outcome (e.g. termination/redirection).");

        ((ParameterGroup) parameters.get(ADD_CONDITION)).getTemplateParameters().get(REFERENCE_VALUE)
                .setDescription("Value \"" + TEST_VALUE
                        + "\" will be compared against.  If the two values match, the associated handling outcome of this condition will be implemented.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                returnedParameters.add(params.getParameter(REFERENCE_VALUE));
                returnedParameters.add(params.getParameter(CONTINUATION_MODE));
                switch ((String) params.getValue(CONTINUATION_MODE,null)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        returnedParameters.add(params.getParameter(REDIRECT_MODULE));
                        redirectModule = params.getValue(REDIRECT_MODULE,null);
                        returnedParameters.add(params.getParameter(SHOW_REDIRECT_MESSAGE));
                        if ((boolean) params.getValue(SHOW_REDIRECT_MESSAGE,null)) {
                            returnedParameters.add(params.getParameter(REDIRECT_MESSAGE));
                        }
                        break;
                    case ContinuationModes.TERMINATE:
                        returnedParameters.add(params.getParameter(SHOW_TERMINATION_WARNING));
                        returnedParameters.add(params.getParameter(EXPORT_WORKSPACE));
                        returnedParameters.add(params.getParameter(REMOVE_IMAGES));
                        returnedParameters.add(params.getParameter(REMOVE_OBJECTS));
                        redirectModule = null;
                        break;
                }

                return returnedParameters;

            }
        };
    }
}
