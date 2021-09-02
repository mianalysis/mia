package io.github.mianalysis.MIA.Module.Core;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import loci.common.DebugTools;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.MissingLibraryException;
import loci.formats.UnknownFormatException;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.xml.meta.IMetadata;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Miscellaneous.Macros.RunMacro;
import io.github.mianalysis.MIA.Module.Miscellaneous.Macros.RunMacroOnObjects;
import io.github.mianalysis.MIA.Object.Colours;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.FileFolderPathP;
import io.github.mianalysis.MIA.Object.Parameters.GenericButtonP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.ParameterGroup;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.MIA.Object.Parameters.Text.MessageP;
import io.github.mianalysis.MIA.Object.Parameters.Text.SeriesListSelectorP;
import io.github.mianalysis.MIA.Object.Parameters.Text.StringP;
import io.github.mianalysis.MIA.Object.Refs.ObjMeasurementRef;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.MIA.Object.Units.SpatialUnit;
import io.github.mianalysis.MIA.Object.Units.TemporalUnit;
import io.github.mianalysis.MIA.Process.CommaSeparatedStringInterpreter;
import io.github.sjcross.common.FileConditions.ExtensionMatchesString;
import io.github.sjcross.common.FileConditions.FileCondition;
import io.github.sjcross.common.FileConditions.NameContainsString;
import io.github.sjcross.common.FileConditions.ParentContainsString;
import io.github.sjcross.common.MetadataExtractors.Metadata;
import io.github.sjcross.common.System.FileCrawler;

/**
 * Created by Stephen on 29/07/2017.
 */
public class InputControl extends Module {
    public static final String MESSAGE_SEPARATOR = "Message";
    public static final String NO_LOAD_MESSAGE = "No load message";

    public static final String IMPORT_SEPARATOR = "Core import controls";
    public static final String INPUT_PATH = "Input path";
    // public static final String FILE_LIST = "File list";
    public static final String SERIES_MODE = "Series mode";
    public static final String SERIES_LIST = "Series list";
    public static final String LOAD_FIRST_PER_FOLDER = "Only load first file per folder";
    public static final String LOAD_FIRST_MATCHING_GROUP = "Only load first matching group";
    public static final String PATTERN = "Pattern";
    public static final String REFRESH_FILE = "Refresh test file";

    public static final String CALIBRATION_SEPARATOR = "Calibration controls";
    public static final String SPATIAL_UNIT = "Spatial unit";
    public static final String TEMPORAL_UNIT = "Temporal unit";

    public static final String FILTER_SEPARATOR = "File/folder filters";
    public static final String ADD_FILTER = "Add filter";
    public static final String FILTER_SOURCE = "Filter source";
    public static final String FILTER_VALUE = "Filter value";
    public static final String FILTER_TYPE = "Filter type";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String SIMULTANEOUS_JOBS = "Simultaneous jobs";
    public static final String MACRO_WARNING = "Macro warning";

    // A special store for timepoint references for all objects
    private HashMap<String, ObjMeasurementRef> objectTimepointRefs = new HashMap<>();

    public InputControl(Modules modules) {
        super("Input control", modules);
    }

    public static interface InputModes {
        String SINGLE_FILE = "Single file";
        String BATCH = "Batch";

        String[] ALL = new String[] { BATCH, SINGLE_FILE };

    }

    public static interface SeriesModes {
        String ALL_SERIES = "All series";
        String SERIES_LIST = "Series list";

        String[] ALL = new String[] { ALL_SERIES, SERIES_LIST };

    }

    public static interface FilterSources {
        String EXTENSION = "Extension";
        String FILENAME = "Filename";
        String FILEPATH = "Filepath";
        String SERIESNAME = "Seriesname";

        String[] ALL = new String[] { EXTENSION, FILENAME, FILEPATH, SERIESNAME };

    }

