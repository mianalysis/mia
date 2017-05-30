package wbif.sjx.HighContent.Module;

import wbif.sjx.HighContent.Object.*;

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
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        HCName imageName = parameters.getValue(DISPLAY_IMAGE);

        workspace.getImages().get(imageName).getImagePlus().show();

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(DISPLAY_IMAGE, HCParameter.INPUT_IMAGE,null));

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
