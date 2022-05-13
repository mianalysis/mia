// TODO: Could do with spinning the core element of this into a series of Track classes in the Common library
// TODO: Get direction costs working in 3D

package io.github.mianalysis.mia.module.objects.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.features.track.TrackIndexAnalyzer;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import fiji.plugin.trackmate.visualization.trackscheme.TrackScheme;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ProjectImage;
import io.github.mianalysis.mia.module.objects.relate.TrackObjects.Measurements;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

/**
 * Created by Stephen on 13/05/2021.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TrackEditor extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";

    public static final String DISPLAY_SEPARATOR = "Display controls";
    public static final String DISPLAY_IMAGE = "Display image";
    public static final String SHOW_PROJECTED = "Show projected (3D only)";
    public static final String MEASUREMENT_WARNING = "Measurement warning";

    public TrackEditor(Modules modules) {
        super("Track editor", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "Display TrackMate's TrackScheme track editor to allow tracks to be re-linked between detected objects.  TrackScheme also allows individual timepoint objects to be deleted.  TrackScheme represents the connectivity within a track (i.e. between instances in different timepoints) as a graph, which can be adjusted by deleting and drawing lines between each timepoint instance.  Interaction with the TrackScheme window can be directly visualised on the accompanying overlay image.<br><br>"

                + "Full details on using TrackScheme can be found at <a href=\"https://imagej.net/plugins/trackmate/trackscheme\">https://imagej.net/plugins/trackmate/trackscheme</a>.<br><br>"

                + "Note: This module will update the existing spot (individual timepoint instances) and track objects.  As such, any track-related measurements will still be available, but may no longer be valid.  They can be re-calculated by re-running the relevant measurement modules.";

    }

    public static Model initialiseModel(Objs trackObjects, String inputSpotObjectsName) {
        // Initialising the stores for tracks and spots
        SpotCollection spotCollection = new SpotCollection();
        SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Converting tracks to TrackMate Model object
        for (Obj trackObj : trackObjects.values()) {
            // Getting associated objects
            Objs spotObjects = trackObj.getChildren(inputSpotObjectsName);

            // Ensuring the spots are ordered by time
            ArrayList<Obj> orderedSpots = new ArrayList<>(spotObjects.values());
            orderedSpots.sort((s1, s2) -> s1.getT() - s2.getT());

            Spot prevSpot = null;
            for (Obj spotObj : orderedSpots) {
                double xCent = spotObj.getXMean(true);
                double yCent = spotObj.getYMean(true);
                double zCent = spotObj.getZMean(true, false);

                Spot currSpot = new Spot(xCent, yCent, zCent, 3, 1);
                currSpot.putFeature(Spot.POSITION_T, new Double(spotObj.getT()));
                currSpot.putFeature("ID", new Double(spotObj.getID()));

                spotCollection.add(currSpot, spotObj.getT());

                // Adding spot
                if (prevSpot != null) {
                    graph.addVertex(prevSpot);
                    graph.addVertex(currSpot);
                    DefaultWeightedEdge edge = graph.addEdge(prevSpot, currSpot);
                    graph.setEdgeWeight(edge, 1);
                }

                prevSpot = currSpot;

            }
        }

        // Applying spots and tracks to model
        Model model = new Model();
        model.setSpots(spotCollection, false);
        model.setTracks(graph, false);

        return model;

    }

    public static void addSpots(Objs spotObjects, Model model) {
        SpotCollection spots = model.getSpots();

        int maxID = spotObjects.getLargestID();

        for (Spot spot : spots.iterable(false)) {
            if (spot.getFeatures().containsKey("ID"))
                continue;

            Obj spotObj = spotObjects.createAndAddNewObject(VolumeType.POINTLIST,++maxID);
            int x = (int) Math.round(spot.getFeature(Spot.POSITION_X));
            int y = (int) Math.round(spot.getFeature(Spot.POSITION_Y));
            int z = (int) Math.round(spot.getFeature(Spot.POSITION_Z));
            int t = (int) Math.round(spot.getFeature(Spot.FRAME));
            try {
                spotObj.add(x, y, z);
            } catch (PointOutOfRangeException e) {
            }
            spotObj.setT(t);

            spot.putFeature(Spot.POSITION_T, new Double(spotObj.getT()));
            spot.putFeature("ID", new Double(spotObj.getID()));

        }
    }

    public static void removeDeletedSpots(Objs spotObjects, Model model) {
        // Finding any "spots" that have been deleted
        SpotCollection spots = model.getSpots();

        ArrayList<Integer> availableIDs = new ArrayList<>();
        for (Spot spot : spots.iterable(false))
            availableIDs.add((int) Math.round(spot.getFeature("ID")));
                
        Iterator<Integer> iterator = spotObjects.keySet().iterator();
        while (iterator.hasNext()) {
            int ID = iterator.next();
            if (!availableIDs.contains(ID)) {
                Obj spot = spotObjects.get(ID);
                spot.removeRelationships();
                iterator.remove();
            }
        }
    }

    static int getMaxID(Set<Integer> IDs) {
        int maxID = 0;
        for (int ID : IDs)
            maxID = Math.max(ID, maxID);

        return maxID;

    }

    public static void displayTrackScheme(Model model, Image image, boolean showProjected) {
        SelectionModel selectionModel = new SelectionModel(model);

        // Removing the image spatial calibration, since values are in pixel and slice
        // units
        ImagePlus inputIpl = image.getImagePlus().duplicate();
        inputIpl.getCalibration().pixelWidth = 1;
        inputIpl.getCalibration().pixelHeight = 1;
        inputIpl.getCalibration().pixelDepth = 1;

        DisplaySettings displaySettings = new DisplaySettings();
        displaySettings.setTrackColorBy(TrackMateObject.TRACKS, TrackIndexAnalyzer.TRACK_INDEX);

        HyperStackDisplayer stackDisplayer = new HyperStackDisplayer(model, selectionModel, inputIpl, displaySettings);
        stackDisplayer.render();

        ImagePlus projectedIpl = null;
        HyperStackDisplayer projectedDisplayer = null;
        if (showProjected && inputIpl.getNSlices() > 1) {
            projectedIpl = ProjectImage.projectImageInZ(image, "Projected", ProjectImage.ProjectionModes.MAX)
                    .getImagePlus();
            projectedIpl.getCalibration().pixelWidth = 1;
            projectedIpl.getCalibration().pixelHeight = 1;
            projectedIpl.getCalibration().pixelDepth = 1;

            projectedDisplayer = new HyperStackDisplayer(model, selectionModel, projectedIpl, displaySettings);
            projectedDisplayer.render();
        }

        TrackScheme trackScheme = new TrackScheme(model, selectionModel, displaySettings);
        trackScheme.render();

        while (trackScheme.getGUI().isVisible())
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Do nothing as the user has selected this
            }

        // Closing the images
        if (inputIpl.isVisible())
            inputIpl.close();

        if (projectedIpl != null && projectedIpl.isVisible())
            projectedIpl.close();

        stackDisplayer.clear();
        if (projectedDisplayer != null)
            projectedDisplayer.clear();

    }

    static void addSingleTimepointTracks(Set<Integer> trackIDs, Objs spotObjects, Objs trackObjects,
            String inputTrackObjectsName) {
        // Determining the maximum current track ID
        int maxID = 0;
        for (int trackID : trackIDs)
            maxID = Math.max(maxID, trackID);

        // Single timepoint "tracks" aren't assigned track IDs yet, so doing that now
        for (Obj obj : spotObjects.values()) {
            if (obj.getParent(inputTrackObjectsName) == null) {
                Obj trackObject = trackObjects.createAndAddNewObject(VolumeType.POINTLIST, ++maxID + 1);
                obj.addParent(trackObject);
                trackObject.addChild(obj);
                obj.addMeasurement(new Measurement(Measurements.TRACK_NEXT_ID, Double.NaN));
                obj.addMeasurement(new Measurement(Measurements.TRACK_PREV_ID, Double.NaN));
            }
        }

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input track objects
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
        Objs trackObjects = workspace.getObjectSet(inputTrackObjectsName);

        // Getting input spot objects
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);
        Objs spotObjects = workspace.getObjectSet(inputSpotObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(DISPLAY_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        boolean showProjected = parameters.getValue(SHOW_PROJECTED);

        // Converting MIA objects and tracks into TrackMate Model format
        Model model = initialiseModel(trackObjects, inputSpotObjectsName);

        // Displaying the TrackScheme tool
        displayTrackScheme(model, inputImage, showProjected);

        // Adding IDs for new spots
        addSpots(spotObjects, model);

        // Removing deleted spots
        removeDeletedSpots(spotObjects, model);

        // Clearing the existing tracks
        trackObjects.removeChildren(inputSpotObjectsName);
        spotObjects.removeParents(inputTrackObjectsName);
        trackObjects.resetCollection();

        // Creating new tracks
        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);

        for (Integer trackID : trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = trackObjects.createAndAddNewObject(VolumeType.POINTLIST, trackID + 1);
            ArrayList<Spot> spots = new ArrayList<>(trackModel.trackSpots(trackID));

            // Sorting spots based on frame number
            spots.sort((o1, o2) -> {
                double t1 = o1.getFeature(Spot.FRAME);
                double t2 = o2.getFeature(Spot.FRAME);
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });

            // Finding the relevant spot and adding relationships
            Obj prevSpotObj = null;
            for (Spot currSpot : spots) {
                int spotID = (int) Math.round(currSpot.getFeature("ID"));
                Obj currSpotObj = spotObjects.get(spotID);

                // Adding the connection between instance and summary objects
                currSpotObj.addParent(trackObject);
                trackObject.addChild(currSpotObj);

                // Adding references to each other
                if (prevSpotObj == null) {
                    currSpotObj.addMeasurement(new Measurement(Measurements.TRACK_PREV_ID, Double.NaN));
                } else {
                    prevSpotObj.addMeasurement(new Measurement(Measurements.TRACK_NEXT_ID, currSpotObj.getID()));
                    currSpotObj.addMeasurement(new Measurement(Measurements.TRACK_PREV_ID, prevSpotObj.getID()));
                }

                prevSpotObj = currSpotObj;

            }
            prevSpotObj.addMeasurement(new Measurement(Measurements.TRACK_NEXT_ID, Double.NaN));
        }

        addSingleTimepointTracks(trackIDs, spotObjects, trackObjects, inputTrackObjectsName);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_TRACK_OBJECTS, this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS, this));

        parameters.add(new SeparatorP(DISPLAY_SEPARATOR, this));
        parameters.add(new InputImageP(DISPLAY_IMAGE, this));
        parameters.add(new BooleanP(SHOW_PROJECTED, this, false));
        parameters.add(new MessageP(MEASUREMENT_WARNING, this,
                "Previously-acquired measurements for spot and track objects may become invalid.  Please reacquire using the relevant measurement modules.",
                Colours.ORANGE));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(DISPLAY_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE));
        returnedParameters.add(parameters.getParameter(SHOW_PROJECTED));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_WARNING));

        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ((ChildObjectsP) parameters.getParameter(INPUT_SPOT_OBJECTS)).setParentObjectsName(objectName);

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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_TRACK_OBJECTS).setDescription(
                "Track objects that will be edited.  This same object collection will be updated by this module; however, all track objects will effectively be new, so any previous measurements and relationships will be lost.");

        parameters.get(INPUT_SPOT_OBJECTS).setDescription(
                "Spot objects for the selected tracks that will be edited.  These \"spot\" objects are the individual timepoint instances of the tracks.  This same object collection will be updated by this module and all spot objects (unless deleted during editing) will be retained; however, as their associated tracks may be different, any track-related measurements may become invalid.  Track measurements should be re-calculated using the relevant modules.");

        parameters.get(DISPLAY_IMAGE).setDescription(
                "In addition to the graph-based TrackScheme editor, tracks and spots will be displayed as an overlay on this image.");

        parameters.get(SHOW_PROJECTED).setDescription("When selected (and when \"" + DISPLAY_IMAGE
                + "\" has more than one Z-slice), a separate projected image will be displayed.  This can assist understanding track connectivity in 3D.");

    }
}