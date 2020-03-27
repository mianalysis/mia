package wbif.sjx.MIA.Module.Visualisation.Overlays;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.ColourFactory;

import java.util.HashMap;

public abstract class Overlay extends Module {
    public static final String COLOUR_MODE = "Colour mode";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String CHILD_OBJECTS_FOR_COLOUR = "Child objects for colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String OPACITY = "Opacity (%)";


    public Overlay(String name, ModuleCollection modules) {
        super(name, modules);
    }

    @Override
    public String getDescription() {
        return null;
    }

    public interface ColourModes {
        String CHILD_COUNT = "Child count";
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";
        String RANDOM_COLOUR = "Random colour";
        String SINGLE_COLOUR = "Single colour";

        String[] ALL = new String[]{CHILD_COUNT,ID,MEASUREMENT_VALUE,PARENT_ID,PARENT_MEASUREMENT_VALUE,RANDOM_COLOUR,SINGLE_COLOUR};

    }

    public interface SingleColours extends ColourFactory.SingleColours {}

    
    public HashMap<Integer,Float> getHues(ObjCollection inputObjects) {
        // Getting colour settings
        String colourMode = parameters.getValue(COLOUR_MODE);
        String singleColour = parameters.getValue(SINGLE_COLOUR);
        String childObjectsForColourName = parameters.getValue(CHILD_OBJECTS_FOR_COLOUR);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR);

        // Generating colours for each object
        switch (colourMode) {
            case ColourModes.SINGLE_COLOUR:
            default:
                return ColourFactory.getSingleColourHues(inputObjects,singleColour);
            case ColourModes.CHILD_COUNT:
                return ColourFactory.getChildCountHues(inputObjects,childObjectsForColourName,true);
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

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(COLOUR_MODE,this, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR,this,SingleColours.WHITE,SingleColours.ALL));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_FOR_COLOUR,this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_COLOUR,this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR,this));
        parameters.add(new DoubleP(OPACITY,this,100));

    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    protected boolean process(Workspace workspace) {
        return false;
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return null;
    }

    public ParameterCollection updateAndGetParameters(String inputObjectsName) {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE)) {
            case ColourModes.CHILD_COUNT:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR));
                ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS_FOR_COLOUR)).setParentObjectsName(inputObjectsName);
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
                break;

            case ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);
                break;

            case ColourModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                colourMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_COLOUR));

                break;
        }

        returnedParameters.add(parameters.getParameter(OPACITY));

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

}
