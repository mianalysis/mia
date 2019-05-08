package wbif.sjx.MIA.Module.InputOutput;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.MetadataExtractors.*;
import wbif.sjx.common.Object.HCMetadata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends Module {
    public static final String EXTRACTOR_SEPARATOR = "Metadata extractor selection";
    public static final String EXTRACTOR_MODE = "Extractor mode";
    public static final String FILENAME_EXTRACTOR = "Filename extractor";
    public static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";

    public static final String KEYWORD_LIST = "Keyword list";
    public static final String KEYWORD_SOURCE = "Keyword source";
    public static final String METADATA_FILE_EXTRACTOR = "Metadata file extractor";

    public static final String SOURCE_SEPARATOR = "Metadata source";
    public static final String INPUT_SOURCE = "Input source";
    public static final String METADATA_FILE = "Metadata file";
    public static final String METADATA_FILE_NAME = "Metadata file name";
    public static final String METADATA_ITEM_TO_MATCH = "Metadata item to match";
    public static final String REGEX_SPLITTING = "Split using regular expressions";

    public static final String REGEX_SEPARATOR = "Regular expression controls";
    public static final String PATTERN = "Pattern";
    public static final String GROUPS = "Groups (comma separated)";
    public static final String SHOW_TEST = "Show pattern matching test";
    public static final String EXAMPLE_STRING = "Example string";
    public static final String IDENTIFIED_GROUPS = "Identified groups";
    public static final String METADATA_VALUE_NAME = "Metadata value name";


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
        String CSV_FILE = "CSV file";
        String OPERA_METADATA_FILE_EXTRACTOR = "Opera file (.flex)";

        String[] ALL = new String[]{NONE, CSV_FILE, OPERA_METADATA_FILE_EXTRACTOR};

    }

    public interface KeywordSources {
        String FILENAME = "File name";
        String SERIESNAME = "Series name";

        String[] ALL = new String[]{FILENAME,SERIESNAME};

    }

    public interface InputSources {
        String FILE_IN_INPUT_FOLDER = "File in input folder";
        String STATIC_FILE = "Static file";

        String[] ALL = new String[]{FILE_IN_INPUT_FOLDER,STATIC_FILE};

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

    private void extractGeneric(HCMetadata metadata, String input, String pattern, String groupString) {
        String[] groups = getGroups(groupString);

        NameExtractor extractor = new GenericExtractor(pattern,groups);
        extractor.extract(metadata,input);

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

    private String getExternalMetadataRegex(HCMetadata metadata, String inputFilePath, String metadataItemToMatch) {
        // Reading contents of metadata file
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
            String line = "";
            HashMap<String, String> referenceValues = new HashMap<>();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.toString().replace("\uFEFF","");
                String[] split = line.split(",");
                if (split.length == 2) referenceValues.put(split[0], split[1]);
            }
            bufferedReader.close();

            if (metadata.containsKey(metadataItemToMatch)) {
                return referenceValues.get(metadata.get(metadataItemToMatch).toString());
            } else return null;

        } catch (IOException e) {
            return null;
        }
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
            stringBuilder.append(group);
            stringBuilder.append(": ");
            stringBuilder.append(value);
            stringBuilder.append("\n");
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
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting parameters
        String extractorMode = parameters.getValue(EXTRACTOR_MODE);
        String filenameExtractorName = parameters.getValue(FILENAME_EXTRACTOR);
        String foldernameExtractorName = parameters.getValue(FOLDERNAME_EXTRACTOR);
        String metadataFileExtractorName = parameters.getValue(METADATA_FILE_EXTRACTOR);
        String inputSource = parameters.getValue(INPUT_SOURCE);
        String metadataFilePath = parameters.getValue(METADATA_FILE);
        String metadataFileName = parameters.getValue(METADATA_FILE_NAME);
        String metadataItemToMatch = parameters.getValue(METADATA_ITEM_TO_MATCH);
        String pattern = parameters.getValue(PATTERN);
        String groups = parameters.getValue(GROUPS);
        String keywordList = parameters.getValue(KEYWORD_LIST);
        String keywordSource = parameters.getValue(KEYWORD_SOURCE);
        boolean regexSplitting = parameters.getValue(REGEX_SPLITTING);
        String metadataValueName = parameters.getValue(METADATA_VALUE_NAME);

        // Getting current result
        HCMetadata metadata = workspace.getMetadata();

        switch (extractorMode) {
            case ExtractorModes.FILENAME_MODE:
                // Getting filename extractor
                if (filenameExtractorName.equals(FilenameExtractors.GENERIC)) {
                    extractGeneric(metadata,metadata.getFile().getName(),pattern,groups);
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
                switch (metadataFileExtractorName) {
                    case MetadataFileExtractors.CSV_FILE:
                        String metadataSourcePath = null;
                        switch (inputSource) {
                            case InputSources.FILE_IN_INPUT_FOLDER:
                                metadataSourcePath = workspace.getMetadata().getFile().getParentFile().getPath();
                                metadataSourcePath = metadataSourcePath + MIA.getSlashes() + metadataFileName;
                                break;
                            case InputSources.STATIC_FILE:
                                metadataSourcePath = metadataFilePath;
                                break;

                            default:
                                return true;
                        }
                        String metadataString = getExternalMetadataRegex(metadata,metadataSourcePath,metadataItemToMatch);
                        if (metadataString != null) {
                            if (regexSplitting) extractGeneric(metadata,metadataString,pattern,groups);
                            else extractGeneric(metadata,metadataString,"([^s]+)",metadataValueName);
                        }
                        break;

                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        extractMetadataFile(metadata,metadataFileExtractorName);
                        break;
                }
                break;
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(EXTRACTOR_SEPARATOR,this));
        parameters.add(new ChoiceP(EXTRACTOR_MODE,this,ExtractorModes.FILENAME_MODE,ExtractorModes.ALL));

        parameters.add(new ChoiceP(FILENAME_EXTRACTOR, this,FilenameExtractors.GENERIC,FilenameExtractors.ALL));
        parameters.add(new ChoiceP(FOLDERNAME_EXTRACTOR, this,FoldernameExtractors.NONE,FoldernameExtractors.ALL));
        parameters.add(new StringP(KEYWORD_LIST,this));

        parameters.add(new ParamSeparatorP(SOURCE_SEPARATOR,this));
        parameters.add(new ChoiceP(KEYWORD_SOURCE, this,KeywordSources.FILENAME,KeywordSources.ALL));
        parameters.add(new ChoiceP(METADATA_FILE_EXTRACTOR,this,MetadataFileExtractors.NONE,MetadataFileExtractors.ALL));
        parameters.add(new ChoiceP(INPUT_SOURCE,this,InputSources.FILE_IN_INPUT_FOLDER,InputSources.ALL));
        parameters.add(new FilePathP(METADATA_FILE,this));
        parameters.add(new StringP(METADATA_FILE_NAME,this));
        parameters.add(new MetadataItemP(METADATA_ITEM_TO_MATCH,this));

        parameters.add(new ParamSeparatorP(REGEX_SEPARATOR,this));
        parameters.add(new StringP(PATTERN,this));
        parameters.add(new StringP(GROUPS,this));
        parameters.add(new BooleanP(SHOW_TEST,this,false));
        parameters.add(new StringP(EXAMPLE_STRING,this));
        parameters.add(new TextDisplayP(IDENTIFIED_GROUPS,this));
        parameters.add(new BooleanP(REGEX_SPLITTING,this,false));
        parameters.add(new StringP(METADATA_VALUE_NAME,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(EXTRACTOR_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EXTRACTOR_MODE));
        switch((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                returnedParameters.add(parameters.getParameter(FILENAME_EXTRACTOR));
                switch ((String) parameters.getValue(FILENAME_EXTRACTOR)) {
                    case FilenameExtractors.GENERIC:
                        returnedParameters.add(parameters.getParameter(REGEX_SEPARATOR));
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
                            TextDisplayP identifiedGroups = parameters.getParameter(IDENTIFIED_GROUPS);
                            identifiedGroups.setValue(groupsString);

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
                switch ((String) parameters.getValue(METADATA_FILE_EXTRACTOR)) {
                    case MetadataFileExtractors.CSV_FILE:
                        returnedParameters.add(parameters.getParameter(SOURCE_SEPARATOR));
                        returnedParameters.add(parameters.getParameter(INPUT_SOURCE));
                        switch ((String) parameters.getValue(INPUT_SOURCE)) {
                            case InputSources.FILE_IN_INPUT_FOLDER:
                                returnedParameters.add(parameters.getParameter(METADATA_FILE_NAME));
                                break;

                            case InputSources.STATIC_FILE:
                                returnedParameters.add(parameters.getParameter(METADATA_FILE));
                                break;
                        }

                        returnedParameters.add(parameters.getParameter(METADATA_ITEM_TO_MATCH));

                        returnedParameters.add(parameters.getParameter(REGEX_SPLITTING));
                        if (parameters.getValue(REGEX_SPLITTING)) {
                            returnedParameters.add(parameters.getParameter(REGEX_SEPARATOR));
                            returnedParameters.add(parameters.getParameter(PATTERN));
                            returnedParameters.add(parameters.getParameter(GROUPS));

                            returnedParameters.add(parameters.getParameter(SHOW_TEST));
                            if (parameters.getValue(SHOW_TEST)) {
                                returnedParameters.add(parameters.getParameter(EXAMPLE_STRING));
                                returnedParameters.add(parameters.getParameter(IDENTIFIED_GROUPS));

                                String pattern = parameters.getValue(PATTERN);
                                String groups = parameters.getValue(GROUPS);
                                String exampleString = parameters.getValue(EXAMPLE_STRING);
                                String groupsString = getTestString(pattern, groups, exampleString);
                                TextDisplayP identifiedGroups = parameters.getParameter(IDENTIFIED_GROUPS);
                                identifiedGroups.setValue(groupsString);

                            }
                        } else {
                            returnedParameters.add(parameters.getParameter(METADATA_VALUE_NAME));
                        }
                        break;
                }
                break;

        }

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
        switch((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                switch ((String) parameters.getValue(FILENAME_EXTRACTOR)) {
                    case FilenameExtractors.GENERIC:
                        String groupString = parameters.getValue(GROUPS);
                        String[] groups = getGroups(groupString);
                        for (String group:groups) metadataRefs.getOrPut((group));
                        break;

                    case FilenameExtractors.CELLVOYAGER_FILENAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.CHANNEL));
                        metadataRefs.getOrPut((HCMetadata.EXTENSION));
                        metadataRefs.getOrPut((HCMetadata.FIELD));
                        metadataRefs.getOrPut((HCMetadata.TIMEPOINT));
                        metadataRefs.getOrPut((HCMetadata.WELL));
                        metadataRefs.getOrPut((HCMetadata.ZPOSITION));
                        break;

                    case FilenameExtractors.INCUCYTE_LONG_FILENAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.EXTENSION));
                        metadataRefs.getOrPut((HCMetadata.COMMENT));
                        metadataRefs.getOrPut((HCMetadata.WELL));
                        metadataRefs.getOrPut((HCMetadata.FIELD));
                        metadataRefs.getOrPut((HCMetadata.YEAR));
                        metadataRefs.getOrPut((HCMetadata.MONTH));
                        metadataRefs.getOrPut((HCMetadata.DAY));
                        metadataRefs.getOrPut((HCMetadata.HOUR));
                        metadataRefs.getOrPut((HCMetadata.MINUTE));
                        break;

                    case FilenameExtractors.INCUCYTE_SHORT_FILENAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.EXTENSION));
                        metadataRefs.getOrPut((HCMetadata.COMMENT));
                        metadataRefs.getOrPut((HCMetadata.WELL));
                        metadataRefs.getOrPut((HCMetadata.FIELD));
                        break;

                    case FilenameExtractors.OPERA_FILENAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.ROW));
                        metadataRefs.getOrPut((HCMetadata.COL));
                        metadataRefs.getOrPut((HCMetadata.FIELD));
                        metadataRefs.getOrPut((HCMetadata.WELL));
                        break;

                    case FilenameExtractors.YOKOGAWA_FILENAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.EXTENSION));
                        metadataRefs.getOrPut((HCMetadata.PLATE_NAME));
                        metadataRefs.getOrPut((HCMetadata.PLATE_MANUFACTURER));
                        metadataRefs.getOrPut((HCMetadata.PLATE_MODEL));
                        metadataRefs.getOrPut((HCMetadata.WELL));
                        metadataRefs.getOrPut((HCMetadata.TIMEPOINT));
                        metadataRefs.getOrPut((HCMetadata.FIELD));
                        metadataRefs.getOrPut((HCMetadata.TIMELINE_NUMBER));
                        metadataRefs.getOrPut((HCMetadata.ACTION_NUMBER));
                        metadataRefs.getOrPut((HCMetadata.ZPOSITION));
                        metadataRefs.getOrPut((HCMetadata.CHANNEL));
                        break;
                }

                break;

            case ExtractorModes.FOLDERNAME_MODE:
                switch ((String) parameters.getValue(FOLDERNAME_EXTRACTOR)) {
                    case FoldernameExtractors.CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.YEAR));
                        metadataRefs.getOrPut((HCMetadata.MONTH));
                        metadataRefs.getOrPut((HCMetadata.DAY));
                        metadataRefs.getOrPut((HCMetadata.HOUR));
                        metadataRefs.getOrPut((HCMetadata.MINUTE));
                        metadataRefs.getOrPut((HCMetadata.SECOND));
                        metadataRefs.getOrPut((HCMetadata.MAGNIFICATION));
                        metadataRefs.getOrPut((HCMetadata.CELLTYPE));
                        metadataRefs.getOrPut((HCMetadata.COMMENT));
                        break;

                    case FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR:
                        metadataRefs.getOrPut((HCMetadata.YEAR));
                        metadataRefs.getOrPut((HCMetadata.MONTH));
                        metadataRefs.getOrPut((HCMetadata.DAY));
                        metadataRefs.getOrPut((HCMetadata.HOUR));
                        metadataRefs.getOrPut((HCMetadata.MINUTE));
                        metadataRefs.getOrPut((HCMetadata.SECOND));
                        break;

                    case FoldernameExtractors.OPERA_BARCODE_EXTRACTOR:
                        metadataRefs.getOrPut(("Barcode"));
                        break;
                }
                break;

            case ExtractorModes.KEYWORD_MODE:
                metadataRefs.getOrPut((HCMetadata.KEYWORD));
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                switch ((String) parameters.getValue(METADATA_FILE_EXTRACTOR)) {
                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        metadataRefs.getOrPut(("AreaName"));
                        break;

                    case MetadataFileExtractors.CSV_FILE:
                        if (parameters.getValue(REGEX_SPLITTING)) {
                            String groupString = parameters.getValue(GROUPS);
                            String[] groups = getGroups(groupString);
                            for (String group : groups) metadataRefs.getOrPut((group));
                        } else {
                            metadataRefs.getOrPut((parameters.getValue(METADATA_VALUE_NAME)));
                        }
                        break;
                }
                break;

        }

        return metadataRefs;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}


