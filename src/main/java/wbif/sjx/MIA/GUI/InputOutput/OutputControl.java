package wbif.sjx.MIA.GUI.InputOutput;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;

/**
 * Created by Stephen on 29/07/2017.
 */
public class OutputControl extends Module {
    public static final String EXPORT_SEPARATOR = "Core export controls";
    public static final String SAVE_LOCATION = "Save location";
    public static final String SAVE_FILE_PATH = "File path";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String EXPORT_MODE = "Export mode";
    public static final String METADATA_ITEM_FOR_GROUPING = "Metadata item for grouping";
    public static final String CONTINUOUS_DATA_EXPORT = "Continuous data export";
    public static final String SAVE_EVERY_N = "Save every n files";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";

    public static final String SUMMARY_SEPARATOR = "Summary sheets";
    public static final String EXPORT_SUMMARY = "Export summary";
    public static final String SUMMARY_MODE = "Summary mode";
    public static final String METADATA_ITEM_FOR_SUMMARY = "Metadata item for summary";
    public static final String SHOW_OBJECT_COUNTS = "Show object counts";
    public static final String SHOW_NUMBER_OF_CHILDREN = "Show number of children";
    public static final String CALCULATE_COUNT_MEAN = "Calculate count means";
    public static final String CALCULATE_COUNT_MIN = "Calculate count minima";
    public static final String CALCULATE_COUNT_MAX = "Calculate count maxima";
    public static final String CALCULATE_COUNT_STD = "Calculate count standard deviations";
    public static final String CALCULATE_COUNT_SUM = "Calculate count sums";
    public static final String EXPORT_INDIVIDUAL_OBJECTS = "Export individual objects";

    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    public static final String SELECT_MEASUREMENTS = "Show measurement selection";


    public interface SaveLocations {
        String SAVE_WITH_INPUT = "Save with input file";
        String SPECIFIC_LOCATION = "Specific location";

        String[] ALL = new String[]{SAVE_WITH_INPUT, SPECIFIC_LOCATION};

    }

    public interface SaveNameModes {
        String MATCH_INPUT = "Match input file/folder name";
        String SPECIFIC_NAME = "Specific name";

        String[] ALL = new String[]{MATCH_INPUT, SPECIFIC_NAME};

    }

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
        String GROUP_BY_METADATA = "Group by metadata";

        String[] ALL = new String[]{ONE_AVERAGE_PER_FILE,AVERAGE_PER_TIMEPOINT,GROUP_BY_METADATA};

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
        return "General";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(EXPORT_SEPARATOR,this));
        parameters.add(new ChoiceP(SAVE_LOCATION, this,SaveLocations.SAVE_WITH_INPUT,SaveLocations.ALL));
        parameters.add(new FolderPathP(SAVE_FILE_PATH,this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this,SaveNameModes.MATCH_INPUT,SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME,this));
        parameters.add(new ChoiceP(EXPORT_MODE,this,ExportModes.ALL_TOGETHER,ExportModes.ALL));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_GROUPING,this));
        parameters.add(new BooleanP(CONTINUOUS_DATA_EXPORT,this,false));
        parameters.add(new IntegerP(SAVE_EVERY_N,this,10));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE,this,AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));

        parameters.add(new ParamSeparatorP(SUMMARY_SEPARATOR,this));
        parameters.add(new BooleanP(EXPORT_SUMMARY,this,true));
        parameters.add(new ChoiceP(SUMMARY_MODE,this,SummaryModes.ONE_AVERAGE_PER_FILE,SummaryModes.ALL));
        parameters.add(new MetadataItemP(METADATA_ITEM_FOR_SUMMARY,this));
        parameters.add(new BooleanP(SHOW_OBJECT_COUNTS,this,true));
        parameters.add(new BooleanP(SHOW_NUMBER_OF_CHILDREN,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_MEAN,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_MIN,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_MAX,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_STD,this,true));
        parameters.add(new BooleanP(CALCULATE_COUNT_SUM,this,true));
        parameters.add(new BooleanP(EXPORT_INDIVIDUAL_OBJECTS,this,true));

        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new BooleanP(SELECT_MEASUREMENTS,this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(EXPORT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SAVE_LOCATION));
        switch ((String) parameters.getValue(SAVE_LOCATION)) {
            case SaveLocations.SPECIFIC_LOCATION:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE)) {
            case SaveNameModes.SPECIFIC_NAME:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
                break;
        }

        ChoiceP exportMode = (ChoiceP) parameters.getParameter(EXPORT_MODE);
        returnedParameters.add(exportMode);
        switch (exportMode.getChoice()) {
            case ExportModes.GROUP_BY_METADATA:
                returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_GROUPING));
                break;
        }

        BooleanP continuousDataExport = (BooleanP) parameters.getParameter(CONTINUOUS_DATA_EXPORT);
        returnedParameters.add(continuousDataExport);
        if (continuousDataExport.isSelected()) {
            returnedParameters.add(parameters.getParameter(SAVE_EVERY_N));
        }

        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));

        returnedParameters.add(parameters.getParameter(SUMMARY_SEPARATOR));
        BooleanP exportSummary = (BooleanP) parameters.getParameter(EXPORT_SUMMARY);
        returnedParameters.add(exportSummary);
        if (exportSummary.isSelected()) {
            returnedParameters.add(parameters.getParameter(SUMMARY_MODE));
            switch ((String) parameters.getValue(SUMMARY_MODE)) {
                case SummaryModes.GROUP_BY_METADATA:
                    returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_SUMMARY));
                    break;
            }

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

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SELECT_MEASUREMENTS));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}

