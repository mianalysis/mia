package io.github.mianalysis.mia.module.objects.track;

import java.util.Map;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.track.abstrakt.AbstractObjectTracking;
import io.github.mianalysis.mia.module.objects.track.abstrakt.AbstractSplittingMergingObjectTracking;
import io.github.mianalysis.mia.module.objects.track.trackmate.OverlapTracker3DFactory;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TrackObjectsOverlap3D extends AbstractSplittingMergingObjectTracking {
    public static final String TRACKING_SEPARATOR = "Tracking controls";
    public static final String MIN_IOU = "Min IoU";

    public interface Measurements {
        String IOU = "TRACKING // IoU";

    }

    protected interface Features extends AbstractObjectTracking.Features {
        String IoU = "IoU";
    }

    public TrackObjectsOverlap3D(Modules modules) {
        super("Overlap tracking 3D", modules);
    }

    @Override
    protected SpotTracker getSpotTracker(SpotCollection spotCollection, Workspace workspace) {
        SpotTrackerFactory spotTrackerFactory = new OverlapTracker3DFactory();
        Map<String, Object> trackerSettings = spotTrackerFactory.getDefaultSettings();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        trackerSettings.put(OverlapTracker3DFactory.KEY_MIA_OBJECTS, inputObjects);

        double minIoU = parameters.getValue(MIN_IOU, workspace);
        trackerSettings.put(OverlapTracker3DFactory.KEY_MIN_IOU, minIoU);

        boolean allowSplit = parameters.getValue(ALLOW_TRACK_SPLITTING, workspace);
        trackerSettings.put(OverlapTracker3DFactory.KEY_ALLOW_TRACK_SPLITTING, allowSplit);

        boolean allowMerge = parameters.getValue(ALLOW_TRACK_MERGING, workspace);
        trackerSettings.put(OverlapTracker3DFactory.KEY_ALLOW_TRACK_MERGING, allowMerge);

        return spotTrackerFactory.create(spotCollection, trackerSettings);

    }

    @Override
    protected void addSpotMeasurements(Objs inputObjects, SpotCollection spotCollection) {
        for (Spot spot : spotCollection.iterable(false)) {
            int MIA_ID = spot.getFeature(Features.MIA_ID).intValue();
            Obj inputObject = inputObjects.get(MIA_ID);
            if (inputObject == null)
                continue;

            double iou = Double.NaN;
            if (spot.getFeatures().containsKey(Features.IoU))
                iou = spot.getFeature(Features.IoU);
            inputObject.addMeasurement(new Measurement(Measurements.IOU, iou));

        }
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(TRACKING_SEPARATOR, this));
        parameters.add(new DoubleP(MIN_IOU, this, 0.5));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(TRACKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MIN_IOU));

        return returnedParameters;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, null);
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        ObjMeasurementRef ref = returnedRefs.getOrPut(Measurements.IOU);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }
}
