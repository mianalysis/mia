package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.process.ColourFactory;
import java_cup.parse_reduce_row;

public abstract class AbstractOverlay extends Module {
    public static final String COLOUR_SEPARATOR = "Overlay colour";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String COLOUR_MAP = "Colour map";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String CHILD_OBJECTS_FOR_COLOUR = "Child objects for colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String METADATA_ITEM_FOR_COLOUR = "Metadata item for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String PARTNER_OBJECTS_FOR_COLOUR = "Partner objects for colour";
    public static final String OPACITY = "Opacity (%)";
    public static final String RANGE_MINIMUM_MODE = "Range minimum mode";
    public static final String MINIMUM_VALUE = "Minimum value";
    public static final String RANGE_MAXIMUM_MODE = "Range maximum mode";
    public static final String MAXIMUM_VALUE = "Maximum value";

    public AbstractOverlay(String name, Modules modules) {
        super(name, modules);
    }

    public interface ColourModes extends ColourFactory.ColourModes {
    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public interface ColourMaps extends ColourFactory.ColourMaps {
    }

    public interface RangeModes {
        String AUTOMATIC = "Automatic";
        String MANUAL = "Manual";

        String[] ALL = new String[] { AUTOMATIC, MANUAL };

    }

    public HashMap<Integer, Color> getColours(Objs inputObjects, Workspace workspace) {
        // Getting colour settings
        String colourMode = parameters.getValue(COLOUR_MODE, workspace);
        String colourMap = parameters.getValue(COLOUR_MAP, workspace);
        String singleColour = parameters.getValue(SINGLE_COLOUR, workspace);
        String childObjectsForColourName = parameters.getValue(CHILD_OBJECTS_FOR_COLOUR, workspace);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR, workspace);
        String partnerObjectsForColourName = parameters.getValue(PARTNER_OBJECTS_FOR_COLOUR, workspace);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR, workspace);
        String metadataItemForColour = parameters.getValue(METADATA_ITEM_FOR_COLOUR, workspace);
        String rangeMinMode = parameters.getValue(RANGE_MINIMUM_MODE, workspace);
        double minValue = parameters.getValue(MINIMUM_VALUE, workspace);
        String rangeMaxMode = parameters.getValue(RANGE_MAXIMUM_MODE, workspace);
        double maxValue = parameters.getValue(MAXIMUM_VALUE, workspace);
        double opacity = parameters.getValue(OPACITY, workspace);

        double[] range = new double[] { Double.NaN, Double.NaN };
        if (rangeMinMode.equals(RangeModes.MANUAL))
            range[0] = minValue;
        if (rangeMaxMode.equals(RangeModes.MANUAL))
            range[1] = maxValue;

        HashMap<Integer, Float> values = null;
        // Generating colours for each object
        switch (colourMode) {
            default:
                return null;
            case ColourModes.SINGLE_COLOUR:
                // Special case where value actually specifies hue
                return ColourFactory.getColours(ColourFactory.getSingleColourValues(inputObjects, singleColour),
                        opacity);
            case ColourModes.CHILD_COUNT:
                values = ColourFactory.getChildCountHues(inputObjects, childObjectsForColourName, true, range);
                break;
            case ColourModes.ID:
                values = ColourFactory.getIDHues(inputObjects, true);
                break;
            case ColourModes.RANDOM_COLOUR:
                values = ColourFactory.getRandomHues(inputObjects);
                break;
            case ColourModes.MEASUREMENT_VALUE:
                values = ColourFactory.getMeasurementValueHues(inputObjects, measurementForColour, true, range);
                break;
            case ColourModes.OBJ_METADATA_ITEM:
                values = ColourFactory.getObjectMetadataHues(inputObjects, metadataItemForColour);
                break;
            case ColourModes.PARENT_ID:
                values = ColourFactory.getParentIDHues(inputObjects, parentObjectsForColourName, true);
                break;
            case ColourModes.PARENT_MEASUREMENT_VALUE:
                values = ColourFactory.getParentMeasurementValueHues(inputObjects, parentObjectsForColourName,
                        measurementForColour, true, range);
                break;
            case ColourModes.PARTNER_COUNT:
                values = ColourFactory.getPartnerCountHues(inputObjects, partnerObjectsForColourName, true, range);
                break;
        }

