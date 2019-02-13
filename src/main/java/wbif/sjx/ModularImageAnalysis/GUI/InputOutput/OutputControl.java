package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.MetadataRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

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
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SELECT_MEASUREMENTS = "Show measurement selection";

    public interface ExportModes {
        String ALL_TOGETHER = "All together";
        String GROUP_BY_METADATA = "Group by metadata";
        String INDIVIDUAL_FILES = "Individual files";
        String NONE = "None";

        String[] ALL = new String[]{ALL_TOGETHER,GROUP_BY_METADATA,INDIVIDUAL_FILES,NONE};

    }

    public interface SummaryModes {
        String ONE_AVERAGE_PER_FILE = "Per input file";
        String AVERAGE_PER_TIMEPOINT = "Per timepoint per input file";

        String[] ALL = new String[]{ONE_AVERAGE_PER_FILE,AVERAGE_PER_TIMEPOINT};

    }

    public interface AppendDateTimeModes {
        String ALWAYS = "Always";
        String IF_FILE_EXISTS = "If file exists";
        String NEVER = "Never";

        String[] ALL = new String[]{ALWAYS,IF_FILE_EXISTS, NEVER};

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
    public boolean run(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(EXPORT_MODE,this,ExportModes.ALL_TOGETHER,ExportModes.ALL));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_GROUPING,this));
        parameters.add(new BooleanP(EXPORT_SUMMARY,this,true));
        parameters.add(new ChoiceP(SUMMARY_MODE,this,SummaryModes.ONE_AVERAGE_PER_FILE,SummaryModes.ALL));
        parameters.add(new BooleanP(SHOW_OBJECT_COUNTS,this,true));
        parameters.add(new BooleanP(SHOW_NUMBER_OF_CHILDREN,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_MEAN,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_MIN,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_MAX,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_STD,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_SUM,this,true));
        parameters.add(new BooleanP(EXPORT_INDIVIDUAL_OBJECTS,this,true));
        parameters.add(new BooleanP(CONTINUOUS_DATA_EXPORT,this,false));
        parameters.add(new IntegerP(SAVE_EVERY_N,this,10));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE,this,AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new BooleanP(SELECT_MEASUREMENTS,this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        ChoiceP exportMode = (ChoiceP) parameters.getParameter(EXPORT_MODE);
        returnedParameters.add(exportMode);
        switch (exportMode.getChoice()) {
            case ExportModes.GROUP_BY_METADATA:
                returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_GROUPING));
                break;
        }

        BooleanP exportSummary = (BooleanP) parameters.getParameter(EXPORT_SUMMARY);
        returnedParameters.add(exportSummary);
        if (exportSummary.isSelected()) {
            returnedParameters.add(parameters.getParameter(SUMMARY_MODE));
            returnedParameters.add(parameters.getParameter(SHOW_OBJECT_COUNTS));
            returnedParameters.add(parameters.getParameter(SHOW_NUMBER_OF_CHILDREN));

            BooleanP showNumberOfChildren = (BooleanP) parameters.getParameter(SHOW_NUMBER_OF_CHILDREN);
            returnedParameters.add(showNumberOfChildren);
            if (showNumberOfChildren.isSelected()) {
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_MEAN));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_MIN));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_MAX));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_STD));
                returnedParameters.add(parameters.getParameter(CALCULATE_COUNT_SUM));
            }
        }

        returnedParameters.add(parameters.getParameter(EXPORT_INDIVIDUAL_OBJECTS));

        BooleanP continuousDataExport = (BooleanP) parameters.getParameter(CONTINUOUS_DATA_EXPORT);
        returnedParameters.add(continuousDataExport);
        if (continuousDataExport.isSelected()) {
            returnedParameters.add(parameters.getParameter(SAVE_EVERY_N));
        }

        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
        returnedParameters.add(parameters.getParameter(SELECT_MEASUREMENTS));


        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}

