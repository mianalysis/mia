package io.github.mianalysis.mia.module.images.configure;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.WordUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.LutLoader;
import ij.process.LUT;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.WavelengthToColorConverter;

/**
 * Set look-up table (LUT) for an image or a specific channel of an image. The
 * look-up table determines what colour ImageJ will render each intensity value
 * of an image.
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
     * Control if the same look-up table is applied to all channels, or just
     * one:<br>
     * <br>
     * - "All channels" Apply the same look-up table to all channels of the input
     * image.<br>
     * <br>
     * - "Specific channels" Only apply the look-up table to the channel specified
     * by the "Channel" parameter. All other channels will remain unaffected.<br>
     */
    public static final String CHANNEL_MODE = "Channel mode";

    /**
     * When in "Specific channels" mode, this is the channel the look-up table will
     * be applied to. Channel numbering starts at 1.
     */
    public static final String CHANNEL = "Channel";

    /**
    * 
    */
    public static final String REFERENCE_IMAGE = "Reference image";

    /**
     * Look-up table to apply to the relevant channels. Choices are: Grey, Red,
     * Green, Blue, Cyan, Magenta, Yellow, Fire, Ice, Jet, Physics, Spectrum,
     * Thermal, Random.
     */
    public static final String LOOKUP_TABLE = "Lookup table";

    public static final String RED_COMPONENT = "Red component";

    public static final String GREEN_COMPONENT = "Green component";

    public static final String BLUE_COMPONENT = "Blue component";

    public static final String WAVELENGTH = "Wavelength (nm)";

    /**
     * Controls how the minimum value in the look-up table should be rendered:<br>
     * <br>
     * - "Full range" Use the full colour range of the look-up table. This is the
     * default look-up table without modifications.<br>
     * <br>
     * - "Set zero to black" Uses the standard look-up table, except for the lowest
     * value, which is always set to black. This is useful for cases where the
     * background will be 0 or NaN and should be rendered as black.<br>
     */
    public static final String DISPLAY_MODE = "Display mode";

    public static final String INVERT_LUT = "Invert LUT";

    TreeMap<String, String> imageJLUTs;
    // TreeMap<String, String> allLUTs;

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
        String FROM_RGB = "From RGB";
        String FROM_WAVELENGTH = "From wavelength";
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
        String RANDOM = "Random";
        String SPECTRUM = "Spectrum";
        String THERMAL = "Thermal";

        String[] ALL = new String[] { FROM_RGB, FROM_WAVELENGTH, GREY, RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW, FIRE,
                ICE, JET,
                PHYSICS, RANDOM, SPECTRUM, THERMAL };

    }

    public static LUT getLUT(String lookupTableName, double wavelengthNM, @Nullable double[] rgbComponents) {
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
            case LookupTables.FROM_RGB:
                float r = (float) (rgbComponents[0]);
                float g = (float) (rgbComponents[1]);
                float b = (float) (rgbComponents[2]);
                Color colour = new Color(r,g,b);
                return LUT.createLutFromColor(colour);
            case LookupTables.FROM_WAVELENGTH:
                colour = WavelengthToColorConverter.convert(wavelengthNM);
                return LUT.createLutFromColor(colour);
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

    public static void copyLUTFromImage(Image inputImage, Image referenceImage) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        LUT[] luts = referenceImage.getImagePlus().getLuts();
        for (int ch = 0; ch < Math.min(inputIpl.getNChannels(), luts.length); ch++)
            setLUT(inputImage, luts[ch], ChannelModes.SPECIFIC_CHANNELS, ch + 1);

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
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String lookupTableName = parameters.getValue(LOOKUP_TABLE, workspace);
        String channelMode = parameters.getValue(CHANNEL_MODE, workspace);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
        int channel = parameters.getValue(CHANNEL, workspace);
        String displayMode = parameters.getValue(DISPLAY_MODE, workspace);
        double redComponent = parameters.getValue(RED_COMPONENT, workspace);
        double greenComponent = parameters.getValue(GREEN_COMPONENT, workspace);
        double blueComponent = parameters.getValue(BLUE_COMPONENT, workspace);
        double wavelengthNM = parameters.getValue(WAVELENGTH, workspace);
        boolean invertLUT = parameters.getValue(INVERT_LUT, workspace);

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
                LUT lut;
                if (imageJLUTs.keySet().contains(lookupTableName))
                    lut = LutLoader.openLut(IJ.getDir("luts") + lookupTableName + ".lut");
                else
                    lut = getLUT(lookupTableName, wavelengthNM,
                            new double[] { redComponent, greenComponent, blueComponent });

                switch (displayMode) {
                    case DisplayModes.SET_ZERO_TO_BLACK:
                        lut = setZeroToBlack(lut);
                        break;
                }

                if (invertLUT)
                    lut = lut.createInvertedLut();

                setLUT(inputImage, lut, channelMode, channel);

                break;
            case ChannelModes.COPY_FROM_IMAGE:
                Image referenceImage = workspace.getImage(referenceImageName);
                copyLUTFromImage(inputImage, referenceImage);
                break;
        }

        inputImage.getImagePlus().updateChannelAndDraw();

        if (showOutput)
            inputImage.show(inputImageName, null, false, Image.DisplayModes.COMPOSITE);

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

        TreeSet<String> allLUTs = new TreeSet<>();
        imageJLUTs = new TreeMap<>();

        String[] lutFiles = new File(IJ.getDirectory("luts")).list();
        if (lutFiles != null)
            Arrays.stream(lutFiles).forEach(v -> imageJLUTs.put(WordUtils.capitalize(v.replace(".lut", "")), v));

        allLUTs.addAll(imageJLUTs.keySet());
        Arrays.stream(LookupTables.ALL).forEach(v -> allLUTs.add(v));

        parameters.add(new ChoiceP(LOOKUP_TABLE, this, LookupTables.GREY,
                allLUTs.toArray(new String[allLUTs.size()])));
        parameters.add(new DoubleP(RED_COMPONENT, this, 1));
        parameters.add(new DoubleP(GREEN_COMPONENT, this, 1));
        parameters.add(new DoubleP(BLUE_COMPONENT, this, 1));
        parameters.add(new DoubleP(WAVELENGTH, this, 405));
        parameters.add(new ChoiceP(DISPLAY_MODE, this, DisplayModes.FULL_RANGE, DisplayModes.ALL));
        parameters.add(new BooleanP(INVERT_LUT, this, false));

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

        switch ((String) parameters.getValue(CHANNEL_MODE, workspace)) {
            case ChannelModes.ALL_CHANNELS:
                returnedParameters.add(parameters.getParameter(LOOKUP_TABLE));
                switch ((String) parameters.getValue(LOOKUP_TABLE, workspace)) {
                    case LookupTables.FROM_RGB:
                        returnedParameters.add(parameters.getParameter(RED_COMPONENT));
                        returnedParameters.add(parameters.getParameter(GREEN_COMPONENT));
                        returnedParameters.add(parameters.getParameter(BLUE_COMPONENT));
                        break;

                    case LookupTables.FROM_WAVELENGTH:
                        returnedParameters.add(parameters.getParameter(WAVELENGTH));
                        break;
                }
                returnedParameters.add(parameters.getParameter(DISPLAY_MODE));
                returnedParameters.add(parameters.getParameter(INVERT_LUT));
                break;
            case ChannelModes.COPY_FROM_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
            case ChannelModes.SPECIFIC_CHANNELS:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                returnedParameters.add(parameters.getParameter(LOOKUP_TABLE));
                if (((String) parameters.getValue(LOOKUP_TABLE, workspace)).equals(LookupTables.FROM_WAVELENGTH))
                    returnedParameters.add(parameters.getParameter(WAVELENGTH));
                returnedParameters.add(parameters.getParameter(DISPLAY_MODE));
                returnedParameters.add(parameters.getParameter(INVERT_LUT));
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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
