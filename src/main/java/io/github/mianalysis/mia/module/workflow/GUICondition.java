package io.github.mianalysis.mia.module.workflow;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GUICondition extends AbstractWorkspaceHandler {

	/**
	* 
	*/
    public static final String CONDITION_SEPARATOR = "Condition";

	/**
	* Currently-selected choice from the available set (all choices added via "Add choice" option).  The relevant workflow operation (e.g. termination/redirection) will be implemented for the selected condition.  This control can be made visible in the processing view, so users can select between a set of pre-determined outcomes.
	*/
    public static final String CHOICE = "Choice";

	/**
	* When selected, the selected choice will be stored as a metadata item.  This allows it to be exported to the final spreadsheet.
	*/
    public static final String STORE_AS_METADATA_ITEM = "Store as metadata item";

	/**
	* Name for selected choice to be stored as metadata using.  The choice will be accessible via this metadata name in subsequent modules and can be exported to the final spreadsheet.
	*/
    public static final String METADATA_NAME = "Metadata name";

    public static final String CHOICE_SEPARATOR = "Choice settings";

	/**
	* Add another condition that "Choice" can select from.  Each choice can have its own handling outcome (e.g. termination/redirection).
	*/
    public static final String ADD_CHOICE = "Add choice";
    public static final String CHOICE_NAME = "Choice name";

    public GUICondition(Modules modules) {
        super("GUI condition", modules);
        deprecated = true;
    }

    @Override
    public Module getRedirectModule(Workspace workspace) {
        // Default redirect module is the next one in the sequence
        int idx = modules.indexOf(this) + 1;
        if (idx >= modules.size())
            redirectModule = null;
        else
            redirectModule = modules.get(idx);

        String choice = parameters.getValue(CHOICE, workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_CHOICE, workspace);
        for (Parameters collection : collections.values()) {
            if (collection.getValue(CHOICE_NAME, workspace).equals(choice)) {
                switch ((String) collection.getValue(CONTINUATION_MODE, workspace)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        redirectModule = collection.getValue(REDIRECT_MODULE, workspace);
                        break;
                    case ContinuationModes.TERMINATE:
                    default:
                        redirectModule = null;
                        break;
                }
            }
        }

        return redirectModule;

    }

    @Override
    public Category getCategory() {
        return Categories.WORKFLOW;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: Please use \"" + new GlobalVariables(null).getName() + "\" module (in \""
                + GlobalVariables.VariableTypes.CHOICE + "\" mode) in conjunction with \""
                + new FixedTextCondition(null).getName() + "\", which offer equivalent functionality."

                + "<br><br>Implement variable workflow handling outcomes based on a user-selectable drop-down list of choices.  Each choice has a unique workflow outcome, which can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis workflow to be skipped.<br><br>"

                + "An example usage case for GUI conditions is providing a drop-down box on the basic control view.  With this simple control, the user can execute different blocks of the workflow without having to fundamentally understand how they are assembled.";

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String choice = parameters.getValue(CHOICE, workspace);
        boolean storeAsMetadata = parameters.getValue(STORE_AS_METADATA_ITEM, workspace);
        String metadataName = parameters.getValue(METADATA_NAME, workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_CHOICE, workspace);
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE, workspace);

        // Getting choice parameters
        Status status = Status.FAIL;
        for (Parameters collection : collections.values()) {
            if (collection.getValue(CHOICE_NAME, workspace).equals(choice)) {
                status = processTermination(collection, workspace, showRedirectMessage);
            }
        }

        if (storeAsMetadata)
            workspace.getMetadata().put(metadataName, choice);

        if (showOutput && storeAsMetadata)
            workspace.showMetadata(this);

        if (status == Status.FAIL) {
            MIA.log.writeWarning("Did not find matching termination option");
            status = Status.PASS;
        }

        return status;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ChoiceP(CHOICE, this, "", new String[0]));
        parameters.add(new BooleanP(STORE_AS_METADATA_ITEM, this, false));
        parameters.add(new StringP(METADATA_NAME, this));
        parameters.getParameter(CHOICE).setVisible(true);

        Parameters collection = new Parameters();
        collection.add(new SeparatorP(CHOICE_SEPARATOR, this));
        collection.add(new StringP(CHOICE_NAME, this, ""));
        collection.addAll(super.updateAndGetParameters());
        parameters.add(new ParameterGroup(ADD_CHOICE, this, collection, 0, getUpdaterAndGetter()));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHOICE));
        returnedParameters.add(parameters.getParameter(STORE_AS_METADATA_ITEM));
        if ((boolean) parameters.getValue(STORE_AS_METADATA_ITEM, workspace))
            returnedParameters.add(parameters.getParameter(METADATA_NAME));
        returnedParameters.add(parameters.getParameter(ADD_CHOICE));

        // Updating options in choice menu
        ParameterGroup group = (ParameterGroup) parameters.get(ADD_CHOICE);
        String[] choices = getGUIChoices(group.getCollections(false));
        ((ChoiceP) parameters.getParameter(CHOICE)).setChoices(choices);

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
        Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        if ((boolean) parameters.getValue(STORE_AS_METADATA_ITEM, workspace))
            returnedRefs.add(metadataRefs.getOrPut(parameters.getValue(METADATA_NAME, workspace)));

        return returnedRefs;

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

        parameters.get(CHOICE)
                .setDescription("Currently-selected choice from the available set (all choices added via \""
                        + ADD_CHOICE
                        + "\" option).  The relevant workflow operation (e.g. termination/redirection) will be implemented for the selected condition.  This control can be made visible in the processing view, so users can select between a set of pre-determined outcomes.");

        parameters.get(STORE_AS_METADATA_ITEM).setDescription(
                "When selected, the selected choice will be stored as a metadata item.  This allows it to be exported to the final spreadsheet.");

        parameters.get(METADATA_NAME).setDescription(
                "Name for selected choice to be stored as metadata using.  The choice will be accessible via this metadata name in subsequent modules and can be exported to the final spreadsheet.");

        parameters.get(ADD_CHOICE).setDescription("Add another condition that \"" + CHOICE
                + "\" can select from.  Each choice can have its own handling outcome (e.g. termination/redirection).");

        ((ParameterGroup) parameters.get(ADD_CHOICE)).getTemplateParameters().get(CHOICE_NAME)
                .setDescription("Name that this choice will appear as in the \"" + CHOICE + "\" drop-down menu.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                returnedParameters.add(params.getParameter(CHOICE_SEPARATOR));
                returnedParameters.add(params.getParameter(CHOICE_NAME));
                returnedParameters.add(params.getParameter(CONTINUATION_MODE));
                switch ((String) params.getValue(CONTINUATION_MODE, null)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        returnedParameters.add(params.getParameter(REDIRECT_MODULE));
                        redirectModule = params.getValue(REDIRECT_MODULE, null);
                        returnedParameters.add(params.getParameter(SHOW_REDIRECT_MESSAGE));
                        if ((boolean) params.getValue(SHOW_REDIRECT_MESSAGE, null)) {
                            returnedParameters.add(params.getParameter(REDIRECT_MESSAGE));
                        }
                        break;
                    case ContinuationModes.TERMINATE:
                    default:
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

    String[] getGUIChoices(LinkedHashMap<Integer, Parameters> collections) {
        String[] choices = new String[collections.size()];

        int i = 0;
        for (Parameters collection : collections.values()) {
            choices[i++] = collection.getValue(CHOICE_NAME, null);
        }

        return choices;

    }
}
