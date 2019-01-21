package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

/**
 * Created by Stephen on 29/07/2017.
 */
public class InputControl extends Module {
    public static final String INPUT_PATH = "Input path";
    public static final String SIMULTANEOUS_JOBS = "Simultaneous jobs";
    public static final String FILE_EXTENSION = "File extension";
    public static final String SERIES_MODE = "Series mode";
    public static final String SERIES_LIST = "Series list";
    public static final String SERIES_NUMBER = "Series number";
    public static final String USE_SERIESNAME_FILTER_1 = "Use seriesname filter 1";
    public static final String SERIESNAME_FILTER_1 = "Seriesname filter 1";
    public static final String SERIESNAME_FILTER_TYPE_1 = "Seriesname filter type 1";
    public static final String USE_SERIESNAME_FILTER_2 = "Use seriesname filter 2";
    public static final String SERIESNAME_FILTER_2 = "Seriesname filter 2";
    public static final String SERIESNAME_FILTER_TYPE_2 = "Seriesname filter type 2";
    public static final String USE_SERIESNAME_FILTER_3 = "Use seriesname filter 3";
    public static final String SERIESNAME_FILTER_3 = "Seriesname filter 3";
    public static final String SERIESNAME_FILTER_TYPE_3 = "Seriesname filter type 3";
    public static final String USE_FILENAME_FILTER_1 = "Use filename filter 1";
    public static final String FILENAME_FILTER_1 = "Filename filter 1";
    public static final String FILENAME_FILTER_SOURCE_1 = "Filename filter source 1";
    public static final String FILENAME_FILTER_TYPE_1 = "Filter type 1";
    public static final String USE_FILENAME_FILTER_2 = "Use filename filter 2";
    public static final String FILENAME_FILTER_2 = "Filename filter 2";
    public static final String FILENAME_FILTER_SOURCE_2 = "Filename filter source 2";
    public static final String FILENAME_FILTER_TYPE_2 = "Filter type 2";
    public static final String USE_FILENAME_FILTER_3 = "Use filename filter 3";
    public static final String FILENAME_FILTER_3 = "Filename filter 3";
    public static final String FILENAME_FILTER_SOURCE_3 = "Filename filter source 3";
    public static final String FILENAME_FILTER_TYPE_3 = "Filter type 3";
    public static final String SPATIAL_UNITS = "Spatial units";


    public static interface InputModes {
        String SINGLE_FILE = "Single file";
        String BATCH = "Batch";

        String[] ALL = new String[]{BATCH,SINGLE_FILE};

    }

    public static interface SeriesModes {
        String ALL_SERIES = "All series";
        String SERIES_LIST = "Series list (comma separated)";
        String SINGLE_SERIES = "Single series";

        String[] ALL = new String[]{ALL_SERIES,SERIES_LIST,SINGLE_SERIES};

    }

    public static interface FilenameFilterSource {
        String FILENAME = "Filename";
        String FILEPATH = "Filepath";

        String[] ALL = new String[]{FILENAME,FILEPATH};

    }

    public static interface FilterTypes {
        String INCLUDE_MATCHES_PARTIALLY = "Matches partially (include)";
        String INCLUDE_MATCHES_COMPLETELY = "Matches completely (include)";
        String EXCLUDE_MATCHES_PARTIALLY = "Matches partially (exclude)";
        String EXCLUDE_MATCHES_COMPLETELY = "Matches completely (exclude)";

        String[] ALL = new String[]{INCLUDE_MATCHES_PARTIALLY,INCLUDE_MATCHES_COMPLETELY,EXCLUDE_MATCHES_PARTIALLY,EXCLUDE_MATCHES_COMPLETELY};

    }

    public static interface SpatialUnits extends Units.SpatialUnits{}


