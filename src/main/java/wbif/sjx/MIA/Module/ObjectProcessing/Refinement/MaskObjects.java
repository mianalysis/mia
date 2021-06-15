package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import java.util.HashMap;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

public class MaskObjects<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OBJECT_OUTPUT_MODE = "Output object mode";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String MASK_SEPARATOR = "Mask options";
    public static final String MASK_MODE = "Mask mode";
    public static final String MASK_OBJECTS = "Mask objects";
    public static final String MASK_IMAGE = "Mask image";

    public interface MaskModes {
        String MASK_FROM_IMAGE = "Mask from image";
        String MASK_FROM_OBJECTS_REMOVE_OVERLAP = "Mask from objects (remove overlap)";
        String MASK_FROM_OBJECTS_RETAIN_OVERLAP = "Mask from objects (retain overlap)";

        String[] ALL = new String[] { MASK_FROM_IMAGE, MASK_FROM_OBJECTS_REMOVE_OVERLAP, MASK_FROM_OBJECTS_RETAIN_OVERLAP };

    }

    public interface OutputModes {
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[] { CREATE_NEW_OBJECT, UPDATE_INPUT };

    }

    public static <T extends RealType<T> & NativeType<T>> Obj maskObject(Obj inputObject, Image<T> maskImage) {
        ObjCollection tempObjects = new ObjCollection("Mask objects", inputObject.getObjectCollection());

        // Creating the mask object
        Obj maskObject = tempObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());
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

            randomAccess.setPosition(location);
            int value = ((UnsignedByteType) randomAccess.get()).get();

            if (value != 0) {
                try {
                    maskObject.add(point);
                } catch (PointOutOfRangeException e) {
                }
            }
        }

        return maskObject;

    }

    public MaskObjects(ModuleCollection modules) {
        super("Mask objects", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Applies the mask image to the specified object collection.  Only object pixels coincident with black "
                + "pixels (intensity 0) will be removed.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting mask image/objects
        String maskMode = parameters.getValue(MASK_MODE);
        String maskObjectsName = parameters.getValue(MASK_OBJECTS);
        String maskImageName = parameters.getValue(MASK_IMAGE);

        // If masking by objects, converting mask objects to an image
        Image maskImage;
        switch (maskMode) {
        default:
            MIA.log.writeWarning("Mask not found");
            return Status.FAIL;
        case MaskModes.MASK_FROM_IMAGE:
            maskImage = workspace.getImage(maskImageName);
            break;
        case MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP:
        case MaskModes.MASK_FROM_OBJECTS_RETAIN_OVERLAP:
            ObjCollection maskObjects = workspace.getObjectSet(maskObjectsName);
            HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(maskObjects,
                    ColourFactory.SingleColours.WHITE);
            maskImage = maskObjects.convertToImage("Mask", hues, 8, false);

            if (maskMode.equals(MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP))
                InvertIntensity.process(maskImage);

            break;
        }

        // Getting other parameters
        String outputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // If necessary, creating an output object collection
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects);
        switch (outputMode) {
        case OutputModes.CREATE_NEW_OBJECT:
            workspace.addObjects(outputObjects);
            break;
        }

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        for (Obj inputObject : inputObjects.values()) {
            Obj maskedObject = maskObject(inputObject, maskImage);

            switch (outputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                outputObjects.add(maskedObject);
                maskedObject.setObjectCollection(outputObjects);
                inputObject.addChild(maskedObject);
                maskedObject.addParent(inputObject);
                break;
            case OutputModes.UPDATE_INPUT:
                inputObject.getCoordinateSet().clear();
                inputObject.getCoordinateSet().addAll(maskedObject.getCoordinateSet());
                inputObject.clearSurface();
                inputObject.clearCentroid();
                inputObject.clearProjected();
                inputObject.clearROIs();
                break;
            }

            writeProgressStatus(count++, total, "objects");

        }

        if (showOutput) {
            switch (outputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                outputObjects.convertToImageRandomColours().showImage();
                break;
            case OutputModes.UPDATE_INPUT:
                inputObjects.convertToImageRandomColours().showImage();
                break;
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, OutputModes.CREATE_NEW_OBJECT, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(MASK_SEPARATOR, this));
        parameters.add(new ChoiceP(MASK_MODE, this, MaskModes.MASK_FROM_IMAGE, MaskModes.ALL));
        parameters.add(new InputObjectsP(MASK_OBJECTS, this));
        parameters.add(new InputImageP(MASK_IMAGE, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
        case OutputModes.CREATE_NEW_OBJECT:
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
            break;
        }

        returnedParameters.add(parameters.getParameter(MASK_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MASK_MODE));
        switch ((String) parameters.getValue(MASK_MODE)) {
        case MaskModes.MASK_FROM_IMAGE:
            returnedParameters.add(parameters.getParameter(MASK_IMAGE));
            break;
        case MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP:
        case MaskModes.MASK_FROM_OBJECTS_RETAIN_OVERLAP:
            returnedParameters.add(parameters.getParameter(MASK_OBJECTS));
            break;
        }

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

        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
        case OutputModes.CREATE_NEW_OBJECT:
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

            break;
        }

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

                        + "\"" + MaskModes.MASK_FROM_IMAGE
                        + "\" (Default) Input objects will be masked based on the image specified by \"" + MASK_IMAGE
                        + "\".  Any object regions coincident with black pixels (0 pixel intensity) will be removed.<li>"

                        + "\"" + MaskModes.MASK_FROM_OBJECTS_REMOVE_OVERLAP
                        + "\" Input objects will be masked based on all objects of the collection specified by \""
                        + MASK_OBJECTS + "\".  Any object regions coincident with any objects in the masking collection will be removed.  The masking objects will be unaffected by this process.<li>"
                        
                        + "\"" + MaskModes.MASK_FROM_OBJECTS_RETAIN_OVERLAP
                        + "\" Input objects will be masked based on all objects of the collection specified by \""
                        + MASK_OBJECTS + "\".  Any object regions not coincident with any objects in the masking collection will be removed.  The masking objects will be unaffected by this process.<li></ul>");

        parameters.get(MASK_IMAGE).setDescription(
                "Image to use as mask on input objects.  Object coordinates coincident with black pixels (pixel intensity = 0) are removed.");

        parameters.get(MASK_OBJECTS).setDescription("Object collection to use as mask on input objects.  Depending on which object-masking mode is selected, the input objects will either have coordinates coincident with these objects removed or retained.");

    }
}
