package io.github.mianalysis.mia.module.objects.process;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import inra.ijpb.watershed.Watershed;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.ConnectivityInterface;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GrowObjects extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input/output";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * 
     */
    public static final String OBJECT_OUTPUT_MODE = "Output object mode";

    /**
    * 
    */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String GROWTH_SEPARATOR = "Growth controls";

    /**
    * 
    */
    public static final String STARTING_OBJECT_MODE = "Starting object mode";

    /**
    * 
    */
    public static final String GROWTH_MODE = "Growth mode";

    /**
    * 
    */
    public static final String INTENSITY_IMAGE = "Intensity image";

    /**
    * 
    */
    public static final String MASK_OUTPUT_OBJECTS = "Mask output objects";

    /**
    * 
    */
    public static final String MASK_IMAGE = "Mask image";

    /**
    * 
    */
    public static final String BINARY_LOGIC = "Binary logic";

    /**
    * 
    */
    public static final String CONNECTIVITY = "Connectivity";

    /**
    * 
    */
    public static final String EXCLUDE_INPUT_REGIONS = "Exclude input regions";

    public interface OutputModes {
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[] { CREATE_NEW_OBJECT, UPDATE_INPUT };

    }

    public interface StartingObjectModes {
        String CENTROIDS = "Centroids";
        String SURFACES = "Surfaces";

        String[] ALL = new String[] { CENTROIDS, SURFACES };

    }

    public interface GrowthModes {
        String EQUIDISTANT_FROM_OBJECTS = "Equidistant from objects";
        String FROM_IMAGE = "From image";

        String[] ALL = new String[] { EQUIDISTANT_FROM_OBJECTS, FROM_IMAGE };

    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface Connectivity extends ConnectivityInterface {
    }

    public GrowObjects(Modules modules) {
        super("Grow objects", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    public static Objs process(Objs inputObjects, @Nullable String outputObjectsName, String startingObjectsMode,
            String growthMode, @Nullable String intensityImageName, @Nullable String maskImageName,
            boolean blackBackground, int connectivity, boolean excludeInputRegions, WorkspaceI workspace) {
        Objs outputObjects = outputObjectsName == null ? null : new Objs(outputObjectsName, inputObjects);

        // Loop over timepoints
        int nFrames = inputObjects.getNFrames();
        for (int frame = 0; frame < nFrames; frame++) {
            // Get objects in this frame
            Objs currObjs = inputObjects.getObjectsInFrame("This frame", frame);

            // Get marker, intensity and mask images
            Image markerImage = getMarkerImage(currObjs, startingObjectsMode);
            Image intensityImage = getIntensityImage(currObjs, frame, growthMode, intensityImageName, workspace);
            Image maskImage = getMaskImage(currObjs, frame, maskImageName, blackBackground,
                    workspace);

            // Apply watershed transform
            ImagePlus segmentedIpl;
            if (maskImage == null)
                segmentedIpl = Watershed.computeWatershed(intensityImage.getImagePlus(),
                        markerImage.getImagePlus(), connectivity, true, false);
            else
                segmentedIpl = Watershed.computeWatershed(intensityImage.getImagePlus(),
                        markerImage.getImagePlus(), maskImage.getImagePlus(), connectivity, true, false);

            Image segmentedImage = ImageFactory.createImage("Segmented", segmentedIpl);

            // Get objects and create new object collection
            Objs segmentedObjects = segmentedImage.convertImageToObjects(VolumeType.QUADTREE, outputObjectsName);

            // Update timepoint, set relationships, (optionally) apply mask and add objects
            // to output collection
            for (Obj segmentedObject : segmentedObjects.values()) {
                segmentedObject.setT(frame);

                Point<Integer> coord = segmentedObject.getCoordinateIterator().next();
                int ID = (int) Math.round(segmentedIpl.getStack().getProcessor(coord.z + 1).getf(coord.x, coord.y));
                Obj inputObject = inputObjects.get(ID);

                if (excludeInputRegions)
                    for (Point<Integer> point : inputObject.getCoordinateSet())
                        segmentedObject.getCoordinateSet().remove(point);

                if (outputObjects == null) {
                    inputObject.getCoordinateSet().clear();
                    inputObject.setCoordinateSet(segmentedObject.getCoordinateSet());
                    inputObject.clearSurface();
                    inputObject.clearCentroid();
                    inputObject.clearProjected();
                    inputObject.clearROIs();
                } else {
                    segmentedObject.setID(ID);
                    outputObjects.add(segmentedObject);
                    segmentedObject.setObjectCollection(outputObjects);
                    inputObject.addChild(segmentedObject);
                    segmentedObject.addParent(inputObject);
                }
            }
        }

        return outputObjects;

    }

    public static Image getMarkerImage(Objs objects, String startingObjectsMode) {
        HashMap<Integer, Float> hues = ColourFactory.getIDHues(objects, false);

        switch (startingObjectsMode) {
            case StartingObjectModes.CENTROIDS:
                return objects.convertCentroidsToImage("Markers", hues, 32, false);
            case StartingObjectModes.SURFACES:
            default:
                return objects.convertToImage("Markers", hues, 32, false);
        }
    }

    public static Image getIntensityImage(Objs objects, int frame, String growthMode, @Nullable String intensityImageName, WorkspaceI workspace) {
        switch (growthMode) {
            case GrowthModes.EQUIDISTANT_FROM_OBJECTS:
            default:
                // No intensity image, so creating blank (black) image
                Image intensityImage = objects.convertToImageBinary();
                ImageMath.process(intensityImage, ImageMath.CalculationModes.MULTIPLY, 0);
                return intensityImage;
            case GrowthModes.FROM_IMAGE:
                Image fullIntensityImage = workspace.getImage(intensityImageName);
                return ExtractSubstack.extractSubstack(fullIntensityImage, intensityImageName, "1", "1-end",
                        String.valueOf(frame + 1));
        }
    }

    public static Image getMaskImage(Objs objects, int frame, @Nullable String maskImageName, boolean blackBackground,
            WorkspaceI workspace) {
        if (maskImageName != null) {
            Image fullMaskImage = workspace.getImage(maskImageName);
            Image maskImage = ExtractSubstack.extractSubstack(fullMaskImage, maskImageName, "1", "1-end",
                    String.valueOf(frame + 1));

            if (!blackBackground)
                InvertIntensity.process(maskImage);

            return maskImage;

        } else {
            return null;
        }
    }

    @Override
    public Status process(WorkspaceI workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputMode = parameters.getValue(OBJECT_OUTPUT_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String startingObjectMode = parameters.getValue(STARTING_OBJECT_MODE, workspace);
        String growthMode = parameters.getValue(GROWTH_MODE, workspace);
        String intensityImageName = parameters.getValue(INTENSITY_IMAGE, workspace);
        boolean maskOutputObjects = parameters.getValue(MASK_OUTPUT_OBJECTS, workspace);
        String maskImageName = parameters.getValue(MASK_IMAGE, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY, workspace));
        boolean excludeInputRegions = parameters.getValue(EXCLUDE_INPUT_REGIONS, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);

        if (outputMode.equals(OutputModes.UPDATE_INPUT))
            outputObjectsName = null;

        if (!maskOutputObjects)
            maskImageName = null;

        Objs outputObjects = process(inputObjects, outputObjectsName, startingObjectMode, growthMode,
                intensityImageName, maskImageName, blackBackground, connectivity, excludeInputRegions, workspace);

        if (outputObjects == null) {
            if (showOutput)
                inputObjects.convertToImageIDColours().show(false);
        } else {
            workspace.addObjects(outputObjects);
            if (showOutput)
                outputObjects.convertToImageIDColours().show(false);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, OutputModes.CREATE_NEW_OBJECT, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(GROWTH_SEPARATOR, this));
        parameters.add(new ChoiceP(STARTING_OBJECT_MODE, this, StartingObjectModes.SURFACES, StartingObjectModes.ALL));
        parameters.add(new ChoiceP(GROWTH_MODE, this, GrowthModes.FROM_IMAGE, GrowthModes.ALL));
        parameters.add(new InputImageP(INTENSITY_IMAGE, this));
        parameters.add(new BooleanP(MASK_OUTPUT_OBJECTS, this, true));
        parameters.add(new InputImageP(MASK_IMAGE, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new BooleanP(EXCLUDE_INPUT_REGIONS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE, null)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.get(GROWTH_SEPARATOR));
        returnedParameters.add(parameters.get(STARTING_OBJECT_MODE));
        returnedParameters.add(parameters.get(GROWTH_MODE));

        switch ((String) parameters.getValue(GROWTH_MODE, null)) {
            case GrowthModes.FROM_IMAGE:
                returnedParameters.add(parameters.get(INTENSITY_IMAGE));
                break;
        }

        returnedParameters.add(parameters.get(MASK_OUTPUT_OBJECTS));
        if ((boolean) parameters.getValue(MASK_OUTPUT_OBJECTS, null)) {
            returnedParameters.add(parameters.get(MASK_IMAGE));
            returnedParameters.add(parameters.getParameter(BINARY_LOGIC));
        }

        returnedParameters.add(parameters.getParameter(CONNECTIVITY));
        returnedParameters.add(parameters.get(EXCLUDE_INPUT_REGIONS));

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
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE, workspace)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
                returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

                break;
        }

        return returnedRelationships;

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
