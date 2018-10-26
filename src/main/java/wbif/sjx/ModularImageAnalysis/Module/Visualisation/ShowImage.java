package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

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
    public void run(Workspace workspace) {
        if (showOutput) {
            String imageName = parameters.getValue(DISPLAY_IMAGE);
            ImagePlus imageToShow = workspace.getImage(imageName).getImagePlus();
            imageToShow = new Duplicator().run(imageToShow);
            imageToShow.setTitle(imageName);

            IntensityMinMax.run(imageToShow, true, 0.001);
            imageToShow.show();
        }
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
