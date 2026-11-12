package io.github.mianalysis.mia.module.objects.track;

import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_MERGING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_SPLITTING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_GAP_CLOSING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_MAX_DISTANCE;

import java.util.Map;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import fiji.plugin.trackmate.tracking.jaqaman.SparseLAPTrackerFactory;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.track.abstrakt.AbstractSplittingMergingObjectTracking;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TrackObjectsCentroids extends AbstractSplittingMergingObjectTracking {
    public static final String TRACKING_SEPARATOR = "Tracking controls";
    public static final String MAX_DISTANCE = "Max. linking distance (px)";
    public static final String ALLOW_GAP_CLOSING = "Allow gap closing";
    public static final String MAX_FRAME_GAP = "Max. frame gap";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Max. gap-closing distance (px)";

    public TrackObjectsCentroids(Modules modules) {
        super("Track object centroids", modules);
    }

    @Override
    protected SpotTracker getSpotTracker(SpotCollection spotCollection, Workspace workspace) {
        SpotTrackerFactory spotTrackerFactory = new SparseLAPTrackerFactory();
        Map<String, Object> trackerSettings = spotTrackerFactory.getDefaultSettings();

        double maxDist = parameters.getValue(MAX_DISTANCE, workspace);
        trackerSettings.put(KEY_LINKING_MAX_DISTANCE, maxDist);

        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP, workspace);
        boolean allowGapClosing = parameters.getValue(ALLOW_GAP_CLOSING, workspace);
        trackerSettings.put(KEY_ALLOW_GAP_CLOSING, allowGapClosing);
        trackerSettings.put(KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGap);
        double gapClosingMaxDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE, workspace);
        trackerSettings.put(KEY_GAP_CLOSING_MAX_DISTANCE, gapClosingMaxDist);

        boolean allowSplit = parameters.getValue(ALLOW_TRACK_SPLITTING, workspace);
        trackerSettings.put(KEY_ALLOW_TRACK_SPLITTING, allowSplit);
        
        boolean allowMerge = parameters.getValue(ALLOW_TRACK_MERGING, workspace);
        trackerSettings.put(KEY_ALLOW_TRACK_MERGING, allowMerge);

        return spotTrackerFactory.create(spotCollection, trackerSettings);

    }

    @Override
    protected void addSpotMeasurements(Objs inputObjects, SpotCollection spotCollection) {
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(TRACKING_SEPARATOR, this));
        parameters.add(new DoubleP(MAX_DISTANCE, this, 20));
        parameters.add(new DoubleP(GAP_CLOSING_MAX_DISTANCE, this, 20));
        parameters.add(new BooleanP(ALLOW_GAP_CLOSING, this, true));
        parameters.add(new IntegerP(MAX_FRAME_GAP, this, 0));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(TRACKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MAX_DISTANCE));

        returnedParameters.add(parameters.getParameter(ALLOW_GAP_CLOSING));
        if ((Boolean) parameters.getValue(ALLOW_GAP_CLOSING, null)) {
            returnedParameters.add(parameters.getParameter(MAX_FRAME_GAP));
            returnedParameters.add(parameters.getParameter(GAP_CLOSING_MAX_DISTANCE));
        }

        return returnedParameters;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }
}
