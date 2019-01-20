package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

/**
 * Created by Stephen on 29/07/2017.
 */
public class InputControl extends Module {
    public static interface Parameters {
         String INPUT_PATH = "Input path";
         String SIMULTANEOUS_JOBS = "Simultaneous jobs";
         String FILE_EXTENSION = "File extension";
         String SERIES_MODE = "Series mode";
         String SERIES_LIST = "Series list";
         String SERIES_NUMBER = "Series number";
         String USE_SERIESNAME_FILTER_1 = "Use seriesname filter 1";
         String SERIESNAME_FILTER_1 = "Seriesname filter 1";
         String SERIESNAME_FILTER_TYPE_1 = "Seriesname filter type 1";
         String USE_SERIESNAME_FILTER_2 = "Use seriesname filter 2";
         String SERIESNAME_FILTER_2 = "Seriesname filter 2";
         String SERIESNAME_FILTER_TYPE_2 = "Seriesname filter type 2";
         String USE_SERIESNAME_FILTER_3 = "Use seriesname filter 3";
         String SERIESNAME_FILTER_3 = "Seriesname filter 3";
         String SERIESNAME_FILTER_TYPE_3 = "Seriesname filter type 3";
         String USE_FILENAME_FILTER_1 = "Use filename filter 1";
         String FILENAME_FILTER_1 = "Filename filter 1";
         String FILENAME_FILTER_SOURCE_1 = "Filename filter source 1";
         String FILENAME_FILTER_TYPE_1 = "Filter type 1";
         String USE_FILENAME_FILTER_2 = "Use filename filter 2";
         String FILENAME_FILTER_2 = "Filename filter 2";
         String FILENAME_FILTER_SOURCE_2 = "Filename filter source 2";
         String FILENAME_FILTER_TYPE_2 = "Filter type 2";
         String USE_FILENAME_FILTER_3 = "Use filename filter 3";
         String FILENAME_FILTER_3 = "Filename filter 3";
         String FILENAME_FILTER_SOURCE_3 = "Filename filter source 3";
         String FILENAME_FILTER_TYPE_3 = "Filter type 3";
         String SPATIAL_UNITS = "Spatial units";

    }

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
        parameters.add(new FileFolderPathParam(Parameters.INPUT_PATH,this,null));
        parameters.add(new Num<Integer>(Parameters.SIMULTANEOUS_JOBS,this,1));
        parameters.add(new Text(Parameters.FILE_EXTENSION,this,"tif"));
        parameters.add(new ChoiceParam(Parameters.SERIES_MODE,this,SeriesModes.ALL_SERIES,SeriesModes.ALL));
        parameters.add(new Text(Parameters.SERIES_LIST,this,"1"));
        parameters.add(new Num<Integer>(Parameters.SERIES_NUMBER,this,1));
        parameters.add(new BooleanParam(Parameters.USE_SERIESNAME_FILTER_1,this,false));
        parameters.add(new Text(Parameters.SERIESNAME_FILTER_1,this,""));
        parameters.add(new ChoiceParam(Parameters.SERIESNAME_FILTER_TYPE_1,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanParam(Parameters.USE_SERIESNAME_FILTER_2,this,false));
        parameters.add(new Text(Parameters.SERIESNAME_FILTER_2,this,""));
        parameters.add(new ChoiceParam(Parameters.SERIESNAME_FILTER_TYPE_2,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanParam(Parameters.USE_SERIESNAME_FILTER_3,this,false));
        parameters.add(new Text(Parameters.SERIESNAME_FILTER_3,this,""));
        parameters.add(new ChoiceParam(Parameters.SERIESNAME_FILTER_TYPE_3,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanParam(Parameters.USE_FILENAME_FILTER_1,this,false));
        parameters.add(new Text(Parameters.FILENAME_FILTER_1,this,""));
        parameters.add(new ChoiceParam(Parameters.FILENAME_FILTER_SOURCE_1,this,FilenameFilterSource.FILENAME,FilenameFilterSource.ALL));
        parameters.add(new ChoiceParam(Parameters.FILENAME_FILTER_TYPE_1,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanParam(Parameters.USE_FILENAME_FILTER_2,this,false));
        parameters.add(new Text(Parameters.FILENAME_FILTER_2,this,""));
        parameters.add(new ChoiceParam(Parameters.FILENAME_FILTER_SOURCE_2,this,FilenameFilterSource.FILENAME,FilenameFilterSource.ALL));
        parameters.add(new ChoiceParam(Parameters.FILENAME_FILTER_TYPE_2,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new BooleanParam(Parameters.USE_FILENAME_FILTER_3,this,false));
        parameters.add(new Text(Parameters.FILENAME_FILTER_3,this,""));
        parameters.add(new ChoiceParam(Parameters.FILENAME_FILTER_SOURCE_3,this,FilenameFilterSource.FILENAME,FilenameFilterSource.ALL));
        parameters.add(new ChoiceParam(Parameters.FILENAME_FILTER_TYPE_3,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new ChoiceParam(Parameters.SPATIAL_UNITS,this,SpatialUnits.MICROMETRE,SpatialUnits.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        FileFolderPathParam inputPath = (FileFolderPathParam) parameters.getParameter(Parameters.INPUT_PATH);
        returnedParameters.add(inputPath);
        if (inputPath.getFileFolderPath() != null && inputPath.isDirectory()) {
            returnedParameters.add(parameters.getParameter(Parameters.FILE_EXTENSION));
        }

        ChoiceParam seriesMode = (ChoiceParam) parameters.getParameter(Parameters.SERIES_MODE);
        returnedParameters.add(seriesMode);
        switch (seriesMode.getChoice()) {
            case SeriesModes.ALL_SERIES:
                BooleanParam useSeriesNameFilter1 = (BooleanParam) parameters.getParameter(Parameters.USE_SERIESNAME_FILTER_1);
                returnedParameters.add(useSeriesNameFilter1);
                if (useSeriesNameFilter1.isSelected()) {
                    returnedParameters.add(parameters.getParameter(Parameters.SERIESNAME_FILTER_1));
                    returnedParameters.add(parameters.getParameter(Parameters.SERIESNAME_FILTER_TYPE_1));
                }

                BooleanParam useSeriesNameFilter2 = (BooleanParam) parameters.getParameter(Parameters.USE_SERIESNAME_FILTER_2);
                returnedParameters.add(useSeriesNameFilter2);
                if (useSeriesNameFilter2.isSelected()) {
                    returnedParameters.add(parameters.getParameter(Parameters.SERIESNAME_FILTER_2));
                    returnedParameters.add(parameters.getParameter(Parameters.SERIESNAME_FILTER_TYPE_2));
                }

                BooleanParam useSeriesNameFilter3 = (BooleanParam) parameters.getParameter(Parameters.USE_SERIESNAME_FILTER_3);
                returnedParameters.add(useSeriesNameFilter3);
                if (useSeriesNameFilter3.isSelected()) {
                    returnedParameters.add(parameters.getParameter(Parameters.SERIESNAME_FILTER_3));
                    returnedParameters.add(parameters.getParameter(Parameters.SERIESNAME_FILTER_TYPE_3));
                }
                break;
            case SeriesModes.SERIES_LIST:
                returnedParameters.add(parameters.getParameter(Parameters.SERIES_LIST));
                break;
            case SeriesModes.SINGLE_SERIES:
                returnedParameters.add(parameters.getParameter(Parameters.SERIES_NUMBER));
                break;
        }

        BooleanParam useFilenameFilter1 = (BooleanParam) parameters.getParameter(Parameters.USE_FILENAME_FILTER_1);
        returnedParameters.add(useFilenameFilter1);
        if (useFilenameFilter1.isSelected()) {
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_1));
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_SOURCE_1));
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_TYPE_1));
        }

        BooleanParam useFilenameFilter2 = (BooleanParam) parameters.getParameter(Parameters.USE_FILENAME_FILTER_2);
        returnedParameters.add(useFilenameFilter2);
        if (useFilenameFilter2.isSelected()) {
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_2));
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_SOURCE_2));
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_TYPE_2));
        }

        BooleanParam useFilenameFilter3 = (BooleanParam) parameters.getParameter(Parameters.USE_FILENAME_FILTER_3);
        returnedParameters.add(useFilenameFilter3);
        if (useFilenameFilter3.isSelected()) {
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_3));
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_SOURCE_3));
            returnedParameters.add(parameters.getParameter(Parameters.FILENAME_FILTER_TYPE_3));
        }

        returnedParameters.add(parameters.getParameter(Parameters.SPATIAL_UNITS));
        returnedParameters.add(parameters.getParameter(Parameters.SIMULTANEOUS_JOBS));

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
