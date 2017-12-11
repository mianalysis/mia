package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ShowImage extends HCModule {
    public final static String DISPLAY_IMAGE = "Display image";

    @Override
    public String getTitle() {
        return "Show image";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        String imageName = parameters.getValue(DISPLAY_IMAGE);
        ImagePlus imageToShow = workspace.getImage(imageName).getImagePlus();
        imageToShow = new Duplicator().run(imageToShow);

        IntensityMinMax.run(imageToShow,imageToShow.getNSlices() > 1);
        imageToShow.show();

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(DISPLAY_IMAGE, Parameter.INPUT_IMAGE,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
