package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.ColourFactory;

import java.util.HashMap;

public class ColourServer {
    public static final String COLOUR_MODE = "Colour mode";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";

    private ParameterCollection parameters = new ParameterCollection();
    private InputObjectsP inputObjects;

    public interface ColourModes extends ObjCollection.ColourModes {}

    public interface SingleColours extends ColourFactory.SingleColours {}


    public ColourServer(InputObjectsP inputObjects, Module module) {
        this.inputObjects = inputObjects;
        initialiseParameters(module);
    }


    public HashMap<Integer,Float> getHues(ObjCollection inputObjects) {
        // Getting colour settings
        String colourMode = parameters.getValue(COLOUR_MODE);
        String singleColour = parameters.getValue(SINGLE_COLOUR);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR);

        // Generating colours for each object
        switch (colourMode) {
            case ColourModes.SINGLE_COLOUR:
            default:
                return ColourFactory.getSingleColourHues(inputObjects,singleColour);
            case ColourModes.ID:
                return ColourFactory.getIDHues(inputObjects,true);
            case ColourModes.RANDOM_COLOUR:
                return ColourFactory.getRandomHues(inputObjects);
            case ColourModes.MEASUREMENT_VALUE:
                return ColourFactory.getMeasurementValueHues(inputObjects,measurementForColour,true);
            case ColourModes.PARENT_ID:
                return ColourFactory.getParentIDHues(inputObjects,parentObjectsForColourName,true);
            case ColourModes.PARENT_MEASUREMENT_VALUE:
                return ColourFactory.getParentMeasurementValueHues(inputObjects,parentObjectsForColourName,measurementForColour,true);
        }
    }

    private void initialiseParameters(Module module) {
        parameters.add(new ChoiceP(COLOUR_MODE, module, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR,module,SingleColours.WHITE,SingleColours.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_COLOUR, module));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, module));

    }

    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = inputObjects.getFinalValue();
        String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE)) {
            case AddObjectsOverlay.ColourModes.SINGLE_COLOUR:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;

            case AddObjectsOverlay.ColourModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                if (inputObjectsName != null) {
                    ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                    colourMeasurement.setObjectName(inputObjectsName);
                }
                break;

            case AddObjectsOverlay.ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);
                break;

            case AddObjectsOverlay.ColourModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                colourMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_COLOUR));

                break;
        }

        return returnedParameters;

    }

    public ParameterCollection getParameters() {
        return parameters;
    }
}