    @Override
    public String getTitle() {
        return "Input control";
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
    public void initialiseParameters() {
        parameters.add(new FileFolderPathP(INPUT_PATH,this));
        parameters.add(new IntegerP(SIMULTANEOUS_JOBS,this,1));
        parameters.add(new StringP(FILE_EXTENSION,this,"tif"));
        parameters.add(new ChoiceP(SERIES_MODE,this,SeriesModes.ALL_SERIES,SeriesModes.ALL));
        parameters.add(new StringP(SERIES_LIST,this,"1"));
        parameters.add(new IntegerP(SERIES_NUMBER,this,1));
        parameters.add(new BooleanP(USE_SERIESNAME_FILTER_1,this,false));
        parameters.add(new StringP(SERIESNAME_FILTER_1,this));
        parameters.add(new ChoiceP(SERIESNAME_FILTER_TYPE_1,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanP(USE_SERIESNAME_FILTER_2,this,false));
        parameters.add(new StringP(SERIESNAME_FILTER_2,this));
        parameters.add(new ChoiceP(SERIESNAME_FILTER_TYPE_2,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanP(USE_SERIESNAME_FILTER_3,this,false));
        parameters.add(new StringP(SERIESNAME_FILTER_3,this));
        parameters.add(new ChoiceP(SERIESNAME_FILTER_TYPE_3,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanP(USE_FILENAME_FILTER_1,this,false));
        parameters.add(new StringP(FILENAME_FILTER_1,this));
        parameters.add(new ChoiceP(FILENAME_FILTER_SOURCE_1,this,FilenameFilterSource.FILENAME,FilenameFilterSource.ALL));
        parameters.add(new ChoiceP(FILENAME_FILTER_TYPE_1,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanP(USE_FILENAME_FILTER_2,this,false));
        parameters.add(new StringP(FILENAME_FILTER_2,this));
        parameters.add(new ChoiceP(FILENAME_FILTER_SOURCE_2,this,FilenameFilterSource.FILENAME,FilenameFilterSource.ALL));
        parameters.add(new ChoiceP(FILENAME_FILTER_TYPE_2,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanP(USE_FILENAME_FILTER_3,this,false));
        parameters.add(new StringP(FILENAME_FILTER_3,this));
        parameters.add(new ChoiceP(FILENAME_FILTER_SOURCE_3,this,FilenameFilterSource.FILENAME,FilenameFilterSource.ALL));
        parameters.add(new ChoiceP(FILENAME_FILTER_TYPE_3,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new ChoiceP(SPATIAL_UNITS,this,SpatialUnits.MICROMETRE,SpatialUnits.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        FileFolderPathP inputPath = (FileFolderPathP) parameters.getParameter(INPUT_PATH);
        returnedParameters.add(inputPath);
        if (inputPath.getPath() != null && inputPath.isDirectory()) {
            returnedParameters.add(parameters.getParameter(FILE_EXTENSION));
        }

        ChoiceP seriesMode = (ChoiceP) parameters.getParameter(SERIES_MODE);
        returnedParameters.add(seriesMode);
        switch (seriesMode.getChoice()) {
            case SeriesModes.ALL_SERIES:
                BooleanP useSeriesNameFilter1 = (BooleanP) parameters.getParameter(USE_SERIESNAME_FILTER_1);
                returnedParameters.add(useSeriesNameFilter1);
                if (useSeriesNameFilter1.isSelected()) {
                    returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_1));
                    returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_TYPE_1));
                }

                BooleanP useSeriesNameFilter2 = (BooleanP) parameters.getParameter(USE_SERIESNAME_FILTER_2);
                returnedParameters.add(useSeriesNameFilter2);
                if (useSeriesNameFilter2.isSelected()) {
                    returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_2));
                    returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_TYPE_2));
                }

                BooleanP useSeriesNameFilter3 = (BooleanP) parameters.getParameter(USE_SERIESNAME_FILTER_3);
                returnedParameters.add(useSeriesNameFilter3);
                if (useSeriesNameFilter3.isSelected()) {
                    returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_3));
                    returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_TYPE_3));
                }
                break;
            case SeriesModes.SERIES_LIST:
                returnedParameters.add(parameters.getParameter(SERIES_LIST));
                break;
            case SeriesModes.SINGLE_SERIES:
                returnedParameters.add(parameters.getParameter(SERIES_NUMBER));
                break;
        }

        BooleanP useFilenameFilter1 = (BooleanP) parameters.getParameter(USE_FILENAME_FILTER_1);
        returnedParameters.add(useFilenameFilter1);
        if (useFilenameFilter1.isSelected()) {
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_1));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_SOURCE_1));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_TYPE_1));
        }

        BooleanP useFilenameFilter2 = (BooleanP) parameters.getParameter(USE_FILENAME_FILTER_2);
        returnedParameters.add(useFilenameFilter2);
        if (useFilenameFilter2.isSelected()) {
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_2));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_SOURCE_2));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_TYPE_2));
        }

        BooleanP useFilenameFilter3 = (BooleanP) parameters.getParameter(USE_FILENAME_FILTER_3);
        returnedParameters.add(useFilenameFilter3);
        if (useFilenameFilter3.isSelected()) {
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_3));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_SOURCE_3));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_TYPE_3));
        }

        returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
        returnedParameters.add(parameters.getParameter(SIMULTANEOUS_JOBS));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
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
