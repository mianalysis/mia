package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ParentChildRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.Volume;
import wbif.sjx.common.Object.Volume.VolumeType;

/**
 * Created by sc13967 on 01/08/2017.
 */
public class ExtractObjectCrossSection extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String CROSS_SECTION_SEPARATOR = "Cross-section controls";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String RELATIVE_SLICE_INDICES = "Relative slice indices";
    public static final String IMAGE_MEASUREMENT = "Image measurement";
    public static final String IMAGE_FOR_MEASUREMENT = "Image for measurement";
    public static final String OBJECT_MEASUREMENT = "Object measurement";

    public interface ReferenceModes {
        String ABSOLUTE = "Absolute";
        String IMAGE_MEASUREMENT = "Image measurement";
        String OBJECT_MEASUREMENT = "Object measurement";

        String[] ALL = new String[] { ABSOLUTE, IMAGE_MEASUREMENT, OBJECT_MEASUREMENT };

    }

    public ExtractObjectCrossSection(ModuleCollection modules) {
        super("Extract object cross section", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "";
    }

    static int[] applyIndexOffset(int[] inputIndices, Measurement measurement) {
        int[] outputIndices = new int[inputIndices.length];

        int referencePoint = (int) Math.round(measurement.getValue());
        for (int i = 0; i < inputIndices.length; i++)
            outputIndices[i] = inputIndices[i] + referencePoint;

        return outputIndices;

    }

    static void process(Obj inputObject, ObjCollection outputObjects, int[] indices) {
        VolumeType volumeType = inputObject.getVolumeType();
        if (volumeType == VolumeType.OCTREE)
            volumeType = VolumeType.QUADTREE;

        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());

        for (int idx : indices) {
            if (idx < 0 || idx >= inputObject.getNSlices())
                continue;

            Volume slice = inputObject.getSlice(idx);
            if (slice == null)
                continue;

            if (slice.getCoordinateSet() == null)
                continue;

            for (Point<Integer> point : slice.getCoordinateSet()) {
                point.setZ(point.getZ() + idx);
                try {
                    outputObject.add(point);
                } catch (PointOutOfRangeException e) {
                }
            }
        }

        inputObject.addChild(outputObject);
        outputObject.addParent(inputObject);

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String imageForMeasurementName = parameters.getValue(IMAGE_FOR_MEASUREMENT);
        String imageMeasurementName = parameters.getValue(IMAGE_MEASUREMENT);
        String objectMeasurementName = parameters.getValue(OBJECT_MEASUREMENT);
        String indicesString = parameters.getValue(RELATIVE_SLICE_INDICES);

        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects);
        workspace.addObjects(outputObjects);

        int[] indices = CommaSeparatedStringInterpreter.interpretIntegers(indicesString, true);
        if (indicesString.contains("end"))
            indices = CommaSeparatedStringInterpreter.extendRangeToEnd(indices, inputObjects.getNSlices());

        // If using an image measurement, updating the indices here, as they will be the
        // same for all objects
        switch (referenceMode) {
            case ReferenceModes.IMAGE_MEASUREMENT:
                Image imageForMeasurement = workspace.getImage(imageForMeasurementName);
                Measurement imageMeasurement = imageForMeasurement.getMeasurement(imageMeasurementName);
                indices = applyIndexOffset(indices, imageMeasurement);
                break;
        }

        for (Obj inputObject : inputObjects.values()) {
            int[] finalIndices = indices;
            switch (referenceMode) {
                case ReferenceModes.OBJECT_MEASUREMENT:
                    Measurement objectMeasurement = inputObject.getMeasurement(objectMeasurementName);
                    finalIndices = applyIndexOffset(indices, objectMeasurement);
                    break;
            }

            process(inputObject, outputObjects, finalIndices);

        }

        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CROSS_SECTION_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.ABSOLUTE, ReferenceModes.ALL));
        parameters.add(new InputImageP(IMAGE_FOR_MEASUREMENT, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(OBJECT_MEASUREMENT, this));
        parameters.add(new StringP(RELATIVE_SLICE_INDICES, this, "0"));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CROSS_SECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));

        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(IMAGE_FOR_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(IMAGE_MEASUREMENT));
                String imageName = parameters.getValue(IMAGE_FOR_MEASUREMENT);
                ((ImageMeasurementP) parameters.getParameter(IMAGE_MEASUREMENT)).setImageName(imageName);

                break;
            case ReferenceModes.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(OBJECT_MEASUREMENT));
                String objectsName = parameters.getValue(INPUT_OBJECTS);
                ((ObjectMeasurementP) parameters.getParameter(OBJECT_MEASUREMENT)).setObjectName(objectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(RELATIVE_SLICE_INDICES));

        return returnedParameters;

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
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        String inputObjects = parameters.getValue(INPUT_OBJECTS);
        String outputObjects = parameters.getValue(OUTPUT_OBJECTS);

        returnedRelationships.add(new ParentChildRef(inputObjects, outputObjects));

        return returnedRelationships;

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
