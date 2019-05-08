package wbif.sjx.MIA.GUI.InputOutput;

import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.xml.meta.IMetadata;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.BatchProcessor;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;
import wbif.sjx.common.FileConditions.FileCondition;
import wbif.sjx.common.FileConditions.NameContainsString;
import wbif.sjx.common.FileConditions.ParentContainsString;
import wbif.sjx.common.Object.HCMetadata;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Stephen on 29/07/2017.
 */
public class InputControl extends Module {
    public static final String IMPORT_SEPARATOR = "Core import controls";
    public static final String INPUT_PATH = "Input path";
    public static final String SPATIAL_UNITS = "Spatial units";
    public static final String SIMULTANEOUS_JOBS = "Simultaneous jobs";
    public static final String SERIES_MODE = "Series mode";
    public static final String SERIES_LIST = "Series list";
    public static final String FILTER_SEPARATOR = "File/folder filters";
    public static final String ADD_FILTER = "Add filter";
    public static final String FILTER_SOURCE = "Filter source";
    public static final String FILTER_VALUE = "Filter value";
    public static final String FILTER_TYPE = "Filter type";
    public static final String NO_LOAD_MESSAGE = "No load message";


    public static interface InputModes {
        String SINGLE_FILE = "Single file";
        String BATCH = "Batch";

        String[] ALL = new String[]{BATCH,SINGLE_FILE};

    }

    public static interface SeriesModes {
        String ALL_SERIES = "All series";
        String SERIES_LIST = "Series list (comma separated)";

        String[] ALL = new String[]{ALL_SERIES,SERIES_LIST};

    }

    public static interface FilterSources {
        String EXTENSION = "Extension";
        String FILENAME = "Filename";
        String FILEPATH = "Filepath";
        String SERIESNAME = "Seriesname";

        String[] ALL = new String[]{EXTENSION,FILENAME,FILEPATH,SERIESNAME};

    }

    public static interface FilterTypes {
        String INCLUDE_MATCHES_PARTIALLY = "Matches partially (include)";
        String INCLUDE_MATCHES_COMPLETELY = "Matches completely (include)";
        String EXCLUDE_MATCHES_PARTIALLY = "Matches partially (exclude)";
        String EXCLUDE_MATCHES_COMPLETELY = "Matches completely (exclude)";

        String[] ALL = new String[]{INCLUDE_MATCHES_PARTIALLY,INCLUDE_MATCHES_COMPLETELY,EXCLUDE_MATCHES_PARTIALLY,EXCLUDE_MATCHES_COMPLETELY};

    }

    public static interface SpatialUnits extends Units.SpatialUnits{}


    public void addFilenameFilters(BatchProcessor batchProcessor) {
        // Getting filters
        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_FILTER);

