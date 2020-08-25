package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.drew.lang.annotations.Nullable;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

public abstract class ObjectFilter extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    protected ObjectFilter(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public interface FilterModes {
        String DO_NOTHING = "Do nothing";
        String MOVE_FILTERED = "Move filtered objects to new class";
        String REMOVE_FILTERED = "Remove filtered objects";

        String[] ALL = new String[] { DO_NOTHING, MOVE_FILTERED, REMOVE_FILTERED };

    }

    public interface FilterMethods {
        String LESS_THAN = "Less than";
        String LESS_THAN_OR_EQUAL_TO = "Less than or equal to";
        String EQUAL_TO = "Equal to";
        String GREATER_THAN_OR_EQUAL_TO = "Greater than or equal to";
        String GREATER_THAN = "Greater than";
        String NOT_EQUAL_TO = "Not equal to";

        String[] ALL = new String[] { LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO,
                GREATER_THAN, NOT_EQUAL_TO };

    }

    public static boolean testFilter(double testValue, double referenceValue, String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return testValue < referenceValue;
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return testValue <= referenceValue;
            case FilterMethods.EQUAL_TO:
                return testValue == referenceValue;
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return testValue >= referenceValue;
            case FilterMethods.GREATER_THAN:
                return testValue > referenceValue;
            case FilterMethods.NOT_EQUAL_TO:
                return testValue != referenceValue;
        }

        return false;

    }

    public static String getFilterMethodSymbol(String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return "<";
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return "<=";
            case FilterMethods.EQUAL_TO:
                return "==";
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return ">=";
            case FilterMethods.GREATER_THAN:
                return ">";
            case FilterMethods.NOT_EQUAL_TO:
                return "!=";
        }

        return "";

    }

    static void processRemoval(Obj inputObject, @Nullable ObjCollection outputObjects, Iterator<Obj> iterator) {
        // Getting existing relationships
        LinkedHashMap<String, ObjCollection> children = inputObject.getChildren();
        LinkedHashMap<String, Obj> parents = inputObject.getParents(true);
        LinkedHashMap<String, ObjCollection> partners = inputObject.getPartners();

        // Removing existing relationships
        inputObject.removeRelationships();

        if (outputObjects != null) {
            inputObject.setName(outputObjects.getName());
            outputObjects.add(inputObject);

            // Adding new child relationships
            for (ObjCollection childCollection : children.values()) {
                for (Obj childObject : childCollection.values()) {
                    inputObject.addChild(childObject);
                    childObject.addParent(inputObject);
                }
            }

            // Adding new parent relationships
            for (Obj parentObject : parents.values()) {
                parentObject.addChild(inputObject);
                inputObject.addParent(parentObject);
            }

            // Adding new partner relationships
            for (ObjCollection partnerCollection : partners.values()) {
                for (Obj partnerObject : partnerCollection.values()) {
                    inputObject.addPartner(partnerObject);
                    partnerObject.addPartner(inputObject);
                }
            }
        }
        iterator.remove();
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.REMOVE_FILTERED, FilterModes.ALL));

        // Creating a generic OutputObjectsP parameter, which will be changed to a
        // specific type (e.g. OutputTrackObjectsP) to match the input object type
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            // Determining type of input object
            String inputObjectName = parameters.getValue(INPUT_OBJECTS);
            Parameter objectSourceParameter = modules.getObjectSource(inputObjectName, this);

            if (objectSourceParameter != null) {
                // Transferring details
                Parameter oldOutputObjects = parameters.get(OUTPUT_FILTERED_OBJECTS);
                Parameter newOutputObjects;
                try {
                    newOutputObjects = objectSourceParameter.createNewInstance(OUTPUT_FILTERED_OBJECTS, this);
                    newOutputObjects.setControl(oldOutputObjects.getControl());
                    newOutputObjects.setDescription(oldOutputObjects.getDescription());
                    newOutputObjects.setExported(oldOutputObjects.isExported());
                    newOutputObjects.setNickname(oldOutputObjects.getNickname());
                    newOutputObjects.setValue(oldOutputObjects.getValue());
                    newOutputObjects.setVisible(oldOutputObjects.isVisible());
                    parameters.add(newOutputObjects);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            returnedParameters.add(parameters.getParameter(OUTPUT_FILTERED_OBJECTS));

        }

        return returnedParameters;

    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        // Where necessary, redirect relationships
        ParentChildRefCollection returnedRefs = new ParentChildRefCollection();

        switch ((String) parameters.getValue(FILTER_MODE)) {
            case FilterModes.MOVE_FILTERED:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

                // Getting references up to this location
                ParentChildRefCollection currentRefs = modules.getParentChildRefs(this);

                // Adding relationships where the input object is the parent
                String[] childNames = currentRefs.getChildNames(inputObjectsName, true);
                for (String childName : childNames)
                    returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, childName));

                // Adding relationships where the input object is the child
                String[] parentNames = currentRefs.getParentNames(inputObjectsName, true);
                for (String parentName : parentNames)
                returnedRefs.add(parentChildRefs.getOrPut(parentName, outputObjectsName));

                break;

        }

        return returnedRefs;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        PartnerRefCollection returnedRefs = new PartnerRefCollection();

        switch ((String) parameters.getValue(FILTER_MODE)) {
            case FilterModes.MOVE_FILTERED:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

                // Getting references up to this location
                PartnerRefCollection currentRefs = modules.getPartnerRefs(this);

                // Adding relationships
                String[] partnerNames = currentRefs.getPartnerNamesArray(inputObjectsName);
                for (String partnerName : partnerNames)
                    returnedRefs.add(partnerRefs.getOrPut(inputObjectsName, partnerName));

                break;
        }

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
