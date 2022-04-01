package io.github.mianalysis.mia.module.objects.filter;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;

public abstract class AbstractObjectFilter extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    protected AbstractObjectFilter(String name, Modules modules) {
        super(name, modules);
    }

    public interface FilterModes {
        String DO_NOTHING = "Do nothing";
        String MOVE_FILTERED = "Move filtered objects to new class";
        String REMOVE_FILTERED = "Remove filtered objects";

        String[] ALL = new String[] { DO_NOTHING, MOVE_FILTERED, REMOVE_FILTERED };

    }

    static void processRemoval(Obj inputObject, @Nullable Objs outputObjects, Iterator<Obj> iterator) {
        // Getting existing relationships
        LinkedHashMap<String, Objs> children = inputObject.getChildren();
        LinkedHashMap<String, Obj> parents = inputObject.getParents(true);
        LinkedHashMap<String, Objs> partners = inputObject.getPartners();

        // Removing existing relationships
        inputObject.removeRelationships();

        if (outputObjects != null) {
            outputObjects.add(inputObject);
            inputObject.setObjectCollection(outputObjects);

            // Adding new child relationships
            for (Objs childCollection : children.values()) {
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
            for (Objs partnerCollection : partners.values()) {
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

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
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
                    MIA.log.writeError(e);
                }
            }

            returnedParameters.add(parameters.getParameter(OUTPUT_FILTERED_OBJECTS));

        }

        return returnedParameters;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        
        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            ObjMeasurementRefs references = modules.getObjectMeasurementRefs(inputObjectsName, this);

            for (ObjMeasurementRef reference : references.values()) {
                returnedRefs
                        .add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(filteredObjectsName));
            }
        }
        
        return returnedRefs;

    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        // Where necessary, redirect relationships
        ParentChildRefs returnedRefs = new ParentChildRefs();

        switch ((String) parameters.getValue(FILTER_MODE)) {
            case FilterModes.MOVE_FILTERED:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

                // Getting references up to this location
                ParentChildRefs currentRefs = modules.getParentChildRefs(this);

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
    public PartnerRefs updateAndGetPartnerRefs() {
        PartnerRefs returnedRefs = new PartnerRefs();

        switch ((String) parameters.getValue(FILTER_MODE)) {
            case FilterModes.MOVE_FILTERED:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

                // Getting references up to this location
                PartnerRefs currentRefs = modules.getPartnerRefs(this);

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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    protected void addParameterDescriptions() {
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
