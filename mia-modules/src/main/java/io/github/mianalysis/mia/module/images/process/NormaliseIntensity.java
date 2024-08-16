package io.github.mianalysis.mia.module.images.process;

import java.awt.Color;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetDisplayRange;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
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
* Sets the intensity to maximise the dynamic range of the image.
"Clipping fraction" is the fraction of pixels at either end of the range that gets clipped.The "Per object" region mode will normalise all pixels within each object.<br><br>Note: This module will change pixel intensities.  To set the display range without altering pixel intensities, use the "Set intensity display range" module
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class NormaliseIntensity extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from the workspace for which intensity normalisation will be calculated.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Select if the normalisation should be applied directly to the input image, or if it should be applied to a duplicate image, then stored as a different image in the workspace.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If storing the processed image separately in the workspace, this is the name of the output image.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String REGION_SEPARATOR = "Region controls";

	/**
	* Controls whether the intensities are normalised across the entire image stack ("Entire image"), across the image slice-by-slice ("Per slice") or separately within each object ("Per slice")
	*/
    public static final String REGION_MODE = "Region mode";

	/**
	* If normalising intensities on an object-by-object basis ("Region mode" set to "Per object"), these are the objects that will be used.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String NORMALISATION_SEPARATOR = "Intensity normalisation";

	/**
	* Controls how the normalisation is calculated.  In each case, the minimum and maximum intensities are calculated and all values in the output image linearly interpolated between them:<br><ul><li>"Fast" All intensity values in the image are collected in a histogram.  As such, for 8 and 16-bit this is fast to calculate as there are a limited number of bins.  In this instance, the clip fraction corresponds to the fraction of bins.</li><li>"Manual" The minimum and maximum intensities in the final image are manually specified with the "Minimum range value" and "Maximum range value" parameters.</li><li>"Precise" All intensity values are ordered by their intensity and the clip fraction corresponds to the fraction of pixels (rather than the fraction of unique intensities as with "Fast" mode.  As such, this method is more precise; however, can take a much longer time (especially for large images).</li></ul>
	*/
    public static final String CALCULATION_MODE = "Calculation mode";

	/**
	* When applying a single normalisation to the entire image ("Region mode" set to "Entire image"), this parameter controls whether the normalisation range (min, max) will be determined from the input image or another image:<br><ul><li>"External" The image for which the normalisation range is calculated is different to the image that the final normalisation will be applied to.  For example, this could be a single representative slice or substack.  Using this could significantly reduce run-time for large stacks, especially when "Calculation mode" is set to "Precise".</li><li>"Internal" The normalisation range will be determined from the same image or stack onto which the normalisation will be applied.</li></ul>
	*/
    public static final String CALCULATION_SOURCE = "Calculation source";

	/**
	* If using a separate image to determine the normalisation range ("Calculation source" set to "External"), this is the image that will be used for that calculation.
	*/
    public static final String EXTERNAL_SOURCE = "External source";    
    public static final String CLIP_FRACTION_MIN = "Clipping fraction (min)";
    public static final String CLIP_FRACTION_MAX = "Clipping fraction (max)";

	/**
	* If manually setting the minimum intensity, this is the value that will be applied.
	*/
    public static final String MIN_RANGE = "Minimum range value";

	/**
	* If manually setting the maximum intensity, this is the value that will be applied.
	*/
    public static final String MAX_RANGE = "Maximum range value";

    public NormaliseIntensity(Modules modules) {
        super("Normalise intensity", modules);
    }

    public interface RegionModes {
        String ENTIRE_IMAGE = "Entire image";
        String PER_OBJECT = "Per object";
        String PER_SLICE = "Per slice";

        String[] ALL = new String[] { ENTIRE_IMAGE, PER_OBJECT, PER_SLICE };

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

    public static void applyNormalisationFullImage(ImagePlus ipl, String calculationMode, double[] clipFraction,
            @Nullable double[] intRange, @Nullable ImagePlus externalIpl) {
        int bitDepth = ipl.getProcessor().getBitDepth();

        // If externalIpl is null, use the input image for calculation
        if (externalIpl == null)
            externalIpl = ipl;

        // Get min max values for whole stack
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            switch (calculationMode) {
                case CalculationModes.FAST:
                    intRange = IntensityMinMax.getWeightedChannelRangeFast(externalIpl, c - 1, clipFraction[0],
                            clipFraction[1]);
                    break;

                case CalculationModes.PRECISE:
                    intRange = IntensityMinMax.getWeightedChannelRangePrecise(externalIpl, c - 1, clipFraction[0],
                            clipFraction[1]);
                    break;
            }

            if (intRange == null)
                return;

            double min = intRange[0];
            double max = intRange[1];

            // Applying normalisation
            double factor = bitDepth == 32 ? 1 : Math.pow(2, bitDepth) - 1;
            double mult = factor / (max - min);

            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    int idx = ipl.getStackIndex(c, z, t);
                    ImageProcessor ipr = ipl.getStack().getProcessor(idx);
                    ipr.subtract(min);
                    ipr.multiply(mult);
                }
            }

            ipl.updateChannelAndDraw();

        }
    }

    public static void applyNormalisationWithinObjects(ImagePlus ipl, String calculationMode, double[] clipFraction,
            @Nullable double[] intRange, @Nullable Obj maskObject) {
        int bitDepth = ipl.getProcessor().getBitDepth();

        for (int c = 1; c <= ipl.getNChannels(); c++) {
            int frame = maskObject.getT();
            switch (calculationMode) {
                case CalculationModes.FAST:
                    intRange = IntensityMinMax.getWeightedChannelRangeFast(ipl, maskObject.getCoordinateSet(), c - 1, frame,
                            clipFraction[0], clipFraction[1]);
                    break;

                case CalculationModes.PRECISE:
                    intRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl, maskObject.getCoordinateSet(), c - 1, frame,
                            clipFraction[0], clipFraction[1]);
                    break;
            }

            if (intRange == null)
                return;

            double min = intRange[0];
            double max = intRange[1];

            // Applying normalisation
            double factor = bitDepth == 32 ? 1 : Math.pow(2, bitDepth) - 1;
            double mult = factor / (max - min);

            for (Point<Integer> point : maskObject.getCoordinateSet()) {
                ipl.setPosition(c, point.getZ() + 1, frame + 1);

                ImageProcessor ipr = ipl.getProcessor();
                double val = ipr.getf(point.getX(), point.getY());
                val = (val - min) * mult;
                ipl.getProcessor().setf(point.getX(), point.getY(), (float) val);

            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Sets the intensity to maximise the dynamic range of the image.\n"
                + "\"Clipping fraction\" is the fraction of pixels at either end of the range that gets clipped."
                + "The \"Per object\" region mode will normalise all pixels within each object.<br><br>"

                + "Note: This module will change pixel intensities.  To set the display range without altering pixel intensities, use the \""
                + new SetDisplayRange(null).getName() + "\" module";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String regionMode = parameters.getValue(REGION_MODE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE, workspace);
        String externalImageName = parameters.getValue(EXTERNAL_SOURCE, workspace);
        String calculationMode = parameters.getValue(CALCULATION_MODE, workspace);
        double clipFractionMin = parameters.getValue(CLIP_FRACTION_MIN, workspace);
        double clipFractionMax = parameters.getValue(CLIP_FRACTION_MAX, workspace);
        double minRange = parameters.getValue(MIN_RANGE, workspace);
        double maxRange = parameters.getValue(MAX_RANGE, workspace);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        // Setting ranges
        double[] clipFraction = new double[] { clipFractionMin, clipFractionMax };
        double[] intRange = new double[] { minRange, maxRange };

        // Running intensity normalisation
        switch (regionMode) {
            case RegionModes.ENTIRE_IMAGE:
                ImagePlus externalIpl = calculationSource.equals(CalculationSources.EXTERNAL)
                        ? workspace.getImage(externalImageName).getImagePlus()
                        : null;
                applyNormalisationFullImage(inputImagePlus, calculationMode, clipFraction, intRange, externalIpl);
                break;

            case RegionModes.PER_OBJECT:
                Objs inputObjects = workspace.getObjects(inputObjectsName);
                int count = 0;
                int total = inputObjects.size();
                for (Obj inputObject : inputObjects.values()) {
                    applyNormalisationWithinObjects(inputImagePlus, calculationMode, clipFraction, intRange,
                            inputObject);
                    writeProgressStatus(++count, total, "objects");
                }
                break;

            case RegionModes.PER_SLICE:
                count = 0;
                total = inputImagePlus.getStack().size();
                for (int z = 0; z < total; z++) {
                    ImageProcessor ipr = inputImagePlus.getStack().getProcessor(z + 1);
                    ImagePlus tempIpl = new ImagePlus("Temp", ipr);
                    applyNormalisationFullImage(tempIpl, calculationMode, clipFraction, intRange, null);
                    inputImagePlus.getStack().setProcessor(tempIpl.getProcessor(), z + 1);
                    writeProgressStatus(++count, total, "slices");
                }
                break;
        }

        // Resetting location of the image
        inputImagePlus.setPosition(1, 1, 1);
        inputImagePlus.updateChannelAndDraw();

        // Set brightness/contrast
        IntensityMinMax.run(inputImagePlus, true);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show(outputImageName, LUT.createLutFromColor(Color.WHITE), false, true);

        } else {
            if (showOutput)
                inputImage.show(inputImageName, LUT.createLutFromColor(Color.WHITE), false, true);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(REGION_SEPARATOR, this));
        parameters.add(new ChoiceP(REGION_MODE, this, RegionModes.ENTIRE_IMAGE, RegionModes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(NORMALISATION_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.FAST, CalculationModes.ALL));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));        
        parameters.add(new DoubleP(CLIP_FRACTION_MIN, this, 0d));
        parameters.add(new DoubleP(CLIP_FRACTION_MAX, this, 0d));
        parameters.add(new DoubleP(MIN_RANGE, this, 0));
        parameters.add(new DoubleP(MAX_RANGE, this, 255));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REGION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REGION_MODE));
        switch ((String) parameters.getValue(REGION_MODE, workspace)) {
            case RegionModes.PER_OBJECT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(NORMALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        
        if ((boolean) parameters.getValue(REGION_MODE, workspace).equals(RegionModes.ENTIRE_IMAGE)
                & !((String) parameters.getValue(CALCULATION_MODE, workspace)).equals(CalculationModes.MANUAL)) {
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
        parameters.get(INPUT_IMAGE)
                .setDescription("Image from the workspace for which intensity normalisation will be calculated.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the normalisation should be applied directly to the input image, or if it should be applied to a duplicate image, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "If storing the processed image separately in the workspace, this is the name of the output image.");

        parameters.get(REGION_MODE)
                .setDescription("Controls whether the intensities are normalised across the entire image stack (\""
                        + RegionModes.ENTIRE_IMAGE + "\"), across the image slice-by-slice (\"" + RegionModes.PER_SLICE
                        + "\") or separately within each object (\"" + RegionModes.PER_SLICE + "\")");

        parameters.get(INPUT_OBJECTS)
                .setDescription("If normalising intensities on an object-by-object basis (\"" + REGION_MODE
                        + "\" set to \"" + RegionModes.PER_OBJECT + "\"), these are the objects that will be used.");

        parameters.get(CALCULATION_SOURCE).setDescription(
                "When applying a single normalisation to the entire image (\"" + REGION_MODE + "\" set to \""
                        + RegionModes.ENTIRE_IMAGE
                        + "\"), this parameter controls whether the normalisation range (min, max) will be determined from the input image or another image:<br><ul>"

                        + "<li>\"" + CalculationSources.EXTERNAL
                        + "\" The image for which the normalisation range is calculated is different to the image that the final normalisation will be applied to.  For example, this could be a single representative slice or substack.  Using this could significantly reduce run-time for large stacks, especially when \""
                        + CALCULATION_MODE + "\" is set to \"" + CalculationModes.PRECISE + "\".</li>"

                        + "<li>\"" + CalculationSources.INTERNAL
                        + "\" The normalisation range will be determined from the same image or stack onto which the normalisation will be applied.</li></ul>");

        parameters.get(EXTERNAL_SOURCE)
                .setDescription("If using a separate image to determine the normalisation range (\""
                        + CALCULATION_SOURCE + "\" set to \"" + CalculationSources.EXTERNAL
                        + "\"), this is the image that will be used for that calculation.");

        parameters.get(CALCULATION_MODE).setDescription(
                "Controls how the normalisation is calculated.  In each case, the minimum and maximum intensities are calculated and all values in the output image linearly interpolated between them:<br><ul>"

                        + "<li>\"" + CalculationModes.FAST
                        + "\" All intensity values in the image are collected in a histogram.  As such, for 8 and 16-bit this is fast to calculate as there are a limited number of bins.  In this instance, the clip fraction corresponds to the fraction of bins.</li>"

                        + "<li>\"" + CalculationModes.MANUAL
                        + "\" The minimum and maximum intensities in the final image are manually specified with the \""
                        + MIN_RANGE
                        + "\" and \"" + MAX_RANGE + "\" parameters.</li>"

                        + "<li>\"" + CalculationModes.PRECISE
                        + "\" All intensity values are ordered by their intensity and the clip fraction corresponds to the fraction of pixels (rather than the fraction of unique intensities as with \""
                        + CalculationModes.FAST
                        + "\" mode.  As such, this method is more precise; however, can take a much longer time (especially for large images).</li></ul>");

        parameters.get(CLIP_FRACTION_MIN).setDescription("Fraction of unique intensities (\"" + CalculationModes.PRECISE
                + "\") or pixels (\"" + CalculationModes.FAST
                + "\") that are clipped when setting the minimum intensity.  Any values below this will be set to the calculated minimum intensity.");

        parameters.get(CLIP_FRACTION_MAX).setDescription("Fraction of unique intensities (\"" + CalculationModes.PRECISE
                + "\") or pixels (\"" + CalculationModes.FAST
                + "\") that are clipped when setting the maximum intensity.  Any values above this will be set to the calculated maximum intensity.");

        parameters.get(MIN_RANGE).setDescription(
                "If manually setting the minimum intensity, this is the value that will be applied.");

        parameters.get(MAX_RANGE).setDescription(
                "If manually setting the maximum intensity, this is the value that will be applied.");

    }
}
