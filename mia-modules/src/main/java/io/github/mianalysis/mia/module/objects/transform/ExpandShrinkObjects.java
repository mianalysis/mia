package io.github.mianalysis.mia.module.objects.transform;

import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.images.process.binary.DilateErode;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 16/01/2018.
 */

/**
* Expands or shrinks all objects in a specified object collection from the workspace.  Expand and shrink operations can be performed in 2D or 3D.  These are effectively binary dilate and erode operations, respectively.  Input objects can be updated with the post-hole filling coordinates, or all output objects can be stored in the workspace as a new collection.<br><br>Note: MIA permits object overlap, so objects may share coordinates.  This is important to consider if subsequently converting objects to an image, where it's not possible to represent both objects in shared pixels.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ExpandShrinkObjects extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input/output";

	/**
	* Object collection from the workspace to apply the expand or shrink operation to.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* When selected, the post-operation objects will update the input objects in the workspace (all measurements and relationships will be retained).  Otherwise, the objects will be saved to the workspace in a new collection with the name specified by the "Output objects" parameter.  Note: If updating the objects, any previously-measured object properties (e.g. object volume) may become invalid.  To update such measurements it's necessary to re-run the relevant measurement modules.
	*/
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";

	/**
	* If "Update input objects" is not selected, the post-operation objects will be saved to the workspace in a new collection with this name.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";


	/**
	* 
	*/
    public static final String PROCESSING_SEPARATOR = "Processing options";

	/**
	* Controls which expand or shrink operation is applied to the input objects:<br><ul><li>"Expand 2D" Adds any non-object coordinates within "Radius change" of the object to the object.  This operates in a slice-by-slice manner, irrespective of whether a 2D or 3D object is provided.  This effectively runs a 2D binary dilation operation on each object. Uses ImageJ implementation.</li><li>"Expand 3D" Adds any non-object coordinates within "Radius change" of the object to the object.  This effectively runs a 3D binary dilation operation on each object.  Uses MorphoLibJ implementation.</li><li>"Shrink 2D" Removes any object coordinates within "Radius change" of the object boundary from the object.  This operates in a slice-by-slice manner, irrespective of whether a 2D or 3D object is provided.  This effectively runs a 2D binary erosion operation on each object.  Uses ImageJ implementation.</li><li>"Shrink 3D" Removes any object coordinates within "Radius change" of the object boundary from the object.  This effectively runs a 3D binary erosion operation on each object.  Uses MorphoLibJ implementation.</li></ul>
	*/
    public static final String METHOD = "Method";

	/**
	* 
	*/
    public static final String RADIUS_CHANGE_SOURCE = "Radius change source";

	/**
	* Distance from the object boundary to test for potential inclusion or removal of coordinates.  When expanding, any non-object coordinates within this distance of the object are included in the object.  While shrinking, any object coordinates within this distance of the object boundary are removed from the object.  This value is assumed specified in pixel coordinates unless "Calibrated units" is selected.
	*/
    public static final String RADIUS_CHANGE = "Radius change";

	/**
	* 
	*/
    public static final String MEASUREMENT = "Measurement";

	/**
	* When selected, "Radius change" is assumed to be specified in calibrated units (as defined by the "Input control" parameter "Spatial unit").  Otherwise, pixel units are assumed.
	*/
    public static final String CALIBRATED_UNITS = "Calibrated units";

    public ExpandShrinkObjects(ModulesI modules) {
        super("Expand and shrink objects", modules);
    }

    public interface Methods {
        String EXPAND_2D = "Expand 2D";
        String EXPAND_3D = "Expand 3D";
        String SHRINK_2D = "Shrink 2D";
        String SHRINK_3D = "Shrink 3D";

        String[] ALL = new String[] { EXPAND_2D, EXPAND_3D, SHRINK_2D, SHRINK_3D };

    }

    public interface RadiusChangeSources {
        String FIXED_VALUE = "Fixed value";
        String OBJECT_MEASUREMENT = "Object measurement";

        String[] ALL = new String[] { FIXED_VALUE, OBJECT_MEASUREMENT };

    }

    public static ObjI processObject(ObjI inputObject, String method, int radiusChangePx)
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
                borderWidths = new int[][] { { 1, 1 }, { 1, 1 }, { 1, 1 } };
                break;
        }

        ImageI objectImage = inputObject.getAsTightImageWithBorders("Temp", borderWidths);

        Prefs.blackBackground = false;

        // Applying morphological transform. Erode and dilate are used "backwards", as
        // the image that comes
        // from the converter has white objects on a black background.
        switch (method) {
            case Methods.EXPAND_2D:
                DilateErode.process(objectImage.getImagePlus(), DilateErode.OperationModes.DILATE_2D, true,
                        radiusChangePx, false);
                break;

            case Methods.EXPAND_3D:
                DilateErode.process(objectImage.getImagePlus(), DilateErode.OperationModes.DILATE_3D, true,
                        radiusChangePx, false);
                break;

            case Methods.SHRINK_2D:
                DilateErode.process(objectImage.getImagePlus(), DilateErode.OperationModes.ERODE_2D, true,
                        radiusChangePx, false);
                break;

            case Methods.SHRINK_3D:
                DilateErode.process(objectImage.getImagePlus(), DilateErode.OperationModes.ERODE_3D, true,
                        radiusChangePx, false);
                break;
        }

        // Creating a new object collection (only contains one image) from the
        // transformed image
        ObjsI outputObjects = objectImage.convertImageToObjects(inputObject.getCoordinateSetFactory(), "NewObjects", false);

        // During object shrinking it's possible the object will disappear entirely
        if (outputObjects.size() == 0)
            return null;

        ObjI outputObject = outputObjects.getFirst();

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]) - borderWidths[0][0];
        int yOffs = (int) Math.round(extents[1][0]) - borderWidths[1][0];
        int zOffs = (int) Math.round(extents[2][0]) - borderWidths[2][0];

        // Updating the output objects spatial calibration to the full range, then
        // moving objects to the correct positions
        outputObjects.setCalibrationFromExample(inputObject, true);
        outputObject.translateCoords(xOffs, yOffs, zOffs);
        outputObject.setT(inputObject.getT());

        return outputObject;

    }

    static void transferObjectCoordinates(ObjI fromObj, ObjI toObj, int[][] borderWidths, double[][] extents) {
        // Create empty image
        int xOffs = (int) Math.round(extents[0][0]) - borderWidths[0][0];
        int yOffs = (int) Math.round(extents[1][0]) - borderWidths[1][0];
        int zOffs = (int) Math.round(extents[2][0]) - borderWidths[2][0];

        // Populating ipl
        for (Point<Integer> point : fromObj.getCoordinateSet())
            try {
                toObj.addCoord(point.x + xOffs, point.y + yOffs, point.z + zOffs);
            } catch (PointOutOfRangeException e) {
            }

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
        return "Expands or shrinks all objects in a specified object collection from the workspace.  Expand and shrink operations can be performed in 2D or 3D.  These are effectively binary dilate and erode operations, respectively.  Input objects can be updated with the post-hole filling coordinates, or all output objects can be stored in the workspace as a new collection."

                + "<br><br>Note: MIA permits object overlap, so objects may share coordinates.  This is important to consider if subsequently converting objects to an image, where it's not possible to represent both objects in shared pixels.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromExample(outputObjectsName, inputObjects);

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS,workspace);
        String method = parameters.getValue(METHOD,workspace);
        String radiusChangeSource = parameters.getValue(RADIUS_CHANGE_SOURCE,workspace);
        double radiusChange = parameters.getValue(RADIUS_CHANGE,workspace);
        String measurementName = parameters.getValue(MEASUREMENT,workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS,workspace);

        // Storing the image calibration
        ObjI firstObj = inputObjects.getFirst();
        if (firstObj == null) {
            if (!updateInputObjects)
                workspace.addObjects(outputObjects);

            return Status.PASS;

        }

        // Iterating over all objects
        int count = 0;
        int total = inputObjects.size();

        Iterator<ObjI> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            ObjI inputObject = iterator.next();
            ObjI newObject = null;

            double appliedRadiusChange = radiusChange;
            if (radiusChangeSource.equals(RadiusChangeSources.OBJECT_MEASUREMENT))
                appliedRadiusChange = inputObject.getMeasurement(measurementName).getValue();

            if (calibratedUnits)
                appliedRadiusChange = appliedRadiusChange / firstObj.getDppXY();

            int radiusChangePx = (int) Math.round(appliedRadiusChange);

            try {
                newObject = processObject(inputObject, method, radiusChangePx);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }

            // During object shrinking it's possible the object will disappear entirely
            if (newObject == null) {
                iterator.remove();
                count++;
                continue;
            }

            // If the input objects are to be transformed, taking the new pixel coordinates
            // and applying them to
            // the input object. Otherwise, the new object is added to the nascent
            // Objs.
            if (updateInputObjects) {
                inputObject.getCoordinateSet().clear();
                inputObject.getCoordinateSet().addAll(newObject.getCoordinateSet());
                inputObject.clearSurface();
                inputObject.clearCentroid();
                inputObject.clearProjected();
                inputObject.clearROIs();

            } else {
                ObjI outputObject = outputObjects.createAndAddNewObject(firstObj.getCoordinateSetFactory());
                outputObject.setCoordinateSet(newObject.getCoordinateSet());
                outputObject.setT(newObject.getT());
                outputObject.addParent(inputObject);
                inputObject.addChild(outputObject);
            }

            writeProgressStatus(++count, total, "objects");

        }

        // If selected, adding new Objs to the Workspace
        if (!updateInputObjects)
            workspace.addObjects(outputObjects);

        // Displaying updated objects
        if (showOutput) {
            if (updateInputObjects)
                inputObjects.convertToImageIDColours().showWithNormalisation(false);
            else
                outputObjects.convertToImageIDColours().showWithNormalisation(false);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(UPDATE_INPUT_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(PROCESSING_SEPARATOR, this));
        parameters.add(new ChoiceP(METHOD, this, Methods.EXPAND_2D, Methods.ALL));
        parameters
                .add(new ChoiceP(RADIUS_CHANGE_SOURCE, this, RadiusChangeSources.FIXED_VALUE, RadiusChangeSources.ALL));
        parameters.add(new DoubleP(RADIUS_CHANGE, this, 1d));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS,workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(METHOD));
        returnedParameters.add(parameters.getParameter(RADIUS_CHANGE_SOURCE));

        switch ((String) parameters.getValue(RADIUS_CHANGE_SOURCE,workspace)) {
            case RadiusChangeSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(RADIUS_CHANGE));
                break;
            case RadiusChangeSources.OBJECT_MEASUREMENT:
                ObjectMeasurementP parameter = parameters.getParameter(MEASUREMENT);
                parameter.setObjectName(parameters.getValue(INPUT_OBJECTS,workspace));
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                break;
        }
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

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

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS,workspace))
            returnedRelationships.add(
                    parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS,workspace), parameters.getValue(OUTPUT_OBJECTS,workspace)));

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
        parameters.get(INPUT_OBJECTS)
                .setDescription("Object collection from the workspace to apply the expand or shrink operation to.");

        parameters.get(UPDATE_INPUT_OBJECTS).setDescription(
                "When selected, the post-operation objects will update the input objects in the workspace (all measurements and relationships will be retained).  Otherwise, the objects will be saved to the workspace in a new collection with the name specified by the \""
                        + OUTPUT_OBJECTS
                        + "\" parameter.  Note: If updating the objects, any previously-measured object properties (e.g. object volume) may become invalid.  To update such measurements it's necessary to re-run the relevant measurement modules.");

        parameters.get(OUTPUT_OBJECTS).setDescription("If \"" + UPDATE_INPUT_OBJECTS
                + "\" is not selected, the post-operation objects will be saved to the workspace in a new collection with this name.");

        parameters.get(METHOD)
                .setDescription("Controls which expand or shrink operation is applied to the input objects:<br><ul>"

                        + "<li>\"" + Methods.EXPAND_2D + "\" Adds any non-object coordinates within \"" + RADIUS_CHANGE
                        + "\" of the object to the object.  This operates in a slice-by-slice manner, irrespective of whether a 2D or 3D object is provided.  This effectively runs a 2D binary dilation operation on each object. Uses ImageJ implementation.</li>"

                        + "<li>\"" + Methods.EXPAND_3D + "\" Adds any non-object coordinates within \"" + RADIUS_CHANGE
                        + "\" of the object to the object.  This effectively runs a 3D binary dilation operation on each object.  Uses MorphoLibJ implementation.</li>"

                        + "<li>\"" + Methods.SHRINK_2D + "\" Removes any object coordinates within \"" + RADIUS_CHANGE
                        + "\" of the object boundary from the object.  This operates in a slice-by-slice manner, irrespective of whether a 2D or 3D object is provided.  This effectively runs a 2D binary erosion operation on each object.  Uses ImageJ implementation.</li>"

                        + "<li>\"" + Methods.SHRINK_3D + "\" Removes any object coordinates within \"" + RADIUS_CHANGE
                        + "\" of the object boundary from the object.  This effectively runs a 3D binary erosion operation on each object.  Uses MorphoLibJ implementation.</li></ul>");

        parameters.get(RADIUS_CHANGE).setDescription(
                "Distance from the object boundary to test for potential inclusion or removal of coordinates.  When expanding, any non-object coordinates within this distance of the object are included in the object.  While shrinking, any object coordinates within this distance of the object boundary are removed from the object.  This value is assumed specified in pixel coordinates unless \""
                        + CALIBRATED_UNITS + "\" is selected.");

        parameters.get(CALIBRATED_UNITS)
                .setDescription("When selected, \"" + RADIUS_CHANGE
                        + "\" is assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

    }
}
