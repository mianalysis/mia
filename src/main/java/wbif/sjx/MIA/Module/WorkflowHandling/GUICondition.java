package wbif.sjx.MIA.Module.WorkflowHandling;

import java.util.LinkedHashMap;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup.ParameterUpdaterAndGetter;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
public class GUICondition extends CoreWorkspaceHandler {
    public static final String CONDITION_SEPARATOR = "Condition";
    public static final String CHOICE = "Choice";
    public static final String ADD_CHOICE = "Add choice";
    public static final String CHOICE_NAME = "Choice name";

    public GUICondition(ModuleCollection modules) {
        super("GUI condition", modules);
    }

    @Override
    public Module getRedirectModule() {
        String choice = parameters.getValue(CHOICE);
        LinkedHashMap<Integer, ParameterCollection> collections = parameters.getValue(ADD_CHOICE);
        for (ParameterCollection collection : collections.values()) {
            if (collection.getValue(CHOICE_NAME).equals(choice)) {
                switch ((String) collection.getValue(CONTINUATION_MODE)) {
                    case ContinuationModes.REDIRECT_TO_MODULE:
                        redirectModule = collection.getValue(REDIRECT_MODULE);
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
    public String getPackageName() {
        return PackageNames.WORKFLOW_HANDLING;
    }

    @Override
    public String getDescription() {
        return "Implement variable workflow handling outcomes based on a user-selectable drop-down list of choices.  Each choice has a unique workflow outcome, which can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis workflow to be skipped.<br><br>"
        
        + "An example usage case for GUI conditions is providing a drop-down box on the basic control view.  With this simple control, the user can execute different blocks of the workflow without having to fundamentally understand how they are assembled.";

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String choice = parameters.getValue(CHOICE);
        LinkedHashMap<Integer, ParameterCollection> collections = parameters.getValue(ADD_CHOICE);
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE);

        // Getting choice parameters
        for (ParameterCollection collection : collections.values()) {
            if (collection.getValue(CHOICE_NAME).equals(choice)) {
                return processTermination(collection, workspace, showRedirectMessage);
            }
        }
        MIA.log.writeWarning("Did not find matching termination option");

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ParamSeparatorP(CONDITION_SEPARATOR, this));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new StringP(CHOICE_NAME, this, ""));
        collection.addAll(super.updateAndGetParameters());

        parameters.add(new ChoiceP(CHOICE, this, "", new String[0]));
        parameters.getParameter(CHOICE).setVisible(true);
        parameters.add(new ParameterGroup(ADD_CHOICE, this, collection, 0, getUpdaterAndGetter()));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHOICE));
        returnedParameters.add(parameters.getParameter(ADD_CHOICE));

        // Updating options in choice menu
        ParameterGroup group = (ParameterGroup) parameters.get(ADD_CHOICE);
        String[] choices = getGUIChoices(group.getCollections(false));
        ((ChoiceP) parameters.getParameter(CHOICE)).setChoices(choices);

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
        parameters.get(CHOICE).setDescription("Currently-selected choice from the available set (all choices added via \""+ADD_CHOICE+"\" option).  The relevant workflow operation (e.g. termination/redirection) will be implemented for the selected condition.  This control can be made visible in the basic view, so users can select between a set of pre-determined outcomes.");

        parameters.get(ADD_CHOICE).setDescription("Add another condition that \""+CHOICE+"\" can select from.  Each choice can have its own handling outcome (e.g. termination/redirection).");

        ((ParameterGroup) parameters.get(ADD_CHOICE)).getTemplateParameters().get(CHOICE_NAME).setDescription("Name that this choice will appear as in the \""+CHOICE+"\" drop-down menu.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public ParameterCollection updateAndGet(ParameterCollection params) {
                ParameterCollection returnedParameters = new ParameterCollection();

                returnedParameters.add(params.getParameter(CHOICE_NAME));
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
                    default:
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

    String[] getGUIChoices(LinkedHashMap<Integer, ParameterCollection> collections) {
        String[] choices = new String[collections.size()];

        int i = 0;
        for (ParameterCollection collection : collections.values()) {
            choices[i++] = collection.getValue(CHOICE_NAME);
        }

        return choices;

    }
}
