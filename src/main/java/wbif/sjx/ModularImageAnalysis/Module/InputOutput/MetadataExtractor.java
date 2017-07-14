package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.Extractor;
import wbif.sjx.common.Object.HCMetadata;


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
    public void execute(Workspace workspace, boolean verbose) {
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

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(FILENAME_EXTRACTOR, Parameter.OBJECT,null));
        parameters.addParameter(new Parameter(FOLDERNAME_EXTRACTOR, Parameter.OBJECT,null));

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


