package io.github.mianalysis.mia.module.objects.transform;

import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations2D;
import io.github.mianalysis.mia.module.images.process.binary.FillHoles;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
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
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 16/01/2018.
 */

/**
* Fills holes in all objects in a collection.  Holes are considered as non-object regions bounded on all sides by object coordinates.  This operation is performed on an object-by-object basis, so only holes bounded by coordinates of the same object will be filled.  Holes can be filled slice-by-slice in 2D (considering only coordiantes in a single XY plane using 4-way connectivity) or in full 3D (considering all surrounding coordinates using 6-way connectivity).  Input objects can be updated with the post-hole filling coordinates, or all output objects can be stored in the workspace as a new collection.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FillHolesInObjects extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input/output";

	/**
	* Object collection from the workspace to apply fill holes operation to.
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
	* Controls whether the holes are filled in 2D or 3D:<br><ul><li>"Fill holes 2D" Holes are considered on a slice-by-slice basis (i.e. 4-way connectivity within a slice).  This can be applied to both 2D and 3D objects.</li><li>"Fill holes 2D" Holes are considered in 3D (i.e. 6-way connectivity).  Note: If a 2D object (either from a 2D or 3D source) is loaded, no holes will be filled, as the top and bottom will be considered "open".</li></ul>
	*/
    public static final String METHOD = "Method";

    public FillHolesInObjects(Modules modules) {
        super("Fill holes in objects", modules);
    }

    public interface Methods {
        String FILL_HOLES_2D = "Fill holes 2D";
        String FILL_HOLES_3D = "Fill holes 3D";

        String[] ALL = new String[] { FILL_HOLES_2D, FILL_HOLES_3D };

    }

    public static Obj processObject(Obj inputObject, String method) throws IntegerOverflowException {
        // Convert each object to an image, do the hole filling, then convert back to an
        // object
        Image objectImage = inputObject.getAsTightImage("Temp");

        // Applying morphological transform. Erode and dilate are used "backwards", as
        // the image that comes
        // from the converter has white objects on a black background.
        switch (method) {
            case Methods.FILL_HOLES_2D:
                BinaryOperations2D.process(objectImage, BinaryOperations2D.OperationModes.FILL_HOLES, 1, 1, true);
                break;

            case Methods.FILL_HOLES_3D:
                FillHoles.process(objectImage.getImagePlus(),true);
                break;
        }

        // Creating a new object collection (only contains one image) from the
        // transformed image
        Objs outputObjects = objectImage.convertImageToObjects(inputObject.getVolumeType(), "NewObjects");
        Obj outputObject = outputObjects.getFirst();

        double[][] extents = inputObject.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

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
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Fills holes in all objects in a collection.  Holes are considered as non-object regions bounded on all sides by object coordinates.  This operation is performed on an object-by-object basis, so only holes bounded by coordinates of the same object will be filled.  Holes can be filled slice-by-slice in 2D (considering only coordiantes in a single XY plane using 4-way connectivity) or in full 3D (considering all surrounding coordinates using 6-way connectivity).  Input objects can be updated with the post-hole filling coordinates, or all output objects can be stored in the workspace as a new collection.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS,workspace);
        String method = parameters.getValue(METHOD,workspace);

        // Storing the image calibration
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null)
            return Status.PASS;

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            Obj newObject = null;
            try {
                newObject = processObject(inputObject, method);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
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
                Obj outputObject = outputObjects.createAndAddNewObject(firstObj.getVolumeType());
                outputObject.setCoordinateSet(newObject.getCoordinateSet());
                outputObject.setT(newObject.getT());
                outputObject.addParent(inputObject);
                inputObject.addChild(outputObject);

            }

            writeProgressStatus(count++, total, "objects");
            
        }

        // If selected, adding new Objs to the Workspace
        if (!updateInputObjects)
            workspace.addObjects(outputObjects);

        // Displaying updated objects
        if (showOutput) {
            if (updateInputObjects)
                inputObjects.convertToImageIDColours().show(false);
            else
                outputObjects.convertToImageIDColours().show(false);
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
        parameters.add(new ChoiceP(METHOD, this, Methods.FILL_HOLES_2D, Methods.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(METHOD));

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

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS,workspace)) {
            returnedRelationships.add(
                    parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS,workspace), parameters.getValue(OUTPUT_OBJECTS,workspace)));
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
        parameters.get(INPUT_OBJECTS)
                .setDescription("Object collection from the workspace to apply fill holes operation to.");

        parameters.get(UPDATE_INPUT_OBJECTS).setDescription(
                "When selected, the post-operation objects will update the input objects in the workspace (all measurements and relationships will be retained).  Otherwise, the objects will be saved to the workspace in a new collection with the name specified by the \""
                        + OUTPUT_OBJECTS + "\" parameter.  Note: If updating the objects, any previously-measured object properties (e.g. object volume) may become invalid.  To update such measurements it's necessary to re-run the relevant measurement modules.");

        parameters.get(OUTPUT_OBJECTS).setDescription("If \"" + UPDATE_INPUT_OBJECTS
                + "\" is not selected, the post-operation objects will be saved to the workspace in a new collection with this name.");

        parameters.get(METHOD).setDescription("Controls whether the holes are filled in 2D or 3D:<br><ul>"

                + "<li>\"" + Methods.FILL_HOLES_2D
                + "\" Holes are considered on a slice-by-slice basis (i.e. 4-way connectivity within a slice).  This can be applied to both 2D and 3D objects.</li>"

                + "<li>\"" + Methods.FILL_HOLES_2D
                + "\" Holes are considered in 3D (i.e. 6-way connectivity).  Note: If a 2D object (either from a 2D or 3D source) is loaded, no holes will be filled, as the top and bottom will be considered \"open\".</li></ul>");

    }
}
