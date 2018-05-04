package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.*;
import wbif.sjx.common.Object.HCMetadata;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends Module {
    public static final String EXTRACTOR_MODE = "Extractor mode";
    public static final String FILENAME_EXTRACTOR = "Filename extractor";
    public static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";
    public static final String KEYWORD_LIST = "Keyword list";
    public static final String KEYWORD_SOURCE = "Keyword source";
    public static final String METADATA_FILE_EXTRACTOR = "Metadata file extractor";

    public interface ExtractorModes {
        String FILENAME_MODE = "Filename";
        String FOLDERNAME_MODE = "Foldername";
        String KEYWORD_MODE = "Keyword";
        String METADATA_FILE_MODE = "Metadata file";

        String[] ALL = new String[]{FILENAME_MODE, FOLDERNAME_MODE, KEYWORD_MODE, METADATA_FILE_MODE};

    }

    public interface FilenameExtractors {
        String NONE = "None";
        String CELLVOYAGER_FILENAME_EXTRACTOR = "Cell Voyager filename";
        String INCUCYTE_LONG_FILENAME_EXTRACTOR = "IncuCyte long filename";
        String INCUCYTE_SHORT_FILENAME_EXTRACTOR = "IncuCyte short filename";
        String OPERA_FILENAME_EXTRACTOR = "Opera filename";

        String[] ALL = new String[]{NONE, CELLVOYAGER_FILENAME_EXTRACTOR, INCUCYTE_LONG_FILENAME_EXTRACTOR,
                INCUCYTE_SHORT_FILENAME_EXTRACTOR, OPERA_FILENAME_EXTRACTOR};

    }

    public interface FoldernameExtractors {
        String NONE = "None";
        String CELLVOYAGER_FOLDERNAME_EXTRACTOR = "Cell Voyager foldername";
        String OPERA_FOLDERNAME_EXTRACTOR = "Opera foldername";

        String[] ALL = new String[]{NONE, CELLVOYAGER_FOLDERNAME_EXTRACTOR, OPERA_FOLDERNAME_EXTRACTOR};
    }

    public interface MetadataFileExtractors {
        String NONE = "None";
        String OPERA_METADATA_FILE_EXTRACTOR = "Opera file (.flex)";

        String[] ALL = new String[]{NONE, OPERA_METADATA_FILE_EXTRACTOR};

    }

    public interface KeywordSources {
        String FILENAME = "File name";
        String SERIESNAME = "Series name";

        String[] ALL = new String[]{FILENAME,SERIESNAME};

    }


    private void extractFilename(HCMetadata metadata, String filenameExtractorName) {
        NameExtractor filenameExtractor = null;

        switch (filenameExtractorName) {
            case FilenameExtractors.CELLVOYAGER_FILENAME_EXTRACTOR:
                filenameExtractor = new CellVoyagerFilenameExtractor();
                break;

            case FilenameExtractors.INCUCYTE_LONG_FILENAME_EXTRACTOR:
                filenameExtractor = new IncuCyteLongFilenameExtractor();
                break;

            case FilenameExtractors.INCUCYTE_SHORT_FILENAME_EXTRACTOR:
                filenameExtractor = new IncuCyteShortFilenameExtractor();
                break;

            case FilenameExtractors.OPERA_FILENAME_EXTRACTOR:
                filenameExtractor = new OperaFilenameExtractor();
                break;

        }

        if (filenameExtractor != null) filenameExtractor.extract(metadata, metadata.getFile().getName());
    }

    private void extractFoldername(HCMetadata metadata, String foldernameExtractorName) {
        // Getting folder name extractor
        NameExtractor foldernameExtractor = null;
        switch (foldernameExtractorName) {
            case FoldernameExtractors.CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                foldernameExtractor = new CellVoyagerFoldernameExtractor();
                break;

            case FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR:
                foldernameExtractor = new OperaFoldernameExtractor();
                break;
        }

        if (foldernameExtractor != null) foldernameExtractor.extract(metadata,metadata.getFile().getParent());
    }

    private void extractKeyword(HCMetadata metadata, String keywordList, String keywordSource) {
        KeywordExtractor keywordExtractor = new KeywordExtractor(keywordList);

        switch (keywordSource) {
            case KeywordSources.FILENAME:
                keywordExtractor.extract(metadata,metadata.getFile().getName());
                break;
            case KeywordSources.SERIESNAME:
                keywordExtractor.extract(metadata,metadata.getSeriesName());
                break;
        }
    }

    private void extractMetadataFile(HCMetadata metadata, String metadataFileExtractorName) {
        FileExtractor metadataFileExtractor = null;
        switch (metadataFileExtractorName) {
            case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                metadataFileExtractor = new OperaFileExtractor();
                break;
        }

        if (metadataFileExtractor != null) metadataFileExtractor.extract(metadata,metadata.getFile());
    }


    @Override
    public String getTitle() {
        return "Extract metadata";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting current result
        HCMetadata metadata = workspace.getMetadata();

        switch ((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                // Getting filename extractor
                String filenameExtractorName = parameters.getValue(FILENAME_EXTRACTOR);
                extractFilename(metadata,filenameExtractorName);
                break;

            case ExtractorModes.FOLDERNAME_MODE:
                String foldernameExtractorName = parameters.getValue(FOLDERNAME_EXTRACTOR);
                extractFoldername(metadata,foldernameExtractorName);
                break;

            case ExtractorModes.KEYWORD_MODE:
                String keywordList = parameters.getValue(KEYWORD_LIST);
                String keywordSource = parameters.getValue(KEYWORD_SOURCE);
                extractKeyword(metadata,keywordList,keywordSource);
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                // Getting metadata file extractor
                String metadataFileExtractorName = parameters.getValue(METADATA_FILE_EXTRACTOR);
                extractMetadataFile(metadata,metadataFileExtractorName);
                break;
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(EXTRACTOR_MODE,Parameter.CHOICE_ARRAY,ExtractorModes.FILENAME_MODE,ExtractorModes.ALL));
        parameters.add(new Parameter(FILENAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FilenameExtractors.NONE,FilenameExtractors.ALL));
        parameters.add(new Parameter(FOLDERNAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FoldernameExtractors.NONE,FoldernameExtractors.ALL));
        parameters.add(new Parameter(KEYWORD_LIST,Parameter.STRING,""));
        parameters.add(new Parameter(KEYWORD_SOURCE, Parameter.CHOICE_ARRAY,KeywordSources.FILENAME,KeywordSources.ALL));
        parameters.add(new Parameter(METADATA_FILE_EXTRACTOR,Parameter.CHOICE_ARRAY,MetadataFileExtractors.NONE,MetadataFileExtractors.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(EXTRACTOR_MODE));

        switch((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                returnedParameters.add(parameters.getParameter(FILENAME_EXTRACTOR));
                break;

            case ExtractorModes.FOLDERNAME_MODE:
                returnedParameters.add(parameters.getParameter(FOLDERNAME_EXTRACTOR));
                break;

            case ExtractorModes.KEYWORD_MODE:
                returnedParameters.add(parameters.getParameter(KEYWORD_LIST));
                returnedParameters.add(parameters.getParameter(KEYWORD_SOURCE));
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                returnedParameters.add(parameters.getParameter(METADATA_FILE_EXTRACTOR));
                break;

        }

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