    public static interface FilterTypes {
        String INCLUDE_MATCHES_PARTIALLY = "Matches partially (include)";
        String INCLUDE_MATCHES_COMPLETELY = "Matches completely (include)";
        String EXCLUDE_MATCHES_PARTIALLY = "Matches partially (exclude)";
        String EXCLUDE_MATCHES_COMPLETELY = "Matches completely (exclude)";

        String[] ALL = new String[] { INCLUDE_MATCHES_PARTIALLY, INCLUDE_MATCHES_COMPLETELY, EXCLUDE_MATCHES_PARTIALLY,
                EXCLUDE_MATCHES_COMPLETELY };

    }

    public static interface AvailableSpatialUnits extends SpatialUnit.AvailableUnits {
    }

    public static interface AvailableTemporalUnits extends TemporalUnit.AvailableUnits {
    }

    public File getRootFile() {
        return new File((String) parameters.getValue(INPUT_PATH));
    }

    public void addFilenameFilters(FileCrawler fileCrawler) {
        // Getting filters
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_FILTER);

        // Iterating over each filter
        for (Parameters collection : collections.values()) {
            // If this filter is a filename filter type, add it to the AnalysisRunner
            String filterSource = collection.getValue(FILTER_SOURCE);
            String filterValue = collection.getValue(FILTER_VALUE);
            String filterType = collection.getValue(FILTER_TYPE);

            switch (filterSource) {
                case FilterSources.EXTENSION:
                case FilterSources.FILENAME:
                case FilterSources.FILEPATH:
                    fileCrawler.addFileCondition(getFilenameFilter(filterType, filterValue, filterSource));
                    break;
            }
        }

