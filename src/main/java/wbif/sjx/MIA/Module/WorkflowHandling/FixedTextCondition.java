package wbif.sjx.MIA.Module.WorkflowHandling;

import java.util.LinkedHashMap;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup.ParameterUpdaterAndGetter;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
public class FixedTextCondition extends AbstractWorkspaceHandler {
    public static final String CONDITION_SEPARATOR = "Condition";
    public static final String TEST_VALUE = "Test value";
    public static final String ADD_CONDITION = "Add condition";
    public static final String REFERENCE_VALUE = "Reference value";

    public FixedTextCondition(ModuleCollection modules) {
        super("Fixed text condition", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.WORKFLOW_HANDLING;
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
    public Module getRedirectModule() {
        // Default redirect module is the next one in the sequence
        int idx = modules.indexOf(this) + 1;
        if (idx >= modules.size())
            redirectModule = null;
        else
            redirectModule = modules.get(idx);

        String testValue = parameters.getValue(TEST_VALUE);
        LinkedHashMap<Integer, ParameterCollection> collections = parameters.getValue(ADD_CONDITION);

        for (ParameterCollection collection : collections.values()) {
            if (collection.getValue(REFERENCE_VALUE).equals(testValue)) {
                switch ((String) collection.getValue(CONTINUATION_MODE)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        redirectModule = collection.getValue(REDIRECT_MODULE);
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
        String testValue = parameters.getValue(TEST_VALUE);
        LinkedHashMap<Integer, ParameterCollection> collections = parameters.getValue(ADD_CONDITION);
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE);

        // Getting choice parameters
        for (ParameterCollection collection : collections.values()) {
            if (collection.getValue(REFERENCE_VALUE).equals(testValue)) {
                return processTermination(collection, workspace, showRedirectMessage);
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        ParameterCollection collection = new ParameterCollection();
        collection.add(new StringP(REFERENCE_VALUE, this, ""));
        collection.addAll(super.updateAndGetParameters());

        parameters.add(new StringP(TEST_VALUE, this));
        parameters.add(new SeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ParameterGroup(ADD_CONDITION, this, collection, 1, getUpdaterAndGetter()));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(TEST_VALUE));
        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_CONDITION));

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
            public ParameterCollection updateAndGet(ParameterCollection params) {
                ParameterCollection returnedParameters = new ParameterCollection();

                returnedParameters.add(params.getParameter(REFERENCE_VALUE));
                returnedParameters.add(params.getParameter(CONTINUATION_MODE));
                switch ((String) params.getValue(CONTINUATION_MODE)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        returnedParameters.add(params.getParameter(REDIRECT_MODULE));
                        redirectModule = params.getValue(REDIRECT_MODULE);
                        returnedParameters.add(params.getParameter(SHOW_REDIRECT_MESSAGE));
                        if ((boolean) params.getValue(SHOW_REDIRECT_MESSAGE)) {
                            returnedParameters.add(params.getParameter(REDIRECT_MESSAGE));
                        }
                        break;
                    case ContinuationModes.TERMINATE:
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
