package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.TreeMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.relate.RelateManyToOne;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 27/01/2025.
 */

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureDistancesToNeighbours extends Module {

    public static final String INPUT_SEPARATOR = "Objects input";

    public static final String INPUT_OBJECTS = "Input objects";

    public static final String RELATIONSHIP_MODE = "Relationship mode";

    public static final String NEIGHBOUR_OBJECTS = "Neighbour objects";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship settings";

    public static final String REFERENCE_MODE = "Reference mode";

    public static final String CALCULATE_WITHIN_PARENT = "Only calculate for objects in same parent";

    public static final String PARENT_OBJECTS = "Parent objects";

    public static final String LIMIT_LINKING_DISTANCE = "Limit linking distance";

    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance";

    public static final String CALIBRATED_DISTANCE = "Calibrated distance";

    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public static final String NUMBER_OF_NEIGHBOURS = "Number of neighbours";

    public static final String ADD_NEIGHBOURS_AS_PARTNERS = "Add neighbours as partners";

    public interface RelationshipModes {
        String WITHIN_SAME_SET = "Within same object set";
        String DIFFERENT_SET = "Different object set";

        String[] ALL = new String[] { WITHIN_SAME_SET, DIFFERENT_SET };

    }

    public interface ReferenceModes {
        String CENTROID_2D = "Centroid (2D)";
        String CENTROID_3D = "Centroid (3D)";
        String SURFACE_2D = "Surface (2D)";
        String SURFACE_3D = "Surface (3D)";

        String[] ALL = new String[] { CENTROID_2D, CENTROID_3D, SURFACE_2D, SURFACE_3D };

    }

    public interface InsideOutsideModes extends RelateManyToOne.InsideOutsideModes {
    }

    public interface Measurements {
        String MAX_DISTANCE_PX = "N${N}_MAX_DISTANCE_(PX)";
        String MAX_DISTANCE_CAL = "N${N}_MAX_DISTANCE_(${SCAL})";
        String MEAN_DISTANCE_PX = "N${N}_MEAN_DISTANCE_(PX)";
        String MEAN_DISTANCE_CAL = "N${N}_MEAN_DISTANCE_(${SCAL})";
        String MIN_DISTANCE_PX = "N${N}_MIN_DISTANCE_(PX)";
        String MIN_DISTANCE_CAL = "N${N}_MIN_DISTANCE_(${SCAL})";
        String STD_DISTANCE_PX = "N${N}_STD_DISTANCE_(PX)";
        String STD_DISTANCE_CAL = "N${N}_STD_DISTANCE_(${SCAL})";
        String N_NEIGHBOURS = "N${N}_N_NEIGHBOURS";

    }

    public MeasureDistancesToNeighbours(Modules modules) {
        super("Measure distances to neighbours", modules);
    }

    public static String getFullName(String measurement, String neighbourObjectsName, String referenceMode,
            int number) {
        measurement = measurement.replace("${N}", String.valueOf(number));
        return "NEIGHBOUR_DISTANCES // " + neighbourObjectsName + " // " + referenceMode + " // " + measurement;
    }

    public static TreeMap<Double, Object[]> getNearestNeighbours(Obj inputObject, Objs testObjects,
            String referenceMode,
            double maximumLinkingDistance, boolean linkInSameFrame, int nNeighbours) {
        TreeMap<Double, Object[]> nearestNeighbours = new TreeMap<>();

        if (testObjects == null)
            return null;

        for (Obj testObject : testObjects.values()) {
            // Don't compare an object to itself
            if (testObject == inputObject)
                continue;

            // Check if we should only be comparing objects in same timepoint
            if (linkInSameFrame && (inputObject.getT() != testObject.getT()))
                continue;

            double dist;
            switch (referenceMode) {
                case ReferenceModes.CENTROID_2D:
                    dist = inputObject.getCentroidSeparation(testObject, true, true);
                    break;
                default:
                case ReferenceModes.CENTROID_3D:
                    dist = inputObject.getCentroidSeparation(testObject, true, false);
                    break;
                case ReferenceModes.SURFACE_2D:
                    dist = inputObject.getSurfaceSeparation(testObject, true, true, false, false);
                    break;
                case ReferenceModes.SURFACE_3D:
                    dist = inputObject.getSurfaceSeparation(testObject, true, false, false, false);
                    break;
            }

            if (Math.abs(dist) > maximumLinkingDistance)
                continue;

            // Add this object if it's closer than the previous max or there aren't the
            // right number of neighbours yet
            if (nearestNeighbours.size() < nNeighbours) {
                nearestNeighbours.put(Math.abs(dist), new Object[] { testObject, dist });
            } else if (Math.abs(dist) < nearestNeighbours.lastKey()) {
                nearestNeighbours.remove(nearestNeighbours.lastKey());
                nearestNeighbours.put(Math.abs(dist), new Object[] { testObject, dist });
            }
        }

        return nearestNeighbours;

    }

    public void addMeasurements(Obj inputObject, CumStat cs, String referenceMode,
            String nearestNeighbourName, int nNeighbours) {

        // Adding details of the nearest neighbour to the input object's measurements
        if (cs == null) {
            String name = getFullName(Measurements.MAX_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.MAX_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.MEAN_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.MEAN_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.MIN_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.MIN_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.STD_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.STD_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

            name = getFullName(Measurements.N_NEIGHBOURS, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            double dppXY = inputObject.getDppXY();

            String name = getFullName(Measurements.MAX_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getMax() * dppXY));

            name = getFullName(Measurements.MAX_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getMax()));

            name = getFullName(Measurements.MEAN_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getMean() * dppXY));

            name = getFullName(Measurements.MEAN_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getMean()));

            name = getFullName(Measurements.MIN_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getMin() * dppXY));

            name = getFullName(Measurements.MIN_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getMin()));

            name = getFullName(Measurements.STD_DISTANCE_CAL, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getStd() * dppXY));

            name = getFullName(Measurements.STD_DISTANCE_PX, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getStd()));

            name = getFullName(Measurements.N_NEIGHBOURS, nearestNeighbourName, referenceMode, nNeighbours);
            inputObject.addMeasurement(new Measurement(name, cs.getN()));

        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measures distances to the closest N objects in the specified input collection.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE, workspace);
        String neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        boolean calculateWithinParent = parameters.getValue(CALCULATE_WITHIN_PARENT, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        boolean limitLinkingDistance = parameters.getValue(LIMIT_LINKING_DISTANCE, workspace);
        double maxLinkingDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE, workspace);
        boolean calibratedDistance = parameters.getValue(CALIBRATED_DISTANCE, workspace);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);
        int nNeighbours = parameters.getValue(NUMBER_OF_NEIGHBOURS, workspace);
        boolean addObjectsAsPartners = parameters.getValue(ADD_NEIGHBOURS_AS_PARTNERS, workspace);

        // If there are no input objects skip the module
        if (inputObjects == null)
            return Status.PASS;
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null)
            return Status.PASS;

        // If the maximum linking distance was specified in calibrated units convert it
        // to pixels
        if (limitLinkingDistance && calibratedDistance)
            maxLinkingDist = maxLinkingDist / firstObj.getDppXY();

        // If the linking distance limit isn't to be used, use Double.MAX_VALUE instead
        if (!limitLinkingDistance)
            maxLinkingDist = Double.MAX_VALUE;

        Objs neighbourObjects = null;
        String nearestNeighbourName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjects = workspace.getObjects(neighbourObjectsName);
                nearestNeighbourName = neighbourObjectsName;
                break;

            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjects = inputObjects;
                nearestNeighbourName = inputObjectsName;
                break;
        }

        // Running through each object, calculating the nearest neighbour distance
        int count = 0;
        int total = inputObjects.size();
        for (Obj inputObject : inputObjects.values()) {
            TreeMap<Double, Object[]> nearestNeighbours;
            if (calculateWithinParent) {
                Obj parentObject = inputObject.getParent(parentObjectsName);
                if (parentObject == null) {
                    addMeasurements(inputObject, null, referenceMode, nearestNeighbourName, nNeighbours);
                    continue;
                }

                Objs childObjects = parentObject.getChildren(nearestNeighbourName);
                nearestNeighbours = getNearestNeighbours(inputObject, childObjects, referenceMode, maxLinkingDist,
                        linkInSameFrame, nNeighbours);

            } else {
                nearestNeighbours = getNearestNeighbours(inputObject, neighbourObjects, referenceMode, maxLinkingDist,
                        linkInSameFrame, nNeighbours);
            }

            // Calculating statistics and adding measurements
            CumStat cs = new CumStat();
            for (Object[] entry : nearestNeighbours.values())
                cs.addMeasure((double) entry[1]);

            addMeasurements(inputObject, cs, referenceMode, nearestNeighbourName, nNeighbours);

            // Adding relationships
            if (addObjectsAsPartners) {
                inputObject.removePartners(nearestNeighbourName);
                for (Object[] entry : nearestNeighbours.values()) {
                    Obj partner = (Obj) entry[0];

                    // Importantly, we don't add the reverse partnership, as this may not be one of
                    // the nearest neighbours for that object
                    inputObject.addPartner(partner);

                }
            }

            writeProgressStatus(++count, total, "objects");

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(RELATIONSHIP_MODE, this, RelationshipModes.WITHIN_SAME_SET, RelationshipModes.ALL));
        parameters.add(new InputObjectsP(NEIGHBOUR_OBJECTS, this));

        parameters.add(new SeparatorP(RELATIONSHIP_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.CENTROID_3D, ReferenceModes.ALL));
        parameters.add(new BooleanP(CALCULATE_WITHIN_PARENT, this, false));
        parameters.add(new ParentObjectsP(PARENT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_LINKING_DISTANCE, this, false));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this, 100d));
        parameters.add(new BooleanP(CALIBRATED_DISTANCE, this, false));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, false));
        parameters.add(new IntegerP(NUMBER_OF_NEIGHBOURS, this, 1));
        parameters.add(new BooleanP(ADD_NEIGHBOURS_AS_PARTNERS, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(RELATIONSHIP_MODE));

        switch ((String) parameters.getValue(RELATIONSHIP_MODE, workspace)) {
            case RelationshipModes.DIFFERENT_SET:
                returnedParameters.add(parameters.getParameter(NEIGHBOUR_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        returnedParameters.add(parameters.getParameter(CALCULATE_WITHIN_PARENT));
        if ((boolean) parameters.getValue(CALCULATE_WITHIN_PARENT, workspace)) {
            returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
            ((ParentObjectsP) parameters.getParameter(PARENT_OBJECTS)).setChildObjectsName(inputObjectsName);
        }

        returnedParameters.add(parameters.getParameter(LIMIT_LINKING_DISTANCE));
        if ((boolean) parameters.getValue(LIMIT_LINKING_DISTANCE, workspace)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCE));
        }
        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_NEIGHBOURS));
        returnedParameters.add(parameters.getParameter(ADD_NEIGHBOURS_AS_PARTNERS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        int nNeighbours = parameters.getValue(NUMBER_OF_NEIGHBOURS, workspace);

        String neighbourObjectsName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS, workspace);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }

        String name = getFullName(Measurements.MAX_DISTANCE_CAL, neighbourObjectsName, referenceMode, nNeighbours);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MAX_DISTANCE_PX, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_DISTANCE_CAL, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_DISTANCE_PX, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MIN_DISTANCE_CAL, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MIN_DISTANCE_PX, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_NEIGHBOURS, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.STD_DISTANCE_CAL, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        name = getFullName(Measurements.STD_DISTANCE_PX, neighbourObjectsName, referenceMode, nNeighbours);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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
        Workspace workspace = null;

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE, workspace);

        String neighbourObjectsName = null;
        switch (relationshipMode) {
            case RelationshipModes.DIFFERENT_SET:
                neighbourObjectsName = parameters.getValue(NEIGHBOUR_OBJECTS, workspace);
                break;
            case RelationshipModes.WITHIN_SAME_SET:
                neighbourObjectsName = inputObjectsName;
                break;
        }

        PartnerRefs returnedRefs = new PartnerRefs();

        returnedRefs.add(partnerRefs.getOrPut(inputObjectsName, neighbourObjectsName));

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