        // Adding a filter to specifically remove OSX temp files
        fileCrawler.addFileCondition(new NameContainsString("._", NameContainsString.Mode.EXC_PARTIAL));
        fileCrawler.addFileCondition(new NameContainsString(".DS_Store", NameContainsString.Mode.EXC_COMPLETE));

    }

    private static FileCondition getFilenameFilter(String filterType, String filterValue, String filterSource) {
        FileCondition.Mode fileCondition;
        switch (filterType) {
            case FilterTypes.INCLUDE_MATCHES_PARTIALLY:
            default:
                fileCondition = FileCondition.Mode.INC_PARTIAL;
                break;
            case FilterTypes.INCLUDE_MATCHES_COMPLETELY:
                fileCondition = FileCondition.Mode.INC_COMPLETE;
                break;
            case FilterTypes.EXCLUDE_MATCHES_PARTIALLY:
                fileCondition = FileCondition.Mode.EXC_PARTIAL;
                break;
            case FilterTypes.EXCLUDE_MATCHES_COMPLETELY:
                fileCondition = FileCondition.Mode.EXC_COMPLETE;
                break;
        }

        switch (filterSource) {
            case FilterSources.EXTENSION:
                return new ExtensionMatchesString(filterValue, fileCondition);
            case FilterSources.FILENAME:
            default:
                return new NameContainsString(filterValue, fileCondition);
            case FilterSources.FILEPATH:
                return new ParentContainsString(filterValue, fileCondition);
        }
    }

    public TreeMap<Integer, String> getSeriesNumbers(File inputFile) {
        try {
            switch ((String) parameters.getValue(SERIES_MODE)) {
                case SeriesModes.ALL_SERIES:
                    return getAllSeriesNumbers(inputFile);

                case InputControl.SeriesModes.SERIES_LIST:
                    return getSeriesListNumbers(inputFile);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TreeMap<>();

    }

    private TreeMap<Integer, String> getAllSeriesNumbers(File inputFile) throws Exception {
        // Creating the output collection
        TreeMap<Integer, String> namesAndNumbers = new TreeMap<>();

        if (inputFile == null)
            return namesAndNumbers;

        // If the input is a tif or video, we can skip this
        if (FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("tif")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("zip")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("tiff")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("avi")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("mp4")) {
            namesAndNumbers.put(1, inputFile.getName());
            return namesAndNumbers;
        }

        // Using BioFormats to get the number of series
        DebugTools.enableLogging("off");
        DebugTools.setRootLevel("off");

        // Initialising file reader
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        IMetadata meta = service.createOMEXMLMetadata();
        ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
        reader.setMetadataStore((MetadataStore) meta);
        reader.setGroupFiles(false);
        try {
            reader.setId(inputFile.getAbsolutePath());
        } catch (IllegalArgumentException | MissingLibraryException | UnknownFormatException e) {
            namesAndNumbers.put(1, inputFile.getName());
            return namesAndNumbers;
        }

        // Creating a Collection of seriesname filters
        HashSet<FileCondition> filters = new HashSet<>();
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_FILTER);
        for (Parameters collection : collections.values()) {
            // If this filter is a filename filter type, addRef it to the AnalysisRunner
            String filterSource = collection.getValue(FILTER_SOURCE);
            String filterValue = collection.getValue(FILTER_VALUE);
            String filterType = collection.getValue(FILTER_TYPE);

            switch (filterSource) {
                case FilterSources.SERIESNAME:
                    filters.add(getFilenameFilter(filterType, filterValue, filterSource));
                    break;
            }
        }

        for (int seriesNumber = 0; seriesNumber < reader.getSeriesCount(); seriesNumber++) {
            String name = meta.getImageName(seriesNumber);

            boolean pass = true;
            for (FileCondition filter : filters) {
                if (name != null & !filter.test(new File(name))) {
                    pass = false;
                    break;
                }
            }

            if (pass)
                namesAndNumbers.put(seriesNumber + 1, name);

        }

        reader.close();

        return namesAndNumbers;

    }

    private TreeMap<Integer, String> getSeriesListNumbers(File inputFile) throws Exception {
        TreeMap<Integer, String> namesAndNumbers = new TreeMap<>();

        if (inputFile == null)
            return namesAndNumbers;

        // If the input is a tif or video, we can skip this
        if (FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("tif")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("zip")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("tiff")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("avi")
                || FilenameUtils.getExtension(inputFile.getName()).equalsIgnoreCase("mp4")) {
            namesAndNumbers.put(1, inputFile.getName());
            return namesAndNumbers;
        }

        // Using BioFormats to get the number of series
        DebugTools.enableLogging("off");
        DebugTools.setRootLevel("off");

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        OMEXMLMetadata meta = service.createOMEXMLMetadata();
        ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
        reader.setMetadataStore((MetadataStore) meta);
        reader.setGroupFiles(false);
        try {
            reader.setId(inputFile.getAbsolutePath());
        } catch (IllegalArgumentException | MissingLibraryException | UnknownFormatException e) {
            namesAndNumbers.put(1, inputFile.getName());
            return namesAndNumbers;
        }

        String seriesListString = parameters.getValue(InputControl.SERIES_LIST);
        int[] seriesList = CommaSeparatedStringInterpreter.interpretIntegers(seriesListString, true,
                reader.getSeriesCount());

        for (int series : seriesList) {
            if (series > reader.getSeriesCount())
                continue;

            namesAndNumbers.put(series, meta.getImageName(series - 1));

        }

        reader.close();

        return namesAndNumbers;

    }

    @Override
    public Category getCategory() {
        return Categories.CORE;
    }

    @Override
    public String getDescription() {
        return "Select which file(s) or folder(s) MIA will process through.  If a file is selected, that file alone "
                + "will be processed; however, selecting a folder will cause the system to iterate over all files and "
                + "sub-folders within that folder.  Each file identified here will initialise its own workspace.  "
                + "<br><br>"
                + "It is possible to add filters to limit which files are used.  Multiple filters can be applied."
                + "<br><br>"
                + "n.b. This module simply creates the workspace for subsequent analysis; no images are automatically "
                + "loaded at this point.  To load image data to the workspace use the \"Load image\" module.";

    }

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(MESSAGE_SEPARATOR, this));
        parameters.add(new MessageP(NO_LOAD_MESSAGE, this,
                "\"Input control\" only specifies the path to the root image; no image is loaded into the workspace at this point.  To load images, add one or more \"Load Image\" modules.",
                Colours.ORANGE));

        parameters.add(new SeparatorP(IMPORT_SEPARATOR, this));
        parameters.add(new FileFolderPathP(INPUT_PATH, this));
        // parameters.add(new FileListP(FILE_LIST,this));
        parameters.add(new ChoiceP(SERIES_MODE, this, SeriesModes.ALL_SERIES, SeriesModes.ALL));
        parameters.add(new SeriesListSelectorP(SERIES_LIST, this, "1"));
        parameters.add(new BooleanP(LOAD_FIRST_PER_FOLDER, this, false));
        parameters.add(new BooleanP(LOAD_FIRST_MATCHING_GROUP, this, false));
        parameters.add(new StringP(PATTERN, this));
        parameters.add(new GenericButtonP(REFRESH_FILE, this, REFRESH_FILE, GenericButtonP.DefaultModes.REFRESH_FILE));

        parameters.add(new SeparatorP(CALIBRATION_SEPARATOR, this));
        parameters.add(new ChoiceP(SPATIAL_UNIT, this, AvailableSpatialUnits.MICROMETRE, AvailableSpatialUnits.ALL));
        parameters
                .add(new ChoiceP(TEMPORAL_UNIT, this, AvailableTemporalUnits.MILLISECOND, AvailableTemporalUnits.ALL));

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new ChoiceP(FILTER_SOURCE, this, FilterSources.EXTENSION, FilterSources.ALL));
        collection.add(new StringP(FILTER_VALUE, this));
        collection.add(new ChoiceP(FILTER_TYPE, this, FilterTypes.INCLUDE_MATCHES_PARTIALLY, FilterTypes.ALL));
        parameters.add(new ParameterGroup(ADD_FILTER, this, collection, 0));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new IntegerP(SIMULTANEOUS_JOBS, this, 1));
        parameters.add(new MessageP(MACRO_WARNING, this,
                "Analysis can only be run as a single simultaneous job when ImageJ macro module is present.",
                Colours.RED));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(MESSAGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(NO_LOAD_MESSAGE));

        returnedParameters.add(parameters.getParameter(IMPORT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_PATH));
        // returnedParameters.add(parameters.getParameter(FILE_LIST));

        ChoiceP seriesMode = (ChoiceP) parameters.getParameter(SERIES_MODE);
        returnedParameters.add(seriesMode);
        switch (seriesMode.getChoice()) {
            case SeriesModes.SERIES_LIST:
                returnedParameters.add(parameters.getParameter(SERIES_LIST));
                break;
        }
        returnedParameters.add(parameters.getParameter(LOAD_FIRST_PER_FOLDER));
        returnedParameters.add(parameters.getParameter(LOAD_FIRST_MATCHING_GROUP));
        if ((boolean) parameters.getValue(LOAD_FIRST_MATCHING_GROUP))
            returnedParameters.add(parameters.getParameter(PATTERN));
        returnedParameters.add(parameters.getParameter(REFRESH_FILE));

        returnedParameters.add(parameters.getParameter(CALIBRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SPATIAL_UNIT));
        returnedParameters.add(parameters.getParameter(TEMPORAL_UNIT));

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_FILTER));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SIMULTANEOUS_JOBS));
        // If a the RunMacro module is present, this analysis must be run as a single
        // job
        if ((int) parameters.getValue(SIMULTANEOUS_JOBS) > 1) {
            for (Module module : modules) {
                if (module instanceof RunMacro || module instanceof RunMacroOnObjects) {
                    returnedParameters.add(parameters.getParameter(MACRO_WARNING));
                    parameters.getParameter(SIMULTANEOUS_JOBS).setValid(false);
                }
            }
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

        // The following are added to the MetadataRefs during Workspace
        // construction
        returnedRefs.add(metadataRefs.getOrPut(Metadata.EXTENSION));
        returnedRefs.add(metadataRefs.getOrPut(Metadata.FILE));
        returnedRefs.add(metadataRefs.getOrPut(Metadata.FILEPATH));
        returnedRefs.add(metadataRefs.getOrPut(Metadata.FILENAME));
        returnedRefs.add(metadataRefs.getOrPut(Metadata.SERIES_NUMBER));
        returnedRefs.add(metadataRefs.getOrPut(Metadata.SERIES_NAME));

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
        parameters.get(INPUT_PATH).setDescription(
                "The file or folder path to process.  If a file is selected, that file alone will be processed.  If a folder is selected, each file in that folder (and all sub-folders) passing the filters will be processed.");

        parameters.get(SIMULTANEOUS_JOBS).setDescription(
                "The number of images that will be processed simultaneously.  If this is set to \"1\" while processing a folder each valid file will still be processed, they will just complete one at a time.  For large images this is best left as \"1\" unless using a system with large amounts of RAM.");

        parameters.get(SERIES_MODE).setDescription("For multi-series files, select which series to process:<br><ul>"

                + "<li>\"" + SeriesModes.ALL_SERIES + "\" will create a new workspace for each series in the file.</li>"
                + "<li>\"" + SeriesModes.SERIES_LIST
                + "\" allows a comma-separated list of series numbers to be specified.</li></ul>");

        parameters.get(SERIES_LIST).setDescription("Comma-separated list of series numbers to be processed.");

        parameters.get(LOAD_FIRST_PER_FOLDER)
                .setDescription("Only load the (alphabetically) first file in each folder.");

        parameters.get(LOAD_FIRST_MATCHING_GROUP).setDescription(
                "Only load the (alphabetically) first file matching the group specified by the regular expression \""
                        + PATTERN
                        + "\" parameter.  Each candidate filename will be matched against the regular expression in \""
                        + PATTERN
                        + "\" and retained for analysis if the selected group hasn't been seen before.  Only one group (specified using standard regex parenthesis notation) can be used.");

        parameters.get(PATTERN).setDescription("Regular expression pattern to use when \"" + LOAD_FIRST_MATCHING_GROUP
                + "\" is enabled.  This pattern must contain at least one group (specified using standard regex parenthesis notation).");

        parameters.get(REFRESH_FILE).setDescription(
                "When pressed, MIA will update the active file used in test mode (i.e. when executing individual modules from editing mode) based on the currently-selected input file/folder and series settings.");

        Parameters templateParameters = ((ParameterGroup) parameters.get(ADD_FILTER)).getTemplateParameters();
        templateParameters.get(FILTER_SOURCE).setDescription("Type of filter to add.");

        templateParameters.get(FILTER_VALUE).setDescription("Value to filter filenames against.");

        templateParameters.get(FILTER_TYPE).setDescription("Control how the present filter operates:"

                + "<li>\"" + FilterTypes.INCLUDE_MATCHES_PARTIALLY
                + "\" will process an image if the filter value is partially present in the source (e.g. filename or extension).</li>"

                + "<li>\"" + FilterTypes.INCLUDE_MATCHES_COMPLETELY
                + "\" will process an image if the filter value is exactly the same as the source.</li>"

                + "<li>\"" + FilterTypes.EXCLUDE_MATCHES_PARTIALLY
                + "\" will not process an image if the filter value is partially present in the source.</li>"

                + "<li>\"" + FilterTypes.EXCLUDE_MATCHES_COMPLETELY
                + "\" will not process an image if the filter value is exactly the same as the source.</li></ul>");

        parameters.get(ADD_FILTER)
                .setDescription("Add another filename filter.  All images to be processed will pass all filters.");

        parameters.get(SPATIAL_UNIT).setDescription(
                "Spatial units for calibrated measurements.  Assuming spatial calibration can be read from the input file when loaded, this will convert the input calibrated units to the units specified here.");

        parameters.get(TEMPORAL_UNIT).setDescription(
                "Temporal units for calibrated measurements.  Assuming temporal calibration can be read from the input file when loaded, this will convert the input calibrated units to the units specified here.");

    }
}
