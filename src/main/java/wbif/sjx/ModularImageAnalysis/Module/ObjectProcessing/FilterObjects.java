package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 23/05/2017.
 */
public class FilterObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String REFERENCE_VALUE = "Reference value";

    public interface FilterMethods {
        String REMOVE_ON_IMAGE_EDGE_2D = "Exclude objects on image edge (2D)";
        String MISSING_MEASUREMENTS = "Remove objects with missing measurements";
        String NO_PARENT = "Remove objects without parent";
        String WITH_PARENT = "Remove objects with a parent";
        String MIN_NUMBER_OF_CHILDREN = "Remove objects with few children than:";
        String MEASUREMENTS_SMALLER_THAN = "Remove objects with measurements < than:";
        String MEASUREMENTS_LARGER_THAN = "Remove objects with measurements > than:";

        String[] ALL = new String[]{REMOVE_ON_IMAGE_EDGE_2D, MISSING_MEASUREMENTS, NO_PARENT, WITH_PARENT,
                MIN_NUMBER_OF_CHILDREN, MEASUREMENTS_SMALLER_THAN, MEASUREMENTS_LARGER_THAN};

    }

    @Override
    public String getTitle() {
        return "Filter objects";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String method = parameters.getValue(FILTER_METHOD);

        // Removing objects with a missing measurement (i.e. value set to null)
        switch (method) {
            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                String inputImageName = parameters.getValue(REFERENCE_IMAGE);
                Image inputImage = workspace.getImage(inputImageName);

                int minX = 0;
                int minY = 0;
                int maxX = inputImage.getImagePlus().getWidth()-1;
                int maxY = inputImage.getImagePlus().getHeight()-1;

                Iterator<Obj> iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();

                    ArrayList<Integer> x = inputObject.getXCoords();
                    ArrayList<Integer> y = inputObject.getYCoords();

                    for (int i=0;i<x.size();i++) {
                        if (x.get(i) == minX | x.get(i) == maxX | y.get(i) == minY | y.get(i) == maxY) {
                            inputObject.removeRelationships();
                            iterator.remove();

                            break;

                        }
                    }
                }

                break;

            case FilterMethods.MISSING_MEASUREMENTS:
                String measurement = parameters.getValue(MEASUREMENT);

                iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();

                    if (inputObject.getMeasurement(measurement).getValue() == Double.NaN) {
                        inputObject.removeRelationships();
                        iterator.remove();
                    }
                }

                break;

            case FilterMethods.NO_PARENT:
                String parentObjectName = parameters.getValue(PARENT_OBJECT);

                iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();

                    LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
                    if (parents.get(parentObjectName) == null) {
                        inputObject.removeRelationships();
                        iterator.remove();
                    }
                }

                break;

            case FilterMethods.WITH_PARENT:
                parentObjectName = parameters.getValue(PARENT_OBJECT);

                iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();

                    LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
                    if (parents.get(parentObjectName) != null) {
                        inputObject.removeRelationships();
                        iterator.remove();
                    }
                }

                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
                String childObjectsName = parameters.getValue(CHILD_OBJECTS);
                double minChildN = parameters.getValue(REFERENCE_VALUE);

                iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();
                    ObjCollection childObjects = inputObject.getChildren(childObjectsName);

                    // Removing the object if it has no children
                    if (childObjects == null) {
                        inputObject.removeRelationships();
                        iterator.remove();
                        continue;

                    }

                    // Removing the object if it has too few children
                    if (childObjects.size() < minChildN) {
                        inputObject.removeRelationships();
                        iterator.remove();

                    }
                }

                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
                measurement = parameters.getValue(MEASUREMENT);
                double referenceValue = parameters.getValue(REFERENCE_VALUE);

                iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();

                    // Removing the object if it has no children
                    if (inputObject.getMeasurement(measurement).getValue() < referenceValue) {
                        inputObject.removeRelationships();
                        iterator.remove();

                    }
                }

                break;

            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                measurement = parameters.getValue(MEASUREMENT);
                referenceValue = parameters.getValue(REFERENCE_VALUE);

                iterator = inputObjects.values().iterator();
                while (iterator.hasNext()) {
                    Obj inputObject = iterator.next();

                    // Removing the object if it has no children
                    if (inputObject.getMeasurement(measurement).getValue() > referenceValue) {
                        inputObject.removeRelationships();
                        iterator.remove();
                    }
                }

                break;

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(FILTER_METHOD, Parameter.CHOICE_ARRAY,FilterMethods.REMOVE_ON_IMAGE_EDGE_2D,FilterMethods.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(PARENT_OBJECT, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(CHILD_OBJECTS, Parameter.CHILD_OBJECTS,null,null));
        parameters.add(new Parameter(REFERENCE_VALUE, Parameter.DOUBLE,1.0));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));

        switch ((String) parameters.getValue(FILTER_METHOD)) {
            case FilterMethods.MISSING_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueSource(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

                }
                break;

            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;

            case FilterMethods.NO_PARENT:
            case FilterMethods.WITH_PARENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(PARENT_OBJECT,inputObjectsName);
                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));

                inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(CHILD_OBJECTS,inputObjectsName);
                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                returnedParameters.add(parameters.getParameter(MEASUREMENT));

                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueSource(MEASUREMENT, parameters.getValue(INPUT_OBJECTS));

                }
                break;

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
