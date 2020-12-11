package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.MessageP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

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
                nFrames);

        String name = new DuplicateAcrossTime(null).getName();

        // Duplicating objects
        int count = 0;
        for (Obj inputObject : inputObjects.values()) {
            writeStatus("Duplicating object " + (++count) + " of " + inputObjects.size(), name);
            
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
        }

        return outputObjects;

    }

    public DuplicateAcrossTime(ModuleCollection modules) {
        super("Duplicate objects across time", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Creates a copy of objects across all frames in the specified image stack.";
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
}
