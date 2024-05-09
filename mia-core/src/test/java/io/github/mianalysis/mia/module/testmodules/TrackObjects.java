package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
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

public class TrackObjects extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TRACK_OBJECTS = "Output track objects";
    public static final String SPATIAL_SEPARATOR = "Spatial cost";
    public static final String LINKING_METHOD = "Linking method";
    public static final String MINIMUM_OVERLAP = "Minimum overlap";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String TEMPORAL_SEPARATOR = "Temporal cost";
    public static final String FRAME_GAP_WEIGHTING = "Frame gap weighting";
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";
    public static final String FAVOUR_ESTABLISHED_TRACKS = "Favour established tracks";
    public static final String TRACK_LENGTH_WEIGHTING = "Track length weighting";
    public static final String VOLUME_SEPARATOR = "Volume cost";
    public static final String USE_VOLUME = "Use volume (minimise volume change)";
    public static final String VOLUME_WEIGHTING = "Volume weighting";
    public static final String MAXIMUM_VOLUME_CHANGE = "Maximum volume change (px^3)";
    public static final String DIRECTION_SEPARATOR = "Direction cost";
    public static final String DIRECTION_WEIGHTING_MODE = "Direction weighting mode";
    public static final String ORIENTATION_RANGE_MODE = "Orientation range mode";
    public static final String PREFERRED_DIRECTION = "Preferred direction";
    public static final String DIRECTION_TOLERANCE = "Direction tolerance";
    public static final String DIRECTION_WEIGHTING = "Direction weighting";
    public static final String MEASUREMENT_SEPARATOR = "Measurement cost";
    public static final String USE_MEASUREMENT = "Use measurement (minimise change)";
    public static final String MEASUREMENT = "Measurement";
    public static final String MEASUREMENT_WEIGHTING = "Measurement weighting";
    public static final String MAXIMUM_MEASUREMENT_CHANGE = "Maximum measurement change";

    public TrackObjects(Modules modules) {
        super("Track objects", modules);
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

    public interface Measurements {
        String TRACK_PREV_ID = "TRACKING // PREVIOUS_OBJECT_IN_TRACK_ID";
        String TRACK_NEXT_ID = "TRACKING // NEXT_OBJECT_IN_TRACK_ID";

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

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputTrackObjectsP(TRACK_OBJECTS, this));

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

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));

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
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        ObjMeasurementRef trackPrevID = objectMeasurementRefs.getOrPut(Measurements.TRACK_PREV_ID);
        ObjMeasurementRef trackNextID = objectMeasurementRefs.getOrPut(Measurements.TRACK_NEXT_ID);

        trackPrevID.setObjectsName(inputObjectsName);
        trackNextID.setObjectsName(inputObjectsName);

        returnedRefs.add(trackPrevID);
        returnedRefs.add(trackNextID);

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
