package io.github.mianalysis.mia.module.objects.transform;

import java.util.HashMap;
import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Applies the mask image to the specified object collection. Any object
 * coordinates coincident with black pixels (intensity 0) will be removed.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MaskObjects<T extends RealType<T> & NativeType<T>> extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input/output";

    /**
     * Objects to be masked.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Controls how the masked objects will be stored:<br>
     * <ul>
     * <li>"Create new objects" (Default) Will add the masked objects to a new
     * object set and store this set in the workspace.</li>
     * <li>"Update input objects" Will replace the coordinates of the input object
     * with the masked coordinates. All measurements associated with input objects
     * will be transferred to the masked objects.</li>
     * </ul>
     */
    public static final String OBJECT_OUTPUT_MODE = "Output object mode";

    /**
     * Name for the output masked objects to be stored in workspace.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String MASK_SEPARATOR = "Mask options";

    /**
     * Controls whether the input objects will be masked by an image or an object
     * collection:<br>
     * <ul>
     * <li>"Mask from image" (Default) Input objects will be masked based on the
     * image specified by "Mask image". Any object regions coincident with black
     * pixels (0 pixel intensity) will be removed.</li>
     * <li>"Mask from objects (remove overlap)" Input objects will be masked based
     * on all objects of the collection specified by "Mask objects". Any object
     * regions coincident with any objects in the masking collection will be
     * removed. The masking objects will be unaffected by this process.</li>
     * <li>"Mask from objects (retain overlap)" Input objects will be masked based
     * on all objects of the collection specified by "Mask objects". Any object
     * regions not coincident with any objects in the masking collection will be
     * removed. The masking objects will be unaffected by this process.</li>
     * </ul>
     */
    public static final String MASK_MODE = "Mask mode";

    /**
     * Object collection to use as mask on input objects. Depending on which
     * object-masking mode is selected, the input objects will either have
     * coordinates coincident with these objects removed or retained.
     */
    public static final String MASK_OBJECTS = "Mask objects";

    /**
     * Image to use as mask on input objects. Object coordinates coincident with
     * black pixels (pixel intensity = 0) are removed.
     */
    public static final String MASK_IMAGE = "Mask image";

    /**
     * When selected, any objects which have no volume following masking will be
     * removed.
     */
    public static final String REMOVE_EMPTY_OBJECTS = "Remove empty objects";

    public interface MaskModes {
        String MASK_FROM_IMAGE = "Mask from image";
        String MASK_FROM_OBJECTS_REMOVE_OVERLAP = "Mask from objects (remove overlap)";
        String MASK_FROM_OBJECTS_RETAIN_OVERLAP = "Mask from objects (retain overlap)";

        String[] ALL = new String[] { MASK_FROM_IMAGE, MASK_FROM_OBJECTS_REMOVE_OVERLAP,
                MASK_FROM_OBJECTS_RETAIN_OVERLAP };

    }

    public interface OutputModes {
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[] { CREATE_NEW_OBJECT, UPDATE_INPUT };

    }

    public static ObjsI maskObjects(ObjsI inputObjects, ImageI maskImage, @Nullable String outputObjectsName,
            boolean removeEmptyObjects, boolean verbose) {
        String moduleName = new MaskObjects<>(null).getName();
        String outputMode = outputObjectsName == null ? OutputModes.UPDATE_INPUT : OutputModes.CREATE_NEW_OBJECT;
        ObjsI outputObjects = outputObjectsName == null ? null : ObjsFactories.getDefaultFactory().createFromExample(outputObjectsName, inputObjects);

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        for (ObjI inputObject : inputObjects.values()) {
            ObjI outputObject = maskObject(inputObject, maskImage, outputObjectsName);

            switch (outputMode) {
                case OutputModes.CREATE_NEW_OBJECT:
                    outputObjects.add(outputObject);
                    outputObject.setObjectCollection(outputObjects);
                    inputObject.addChild(outputObject);
                    outputObject.addParent(inputObject);
                    break;
                case OutputModes.UPDATE_INPUT:
                    inputObject.getCoordinateSet().clear();
                    inputObject.setCoordinateSet(outputObject.getCoordinateSet());
                    inputObject.clearSurface();
                    inputObject.clearCentroid();
                    inputObject.clearProjected();
                    inputObject.clearROIs();
                    break;
            }

            if (verbose)
                writeProgressStatus(count++, total, "objects", moduleName);

        }

        // Removing any objects which now have no volume
        if (removeEmptyObjects) {
            Iterator<ObjI> iterator = inputObjects.values().iterator();
            while (iterator.hasNext())
                if (iterator.next().getCoordinateSet().size() == 0)
                    iterator.remove();
        }

        return outputObjects;

    }

    public static <T extends RealType<T> & NativeType<T>> ObjI maskObject(ObjI inputObject, ImageI<T> maskImage,
            String maskObjectsName) {
        ObjsI tempObjects = ObjsFactories.getDefaultFactory().createFromExample(maskObjectsName, inputObject.getObjectCollection());

        // Creating the mask object
        ObjI maskObject = tempObjects.createAndAddNewObjectWithID(inputObject.getCoordinateSetFactory(), inputObject.getID());
        maskObject.setT(inputObject.getT());

        ImgPlus<T> maskImg = maskImage.getImgPlus();
        RandomAccess<T> randomAccess = maskImg.randomAccess();

        int xAx = maskImg.dimensionIndex(Axes.X);
        int yAx = maskImg.dimensionIndex(Axes.Y);
        int cAx = maskImg.dimensionIndex(Axes.CHANNEL);
        int zAx = maskImg.dimensionIndex(Axes.Z);
        int tAx = maskImg.dimensionIndex(Axes.TIME);

        // Iterating over all points in the object, retaining all points with non-zero
        // intensity
        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            long[] location = new long[maskImg.numDimensions()];
            if (xAx != -1)
                location[xAx] = point.getX();
            if (yAx != -1)
                location[yAx] = point.getY();
            if (cAx != -1)
                location[cAx] = 0;
            if (zAx != -1)
                location[zAx] = point.getZ();
            if (tAx != -1)
                location[tAx] = inputObject.getT();

            int value = ((UnsignedByteType) randomAccess.setPositionAndGet(location)).get();

            if (value != 0) {
                try {
                    maskObject.addPoint(point);
                } catch (PointOutOfRangeException e) {
                }
            }
        }

        return maskObject;

    }

    public MaskObjects(Modules modules) {
        super("Mask objects", modules);
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
        return "Applies the mask image to the specified object collection.  Any object coordinates coincident with black "
                + "pixels (intensity 0) will be removed.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);        
        String maskMode = parameters.getValue(MASK_MODE, workspace);
        String maskObjectsName = parameters.getValue(MASK_OBJECTS, workspace);
        String maskImageName = parameters.getValue(MASK_IMAGE, workspace);
        boolean removeEmptyObjects = parameters.getValue(REMOVE_EMPTY_OBJECTS, workspace);
        String outputMode = parameters.getValue(OBJECT_OUTPUT_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        if (outputMode.equals(OutputModes.UPDATE_INPUT))
            outputObjectsName = null;
            
        // If masking by objects, converting mask objects to an image
        ImageI<T> maskImage;
        switch (maskMode) {
            default:
                MIA.log.writeWarning("Mask not found");
                return Status.FAIL;
            case MaskModes.MASK_FROM_IMAGE:
                maskImage = workspace.getImage(maskImageName);
                break;
            case MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP:
            case MaskModes.MASK_FROM_OBJECTS_RETAIN_OVERLAP:
                ObjsI maskObjects = workspace.getObjects(maskObjectsName);
                HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(maskObjects,
                        ColourFactory.SingleColours.WHITE);
                maskImage = maskObjects.convertToImage("Mask", hues, 8, false, false);

                if (maskMode.equals(MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP))
                    InvertIntensity.process(maskImage);

                break;
        }

        ObjsI outputObjects = maskObjects(inputObjects, maskImage, outputObjectsName, removeEmptyObjects,
                removeEmptyObjects);

        switch (outputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                workspace.addObjects(outputObjects);
                if (showOutput)
                    outputObjects.convertToImageIDColours().showWithNormalisation(false);
                break;

            case OutputModes.UPDATE_INPUT:
                if (showOutput)
                    inputObjects.convertToImageIDColours().showWithNormalisation(false);
                break;
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, OutputModes.CREATE_NEW_OBJECT, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(MASK_SEPARATOR, this));
        parameters.add(new ChoiceP(MASK_MODE, this, MaskModes.MASK_FROM_IMAGE, MaskModes.ALL));
        parameters.add(new InputObjectsP(MASK_OBJECTS, this));
        parameters.add(new InputImageP(MASK_IMAGE, this));
        parameters.add(new BooleanP(REMOVE_EMPTY_OBJECTS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE, workspace)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(MASK_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MASK_MODE));
        switch ((String) parameters.getValue(MASK_MODE, workspace)) {
            case MaskModes.MASK_FROM_IMAGE:
                returnedParameters.add(parameters.getParameter(MASK_IMAGE));
                break;
            case MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP:
            case MaskModes.MASK_FROM_OBJECTS_RETAIN_OVERLAP:
                returnedParameters.add(parameters.getParameter(MASK_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(REMOVE_EMPTY_OBJECTS));

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
        parameters.get(INPUT_OBJECTS).setDescription("Objects to be masked.");

        parameters.get(OBJECT_OUTPUT_MODE).setDescription("Controls how the masked objects will be stored:<br><ul>"

                + "<li>\"" + OutputModes.CREATE_NEW_OBJECT
                + "\" (Default) Will add the masked objects to a new object set and store this set in the workspace.</li>"

                + "<li>\"" + OutputModes.UPDATE_INPUT
                + "\" Will replace the coordinates of the input object with the masked coordinates.  All measurements associated with input objects will be transferred to the masked objects.</li></ul>");

        parameters.get(OUTPUT_OBJECTS).setDescription("Name for the output masked objects to be stored in workspace.");

        parameters.get(MASK_MODE).setDescription(
                "Controls whether the input objects will be masked by an image or an object collection:<br><ul>"

                        + "<li>\"" + MaskModes.MASK_FROM_IMAGE
                        + "\" (Default) Input objects will be masked based on the image specified by \"" + MASK_IMAGE
                        + "\".  Any object regions coincident with black pixels (0 pixel intensity) will be removed.</li>"

                        + "<li>\"" + MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP
                        + "\" Input objects will be masked based on all objects of the collection specified by \""
                        + MASK_OBJECTS
                        + "\".  Any object regions coincident with any objects in the masking collection will be removed.  The masking objects will be unaffected by this process.</li>"

                        + "<li>\"" + MaskModes.MASK_FROM_OBJECTS_RETAIN_OVERLAP
                        + "\" Input objects will be masked based on all objects of the collection specified by \""
                        + MASK_OBJECTS
                        + "\".  Any object regions not coincident with any objects in the masking collection will be removed.  The masking objects will be unaffected by this process.</li></ul>");

        parameters.get(MASK_IMAGE).setDescription(
                "Image to use as mask on input objects.  Object coordinates coincident with black pixels (pixel intensity = 0) are removed.");

        parameters.get(MASK_OBJECTS).setDescription(
                "Object collection to use as mask on input objects.  Depending on which object-masking mode is selected, the input objects will either have coordinates coincident with these objects removed or retained.");

        parameters.get(REMOVE_EMPTY_OBJECTS).setDescription(
                "When selected, any objects which have no volume following masking will be removed.");

    }
}
