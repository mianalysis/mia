package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

/**
 * Created by sc13967 on 22/06/2017.
 */
public class CalculateNearestNeighbour extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RELATIONSHIP_MODE = "Relationship mode";
    public static final String NEIGHBOUR_OBJECTS = "Neighbour objects";
    public static final String CALCULATE_WITHIN_PARENT = "Only calculate for objects in same parent";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String LIMIT_LINKING_DISTANCE = "Limit linking distance";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance";
    public static final String CALIBRATED_DISTANCE = "Calibrated distance";

    public interface RelationshipModes {
        String WITHIN_SAME_SET = "Within same object set";
        String DIFFERENT_SET = "Different object set";

        String[] ALL = new String[]{WITHIN_SAME_SET, DIFFERENT_SET};

    }

    public interface Measurements {
        String NN_DISTANCE_PX = "NN_DISTANCE_TO_${NEIGHBOUR}_(PX)";
        String NN_DISTANCE_CAL = "NN_DISTANCE_TO_${NEIGHBOUR}_(${CAL})";
        String NN_ID = "NN_${NEIGHBOUR}_ID";
    }

    public static String getFullName(String measurement,String neighbourObjectsName) {
        return Units.replace("NEAREST_NEIGHBOUR // "+measurement.replace("${NEIGHBOUR}",neighbourObjectsName));
    }


    public Obj getNearestNeighbour(Obj inputObject, ObjCollection testObjects, double maximumLinkingDistance) {
        double minDist = Double.MAX_VALUE;
        Obj nearestNeighbour = null;

        for (Obj testObject : testObjects.values()) {
            if (testObject == inputObject) continue;

            double dist = inputObject.getCentroidSeparation(testObject,true);
            if (dist < minDist) {
                minDist = dist;
                nearestNeighbour = testObject;
            }
        }

        if (minDist > maximumLinkingDistance) return null;

        return nearestNeighbour;

    }

    public void addMeasurements(Obj inputObject, Obj nearestNeighbour, String nearestNeighbourName) {
        // Adding details of the nearest neighbour to the input object's measurements
        if (nearestNeighbour != null) {
            double dppXY = inputObject.getDistPerPxXY();
            double minDist = inputObject.getCentroidSeparation(nearestNeighbour,true);

            String name = getFullName(Measurements.NN_ID,nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, nearestNeighbour.getID()));

            name = getFullName(Measurements.NN_DISTANCE_PX,nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, minDist));

            name = getFullName(Units.replace(Measurements.NN_DISTANCE_CAL),nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, minDist*dppXY));

        } else {
            String name = getFullName(Measurements.NN_ID,nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.NN_DISTANCE_PX,nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Units.replace(Measurements.NN_DISTANCE_CAL),nearestNeighbourName);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

        }
    }


    @Override
    public String getTitle() {
        return "Calculate nearest neighbour";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE);
        String neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS);
        boolean calculateWithinParent = parameters.getValue(CALCULATE_WITHIN_PARENT);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        boolean limitLinkingDistance = parameters.getValue(LIMIT_LINKING_DISTANCE);
        double maxLinkingDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE);
        boolean calibratedDistance = parameters.getValue(CALIBRATED_DISTANCE);

        // If there are no input objects skip the module
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null) return true;

        // If the maximum linking distance was specified in calibrated units convert it to pixels
        if (limitLinkingDistance && calibratedDistance) maxLinkingDist = maxLinkingDist/firstObj.getDistPerPxXY();

        // If the linking distance limit isn't to be used, use Double.MAX_VALUE instead
        if (!limitLinkingDistance) maxLinkingDist = Double.MAX_VALUE;

        ObjCollection neighbourObjects = null;
        String nearestNeighbourName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjects = workspace.getObjectSet(neighbourObjectsName);
                nearestNeighbourName = neighbourObjectsName;
                break;

            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjects = inputObjects;
                nearestNeighbourName = inputObjectsName;
                break;
        }

        // Running through each object, calculating the nearest neighbour distance
        for (Obj inputObject:inputObjects.values()) {
            if (calculateWithinParent) {
                Obj parentObject = inputObject.getParent(parentObjectsName);
                if (parentObject == null) {
                    addMeasurements(inputObject,null,nearestNeighbourName);
                    continue;
                }

                ObjCollection childObjects = parentObject.getChildren(nearestNeighbourName);
                Obj nearestNeighbour = getNearestNeighbour(inputObject,childObjects,maxLinkingDist);
                addMeasurements(inputObject,nearestNeighbour,nearestNeighbourName);

            } else {
                Obj nearestNeighbour = getNearestNeighbour(inputObject,neighbourObjects,maxLinkingDist);
                addMeasurements(inputObject,nearestNeighbour,nearestNeighbourName);
            }
        }

        if (showOutput) inputObjects.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(RELATIONSHIP_MODE, this, RelationshipModes.WITHIN_SAME_SET,RelationshipModes.ALL));
        parameters.add(new InputObjectsP(NEIGHBOUR_OBJECTS, this));
        parameters.add(new BooleanP(CALCULATE_WITHIN_PARENT, this,false));
        parameters.add(new ParentObjectsP(PARENT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_LINKING_DISTANCE, this,false));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this,100d));
        parameters.add(new BooleanP(CALIBRATED_DISTANCE, this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(RELATIONSHIP_MODE));

        switch ((String) parameters.getValue(RELATIONSHIP_MODE)) {
            case RelationshipModes.DIFFERENT_SET:
                returnedParameters.add(parameters.getParameter(NEIGHBOUR_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATE_WITHIN_PARENT));
        if (parameters.getValue(CALCULATE_WITHIN_PARENT)) {
            returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            ((ParentObjectsP) parameters.getParameter(PARENT_OBJECTS)).setChildObjectsName(inputObjectsName);
        }

        returnedParameters.add(parameters.getParameter(LIMIT_LINKING_DISTANCE));
        if (parameters.getValue(LIMIT_LINKING_DISTANCE)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCE));
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE);

        String neighbourObjectsName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }


        String name = getFullName(Units.replace(Measurements.NN_DISTANCE_CAL),neighbourObjectsName);
        MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        name = getFullName(Measurements.NN_DISTANCE_PX,neighbourObjectsName);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        name = getFullName(Measurements.NN_ID,neighbourObjectsName);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputObjectsName);
        reference.setCalculated(true);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}

