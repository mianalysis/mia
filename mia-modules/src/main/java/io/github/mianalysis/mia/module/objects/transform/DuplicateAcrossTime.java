package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 29/06/2017.
 */

/**
* Creates a copy of objects across all frames in the specified image stack.  Duplicated objects can either have their own set of coordinates or all share the set from the input object.  While sharing coordinates across all timepoints can be much more memory efficient (no redundant duplication of data is required), any change to the coordinates in one frame will result in the change being mirrored across all timepoints, so this mode should be used with care.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DuplicateAcrossTime extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Input objects to duplicate across multiple timepoints.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Object output";

	/**
	* Output duplicated objects which will be added to the workspace.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";

	/**
	* Controls whether the duplicated objects share the same common coordinate set or each have their own.  A duplicated coordinate set will use a lot less memory (as no redundant duplication of data is required), but should be used with caution, as any change to the coordinates in one frame will result in the change being mirrored across all timepoints.
	*/
    public static final String STORAGE_MODE = "Coordinates storage mode";

	/**
	* 
	*/
    public static final String COMMON_WARNING = "Common warning";


	/**
	* 
	*/
    public static final String FRAME_SEPARATOR = "Frame range";

	/**
	* Specifies the source for the first timepoint the objects will be created for:<br><ul><li>"Fixed value" The first timepoint will be controlled by the value specified in the "Start frame fixed value" parameter.</li><li>"Image measurement" The first timepoint will be taken from a measurement ("Start frame image measurement" parameter) assigned to the image specified by "Start frame image".</li></ul>
	*/
    public static final String START_FRAME_MODE = "Start frame mode";

	/**
	* If "Start frame mode" is set to "Fixed value", this is the fixed value that will be used as the first timepoint for the output duplicated objects.
	*/
    public static final String START_FRAME_FIXED_VALUE = "Start frame fixed value";

	/**
	* If "Start frame mode" is set to "Image measurement", this is the image from which the measurerment "Start frame image measurement" will be taken.
	*/
    public static final String START_FRAME_IMAGE = "Start frame image";

	/**
	* If "Start frame mode" is set to "Image measurement", this is the measurement that will be used as the first timepoint for the duplicated objects.
	*/
    public static final String START_FRAME_IMAGE_MEASUREMENT = "Start frame image measurement";

	/**
	* If "Start frame mode" is set to "Image measurement", the first frame can be offset relative to the specified measurement by this number of frames.  For example, if the provided measurement is 5 and an offset of -1 is used, the first frame in the duplicated set will be 4.
	*/
    public static final String START_OFFSET = "Start frame offset";

	/**
	* Specifies the source for the last timepoint the objects will be created for:<br><ul><li>"Fixed value" The last timepoint will be controlled by the value specified in the "End frame fixed value" parameter.</li><li>"Image measurement" The last timepoint will be taken from a measurement ("End frame image measurement" parameter) assigned to the image specified by "End frame image".</li></ul>
	*/
    public static final String END_FRAME_MODE = "End frame mode";

	/**
	* If "End frame mode" is set to "Fixed value", this is the fixed value that will be used as the last timepoint for the output duplicated objects.
	*/
    public static final String END_FRAME_FIXED_VALUE = "End frame fixed value";

	/**
	* If "End frame mode" is set to "Image measurement", this is the image from which the measurerment "End frame image measurement" will be taken.
	*/
    public static final String END_FRAME_IMAGE = "End frame image";

	/**
	* If "End frame mode" is set to "Image measurement", this is the measurement that will be used as the last timepoint for the duplicated objects.
	*/
    public static final String END_FRAME_IMAGE_MEASUREMENT = "End frame image measurement";

	/**
	* If "End frame mode" is set to "Image measurement", the end frame can be offset relative to the specified measurement by this number of frames.  For example, if the provided measurement is 5 and an offset of -1 is used, the last frame in the duplicated set will be 4.
	*/
    public static final String END_OFFSET = "End frame offset";

    public interface StorageModes {
        final String COMMON_ACROSS_ALL_OBJECTS = "Common";
        final String DUPLICATE = "Duplicate";

        final String[] ALL = new String[] { COMMON_ACROSS_ALL_OBJECTS, DUPLICATE };

    }

    public interface FrameModes {
        final String FIXED_VALUE = "Fixed value";
        final String IMAGE_MEASUREMENT = "Image measurement";

        final String[] ALL = new String[] { FIXED_VALUE, IMAGE_MEASUREMENT };

    }

    public static Objs duplicate(Objs inputObjects, String outputObjectsName, String storageMode,
            int startFrame, int endFrame) {
        // Creating output object collection
        int nFrames = endFrame - startFrame + 1;
        Objs outputObjects = new Objs(outputObjectsName, inputObjects.getSpatialCalibration(),
                nFrames, inputObjects.getFrameInterval(), inputObjects.getTemporalUnit());

        String name = new DuplicateAcrossTime(null).getName();

        // Duplicating objects
        int count = 0;
        for (Obj inputObject : inputObjects.values()) {
            for (int t = startFrame; t <= endFrame; t++) {
                // Creating object for this timepoint
                Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getCoordinateSetFactory());
                outputObject.setT(t);

                // Setting object relationships
                inputObject.addChild(outputObject);
                outputObject.addParent(inputObject);

                // Adding coordinates
                switch (storageMode) {
                    case StorageModes.COMMON_ACROSS_ALL_OBJECTS:
                        outputObject.setCoordinateSet(inputObject.getCoordinateSet());
                        break;

                    case StorageModes.DUPLICATE:
                        for (Point<Integer> point : inputObject.getCoordinateSet()) {
                            try {
                                outputObject.addCoord(point.x, point.y, point.z);
                            } catch (PointOutOfRangeException e) {
                            }
                        }
                        break;
                }
            }

            writeProgressStatus(++count, inputObjects.size(), "objects", name);

        }

        return outputObjects;

    }

    public DuplicateAcrossTime(Modules modules) {
        super("Duplicate objects across time", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Creates a copy of objects across all frames in the specified image stack.  Duplicated objects can either have their own set of coordinates or all share the set from the input object.  While sharing coordinates across all timepoints can be much more memory efficient (no redundant duplication of data is required), any change to the coordinates in one frame will result in the change being mirrored across all timepoints, so this mode should be used with care.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String storageMode = parameters.getValue(STORAGE_MODE, workspace);
        String startFrameMode = parameters.getValue(START_FRAME_MODE, workspace);
        int startFrame = parameters.getValue(START_FRAME_FIXED_VALUE, workspace);
        String startImageName = parameters.getValue(START_FRAME_IMAGE, workspace);
        String startImageMeasurementName = parameters.getValue(START_FRAME_IMAGE_MEASUREMENT, workspace);
        int startOffset = parameters.getValue(START_OFFSET, workspace);
        String endMode = parameters.getValue(END_FRAME_MODE, workspace);
        int endFrame = parameters.getValue(END_FRAME_FIXED_VALUE, workspace);
        String endImageName = parameters.getValue(END_FRAME_IMAGE, workspace);
        String endImageMeasurementName = parameters.getValue(END_FRAME_IMAGE_MEASUREMENT, workspace);
        int endOffset = parameters.getValue(END_OFFSET, workspace);

        // Getting start and end frames
        switch (startFrameMode) {
            case FrameModes.IMAGE_MEASUREMENT:
                Measurement startMeas = workspace.getImage(startImageName).getMeasurement(startImageMeasurementName);
                startFrame = (int) Math.round(startMeas.getValue());
                startFrame = startFrame + startOffset;
                break;
        }

        switch (endMode) {
            case FrameModes.IMAGE_MEASUREMENT:
                Measurement endMeas = workspace.getImage(endImageName).getMeasurement(endImageMeasurementName);
                endFrame = (int) Math.round(endMeas.getValue());
                endFrame = endFrame + endOffset;
                break;
        }

        // Duplicating objects
        Objs outputObjects = duplicate(inputObjects, outputObjectsName, storageMode, startFrame, endFrame);
        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(STORAGE_MODE, this, StorageModes.COMMON_ACROSS_ALL_OBJECTS, StorageModes.ALL));
        parameters.add(new MessageP(COMMON_WARNING, this,
                "\"Common\" coordinate storage will use a single set of coordinates for all copies of an object.  Therefore, changes made to one object will be reflected in all objects.",
                ParameterState.WARNING));

        parameters.add(new SeparatorP(FRAME_SEPARATOR, this));
        parameters.add(new ChoiceP(START_FRAME_MODE, this, FrameModes.FIXED_VALUE, FrameModes.ALL));
        parameters.add(new IntegerP(START_FRAME_FIXED_VALUE, this, 0));
        parameters.add(new InputImageP(START_FRAME_IMAGE, this));
        parameters.add(new ImageMeasurementP(START_FRAME_IMAGE_MEASUREMENT, this));
        parameters.add(new IntegerP(START_OFFSET, this, 0));
        parameters.add(new ChoiceP(END_FRAME_MODE, this, FrameModes.FIXED_VALUE, FrameModes.ALL));
        parameters.add(new IntegerP(END_FRAME_FIXED_VALUE, this, 0));
        parameters.add(new InputImageP(END_FRAME_IMAGE, this));
        parameters.add(new ImageMeasurementP(END_FRAME_IMAGE_MEASUREMENT, this));
        parameters.add(new IntegerP(END_OFFSET, this, 0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParams = new Parameters();

        returnedParams.add(parameters.get(INPUT_SEPARATOR));
        returnedParams.add(parameters.get(INPUT_OBJECTS));

        returnedParams.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParams.add(parameters.get(OUTPUT_OBJECTS));
        returnedParams.add(parameters.get(STORAGE_MODE));
        switch ((String) parameters.getValue(STORAGE_MODE, workspace)) {
            case StorageModes.COMMON_ACROSS_ALL_OBJECTS:
                returnedParams.add(parameters.get(COMMON_WARNING));
                break;
        }

        returnedParams.add(parameters.get(FRAME_SEPARATOR));
        returnedParams.add(parameters.get(START_FRAME_MODE));
        switch ((String) parameters.getValue(START_FRAME_MODE, workspace)) {
            case FrameModes.FIXED_VALUE:
                returnedParams.add(parameters.get(START_FRAME_FIXED_VALUE));
                break;
            case FrameModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.get(START_FRAME_IMAGE));
                returnedParams.add(parameters.get(START_FRAME_IMAGE_MEASUREMENT));
                returnedParams.add(parameters.get(START_OFFSET));

                String startImageName = parameters.getValue(START_FRAME_IMAGE, workspace);
                ((ImageMeasurementP) parameters.get(START_FRAME_IMAGE_MEASUREMENT)).setImageName(startImageName);
                break;
        }

        returnedParams.add(parameters.get(FRAME_SEPARATOR));
        returnedParams.add(parameters.get(END_FRAME_MODE));
        switch ((String) parameters.getValue(END_FRAME_MODE, workspace)) {
            case FrameModes.FIXED_VALUE:
                returnedParams.add(parameters.get(END_FRAME_FIXED_VALUE));
                break;
            case FrameModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.get(END_FRAME_IMAGE));
                returnedParams.add(parameters.get(END_FRAME_IMAGE_MEASUREMENT));
                returnedParams.add(parameters.get(END_OFFSET));

                String endImageName = parameters.getValue(END_FRAME_IMAGE, workspace);
                ((ImageMeasurementP) parameters.get(END_FRAME_IMAGE_MEASUREMENT)).setImageName(endImageName);
                break;
        }

        return returnedParams;

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
        WorkspaceI workspace = null;
        ParentChildRefs returnedRefs = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Input objects to duplicate across multiple timepoints.");

        parameters.get(OUTPUT_OBJECTS)
                .setDescription("Output duplicated objects which will be added to the workspace.");

        parameters.get(STORAGE_MODE).setDescription(
                "Controls whether the duplicated objects share the same common coordinate set or each have their own.  A duplicated coordinate set will use a lot less memory (as no redundant duplication of data is required), but should be used with caution, as any change to the coordinates in one frame will result in the change being mirrored across all timepoints.");

        parameters.get(START_FRAME_MODE).setDescription(
                "Specifies the source for the first timepoint the objects will be created for:<br><ul>" +

                        "<li>\"" + FrameModes.FIXED_VALUE
                        + "\" The first timepoint will be controlled by the value specified in the \""
                        + START_FRAME_FIXED_VALUE + "\" parameter.</li>" +

                        "<li>\"" + FrameModes.IMAGE_MEASUREMENT
                        + "\" The first timepoint will be taken from a measurement (\"" + START_FRAME_IMAGE_MEASUREMENT
                        + "\" parameter) assigned to the image specified by \"" + START_FRAME_IMAGE + "\".</li></ul>");

        parameters.get(START_FRAME_FIXED_VALUE).setDescription("If \"" + START_FRAME_MODE + "\" is set to \""
                + FrameModes.FIXED_VALUE
                + "\", this is the fixed value that will be used as the first timepoint for the output duplicated objects.");

        parameters.get(START_FRAME_IMAGE)
                .setDescription("If \"" + START_FRAME_MODE + "\" is set to \"" + FrameModes.IMAGE_MEASUREMENT
                        + "\", this is the image from which the measurerment \"" + START_FRAME_IMAGE_MEASUREMENT
                        + "\" will be taken.");

        parameters.get(START_FRAME_IMAGE_MEASUREMENT).setDescription("If \"" + START_FRAME_MODE + "\" is set to \""
                + FrameModes.IMAGE_MEASUREMENT
                + "\", this is the measurement that will be used as the first timepoint for the duplicated objects.");

        parameters.get(START_OFFSET).setDescription("If \"" + START_FRAME_MODE + "\" is set to \""
                + FrameModes.IMAGE_MEASUREMENT
                + "\", the first frame can be offset relative to the specified measurement by this number of frames.  For example, if the provided measurement is 5 and an offset of -1 is used, the first frame in the duplicated set will be 4.");

        parameters.get(END_FRAME_MODE)
                .setDescription("Specifies the source for the last timepoint the objects will be created for:<br><ul>" +

                        "<li>\"" + FrameModes.FIXED_VALUE
                        + "\" The last timepoint will be controlled by the value specified in the \""
                        + END_FRAME_FIXED_VALUE + "\" parameter.</li>" +

                        "<li>\"" + FrameModes.IMAGE_MEASUREMENT
                        + "\" The last timepoint will be taken from a measurement (\"" + END_FRAME_IMAGE_MEASUREMENT
                        + "\" parameter) assigned to the image specified by \"" + END_FRAME_IMAGE + "\".</li></ul>");

        parameters.get(END_FRAME_FIXED_VALUE).setDescription("If \"" + END_FRAME_MODE + "\" is set to \""
                + FrameModes.FIXED_VALUE
                + "\", this is the fixed value that will be used as the last timepoint for the output duplicated objects.");

        parameters.get(END_FRAME_IMAGE)
                .setDescription("If \"" + END_FRAME_MODE + "\" is set to \"" + FrameModes.IMAGE_MEASUREMENT
                        + "\", this is the image from which the measurerment \"" + END_FRAME_IMAGE_MEASUREMENT
                        + "\" will be taken.");

        parameters.get(END_FRAME_IMAGE_MEASUREMENT).setDescription("If \"" + END_FRAME_MODE + "\" is set to \""
                + FrameModes.IMAGE_MEASUREMENT
                + "\", this is the measurement that will be used as the last timepoint for the duplicated objects.");

        parameters.get(END_OFFSET).setDescription("If \"" + END_FRAME_MODE + "\" is set to \""
                + FrameModes.IMAGE_MEASUREMENT
                + "\", the end frame can be offset relative to the specified measurement by this number of frames.  For example, if the provided measurement is 5 and an offset of -1 is used, the last frame in the duplicated set will be 4.");

    }
}
