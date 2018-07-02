// TODO: For image to objects, could add the image ID number as a measurement to the object
// TODO: For image to objects, could create parent object for all instances of that image ID in different frames
// TODO: Colour based on parent measurement

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.HashMap;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ConvertObjectsToImage extends Module {
    public static final String CONVERSION_MODE = "Conversion mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String MEASUREMENT = "Measurement";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";

    public interface ConversionModes {
        String IMAGE_TO_OBJECTS = "Image to objects";
        String OBJECTS_TO_IMAGE = "Objects to image";

        String[] ALL = new String[]{IMAGE_TO_OBJECTS, OBJECTS_TO_IMAGE};

    }

    public interface ColourModes extends ObjCollection.ColourModes  {}

    @Override
    public String getTitle() {
        return "Convert objects to image";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        String conversionMode = parameters.getValue(CONVERSION_MODE);

        if (conversionMode.equals(ConversionModes.IMAGE_TO_OBJECTS)) {
            String inputImageName = parameters.getValue(INPUT_IMAGE);
            Image inputImage = workspace.getImages().get(inputImageName);

            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

            ObjCollection objects = inputImage.convertImageToObjects(outputObjectsName);

            workspace.addObjects(objects);

        } else if (conversionMode.equals(ConversionModes.OBJECTS_TO_IMAGE)) {
            String objectName = parameters.getValue(INPUT_OBJECTS);
            String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            String colourMode = parameters.getValue(COLOUR_MODE);
            String measurementForColour = parameters.getValue(MEASUREMENT);
            String parentForColour = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

            ObjCollection inputObjects = workspace.getObjects().get(objectName);
            Image templateImage = workspace.getImages().get(templateImageName);

            // Generating colours for each object
            String sourceColour = "";
            switch (colourMode) {
                case AddObjectsOverlay.ColourModes.MEASUREMENT_VALUE:
                    sourceColour = measurementForColour;
                    break;
                case AddObjectsOverlay.ColourModes.PARENT_ID:
                    sourceColour = parentForColour;
                    break;
            }
            HashMap<Integer, Float> hues = inputObjects.getHues(colourMode, sourceColour,false);

            Image outputImage = inputObjects.convertObjectsToImage(outputImageName, templateImage.getImagePlus(), colourMode,hues);

            // Applying spatial calibration from template image
            Calibration calibration = templateImage.getImagePlus().getCalibration();
            outputImage.getImagePlus().setCalibration(calibration);

            // Adding image to workspace
            workspace.addImage(outputImage);

            if (showOutput) {
                ImagePlus ipl = outputImage.getImagePlus();
                IntensityMinMax.run(ipl,ipl.getNSlices() > 1);
                ImagePlus iplShow = new Duplicator().run(ipl);

                switch (colourMode) {
                    case ColourModes.ID:
                    case ColourModes.PARENT_ID:
                    case ColourModes.RANDOM_COLOUR:
                        iplShow.setLut(LUTs.Random(true));
                        break;

                    case ColourModes.MEASUREMENT_VALUE:
                        iplShow.setLut(LUTs.BlackFire());
                        break;

                    case ColourModes.SINGLE_COLOUR:
                        IJ.run(iplShow,"Grays","");
                        break;
                }

                iplShow.show();

            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(CONVERSION_MODE, Parameter.CHOICE_ARRAY,ConversionModes.OBJECTS_TO_IMAGE,ConversionModes.ALL));
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(TEMPLATE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(COLOUR_MODE, Parameter.CHOICE_ARRAY,ColourModes.SINGLE_COLOUR,ColourModes.ALL));
        parameters.add(new Parameter(MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(PARENT_OBJECT_FOR_COLOUR, Parameter.PARENT_OBJECTS,null,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(CONVERSION_MODE));

        if (parameters.getValue(CONVERSION_MODE).equals(ConversionModes.IMAGE_TO_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        } else if(parameters.getValue(CONVERSION_MODE).equals(ConversionModes.OBJECTS_TO_IMAGE)) {
            returnedParameters.add(parameters.getParameter(TEMPLATE_IMAGE));
            returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            returnedParameters.add(parameters.getParameter(COLOUR_MODE));
            if (parameters.getValue(COLOUR_MODE).equals(ColourModes.MEASUREMENT_VALUE)) {
                // Use measurement
                returnedParameters.add(parameters.getParameter(MEASUREMENT));

                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueSource(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

                }

            } else if (parameters.getValue(COLOUR_MODE).equals(ColourModes.PARENT_ID)) {
                // Use Parent ID
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));

                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(PARENT_OBJECT_FOR_COLOUR,inputObjectsName);

            }
        }

        return returnedParameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}