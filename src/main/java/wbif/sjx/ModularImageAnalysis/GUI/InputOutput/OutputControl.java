package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by Stephen on 29/07/2017.
 */
public class OutputControl extends Module {
    public static final String EXPORT_MODE = "Export mode";
    public static final String METADATA_ITEM_FOR_GROUPING = "Metadata item for grouping";
    public static final String EXPORT_SUMMARY = "Export summary";
    public static final String SUMMARY_MODE = "Summary mode";
    public static final String SHOW_OBJECT_COUNTS = "Show object counts";
    public static final String SHOW_NUMBER_OF_CHILDREN = "Show number of children";
    public static final String CALCULATE_COUNT_MEAN = "Calculate count means";
    public static final String CALCULATE_COUNT_MIN = "Calculate count minima";
    public static final String CALCULATE_COUNT_MAX = "Calculate count maxima";
    public static final String CALCULATE_COUNT_STD = "Calculate count standard deviations";
    public static final String CALCULATE_COUNT_SUM = "Calculate count sums";
    public static final String EXPORT_INDIVIDUAL_OBJECTS = "Export individual objects";
    public static final String CONTINUOUS_DATA_EXPORT = "Continuous data export";
    public static final String SAVE_EVERY_N = "Save every n files";
    public static final String SELECT_MEASUREMENTS = "Show measurement selection";

    public interface ExportModes {
        String ALL_TOGETHER = "All together";
        String GROUP_BY_METADATA = "Group by metadata";
        String INDIVIDUAL_FILES = "Individual files";

        String[] ALL = new String[]{ALL_TOGETHER,GROUP_BY_METADATA,INDIVIDUAL_FILES};

    }

    public interface SummaryModes {
        String ONE_AVERAGE_PER_FILE = "Per input file";
        String AVERAGE_PER_TIMEPOINT = "Per timepoint per input file";

        String[] ALL = new String[]{ONE_AVERAGE_PER_FILE,AVERAGE_PER_TIMEPOINT};

    }


    @Override
    public String getTitle() {
        return "Output control";
    }

    @Override
    public String getPackageName() {
        return "";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(EXPORT_MODE,Parameter.CHOICE_ARRAY,ExportModes.ALL_TOGETHER,ExportModes.ALL));
        parameters.add(new Parameter(METADATA_ITEM_FOR_GROUPING,Parameter.METADATA_ITEM,""));
        parameters.add(new Parameter(EXPORT_SUMMARY,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(SUMMARY_MODE,Parameter.CHOICE_ARRAY,SummaryModes.ONE_AVERAGE_PER_FILE,SummaryModes.ALL));
        parameters.add(new Parameter(SHOW_OBJECT_COUNTS,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(SHOW_NUMBER_OF_CHILDREN,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_COUNT_MEAN,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_COUNT_MIN,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_COUNT_MAX,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_COUNT_STD,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CALCULATE_COUNT_SUM,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(EXPORT_INDIVIDUAL_OBJECTS,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(CONTINUOUS_DATA_EXPORT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SAVE_EVERY_N,Parameter.INTEGER,10));
        parameters.add(new Parameter(SELECT_MEASUREMENTS,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(EXPORT_MODE));
        switch ((String) parameters.getValue(EXPORT_MODE)) {
            case ExportModes.GROUP_BY_METADATA:
                returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_GROUPING));
                break;
        }

        returnedParameters.add(parameters.getParameter(EXPORT_SUMMARY));
        if (parameters.getValue(EXPORT_SUMMARY)) {
            returnedParameters.add(parameters.getParameter(SUMMARY_MODE));
            returnedParameters.add(parameters.getParameter(SHOW_OBJECT_COUNTS));
            returnedParameters.add(parameters.getParameter(SHOW_NUMBER_OF_CHILDREN));

            if (parameters.getValue(SHOW_NUMBER_OF_CHILDREN)) {
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_MEAN));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_MIN));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_MAX));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_STD));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_SUM));
            }
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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}

