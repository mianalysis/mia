package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Scaler;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ScaleStack<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String SCALE_SEPARATOR = "General scaling controls";
    public static final String INTERPOLATION_MODE = "Interpolation mode";

    public static final String X_AXIS_SEPARATOR = "X-axis scaling controls";
    public static final String X_SCALE_MODE = "Scale mode (x-axis)";
    public static final String X_RESOLUTION = "Resolution (x-axis)";
    public static final String X_IMAGE = "Image (x-axis)";
    public static final String X_ADOPT_CALIBRATION = "Adopt calibration (x-axis)";
    public static final String X_SCALE_FACTOR = "Scale factor (x-axis)";

    public static final String Y_AXIS_SEPARATOR = "Y-axis scaling controls";
    public static final String Y_SCALE_MODE = "Scale mode (y-axis)";
    public static final String Y_RESOLUTION = "Resolution (y-axis)";
    public static final String Y_IMAGE = "Image (y-axis)";
    public static final String Y_ADOPT_CALIBRATION = "Adopt calibration (y-axis)";
    public static final String Y_SCALE_FACTOR = "Scale factor (y-axis)";

    public static final String Z_AXIS_SEPARATOR = "Z-axis scaling controls";
    public static final String Z_SCALE_MODE = "Scale mode (z-axis)";
    public static final String Z_RESOLUTION = "Resolution (z-axis)";
    public static final String Z_IMAGE = "Image (z-axis)";
    public static final String Z_ADOPT_CALIBRATION = "Adopt calibration (z-axis)";
    public static final String Z_SCALE_FACTOR = "Scale factor (z-axis)";

    public ScaleStack(Modules modules) {
        super("Scale stack", modules);
    }

    public interface ScaleModes {
        String FIXED_RESOLUTION = "Fixed resolution";
        String MATCH_IMAGE = "Match image";
        String SCALE_FACTOR = "Scale factor";

        String[] ALL = new String[] { FIXED_RESOLUTION, MATCH_IMAGE, SCALE_FACTOR };

    }

    public interface InterpolationModes {
        String NONE = "None";
        String BICUBIC = "Bicubic";
        String BILINEAR = "Bilinear";

        String[] ALL = new String[] { NONE, BICUBIC, BILINEAR };

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String interpolationMode = ((String) parameters.getValue(INTERPOLATION_MODE)).toLowerCase();
        String xScaleMode = parameters.getValue(X_SCALE_MODE);
        int xResolution = parameters.getValue(X_RESOLUTION);
        String xImageName = parameters.getValue(X_IMAGE);
        boolean xAdoptCalibration = parameters.getValue(X_ADOPT_CALIBRATION);
        double xScaleFactor = parameters.getValue(X_SCALE_FACTOR);
        String yScaleMode = parameters.getValue(Y_SCALE_MODE);
        int yResolution = parameters.getValue(Y_RESOLUTION);
        String yImageName = parameters.getValue(Y_IMAGE);
        boolean yAdoptCalibration = parameters.getValue(Y_ADOPT_CALIBRATION);
        double yScaleFactor = parameters.getValue(Y_SCALE_FACTOR);
        String zScaleMode = parameters.getValue(Z_SCALE_MODE);
        int zResolution = parameters.getValue(Z_RESOLUTION);
        String zImageName = parameters.getValue(Z_IMAGE);
        boolean zAdoptCalibration = parameters.getValue(Z_ADOPT_CALIBRATION);
        double zScaleFactor = parameters.getValue(Z_SCALE_FACTOR);

        Image inputImage = workspace.getImages().get(inputImageName);

        // Updating resolution values if fixed resolution not already provided
        switch (xScaleMode) {
            case ScaleModes.MATCH_IMAGE:
                xResolution = workspace.getImage(xImageName).getImagePlus().getWidth();
                break;
            case ScaleModes.SCALE_FACTOR:
                xResolution = (int) Math.round(inputImage.getImagePlus().getWidth() * xScaleFactor);
                break;
        }

        switch (yScaleMode) {
            case ScaleModes.MATCH_IMAGE:
                yResolution = workspace.getImage(yImageName).getImagePlus().getHeight();
                break;
            case ScaleModes.SCALE_FACTOR:
                yResolution = (int) Math.round(inputImage.getImagePlus().getHeight() * yScaleFactor);
                break;
        }

        switch (zScaleMode) {
            case ScaleModes.MATCH_IMAGE:
                zResolution = workspace.getImage(zImageName).getImagePlus().getNSlices();
                break;
            case ScaleModes.SCALE_FACTOR:
                zResolution = (int) Math.round(inputImage.getImagePlus().getNSlices() * zScaleFactor);
                break;
        }

        // Applying scaling
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus outputIpl = Scaler.resize(inputIpl, xResolution, yResolution, zResolution, interpolationMode);

        // Applying new calibration
        Calibration inputCal = inputIpl.getCalibration();
        Calibration outputCal = outputIpl.getCalibration();
        outputCal.pixelWidth = inputCal.pixelWidth * inputIpl.getWidth() / outputIpl.getWidth();
        outputCal.pixelHeight = inputCal.pixelHeight * inputIpl.getHeight() / outputIpl.getHeight();
        outputCal.pixelDepth = inputCal.pixelDepth * inputIpl.getNSlices() / outputIpl.getNSlices();
        outputCal.setUnit(inputCal.getUnit());
        outputCal.frameInterval = inputCal.frameInterval;
        outputCal.fps = inputCal.fps;

        // If setting a dimension to an image, there is an option to use that image's
        // calibration
        if (xScaleMode.equals(ScaleModes.MATCH_IMAGE) && xAdoptCalibration)
                outputCal.pixelWidth = workspace.getImage(xImageName).getImagePlus().getCalibration().pixelWidth;

        if (yScaleMode.equals(ScaleModes.MATCH_IMAGE) && yAdoptCalibration)
                outputCal.pixelHeight = workspace.getImage(yImageName).getImagePlus().getCalibration().pixelHeight;

        if (zScaleMode.equals(ScaleModes.MATCH_IMAGE)  && zAdoptCalibration)
                outputCal.pixelDepth = workspace.getImage(zImageName).getImagePlus().getCalibration().pixelDepth;

        Image outputImage = new Image(outputImageName, outputIpl);

        if (showOutput)
            outputImage.showImage();

        workspace.addImage(outputImage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to process."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output scaled image."));

        parameters.add(new SeparatorP(SCALE_SEPARATOR, this));
        parameters.add(new ChoiceP(INTERPOLATION_MODE, this, InterpolationModes.BILINEAR, InterpolationModes.ALL));

        parameters.add(new SeparatorP(X_AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(X_SCALE_MODE, this, ScaleModes.SCALE_FACTOR, ScaleModes.ALL));
        parameters.add(new IntegerP(X_RESOLUTION, this, 1));
        parameters.add(new InputImageP(X_IMAGE, this));
        parameters.add(new BooleanP(X_ADOPT_CALIBRATION, this, false));
        parameters.add(new DoubleP(X_SCALE_FACTOR, this, 1.0));

        parameters.add(new SeparatorP(Y_AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(Y_SCALE_MODE, this, ScaleModes.SCALE_FACTOR, ScaleModes.ALL));
        parameters.add(new IntegerP(Y_RESOLUTION, this, 1));
        parameters.add(new InputImageP(Y_IMAGE, this));
        parameters.add(new BooleanP(Y_ADOPT_CALIBRATION, this, false));
        parameters.add(new DoubleP(Y_SCALE_FACTOR, this, 1.0));

        parameters.add(new SeparatorP(Z_AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(Z_SCALE_MODE, this, ScaleModes.SCALE_FACTOR, ScaleModes.ALL));
        parameters.add(new IntegerP(Z_RESOLUTION, this, 1));
        parameters.add(new InputImageP(Z_IMAGE, this));
        parameters.add(new BooleanP(Z_ADOPT_CALIBRATION, this, false));
        parameters.add(new DoubleP(Z_SCALE_FACTOR, this, 1.0));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(SCALE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INTERPOLATION_MODE));

        returnedParameters.add(parameters.getParameter(X_AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_SCALE_MODE));
        switch ((String) parameters.getValue(X_SCALE_MODE)) {
            case ScaleModes.FIXED_RESOLUTION:
                returnedParameters.add(parameters.getParameter(X_RESOLUTION));
                break;
            case ScaleModes.MATCH_IMAGE:
                returnedParameters.add(parameters.getParameter(X_IMAGE));
                returnedParameters.add(parameters.getParameter(X_ADOPT_CALIBRATION));
                break;
            case ScaleModes.SCALE_FACTOR:
                returnedParameters.add(parameters.getParameter(X_SCALE_FACTOR));
                break;
        }

        returnedParameters.add(parameters.getParameter(Y_AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(Y_SCALE_MODE));
        switch ((String) parameters.getValue(Y_SCALE_MODE)) {
            case ScaleModes.FIXED_RESOLUTION:
                returnedParameters.add(parameters.getParameter(Y_RESOLUTION));
                break;
            case ScaleModes.MATCH_IMAGE:
                returnedParameters.add(parameters.getParameter(Y_IMAGE));
                returnedParameters.add(parameters.getParameter(Y_ADOPT_CALIBRATION));
                break;
            case ScaleModes.SCALE_FACTOR:
                returnedParameters.add(parameters.getParameter(Y_SCALE_FACTOR));
                break;
        }

        returnedParameters.add(parameters.getParameter(Z_AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(Z_SCALE_MODE));
        switch ((String) parameters.getValue(Z_SCALE_MODE)) {
            case ScaleModes.FIXED_RESOLUTION:
                returnedParameters.add(parameters.getParameter(Z_RESOLUTION));
                break;
            case ScaleModes.MATCH_IMAGE:
                returnedParameters.add(parameters.getParameter(Z_IMAGE));
                returnedParameters.add(parameters.getParameter(Z_ADOPT_CALIBRATION));
                break;
            case ScaleModes.SCALE_FACTOR:
                returnedParameters.add(parameters.getParameter(Z_SCALE_FACTOR));
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
}
