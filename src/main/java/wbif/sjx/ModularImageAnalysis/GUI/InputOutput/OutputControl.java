package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by steph on 29/07/2017.
 */
public class OutputControl extends HCModule {
    public static final String EXPORT_XLSX = "Export results to Excel file";
    public static final String EXPORT_SUMMARY = "Export summary";
    public static final String EXPORT_INDIVIDUAL_OBJECTS = "Export individual objects";
    public static final String CONTINUOUS_DATA_EXPORT = "Continuous data export";
    public static final String SAVE_EVERY_N = "Save every n files";
    public static final String SELECT_MEASUREMENTS = "Select measurements";


    @Override
    public String getTitle() {
        return "Output control";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(EXPORT_XLSX,Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(EXPORT_SUMMARY,Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(EXPORT_INDIVIDUAL_OBJECTS,Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(CONTINUOUS_DATA_EXPORT,Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(SAVE_EVERY_N,Parameter.INTEGER,10));
        parameters.addParameter(new Parameter(SELECT_MEASUREMENTS,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(EXPORT_XLSX));

        if (parameters.getValue(EXPORT_XLSX)) {
            returnedParameters.addParameter(parameters.getParameter(EXPORT_SUMMARY));
            returnedParameters.addParameter(parameters.getParameter(EXPORT_INDIVIDUAL_OBJECTS));
            returnedParameters.addParameter(parameters.getParameter(CONTINUOUS_DATA_EXPORT));

            if (parameters.getValue(CONTINUOUS_DATA_EXPORT)) {
                returnedParameters.addParameter(parameters.getParameter(SAVE_EVERY_N));
            }

            returnedParameters.addParameter(parameters.getParameter(SELECT_MEASUREMENTS));

        }

        return returnedParameters;

    }

    @Override
    public void initialiseReferences() {

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}

