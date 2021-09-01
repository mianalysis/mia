package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel;

import java.awt.Color;

import org.eclipse.sisu.Nullable;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.Visualisation.ImageRendering.SetDisplayRange;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.OutputImageP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Text.DoubleP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;
import io.github.sjcross.common.Object.Point;
import io.github.sjcross.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class NormaliseIntensity extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String REGION_SEPARATOR = "Region controls";
    public static final String REGION_MODE = "Region mode";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String NORMALISATION_SEPARATOR = "Intensity normalisation";
    public static final String CALCULATION_MODE = "Calculation mode";
    public static final String CLIP_FRACTION_MIN = "Clipping fraction (min)";
    public static final String CLIP_FRACTION_MAX = "Clipping fraction (max)";
    public static final String MIN_RANGE = "Minimum range value";
    public static final String MAX_RANGE = "Maximum range value";

    public NormaliseIntensity(ModuleCollection modules) {
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

    public static void applyNormalisation(ImagePlus ipl, String calculationMode, double[] clipFraction,
            @Nullable double[] intRange) {
        applyNormalisation(ipl, calculationMode, clipFraction, intRange, null);
    }

    public static void applyNormalisation(ImagePlus ipl, String calculationMode, double[] clipFraction,
            @Nullable double[] intRange, @Nullable Obj maskObject) {
        int bitDepth = ipl.getProcessor().getBitDepth();

        // Get min max values for whole stack
        if (maskObject == null) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                switch (calculationMode) {
                    case CalculationModes.FAST:
                        intRange = IntensityMinMax.getWeightedChannelRangeFast(ipl, c - 1, clipFraction[0],
                                clipFraction[1]);
                        break;

                    case CalculationModes.PRECISE:
                        intRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl, c - 1, clipFraction[0],
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
                        ipl.setPosition(c, z, t);
                        ipl.getProcessor().subtract(min);
                        ipl.getProcessor().multiply(mult);
                    }
                }
            }

        } else {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                int frame = maskObject.getT();
                switch (calculationMode) {
                    case CalculationModes.FAST:
                        intRange = IntensityMinMax.getWeightedChannelRangeFast(ipl, maskObject, c - 1, frame,
                                clipFraction[0], clipFraction[1]);
                        break;

                    case CalculationModes.PRECISE:
                        intRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl, maskObject, c - 1, frame,
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

        // Resetting location of the image
        ipl.setPosition(1, 1, 1);

        // Set brightness/contrast
        IntensityMinMax.run(ipl, true);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL;
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
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String regionMode = parameters.getValue(REGION_MODE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String calculationMode = parameters.getValue(CALCULATION_MODE);
        double clipFractionMin = parameters.getValue(CLIP_FRACTION_MIN);
        double clipFractionMax = parameters.getValue(CLIP_FRACTION_MAX);
        double minRange = parameters.getValue(MIN_RANGE);
        double maxRange = parameters.getValue(MAX_RANGE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        // Setting ranges
        double[] clipFraction = new double[] { clipFractionMin, clipFractionMax };
        double[] intRange = new double[] { minRange, maxRange };

        // Running intensity normalisation
        switch (regionMode) {
            case RegionModes.ENTIRE_IMAGE:
                writeStatus("Applying entire-image pixel normalisation");
                applyNormalisation(inputImagePlus, calculationMode, clipFraction, intRange);
                break;

            case RegionModes.PER_OBJECT:
                ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                int count = 0;
                int total = inputObjects.size();
                for (Obj inputObject : inputObjects.values()) {
                    applyNormalisation(inputImagePlus, calculationMode, clipFraction, intRange, inputObject);
                    writeProgressStatus(++count, total, "objects");
                }
                break;

            case RegionModes.PER_SLICE:
                count = 0;
                total = inputImagePlus.getStack().size();
                for (int z = 0; z < total; z++) {
                    ImageProcessor ipr = inputImagePlus.getStack().getProcessor(z + 1);
                    ImagePlus tempIpl = new ImagePlus("Temp", ipr);
                    applyNormalisation(tempIpl, calculationMode, clipFraction, intRange);
                    inputImagePlus.getStack().setProcessor(tempIpl.getProcessor(), z + 1);
                    writeProgressStatus(++count, total, "slices");
                }
                break;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage(outputImageName, LUT.createLutFromColor(Color.WHITE), false, true);

        } else {
            if (showOutput)
                inputImage.showImage(inputImageName, LUT.createLutFromColor(Color.WHITE), false, true);

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
        parameters.add(new DoubleP(CLIP_FRACTION_MIN, this, 0d));
        parameters.add(new DoubleP(CLIP_FRACTION_MAX, this, 0d));
        parameters.add(new DoubleP(MIN_RANGE, this, 0));
        parameters.add(new DoubleP(MAX_RANGE, this, 255));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REGION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REGION_MODE));
        switch ((String) parameters.getValue(REGION_MODE)) {
            case RegionModes.PER_OBJECT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(NORMALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        switch ((String) parameters.getValue(CALCULATION_MODE)) {
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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
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

        parameters.get(CALCULATION_MODE).setDescription("Controls how the normalisation is calculated.  In each case, the minimum and maximum intensities are calculated and all values in the output image linearly interpolated between them:<br><ul>"

                + "<li>\"" + CalculationModes.FAST
                + "\" All intensity values in the image are collected in a histogram.  As such, for 8 and 16-bit this is fast to calculate as there are a limited number of bins.  In this instance, the clip fraction corresponds to the fraction of bins.</li>"

                + "<li>\"" + CalculationModes.MANUAL
                + "\" The minimum and maximum intensities in the final image are manually specified with the \"" + MIN_RANGE
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