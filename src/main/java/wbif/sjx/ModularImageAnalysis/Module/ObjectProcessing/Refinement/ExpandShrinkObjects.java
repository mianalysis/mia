package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import ij.ImagePlus;
import ij.Prefs;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sc13967 on 16/01/2018.
 */
public class ExpandShrinkObjects extends Module {
    public static final String INPUT_IMAGE = "Template image (sets object limits)";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String METHOD = "Method";
    public static final String RADIUS_CHANGE_PX = "Radius change (px)";

    public interface Methods {
        String EXPAND_2D = "Expand 2D";
        String EXPAND_3D = "Expand 3D";
        String SHRINK_2D = "Shrink 2D";
        String SHRINK_3D = "Shrink 3D";

        String[] ALL = new String[]{EXPAND_2D,EXPAND_3D,SHRINK_2D,SHRINK_3D};

    }

    @Override
    public String getTitle() {
        return "Expand and shrink objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String templateImageName = parameters.getValue(INPUT_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);
        ImagePlus templateImagePlus = templateImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS);
        String method = parameters.getValue(METHOD);
        int radiusChangePx = parameters.getValue(RADIUS_CHANGE_PX);

        // Storing the image calibration
        Obj firstObject = inputObjects.getFirst();
        double dppXY = firstObject.getDistPerPxXY();
        double dppZ = firstObject.getDistPerPxZ();
        String calibrationUnits = firstObject.getCalibratedUnits();
        boolean twoD = firstObject.is2D();

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()){
            Obj inputObject = iterator.next();
            writeMessage("Processing object " + (count++) + " of " + total);

            // Convert each object to an image, do the dilation/erosion, then convert back to an object
            ObjCollection objectCollection = new ObjCollection("ObjectToMorph");
            objectCollection.add(inputObject);
            HashMap<Integer,Float> hues = objectCollection.getHues(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
            Image objectImage = objectCollection.convertObjectsToImage("Object image", templateImagePlus, ConvertObjectsToImage.ColourModes.SINGLE_COLOUR,hues);
            InvertIntensity.process(objectImage.getImagePlus());

            Prefs.blackBackground = false;

            // Applying morphological transform.  Erode and dilate are used "backwards", as the image that comes
            // from the converter has white objects on a black background.
            switch (method) {
                case Methods.EXPAND_2D:
                    BinaryOperations.applyStockBinaryTransform(objectImage.getImagePlus(),
                            BinaryOperations.OperationModes.DILATE_2D,radiusChangePx);
                    break;

                case Methods.EXPAND_3D:
                    BinaryOperations.getDilateErode3D(objectImage.getImagePlus(),
                            BinaryOperations.OperationModes.DILATE_3D,radiusChangePx);
                    break;

                case Methods.SHRINK_2D:
                    BinaryOperations.applyStockBinaryTransform(objectImage.getImagePlus(),
                            BinaryOperations.OperationModes.ERODE_2D,radiusChangePx);
                    break;

                case Methods.SHRINK_3D:
                    BinaryOperations.getDilateErode3D(objectImage.getImagePlus(),
                            BinaryOperations.OperationModes.ERODE_3D,radiusChangePx);
                    break;
            }

            InvertIntensity.process(objectImage.getImagePlus());

            // Creating a new object collection (only contains one image) from the transformed image
            ObjCollection newObjects = objectImage.convertImageToObjects("NewObjects");

            // During object shrinking it's possible the object will disappear entirely
            if (newObjects.size() == 0) {
                iterator.remove();
                continue;
            }

            // If the input objects are to be transformed, taking the new pixel coordinates and applying them to
            // the input object.  Otherwise, the new object is added to the nascent ObjCollection.
            if (updateInputObjects) {
                inputObject.setPoints(newObjects.values().iterator().next().getPoints());
            } else {
                Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibrationUnits,twoD);
                outputObject.setPoints(newObjects.values().iterator().next().getPoints());
                outputObjects.add(outputObject);
            }
        }

        // If selected, adding new ObjCollection to the Workspace
        if (!updateInputObjects) workspace.addObjects(outputObjects);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(UPDATE_INPUT_OBJECTS,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(METHOD,Parameter.CHOICE_ARRAY,Methods.EXPAND_2D,Methods.ALL));
        parameters.add(new Parameter(RADIUS_CHANGE_PX,Parameter.INTEGER,1.0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (! (boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(METHOD));
        returnedParameters.add(parameters.getParameter(RADIUS_CHANGE_PX));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
