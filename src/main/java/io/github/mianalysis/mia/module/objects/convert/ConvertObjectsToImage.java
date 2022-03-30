package io.github.mianalysis.mia.module.objects.convert;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.visualise.overlays.AbstractOverlay;
import io.github.mianalysis.mia.module.visualise.overlays.AddAllObjectPoints;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.common.imagej.LUTs;
import io.github.sjcross.common.process.IntensityMinMax;

/**
 * Created by sc13967 on 04/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ConvertObjectsToImage extends Module {
  public static final String INPUT_SEPARATOR = "Object input/image output";
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

  public ConvertObjectsToImage(Modules modules) {
    super("Convert objects to image", modules);
  }

  public interface OutputModes {
    String CENTROID = "Object centroid";
    String WHOLE_OBJECT = "Whole object";

    String[] ALL = new String[] { CENTROID, WHOLE_OBJECT };

  }

  public interface ColourModes extends AbstractOverlay.ColourModes {
  }

  public interface SingleColourModes {
    String B_ON_W = "Black objects, white background";
    String W_ON_B = "White objects, black background";

    String[] ALL = new String[] { B_ON_W, W_ON_B };

  }

  @Override
  public Category getCategory() {
    return Categories.OBJECTS_CONVERT;
  }

  @Override
  public String getDescription() {
    return "Creates an image showing all objects in a specified collection.  The value (intensity) of each pixel can be based on object (or relative) ID numbers as well as various metrics, such as measurements or relationship counts.  Output images will be 32-bit type, except when in \""
        + ColourModes.RANDOM_COLOUR + "\" or \"" + ColourModes.SINGLE_COLOUR
        + "\" modes, which are 8-bit as the extra precision is not required.<br><br>Note: This output method is unable to correctly render overlapping objects (those with any matching coordinates); as such, the output image will show the result for one of objects for these coordinates.";

  }

  @Override
  public Status process(Workspace workspace) {
    String objectName = parameters.getValue(INPUT_OBJECTS);
    String outputImageName = parameters.getValue(OUTPUT_IMAGE);
    String outputMode = parameters.getValue(OUTPUT_MODE);
    String colourMode = parameters.getValue(COLOUR_MODE);
    String singleColourMode = parameters.getValue(SINGLE_COLOUR_MODE);
    String measurementForColour = parameters.getValue(MEASUREMENT);
    String childObjectsForColour = parameters.getValue(CHILD_OBJECTS_FOR_COLOUR);
    String parentForColour = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
    String partnerForColour = parameters.getValue(PARTNER_OBJECTS_FOR_COLOUR);

    Objs inputObjects = workspace.getObjects().get(objectName);

    // Generating colours for each object
    HashMap<Integer, Float> hues = null;
    boolean nanBackground = false;
    int bitDepth = 8;
    switch (colourMode) {
      case ColourModes.CHILD_COUNT:
        hues = ColourFactory.getChildCountHues(inputObjects, childObjectsForColour, false, new double[]{Double.NaN,Double.NaN});
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
        hues = ColourFactory.getMeasurementValueHues(inputObjects, measurementForColour, false, new double[]{Double.NaN,Double.NaN});
        bitDepth = 32;
        break;
      case ColourModes.PARENT_ID:
        hues = ColourFactory.getParentIDHues(inputObjects, parentForColour, false);
        bitDepth = 32;
        break;
      case ColourModes.PARENT_MEASUREMENT_VALUE:
        hues = ColourFactory.getParentMeasurementValueHues(inputObjects, parentForColour, measurementForColour, false, new double[]{Double.NaN,Double.NaN});
        bitDepth = 32;
        break;
      case ColourModes.PARTNER_COUNT:
        hues = ColourFactory.getPartnerCountHues(inputObjects, partnerForColour, false, new double[]{Double.NaN,Double.NaN});
        bitDepth = 32;
        break;
      case ColourModes.SINGLE_COLOUR:
      default:
        hues = ColourFactory.getSingleColourValues(inputObjects, ColourFactory.SingleColours.WHITE);
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

    return Status.PASS;

  }

  @Override
  protected void initialiseParameters() {
    parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
    parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
    parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
    parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
    parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.WHOLE_OBJECT, OutputModes.ALL));
    parameters.add(new ChoiceP(COLOUR_MODE, this, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
    parameters.add(new ChoiceP(SINGLE_COLOUR_MODE, this, SingleColourModes.W_ON_B, SingleColourModes.ALL));
    parameters.add(new ChildObjectsP(CHILD_OBJECTS_FOR_COLOUR, this));
    parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
    parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, this));
    parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_FOR_COLOUR, this));

    addParameterDescriptions();

  }

  @Override
  public Parameters updateAndGetParameters() {
    String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
    String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

    Parameters returnedParameters = new Parameters();

    returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
    returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
    returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

    returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
    returnedParameters.add(parameters.getParameter(OUTPUT_MODE));

    returnedParameters.add(parameters.getParameter(COLOUR_MODE));
    switch ((String) parameters.getValue(COLOUR_MODE)) {
      case ColourModes.CHILD_COUNT:
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR));
        if (parameters.getValue(INPUT_OBJECTS) != null) {
          ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR)).setParentObjectsName(inputObjectsName);
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
        ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);
        break;

      case ColourModes.PARENT_MEASUREMENT_VALUE:
        returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
        ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);

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
    parameters.get(INPUT_OBJECTS).setDescription(
        "Object collection to convert to an image.  All objects will be rendered onto the same output image.");

    parameters.get(OUTPUT_IMAGE).setDescription(
        "Image showing all objects in the input collection.  Note: This output method is unable to correctly render overlapping objects (those with any matching coordinates); as such, the output image will show the result for one of objects for these coordinates.");

    parameters.get(OUTPUT_MODE).setDescription("Controls what coordinates are used to represent each object.<br><ul>"

        + "<li>\"" + OutputModes.CENTROID
        + "\" Only the pixel closest to the centroid (mean XYZ coordinate) of each object is added to the output image.</li>"

        + "<li>\"" + OutputModes.WHOLE_OBJECT
        + "\" All coordinates of each object are added to the output image.</li></ul>");

    String description = new AddAllObjectPoints(null).getParameter(AbstractOverlay.COLOUR_MODE).getDescription();
    parameters.get(COLOUR_MODE).setDescription(description);

    parameters.get(SINGLE_COLOUR_MODE).setDescription("When \"" + COLOUR_MODE + "\" is set to \""
        + ColourModes.SINGLE_COLOUR
        + "\", the input objects will be converted to a binary image.  This parameter controls if the output image will have the logic \""
        + SingleColourModes.B_ON_W + "\" or \"" + SingleColourModes.W_ON_B + "\".");

    description = new AddAllObjectPoints(null).getParameter(AbstractOverlay.CHILD_OBJECTS_FOR_COLOUR).getDescription();
    parameters.get(CHILD_OBJECTS_FOR_COLOUR).setDescription(description);

    description = new AddAllObjectPoints(null).getParameter(AbstractOverlay.MEASUREMENT_FOR_COLOUR).getDescription();
    parameters.get(MEASUREMENT).setDescription(description);

    description = new AddAllObjectPoints(null).getParameter(AbstractOverlay.PARENT_OBJECT_FOR_COLOUR).getDescription();
    parameters.get(PARENT_OBJECT_FOR_COLOUR).setDescription(description);

    description = new AddAllObjectPoints(null).getParameter(AbstractOverlay.PARTNER_OBJECTS_FOR_COLOUR)
        .getDescription();
    parameters.get(PARTNER_OBJECTS_FOR_COLOUR).setDescription(description);

  }
}
