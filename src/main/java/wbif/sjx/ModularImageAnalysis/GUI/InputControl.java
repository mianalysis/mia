package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by steph on 29/07/2017.
 */
public class InputControl extends HCModule {
    public static final String INPUT_MODE = "Input mode";
    public static final String SINGLE_FILE_PATH = "Single file path";

    private static final String SINGLE_FILE = "Single file";
    private static final String BATCH = "Batch";
    public static final String[] INPUT_MODES = new String[]{SINGLE_FILE,BATCH};


    @Override
    public String getTitle() {
        return "Input control";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) throws GenericMIAException {

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_MODE, Parameter.CHOICE_ARRAY,INPUT_MODES[0],INPUT_MODES));
        parameters.addParameter(new Parameter(SINGLE_FILE_PATH, Parameter.FILE_PATH,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(INPUT_MODE));

        switch ((String) parameters.getValue(INPUT_MODE)) {
            case SINGLE_FILE:
                returnedParameters.addParameter(parameters.getParameter(SINGLE_FILE_PATH));
                break;

            case BATCH:
                break;

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
