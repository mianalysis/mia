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
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;


/**
* Applies independent X,Y and Z-axis scaling to an input image.  Output dimensions can be specified explicitly, matched to another image in the workspace or calculated with a scaling factor.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ScaleStack<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image to process.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Name of the output scaled image.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String SCALE_SEPARATOR = "General scaling controls";

	/**
	* Controls how interpolated pixel values are calculated.  Choices are: None,Bicubic,Bilinear
	*/
    public static final String INTERPOLATION_MODE = "Interpolation mode";


	/**
	* 
	*/
    public static final String X_AXIS_SEPARATOR = "X-axis scaling controls";
    public static final String X_SCALE_MODE = "Scale mode (x-axis)";
    public static final String X_RESOLUTION = "Resolution (x-axis)";
    public static final String X_IMAGE = "Image (x-axis)";
    public static final String X_ADOPT_CALIBRATION = "Adopt calibration (x-axis)";
    public static final String X_SCALE_FACTOR = "Scale factor (x-axis)";


	/**
	* 
	*/
    public static final String Y_AXIS_SEPARATOR = "Y-axis scaling controls";
    public static final String Y_SCALE_MODE = "Scale mode (y-axis)";
    public static final String Y_RESOLUTION = "Resolution (y-axis)";
    public static final String Y_IMAGE = "Image (y-axis)";
    public static final String Y_ADOPT_CALIBRATION = "Adopt calibration (y-axis)";
    public static final String Y_SCALE_FACTOR = "Scale factor (y-axis)";


	/**
	* 
	*/
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
        return "Applies independent X,Y and Z-axis scaling to an input image.  Output dimensions can be specified explicitly, matched to another image in the workspace or calculated with a scaling factor.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String interpolationMode = ((String) parameters.getValue(INTERPOLATION_MODE, workspace)).toLowerCase();
        String xScaleMode = parameters.getValue(X_SCALE_MODE, workspace);
        int xResolution = parameters.getValue(X_RESOLUTION, workspace);
        String xImageName = parameters.getValue(X_IMAGE, workspace);
        boolean xAdoptCalibration = parameters.getValue(X_ADOPT_CALIBRATION, workspace);
        double xScaleFactor = parameters.getValue(X_SCALE_FACTOR, workspace);
        String yScaleMode = parameters.getValue(Y_SCALE_MODE, workspace);
        int yResolution = parameters.getValue(Y_RESOLUTION, workspace);
        String yImageName = parameters.getValue(Y_IMAGE, workspace);
        boolean yAdoptCalibration = parameters.getValue(Y_ADOPT_CALIBRATION, workspace);
        double yScaleFactor = parameters.getValue(Y_SCALE_FACTOR, workspace);
        String zScaleMode = parameters.getValue(Z_SCALE_MODE, workspace);
        int zResolution = parameters.getValue(Z_RESOLUTION, workspace);
        String zImageName = parameters.getValue(Z_IMAGE, workspace);
        boolean zAdoptCalibration = parameters.getValue(Z_ADOPT_CALIBRATION, workspace);
        double zScaleFactor = parameters.getValue(Z_SCALE_FACTOR, workspace);

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

        if (zScaleMode.equals(ScaleModes.MATCH_IMAGE) && zAdoptCalibration)
            outputCal.pixelDepth = workspace.getImage(zImageName).getImagePlus().getCalibration().pixelDepth;

        Image outputImage = ImageFactory.createImage(outputImageName, outputIpl);

        if (showOutput)
            outputImage.show();

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

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(SCALE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INTERPOLATION_MODE));

        returnedParameters.add(parameters.getParameter(X_AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_SCALE_MODE));
        switch ((String) parameters.getValue(X_SCALE_MODE, workspace)) {
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
        switch ((String) parameters.getValue(Y_SCALE_MODE, workspace)) {
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
        switch ((String) parameters.getValue(Z_SCALE_MODE, workspace)) {
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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to process.");

        parameters.get(OUTPUT_IMAGE).setDescription("Name of the output scaled image.");

        parameters.get(INTERPOLATION_MODE)
                .setDescription("Controls how interpolated pixel values are calculated.  Choices are: "
                        + String.join(",", InterpolationModes.ALL));

        parameters.get(X_SCALE_MODE)
                .setDescription("Controls how the output x-axis resolution is calculated:<br><ul>"

                        + "<li>\"" + ScaleModes.FIXED_RESOLUTION
                        + "\" The output image will have the specific resolution defined by the \"" + X_RESOLUTION
                        + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_IMAGE
                        + "\" The output image will have the same resolution as the image specified by the \""
                        + X_IMAGE + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.SCALE_FACTOR
                        + "\" The output image will have an equal resolution to the input resolution multiplied by the scaling factor, \""
                        + X_SCALE_FACTOR
                        + "\".  The output resolution will be rounded to the closest whole number.</li></ul>");

        parameters.get(X_RESOLUTION).setDescription("If \"" + X_SCALE_MODE + "\" is set to \""
                + ScaleModes.FIXED_RESOLUTION + "\", this is the resolution in the output image.");

        parameters.get(X_IMAGE).setDescription("If \"" + X_SCALE_MODE + "\" is set to \"" + ScaleModes.MATCH_IMAGE
                + "\", the output image will have the same resolution as this image.");

        parameters.get(X_ADOPT_CALIBRATION).setDescription("If \"" + X_SCALE_MODE + "\" is set to \""
                + ScaleModes.MATCH_IMAGE
                + "\", and this is selected, the output image's x-axis calibration will be copied from the input image.");

        parameters.get(X_SCALE_FACTOR).setDescription("If \"" + X_SCALE_MODE + "\" is set to \""
                + ScaleModes.SCALE_FACTOR
                + "\", the output image will have a resolution equal to the input resolution multiplied by this scale factor.  The applied resolution will be rounded to the closest whole number");

        parameters.get(Y_SCALE_MODE)
                .setDescription("Controls how the output y-axis resolution is calculated:<br><ul>"

                        + "<li>\"" + ScaleModes.FIXED_RESOLUTION
                        + "\" The output image will have the specific resolution defined by the \"" + Y_RESOLUTION
                        + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_IMAGE
                        + "\" The output image will have the same resolution as the image specified by the \""
                        + Y_IMAGE + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.SCALE_FACTOR
                        + "\" The output image will have an equal resolution to the input resolution multiplied by the scaling factor, \""
                        + Y_SCALE_FACTOR
                        + "\".  The output resolution will be rounded to the closest whole number.</li></ul>");

        parameters.get(Y_RESOLUTION).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \""
                + ScaleModes.FIXED_RESOLUTION + "\", this is the resolution in the output image.");

        parameters.get(Y_IMAGE).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \"" + ScaleModes.MATCH_IMAGE
                + "\", the output image will have the same resolution as this image.");

        parameters.get(Y_ADOPT_CALIBRATION).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \""
                + ScaleModes.MATCH_IMAGE
                + "\", and this is selected, the output image's y-axis calibration will be copied from the input image.");

        parameters.get(Y_SCALE_FACTOR).setDescription("If \"" + Y_SCALE_MODE + "\" is set to \""
                + ScaleModes.SCALE_FACTOR
                + "\", the output image will have a resolution equal to the input resolution multiplied by this scale factor.  The applied resolution will be rounded to the closest whole number");

        parameters.get(Z_SCALE_MODE)
                .setDescription("Controls how the output z-axis resolution (number of slices) is calculated:<br><ul>"

                        + "<li>\"" + ScaleModes.FIXED_RESOLUTION
                        + "\" The output image will have the specific number of slices defined by the \"" + Z_RESOLUTION
                        + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.MATCH_IMAGE
                        + "\" The output image will have the same number of slices as the image specified by the \""
                        + Z_IMAGE + "\" parameter.</li>"

                        + "<li>\"" + ScaleModes.SCALE_FACTOR
                        + "\" The output image will have an equal number of slices to the input number of slices multiplied by the scaling factor, \""
                        + Z_SCALE_FACTOR
                        + "\".  The output number of slices will be rounded to the closest whole number.</li></ul>");

        parameters.get(Z_RESOLUTION).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \""
                + ScaleModes.FIXED_RESOLUTION + "\", this is the number of slices in the output image.");

        parameters.get(Z_IMAGE).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \"" + ScaleModes.MATCH_IMAGE
                + "\", the output image will have the same number of slices as there are in this image.");

        parameters.get(Z_ADOPT_CALIBRATION).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \""
                + ScaleModes.MATCH_IMAGE
                + "\", and this is selected, the output image's z-axis calibration will be copied from the input image.");

        parameters.get(Z_SCALE_FACTOR).setDescription("If \"" + Z_SCALE_MODE + "\" is set to \""
                + ScaleModes.SCALE_FACTOR
                + "\", the output image will have a number of slices equal to the input number of slices multiplied by this scale factor.  The applied number of slices will be rounded to the closest whole number");
    }
}