        // Iterating over each filter
        for (ParameterCollection collection:collections) {
            // If this filter is a filename filter type, add it to the BatchProcessor
            String filterSource = collection.getValue(FILTER_SOURCE);
            String filterValue = collection.getValue(FILTER_VALUE);
            String filterType = collection.getValue(FILTER_TYPE);

            switch (filterSource) {
                case FilterSources.EXTENSION:
                case FilterSources.FILENAME:
                case FilterSources.FILEPATH:
                    batchProcessor.addFileCondition(getFilenameFilter(filterType,filterValue,filterSource));
                    break;
            }
        }
    }

    private static FileCondition getFilenameFilter(String filterType, String filterValue, String filterSource) {
        int fileCondition;
        switch (filterType) {
            case FilterTypes.INCLUDE_MATCHES_PARTIALLY:
            default:
                fileCondition = FileCondition.INC_PARTIAL;
                break;
            case FilterTypes.INCLUDE_MATCHES_COMPLETELY:
                fileCondition = FileCondition.INC_COMPLETE;
                break;
            case FilterTypes.EXCLUDE_MATCHES_PARTIALLY:
                fileCondition = FileCondition.EXC_PARTIAL;
                break;
            case FilterTypes.EXCLUDE_MATCHES_COMPLETELY:
                fileCondition = FileCondition.EXC_COMPLETE;
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

    public TreeMap<Integer,String> getSeriesNumbers(File inputFile) {
        try {
            switch ((String) parameters.getValue(SERIES_MODE)) {
                case SeriesModes.ALL_SERIES:
                    return getAllSeriesNumbers(inputFile);

                case InputControl.SeriesModes.SERIES_LIST:
                    return getSeriesListNumbers(inputFile);

            }
        } catch (DependencyException | FormatException | ServiceException | IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private TreeMap<Integer,String> getAllSeriesNumbers(File inputFile) throws DependencyException, ServiceException, IOException, FormatException {
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
        reader.setId(inputFile.getAbsolutePath());

        // Creating a Collection of seriesname filters
        HashSet<FileCondition> filters = new HashSet<>();
        LinkedHashSet<ParameterCollection> collections = parameters.getValue(ADD_FILTER);
        for (ParameterCollection collection:collections) {
            // If this filter is a filename filter type, add it to the BatchProcessor
            String filterSource = collection.getValue(FILTER_SOURCE);
            String filterValue = collection.getValue(FILTER_VALUE);
            String filterType = collection.getValue(FILTER_TYPE);

            switch (filterSource) {
                case FilterSources.SERIESNAME:
                    filters.add(getFilenameFilter(filterType,filterValue,filterSource));
                    break;
            }
        }

        TreeMap<Integer,String> namesAndNumbers = new TreeMap<>();
        for (int seriesNumber=0;seriesNumber<reader.getSeriesCount();seriesNumber++) {
            String name = meta.getImageName(seriesNumber);

            boolean pass = true;
            for (FileCondition filter:filters) {
                if (name!= null && filter.test(new File(name))) {
                    pass = false;
                    break;
                }
            }

            if (pass)namesAndNumbers.put(seriesNumber+1,name);

        }

        reader.close();

        return namesAndNumbers;

    }

    private TreeMap<Integer,String> getSeriesListNumbers(File inputFile) throws DependencyException, ServiceException, IOException, FormatException {
        TreeMap<Integer,String> namesAndNumbers = new TreeMap<>();

        // Using BioFormats to get the number of series
        DebugTools.enableLogging("off");
        DebugTools.setRootLevel("off");

        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        OMEXMLMetadata meta = service.createOMEXMLMetadata();
        ImageProcessorReader reader = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
        reader.setMetadataStore((MetadataStore) meta);
        reader.setGroupFiles(false);
        reader.setId(inputFile.getAbsolutePath());

        SeriesListSelectorP seriesListSelectorP = parameters.getParameter(InputControl.SERIES_LIST);
        int[] seriesList = seriesListSelectorP.getSeriesList();
        for (int aSeriesList : seriesList) {
            namesAndNumbers.put(aSeriesList, meta.getImageName(aSeriesList - 1));
        }

        reader.close();

        return namesAndNumbers;

    }

    @Override
    public String getTitle() {
        return "Input control";
    }

    @Override
    public String getPackageName() {
        return "General";
    }

    @Override
    public String getHelp() {
        return "Select which file(s) or folder(s) MIA will process through.  If a file is selected, that file alone " +
                "will be processed; however, selecting a folder will cause the system to iterate over all files and " +
                "sub-folders within that folder.  Each file identified here will initialise its own workspace.  " +
                "<br><br>" +
                "It is possible to add filters to limit which files are used.  Multiple filters can be applied." +
                "<br><br>" +
                "n.b. This module simply creates the workspace for subsequent analysis; no images are automatically " +
                "loaded at this point.  To load image data to the workspace use the \"Load image\" module.";

    }

    @Override
    public boolean process(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(IMPORT_SEPARATOR,this));
        parameters.add(new FileFolderPathP(INPUT_PATH,this,"","The file or folder path to process.  If a file is selected, that file alone will be processed.  If a folder is selected, each file in that folder (and all sub-folders) passing the filters will be processed."));
        parameters.add(new IntegerP(SIMULTANEOUS_JOBS,this,1,"The number of images that will be processed simultaneously.  If this is set to \"1\" while processing a folder each valid file will still be processed, they will just complete one at a time.  For large images this is best left as \"1\" unless using a system with large amounts of RAM."));
        parameters.add(new ChoiceP(SERIES_MODE,this,SeriesModes.ALL_SERIES,SeriesModes.ALL,"For multi-series files, select which series to process.  \"All series\" will create a new workspace for each series in the file.  \"Series list (comma separated)\" allows a comma-separated list of series numbers to be specified."));
        parameters.add(new SeriesListSelectorP(SERIES_LIST,this,"1","Comma-separated list of series numbers to be processed."));

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new ChoiceP(FILTER_SOURCE,this,FilterSources.FILENAME,FilterSources.ALL,"Type of filter to add."));
        collection.add(new StringP(FILTER_VALUE,this,"","Value to filter filenames against."));
        collection.add(new ChoiceP(FILTER_TYPE,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL,"Control how the present filter operates.  \"Matches partially (include)\" will process an image if the filter value is partially present in the source (e.g. filename or extension).  \"Matches completely (include)\" will process an image if the filter value is exactly the same as the source.  \"Matches partially (include)\" will process an image if the filter value is partially present in the source.  \"Matches completely (exclude)\" will not process an image if the filter value is exactly the same as the source."));
        parameters.add(new ParameterGroup(ADD_FILTER,this,collection,"Add another filename filter.  All images to be processed will pass all filters."));

        parameters.add(new ChoiceP(SPATIAL_UNITS,this,SpatialUnits.MICROMETRE,SpatialUnits.ALL,"Spatial units for calibrated measurements.  Assuming spatial calibration can be read from the input file when loaded, this will convert the input calibrated units to the units specified here."));

        parameters.add(new MessageP(NO_LOAD_MESSAGE,this,"\"Input control\" only specifies the path to the root image; no image is loaded into the active workspace at this point.  To load images, add a \"Load Image\" module (multiple copies of this can be added to a single workflow).",Color.RED));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(IMPORT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_PATH));

        ChoiceP seriesMode = (ChoiceP) parameters.getParameter(SERIES_MODE);
        returnedParameters.add(seriesMode);
        switch (seriesMode.getChoice()) {
            case SeriesModes.SERIES_LIST:
                returnedParameters.add(parameters.getParameter(SERIES_LIST));
                break;
        }
        returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
        returnedParameters.add(parameters.getParameter(SIMULTANEOUS_JOBS));

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_FILTER));
        returnedParameters.add(parameters.getParameter(NO_LOAD_MESSAGE));

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
        metadataRefs.getOrPut(HCMetadata.FILE).setAvailable(true);
        metadataRefs.getOrPut(HCMetadata.FILENAME).setAvailable(true);
        metadataRefs.getOrPut(HCMetadata.SERIES_NUMBER).setAvailable(true);
        metadataRefs.getOrPut(HCMetadata.SERIES_NAME).setAvailable(true);

        return metadataRefs;

    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
