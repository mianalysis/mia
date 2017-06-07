package wbif.sjx.ModularImageAnalysis.Module.IO;

import wbif.sjx.ModularImageAnalysis.Extractor.Extractor;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;


/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends HCModule {
    public static final String FILENAME_EXTRACTOR = "Filename extractor";
    public static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";

    @Override
    public String getTitle() {
        return "Extract metadata";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting current result
        HCMetadata metadata = workspace.getMetadata();

        // Getting filename extractor
        Extractor filenameExtractor = parameters.getValue(FILENAME_EXTRACTOR);

        // Getting foldername extractor
        Extractor foldernameExtractor = parameters.getValue(FOLDERNAME_EXTRACTOR);

        // Preparing Result object
        metadata.setFile(workspace.getMetadata().getFile());
        foldernameExtractor.extract(metadata,metadata.getFile().getParent());
        filenameExtractor.extract(metadata,metadata.getFile().getName());

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(FILENAME_EXTRACTOR, HCParameter.OBJECT,null));
        parameters.addParameter(new HCParameter(FOLDERNAME_EXTRACTOR, HCParameter.OBJECT,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}


