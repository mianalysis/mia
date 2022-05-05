package io.github.mianalysis.mia.module.images.configure;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.NormaliseIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.process.IntensityMinMax;

/**
 * Created by sc13967 on 10/08/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class SetDisplayRange extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RANGE_SEPARATOR = "Intensity range";
    public static final String CALCULATION_MODE = "Calculation mode";
    public static final String CLIP_FRACTION_MIN = "Clipping fraction (min)";
    public static final String CLIP_FRACTION_MAX = "Clipping fraction (max)";
    public static final String SET_MINIMUM_VALUE = "Set minimum value";
    public static final String SET_MAXIMUM_VALUE = "Set maximum value";
    public static final String MIN_RANGE = "Minimum range value";
    public static final String MAX_RANGE = "Maximum range value";

    public SetDisplayRange(Modules modules) {
        super("Set intensity display range", modules);
    }

    public interface CalculationModes {
        String FAST = "Fast";
        String MANUAL = "Manual";
        String PRECISE = "Precise";

        String[] ALL = new String[] { FAST, MANUAL, PRECISE };

    }

    public static void setDisplayRangeManual(ImagePlus ipl, String calculationMode, double[] intRange) {
        // Get min max values for whole stack
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            if (ipl.isHyperStack())
                ipl.setPosition(c, 1, 1);
            else
                ipl.setPosition(c);

            ipl.setDisplayRange(intRange[0], intRange[1]);

        }

        // Resetting location of the image
        ipl.setPosition(1, 1, 1);

    }

    public static void setDisplayRangeAuto(ImagePlus ipl, String calculationMode, double[] clipFraction,
            boolean[] setRange) {
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            // Get min max values for whole stack
            double[] intRange = new double[] { ipl.getDisplayRangeMin(), ipl.getDisplayRangeMax() };
            double[] newIntRange;

            switch (calculationMode) {
                case CalculationModes.FAST:
                default:
                    newIntRange = IntensityMinMax.getWeightedChannelRangeFast(ipl, c - 1, clipFraction[0],
                            clipFraction[1]);
                    break;

                case CalculationModes.PRECISE:
                    newIntRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl, c - 1, clipFraction[0],
                            clipFraction[1]);
                    break;
            }

            if (setRange[0])
                intRange[0] = newIntRange[0];
            if (setRange[1])
                intRange[1] = newIntRange[1];

            ipl.setDisplayRange(intRange[0], intRange[1]);

        }

        // Resetting location of the image
        ipl.setPosition(1, 1, 1);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_CONFIGURE;
    }

    @Override
    public String getDescription() {
        return "Set the minimum and maximum displayed intensities for a specified image from the workspace.  Any pixels with intensities outside the set displayed range will be rendered with the corresponding extreme value (i.e. any pixels with intensities less than the minimum display value will be shown with the same as the value at the minimum display value).  Display ranges can be calculated automatically or specified manually.  One or both extrema can be set at a time.<br><br>"
        
                + "Note: Unlike the \"" + new NormaliseIntensity(null).getName() + "\" module, pixel values are unchanged by this module.  The only change is to the way ImageJ/Fiji renders the image.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String calculationMode = parameters.getValue(CALCULATION_MODE);
        double clipFractionMin = parameters.getValue(CLIP_FRACTION_MIN);
        double clipFractionMax = parameters.getValue(CLIP_FRACTION_MAX);
        boolean setMinimumValue = parameters.getValue(SET_MINIMUM_VALUE);
        boolean setMaximumValue = parameters.getValue(SET_MAXIMUM_VALUE);
        double minRange = parameters.getValue(MIN_RANGE);
        double maxRange = parameters.getValue(MAX_RANGE);

        // If this image doesn't exist, skip this module. This returns true, because
        // this isn't terminal for the analysis.
        if (inputImage == null)
            return Status.PASS;

        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        // Setting ranges
        double[] clipFraction = new double[] { clipFractionMin, clipFractionMax };
        boolean[] setRange = new boolean[] { setMinimumValue, setMaximumValue };
        double[] manualRange = new double[] { minRange, maxRange };

        // Adjusting display range
        writeStatus("Adjusting display range");
        switch (calculationMode) {
            case CalculationModes.FAST:
            case CalculationModes.PRECISE:
                setDisplayRangeAuto(inputImagePlus, calculationMode, clipFraction, setRange);
                break;
            case CalculationModes.MANUAL:
                setDisplayRangeManual(inputImagePlus, calculationMode, manualRange);
                break;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage(outputImageName, null, false, true);

        } else {
            if (showOutput)
                inputImage.showImage(inputImageName, null, false, true);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.FAST, CalculationModes.ALL));
        parameters.add(new DoubleP(CLIP_FRACTION_MIN, this, 0d));
        parameters.add(new DoubleP(CLIP_FRACTION_MAX, this, 0d));
        parameters.add(new BooleanP(SET_MINIMUM_VALUE, this, true));
        parameters.add(new BooleanP(SET_MAXIMUM_VALUE, this, true));
        parameters.add(new DoubleP(MIN_RANGE, this, 0));
        parameters.add(new DoubleP(MAX_RANGE, this, 255));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        switch ((String) parameters.getValue(CALCULATION_MODE)) {
            case CalculationModes.FAST:
            case CalculationModes.PRECISE:
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION_MIN));
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION_MAX));
                returnedParameters.add(parameters.getParameter(SET_MINIMUM_VALUE));
                returnedParameters.add(parameters.getParameter(SET_MAXIMUM_VALUE));
                break;

            case CalculationModes.MANUAL:
                returnedParameters.add(parameters.getParameter(MIN_RANGE));
                returnedParameters.add(parameters.getParameter(MAX_RANGE));
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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace for which the updated intensity display range will be calculated.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the new intensity display range should be applied directly to the input image, or if it should be applied to a duplicate image, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "If storing the processed image separately in the workspace, this is the name of the output image.");

        parameters.get(CALCULATION_MODE).setDescription("Controls how the display range is calculated:<br><ul>"

                + "<li>\"" + CalculationModes.FAST
                + "\" All intensity values in the image are collected in a histogram.  As such, for 8 and 16-bit this is fast to calculate as there are a limited number of bins.  In this instance, the clip fraction corresponds to the fraction of bins.</li>"

                + "<li>\"" + CalculationModes.MANUAL
                + "\" The minimum and maximum displayed intensities are manually specified with the \"" + MIN_RANGE
                + "\" and \"" + MAX_RANGE + "\" parameters.</li>"

                + "<li>\"" + CalculationModes.PRECISE
                + "\" All intensity values are ordered by their intensity and the clip fraction corresponds to the fraction of pixels (rather than the fraction of unique intensities as with \""
                + CalculationModes.FAST
                + "\" mode.  As such, this method is more precise; however, can take a much longer time (especially for large images).</li></ul>");

        parameters.get(CLIP_FRACTION_MIN).setDescription("Fraction of unique intensities (\""+CalculationModes.PRECISE+"\") or pixels (\""+CalculationModes.FAST+"\") that are clipped when setting the minimum displayed intensity.  Any values below this will be displayed equally with the minimum value of the LUT.");

        parameters.get(CLIP_FRACTION_MAX).setDescription("Fraction of unique intensities (\""+CalculationModes.PRECISE+"\") or pixels (\""+CalculationModes.FAST+"\") that are clipped when setting the maximum displayed intensity.  Any values above this will be displayed equally with the maximum value of the LUT.");

        parameters.get(SET_MINIMUM_VALUE).setDescription("When selected, the minimum displayed intensity will be updated.  Otherwise, the value will be unchanged.");

        parameters.get(SET_MAXIMUM_VALUE).setDescription("When selected, the maximum displayed intensity will be updated.  Otherwise, the value will be unchanged.");

        parameters.get(MIN_RANGE).setDescription("If manually setting the minimum displayed intensity, this is the value that will be applied.");

        parameters.get(MAX_RANGE).setDescription("If manually setting the maximum displayed intensity, this is the value that will be applied.");

    }
}