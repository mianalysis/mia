package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.*;
import wbif.sjx.common.Object.HCMetadata;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends HCModule {
    public static final String EXTRACTOR_MODE = "Extractor mode";
    private static final String FILENAME_EXTRACTOR = "Filename extractor";
    private static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";
    private static final String METADATA_FILE_EXTRACTOR = "Metadata file extractor";

    private static final String FILENAME_MODE = "Filename";
    private static final String FOLDERNAME_MODE = "Foldername";
    private static final String METADATA_FILE_MODE = "Metadata file";
    private static final String[] EXTRACTOR_MODES = new String[]{FILENAME_MODE,FOLDERNAME_MODE,METADATA_FILE_MODE};

    private static final String CELLVOYAGER_FILENAME_EXTRACTOR = "Cell Voyager filename";
    private static final String INCUCYTE_LONG_FILENAME_EXTRACTOR = "IncuCyte long filename";
    private static final String INCUCYTE_SHORT_FILENAME_EXTRACTOR = "IncuCyte short filename";
    private static final String OPERA_FILENAME_EXTRACTOR = "Opera filename";
    private static final String[] FILENAME_EXTRACTORS = new String[]{"None",CELLVOYAGER_FILENAME_EXTRACTOR,
            INCUCYTE_LONG_FILENAME_EXTRACTOR,INCUCYTE_SHORT_FILENAME_EXTRACTOR,OPERA_FILENAME_EXTRACTOR};

    private static final String CELLVOYAGER_FOLDERNAME_EXTRACTOR = "Cell Voyager foldername";
    private static final String OPERA_FOLDERNAME_EXTRACTOR = "Opera foldername";
    private static final String[] FOLDERNAME_EXTRACTORS = new String[]{"None",CELLVOYAGER_FOLDERNAME_EXTRACTOR,
            OPERA_FOLDERNAME_EXTRACTOR};

    private static final String OPERA_METADATA_FILE_EXTRACTOR = "Opera file (.flex)";
    private static final String[] METADATA_FILE_EXTRACTORS = new String[]{"None",OPERA_METADATA_FILE_EXTRACTOR};

    @Override
    public String getTitle() {
        return "Extract metadata";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting current result
        HCMetadata metadata = workspace.getMetadata();

        // Getting parameters
        String metadataExtractorMode = parameters.getValue(EXTRACTOR_MODE);

        switch (metadataExtractorMode) {
            case FILENAME_MODE:
                // Getting filename extractor
                String filenameExtractorName = parameters.getValue(FILENAME_EXTRACTOR);
                NameExtractor filenameExtractor = null;

                switch (filenameExtractorName) {
                    case CELLVOYAGER_FILENAME_EXTRACTOR:
                        filenameExtractor = new CellVoyagerFilenameExtractor();
                        break;

                    case INCUCYTE_LONG_FILENAME_EXTRACTOR:
                        filenameExtractor = new IncuCyteLongFilenameExtractor();
                        break;

                    case INCUCYTE_SHORT_FILENAME_EXTRACTOR:
                        filenameExtractor = new IncuCyteShortFilenameExtractor();
                        break;

                    case OPERA_FILENAME_EXTRACTOR:
                        filenameExtractor = new OperaFilenameExtractor();
                        break;

                }

                if (filenameExtractor != null) filenameExtractor.extract(metadata, metadata.getFile().getName());
                break;

            case FOLDERNAME_MODE:
                // Getting folder name extractor
                String foldernameExtractorName = parameters.getValue(FOLDERNAME_EXTRACTOR);
                NameExtractor foldernameExtractor = null;
                switch (foldernameExtractorName) {
                    case CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                        foldernameExtractor = new CellVoyagerFoldernameExtractor();
                        break;

                    case OPERA_FOLDERNAME_EXTRACTOR:
                        foldernameExtractor = new OperaFoldernameExtractor();
                        break;
                }

                if (foldernameExtractor != null) foldernameExtractor.extract(metadata,metadata.getFile().getParent());
                break;

            case METADATA_FILE_MODE:
                // Getting metadata file extractor
                String metadataFileExtractorName = parameters.getValue(METADATA_FILE_EXTRACTOR);
                FileExtractor metadataFileExtractor = null;
                switch (metadataFileExtractorName) {
                    case OPERA_METADATA_FILE_EXTRACTOR:
                        metadataFileExtractor = new OperaFileExtractor();
                        break;
                }

                if (metadataFileExtractor != null) metadataFileExtractor.extract(metadata,metadata.getFile());
                break;

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(EXTRACTOR_MODE,Parameter.CHOICE_ARRAY,EXTRACTOR_MODES[0],EXTRACTOR_MODES));
        parameters.addParameter(new Parameter(FILENAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FILENAME_EXTRACTORS[0],FILENAME_EXTRACTORS));
        parameters.addParameter(new Parameter(FOLDERNAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FOLDERNAME_EXTRACTORS[0],FOLDERNAME_EXTRACTORS));
        parameters.addParameter(new Parameter(METADATA_FILE_EXTRACTOR,Parameter.CHOICE_ARRAY,METADATA_FILE_EXTRACTORS[0],METADATA_FILE_EXTRACTORS));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(EXTRACTOR_MODE));

        if (parameters.getValue(EXTRACTOR_MODE).equals(FILENAME_MODE)) {
            returnedParameters.addParameter(parameters.getParameter(FILENAME_EXTRACTOR));

        } else if (parameters.getValue(EXTRACTOR_MODE).equals(FOLDERNAME_MODE)) {
            returnedParameters.addParameter(parameters.getParameter(FOLDERNAME_EXTRACTOR));

        }if (parameters.getValue(EXTRACTOR_MODE).equals(METADATA_FILE_MODE)) {
            returnedParameters.addParameter(parameters.getParameter(METADATA_FILE_EXTRACTOR));

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}


