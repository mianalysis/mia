package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ShowImage extends Module {
    public final static String DISPLAY_IMAGE = "Display image";
    public static final String TITLE_MODE = "Title mode";

    public interface TitleModes {
        String FILE_NAME = "Filename";
        String IMAGE_NAME = "Image name";
        String IMAGE_AND_FILE_NAME = "Image and filename";

        String[] ALL = new String[]{FILE_NAME,IMAGE_NAME,IMAGE_AND_FILE_NAME};

    }

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
    public boolean process(Workspace workspace) {
        String imageName = parameters.getValue(DISPLAY_IMAGE);
        Image image = workspace.getImage(imageName);
        String titleMode = parameters.getValue(TITLE_MODE);

        String title = "";
        switch (titleMode) {
            case TitleModes.FILE_NAME:
                title = workspace.getMetadata().getFilename();
                break;
            case TitleModes.IMAGE_NAME:
                title = imageName;
                break;
            case TitleModes.IMAGE_AND_FILE_NAME:
                title = workspace.getMetadata().getFilename()+"_"+imageName;
                break;
        }

        if (showOutput) image.showImage(title);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(DISPLAY_IMAGE, this));
        parameters.add(new ChoiceP(TITLE_MODE,this,TitleModes.IMAGE_NAME,TitleModes.ALL));

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
    public MetadataRefCollection updateAndGetImageMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
