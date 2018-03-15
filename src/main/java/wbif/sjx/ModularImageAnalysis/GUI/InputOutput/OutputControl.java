package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by steph on 29/07/2017.
 */
public class OutputControl extends Module {
    public static final String EXPORT_SUMMARY = "Export summary";
    public static final String SUMMARY_TYPE = "Summary type";
    public static final String CALCULATE_SUMMARY_MEAN = "Calculate summary means";
    public static final String CALCULATE_SUMMARY_STD = "Calculate summary standard deviations";
    public static final String CALCULATE_SUMMARY_SUM = "Calculate summary sums";
    public static final String EXPORT_INDIVIDUAL_OBJECTS = "Export individual objects";
    public static final String CONTINUOUS_DATA_EXPORT = "Continuous data export";
    public static final String SAVE_EVERY_N = "Save every n files";
    public static final String SELECT_MEASUREMENTS = "Show measurement selection";

    public interface SummaryTypes {
        String ONE_AVERAGE_PER_FILE = "Per input file";
        String AVERAGE_PER_TIMEPOINT = "Per timepoint per input file";

        String[] ALL = new String[]{ONE_AVERAGE_PER_FILE,AVERAGE_PER_TIMEPOINT};

    }


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
        parameters.add(new Parameter(EXPORT_SUMMARY,Parameter.BOOLEAN,true));
        parameters.add(
                new Parameter(SUMMARY_TYPE,Parameter.CHOICE_ARRAY,SummaryTypes.ONE_AVERAGE_PER_FILE,SummaryTypes.ALL));

        parameters.add(new Parameter(CALCULATE_SUMMARY_MEAN,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_SUMMARY_STD,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_SUMMARY_SUM,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(EXPORT_INDIVIDUAL_OBJECTS,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CONTINUOUS_DATA_EXPORT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SAVE_EVERY_N,Parameter.INTEGER,10));
        parameters.add(new Parameter(SELECT_MEASUREMENTS,Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(EXPORT_SUMMARY));
        if (parameters.getValue(EXPORT_SUMMARY)) {
            returnedParameters.add(parameters.getParameter(SUMMARY_TYPE));
            returnedParameters.add(parameters.getParameter(CALCULATE_SUMMARY_MEAN));
            returnedParameters.add(parameters.getParameter(CALCULATE_SUMMARY_STD));
            returnedParameters.add(parameters.getParameter(CALCULATE_SUMMARY_SUM));
        }

        returnedParameters.add(parameters.getParameter(EXPORT_INDIVIDUAL_OBJECTS));
        returnedParameters.add(parameters.getParameter(CONTINUOUS_DATA_EXPORT));
        if (parameters.getValue(CONTINUOUS_DATA_EXPORT)) {
            returnedParameters.add(parameters.getParameter(SAVE_EVERY_N));
        }

        returnedParameters.add(parameters.getParameter(SELECT_MEASUREMENTS));


        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}

