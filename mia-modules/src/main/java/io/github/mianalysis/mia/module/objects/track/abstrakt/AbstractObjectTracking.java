package io.github.mianalysis.mia.module.objects.track.abstrakt;

import java.util.HashMap;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.SpotRoi;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.tracking.SpotTracker;
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
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;

public abstract class AbstractObjectTracking extends Module {
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

    public AbstractObjectTracking(String name, Modules modules) {
        super(name, modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRACK;
    }

    protected interface Features {
        String MIA_ID = "MIA_ID";
    }

    protected abstract SpotTracker getSpotTracker(SpotCollection spotCollection, Workspace workspace);

    protected abstract void addSpotMeasurements(Objs inputObjects, SpotCollection spotCollection);

    public static void process(Objs inputObjects, Objs trackObjects, SpotCollection spotCollection,
            SpotTracker spotTracker) {
        Model model = new Model();
        model.setSpots(spotCollection, false);

        if (!spotTracker.process()) {
            MIA.log.writeError(spotTracker.getErrorMessage());
            return;
        }

        model.setTracks(spotTracker.getResult(), false);
        SimpleWeightedGraph<Spot, DefaultWeightedEdge> result = spotTracker.getResult();

        for (DefaultWeightedEdge edge : result.edgeSet()) {
            Spot sourceSpot = result.getEdgeSource(edge);
            Spot targetSpot = result.getEdgeTarget(edge);

            Obj sourceObj = inputObjects.get(sourceSpot.getFeature(Features.MIA_ID).intValue());
            Obj targetObj = inputObjects.get(targetSpot.getFeature(Features.MIA_ID).intValue());

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

                Obj sourceObj = inputObjects.get(sourceSpot.getFeature(Features.MIA_ID).intValue());
                Obj targetObj = inputObjects.get(targetSpot.getFeature(Features.MIA_ID).intValue());

                sourceObj.addPartner(targetObj);
                targetObj.addPartner(sourceObj);

                sourceObj.addParent(trackObject);
                trackObject.addChild(sourceObj);

                targetObj.addParent(trackObject);
                trackObject.addChild(targetObj);

            }
        }

        // Ensuring every object has a track
        for (Obj inputObject : inputObjects.values()) {
            if (inputObject.getParent(trackObjects.getName()) != null)
                continue;

            Obj trackObject = trackObjects.createAndAddNewObject(VolumeType.POINTLIST);
            inputObject.addParent(trackObject);
            trackObject.addChild(inputObject);

        }
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

    public static Spot convertObjToSpot(Obj obj) {
        double x = obj.getXMean(true);
        double y = obj.getYMean(true);
        double z = obj.getZMean(true, false);

        Spot spot = new Spot(x, y, z, 1, 1);
        spot.putFeature(Features.MIA_ID, (double) obj.getID());

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
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);

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
            inputObj.removePartner(inputObjectsName);
        }

        SpotCollection spotCollection = createSpotCollection(inputObjects, true);
        SpotTracker spotTracker = getSpotTracker(spotCollection, workspace);

        process(inputObjects, trackObjects, spotCollection, spotTracker);

        addSpotMeasurements(inputObjects, spotCollection);

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

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));

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

        returnedRelationships.add(parentChildRefs.getOrPut(trackObjectsName, inputObjectsName));

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
}
