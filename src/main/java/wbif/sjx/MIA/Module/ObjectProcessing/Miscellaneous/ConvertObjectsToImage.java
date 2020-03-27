package wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Visualisation.Overlays.Overlay;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.HashMap;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ConvertObjectsToImage extends Module {
    public static final String INPUT_SEPARATOR = "Image/object input/output";
    public static final String CONVERSION_MODE = "Conversion mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String VOLUME_TYPE = "Volume type";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Rendering controls";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String CHILD_OBJECTS_FOR_COLOUR = "Child objects for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String PARTNER_OBJECTS_FOR_COLOUR = "Partner objects for colour";
    public static final String SINGLE_COLOUR_MODE = "Single colour mode";
    public static final String MEASUREMENT = "Measurement";

    public ConvertObjectsToImage(ModuleCollection modules) {
        super("Convert objects to image", modules);
    }

    public interface ConversionModes {
        String IMAGE_TO_OBJECTS = "Image to objects";
        String OBJECTS_TO_IMAGE = "Objects to image";

        String[] ALL = new String[] { IMAGE_TO_OBJECTS, OBJECTS_TO_IMAGE };

    }

    public interface OutputModes {
        String CENTROID = "Object centroid";
        String WHOLE_OBJECT = "Whole object";

        String[] ALL = new String[] { CENTROID, WHOLE_OBJECT };

    }

    public interface ColourModes extends Overlay.ColourModes {
    }

    public interface SingleColourModes {
        String B_ON_W = "Black objects, white background";
        String W_ON_B = "White objects, black background";

        String[] ALL = new String[] { B_ON_W, W_ON_B };

    }

    public interface VolumeTypes extends Image.VolumeTypes {
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        String conversionMode = parameters.getValue(CONVERSION_MODE);

        if (conversionMode.equals(ConversionModes.IMAGE_TO_OBJECTS)) {
            String inputImageName = parameters.getValue(INPUT_IMAGE);
            Image inputImage = workspace.getImages().get(inputImageName);

            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
            String volumeType = parameters.getValue(VOLUME_TYPE);

            ObjCollection objects = null;
            try {
                objects = inputImage.convertImageToObjects(volumeType, outputObjectsName);
            } catch (IntegerOverflowException e) {
                return false;
            }

            if (showOutput)
                objects.convertToImageRandomColours().showImage();

            workspace.addObjects(objects);

        } else if (conversionMode.equals(ConversionModes.OBJECTS_TO_IMAGE)) {
            String objectName = parameters.getValue(INPUT_OBJECTS);
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            String outputMode = parameters.getValue(OUTPUT_MODE);
            String colourMode = parameters.getValue(COLOUR_MODE);
            String singleColourMode = parameters.getValue(SINGLE_COLOUR_MODE);
            String measurementForColour = parameters.getValue(MEASUREMENT);
            String childObjectsForColour = parameters.getValue(CHILD_OBJECTS_FOR_COLOUR);
            String parentForColour = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
            String partnerForColour = parameters.getValue(PARTNER_OBJECTS_FOR_COLOUR);

            ObjCollection inputObjects = workspace.getObjects().get(objectName);

            // Generating colours for each object
            HashMap<Integer, Float> hues = null;
            boolean nanBackground = false;
            int bitDepth = 8;
            switch (colourMode) {
                case ColourModes.CHILD_COUNT:
                    hues = ColourFactory.getChildCountHues(inputObjects, childObjectsForColour, false);
                    bitDepth = 32;
                    break;
                case ColourModes.ID:
                    hues = ColourFactory.getIDHues(inputObjects, false);
                    bitDepth = 32;
                    break;
                case ColourModes.RANDOM_COLOUR:
                    hues = ColourFactory.getRandomHues(inputObjects);
                    break;
                case ColourModes.MEASUREMENT_VALUE:
                    nanBackground = true;
                    hues = ColourFactory.getMeasurementValueHues(inputObjects, measurementForColour, false);
                    bitDepth = 32;
                    break;
                case ColourModes.PARENT_ID:
                    hues = ColourFactory.getParentIDHues(inputObjects, parentForColour, false);
                    bitDepth = 32;
                    break;
                case ColourModes.PARENT_MEASUREMENT_VALUE:
                    hues = ColourFactory.getParentMeasurementValueHues(inputObjects, parentForColour,
                            measurementForColour, false);
                    bitDepth = 32;
                    break;
                case ColourModes.PARTNER_COUNT:
                    hues = ColourFactory.getPartnerCountHues(inputObjects, partnerForColour, false);
                    bitDepth = 32;
                    break;
                case ColourModes.SINGLE_COLOUR:
                default:
                    hues = ColourFactory.getSingleColourHues(inputObjects, ColourFactory.SingleColours.WHITE);
                    break;
            }

            Image outputImage = null;
            switch (outputMode) {
                case OutputModes.CENTROID:
                    outputImage = inputObjects.convertCentroidsToImage(outputImageName, hues, bitDepth, nanBackground);
                    break;
                case OutputModes.WHOLE_OBJECT:
                default:
                    outputImage = inputObjects.convertToImage(outputImageName, hues, bitDepth, nanBackground);
                    break;
            }

            if (colourMode.equals(ColourModes.SINGLE_COLOUR) && singleColourMode.equals(SingleColourModes.B_ON_W)) {
                InvertIntensity.process(outputImage);
            }

            // Applying spatial calibration from template image
            Calibration calibration = inputObjects.getSpatialCalibration().createImageCalibration();
            outputImage.getImagePlus().setCalibration(calibration);

            // Adding image to workspace
            workspace.addImage(outputImage);

            if (showOutput) {
                ImagePlus dispIpl = new Duplicator().run(outputImage.getImagePlus());
                dispIpl.setTitle(outputImage.getName());

                switch (colourMode) {
                    case ColourModes.ID:
                    case ColourModes.PARENT_ID:
                    case ColourModes.RANDOM_COLOUR:
                        dispIpl.setLut(LUTs.Random(true));
                        break;

                    case ColourModes.CHILD_COUNT:
                    case ColourModes.MEASUREMENT_VALUE:
                        dispIpl.setLut(LUTs.BlackFire());
                        break;

                    case ColourModes.SINGLE_COLOUR:
                        IJ.run(dispIpl, "Grays", "");
                        break;
                }

                IntensityMinMax.run(dispIpl, dispIpl.getNSlices() > 1);
                dispIpl.setPosition(1, 1, 1);
                dispIpl.updateChannelAndDraw();
                dispIpl.show();

            }
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(CONVERSION_MODE, this, ConversionModes.OBJECTS_TO_IMAGE, ConversionModes.ALL));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.POINTLIST, VolumeTypes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.WHOLE_OBJECT, OutputModes.ALL));
        parameters.add(new ChoiceP(COLOUR_MODE, this, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR_MODE, this, SingleColourModes.W_ON_B, SingleColourModes.ALL));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_FOR_COLOUR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_FOR_COLOUR, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CONVERSION_MODE));

        if (parameters.getValue(CONVERSION_MODE).equals(ConversionModes.IMAGE_TO_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
            returnedParameters.add(parameters.getParameter(VOLUME_TYPE));

        } else if (parameters.getValue(CONVERSION_MODE).equals(ConversionModes.OBJECTS_TO_IMAGE)) {
            returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
            returnedParameters.add(parameters.getParameter(OUTPUT_MODE));

            returnedParameters.add(parameters.getParameter(COLOUR_MODE));
            switch ((String) parameters.getValue(COLOUR_MODE)) {
                case ColourModes.CHILD_COUNT:
                    returnedParameters.add(parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR));
                    if (parameters.getValue(INPUT_OBJECTS) != null) {
                        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR))
                                .setParentObjectsName(inputObjectsName);
                    }
                    break;
                case ColourModes.MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(MEASUREMENT));
                    if (parameters.getValue(INPUT_OBJECTS) != null) {
                        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
                    }
                    break;

                case ColourModes.PARENT_ID:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                    ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                            .setChildObjectsName(inputObjectsName);
                    break;

                case ColourModes.PARENT_MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                    ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                            .setChildObjectsName(inputObjectsName);

                    returnedParameters.add(parameters.getParameter(MEASUREMENT));
                    if (parentObjectsName != null) {
                        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(parentObjectsName);
                    }

                    break;

                case ColourModes.PARTNER_COUNT:
                    returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS_FOR_COLOUR));
                    if (parameters.getValue(INPUT_OBJECTS) != null) {
                        ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS_FOR_COLOUR))
                                .setPartnerObjectsName(inputObjectsName);
                    }
                    break;

                case ColourModes.SINGLE_COLOUR:
                    returnedParameters.add(parameters.getParameter(SINGLE_COLOUR_MODE));
                    break;
            }
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
}