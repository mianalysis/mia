package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.CompositeImage;
import ij.process.LUT;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Object.LUTs;

import java.awt.*;

public class SetLookupTable extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String LUT_SEPARATOR = "Lookup table selection";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String CHANNEL = "Channel";
    public static final String LOOKUP_TABLE = "Lookup table";
    public static final String DISPLAY_MODE = "Display mode";

    public SetLookupTable(ModuleCollection modules) {
        super("Set lookup table",modules);
    }


    public interface ChannelModes {
        String ALL_CHANNELS = "All channels";
        String SPECIFIC_CHANNELS = "Specific channels";

        String[] ALL = new String[]{ALL_CHANNELS,SPECIFIC_CHANNELS};

    }

    public interface DisplayModes {
        String FULL_RANGE = "Full range";
        String SET_ZERO_TO_BLACK = "Set zero to black";

        String[] ALL = new String[]{FULL_RANGE,SET_ZERO_TO_BLACK};

    }

    public interface LookupTables {
        String GREY = "Grey";
        String RED = "Red";
        String GREEN = "Green";
        String BLUE = "Blue";
        String CYAN = "Cyan";
        String MAGENTA = "Magenta";
        String YELLOW = "Yellow";
        String FIRE = "Fire";
        String ICE = "Ice";
        String SPECTRUM = "Spectrum";
        String THERMAL = "Thermal";
        String RANDOM = "Random";

        String[] ALL = new String[]{GREY,RED,GREEN,BLUE,CYAN,MAGENTA,YELLOW,FIRE,ICE,SPECTRUM,THERMAL,RANDOM};

    }


    public static LUT getLUT(String lookupTableName) {
        switch (lookupTableName) {
            case LookupTables.GREY:
            default:
                return LUT.createLutFromColor(Color.WHITE);
            case LookupTables.RED:
                return LUT.createLutFromColor(Color.RED);
            case LookupTables.GREEN:
                return LUT.createLutFromColor(Color.GREEN);
            case LookupTables.BLUE:
                return LUT.createLutFromColor(Color.BLUE);
            case LookupTables.CYAN:
                return LUT.createLutFromColor(Color.CYAN);
            case LookupTables.MAGENTA:
                return LUT.createLutFromColor(Color.MAGENTA);
            case LookupTables.YELLOW:
                return LUT.createLutFromColor(Color.YELLOW);
            case LookupTables.FIRE:
                return LUTs.BlackFire();
            case LookupTables.ICE:
                return LUTs.Ice();
            case LookupTables.SPECTRUM:
                return LUTs.Spectrum();
            case LookupTables.THERMAL:
                return LUTs.Thermal();
            case LookupTables.RANDOM:
                return LUTs.Random(true);
        }
    }

    public static LUT setZeroToBlack(LUT lut) {
        byte[] reds = new byte[lut.getMapSize()];
        byte[] greens = new byte[lut.getMapSize()];
        byte[] blues = new byte[lut.getMapSize()];

        lut.getReds(reds);
        lut.getGreens(greens);
        lut.getBlues(blues);

        reds[0] = 0;
        greens[0] = 0;
        blues[0] = 0;

        return new LUT(reds,greens,blues);

    }

    public static void setLUT(Image inputImage, LUT lut, String channelMode, int channel) {
        // Single channel images shouldn't be set to composite
        if (inputImage.getImagePlus().getNChannels() == 1) {
            inputImage.getImagePlus().setLut(lut);
            return;
        }

        switch (channelMode) {
            case ChannelModes.ALL_CHANNELS:
                for (int c=1;c<=inputImage.getImagePlus().getNChannels();c++) {
                    ((CompositeImage) inputImage.getImagePlus()).setChannelLut(lut,c);
                }
                break;

            case ChannelModes.SPECIFIC_CHANNELS:
                ((CompositeImage) inputImage.getImagePlus()).setChannelLut(lut,channel);
                break;
        }
    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String lookupTableName = parameters.getValue(LOOKUP_TABLE);
        String channelMode = parameters.getValue(CHANNEL_MODE);
        int channel = parameters.getValue(CHANNEL);
        String displayMode = parameters.getValue(DISPLAY_MODE);

        LUT lut = getLUT(lookupTableName);

        switch (displayMode) {
            case DisplayModes.SET_ZERO_TO_BLACK:
                lut = setZeroToBlack(lut);
                break;
        }

        setLUT(inputImage,lut,channelMode,channel);

        if (showOutput) inputImage.showImage(inputImageName,null,false,true);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this));

        parameters.add(new ParamSeparatorP(LUT_SEPARATOR,this));
        parameters.add(new ChoiceP(CHANNEL_MODE,this,ChannelModes.ALL_CHANNELS,ChannelModes.ALL));
        parameters.add(new IntegerP(CHANNEL,this,1));
        parameters.add(new ChoiceP(LOOKUP_TABLE,this,LookupTables.GREY,LookupTables.ALL));
        parameters.add(new ChoiceP(DISPLAY_MODE,this,DisplayModes.FULL_RANGE,DisplayModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(LUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHANNEL_MODE));

        switch ((String) parameters.getValue(CHANNEL_MODE)) {
            case ChannelModes.SPECIFIC_CHANNELS:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                break;
        }

        returnedParameters.add(parameters.getParameter(LOOKUP_TABLE));
        returnedParameters.add(parameters.getParameter(DISPLAY_MODE));

        return returnedParameters;

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
