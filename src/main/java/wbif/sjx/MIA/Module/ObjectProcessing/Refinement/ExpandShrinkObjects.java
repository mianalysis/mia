package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import java.util.Iterator;

import ij.Prefs;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DilateErode;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

/**
 * Created by sc13967 on 16/01/2018.
 */
public class ExpandShrinkObjects extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String PROCESSING_SEPARATOR = "Processing options";
    public static final String METHOD = "Method";
    public static final String RADIUS_CHANGE = "Radius change";
    public static final String CALIBRATED_UNITS = "Calibrated units";

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
        int[][] borderWidths;
        switch (method) {
            case Methods.EXPAND_2D:
                borderWidths = new int[][] { { radiusChangePx, radiusChangePx }, { radiusChangePx, radiusChangePx },
                        { 0, 0 } };
                break;

            case Methods.EXPAND_3D:
            default:
                borderWidths = new int[][] { { radiusChangePx, radiusChangePx }, { radiusChangePx, radiusChangePx },
                        { radiusChangePx, radiusChangePx } };
                break;

            case Methods.SHRINK_2D:
            case Methods.SHRINK_3D:
                borderWidths = new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } };
                break;
        }

        Image objectImage = inputObject.getAsTightImage("Temp", borderWidths);
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
        ObjCollection outputObjects = objectImage.convertImageToObjects(inputObject.getVolumeType(), "NewObjects");

        // During object shrinking it's possible the object will disappear entirely
        if (outputObjects.size() == 0)
            return null;

        Obj outputObject = outputObjects.getFirst();

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]) - borderWidths[0][0];
        int yOffs = (int) Math.round(extents[1][0]) - borderWidths[1][0];
        int zOffs = (int) Math.round(extents[2][0]) - borderWidths[2][0];

        // Updating the output objects spatial calibration to the full range, then
        // moving objects to the correct positions
        outputObjects.setSpatialCalibration(inputObject.getSpatialCalibration(), true);
        outputObject.translateCoords(xOffs, yOffs, zOffs);

        return outputObject;

    }

    static void transferObjectCoordinates(Obj fromObj, Obj toObj, int[][] borderWidths, double[][] extents) {
        // Create empty image
        int xOffs = (int) Math.round(extents[0][0]) - borderWidths[0][0];
        int yOffs = (int) Math.round(extents[1][0]) - borderWidths[1][0];
        int zOffs = (int) Math.round(extents[2][0]) - borderWidths[2][0];

        // Populating ipl
        for (Point<Integer> point : fromObj.getCoordinateSet())
            try {
                toObj.add(point.x + xOffs, point.y + yOffs, point.z + zOffs);
            } catch (PointOutOfRangeException e) {
            }

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Expands or shrinks all objects in a specified object collection from the workspace.  Expand and shrink operations can be performed in 2D or 3D.  These are effectively binary dilate and erode operations, respectively.  Input objects can be updated with the post-hole filling coordinates, or all output objects can be stored in the workspace as a new collection."

        +"<br><br>Note: MIA permits object overlap, so objects may share coordinates.  This is important to consider if subsequently converting objects to an image, where it's not possible to represent both objects in shared pixels.";
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
        double radiusChange = parameters.getValue(RADIUS_CHANGE);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);

        // Storing the image calibration
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null) {
            if (!updateInputObjects)
                workspace.addObjects(outputObjects);

            return Status.PASS;
            
        }
            
        if (calibratedUnits)
            radiusChange = radiusChange / firstObj.getDppXY();

        int radiusChangePx = (int) Math.round(radiusChange);

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            writeStatus("Processing object " + (count++) + " of " + total);

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
                inputObject.clearROIs();

            } else {
                Obj outputObject = new Obj(outputObjectsName, outputObjects.getAndIncrementID(), firstObj);
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
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(UPDATE_INPUT_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(PROCESSING_SEPARATOR, this));
        parameters.add(new ChoiceP(METHOD, this, Methods.EXPAND_2D, Methods.ALL));
        parameters.add(new DoubleP(RADIUS_CHANGE, this, 1));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(METHOD));
        returnedParameters.add(parameters.getParameter(RADIUS_CHANGE));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

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

    void addParameterDescriptions() {
      parameters.get(INPUT_OBJECTS)
              .setDescription("Object collection from the workspace to apply the expand or shrink operation to.");

      parameters.get(UPDATE_INPUT_OBJECTS).setDescription(
              "When selected, the post-operation objects will update the input objects in the workspace (all measurements and relationships will be retained).  Otherwise, the objects will be saved to the workspace in a new collection with the name specified by the \""
                      + OUTPUT_OBJECTS + "\" parameter.  Note: If updating the objects, any previously-measured object properties (e.g. object volume) may become invalid.  To update such measurements it's necessary to re-run the relevant measurement modules.");

      parameters.get(OUTPUT_OBJECTS).setDescription("If \"" + UPDATE_INPUT_OBJECTS
              + "\" is not selected, the post-operation objects will be saved to the workspace in a new collection with this name.");

        parameters.get(METHOD).setDescription("Controls which expand or shrink operation is applied to the input objects:<br><ul>"

        + "<li>\"" + Methods.EXPAND_2D
        + "\" Adds any non-object coordinates within \""+RADIUS_CHANGE+"\" of the object to the object.  This operates in a slice-by-slice manner, irrespective of whether a 2D or 3D object is provided.  This effectively runs a 2D binary dilation operation on each object. Uses ImageJ implementation.</li>"

        + "<li>\"" + Methods.EXPAND_3D
        + "\" Adds any non-object coordinates within \""+RADIUS_CHANGE+"\" of the object to the object.  This effectively runs a 3D binary dilation operation on each object.  Uses MorphoLibJ implementation.</li>"

        + "<li>\"" + Methods.SHRINK_2D
        + "\" Removes any object coordinates within \""+RADIUS_CHANGE+"\" of the object boundary from the object.  This operates in a slice-by-slice manner, irrespective of whether a 2D or 3D object is provided.  This effectively runs a 2D binary erosion operation on each object.  Uses ImageJ implementation.</li>"

        + "<li>\"" + Methods.SHRINK_3D
        + "\" Removes any object coordinates within \""+RADIUS_CHANGE+"\" of the object boundary from the object.  This effectively runs a 3D binary erosion operation on each object.  Uses MorphoLibJ implementation.</li></ul>");

        parameters.get(RADIUS_CHANGE).setDescription("Distance from the object boundary to test for potential inclusion or removal of coordinates.  When expanding, any non-object coordinates within this distance of the object are included in the object.  While shrinking, any object coordinates within this distance of the object boundary are removed from the object.  This value is assumed specified in pixel coordinates unless \""+CALIBRATED_UNITS+"\" is selected.");

        parameters.get(CALIBRATED_UNITS).setDescription("When selected, \""+RADIUS_CHANGE+"\" is assumed to be specified in calibrated units (as defined by the \""+new InputControl(null).getName()+"\" parameter \""+InputControl.SPATIAL_UNITS+"\").  Otherwise, pixel units are assumed.");

    }
}
