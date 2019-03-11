package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

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
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;
import wbif.sjx.common.FileConditions.FileCondition;
import wbif.sjx.common.FileConditions.NameContainsString;
import wbif.sjx.common.FileConditions.ParentContainsString;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Stephen on 29/07/2017.
 */
public class InputControl extends Module {
    public static final String INPUT_PATH = "Input path";
    public static final String SIMULTANEOUS_JOBS = "Simultaneous jobs";
    public static final String FILE_EXTENSION = "File extension";
    public static final String SERIES_MODE = "Series mode";
    public static final String SERIES_LIST = "Series list";
    public static final String ADD_FILTER = "Add filter";
    public static final String FILTER_SOURCE = "Filter source";
    public static final String FILTER_VALUE = "Filter value";
    public static final String FILTER_TYPE = "Filter type";
    public static final String SPATIAL_UNITS = "Spatial units";


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
        String FILENAME = "Filename";
        String FILEPATH = "Filepath";
        String SERIESNAME = "Seriesname";

        String[] ALL = new String[]{FILENAME,FILEPATH,SERIESNAME};

    }

    public static interface FilterTypes {
        String INCLUDE_MATCHES_PARTIALLY = "Matches partially (include)";
        String INCLUDE_MATCHES_COMPLETELY = "Matches completely (include)";
        String EXCLUDE_MATCHES_PARTIALLY = "Matches partially (exclude)";
        String EXCLUDE_MATCHES_COMPLETELY = "Matches completely (exclude)";

        String[] ALL = new String[]{INCLUDE_MATCHES_PARTIALLY,INCLUDE_MATCHES_COMPLETELY,EXCLUDE_MATCHES_PARTIALLY,EXCLUDE_MATCHES_COMPLETELY};

    }

    public static interface SpatialUnits extends Units.SpatialUnits{}


    public void addFilenameExtensionFilter(BatchProcessor batchProcessor) {
        String extension = parameters.getValue(FILE_EXTENSION);

        // Adding extension filter
        batchProcessor.addFileCondition(new ExtensionMatchesString(new String[]{extension}));

    }

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
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new FileFolderPathP(INPUT_PATH,this));
        parameters.add(new IntegerP(SIMULTANEOUS_JOBS,this,1));
        parameters.add(new StringP(FILE_EXTENSION,this,"tif"));
        parameters.add(new ChoiceP(SERIES_MODE,this,SeriesModes.ALL_SERIES,SeriesModes.ALL));
        parameters.add(new SeriesListSelectorP(SERIES_LIST,this,"1"));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new ChoiceP(FILTER_SOURCE,this,FilterSources.FILENAME,FilterSources.ALL));
        collection.add(new StringP(FILTER_VALUE,this,""));
        collection.add(new ChoiceP(FILTER_TYPE,this,FilterTypes.INCLUDE_MATCHES_PARTIALLY,FilterTypes.ALL));
        parameters.add(new ParameterGroup(ADD_FILTER,this,collection));

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
            case SeriesModes.SERIES_LIST:
                returnedParameters.add(parameters.getParameter(SERIES_LIST));
                break;
        }

        returnedParameters.add(parameters.getParameter(ADD_FILTER));
        returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
        returnedParameters.add(parameters.getParameter(SIMULTANEOUS_JOBS));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
