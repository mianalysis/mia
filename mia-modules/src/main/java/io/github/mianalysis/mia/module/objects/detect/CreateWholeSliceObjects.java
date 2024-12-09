package io.github.mianalysis.mia.module.objects.detect;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 06/06/2017.
 */

/**
 * For conversion of an image to individual slice objects. This is useful for
 * subsequently making independent measurements on each slice of an image.
 * Individual objects can either be created per slice or per timepoint.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateWholeSliceObjects extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
    * 
    */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String OUTPUT_MODE = "Output mode";

    public interface OutputModes {
        String PER_SLICE = "Per slice";
        String PER_TIMEPOINT = "Per timepoint";

        String[] ALL = new String[] { PER_SLICE, PER_TIMEPOINT };

    }

    public interface Measurements {
        String SLICE = "Slice";
        String TIMEPOINT = "Timepoint";

    }

    public CreateWholeSliceObjects(Modules modules) {
        super("Create whole slice objects", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "For conversion of an image to individual slice objects.  This is useful for subsequently making independent measurements on each slice of an image.  Individual objects can either be created per slice or per timepoint.  Output objects are automatically assigned measurements for timepoint and optionally also slice if processing slice-by-slice.";
    }

    public static Objs process(Image inputImage, String outputObjectsName, String outputMode) {
        Image blankImage = ImageFactory.createImage("Temp", inputImage.getImagePlus().duplicate());
        ImageMath.process(blankImage, ImageMath.CalculationModes.MULTIPLY, 0);
        ImageTypeConverter.process(blankImage, 8, ImageTypeConverter.ScalingModes.CLIP);

        String detectionMode;
        switch (outputMode) {
            case OutputModes.PER_SLICE:
            default:
                detectionMode = IdentifyObjects.DetectionModes.SLICE_BY_SLICE;
                break;
            case OutputModes.PER_TIMEPOINT:
                detectionMode = IdentifyObjects.DetectionModes.THREE_D;
                break;
        }

        int connectivity = IdentifyObjects.getConnectivity(IdentifyObjects.Connectivity.SIX);
        String type = IdentifyObjects.VolumeTypes.QUADTREE;

        Objs outputObjects = IdentifyObjects.process(blankImage, outputObjectsName, false, false, detectionMode,
                connectivity, type, false, 60, false);

        for (Obj outputObject : outputObjects.values()) {
            outputObject.addMeasurement(new Measurement(Measurements.TIMEPOINT, outputObject.getT()));

            switch (outputMode) {
                case OutputModes.PER_SLICE:
                default:
                    Point<Integer> pt = outputObject.getCoordinateIterator().next();
                    if (pt != null)
                        outputObject.addMeasurement(new Measurement(Measurements.SLICE, pt.z));
                    break;
            }
        }

        return outputObjects;

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String outputMode = parameters.getValue(OUTPUT_MODE, workspace);

        // Getting input image
        Image inputImage = workspace.getImage(inputImageName);

        // Getting objects
        Objs outputObjects = process(inputImage, outputObjectsName, outputMode);

        // Adding objects to workspace
        workspace.addObjects(outputObjects);

        for (Obj o:outputObjects.values()) {
            
        }
        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.PER_SLICE, OutputModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, null);
        String outputMode = parameters.getValue(OUTPUT_MODE, null);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(Measurements.TIMEPOINT);
        ref.setObjectsName(outputObjectsName);
        returnedRefs.add(ref);

        switch (outputMode) {
            case OutputModes.PER_SLICE:
                ref = objectMeasurementRefs.getOrPut(Measurements.SLICE);
                ref.setObjectsName(outputObjectsName);
                returnedRefs.add(ref);
                break;
        }

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
        return null;
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

    }
}