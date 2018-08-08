package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by steph on 29/07/2017.
 */
public class InputControl extends Module {
    public static final String INPUT_MODE = "Input mode";
    public static final String SINGLE_FILE_PATH = "Single file path";
    public static final String BATCH_FOLDER_PATH = "Batch folder path";
    public static final String NUMBER_OF_THREADS = "Number of CPU threads";
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
    public static final String FILENAME_FILTER_TYPE_1 = "Filter type 1";
    public static final String USE_FILENAME_FILTER_2 = "Use filename filter 2";
    public static final String FILENAME_FILTER_2 = "Filename filter 2";
    public static final String FILENAME_FILTER_TYPE_2 = "Filter type 2";
    public static final String USE_FILENAME_FILTER_3 = "Use filename filter 3";
    public static final String FILENAME_FILTER_3 = "Filename filter 3";
    public static final String FILENAME_FILTER_TYPE_3 = "Filter type 3";
    public static final String SPATIAL_UNITS = "Spatial units";

    public interface InputModes {
        String SINGLE_FILE = "Single file";
        String BATCH = "Batch";

        String[] ALL = new String[]{BATCH,SINGLE_FILE};

    }

    public interface SeriesModes {
        String ALL_SERIES = "All series";
        String SERIES_LIST = "Series list (comma separated)";
        String SINGLE_SERIES = "Single series";

        String[] ALL = new String[]{ALL_SERIES,SERIES_LIST,SINGLE_SERIES};

    }

    public interface FilterTypes {
        String INCLUDE_MATCHES_PARTIALLY = "Matches partially (include)";
        String INCLUDE_MATCHES_COMPLETELY = "Matches completely (include)";
        String EXCLUDE_MATCHES_PARTIALLY = "Matches partially (exclude)";
        String EXCLUDE_MATCHES_COMPLETELY = "Matches completely (exclude)";

        String[] ALL = new String[]{INCLUDE_MATCHES_PARTIALLY,INCLUDE_MATCHES_COMPLETELY,EXCLUDE_MATCHES_PARTIALLY,EXCLUDE_MATCHES_COMPLETELY};

    }

    public interface SpatialUnits extends Units.SpatialUnits{}

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
    public void run(Workspace workspace) throws GenericMIAException {

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_MODE, Parameter.CHOICE_ARRAY,InputModes.SINGLE_FILE,InputModes.ALL));
        parameters.add(new Parameter(SINGLE_FILE_PATH, Parameter.FILE_PATH,null));
        parameters.add(new Parameter(BATCH_FOLDER_PATH, Parameter.FOLDER_PATH,null));
        int nThreads = Runtime.getRuntime().availableProcessors()/2;
        parameters.add(new Parameter(NUMBER_OF_THREADS,Parameter.INTEGER,nThreads));
        parameters.add(new Parameter(FILE_EXTENSION, Parameter.STRING,"tif"));
        parameters.add(new Parameter(SERIES_MODE,Parameter.CHOICE_ARRAY,SeriesModes.ALL_SERIES,SeriesModes.ALL));
        parameters.add(new Parameter(SERIES_LIST,Parameter.STRING,"1"));
        parameters.add(new Parameter(SERIES_NUMBER,Parameter.INTEGER,1));
        parameters.add(new Parameter(USE_SERIESNAME_FILTER_1,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SERIESNAME_FILTER_1,Parameter.STRING,""));
        parameters.add(new Parameter(SERIESNAME_FILTER_TYPE_1,Parameter.CHOICE_ARRAY,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new Parameter(USE_SERIESNAME_FILTER_2,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SERIESNAME_FILTER_2,Parameter.STRING,""));
        parameters.add(new Parameter(SERIESNAME_FILTER_TYPE_2,Parameter.CHOICE_ARRAY,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new Parameter(USE_SERIESNAME_FILTER_3,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SERIESNAME_FILTER_3,Parameter.STRING,""));
        parameters.add(new Parameter(SERIESNAME_FILTER_TYPE_3,Parameter.CHOICE_ARRAY,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new Parameter(USE_FILENAME_FILTER_1,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(FILENAME_FILTER_1,Parameter.STRING,""));
        parameters.add(new Parameter(FILENAME_FILTER_TYPE_1,Parameter.CHOICE_ARRAY,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new Parameter(USE_FILENAME_FILTER_2,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(FILENAME_FILTER_2,Parameter.STRING,""));
        parameters.add(new Parameter(FILENAME_FILTER_TYPE_2,Parameter.CHOICE_ARRAY,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new Parameter(USE_FILENAME_FILTER_3,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(FILENAME_FILTER_3,Parameter.STRING,""));
        parameters.add(new Parameter(FILENAME_FILTER_TYPE_3,Parameter.CHOICE_ARRAY,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new Parameter(SPATIAL_UNITS,Parameter.CHOICE_ARRAY,SpatialUnits.MICROMETRE,SpatialUnits.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_MODE));

        switch ((String) parameters.getValue(INPUT_MODE)) {
            case InputModes.SINGLE_FILE:
                returnedParameters.add(parameters.getParameter(SINGLE_FILE_PATH));
                break;

            case InputModes.BATCH:
                returnedParameters.add(parameters.getParameter(BATCH_FOLDER_PATH));
                returnedParameters.add(parameters.getParameter(FILE_EXTENSION));
                break;
        }

        returnedParameters.add(parameters.getParameter(SERIES_MODE));
        switch ((String) parameters.getValue(SERIES_MODE)) {
            case SeriesModes.SERIES_LIST:
                returnedParameters.add(parameters.getParameter(SERIES_LIST));
                break;
            case SeriesModes.SINGLE_SERIES:
                returnedParameters.add(parameters.getParameter(SERIES_NUMBER));
                break;
        }

        returnedParameters.add(parameters.getParameter(USE_SERIESNAME_FILTER_1));
        if (parameters.getValue(USE_SERIESNAME_FILTER_1)) {
            returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_1));
            returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_TYPE_1));
        }

        returnedParameters.add(parameters.getParameter(USE_SERIESNAME_FILTER_2));
        if (parameters.getValue(USE_SERIESNAME_FILTER_2)) {
            returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_2));
            returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_TYPE_2));
        }

        returnedParameters.add(parameters.getParameter(USE_SERIESNAME_FILTER_3));
        if (parameters.getValue(USE_SERIESNAME_FILTER_3)) {
            returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_3));
            returnedParameters.add(parameters.getParameter(SERIESNAME_FILTER_TYPE_3));
        }

        returnedParameters.add(parameters.getParameter(USE_FILENAME_FILTER_1));
        if (returnedParameters.getValue(USE_FILENAME_FILTER_1)) {
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_1));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_TYPE_1));
        }

        returnedParameters.add(parameters.getParameter(USE_FILENAME_FILTER_2));
        if (returnedParameters.getValue(USE_FILENAME_FILTER_2)) {
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_2));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_TYPE_2));
        }

        returnedParameters.add(parameters.getParameter(USE_FILENAME_FILTER_3));
        if (returnedParameters.getValue(USE_FILENAME_FILTER_3)) {
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_3));
            returnedParameters.add(parameters.getParameter(FILENAME_FILTER_TYPE_3));
        }

        returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_THREADS));

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
