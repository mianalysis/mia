package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.*;
import wbif.sjx.common.Object.HCMetadata;

import java.util.StringTokenizer;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends Module {
    public static final String EXTRACTOR_MODE = "Extractor mode";
    public static final String FILENAME_EXTRACTOR = "Filename extractor";
    public static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";
    public static final String PATTERN = "Pattern";
    public static final String GROUPS = "Groups (comma separated)";
    public static final String SHOW_TEST = "Show pattern matching test";
    public static final String EXAMPLE_STRING = "Example string";
    public static final String IDENTIFIED_GROUPS = "Identified groups";
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
        String GENERIC = "Generic";
        String CELLVOYAGER_FILENAME_EXTRACTOR = "Cell Voyager filename";
        String INCUCYTE_LONG_FILENAME_EXTRACTOR = "IncuCyte long filename";
        String INCUCYTE_SHORT_FILENAME_EXTRACTOR = "IncuCyte short filename";
        String OPERA_FILENAME_EXTRACTOR = "Opera filename";
        String YOKOGAWA_FILENAME_EXTRACTOR = "Yokogawa filename";

        String[] ALL = new String[]{GENERIC, CELLVOYAGER_FILENAME_EXTRACTOR, INCUCYTE_LONG_FILENAME_EXTRACTOR,
                INCUCYTE_SHORT_FILENAME_EXTRACTOR, OPERA_FILENAME_EXTRACTOR, YOKOGAWA_FILENAME_EXTRACTOR};

    }

    public interface FoldernameExtractors {
        String NONE = "None";
        String CELLVOYAGER_FOLDERNAME_EXTRACTOR = "Cell Voyager foldername";
        String OPERA_FOLDERNAME_EXTRACTOR = "Opera measurement foldername";
        String OPERA_BARCODE_EXTRACTOR = "Opera barcode";

        String[] ALL = new String[]{NONE, CELLVOYAGER_FOLDERNAME_EXTRACTOR, OPERA_FOLDERNAME_EXTRACTOR,
                OPERA_BARCODE_EXTRACTOR};
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

            case FilenameExtractors.YOKOGAWA_FILENAME_EXTRACTOR:
                filenameExtractor = new CV7000FilenameExtractor();
                break;

            default:
                return;

        }

        filenameExtractor.extract(metadata, metadata.getFile().getName());

    }

    private void extractGenericFilename(HCMetadata metadata, String pattern, String groupString) {
        String[] groups = getGroups(groupString);

        NameExtractor extractor = new GenericExtractor(pattern,groups);
        extractor.extract(metadata,metadata.getFile().getName());

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

            case FoldernameExtractors.OPERA_BARCODE_EXTRACTOR:
                metadata.put("Barcode",metadata.getFile().getParentFile().getParentFile().getName());
                return;
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

    public String[] getGroups(String groupString) {
        groupString = groupString.replace(" ","");

        StringTokenizer tokenizer = new StringTokenizer(groupString,",");
        int nTokens = tokenizer.countTokens();

        int i=0;
        String[] groups = new String[nTokens];
        while (tokenizer.hasMoreTokens()) groups[i++] = tokenizer.nextToken();

        return groups;

    }

    public String getTestString(String pattern, String groupString, String exampleString) {
        String[] groups = getGroups(groupString);

        HCMetadata metadata = new HCMetadata();
        NameExtractor extractor = new GenericExtractor(pattern,groups);
        extractor.extract(metadata,exampleString);

        StringBuilder stringBuilder = new StringBuilder();
        for (String group:groups) {
            String value = metadata.getAsString(group);
            if (value == null) value = "NA";
            stringBuilder.append("Group \""+group+"\": "+value+"\n");
        }

        return stringBuilder.toString();

    }


    @Override
    public String getTitle() {
        return "Extract metadata";

    }

    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting parameters
        String extractorMode = parameters.getValue(EXTRACTOR_MODE);
        String filenameExtractorName = parameters.getValue(FILENAME_EXTRACTOR);
        String foldernameExtractorName = parameters.getValue(FOLDERNAME_EXTRACTOR);
        String pattern = parameters.getValue(PATTERN);
        String groups = parameters.getValue(GROUPS);
        String keywordList = parameters.getValue(KEYWORD_LIST);
        String keywordSource = parameters.getValue(KEYWORD_SOURCE);
        String metadataFileExtractorName = parameters.getValue(METADATA_FILE_EXTRACTOR);

        // Getting current result
        HCMetadata metadata = workspace.getMetadata();

        switch (extractorMode) {
            case ExtractorModes.FILENAME_MODE:
                // Getting filename extractor
                if (filenameExtractorName.equals(FilenameExtractors.GENERIC)) {
                    extractGenericFilename(metadata,pattern,groups);
                } else {
                    extractFilename(metadata, filenameExtractorName);
                }
                break;

            case ExtractorModes.FOLDERNAME_MODE:
                extractFoldername(metadata,foldernameExtractorName);
                break;

            case ExtractorModes.KEYWORD_MODE:
                extractKeyword(metadata,keywordList,keywordSource);
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                // Getting metadata file extractor
                extractMetadataFile(metadata,metadataFileExtractorName);
                break;
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(EXTRACTOR_MODE,Parameter.CHOICE_ARRAY,ExtractorModes.FILENAME_MODE,ExtractorModes.ALL));
        parameters.add(new Parameter(FILENAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FilenameExtractors.GENERIC,FilenameExtractors.ALL));
        parameters.add(new Parameter(FOLDERNAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FoldernameExtractors.NONE,FoldernameExtractors.ALL));
        parameters.add(new Parameter(PATTERN,Parameter.STRING,""));
        parameters.add(new Parameter(GROUPS,Parameter.STRING,""));
        parameters.add(new Parameter(SHOW_TEST,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(EXAMPLE_STRING,Parameter.STRING,""));
        parameters.add(new Parameter(IDENTIFIED_GROUPS,Parameter.TEXT_DISPLAY,""));
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
                switch ((String) parameters.getValue(FILENAME_EXTRACTOR)) {
                    case FilenameExtractors.GENERIC:
                        returnedParameters.add(parameters.getParameter(PATTERN));
                        returnedParameters.add(parameters.getParameter(GROUPS));

                        returnedParameters.add(parameters.getParameter(SHOW_TEST));
                        if (parameters.getValue(SHOW_TEST)) {
                            returnedParameters.add(parameters.getParameter(EXAMPLE_STRING));
                            returnedParameters.add(parameters.getParameter(IDENTIFIED_GROUPS));

                            String pattern = parameters.getValue(PATTERN);
                            String groups = parameters.getValue(GROUPS);
                            String exampleString = parameters.getValue(EXAMPLE_STRING);
                            String groupsString = getTestString(pattern,groups,exampleString);
                            parameters.updateValue(IDENTIFIED_GROUPS,groupsString);

                        }
                        break;
                }
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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        MetadataReferenceCollection metadataReferences = new MetadataReferenceCollection();

        switch((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                switch ((String) parameters.getValue(FILENAME_EXTRACTOR)) {
                    case FilenameExtractors.GENERIC:
                        String groupString = parameters.getValue(GROUPS);
                        String[] groups = getGroups(groupString);
                        for (String group:groups) metadataReferences.add(new MetadataReference(group));
                        break;

                    case FilenameExtractors.CELLVOYAGER_FILENAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.CHANNEL));
                        metadataReferences.add(new MetadataReference(HCMetadata.EXTENSION));
                        metadataReferences.add(new MetadataReference(HCMetadata.FIELD));
                        metadataReferences.add(new MetadataReference(HCMetadata.TIMEPOINT));
                        metadataReferences.add(new MetadataReference(HCMetadata.WELL));
                        metadataReferences.add(new MetadataReference(HCMetadata.ZPOSITION));
                        break;

                    case FilenameExtractors.INCUCYTE_LONG_FILENAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.EXTENSION));
                        metadataReferences.add(new MetadataReference(HCMetadata.COMMENT));
                        metadataReferences.add(new MetadataReference(HCMetadata.WELL));
                        metadataReferences.add(new MetadataReference(HCMetadata.FIELD));
                        metadataReferences.add(new MetadataReference(HCMetadata.YEAR));
                        metadataReferences.add(new MetadataReference(HCMetadata.MONTH));
                        metadataReferences.add(new MetadataReference(HCMetadata.DAY));
                        metadataReferences.add(new MetadataReference(HCMetadata.HOUR));
                        metadataReferences.add(new MetadataReference(HCMetadata.MINUTE));
                        break;

                    case FilenameExtractors.INCUCYTE_SHORT_FILENAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.EXTENSION));
                        metadataReferences.add(new MetadataReference(HCMetadata.COMMENT));
                        metadataReferences.add(new MetadataReference(HCMetadata.WELL));
                        metadataReferences.add(new MetadataReference(HCMetadata.FIELD));
                        break;

                    case FilenameExtractors.OPERA_FILENAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.ROW));
                        metadataReferences.add(new MetadataReference(HCMetadata.COL));
                        metadataReferences.add(new MetadataReference(HCMetadata.FIELD));
                        metadataReferences.add(new MetadataReference(HCMetadata.WELL));
                        break;

                    case FilenameExtractors.YOKOGAWA_FILENAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.EXTENSION));
                        metadataReferences.add(new MetadataReference(HCMetadata.PLATE_NAME));
                        metadataReferences.add(new MetadataReference(HCMetadata.PLATE_MANUFACTURER));
                        metadataReferences.add(new MetadataReference(HCMetadata.PLATE_MODEL));
                        metadataReferences.add(new MetadataReference(HCMetadata.WELL));
                        metadataReferences.add(new MetadataReference(HCMetadata.TIMEPOINT));
                        metadataReferences.add(new MetadataReference(HCMetadata.FIELD));
                        metadataReferences.add(new MetadataReference(HCMetadata.TIMELINE_NUMBER));
                        metadataReferences.add(new MetadataReference(HCMetadata.ACTION_NUMBER));
                        metadataReferences.add(new MetadataReference(HCMetadata.ZPOSITION));
                        metadataReferences.add(new MetadataReference(HCMetadata.CHANNEL));
                        break;
                }

                break;

            case ExtractorModes.FOLDERNAME_MODE:
                switch ((String) parameters.getValue(FOLDERNAME_EXTRACTOR)) {
                    case FoldernameExtractors.CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.YEAR));
                        metadataReferences.add(new MetadataReference(HCMetadata.MONTH));
                        metadataReferences.add(new MetadataReference(HCMetadata.DAY));
                        metadataReferences.add(new MetadataReference(HCMetadata.HOUR));
                        metadataReferences.add(new MetadataReference(HCMetadata.MINUTE));
                        metadataReferences.add(new MetadataReference(HCMetadata.SECOND));
                        metadataReferences.add(new MetadataReference(HCMetadata.MAGNIFICATION));
                        metadataReferences.add(new MetadataReference(HCMetadata.CELLTYPE));
                        metadataReferences.add(new MetadataReference(HCMetadata.COMMENT));
                        break;

                    case FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR:
                        metadataReferences.add(new MetadataReference(HCMetadata.YEAR));
                        metadataReferences.add(new MetadataReference(HCMetadata.MONTH));
                        metadataReferences.add(new MetadataReference(HCMetadata.DAY));
                        metadataReferences.add(new MetadataReference(HCMetadata.HOUR));
                        metadataReferences.add(new MetadataReference(HCMetadata.MINUTE));
                        metadataReferences.add(new MetadataReference(HCMetadata.SECOND));
                        break;

                    case FoldernameExtractors.OPERA_BARCODE_EXTRACTOR:
                        metadataReferences.add(new MetadataReference("Barcode"));
                        break;
                }
                break;

            case ExtractorModes.KEYWORD_MODE:
                metadataReferences.add(new MetadataReference(HCMetadata.KEYWORD));
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                switch ((String) parameters.getValue(METADATA_FILE_EXTRACTOR)) {
                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        metadataReferences.add(new MetadataReference("AreaName"));
                        break;
                }
                break;

        }

        return metadataReferences;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}


