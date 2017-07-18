package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.*;
import wbif.sjx.common.Object.HCMetadata;


/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends HCModule {
    public static final String FILENAME_EXTRACTOR = "Filename extractor";
    public static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";

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

        // Getting filename extractor
        String filenameExtractorName = parameters.getValue(FILENAME_EXTRACTOR);
        Extractor filenameExtractor = null;
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

        // Getting foldername extractor
        String foldernameExtractorName = parameters.getValue(FOLDERNAME_EXTRACTOR);
        Extractor foldernameExtractor = null;
        switch (foldernameExtractorName) {
            case CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                foldernameExtractor = new CellVoyagerFoldernameExtractor();
                break;

            case OPERA_FOLDERNAME_EXTRACTOR:
                foldernameExtractor = new OperaFoldernameExtractor();
                break;
        }

        // Preparing Result object
        metadata.setFile(workspace.getMetadata().getFile());
        if (foldernameExtractor != null) foldernameExtractor.extract(metadata,metadata.getFile().getParent());
        if (filenameExtractor != null) filenameExtractor.extract(metadata,metadata.getFile().getName());

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(FILENAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FILENAME_EXTRACTORS[0],FILENAME_EXTRACTORS));
        parameters.addParameter(new Parameter(FOLDERNAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FOLDERNAME_EXTRACTORS[0],FOLDERNAME_EXTRACTORS));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}


