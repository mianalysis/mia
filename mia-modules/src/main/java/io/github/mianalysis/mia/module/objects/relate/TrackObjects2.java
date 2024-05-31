// TODO: Could do with spinning the core element of this into a series of Track classes in the Common library
// TODO: Get direction costs working in 3D

package io.github.mianalysis.mia.module.objects.relate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.SpotRoi;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.jaqaman.SparseLAPTrackerFactory;
import ij.ImagePlus;
import ij.gui.Roi;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;

/**
 * Created by sc13967 on 20/09/2017.
 */

/**
 * Track objects between frames. Tracks are produced as separate "parent"
 * objects to the "child" spots. Track objects only serve to link different
 * timepoint instances of objects together. As such, track objects store no
 * coordinate information.<br>
 * <br>
 * Uses the <a href="https://imagej.net/plugins/trackmate/">TrackMate</a>
 * implementation of the Jaqaman linear assignment problem solving algorithm
 * (Jaqaman, et al., Nature Methods, 2008). The implementation utilises sparse
 * matrices for calculating costs in order to minimise memory overhead.<br>
 * <br>
 * Note: Leading point determination currently only works in 2D
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TrackObjects2 extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input/output";

    /**
     * Objects present in individual timepoints which will be tracked across
     * multiple frames. These objects will become children of their assigned "track"
     * parent.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Output track objects to be stored in the workspace. These objects will
     * contain no spatial information, rather they act as (parent) linking objects
     * for the individual timepoint instances of the tracked object.
     */
    public static final String TRACK_OBJECTS = "Output track objects";

    public static final String ALLOW_SPLITTING_AND_MERGING = "Allow track splitting and merging";

    public static final String TRACK_SEGMENT_OBJECTS = "Track segment objects";

    /**
    * 
    */
    public static final String SPATIAL_SEPARATOR = "Spatial cost";

    /**
     * The spatial cost for linking objects together:<br>
     * <ul>
     * <li>"Absolute overlap" Tracks will be assigned in order to maxmimise the
     * spatial overlap between objects in adjacent frames. This linking method uses
     * the full 3D volume of the objects being tracked. Note: There is no
     * consideration of distance between objects, so non-overlapping objects next to
     * each other will score equally to non-overlapping objects far away (not taking
     * additional linking weights and restrictions into account).</li>
     * <li>"Centroid" Tracks will be assigned in order to minimise the total
     * distance between object centroids. This linking method doesn't take object
     * size and shape into account (unless included via volume weighting), but will
     * work at all object separations.</li>
     * </ul>
     */
    public static final String LINKING_METHOD = "Linking method";

    /**
     * If "Linking method" is set to "Absolute overlap", this is the minimum
     * absolute spatial overlap (number of coincident pixels/voxels) two objects
     * must have for them to be considered as candidates for linking.
     */
    public static final String MINIMUM_OVERLAP = "Minimum overlap";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";

    /**
    * 
    */
    public static final String TEMPORAL_SEPARATOR = "Temporal cost";

    /**
     * When non-zero, an additional cost will be included that penalises linking
     * objects with large temporal separations. The frame gap between candidate
     * objects will be multiplied by this weight. For example, if calculating
     * spatial costs using centroid spatial separation, a frame gap weight of 1 will
     * equally weight 1 frame of temporal separation to 1 pixel of spatial
     * separation. The larger the weight, the more this frame gap will contribute
     * towards the total linking cost.
     */
    public static final String FRAME_GAP_WEIGHTING = "Frame gap weighting";

    /**
     * Maximum number of missing frames for an object to still be tracked. A single
     * object undetected for longer than this would be identified as two separate
     * tracks.
     */
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";

    /**
     * When selected, points will be preferentially linked to tracks containing more
     * previous points. For example, in cases where an object was detected twice in
     * one timepoint this will favour linking to the original track, rather than
     * establishing the on-going track from the new point.
     */
    public static final String FAVOUR_ESTABLISHED_TRACKS = "Favour established tracks";

    /**
     * If "Favour established tracks" is selected this is the weight assigned to the
     * existing track duration. Track duration costs are calculated as 1 minus the
     * ratio of frames in which the track was detected (up to the previous
     * time-point).
     */
    public static final String TRACK_LENGTH_WEIGHTING = "Track length weighting";

    /**
    * 
    */
    public static final String VOLUME_SEPARATOR = "Volume cost";
    public static final String USE_VOLUME = "Use volume (minimise volume change)";

    /**
     * If "Use volume (minimise volume change)" is enabled, this is the weight
     * assigned to the difference in volume of the candidate objects for linking.
     * The difference in volume between candidate objects is multiplied by this
     * weight. The larger the weight, the more this difference in volume will
     * contribute towards the total linking cost.
     */
    public static final String VOLUME_WEIGHTING = "Volume weighting";
    public static final String MAXIMUM_VOLUME_CHANGE = "Maximum volume change (px^3)";

    /**
    * 
    */
    public static final String DIRECTION_SEPARATOR = "Direction cost";

    /**
     * Controls whether cost terms will be included based on the direction a tracked
     * object is moving in:<br>
     * <ul>
     * <li>"None" No direction-based cost terms will be included.</li>
     * <li>"Absolute orientation 2D" Costs will be calculated based on the absolute
     * orientation a candidate object would be moving in. For example, if objects
     * are known to be moving in one particular direction, this can favour links
     * moving that way rather than the opposite direction.</li>
     * <li>"Relative to previous step" Costs will be calculated based on the
     * previous trajectory of a track. This can be used to minimise rapid changes in
     * direction if tracked objects are expected to move smoothly.</li>
     * </ul>
     */
    public static final String DIRECTION_WEIGHTING_MODE = "Direction weighting mode";

    /**
    * 
    */
    public static final String ORIENTATION_RANGE_MODE = "Orientation range mode";

    /**
     * If "Direction weighting mode" is set to "Absolute orientation 2D", this is
     * the preferred direction that a track should be moving in. Orientation is
     * measured in degree units and is positive above the x-axis and negative below
     * it.
     */
    public static final String PREFERRED_DIRECTION = "Preferred direction";

    /**
     * If using directional weighting ("Direction weighting mode" not set to
     * "None"), this is the maximum deviation in direction from the preferred
     * direction that a candidate object can have. For absolute linking, this is
     * relative to the preferred direction and for relative linking, this is
     * relative to the previous frame.
     */
    public static final String DIRECTION_TOLERANCE = "Direction tolerance";

    /**
     * If using directional weighting ("Direction weighting mode" not set to
     * "None"), the angular difference (in degrees) between the candidate track
     * direction and the reference direction will be muliplied by this weight. The
     * larger the weight, the more this angular difference will contribute towards
     * the total linking cost.
     */
    public static final String DIRECTION_WEIGHTING = "Direction weighting";

    /**
    * 
    */
    public static final String MEASUREMENT_SEPARATOR = "Measurement cost";

    /**
    * 
    */
    public static final String USE_MEASUREMENT = "Use measurement (minimise change)";

    /**
     * If "Use measurement (minimise change)" is selected, this is the measurement
     * (associated with the input objects) for which variation within a track will
     * be minimised.
     */
    public static final String MEASUREMENT = "Measurement";

    /**
     * If "Use measurement (minimise change)" is selected, the difference in
     * measurement associated with a candidate object and the previous instance in a
     * target track will be multiplied by this value. The larger the weight, the
     * more this difference in measurement will contribute towards the total linking
     * cost.
     */
    public static final String MEASUREMENT_WEIGHTING = "Measurement weighting";

    /**
     * If "Use measurement (minimise change)" is selected, this is the maximum
     * amount the measurement can change between consecutive instances in a track.
     * Variations greater than this will result in the track being split into two.
     */
    public static final String MAXIMUM_MEASUREMENT_CHANGE = "Maximum measurement change";

    public TrackObjects2(Modules modules) {
        super("Track objects 2", modules);
        this.deprecated = true;
    }

    public interface LinkingMethods {
        String ABSOLUTE_OVERLAP = "Absolute overlap";
        String CENTROID = "Centroid";

        String[] ALL = new String[] { ABSOLUTE_OVERLAP, CENTROID };
    }

    public interface DirectionWeightingModes {
        String NONE = "None";
        String ABSOLUTE_ORIENTATION = "Absolute orientation 2D";
        String RELATIVE_TO_PREVIOUS_STEP = "Relative to previous step";

        String[] ALL = new String[] { NONE, ABSOLUTE_ORIENTATION, RELATIVE_TO_PREVIOUS_STEP };

    }

    public interface OrientationRangeModes {
        String NINETY = "-90 to 90 degs";
        String ONE_EIGHTY = "-180 to 180 degs";

        String[] ALL = new String[] { NINETY, ONE_EIGHTY };

    }

    public static void showObjects(Objs spotObjects, String trackObjectsName) {
        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(spotObjects, trackObjectsName, true);

        // Creating a parent-ID encoded image of the objects
        Image dispImage = spotObjects.convertToImage(spotObjects.getName(), hues, 32, false);

        // Displaying the overlay
        ImagePlus ipl = dispImage.getImagePlus();
        ipl.setPosition(1, 1, 1);
        ipl.setLut(LUTs.Random(true));
        ipl.updateChannelAndDraw();
        ipl.show();

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static Spot convertObjToSpot(Obj obj) {
        double x = obj.getXMean(true);
        double y = obj.getYMean(true);
        double z = obj.getZMean(true, false);

        Spot spot = new Spot(x, y, z, 1, 1);
        spot.putFeature("MIA_ID", (double) obj.getID());

        return spot;

    }

    public static void addSpotRoi(Spot spot, Obj obj) {
        Roi roi = obj.getProjected().getRoi(0);
        float[] fx = roi.getFloatPolygon().xpoints;
        float[] fy = roi.getFloatPolygon().ypoints;

        double[] x = new double[fx.length];
        double[] y = new double[fy.length];

        for (int i = 0; i < fx.length; i++) {
            x[i] = fx[i];
            y[i] = fy[i];
        }

        spot.setRoi(new SpotRoi(x, y));

    }

    public static SpotCollection createSpotCollection(Objs inputObjects, boolean asROIs) {
        SpotCollection spotCollection = new SpotCollection();

        for (Obj inputObject : inputObjects.values()) {
            Spot spot = convertObjToSpot(inputObject);

            if (asROIs)
                addSpotRoi(spot, inputObject);

            spotCollection.add(spot, inputObject.getT());

        }

        return spotCollection;

    }

    public static void addTrackSegmentRelationships(Obj inputObject, Obj trackSegmentObject, String trackObjectsName) {
        inputObject.addParent(trackSegmentObject);
        trackSegmentObject.addChild(inputObject);

        // Getting track and reassigning associations
        Obj trackObject = inputObject.getParent(trackObjectsName);
        trackObject.removeChild(inputObject);
        inputObject.removeParent(trackObject);
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
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        boolean allowSplitMerge = parameters.getValue(ALLOW_SPLITTING_AND_MERGING, workspace);
        String trackSegmentObjectsName = parameters.getValue(TRACK_SEGMENT_OBJECTS, workspace);

        // Getting objects
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);
        Objs trackObjects = new Objs(trackObjectsName, inputObjects);
        workspace.addObjects(trackObjects);

        Objs trackSegmentObjects = null;
        // if (allowSplitMerge) {
        MIA.log.writeWarning("Need to make this optional again");
        trackSegmentObjects = new Objs(trackSegmentObjectsName, inputObjects);
        workspace.addObjects(trackSegmentObjects);
        // }

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
            inputObj.removePartner(inputObjectsName);
        }

        SpotCollection spotCollection = createSpotCollection(inputObjects, true);

        SparseLAPTrackerFactory trackerFactory = new SparseLAPTrackerFactory();
        Map<String, Object> trackerSettings = trackerFactory.getDefaultSettings();
        trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, true);
        trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, true);

        // Map<String, Object> trackerSettings = trackerFactory.getDefaultSettings();
        // // trackerSettings.entrySet().forEach(MIA.log::writeDebug);
        // trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, true);
        // trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, true);

        // Map<String, Object> settings = new SparseLAPTrackerFactory().getDefaultSettings();
        // final Map< String, Object > ftfSettings = new HashMap<>();
		// ftfSettings.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, 1000 );
		// ftfSettings.put( TrackerKeys.KEY_ALTERNATIVE_LINKING_COST_FACTOR, settings.get( TrackerKeys.KEY_ALTERNATIVE_LINKING_COST_FACTOR ) );
		// ftfSettings.put( TrackerKeys.KEY_LINKING_FEATURE_PENALTIES, settings.get( TrackerKeys.KEY_LINKING_FEATURE_PENALTIES ) );

		// final SparseLAPFrameToFrameTracker frameToFrameLinker = new SparseLAPFrameToFrameTracker( spotCollection, ftfSettings );
        // Model model = new Model();
        // model.setSpots(spotCollection, false);
        // frameToFrameLinker.process();
        // SimpleWeightedGraph<Spot, DefaultWeightedEdge> result = frameToFrameLinker.getResult();
        // model.setTracks(frameToFrameLinker.getResult(), false);
        // // AdvancedKalmanTrackerFactory factory = new AdvancedKalmanTrackerFactory();
        // OverlapTrackerFactory trackerFactory = new OverlapTrackerFactory();

        // Map<String, Object> trackerSettings = trackerFactory.getDefaultSettings();
        // // trackerSettings.entrySet().forEach(MIA.log::writeDebug);
        // trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, true);
        // trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, true);
        // trackerSettings.put(OverlapTrackerFactory.KEY_MIN_IOU,0.001);

        SpotTracker spotTracker = trackerFactory.create(spotCollection, trackerSettings);

        Model model = new Model();
        model.setSpots(spotCollection, false);

        if (!spotTracker.process()) {
            MIA.log.writeError(spotTracker.getErrorMessage());
            return Status.FAIL;
        }

        model.setTracks(spotTracker.getResult(), false);
        SimpleWeightedGraph<Spot, DefaultWeightedEdge> result = spotTracker.getResult();

        // SparseLAPTrackerFactory lapTrackerFactory = new SparseLAPTrackerFactory();
        // Map<String,Object> lapTrackerSettings = lapTrackerFactory.getDefaultSettings();
        // lapTrackerSettings.remove(TrackerKeys.KEY_LINKING_FEATURE_PENALTIES);
        // lapTrackerSettings.remove(TrackerKeys.KEY_LINKING_MAX_DISTANCE);
        // lapTrackerSettings.remove(TrackerKeys.KEY_BLOCKING_VALUE);
        // final SegmentTracker segmentLinker = new SegmentTracker(result1, lapTrackerSettings);
        // if (!segmentLinker.checkInput() || !segmentLinker.process()) {
        //     MIA.log.writeError(segmentLinker.getErrorMessage());
        //     return Status.FAIL;
        // }
        
        // model.setTracks(segmentLinker.getResult(), false);
        // SimpleWeightedGraph<Spot, DefaultWeightedEdge> result = segmentLinker.getResult();

        for (DefaultWeightedEdge edge : result.edgeSet()) {
            Spot sourceSpot = result.getEdgeSource(edge);
            Spot targetSpot = result.getEdgeTarget(edge);

            Obj sourceObj = inputObjects.get(sourceSpot.getFeature("MIA_ID").intValue());
            Obj targetObj = inputObjects.get(targetSpot.getFeature("MIA_ID").intValue());

            sourceObj.addPartner(targetObj);
            targetObj.addPartner(sourceObj);

        }

        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);

        for (Integer trackID : trackIDs) {
            Obj trackObject = trackObjects.createAndAddNewObject(VolumeType.POINTLIST, trackID + 1);

            Set<DefaultWeightedEdge> trackEdges = trackModel.trackEdges(trackID);

            for (DefaultWeightedEdge trackEdge : trackEdges) {
                Spot sourceSpot = result.getEdgeSource(trackEdge);
                Spot targetSpot = result.getEdgeTarget(trackEdge);

                Obj sourceObj = inputObjects.get(sourceSpot.getFeature("MIA_ID").intValue());
                Obj targetObj = inputObjects.get(targetSpot.getFeature("MIA_ID").intValue());

                sourceObj.addPartner(targetObj);
                targetObj.addPartner(sourceObj);

                sourceObj.addParent(trackObject);
                trackObject.addChild(sourceObj);

                targetObj.addParent(trackObject);
                trackObject.addChild(targetObj);

            }
        }

        if (allowSplitMerge) {
            // Inserting track segments between tracks and input objects
            // Iterating over each spot, adding its partners to the current segment unless
            // they are already in one
            for (Obj inputObject : inputObjects.values()) {
                // If this object has already been assigned, skip it
                if (inputObject.getParent(trackSegmentObjectsName) != null)
                    continue;

                // Creating a new track segment object
                Obj trackSegmentObject = trackSegmentObjects.createAndAddNewObject(VolumeType.POINTLIST);
                addTrackSegmentRelationships(inputObject, trackSegmentObject, trackObjectsName);

                // Propagating track segments until a split or merge event is reached
                addPreviousObjectsToTrackSegment(inputObject, trackSegmentObject, trackObjectsName);
                addNextObjectsToTrackSegment(inputObject, trackSegmentObject, trackObjectsName);

            }
        }

        // If selected, showing an overlay of the tracked objects
        if (showOutput)
            showObjects(inputObjects, trackObjectsName);

        // Adding track objects to the workspace
        writeStatus("Assigned " + trackObjects.size() + " tracks");

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputTrackObjectsP(TRACK_OBJECTS, this));
        parameters.add(new BooleanP(ALLOW_SPLITTING_AND_MERGING, this, false));
        parameters.add(new OutputTrackObjectsP(TRACK_SEGMENT_OBJECTS, this));

        parameters.add(new SeparatorP(SPATIAL_SEPARATOR, this));
        parameters.add(new ChoiceP(LINKING_METHOD, this, LinkingMethods.CENTROID, LinkingMethods.ALL));
        parameters.add(new DoubleP(MINIMUM_OVERLAP, this, 1.0));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this, 20.0));

        parameters.add(new SeparatorP(TEMPORAL_SEPARATOR, this));
        parameters.add(new IntegerP(MAXIMUM_MISSING_FRAMES, this, 0));
        parameters.add(new DoubleP(FRAME_GAP_WEIGHTING, this, 0.0));
        parameters.add(new BooleanP(FAVOUR_ESTABLISHED_TRACKS, this, false));
        parameters.add(new DoubleP(TRACK_LENGTH_WEIGHTING, this, 1.0));

        parameters.add(new SeparatorP(VOLUME_SEPARATOR, this));
        parameters.add(new BooleanP(USE_VOLUME, this, false));
        parameters.add(new DoubleP(VOLUME_WEIGHTING, this, 1.0));
        parameters.add(new DoubleP(MAXIMUM_VOLUME_CHANGE, this, 1.0));

        parameters.add(new SeparatorP(DIRECTION_SEPARATOR, this));
        parameters.add(
                new ChoiceP(DIRECTION_WEIGHTING_MODE, this, DirectionWeightingModes.NONE, DirectionWeightingModes.ALL));
        parameters.add(
                new ChoiceP(ORIENTATION_RANGE_MODE, this, OrientationRangeModes.ONE_EIGHTY, OrientationRangeModes.ALL));
        parameters.add(new DoubleP(PREFERRED_DIRECTION, this, 0.0));
        parameters.add(new DoubleP(DIRECTION_TOLERANCE, this, 90.0));
        parameters.add(new DoubleP(DIRECTION_WEIGHTING, this, 1.0));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(USE_MEASUREMENT, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new DoubleP(MEASUREMENT_WEIGHTING, this, 1.0));
        parameters.add(new DoubleP(MAXIMUM_MEASUREMENT_CHANGE, this, 1.0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(ALLOW_SPLITTING_AND_MERGING));
        if ((Boolean) parameters.getValue(ALLOW_SPLITTING_AND_MERGING, workspace))
            returnedParameters.add(parameters.getParameter(TRACK_SEGMENT_OBJECTS));

        returnedParameters.add(parameters.getParameter(SPATIAL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINKING_METHOD));
        switch ((String) parameters.getValue(LINKING_METHOD, workspace)) {
            case LinkingMethods.ABSOLUTE_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP));
                break;

            case LinkingMethods.CENTROID:
                returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
                break;
        }

        returnedParameters.add(parameters.getParameter(TEMPORAL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MAXIMUM_MISSING_FRAMES));
        returnedParameters.add(parameters.getParameter(FRAME_GAP_WEIGHTING));
        returnedParameters.add(parameters.getParameter(FAVOUR_ESTABLISHED_TRACKS));
        if ((boolean) returnedParameters.getValue(FAVOUR_ESTABLISHED_TRACKS, workspace))
            returnedParameters.add(parameters.getParameter(TRACK_LENGTH_WEIGHTING));

        returnedParameters.add(parameters.getParameter(VOLUME_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_VOLUME));
        if ((boolean) returnedParameters.getValue(USE_VOLUME, workspace)) {
            returnedParameters.add(parameters.getParameter(VOLUME_WEIGHTING));
            returnedParameters.add(parameters.getParameter(MAXIMUM_VOLUME_CHANGE));
        }

        returnedParameters.add(parameters.getParameter(DIRECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DIRECTION_WEIGHTING_MODE));
        switch ((String) parameters.getValue(DIRECTION_WEIGHTING_MODE, workspace)) {
            case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                returnedParameters.add(parameters.getParameter(ORIENTATION_RANGE_MODE));
                returnedParameters.add(parameters.getParameter(PREFERRED_DIRECTION));
                returnedParameters.add(parameters.getParameter(DIRECTION_TOLERANCE));
                returnedParameters.add(parameters.getParameter(DIRECTION_WEIGHTING));
                break;

            case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                returnedParameters.add(parameters.getParameter(DIRECTION_TOLERANCE));
                returnedParameters.add(parameters.getParameter(DIRECTION_WEIGHTING));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_MEASUREMENT));
        if ((boolean) returnedParameters.getValue(USE_MEASUREMENT, workspace)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT));
            returnedParameters.add(parameters.getParameter(MEASUREMENT_WEIGHTING));
            returnedParameters.add(parameters.getParameter(MAXIMUM_MEASUREMENT_CHANGE));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
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
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        boolean allowSplitMerge = parameters.getValue(ALLOW_SPLITTING_AND_MERGING, workspace);

        if (allowSplitMerge) {
            String trackSegmentObjectsName = parameters.getValue(TRACK_SEGMENT_OBJECTS, workspace);
            returnedRelationships.add(parentChildRefs.getOrPut(trackObjectsName, trackSegmentObjectsName));
            returnedRelationships.add(parentChildRefs.getOrPut(trackSegmentObjectsName, inputObjectsName));
        } else {
            returnedRelationships.add(parentChildRefs.getOrPut(trackObjectsName, inputObjectsName));
        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;
        PartnerRefs returnedRelationships = new PartnerRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        returnedRelationships.add(partnerRefs.getOrPut(inputObjectsName, inputObjectsName));

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects present in individual timepoints which will be tracked across multiple frames.  These objects will become children of their assigned \"track\" parent.");

        parameters.get(TRACK_OBJECTS).setDescription(
                "Output track objects to be stored in the workspace.  These objects will contain no spatial information, rather they act as (parent) linking  objects for the individual timepoint instances of the tracked object.");

        parameters.get(MAXIMUM_MISSING_FRAMES).setDescription(
                "Maximum number of missing frames for an object to still be tracked.  A single object undetected for longer than this would be identified as two separate tracks.");

        parameters.get(LINKING_METHOD).setDescription("The spatial cost for linking objects together:<br><ul>"

                + "<li>\"" + LinkingMethods.ABSOLUTE_OVERLAP
                + "\" Tracks will be assigned in order to maxmimise the spatial overlap between objects in adjacent frames.  This linking method uses the full 3D volume of the objects being tracked.  Note: There is no consideration of distance between objects, so non-overlapping objects next to each other will score equally to non-overlapping objects far away (not taking additional linking weights and restrictions into account).</li>"

                + "<li>\"" + LinkingMethods.CENTROID
                + "\" Tracks will be assigned in order to minimise the total distance between object centroids.  This linking method doesn't take object size and shape into account (unless included via volume weighting), but will work at all object separations.</li></ul>");

        parameters.get(MINIMUM_OVERLAP).setDescription("If \"" + LINKING_METHOD + "\" is set to \""
                + LinkingMethods.ABSOLUTE_OVERLAP
                + "\", this is the minimum absolute spatial overlap (number of coincident pixels/voxels) two objects must have for them to be considered as candidates for linking.");

        parameters.get(MAXIMUM_LINKING_DISTANCE).setDescription("If \"" + LINKING_METHOD + "\" is set to \""
                + LinkingMethods.CENTROID
                + "\", this is the minimum spatial separation (pixel units) two objects must have for them to be considered as candidates for linking.");

        parameters.get(FRAME_GAP_WEIGHTING).setDescription(
                "When non-zero, an additional cost will be included that penalises linking objects with large temporal separations.  The frame gap between candidate objects will be multiplied by this weight.  For example, if calculating spatial costs using centroid spatial separation, a frame gap weight of 1 will equally weight 1 frame of temporal separation to 1 pixel of spatial separation.  The larger the weight, the more this frame gap will contribute towards the total linking cost.");

        parameters.get(FAVOUR_ESTABLISHED_TRACKS).setDescription(
                "When selected, points will be preferentially linked to tracks containing more previous points.  For example, in cases where an object was detected twice in one timepoint this will favour linking to the original track, rather than establishing the on-going track from the new point.");

        parameters.get(TRACK_LENGTH_WEIGHTING).setDescription("If \"" + FAVOUR_ESTABLISHED_TRACKS
                + "\" is selected this is the weight assigned to the existing track duration.  Track duration costs are calculated as 1 minus the ratio of frames in which the track was detected (up to the previous time-point).");

        parameters.get(USE_VOLUME).setDescription(
                "When enabled, the 3D volume of the objects being linked will contribute towards linking costs.");

        parameters.get(VOLUME_WEIGHTING).setDescription("If \"" + USE_VOLUME
                + "\" is enabled, this is the weight assigned to the difference in volume of the candidate objects for linking.  The difference in volume between candidate objects is multiplied by this weight.  The larger the weight, the more this difference in volume will contribute towards the total linking cost.");

        parameters.get(MAXIMUM_VOLUME_CHANGE).setDescription("If \"" + USE_VOLUME
                + "\" is enabled, the maximum difference in volume between candidate objects can be specified.  This maximum volume change is specified in pixel units.");

        parameters.get(DIRECTION_WEIGHTING_MODE).setDescription(
                "Controls whether cost terms will be included based on the direction a tracked object is moving in:<br><ul>"

                        + "<li>\"" + DirectionWeightingModes.NONE
                        + "\" No direction-based cost terms will be included.</li>"

                        + "<li>\"" + DirectionWeightingModes.ABSOLUTE_ORIENTATION
                        + "\" Costs will be calculated based on the absolute orientation a candidate object would be moving in.  For example, if objects are known to be moving in one particular direction, this can favour links moving that way rather than the opposite direction.</li>"

                        + "<li>\"" + DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP
                        + "\" Costs will be calculated based on the previous trajectory of a track.  This can be used to minimise rapid changes in direction if tracked objects are expected to move smoothly.</li></ul>");

        parameters.get(PREFERRED_DIRECTION).setDescription("If \"" + DIRECTION_WEIGHTING_MODE + "\" is set to \""
                + DirectionWeightingModes.ABSOLUTE_ORIENTATION
                + "\", this is the preferred direction that a track should be moving in.  Orientation is measured in degree units and is positive above the x-axis and negative below it.");

        parameters.get(DIRECTION_TOLERANCE).setDescription("If using directional weighting (\""
                + DIRECTION_WEIGHTING_MODE + "\" not set to \"" + DirectionWeightingModes.NONE
                + "\"), this is the maximum deviation in direction from the preferred direction that a candidate object can have.  For absolute linking, this is relative to the preferred direction and for relative linking, this is relative to the previous frame.");

        parameters.get(DIRECTION_WEIGHTING).setDescription("If using directional weighting (\""
                + DIRECTION_WEIGHTING_MODE + "\" not set to \"" + DirectionWeightingModes.NONE
                + "\"), the angular difference (in degrees) between the candidate track direction and the reference direction will be muliplied by this weight.  The larger the weight, the more this angular difference will contribute towards the total linking cost.");

        parameters.get(USE_MEASUREMENT).setDescription(
                "When selected, an additional cost can be included based on a measurement assigned to each object.  This allows for tracking to favour minimising variation in this measurement.");

        parameters.get(MEASUREMENT).setDescription("If \"" + USE_MEASUREMENT
                + "\" is selected, this is the measurement (associated with the input objects) for which variation within a track will be minimised.");

        parameters.get(MEASUREMENT_WEIGHTING).setDescription("If \"" + USE_MEASUREMENT
                + "\" is selected, the difference in measurement associated with a candidate object and the previous instance in a target track will be multiplied by this value.  The larger the weight, the more this difference in measurement will contribute towards the total linking cost.");

        parameters.get(MAXIMUM_MEASUREMENT_CHANGE).setDescription("If \"" + USE_MEASUREMENT
                + "\" is selected, this is the maximum amount the measurement can change between consecutive instances in a track.  Variations greater than this will result in the track being split into two.");

    }
}
