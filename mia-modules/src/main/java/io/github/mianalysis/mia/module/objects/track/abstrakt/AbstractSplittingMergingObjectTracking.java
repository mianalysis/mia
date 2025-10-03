package io.github.mianalysis.mia.module.objects.track.abstrakt;

import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.system.Status;

public abstract class AbstractSplittingMergingObjectTracking extends AbstractObjectTracking {
    public static final String ALLOW_TRACK_SPLITTING = "Allow track splitting";

    public static final String ALLOW_TRACK_MERGING = "Allow track merging";

    public static final String TRACK_SEGMENT_OBJECTS = "Track segment objects";

    public AbstractSplittingMergingObjectTracking(String name, Modules modules) {
        super(name, modules);
    }

    public static void addTrackSegmentRelationships(Obj inputObject, Obj trackSegmentObject, String trackObjectsName) {
        inputObject.addParent(trackSegmentObject);
        trackSegmentObject.addChild(inputObject);

        // Getting track and reassigning associations
        Obj trackObject = inputObject.getParent(trackObjectsName);
        trackObject.removeChild(inputObject);
        inputObject.removeParent(trackObject.getName());
        trackObject.addChild(trackSegmentObject);
        trackSegmentObject.addParent(trackObject);

    }

    public static void addPreviousObjectsToTrackSegment(Obj inputObject, Obj trackSegmentObject,
            String trackObjectsName) {
        // Adding to previous partners as long as that partner doesn't already have an
        // assigned track segment
        Objs previousObjects = inputObject.getPreviousPartners(inputObject.getName());

        // If the current object was the result of a merge (i.e. multiple previous
        // partners), don't assign track fragment
        if (previousObjects.size() != 1)
            return;

        // Adding relationships
        Obj previousObject = previousObjects.getFirst();

        // If this object has already been assigned, skip it
        if (previousObject.getParent(trackSegmentObject.getName()) != null)
            return;

        // If the previous object splits at the next step, don't assign any further
        if (previousObject.getNextPartners(inputObject.getName()).size() != 1)
            return;

        addTrackSegmentRelationships(previousObject, trackSegmentObject, trackObjectsName);

        // Processing previous partners of the previous object
        addPreviousObjectsToTrackSegment(previousObject, trackSegmentObject, trackObjectsName);

    }

    public static void addNextObjectsToTrackSegment(Obj inputObject, Obj trackSegmentObject,
            String trackObjectsName) {
        // Adding to next partners as long as that partner doesn't already have an
        // assigned track segment
        Objs nextObjects = inputObject.getNextPartners(inputObject.getName());

        // If the current object splits at the next step, don't assign any further
        if (nextObjects.size() != 1)
            return;

        // Adding relationships
        Obj nextObject = nextObjects.getFirst();

        // If this object has already been assigned, skip it
        if (nextObject.getParent(trackSegmentObject.getName()) != null)
            return;

        // If the next object was the result of a merge (i.e. multiple previous
        // partners), don't assign track fragment
        if (nextObject.getPreviousPartners(inputObject.getName()).size() != 1)
            return;

        addTrackSegmentRelationships(nextObject, trackSegmentObject, trackObjectsName);

        // Processing next partners of the next object
        addNextObjectsToTrackSegment(nextObject, trackSegmentObject, trackObjectsName);

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        boolean allowSplit = parameters.getValue(ALLOW_TRACK_SPLITTING, workspace);
        boolean allowMerge = parameters.getValue(ALLOW_TRACK_MERGING, workspace);
        String trackSegmentObjectsName = parameters.getValue(TRACK_SEGMENT_OBJECTS, workspace);

        // Getting objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Objs trackObjects = new Objs(trackObjectsName, inputObjects);
        workspace.addObjects(trackObjects);

        // If there are no input objects, create a blank track set and skip this module
        if (inputObjects == null)
            return Status.PASS;
        if (inputObjects.size() == 0)
            return Status.PASS;

        // Clearing previous relationships and measurements (in case module has been
        // generateModuleList before)
        for (Obj inputObj : inputObjects.values()) {
            inputObj.removeParent(trackObjectsName);
            inputObj.removeParent(trackSegmentObjectsName);
            inputObj.removePartners(inputObjectsName);
        }

        SpotCollection spotCollection = createSpotCollection(inputObjects, true);
        SpotTracker spotTracker = getSpotTracker(spotCollection, workspace);

        process(inputObjects, trackObjects, spotCollection, spotTracker);

        addSpotMeasurements(inputObjects, spotCollection);

        if (allowSplit || allowMerge) {
            Objs trackSegmentObjects = new Objs(trackSegmentObjectsName, inputObjects);
            workspace.addObjects(trackSegmentObjects);

            // Inserting track segments between tracks and input objects
            // Iterating over each spot, adding its partners to the current segment unless
            // they are already in one
            for (Obj inputObject : inputObjects.values()) {
                // If this object has already been assigned, skip it
                if (inputObject.getParent(trackSegmentObjectsName) != null)
                    continue;

                // Creating a new track segment object
                Obj trackSegmentObject = trackSegmentObjects.createAndAddNewObject(new PointListFactory());

                addTrackSegmentRelationships(inputObject, trackSegmentObject, trackObjectsName);

                // Propagating track segments until a split or merge event is reached
                addPreviousObjectsToTrackSegment(inputObject, trackSegmentObject, trackObjectsName);
                addNextObjectsToTrackSegment(inputObject, trackSegmentObject, trackObjectsName);

            }
        }

        // If selected, showing an overlay of the tracked objects
        if (showOutput)
            if (allowSplit || allowMerge)
                showObjects(inputObjects, trackSegmentObjectsName);
            else
                showObjects(inputObjects, trackObjectsName);

        // Adding track objects to the workspace
        writeStatus("Assigned " + trackObjects.size() + " tracks");

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new BooleanP(ALLOW_TRACK_SPLITTING, this, false));
        parameters.add(new BooleanP(ALLOW_TRACK_MERGING, this, false));
        parameters.add(new OutputTrackObjectsP(TRACK_SEGMENT_OBJECTS, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(ALLOW_TRACK_SPLITTING));
        returnedParameters.add(parameters.getParameter(ALLOW_TRACK_MERGING));
        if (((Boolean) parameters.getValue(ALLOW_TRACK_SPLITTING, workspace))
                || ((Boolean) parameters.getValue(ALLOW_TRACK_MERGING, workspace)))
            returnedParameters.add(parameters.getParameter(TRACK_SEGMENT_OBJECTS));

        return returnedParameters;

    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        // Completely overrides superclass definition as we don't want those
        // relationships

        WorkspaceI workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        if (((Boolean) parameters.getValue(ALLOW_TRACK_SPLITTING, workspace))
                || ((Boolean) parameters.getValue(ALLOW_TRACK_MERGING, workspace))) {
            String trackSegmentObjectsName = parameters.getValue(TRACK_SEGMENT_OBJECTS, workspace);

            returnedRelationships.add(parentChildRefs.getOrPut(trackObjectsName, trackSegmentObjectsName));
            returnedRelationships.add(parentChildRefs.getOrPut(trackSegmentObjectsName, inputObjectsName));

        } else {
            returnedRelationships.add(parentChildRefs.getOrPut(trackObjectsName, inputObjectsName));

        }

        return returnedRelationships;

    }
}
