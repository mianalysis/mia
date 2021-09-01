package io.github.mianalysis.MIA.Module.ObjectProcessing.Miscellaneous;

import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Colours;
import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.ImageMeasurementP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.MIA.Object.Parameters.Text.MessageP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;
import io.github.sjcross.common.Object.Point;
import io.github.sjcross.common.Object.Volume.PointOutOfRangeException;

/**
 * Created by sc13967 on 29/06/2017.
 */
public class DuplicateAcrossTime extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String STORAGE_MODE = "Coordinates storage mode";
    public static final String COMMON_WARNING = "Common warning";

    public static final String FRAME_SEPARATOR = "Frame range";
    public static final String START_FRAME_MODE = "Start frame mode";
    public static final String START_FRAME_FIXED_VALUE = "Start frame fixed value";
    public static final String START_FRAME_IMAGE = "Start frame image";
    public static final String START_FRAME_IMAGE_MEASUREMENT = "Start frame image measurement";
    public static final String START_OFFSET = "Start frame offset";
    public static final String END_FRAME_MODE = "End frame mode";
    public static final String END_FRAME_FIXED_VALUE = "End frame fixed value";
    public static final String END_FRAME_IMAGE = "End frame image";
    public static final String END_FRAME_IMAGE_MEASUREMENT = "End frame image measurement";
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

    public static ObjCollection duplicate(ObjCollection inputObjects, String outputObjectsName, String storageMode,
            int startFrame, int endFrame) {
        // Creating output object collection
        int nFrames = endFrame - startFrame + 1;
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects.getSpatialCalibration(),
                nFrames, inputObjects.getFrameInterval(), inputObjects.getTemporalUnit());

        String name = new DuplicateAcrossTime(null).getName();

        // Duplicating objects
        int count = 0;
        for (Obj inputObject : inputObjects.values()) {
            for (int t = startFrame; t <= endFrame; t++) {
                // Creating object for this timepoint
                Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
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
                                outputObject.add(point.x, point.y, point.z);
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

    public DuplicateAcrossTime(ModuleCollection modules) {
        super("Duplicate objects across time", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Creates a copy of objects across all frames in the specified image stack.  Duplicated objects can either have their own set of coordinates or all share the set from the input object.  While sharing coordinates across all timepoints can be much more memory efficient (no redundant duplication of data is required), any change to the coordinates in one frame will result in the change being mirrored across all timepoints, so this mode should be used with care.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String storageMode = parameters.getValue(STORAGE_MODE);
        String startFrameMode = parameters.getValue(START_FRAME_MODE);
        int startFrame = parameters.getValue(START_FRAME_FIXED_VALUE);
        String startImageName = parameters.getValue(START_FRAME_IMAGE);
        String startImageMeasurementName = parameters.getValue(START_FRAME_IMAGE_MEASUREMENT);
        int startOffset = parameters.getValue(START_OFFSET);
        String endMode = parameters.getValue(END_FRAME_MODE);
        int endFrame = parameters.getValue(END_FRAME_FIXED_VALUE);
        String endImageName = parameters.getValue(END_FRAME_IMAGE);
        String endImageMeasurementName = parameters.getValue(END_FRAME_IMAGE_MEASUREMENT);
        int endOffset = parameters.getValue(END_OFFSET);

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
        ObjCollection outputObjects = duplicate(inputObjects, outputObjectsName, storageMode, startFrame, endFrame);
        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(STORAGE_MODE, this, StorageModes.COMMON_ACROSS_ALL_OBJECTS, StorageModes.ALL));
        parameters.add(new MessageP(COMMON_WARNING, this,
                "\"Common\" coordinate storage will use a single set of coordinates for all copies of an object.  Therefore, changes made to one object will be reflected in all objects.",
                Colours.RED));

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
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParams = new ParameterCollection();

        returnedParams.add(parameters.get(INPUT_SEPARATOR));
        returnedParams.add(parameters.get(INPUT_OBJECTS));

        returnedParams.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParams.add(parameters.get(OUTPUT_OBJECTS));
        returnedParams.add(parameters.get(STORAGE_MODE));
        switch ((String) parameters.getValue(STORAGE_MODE)) {
            case StorageModes.COMMON_ACROSS_ALL_OBJECTS:
                returnedParams.add(parameters.get(COMMON_WARNING));
                break;
        }

        returnedParams.add(parameters.get(FRAME_SEPARATOR));
        returnedParams.add(parameters.get(START_FRAME_MODE));
        switch ((String) parameters.getValue(START_FRAME_MODE)) {
            case FrameModes.FIXED_VALUE:
                returnedParams.add(parameters.get(START_FRAME_FIXED_VALUE));
                break;
            case FrameModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.get(START_FRAME_IMAGE));
                returnedParams.add(parameters.get(START_FRAME_IMAGE_MEASUREMENT));
                returnedParams.add(parameters.get(START_OFFSET));

                String startImageName = parameters.getValue(START_FRAME_IMAGE);
                ((ImageMeasurementP) parameters.get(START_FRAME_IMAGE_MEASUREMENT)).setImageName(startImageName);
                break;
        }

        returnedParams.add(parameters.get(FRAME_SEPARATOR));
        returnedParams.add(parameters.get(END_FRAME_MODE));
        switch ((String) parameters.getValue(END_FRAME_MODE)) {
            case FrameModes.FIXED_VALUE:
                returnedParams.add(parameters.get(END_FRAME_FIXED_VALUE));
                break;
            case FrameModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.get(END_FRAME_IMAGE));
                returnedParams.add(parameters.get(END_FRAME_IMAGE_MEASUREMENT));
                returnedParams.add(parameters.get(END_OFFSET));

                String endImageName = parameters.getValue(END_FRAME_IMAGE);
                ((ImageMeasurementP) parameters.get(END_FRAME_IMAGE_MEASUREMENT)).setImageName(endImageName);
                break;
        }

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRefs = new ParentChildRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

        return returnedRefs;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
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
