package io.github.mianalysis.mia.module.inputoutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.GenericButtonP;
import io.github.mianalysis.mia.object.parameters.MetadataItemP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.metadataextractors.CV1000FilenameExtractor;
import io.github.sjcross.common.metadataextractors.CV1000FoldernameExtractor;
import io.github.sjcross.common.metadataextractors.CV7000FilenameExtractor;
import io.github.sjcross.common.metadataextractors.FileExtractor;
import io.github.sjcross.common.metadataextractors.GenericExtractor;
import io.github.sjcross.common.metadataextractors.IncuCyteLongFilenameExtractor;
import io.github.sjcross.common.metadataextractors.IncuCyteShortFilenameExtractor;
import io.github.sjcross.common.metadataextractors.Metadata;
import io.github.sjcross.common.metadataextractors.NameExtractor;
import io.github.sjcross.common.metadataextractors.OperaFileExtractor;
import io.github.sjcross.common.metadataextractors.OperaFilenameExtractor;
import io.github.sjcross.common.metadataextractors.OperaFoldernameExtractor;

/**
 * Created by sc13967 on 05/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MetadataExtractor extends Module {
    public static final String EXTRACTOR_SEPARATOR = "Metadata extractor selection";
    public static final String EXTRACTOR_MODE = "Extractor mode";
    public static final String FILENAME_EXTRACTOR = "Filename extractor";
    public static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";

    public static final String SOURCE_SEPARATOR = "Metadata source";
    public static final String METADATA_FILE_EXTRACTOR = "Metadata file extractor";
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

    public MetadataExtractor(Modules modules) {
        super("Extract metadata", modules);
    }

    public interface ExtractorModes {
        String FILENAME_MODE = "Filename";
        String FOLDERNAME_MODE = "Foldername";
        String METADATA_FILE_MODE = "Metadata file";
        String SERIES_NAME = "Series name";

        String[] ALL = new String[] { FILENAME_MODE, FOLDERNAME_MODE, METADATA_FILE_MODE, SERIES_NAME };

    }

    public interface FilenameExtractors {
        String GENERIC = "Generic";
        String CV1000_FILENAME_EXTRACTOR = "CV1000 filename";
        String CV7000_FILENAME_EXTRACTOR = "CV7000 filename";
        String INCUCYTE_LONG_FILENAME_EXTRACTOR = "IncuCyte long filename";
        String INCUCYTE_SHORT_FILENAME_EXTRACTOR = "IncuCyte short filename";
        String OPERA_FILENAME_EXTRACTOR = "Opera filename";

        String[] ALL = new String[] { GENERIC, CV1000_FILENAME_EXTRACTOR, CV7000_FILENAME_EXTRACTOR,
                INCUCYTE_LONG_FILENAME_EXTRACTOR, INCUCYTE_SHORT_FILENAME_EXTRACTOR, OPERA_FILENAME_EXTRACTOR };

    }

    public interface FoldernameExtractors {
        String GENERIC = "Generic";
        String CV1000_FOLDERNAME_EXTRACTOR = "CV1000 foldername";
        String OPERA_FOLDERNAME_EXTRACTOR = "Opera measurement foldername";

        String[] ALL = new String[] { GENERIC, CV1000_FOLDERNAME_EXTRACTOR, OPERA_FOLDERNAME_EXTRACTOR };
    }

    public interface MetadataFileExtractors {
        String CSV_FILE = "CSV file";
        String OPERA_METADATA_FILE_EXTRACTOR = "Opera file (.flex)";

        String[] ALL = new String[] { CSV_FILE, OPERA_METADATA_FILE_EXTRACTOR };

    }

    public interface InputSources {
        String FILE_IN_INPUT_FOLDER = "File in input folder";
        String STATIC_FILE = "Static file";

        String[] ALL = new String[] { FILE_IN_INPUT_FOLDER, STATIC_FILE };

    }

    private void extractFilename(Metadata metadata, String filenameExtractorName) {
        NameExtractor filenameExtractor = null;

        switch (filenameExtractorName) {
            case FilenameExtractors.CV1000_FILENAME_EXTRACTOR:
                filenameExtractor = new CV1000FilenameExtractor();
                break;

            case FilenameExtractors.CV7000_FILENAME_EXTRACTOR:
                filenameExtractor = new CV7000FilenameExtractor();
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

            default:
                return;

        }

        Metadata tempMetadata = new Metadata();
        filenameExtractor.extract(tempMetadata, metadata.getFile().getName());
        for (String name : tempMetadata.keySet())
            metadata.put(name, tempMetadata.get(name));

    }

    private void extractGeneric(Metadata metadata, String input, String pattern, String groupString) {
        String[] groups = getGroups(groupString);

        NameExtractor extractor = new GenericExtractor(pattern, groups);

        Metadata tempMetadata = new Metadata();
        extractor.extract(tempMetadata, input);
        for (String name : tempMetadata.keySet())
            metadata.put(name, tempMetadata.get(name));

    }

    private void extractFoldername(Metadata metadata, String foldernameExtractorName) {
        // Getting folder name extractor
        NameExtractor foldernameExtractor = null;
        switch (foldernameExtractorName) {
            case FoldernameExtractors.CV1000_FOLDERNAME_EXTRACTOR:
                foldernameExtractor = new CV1000FoldernameExtractor();
                break;

            case FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR:
                foldernameExtractor = new OperaFoldernameExtractor();
                break;
        }

        if (foldernameExtractor != null) {
            Metadata tempMetadata = new Metadata();
            foldernameExtractor.extract(tempMetadata, metadata.getFile().getParent());
            for (String name : tempMetadata.keySet())
                metadata.put(name, tempMetadata.get(name));
        }
    }

    private void extractMetadataFile(Metadata metadata, String metadataFileExtractorName) {
        FileExtractor metadataFileExtractor = null;

        switch (metadataFileExtractorName) {
            case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                metadataFileExtractor = new OperaFileExtractor();
                break;
        }

        if (metadataFileExtractor == null)
            return;

        Metadata tempMetadata = new Metadata();
        metadataFileExtractor.extract(tempMetadata, metadata.getFile());
        for (String name : tempMetadata.keySet())
            metadata.put(name, tempMetadata.get(name));

    }

    private String getExternalMetadataRegex(Metadata metadata, String inputFilePath, String metadataItemToMatch) {
        // Reading contents of metadata file
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
            String line = "";
            HashMap<String, String> referenceValues = new HashMap<>();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.toString().replace("\uFEFF", "");
                String[] split = line.split(",");
                if (split.length == 2)
                    referenceValues.put(split[0], split[1]);
            }
            bufferedReader.close();

            if (metadata.containsKey(metadataItemToMatch)) {
                return referenceValues.get(metadata.get(metadataItemToMatch).toString());
            } else
                return null;

        } catch (IOException e) {
            return null;
        }
    }

    public String[] getGroups(String groupString) {
        groupString = groupString.replace(" ", "");

        StringTokenizer tokenizer = new StringTokenizer(groupString, ",");
        int nTokens = tokenizer.countTokens();

        int i = 0;
        String[] groups = new String[nTokens];
        while (tokenizer.hasMoreTokens())
            groups[i++] = tokenizer.nextToken();

        return groups;

    }

    public String getTestString(String pattern, String groupString, String exampleString) {
        String[] groups = getGroups(groupString);

        Metadata metadata = new Metadata();
        NameExtractor extractor = new GenericExtractor(pattern, groups);
        extractor.extract(metadata, exampleString);

        StringBuilder stringBuilder = new StringBuilder();
        for (String group : groups) {
            String value = metadata.getAsString(group);
            if (value == null)
                value = "NA";
            stringBuilder.append(group);
            stringBuilder.append(": ");
            stringBuilder.append(value);
            stringBuilder.append("\r\n");
        }

        return stringBuilder.toString();

    }

    @Override
    public Category getCategory() {
        return Categories.INPUT_OUTPUT;
    }

    @Override
    public String getDescription() {
        return "Metadata values can be extracted from a variety of sources and assigned to the current workspace.  These metadata values can subsequently be accessed in the form M{[NAME]}, where [NAME] is the metadata name.  Some common file and foldername formats are included as pre-defined metadata extraction methods, while other forms can be constructed using regular expressions.";
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
        boolean regexSplitting = parameters.getValue(REGEX_SPLITTING);
        String metadataValueName = parameters.getValue(METADATA_VALUE_NAME);

        // Getting current result
        Metadata metadata = workspace.getMetadata();

        switch (extractorMode) {
            case ExtractorModes.FILENAME_MODE:
                // Getting filename extractor
                if (filenameExtractorName.equals(FilenameExtractors.GENERIC)) {
                    extractGeneric(metadata, metadata.getFile().getName(), pattern, groups);
                } else {
                    extractFilename(metadata, filenameExtractorName);
                }
                break;

            case ExtractorModes.FOLDERNAME_MODE:
                if (foldernameExtractorName.equals(FoldernameExtractors.GENERIC)) {
                    extractGeneric(metadata, metadata.getFile().getParent(), pattern, groups);
                } else {
                    extractFoldername(metadata, foldernameExtractorName);
                }
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                switch (metadataFileExtractorName) {
                    case MetadataFileExtractors.CSV_FILE:
                        String metadataSourcePath = null;
                        switch (inputSource) {
                            case InputSources.FILE_IN_INPUT_FOLDER:
                                metadataSourcePath = workspace.getMetadata().getFile().getParentFile().getPath();
                                metadataSourcePath = metadataSourcePath + File.separator + metadataFileName;
                                break;
                            case InputSources.STATIC_FILE:
                                metadataSourcePath = metadataFilePath;
                                break;

                            default:
                                return Status.PASS;
                        }
                        String metadataString = getExternalMetadataRegex(metadata, metadataSourcePath,
                                metadataItemToMatch);
                        if (metadataString != null) {
                            if (regexSplitting)
                                extractGeneric(metadata, metadataString, pattern, groups);
                            else
                                extractGeneric(metadata, metadataString, "([^s]+)", metadataValueName);
                        }
                        break;

                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        extractMetadataFile(metadata, metadataFileExtractorName);
                        break;
                }
                break;

            case ExtractorModes.SERIES_NAME:
                extractGeneric(metadata, metadata.getSeriesName(), pattern, groups);
                break;
        }

        if (showOutput)
            workspace.showMetadata(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(EXTRACTOR_SEPARATOR, this));
        parameters.add(new ChoiceP(EXTRACTOR_MODE, this, ExtractorModes.FILENAME_MODE, ExtractorModes.ALL));
        parameters.add(new ChoiceP(FILENAME_EXTRACTOR, this, FilenameExtractors.GENERIC, FilenameExtractors.ALL));
        parameters.add(new ChoiceP(FOLDERNAME_EXTRACTOR, this, FoldernameExtractors.GENERIC, FoldernameExtractors.ALL));

        parameters.add(new SeparatorP(SOURCE_SEPARATOR, this));
        parameters.add(new ChoiceP(METADATA_FILE_EXTRACTOR, this, MetadataFileExtractors.CSV_FILE,
                MetadataFileExtractors.ALL));
        parameters.add(new ChoiceP(INPUT_SOURCE, this, InputSources.FILE_IN_INPUT_FOLDER, InputSources.ALL));
        parameters.add(new FilePathP(METADATA_FILE, this));
        parameters.add(new StringP(METADATA_FILE_NAME, this));
        parameters.add(new MetadataItemP(METADATA_ITEM_TO_MATCH, this));

        parameters.add(new SeparatorP(REGEX_SEPARATOR, this));
        parameters.add(new StringP(PATTERN, this));
        parameters.add(new StringP(GROUPS, this));
        parameters.add(new BooleanP(SHOW_TEST, this, false));
        parameters.add(new StringP(EXAMPLE_STRING, this));
        parameters.add(new TextAreaP(IDENTIFIED_GROUPS, this, false));
        parameters.add(new BooleanP(REGEX_SPLITTING, this, false));
        parameters.add(new StringP(METADATA_VALUE_NAME, this));
        parameters.add(new GenericButtonP(REFRESH_BUTTON, this, "Refresh", GenericButtonP.DefaultModes.REFRESH));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(EXTRACTOR_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EXTRACTOR_MODE));
        switch ((String) parameters.getValue(EXTRACTOR_MODE)) {
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

    private Parameters getGenericExtractorParameters() {
        Parameters returnedParameters = new Parameters();

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
            String groupsString = getTestString(pattern, groups, exampleString);
            TextAreaP identifiedGroups = parameters.getParameter(IDENTIFIED_GROUPS);
            identifiedGroups.setValue(groupsString);

            returnedParameters.add(parameters.getParameter(REFRESH_BUTTON));

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        MetadataRefs returnedRefs = new MetadataRefs();

        switch ((String) parameters.getValue(EXTRACTOR_MODE)) {
            case ExtractorModes.FILENAME_MODE:
                switch ((String) parameters.getValue(FILENAME_EXTRACTOR)) {
                    case FilenameExtractors.GENERIC:
                        String groupString = parameters.getValue(GROUPS);
                        String[] groups = getGroups(groupString);
                        for (String group : groups)
                            returnedRefs.add(metadataRefs.getOrPut((group)));
                        break;

                    case FilenameExtractors.CV1000_FILENAME_EXTRACTOR:
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

                    case FilenameExtractors.CV7000_FILENAME_EXTRACTOR:
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
                        for (String group : groups)
                            returnedRefs.add(metadataRefs.getOrPut((group)));
                        break;

                    case FoldernameExtractors.CV1000_FOLDERNAME_EXTRACTOR:
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
                }
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                switch ((String) parameters.getValue(METADATA_FILE_EXTRACTOR)) {
                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        returnedRefs.add(metadataRefs.getOrPut((Metadata.AREA_NAME)));
                        break;

                    case MetadataFileExtractors.CSV_FILE:
                        if ((boolean) parameters.getValue(REGEX_SPLITTING)) {
                            String groupString = parameters.getValue(GROUPS);
                            String[] groups = getGroups(groupString);
                            for (String group : groups)
                                metadataRefs.getOrPut((group));
                        } else {
                            returnedRefs.add(metadataRefs.getOrPut((parameters.getValue(METADATA_VALUE_NAME))));
                        }
                        break;
                }
                break;

            case ExtractorModes.SERIES_NAME:
                String groupString = parameters.getValue(GROUPS);
                String[] groups = getGroups(groupString);
                for (String group : groups)
                    returnedRefs.add(metadataRefs.getOrPut((group)));
                break;

        }

        return returnedRefs;

    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        // +"<li>\""++"\"</li>"

        parameters.get(EXTRACTOR_MODE).setDescription("Data source for metadata extraction:<br><ul>"

                + "<li>\"" + ExtractorModes.FILENAME_MODE
                + "\" Metadata taken from filename (not including folder path)</li>"

                + "<li>\"" + ExtractorModes.FOLDERNAME_MODE
                + "\" Metadata taken from parent foldername (incuding full system path to that folder)</li>"

                + "<li>\"" + ExtractorModes.METADATA_FILE_MODE
                + "\" Metadata taken from a separate file, specified by the \"" + METADATA_FILE + "\" parameter</li>"

                + "<li>\"" + ExtractorModes.SERIES_NAME
                + "\" Metadata taken from seriesname (if not from a multi-series file, this is just the filename)</li></ul>");

        parameters.get(FILENAME_EXTRACTOR)
                .setDescription("The format of the filename to be converted to metadata values:<br><ul>"

                        + "<li>\"" + FilenameExtractors.GENERIC
                        + "\" Name format is compiled using regular expressions defined in the \"" + PATTERN
                        + "\" parameter.  Each group (pattern enclosed in parenthesis) specified in the pattern is assigned to a metadata value.  Metadata value names are defined by the comma-separated list defined in \""
                        + GROUPS + "\".</li>"

                        + "<li>\"" + FilenameExtractors.CV1000_FILENAME_EXTRACTOR
                        + "\" The Yokogawa CellVoyager CV1000 format (e.g. W1F001T0001Z00C1.tif)</li>"

                        + "<li>\"" + FilenameExtractors.CV7000_FILENAME_EXTRACTOR
                        + "\" The Yokogawa CellVoyager CV7000 format (e.g. AssayPlate_Greiner_#655090_C02_T0001F001L01A01Z01C01.tif)</li>"

                        + "<li>\"" + FilenameExtractors.INCUCYTE_LONG_FILENAME_EXTRACTOR
                        + "\" The Incucyte long format, where each timepoint is stored as a separate image file and accordingly the filename records the time of acquisition (e.g. MySample1_A1_1_2021y08m06d_11h39m.tif)</li>"

                        + "<li>\"" + FilenameExtractors.INCUCYTE_SHORT_FILENAME_EXTRACTOR
                        + "\" The Incucyte short format, where all timepoints are stored in a single file and the filename only records the well and field (e.g. MySample1_A1_1.tif)</li>"

                        + "<li>\"" + FilenameExtractors.OPERA_FILENAME_EXTRACTOR
                        + "\" The Perkin Elmer Opera LX name format, which specifies row, column and field (e.g. 001001001.flex)</li></ul>");

        parameters.get(FOLDERNAME_EXTRACTOR)
                .setDescription("The format of the foldername to be converted to metadata values:<br><ul>" + "<li>\""

                        + "<li>\"" + FoldernameExtractors.GENERIC
                        + "\" Name format is compiled using regular expressions defined in the \"" + PATTERN
                        + "\" parameter.  Each group (pattern enclosed in parenthesis) specified in the pattern is assigned to a metadata value.  Metadata value names are defined by the comma-separated list defined in \""
                        + GROUPS + "\".</li>"

                        + "<li>\"" + FoldernameExtractors.CV1000_FOLDERNAME_EXTRACTOR
                        + "\" The Yokogawa CV1000 format (e.g. 20210806T113905_10x_K01_MySample1)</li>"

                        + "<li>\"" + FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR
                        + "\" The Perkin Elmer Opera LX foldername format (e.g. Meas_01(2021-08-06_11-39-05))</li></ul>"

                );

        parameters.get(METADATA_FILE_EXTRACTOR)
                .setDescription("The format of the metadata file to be converted to metadata values:<br><ul>"

                        + "<li>\"" + MetadataFileExtractors.CSV_FILE
                        + "\" Metadata values stored in a two-column CSV file, where the first column defines an identifier to the current file being processed (e.g. filename, or series name) and the second column defines a value that will be assigned as metadata.  Optionally, this value can be split into multiple metadata values using a regular expression.</li>"

                        + "<li>\"" + MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR
                        + "\" Specifically extracts the \"Area name\" property from the current .flex file.</li></ul>");

        parameters.get(INPUT_SOURCE).setDescription(
                "If extracting metadata from a CSV file, this controls whether a single, static CSV file is used or whether there is one provided (with a fixed name) in the current folder (i.e where the current image file is loaded from).");

        parameters.get(METADATA_FILE).setDescription(
                "If extracting metadata from a static CSV file (i.e. the same file for all processed images), this is the path to that metadata CSV file.");

        parameters.get(METADATA_FILE_NAME).setDescription(
                "If extracting metadata from a dynamic CSV file (i.e. the file is in the current image folder), this is the name of that metadata CSV file (it must be the same name in all folders).");

        parameters.get(METADATA_ITEM_TO_MATCH).setDescription(
                "For CSV-based metadata extraction, the first column of the CSV file identifies which row to read.  This parameter defines the source (e.g. filename, series name, etc.).  Choices are: "
                        + String.join(",", InputSources.ALL));

        parameters.get(REGEX_SPLITTING).setDescription(
                "When selected, the metadata value taken from a CSV file will itself be broken down into multiple metadata values using a regular expressions");

        parameters.get(METADATA_VALUE_NAME).setDescription(
                "If a metadata value loaded from CSV is not to be sub-divided into multiple metadata values, the entire value loaded will be stored as a metadata item with this name.");

        parameters.get(PATTERN).setDescription(
                "Regular expression pattern to use when interpreting generic metadata formats.  This pattern must contain at least one group (specified using standard regex parenthesis notation).");

        parameters.get(GROUPS).setDescription(
                "When interpreting generic metadata formats, these group names will be assigned to each group matched using regular expressions.  The metadata values will subsequently be accessed via these names in the form M{[NAME]}, where [NAME] is the group name.");

        parameters.get(SHOW_TEST).setDescription(
                "When selected (and constructing a regular expression extractor), an example string can be provided and the identified groups displayed.  This allows for regular expression forms to be tested during workflow assembly.");

        parameters.get(EXAMPLE_STRING).setDescription("If testing a regular expression form (\"" + SHOW_TEST
                + "\" selected), this is the example string which will be processed and broken down into its individual metadata values.");

        parameters.get(IDENTIFIED_GROUPS).setDescription("If testing a regular expression form (\"" + SHOW_TEST
                + "\" selected), the extracted metadata values will be displayed here.");

        parameters.get(REFRESH_BUTTON).setDescription(
                "When testing regular expression forms, clicking this button will test the extraction and display any detected metadata values in the \""
                        + IDENTIFIED_GROUPS + "\" window.");

    }
}
