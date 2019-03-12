package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

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
        return "";
    }

    @Override
    public boolean run(Workspace workspace) {
        String imageName = parameters.getValue(DISPLAY_IMAGE);
        Image image = workspace.getImage(imageName);

        if (showOutput) image.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(DISPLAY_IMAGE, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
