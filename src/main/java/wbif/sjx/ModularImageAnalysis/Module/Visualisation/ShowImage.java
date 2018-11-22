package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ShowImage extends Module {
    public final static String DISPLAY_IMAGE = "Display image";

    public ShowImage() {
        // This module likely wants to have this enabled (otherwise it does nothing)
        showOutput = true;
    }

    @Override
    public String getTitle() {
        return "Show image";

    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        String imageName = parameters.getValue(DISPLAY_IMAGE);
        Image image = workspace.getImage(imageName);

        if (showOutput) showImage(image);

        return true;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(DISPLAY_IMAGE, Parameter.INPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
