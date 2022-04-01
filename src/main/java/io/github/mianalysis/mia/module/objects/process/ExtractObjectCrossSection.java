package io.github.mianalysis.mia.module.objects.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.Volume;
import io.github.sjcross.common.object.volume.VolumeType;
import io.github.sjcross.common.process.CommaSeparatedStringInterpreter;

/**
 * Created by sc13967 on 01/08/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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

    public ExtractObjectCrossSection(Modules modules) {
        super("Extract object cross section", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Extracts XY-plane cross-sections of specified objects.  The extracted cross-sections are stored as separate objects, which are children of the associated input object.  Slice indicies can be specified as fixed values or relative to image/object measurements (e.g. relative to the object centroids).";
    }

    static int[] applyIndexOffset(int[] inputIndices, Measurement measurement) {
        int[] outputIndices = new int[inputIndices.length];

        int referencePoint = (int) Math.round(measurement.getValue());
        for (int i = 0; i < inputIndices.length; i++)
            outputIndices[i] = inputIndices[i] + referencePoint;

        return outputIndices;

    }

    static void process(Obj inputObject, Objs outputObjects, int[] indices) {
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
                point.setZ(idx);
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
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String imageForMeasurementName = parameters.getValue(IMAGE_FOR_MEASUREMENT);
        String imageMeasurementName = parameters.getValue(IMAGE_MEASUREMENT);
        String objectMeasurementName = parameters.getValue(OBJECT_MEASUREMENT);
        String indicesString = parameters.getValue(RELATIVE_SLICE_INDICES);

        Objs outputObjects = new Objs(outputObjectsName, inputObjects);
        workspace.addObjects(outputObjects);

        int[] indices = CommaSeparatedStringInterpreter.interpretIntegers(indicesString, true, inputObjects.getNSlices());

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

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
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
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String inputObjects = parameters.getValue(INPUT_OBJECTS);
        String outputObjects = parameters.getValue(OUTPUT_OBJECTS);

        returnedRelationships.add(new ParentChildRef(inputObjects, outputObjects));

        return returnedRelationships;

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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Input objects from workspace for which cross-sections will be extracted.  Output cross-section objects will be stored as children associated with the relevant input object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output cross-section objects.  These will be stored in the workspace with this name.  These objects will be children of their respective input object.");

        parameters.get(REFERENCE_MODE).setDescription("The source for the reference Z-position for each object:<br><ul>"

                + "<li>\"" + ReferenceModes.ABSOLUTE + "\" The slice indices specified by \"" + RELATIVE_SLICE_INDICES
                + "\" correspond to the absolute slice index of the coordinates.  For example, an index of 0 will extract the first slice and indices of \"3-5\" will load the 4th, 5th and 6th slices (note the use of zero-based indexing).</li>"

                + "<li>\"" + ReferenceModes.IMAGE_MEASUREMENT
                + "\" The reference slice index will be taken from a measurement (specified by \"" + IMAGE_MEASUREMENT
                + "\") associated with an image from the workspace (specified by \"" + IMAGE_FOR_MEASUREMENT
                + "\").  The slices indices specified by \"" + RELATIVE_SLICE_INDICES
                + "\" will be relative to this measurement value.  For example, with an image measurement of 4 and specified index of \"-2\", the 3rd slice will be extracted (i.e. 2 below 4 using zero-based indexing).</li>"

                + "<li>\"" + ReferenceModes.OBJECT_MEASUREMENT
                + "\" The reference slice index will be taken from a measurement (specified by \"" + OBJECT_MEASUREMENT
                + "\") associated with the object being processed.  The slices indices specified by \""
                + RELATIVE_SLICE_INDICES
                + "\" will be relative to this measurement value.  For example, for an object with measurement value of 2 and specified index of \"3\", the 6th slice will be extracted (i.e. 3 above 2 using zero-based indexing).</li></ul>");

        parameters.get(RELATIVE_SLICE_INDICES).setDescription("Slices from the input objects will be extracted at these relative indices (relative to the position specified the \""+REFERENCE_MODE+"\" and associated parameters).  Indices can be specified as a comma-separated list, using a range (e.g. \"4-7\" will extract relative indices 4,5,6 and 7) or as a range extracting every nth slice (e.g. \"4-10-2\" will extract slices 4,6,8 and 10).  The \"end\" keyword will be converted to the maximum slice index at runtime.");

        parameters.get(IMAGE_MEASUREMENT).setDescription("If \""+REFERENCE_MODE+"\" is set to \""+ReferenceModes.IMAGE_MEASUREMENT+"\", this is the measurement (associated with the image specified by \""+IMAGE_FOR_MEASUREMENT+"\") which will act as the reference slice index against which the relative slice indices are calculated.");

        parameters.get(IMAGE_FOR_MEASUREMENT).setDescription("If \""+REFERENCE_MODE+"\" is set to \""+ReferenceModes.IMAGE_MEASUREMENT+"\", this is the image from which the reference measurement (specified by \""+IMAGE_MEASUREMENT+"\") will be taken.");

        parameters.get(OBJECT_MEASUREMENT).setDescription("If \""+REFERENCE_MODE+"\" is set to \""+ReferenceModes.OBJECT_MEASUREMENT+"\", this is the measurement (associated with the relevant input object) which will act as the reference slice index against which the relative slice indices are calculated.");

    }
}
