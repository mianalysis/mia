package wbif.sjx.MIA.Module.Visualisation;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ShowImage extends Module {
    public final static String DISPLAY_IMAGE = "Display image";
    public static final String TITLE_MODE = "Title mode";
    public static final String QUICK_NORMALISATION = "Quick normalisation";
    public static final String CHANNEL_MODE = "Channel mode";


    public interface TitleModes {
        String FILE_NAME = "Filename";
        String IMAGE_NAME = "Image name";
        String IMAGE_AND_FILE_NAME = "Image and filename";

        String[] ALL = new String[]{FILE_NAME,IMAGE_NAME,IMAGE_AND_FILE_NAME};

    }

    public interface ChannelModes {
        String COLOUR = "Colour (separate channels)";
        String COMPOSITE = "Composite";

        String[] ALL = new String[]{COLOUR,COMPOSITE};

    }


    public ShowImage(ModuleCollection modules) {
        super(modules);

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
        boolean normalisation = parameters.getValue(QUICK_NORMALISATION);
        String channelMode = parameters.getValue(CHANNEL_MODE);

        boolean composite = channelMode.equals(ChannelModes.COMPOSITE);

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

        if (showOutput) image.showImage(title,null,normalisation,composite);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(DISPLAY_IMAGE, this));
        parameters.add(new ChoiceP(TITLE_MODE,this,TitleModes.IMAGE_NAME,TitleModes.ALL));
        parameters.add(new BooleanP(QUICK_NORMALISATION,this,true));
        parameters.add(new ChoiceP(CHANNEL_MODE,this,ChannelModes.COMPOSITE,ChannelModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
