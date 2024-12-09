package io.github.mianalysis.mia.module.objects.track;

import java.util.Map;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.track.abstrakt.AbstractObjectTracking;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TrackObjectsKalman extends AbstractObjectTracking {
    public static final String TRACKING_SEPARATOR = "Tracking controls";
    public static final String SEARCH_RADIUS = "Search radius (px)";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String MAXIMUM_FRAME_GAP = "Maximum frame gap";


    public TrackObjectsKalman(Modules modules) {
        super("Kalman tracking", modules);
    }

    @Override
    protected SpotTracker getSpotTracker(SpotCollection spotCollection, WorkspaceI workspace) {
        SpotTrackerFactory spotTrackerFactory = new KalmanTrackerFactory();
        Map<String, Object> trackerSettings = spotTrackerFactory.getDefaultSettings();

        double searchRadius = parameters.getValue(SEARCH_RADIUS, workspace);
        trackerSettings.put(TrackerKeys.KEY_KALMAN_SEARCH_RADIUS, searchRadius);

        double maxLinkingDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE, workspace);
        trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkingDist);

        int maxFrameGap = parameters.getValue(MAXIMUM_FRAME_GAP, workspace);
        trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGap);

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
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(TRACKING_SEPARATOR, this));
        parameters.add(new DoubleP(SEARCH_RADIUS, this, TrackerKeys.DEFAULT_KALMAN_SEARCH_RADIUS));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this, TrackerKeys.DEFAULT_LINKING_MAX_DISTANCE));
        parameters.add(new IntegerP(MAXIMUM_FRAME_GAP, this, TrackerKeys.DEFAULT_GAP_CLOSING_MAX_FRAME_GAP));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(TRACKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SEARCH_RADIUS));
        returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
        returnedParameters.add(parameters.getParameter(MAXIMUM_FRAME_GAP));

        return returnedParameters;

    }
}
