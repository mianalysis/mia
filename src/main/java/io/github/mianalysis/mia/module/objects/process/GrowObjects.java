package io.github.mianalysis.mia.module.objects.process;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import inra.ijpb.watershed.Watershed;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
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
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

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
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
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

    public static Image getIntensityImage(String growthMode, String intensityImageName, Workspace workspace, int frame,
            Objs objects) {
        switch (growthMode) {
            case GrowthModes.EQUIDISTANT_FROM_OBJECTS:
            default:
                // No intensity image, so creating blank (black) image
                Image intensityImage = objects.convertToImageRandomColours();
                ImageMath.process(intensityImage, ImageMath.CalculationModes.MULTIPLY, 0);
                return intensityImage;
            case GrowthModes.FROM_IMAGE:
                Image fullIntensityImage = workspace.getImage(intensityImageName);
                return ExtractSubstack.extractSubstack(fullIntensityImage, intensityImageName, "1", "1-end",
                        String.valueOf(frame + 1));
        }
    }

    public static Image getMaskImage(boolean maskOutputObjects, String maskImageName, Workspace workspace,
            boolean blackBackground, int frame, Objs objects) {
        Image maskImage;

        if (maskOutputObjects) {
            Image fullMaskImage = workspace.getImage(maskImageName);
            maskImage = ExtractSubstack.extractSubstack(fullMaskImage, maskImageName, "1", "1-end",
                    String.valueOf(frame + 1));

            if (!blackBackground)
                InvertIntensity.process(maskImage);

        } else {
            // No mask image, so creating blank (white) image
            maskImage = objects.convertToImageIDColours();
            ImageTypeConverter.process(maskImage, 8, ImageTypeConverter.ScalingModes.CLIP);
            ImageMath.process(maskImage, ImageMath.CalculationModes.ADD, 255);
        }

        return maskImage;

    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
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

        Objs inputObjects = workspace.getObjects().get(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        workspace.addObjects(outputObjects);

        // Loop over timepoints
        int nFrames = inputObjects.getNFrames();
        for (int frame = 0; frame < nFrames; frame++) {
            // Get objects in this frame
            Objs currObjs = inputObjects.getObjectsInFrame("This frame", frame);

            // Get marker, intensity and mask images
            Image markerImage = getMarkerImage(currObjs, startingObjectMode);
            Image intensityImage = getIntensityImage(growthMode, intensityImageName, workspace, frame, currObjs);
            Image maskImage = getMaskImage(maskOutputObjects, maskImageName, workspace, blackBackground, frame,
                    currObjs);

            // Apply watershed transform
            ImagePlus segmentedIpl = Watershed.computeWatershed(intensityImage.getImagePlus(),
                    markerImage.getImagePlus(), maskImage.getImagePlus(), connectivity, true, false);
            Image segmentedImage = ImageFactory.createImage("Segmented", segmentedIpl);

            // Get objects and create new object collection
            Objs segmentedObjects = segmentedImage.convertImageToObjects(VolumeType.QUADTREE, outputObjectsName);

            // Update timepoint, set relationships, (optionally) apply mask and add objects to output collection
            for (Obj segmentedObject:segmentedObjects.values()) {
                segmentedObject.setT(frame);
                
                // Updating ID number to match parent
                Point<Integer> coord = segmentedObject.getCoordinateIterator().next();
                int ID = (int) Math.round(segmentedIpl.getStack().getProcessor(coord.z+1).getf(coord.x, coord.y));
                segmentedObject.setID(ID);
                
                Obj inputObject = inputObjects.get(ID);
                segmentedObject.addParent(inputObject);
                inputObject.addChild(segmentedObject);

                if (excludeInputRegions)
                    for (Point<Integer> point:inputObject.getCoordinateSet())
                        segmentedObject.getCoordinateSet().remove(point);

                outputObjects.add(segmentedObject);

            }
        }

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
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
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));

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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS, workspace),
                parameters.getValue(OUTPUT_OBJECTS, workspace)));

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
