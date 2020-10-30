package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.drew.lang.annotations.Nullable;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public abstract class AbstractObjectFilter extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    protected AbstractObjectFilter(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public interface FilterModes {
        String DO_NOTHING = "Do nothing";
        String MOVE_FILTERED = "Move filtered objects to new class";
        String REMOVE_FILTERED = "Remove filtered objects";

        String[] ALL = new String[] { DO_NOTHING, MOVE_FILTERED, REMOVE_FILTERED };

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
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.REMOVE_FILTERED, FilterModes.ALL));

        // Creating a generic OutputObjectsP parameter, which will be changed to a
        // specific type (e.g. OutputTrackObjectsP) to match the input object type
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

        addAbstractParameterDescriptions();

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
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        
        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            ObjMeasurementRefCollection references = modules.getObjectMeasurementRefs(inputObjectsName, this);

            for (ObjMeasurementRef reference : references.values()) {
                returnedRefs
                        .add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(filteredObjectsName));
            }
        }
        
        return returnedRefs;

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

    void addAbstractParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects to be filtered.");

        parameters.get(FILTER_MODE)
                .setDescription("Controls what happens to objects which don't pass the filter:<br><ul>"
                        + "<li>\"" + FilterModes.DO_NOTHING + "\" Retains all input objects, irrespective of whether they passed or failed the filter.  This is useful when also storing the filter results as metadata values (i.e. just counting the number of objects which pass the filter).</li>"

                        + "<li>\"" + FilterModes.MOVE_FILTERED + "\" Objects failing the filter are moved to a new object class.  The name of the class is determined by the \""+OUTPUT_FILTERED_OBJECTS+"\" parameter.  All existing measurements and relationships are carried forward into the new object collection.</li>"

                        + "<li>\"" + FilterModes.REMOVE_FILTERED + "\" (default) Removes objects failing the filter.  Once removed, these objects are unavailable for further use by modules and won't be included in exported results.</li></ul>");

        parameters.get(OUTPUT_FILTERED_OBJECTS).setDescription(
                "New object collection containing input objects which did not pass the filter.  These objects are only stored if \""
                        + FILTER_MODE + "\" is set to \"" + FilterModes.MOVE_FILTERED + "\".");

    }
}
