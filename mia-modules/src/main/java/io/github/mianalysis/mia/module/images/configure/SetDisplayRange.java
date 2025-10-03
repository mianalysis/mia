package io.github.mianalysis.mia.module.images.configure;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.NormaliseIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
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
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.imagej.IntensityMinMax;

/**
 * Created by sc13967 on 10/08/2017.
 */

/**
 * Set the minimum and maximum displayed intensities for a specified image from
 * the workspace. Any pixels with intensities outside the set displayed range
 * will be rendered with the corresponding extreme value (i.e. any pixels with
 * intensities less than the minimum display value will be shown with the same
 * as the value at the minimum display value). Display ranges can be calculated
 * automatically or specified manually. One or both extrema can be set at a
 * time.<br>
 * <br>
 * Note: Unlike the "Normalise intensity" module, pixel values are unchanged by
 * this module. The only change is to the way ImageJ/Fiji renders the image.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SetDisplayRange extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
     * Image from the workspace for which the updated intensity display range will
     * be calculated.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Select if the new intensity display range should be applied directly to the
     * input image, or if it should be applied to a duplicate image, then stored as
     * a different image in the workspace.
     */
    public static final String APPLY_TO_INPUT = "Apply to input image";

    /**
     * If storing the processed image separately in the workspace, this is the name
     * of the output image.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String RANGE_SEPARATOR = "Intensity range";

    /**
     * Controls how the display range is calculated:<br>
     * <ul>
     * <li>"Fast" All intensity values in the image are collected in a histogram. As
     * such, for 8 and 16-bit this is fast to calculate as there are a limited
     * number of bins. In this instance, the clip fraction corresponds to the
     * fraction of bins.</li>
     * <li>"Manual" The minimum and maximum displayed intensities are manually
     * specified with the "Minimum range value" and "Maximum range value"
     * parameters.</li>
     * <li>"Precise" All intensity values are ordered by their intensity and the
     * clip fraction corresponds to the fraction of pixels (rather than the fraction
     * of unique intensities as with "Fast" mode. As such, this method is more
     * precise; however, can take a much longer time (especially for large
     * images).</li>
     * </ul>
     */
    public static final String CALCULATION_MODE = "Calculation mode";

    /**
     * This parameter controls whether the display range (min, max) will be
     * determined from the input image or another image:<br>
     * <ul>
     * <li>"External" The image for which the display range is calculated is
     * different to the image that the final display will be applied to. For
     * example, this could be a single representative slice or substack. Using this
     * could significantly reduce run-time for large stacks, especially when
     * "Calculation mode" is set to "Precise".</li>
     * <li>"Internal" The display range will be determined from the same image or
     * stack onto which the display will be applied.</li>
     * </ul>
     */
    public static final String CALCULATION_SOURCE = "Calculation source";

    /**
    * 
    */
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CLIP_FRACTION_MIN = "Clipping fraction (min)";
    public static final String CLIP_FRACTION_MAX = "Clipping fraction (max)";

    /**
     * When selected, the minimum displayed intensity will be updated. Otherwise,
     * the value will be unchanged.
     */
    public static final String SET_MINIMUM_VALUE = "Set minimum value";

    /**
     * When selected, the maximum displayed intensity will be updated. Otherwise,
     * the value will be unchanged.
     */
    public static final String SET_MAXIMUM_VALUE = "Set maximum value";

    /**
     * If manually setting the minimum displayed intensity, this is the value that
     * will be applied.
     */
    public static final String MIN_RANGE = "Minimum range value";

    /**
     * If manually setting the maximum displayed intensity, this is the value that
     * will be applied.
     */
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

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public static void setDisplayRangeManual(ImageI image, double[] intRange) {
        setDisplayRangeManual(image.getImagePlus(), intRange);
    }

    public static void setDisplayRangeManual(ImagePlus ipl, double[] intRange) {
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

    public static void setDisplayRangeAuto(ImageI image, String calculationMode, double[] clipFraction,
            boolean[] setRange, @Nullable ImageI externalImage) {
        setDisplayRangeAuto(image.getImagePlus(), calculationMode, clipFraction, setRange,
                externalImage.getImagePlus());
    }

    public static void setDisplayRangeAuto(ImagePlus ipl, String calculationMode, double[] clipFraction,
            boolean[] setRange, @Nullable ImagePlus externalIpl) {
        // If externalIpl is null, use the input image for calculation
        if (externalIpl == null)
            externalIpl = ipl;

        for (int c = 1; c <= ipl.getNChannels(); c++) {
            // Get min max values for whole stack
            double[] intRange = new double[] { ipl.getDisplayRangeMin(), ipl.getDisplayRangeMax() };
            double[] newIntRange;

            switch (calculationMode) {
                case CalculationModes.FAST:
                default:
                    newIntRange = IntensityMinMax.getWeightedChannelRangeFast(externalIpl, c - 1, clipFraction[0],
                            clipFraction[1]);
                    break;

                case CalculationModes.PRECISE:
                    newIntRange = IntensityMinMax.getWeightedChannelRangePrecise(externalIpl, c - 1, clipFraction[0],
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
    public String getVersionNumber() {
        return "1.0.1";
    }

    @Override
    public String getDescription() {
        return "Set the minimum and maximum displayed intensities for a specified image from the workspace.  Any pixels with intensities outside the set displayed range will be rendered with the corresponding extreme value (i.e. any pixels with intensities less than the minimum display value will be shown with the same as the value at the minimum display value).  Display ranges can be calculated automatically or specified manually.  One or both extrema can be set at a time.<br><br>"

                + "Note: Unlike the \"" + new NormaliseIntensity(null).getName()
                + "\" module, pixel values are unchanged by this module.  The only change is to the way ImageJ/Fiji renders the image.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE, workspace);
        String externalImageName = parameters.getValue(EXTERNAL_SOURCE, workspace);
        String calculationMode = parameters.getValue(CALCULATION_MODE, workspace);
        double clipFractionMin = parameters.getValue(CLIP_FRACTION_MIN, workspace);
        double clipFractionMax = parameters.getValue(CLIP_FRACTION_MAX, workspace);
        boolean setMinimumValue = parameters.getValue(SET_MINIMUM_VALUE, workspace);
        boolean setMaximumValue = parameters.getValue(SET_MAXIMUM_VALUE, workspace);
        double minRange = parameters.getValue(MIN_RANGE, workspace);
        double maxRange = parameters.getValue(MAX_RANGE, workspace);

        // Getting input image
        ImageI inputImage = workspace.getImages().get(inputImageName);

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
                ImagePlus externalIpl = calculationSource.equals(CalculationSources.EXTERNAL)
                        ? workspace.getImage(externalImageName).getImagePlus()
                        : null;
                setDisplayRangeAuto(inputImagePlus, calculationMode, clipFraction, setRange, externalIpl);
                break;
            case CalculationModes.MANUAL:
                setDisplayRangeManual(inputImagePlus, manualRange);
                break;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
            ImageI outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show(outputImageName, null, false, ImageI.DisplayModes.COMPOSITE, null);

        } else {
            if (showOutput)
                inputImage.show(inputImageName, null, false, ImageI.DisplayModes.COMPOSITE, null);

        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RANGE_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.FAST, CalculationModes.ALL));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));
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
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));

        if (!((String) parameters.getValue(CALCULATION_MODE, workspace)).equals(CalculationModes.MANUAL)) {
            returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
            switch ((String) parameters.getValue(CALCULATION_SOURCE, workspace)) {
                case CalculationSources.EXTERNAL:
                    returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                    break;
            }
        }

        switch ((String) parameters.getValue(CALCULATION_MODE, workspace)) {
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
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace for which the updated intensity display range will be calculated.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the new intensity display range should be applied directly to the input image, or if it should be applied to a duplicate image, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "If storing the processed image separately in the workspace, this is the name of the output image.");

        parameters.get(CALCULATION_SOURCE).setDescription(
                "This parameter controls whether the display range (min, max) will be determined from the input image or another image:<br><ul>"

                        + "<li>\"" + CalculationSources.EXTERNAL
                        + "\" The image for which the display range is calculated is different to the image that the final display will be applied to.  For example, this could be a single representative slice or substack.  Using this could significantly reduce run-time for large stacks, especially when \""
                        + CALCULATION_MODE + "\" is set to \"" + CalculationModes.PRECISE + "\".</li>"

                        + "<li>\"" + CalculationSources.INTERNAL
                        + "\" The display range will be determined from the same image or stack onto which the display will be applied.</li></ul>");

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

        parameters.get(CLIP_FRACTION_MIN).setDescription("Fraction of unique intensities (\"" + CalculationModes.PRECISE
                + "\") or pixels (\"" + CalculationModes.FAST
                + "\") that are clipped when setting the minimum displayed intensity.  Any values below this will be displayed equally with the minimum value of the LUT.");

        parameters.get(CLIP_FRACTION_MAX).setDescription("Fraction of unique intensities (\"" + CalculationModes.PRECISE
                + "\") or pixels (\"" + CalculationModes.FAST
                + "\") that are clipped when setting the maximum displayed intensity.  Any values above this will be displayed equally with the maximum value of the LUT.");

        parameters.get(SET_MINIMUM_VALUE).setDescription(
                "When selected, the minimum displayed intensity will be updated.  Otherwise, the value will be unchanged.");

        parameters.get(SET_MAXIMUM_VALUE).setDescription(
                "When selected, the maximum displayed intensity will be updated.  Otherwise, the value will be unchanged.");

        parameters.get(MIN_RANGE).setDescription(
                "If manually setting the minimum displayed intensity, this is the value that will be applied.");

        parameters.get(MAX_RANGE).setDescription(
                "If manually setting the maximum displayed intensity, this is the value that will be applied.");

    }
}