        return ColourFactory.getColours(values, colourMap, opacity);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(COLOUR_SEPARATOR, this));
        parameters.add(new ChoiceP(COLOUR_MODE, this, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new ChoiceP(COLOUR_MAP, this, ColourMaps.SPECTRUM, ColourMaps.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_FOR_COLOUR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_COLOUR, this));
        parameters.add(new ObjectMetadataP(METADATA_ITEM_FOR_COLOUR, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_FOR_COLOUR, this));
        parameters.add(new DoubleP(OPACITY, this, 100));
        parameters.add(new ChoiceP(RANGE_MINIMUM_MODE, this, RangeModes.AUTOMATIC, RangeModes.ALL));
        parameters.add(new DoubleP(MINIMUM_VALUE, this, 0d));
        parameters.add(new ChoiceP(RANGE_MAXIMUM_MODE, this, RangeModes.AUTOMATIC, RangeModes.ALL));
        parameters.add(new DoubleP(MAXIMUM_VALUE, this, 1d));

    }

    public Parameters updateAndGetParameters(String inputObjectsName) {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(COLOUR_SEPARATOR));
        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE, null)) {
            case ColourModes.CHILD_COUNT:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR));
                ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR))
                        .setParentObjectsName(inputObjectsName);
                returnedParameters.add(parameters.getParameter(COLOUR_MAP));
                break;

            case ColourModes.ID:
                returnedParameters.add(parameters.getParameter(COLOUR_MAP));
                break;

            case ColourModes.SINGLE_COLOUR:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;

            case ColourModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                if (inputObjectsName != null) {
                    ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                    colourMeasurement.setObjectName(inputObjectsName);
                }
                returnedParameters.add(parameters.getParameter(COLOUR_MAP));
                break;

            case ColourModes.OBJ_METADATA_ITEM:
                returnedParameters.add(parameters.getParameter(METADATA_ITEM_FOR_COLOUR));
                if (inputObjectsName != null) {
                    ObjectMetadataP colourMetadataItem = parameters.getParameter(METADATA_ITEM_FOR_COLOUR);
                    colourMetadataItem.setObjectName(inputObjectsName);
                }
                returnedParameters.add(parameters.getParameter(COLOUR_MAP));
                break;

            case ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                        .setChildObjectsName(inputObjectsName);
                returnedParameters.add(parameters.getParameter(COLOUR_MAP));
                break;

            case ColourModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                        .setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                colourMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_COLOUR, null));

                returnedParameters.add(parameters.getParameter(COLOUR_MAP));

                break;

            case ColourModes.PARTNER_COUNT:
                returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS_FOR_COLOUR));
                ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS_FOR_COLOUR))
                        .setPartnerObjectsName(inputObjectsName);
                returnedParameters.add(parameters.getParameter(COLOUR_MAP));
                break;
        }

        switch ((String) parameters.getValue(COLOUR_MODE, null)) {
            case ColourModes.CHILD_COUNT:
            case ColourModes.MEASUREMENT_VALUE:
            case ColourModes.PARENT_MEASUREMENT_VALUE:
            case ColourModes.PARTNER_COUNT:
                returnedParameters.add(parameters.getParameter(RANGE_MINIMUM_MODE));
                if (((String) parameters.getValue(RANGE_MINIMUM_MODE, null)).equals(RangeModes.MANUAL))
                    returnedParameters.add(parameters.getParameter(MINIMUM_VALUE));
                returnedParameters.add(parameters.getParameter(RANGE_MAXIMUM_MODE));
                if (((String) parameters.getValue(RANGE_MAXIMUM_MODE, null)).equals(RangeModes.MANUAL))
                    returnedParameters.add(parameters.getParameter(MAXIMUM_VALUE));
                break;
        }

        returnedParameters.add(parameters.getParameter(OPACITY));

        return returnedParameters;

    }

    protected void addParameterDescriptions() {
        parameters.get(COLOUR_MODE).setDescription("Method for assigning colour of each object:<br><ul>"

                + "<li>\"" + ColourModes.CHILD_COUNT
                + "\" Colour is determined by the number of children each object has.  "
                + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                + "with the fewest children is shown in red and the object with the most, in cyan.  Objects without any children are always shown in red.  "
                + "Child objects used for counting are selected with the \"" + CHILD_OBJECTS_FOR_COLOUR
                + "\" parameter.</li>"

                + "<li>\"" + ColourModes.ID
                + "\" Colour is quasi-randomly selected based on the ID number of the object.  The colour used for a specific "
                + "ID number will always be the same and is calculated using the equation <i>hue = (ID * 1048576 % 255) / 255</i>.</li>"

                + "<li>\"" + ColourModes.MEASUREMENT_VALUE + "\" Colour is determined by a measurement value.  "
                + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                + "with the smallest measurement is shown in red and the object with the largest, in cyan.  Objects missing the relevant measurement "
                + " are always shown in red.  The measurement value is selected with the \"" + MEASUREMENT_FOR_COLOUR
                + "\" parameter.</li>"

                + "<li>\"" + ColourModes.PARENT_ID
                + "\" Colour is quasi-randomly selected based on the ID number of a parent of this object.  "
                + "The colour used for a specific ID number will always be the same and is calculated using the equation <i>hue = (ID * 1048576 % 255) / 255</i>.  "
                + "The parent object is selected with the \"" + PARENT_OBJECT_FOR_COLOUR + "\" parameter.</li>"

                + "<li>\"" + ColourModes.PARENT_MEASUREMENT_VALUE
                + "\" Colour is determined by a measurement value of a parent of this object.  "
                + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                + "with the smallest measurement is shown in red and the object with the largest, in cyan.  Objects either missing the relevant measurement or without "
                + "the relevant parent are always shown in red.  The parent object is selected with the \""
                + PARENT_OBJECT_FOR_COLOUR + "\" parameter and the measurement " + "value is selected with the \""
                + MEASUREMENT_FOR_COLOUR + "\" parameter.</li>"

                + "<li>\"" + ColourModes.PARTNER_COUNT
                + "\"  Colour is determined by the number of partners each object has.  "
                + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                + "with the fewest partners is shown in red and the object with the most, in cyan.  Objects without any partners are always shown in red.  "
                + "Partner objects used for counting are selected with the \"" + PARTNER_OBJECTS_FOR_COLOUR
                + "\" parameter.</li>"

                + "<li>\"" + ColourModes.RANDOM_COLOUR + "\" Colour is randomly selected for each object.  "
                + "Unlike the \"" + ColourModes.ID
                + "\" option, the colours generated here will be different for each evaluation of the module.</li>"

                + "<li>\"" + ColourModes.SINGLE_COLOUR
                + "\" (default option) Colour is fixed to one of a predetermined list of colours.  All objects "
                + " will be assigned the same overlay colour.  The colour is chosen using the \"" + SINGLE_COLOUR
                + "\" parameter.</li></ul>");

        parameters.get(COLOUR_MAP)
                .setDescription("Colourmap used for colour gradients.  This parameter is used if \""
                        + COLOUR_MODE
                        + "\" is set to any mode which yields a range of colours (e.g. measurements or IDs).  Choices are: "
                        + String.join(", ", ColourMaps.ALL) + ".");

        parameters.get(SINGLE_COLOUR)
                .setDescription("Colour for all object overlays to be rendered using.  This parameter is used if \""
                        + COLOUR_MODE + "\" is set to \"" + ColourModes.SINGLE_COLOUR + "\".  Choices are: "
                        + String.join(", ", SingleColours.ALL) + ".");

        parameters.get(CHILD_OBJECTS_FOR_COLOUR).setDescription(
                "Object collection used to determine the colour based on number of children per object when \""
                        + COLOUR_MODE + "\" is set to \"" + ColourModes.CHILD_COUNT
                        + "\".  These objects will be children of the input objects.");

        parameters.get(MEASUREMENT_FOR_COLOUR)
                .setDescription("Measurement used to determine the colour when \"" + COLOUR_MODE
                        + "\" is set to either \"" + ColourModes.MEASUREMENT_VALUE + "\" or \""
                        + ColourModes.PARENT_MEASUREMENT_VALUE + "\".");

        parameters.get(PARENT_OBJECT_FOR_COLOUR).setDescription(
                "Object collection used to determine the colour based on either the ID or measurement value "
                        + " of a parent object when \"" + COLOUR_MODE + "\" is set to either  \""
                        + ColourModes.PARENT_ID + "\" or \"" + ColourModes.PARENT_MEASUREMENT_VALUE
                        + "\".  These objects will be parents of the input objects.");

        parameters.get(PARTNER_OBJECTS_FOR_COLOUR).setDescription(
                "Object collection used to determine the colour based on number of partners per object when \""
                        + COLOUR_MODE + "\" is set to \"" + ColourModes.PARTNER_COUNT
                        + "\".  These objects will be partners of the input objects.");

        parameters.get(OPACITY).setDescription(
                "Opacity of the overlay to be rendered.  This is a value between 0 (totally transparent) and 100 (totally opaque).");

        parameters.get(RANGE_MINIMUM_MODE).setDescription(
                "Controls whether the minimum range for displayed colours is set automatically to the minimum available value (e.g. the smallest measurement being rendered), or whether it is defined manually by the \""
                        + MINIMUM_VALUE + "\" parameter.");

        parameters.get(MINIMUM_VALUE).setDescription("When \"" + RANGE_MINIMUM_MODE + "\" is set to \""
                + RangeModes.MANUAL
                + "\", this is the minimum value that will be displayed as a unique colour.  All values smaller than this will be displayed with the same colour.");

        parameters.get(RANGE_MAXIMUM_MODE).setDescription(
                "Controls whether the maximum range for displayed colours is set automatically to the maximum available value (e.g. the largest measurement being rendered), or whether it is defined manually by the \""
                        + MAXIMUM_VALUE + "\" parameter.");

        parameters.get(MAXIMUM_VALUE).setDescription("When \"" + RANGE_MAXIMUM_MODE + "\" is set to \""
                + RangeModes.MANUAL
                + "\", this is the maximum value that will be displayed as a unique colour.  All values larger than this will be displayed with the same colour.");

    }
}
