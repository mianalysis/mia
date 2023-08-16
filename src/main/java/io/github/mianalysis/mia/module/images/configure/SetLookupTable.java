package io.github.mianalysis.mia.module.images.configure;

import java.awt.Color;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.LUT;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.imagej.LUTs;


/**
* Set look-up table (LUT) for an image or a specific channel of an image.  The look-up table determines what colour ImageJ will render each intensity value of an image.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SetLookupTable extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* Image to set look-up table for.
	*/
    public static final String INPUT_IMAGE = "Input image";


	/**
	* 
	*/
    public static final String LUT_SEPARATOR = "Lookup table selection";

	/**
	* Control if the same look-up table is applied to all channels, or just one:<br><br>- "All channels" Apply the same look-up table to all channels of the input image.<br><br>- "Specific channels" Only apply the look-up table to the channel specified by the "Channel" parameter.  All other channels will remain unaffected.<br>
	*/
    public static final String CHANNEL_MODE = "Channel mode";

	/**
	* When in "Specific channels" mode, this is the channel the look-up table will be applied to.  Channel numbering starts at 1.
	*/
    public static final String CHANNEL = "Channel";

	/**
	* 
	*/
    public static final String REFERENCE_IMAGE = "Reference image";

	/**
	* Look-up table to apply to the relevant channels.  Choices are: Grey, Red, Green, Blue, Cyan, Magenta, Yellow, Fire, Ice, Jet, Physics, Spectrum, Thermal, Random.
	*/
    public static final String LOOKUP_TABLE = "Lookup table";

	/**
	* Controls how the minimum value in the look-up table should be rendered:<br><br>- "Full range" Use the full colour range of the look-up table.  This is the default look-up table without modifications.<br><br>- "Set zero to black" Uses the standard look-up table, except for the lowest value, which is always set to black.  This is useful for cases where the background will be 0 or NaN and should be rendered as black.<br>
	*/
    public static final String DISPLAY_MODE = "Display mode";

    public SetLookupTable(Modules modules) {
        super("Set lookup table", modules);
    }

    public interface ChannelModes {
        String ALL_CHANNELS = "All channels";
        String COPY_FROM_IMAGE = "Copy from image";
        String SPECIFIC_CHANNELS = "Specific channels";

        String[] ALL = new String[] { ALL_CHANNELS, COPY_FROM_IMAGE, SPECIFIC_CHANNELS };

    }

    public interface DisplayModes {
        String FULL_RANGE = "Full range";
        String SET_ZERO_TO_BLACK = "Set zero to black";

        String[] ALL = new String[] { FULL_RANGE, SET_ZERO_TO_BLACK };

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
        String JET = "Jet";
        String PHYSICS = "Physics";
        String SPECTRUM = "Spectrum";
        String THERMAL = "Thermal";
        String RANDOM = "Random";

        String[] ALL = new String[] { GREY, RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW, FIRE, ICE, JET, PHYSICS, SPECTRUM,
                THERMAL, RANDOM };

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
            case LookupTables.JET:
                return LUTs.Jet();
            case LookupTables.PHYSICS:
                return LUTs.Physics();
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

        return new LUT(reds, greens, blues);

    }

    public static void setLUT(Image inputImage, LUT lut, String channelMode, int channel) {
        ImagePlus ipl = inputImage.getImagePlus();

        // Single channel images shouldn't be set to composite
        if (ipl.getNChannels() == 1 & !ipl.isComposite()) {
            ipl.setLut(lut);
            return;
        }

        // If necessary, making composite
        if (!ipl.isComposite()) {
            ipl = new CompositeImage(ipl, CompositeImage.COLOR);
            inputImage.setImagePlus(ipl);
        }

        switch (channelMode) {
            case ChannelModes.ALL_CHANNELS:
                for (int c = 1; c <= ipl.getNChannels(); c++)
                    ((CompositeImage) ipl).setChannelLut(lut, c);
                break;

            case ChannelModes.SPECIFIC_CHANNELS:
                ((CompositeImage) ipl).setChannelLut(lut, channel);
                break;
        }

        ipl.updateAndDraw();

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_CONFIGURE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Set look-up table (LUT) for an image or a specific channel of an image.  The look-up table determines what colour ImageJ will render each intensity value of an image.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String lookupTableName = parameters.getValue(LOOKUP_TABLE,workspace);
        String channelMode = parameters.getValue(CHANNEL_MODE,workspace);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE,workspace);
        int channel = parameters.getValue(CHANNEL,workspace);
        String displayMode = parameters.getValue(DISPLAY_MODE,workspace);

        // If this image doesn't exist, skip this module. This returns true, because
        // this isn't terminal for the analysis.
        if (inputImage == null)
            return Status.PASS;

        // If this image has fewer channels than the specified channel, skip the module
        // (but return true)
        if (channelMode.equals(ChannelModes.SPECIFIC_CHANNELS) && channel > inputImage.getImagePlus().getNChannels())
            return Status.PASS;

        switch (channelMode) {
            case ChannelModes.ALL_CHANNELS:
            case ChannelModes.SPECIFIC_CHANNELS:
                LUT lut = getLUT(lookupTableName);
                switch (displayMode) {
                    case DisplayModes.SET_ZERO_TO_BLACK:
                        lut = setZeroToBlack(lut);
                        break;
                }
                setLUT(inputImage, lut, channelMode, channel);
                break;
                case ChannelModes.COPY_FROM_IMAGE:
                    ImagePlus inputIpl = inputImage.getImagePlus();
                    LUT[] luts = workspace.getImage(referenceImageName).getImagePlus().getLuts();
                    for (int ch = 0; ch < Math.min(inputIpl.getNChannels(),luts.length); ch++)
                        setLUT(inputImage, luts[ch], ChannelModes.SPECIFIC_CHANNELS, ch+1);
                    
                break;
        }

        inputImage.getImagePlus().updateChannelAndDraw();

        if (showOutput)
            inputImage.showImage(inputImageName, null, false, true);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(LUT_SEPARATOR, this));
        parameters.add(new ChoiceP(CHANNEL_MODE, this, ChannelModes.ALL_CHANNELS, ChannelModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new IntegerP(CHANNEL, this, 1));
        parameters.add(new ChoiceP(LOOKUP_TABLE, this, LookupTables.GREY, LookupTables.ALL));
        parameters.add(new ChoiceP(DISPLAY_MODE, this, DisplayModes.FULL_RANGE, DisplayModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(LUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHANNEL_MODE));

        switch ((String) parameters.getValue(CHANNEL_MODE,workspace)) {
            case ChannelModes.ALL_CHANNELS:
                returnedParameters.add(parameters.getParameter(LOOKUP_TABLE));
                returnedParameters.add(parameters.getParameter(DISPLAY_MODE));
                break;
            case ChannelModes.COPY_FROM_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
            case ChannelModes.SPECIFIC_CHANNELS:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                returnedParameters.add(parameters.getParameter(LOOKUP_TABLE));
                returnedParameters.add(parameters.getParameter(DISPLAY_MODE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to set look-up table for.");

        parameters.get(CHANNEL_MODE)
                .setDescription("Control if the same look-up table is applied to all channels, or just one:<br>"

                        + "<br>- \"" + ChannelModes.ALL_CHANNELS
                        + "\" Apply the same look-up table to all channels of the input image.<br>"

                        + "<br>- \"" + ChannelModes.SPECIFIC_CHANNELS
                        + "\" Only apply the look-up table to the channel specified by the \"" + CHANNEL
                        + "\" parameter.  All other channels will remain unaffected.<br>");

        parameters.get(CHANNEL).setDescription("When in \"" + ChannelModes.SPECIFIC_CHANNELS
                + "\" mode, this is the channel the look-up table will be applied to.  Channel numbering starts at 1.");

        parameters.get(LOOKUP_TABLE).setDescription("Look-up table to apply to the relevant channels.  Choices are: "
                + String.join(", ", LookupTables.ALL) + ".");

        parameters.get(DISPLAY_MODE)
                .setDescription("Controls how the minimum value in the look-up table should be rendered:<br>"

                        + "<br>- \"" + DisplayModes.FULL_RANGE
                        + "\" Use the full colour range of the look-up table.  This is the default look-up table without modifications.<br>"

                        + "<br>- \"" + DisplayModes.SET_ZERO_TO_BLACK
                        + "\" Uses the standard look-up table, except for the lowest value, which is always set to black.  This is useful for cases where the background will be 0 or NaN and should be rendered as black.<br>");

    }
}
