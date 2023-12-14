package io.github.mianalysis.mia.module.objects.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.gui.Roi;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Fit 2D convex hull to a 2D object.  If objects are in 3D, a convex hull is fit slice-by-slice.<br><br>Uses the ImageJ "Fit convex hull" function.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitConvexHull2D extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input/output";

	/**
	* Input objects to create 2D convex hulls for.  Each convex hull will be a child of its respective input object.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* Output convex hull objects will be stored in the workspace with this name.  Each convex hull object will be a child of the input object it was created from.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";
    
    public Obj processObject(Obj inputObject, Objs outputObjects) {
        // We have to explicitly define this, as the number of slices is 1 (potentially
        // unlike the input object)
        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
        outputObject.setT(inputObject.getT());
        outputObject.addParent(inputObject);
        inputObject.addChild(outputObject);
        
        // Process slice-by-slice
        for (int z = 0; z < inputObject.getNSlices(); z++) {
            Roi roi = inputObject.getRoi(z);
            if (roi == null)
            continue;
            
            try {
                outputObject.addPointsFromPolygon(roi.getConvexHull(), z);
            } catch (PointOutOfRangeException e) {
            }
        }
        
        return outputObject;
        
    }
    
    public FitConvexHull2D(Modules modules) {
        super("Fit convex hull 2D", modules);
    }
    
    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Fit 2D convex hull to a 2D object.  If objects are in 3D, a convex hull is fit slice-by-slice.<br><br>"
        +
        "Uses the ImageJ \"Fit convex hull\" function.";
    }
    
    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }
    
    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        
        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        
        // If necessary, creating a new Objs and adding it to the Workspace
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);
        workspace.addObjects(outputObjects);
        
        int count = 0;
        int total = inputObjects.size();
        for (Obj inputObject : inputObjects.values()) {
            processObject(inputObject, outputObjects);
            writeProgressStatus(++count, total, "objects");
        }
        
        if (showOutput)
        outputObjects.convertToImageIDColours().show();
        
        return Status.PASS;
        
    }
    
    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        
        addParameterDescriptions();
        
    }
    
    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        
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
        
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));
        
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
        parameters.get(INPUT_OBJECTS).setDescription(
        "Input objects to create 2D convex hulls for.  Each convex hull will be a child of its respective input object.");
        
        parameters.get(OUTPUT_OBJECTS).setDescription(
        "Output convex hull objects will be stored in the workspace with this name.  Each convex hull object will be a child of the input object it was created from.");
        
    }
}
