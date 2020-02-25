package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

public class MaskObjects <T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MASK_IMAGE = "Mask image";
    public static final String OBJECT_OUTPUT_MODE = "Output object mode";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public interface OutputModes {
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[]{CREATE_NEW_OBJECT,UPDATE_INPUT};

    }

    public static <T extends RealType<T> & NativeType<T>> Obj maskObject(Obj inputObject, Image<T> maskImage, String outputName, int outputID) {
        // Creating the mask object
        Obj maskObject = new Obj(outputName, outputID, inputObject);

        ImgPlus<T> maskImg = maskImage.getImgPlus();
        RandomAccess<T> randomAccess = maskImg.randomAccess();

        int xAx = maskImg.dimensionIndex(Axes.X);
        int yAx = maskImg.dimensionIndex(Axes.Y);
        int cAx = maskImg.dimensionIndex(Axes.CHANNEL);
        int zAx = maskImg.dimensionIndex(Axes.Z);
        int tAx = maskImg.dimensionIndex(Axes.TIME);

        // Iterating over all points in the object, retaining all points with non-zero intensity
        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            long[] location = new long[maskImg.numDimensions()];
            if (xAx != -1) location[xAx] = point.getX();
            if (yAx != -1) location[yAx] = point.getY();
            if (cAx != -1) location[cAx] = 0;
            if (zAx != -1) location[zAx] = point.getZ();
            if (tAx != -1) location[tAx] = inputObject.getT();

            randomAccess.setPosition(location);
            int value = ((UnsignedByteType) randomAccess.get()).get();

            if (value != 0) {
                try {maskObject.add(point);}
                catch (PointOutOfRangeException e) {}
            }
        }

        return maskObject;

    }

    public MaskObjects(ModuleCollection modules) {
        super("Mask objects", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Applies the mask image to the specified object collection.  Only object pixels coincident with black " +
                "pixels (intensity 0) will be removed.";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting mask image
        String maskImageName = parameters.getValue(MASK_IMAGE);
        Image maskImage = workspace.getImage(maskImageName);

        // Getting other parameters
        String outputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // If necessary, creating an output object collection
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,inputObjects.getCal());
        switch (outputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                workspace.addObjects(outputObjects);
                break;
        }

        for (Obj inputObject:inputObjects.values()) {
            int ID = outputObjects.getAndIncrementID();
            Obj maskedObject = maskObject(inputObject,maskImage,outputObjectsName,ID);

            switch (outputMode) {
                case OutputModes.CREATE_NEW_OBJECT:
                    outputObjects.add(maskedObject);
                    inputObject.addChild(maskedObject);
                    maskedObject.addParent(inputObject);
                    break;
                case OutputModes.UPDATE_INPUT:
                    inputObject.setCoordinateSet(maskedObject.getCoordinateSet());
                    inputObject.clearSurface();
                    inputObject.clearCentroid();
                    inputObject.clearProjected();
                    break;
            }
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

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this,"","Objects to be masked."));
        parameters.add(new InputImageP(MASK_IMAGE,this,"","Image to use as mask on input objects.  Object coordinates coincident with black pixels (pixel intensity = 0) are removed."));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE,this, OutputModes.CREATE_NEW_OBJECT, OutputModes.ALL,"Controls how the masked objects will be stored.<br>" +
                "<br> - \""+OutputModes.CREATE_NEW_OBJECT+"\" (default) will add the masked objects to a new object set and store this set in the workspace.<br>" +
                "<br> - \""+OutputModes.UPDATE_INPUT+"\" will replace the coordinates of the input object with the masked coordinates.  All measurements associated with input objects will be transferred to the masked objects."));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this,"","Name for the output masked objects to be stored in workspace."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MASK_IMAGE));

        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
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
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
                returnedRelationships.add(relationshipRefs.getOrPut(inputObjectsName,outputObjectsName));

                break;
        }

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
