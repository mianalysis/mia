package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import java.util.HashMap;
import java.util.Iterator;

import ij.Prefs;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DilateErode;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 16/01/2018.
 */
public class ExpandShrinkObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String METHOD = "Method";
    public static final String RADIUS_CHANGE_PX = "Radius change (px)";

    public ExpandShrinkObjects(ModuleCollection modules) {
        super("Expand and shrink objects", modules);
    }

    public interface Methods {
        String EXPAND_2D = "Expand 2D";
        String EXPAND_3D = "Expand 3D";
        String SHRINK_2D = "Shrink 2D";
        String SHRINK_3D = "Shrink 3D";

        String[] ALL = new String[] { EXPAND_2D, EXPAND_3D, SHRINK_2D, SHRINK_3D };

    }

    public static Obj processObject(Obj inputObject, String method, int radiusChangePx)
            throws IntegerOverflowException {
        // Convert each object to an image, do the dilation/erosion, then convert back
        // to an object
        Image objectImage = inputObject.convertObjToImage("ObjectToMorph");
        InvertIntensity.process(objectImage);

        Prefs.blackBackground = false;

        // Applying morphological transform. Erode and dilate are used "backwards", as
        // the image that comes
        // from the converter has white objects on a black background.
        switch (method) {
            case Methods.EXPAND_2D:
                BinaryOperations2D.process(objectImage, BinaryOperations2D.OperationModes.DILATE, radiusChangePx, 1);
                break;

            case Methods.EXPAND_3D:
                DilateErode.process(objectImage.getImagePlus(), DilateErode.OperationModes.DILATE_3D, radiusChangePx);
                break;

            case Methods.SHRINK_2D:
                BinaryOperations2D.process(objectImage, BinaryOperations2D.OperationModes.ERODE, radiusChangePx, 1);
                break;

            case Methods.SHRINK_3D:
                DilateErode.process(objectImage.getImagePlus(), DilateErode.OperationModes.ERODE_3D, radiusChangePx);
                break;
        }

        InvertIntensity.process(objectImage);

        // Creating a new object collection (only contains one image) from the
        // transformed image
        ObjCollection newObjects = objectImage.convertImageToObjects(inputObject.getVolumeType(), "NewObjects");

        // During object shrinking it's possible the object will disappear entirely
        if (newObjects.size() == 0)
            return null;

        return newObjects.getFirst();

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects);

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS);
        String method = parameters.getValue(METHOD);
        int radiusChangePx = parameters.getValue(RADIUS_CHANGE_PX);

        // Storing the image calibration
        Obj firstObject = inputObjects.getFirst();
        if (firstObject == null)
            return Status.PASS;

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            writeMessage("Processing object " + (count++) + " of " + total);

            Obj newObject = null;
            try {
                newObject = processObject(inputObject, method, radiusChangePx);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }

            // During object shrinking it's possible the object will disappear entirely
            if (newObject == null) {
                iterator.remove();
                continue;
            }

            // If the input objects are to be transformed, taking the new pixel coordinates
            // and applying them to
            // the input object. Otherwise, the new object is added to the nascent
            // ObjCollection.
            if (updateInputObjects) {
                inputObject.getCoordinateSet().clear();
                inputObject.getCoordinateSet().addAll(newObject.getCoordinateSet());
                inputObject.clearSurface();
                inputObject.clearCentroid();
                inputObject.clearProjected();
            } else {
                Obj outputObject = new Obj(outputObjectsName, outputObjects.getAndIncrementID(), firstObject);
                outputObject.setCoordinateSet(newObject.getCoordinateSet());
                outputObject.setT(newObject.getT());
                outputObject.addParent(inputObject);
                inputObject.addChild(outputObject);
                outputObjects.add(outputObject);
            }
        }

        // If selected, adding new ObjCollection to the Workspace
        if (!updateInputObjects)
            workspace.addObjects(outputObjects);

        // Displaying updated objects
        if (showOutput) {
            if (updateInputObjects)
                inputObjects.convertToImageRandomColours().showImage();
            else
                outputObjects.convertToImageRandomColours().showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(UPDATE_INPUT_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(METHOD, this, Methods.EXPAND_2D, Methods.ALL));
        parameters.add(new IntegerP(RADIUS_CHANGE_PX, this, 1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(METHOD));
        returnedParameters.add(parameters.getParameter(RADIUS_CHANGE_PX));

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

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedRelationships.add(
                    parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS), parameters.getValue(OUTPUT_OBJECTS)));
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
}
