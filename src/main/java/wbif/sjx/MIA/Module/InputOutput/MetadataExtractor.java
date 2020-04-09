package wbif.sjx.MIA.Module.InputOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.MetadataItemP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.RefreshButtonP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.MetadataExtractors.CV7000FilenameExtractor;
import wbif.sjx.common.MetadataExtractors.CellVoyagerFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.CellVoyagerFoldernameExtractor;
import wbif.sjx.common.MetadataExtractors.FileExtractor;
import wbif.sjx.common.MetadataExtractors.GenericExtractor;
import wbif.sjx.common.MetadataExtractors.IncuCyteLongFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.IncuCyteShortFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.KeywordExtractor;
import wbif.sjx.common.MetadataExtractors.NameExtractor;
import wbif.sjx.common.MetadataExtractors.OperaFileExtractor;
import wbif.sjx.common.MetadataExtractors.OperaFilenameExtractor;
import wbif.sjx.common.MetadataExtractors.OperaFoldernameExtractor;
import wbif.sjx.common.Object.Metadata;

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
    public static final String REFRESH_BUTTON = "Refresh parameters";

    public MetadataExtractor(ModuleCollection modules) {
        super("Extract metadata",modules);
    }


    public interface ExtractorModes {
        String FILENAME_MODE = "Filename";
        String FOLDERNAME_MODE = "Foldername";
        String KEYWORD_MODE = "Keyword";
        String METADATA_FILE_MODE = "Metadata file";
        String SERIES_NAME = "Series name";

        String[] ALL = new String[]{FILENAME_MODE, FOLDERNAME_MODE, KEYWORD_MODE, METADATA_FILE_MODE, SERIES_NAME};

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
        String GENERIC = "Generic";
        String CELLVOYAGER_FOLDERNAME_EXTRACTOR = "Cell Voyager foldername";
        String OPERA_FOLDERNAME_EXTRACTOR = "Opera measurement foldername";
        String OPERA_BARCODE_EXTRACTOR = "Opera barcode";

        String[] ALL = new String[]{GENERIC, CELLVOYAGER_FOLDERNAME_EXTRACTOR, OPERA_FOLDERNAME_EXTRACTOR,
                OPERA_BARCODE_EXTRACTOR};
    }

    public interface MetadataFileExtractors {
        String CSV_FILE = "CSV file";
        String OPERA_METADATA_FILE_EXTRACTOR = "Opera file (.flex)";

        String[] ALL = new String[]{CSV_FILE, OPERA_METADATA_FILE_EXTRACTOR};

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


    private void extractFilename(Metadata metadata, String filenameExtractorName) {
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

    private void extractGeneric(Metadata metadata, String input, String pattern, String groupString) {
        String[] groups = getGroups(groupString);

        NameExtractor extractor = new GenericExtractor(pattern,groups);
        extractor.extract(metadata,input);

    }

    private void extractFoldername(Metadata metadata, String foldernameExtractorName) {
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

    private void extractKeyword(Metadata metadata, String keywordList, String keywordSource) {
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

    private void extractMetadataFile(Metadata metadata, String metadataFileExtractorName) {
        FileExtractor metadataFileExtractor = null;
        switch (metadataFileExtractorName) {
            case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                metadataFileExtractor = new OperaFileExtractor();
                break;
        }

        if (metadataFileExtractor != null) metadataFileExtractor.extract(metadata,metadata.getFile());
    }

    private String getExternalMetadataRegex(Metadata metadata, String inputFilePath, String metadataItemToMatch) {
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

        Metadata metadata = new Metadata();
        NameExtractor extractor = new GenericExtractor(pattern,groups);
        extractor.extract(metadata,exampleString);

        StringBuilder stringBuilder = new StringBuilder();
        for (String group:groups) {
            String value = metadata.getAsString(group);
            if (value == null) value = "NA";
            stringBuilder.append(group);
            stringBuilder.append(": ");
            stringBuilder.append(value);
            stringBuilder.append("\r\n");
        }

        return stringBuilder.toString();

    }


    @Override
    public String getPackageName() {
        return PackageNames.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
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
        Metadata metadata = workspace.getMetadata();

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
                if (foldernameExtractorName.equals(FoldernameExtractors.GENERIC)) {
                    extractGeneric(metadata,metadata.getFile().getParent(),pattern,groups);
                } else {
                    extractFilename(metadata, foldernameExtractorName);
                }
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
                                return Status.PASS;
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

            case ExtractorModes.SERIES_NAME:
                extractGeneric(metadata,metadata.getSeriesName(),pattern,groups);
                break;
        }

        if (showOutput) workspace.showMetadata(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(EXTRACTOR_SEPARATOR,this));
        parameters.add(new ChoiceP(EXTRACTOR_MODE,this,ExtractorModes.FILENAME_MODE,ExtractorModes.ALL));

        parameters.add(new ChoiceP(FILENAME_EXTRACTOR, this,FilenameExtractors.GENERIC,FilenameExtractors.ALL));
        parameters.add(new ChoiceP(FOLDERNAME_EXTRACTOR, this,FoldernameExtractors.GENERIC,FoldernameExtractors.ALL));
        parameters.add(new StringP(KEYWORD_LIST,this));

        parameters.add(new ParamSeparatorP(SOURCE_SEPARATOR,this));
        parameters.add(new ChoiceP(KEYWORD_SOURCE, this,KeywordSources.FILENAME,KeywordSources.ALL));
        parameters.add(new ChoiceP(METADATA_FILE_EXTRACTOR,this,MetadataFileExtractors.CSV_FILE,MetadataFileExtractors.ALL));
        parameters.add(new ChoiceP(INPUT_SOURCE,this,InputSources.FILE_IN_INPUT_FOLDER,InputSources.ALL));
        parameters.add(new FilePathP(METADATA_FILE,this));
        parameters.add(new StringP(METADATA_FILE_NAME,this));
        parameters.add(new MetadataItemP(METADATA_ITEM_TO_MATCH,this));

        parameters.add(new ParamSeparatorP(REGEX_SEPARATOR,this));
        parameters.add(new StringP(PATTERN,this));
        parameters.add(new StringP(GROUPS,this));
        parameters.add(new BooleanP(SHOW_TEST,this,false));
        parameters.add(new StringP(EXAMPLE_STRING,this));
        parameters.add(new TextAreaP(IDENTIFIED_GROUPS,this,false));
        parameters.add(new BooleanP(REGEX_SPLITTING,this,false));
        parameters.add(new StringP(METADATA_VALUE_NAME,this));
        parameters.add(new RefreshButtonP(REFRESH_BUTTON,this));

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
                        returnedParameters.addAll(getGenericExtractorParameters());
                        break;
                }
                break;

            case ExtractorModes.FOLDERNAME_MODE:
                returnedParameters.add(parameters.getParameter(FOLDERNAME_EXTRACTOR));
                switch ((String) parameters.getValue(FOLDERNAME_EXTRACTOR)) {
                    case FoldernameExtractors.GENERIC:
                        returnedParameters.addAll(getGenericExtractorParameters());
                        break;
                }
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
                        if ((boolean) parameters.getValue(REGEX_SPLITTING)) {
                            returnedParameters.addAll(getGenericExtractorParameters());
                        } else {
                            returnedParameters.add(parameters.getParameter(METADATA_VALUE_NAME));
                        }
                        break;
                }
                break;

            case ExtractorModes.SERIES_NAME:
                returnedParameters.addAll(getGenericExtractorParameters());
                break;

        }

        return returnedParameters;

    }

    private ParameterCollection getGenericExtractorParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(REGEX_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PATTERN));
        returnedParameters.add(parameters.getParameter(GROUPS));

        returnedParameters.add(parameters.getParameter(SHOW_TEST));
        if ((boolean) parameters.getValue(SHOW_TEST)) {
            returnedParameters.add(parameters.getParameter(EXAMPLE_STRING));
            returnedParameters.add(parameters.getParameter(IDENTIFIED_GROUPS));

            String pattern = parameters.getValue(PATTERN);
            String groups = parameters.getValue(GROUPS);
            String exampleString = parameters.getValue(EXAMPLE_STRING);
            String groupsString = getTestString(pattern,groups,exampleString);
            TextAreaP identifiedGroups = parameters.getParameter(IDENTIFIED_GROUPS);
            identifiedGroups.setValue(groupsString);

            returnedParameters.add(parameters.getParameter(REFRESH_BUTTON));

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        switch((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                switch ((String) parameters.getValue(FILENAME_EXTRACTOR)) {
                    case FilenameExtractors.GENERIC:
                        String groupString = parameters.getValue(GROUPS);
                        String[] groups = getGroups(groupString);
                        for (String group:groups) returnedRefs.add(metadataRefs.getOrPut((group)));
                        break;

                    case FilenameExtractors.CELLVOYAGER_FILENAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.CHANNEL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.EXTENSION)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.FIELD)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.TIMEPOINT)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.WELL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.ZPOSITION)));
                        break;

                    case FilenameExtractors.INCUCYTE_LONG_FILENAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.EXTENSION)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.COMMENT)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.WELL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.FIELD)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.YEAR)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MONTH)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.DAY)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.HOUR)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MINUTE)));
                        break;

                    case FilenameExtractors.INCUCYTE_SHORT_FILENAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.EXTENSION)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.COMMENT)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.WELL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.FIELD)));
                        break;

                    case FilenameExtractors.OPERA_FILENAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.ROW)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.COL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.FIELD)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.WELL)));
                        break;

                    case FilenameExtractors.YOKOGAWA_FILENAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.EXTENSION)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.PLATE_NAME)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.PLATE_MANUFACTURER)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.PLATE_MODEL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.WELL)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.TIMEPOINT)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.FIELD)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.TIMELINE_NUMBER)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.ACTION_NUMBER)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.ZPOSITION)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.CHANNEL)));
                        break;
                }

                break;

            case ExtractorModes.FOLDERNAME_MODE:
                switch ((String) parameters.getValue(FOLDERNAME_EXTRACTOR)) {
                    case FoldernameExtractors.GENERIC:
                        String groupString = parameters.getValue(GROUPS);
                        String[] groups = getGroups(groupString);
                        for (String group:groups) returnedRefs.add(metadataRefs.getOrPut((group)));
                        break;

                    case FoldernameExtractors.CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.YEAR)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MONTH)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.DAY)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.HOUR)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MINUTE)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.SECOND)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MAGNIFICATION)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.CELLTYPE)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.COMMENT)));
                        break;

                    case FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.YEAR)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MONTH)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.DAY)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.HOUR)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.MINUTE)));
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.SECOND)));
                        break;

                    case FoldernameExtractors.OPERA_BARCODE_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut(("Barcode")));
                        break;
                }
                break;

            case ExtractorModes.KEYWORD_MODE:
                returnedRefs.add(metadataRefs.getOrPut((Metadata.KEYWORD)));
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                switch ((String) parameters.getValue(METADATA_FILE_EXTRACTOR)) {
                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut(("AreaName")));
                        break;

                    case MetadataFileExtractors.CSV_FILE:
                        if ((boolean) parameters.getValue(REGEX_SPLITTING)) {
                            String groupString = parameters.getValue(GROUPS);
                            String[] groups = getGroups(groupString);
                            for (String group : groups) metadataRefs.getOrPut((group));
                        } else {
                            returnedRefs.add(metadataRefs.getOrPut((parameters.getValue(METADATA_VALUE_NAME))));
                        }
                        break;
                }
                break;

            case ExtractorModes.SERIES_NAME:
                String groupString = parameters.getValue(GROUPS);
                String[] groups = getGroups(groupString);
                for (String group:groups) returnedRefs.add(metadataRefs.getOrPut((group)));
                break;

        }

        return returnedRefs;

    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}


