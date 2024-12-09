package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.objects.process.GrowObjects;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ResolveObjectOverlap extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input/output";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";


    public ResolveObjectOverlap(Modules modules) {
        super("Resolve object overlap", modules);
    }

    public static void process(Objs inputObjects, Workspace workspace) {
        if (inputObjects == null || inputObjects.size() == 0)
            return;

        Image finalMask = inputObjects.convertToImageBinary();

        // Creating overlap image
        ImagePlus overlap = IJ.createHyperStack("Overlap", inputObjects.getWidth(), inputObjects.getHeight(), 1,
                inputObjects.getNSlices(), inputObjects.getNFrames(), 8);
        ImageMath.process(overlap, ImageMath.CalculationModes.ADD, 255);

        for (Obj inputObject : inputObjects.values()) {
            for (Obj testObject : inputObjects.values()) {
                if (inputObject == testObject)
                    continue;

                if (inputObject.getT() != testObject.getT())
                    continue;

                for (Point<Integer> pt : inputObject.getCoordinateSet())
                    if (testObject.getCoordinateSet().contains(pt)) {
                        overlap.setPosition(1, pt.z+1, inputObject.getT()+1);
                        overlap.getProcessor().putPixel(pt.x, pt.y, 0);
                    }
            }
        }

        // Masking input objects
        Image maskImage = ImageFactory.createImage("Mask", overlap);
        MaskObjects.maskObjects(inputObjects, maskImage, null, true, false);
  
        // Growing input objects into overlap regions
        String startingObjectMode = GrowObjects.StartingObjectModes.SURFACES;
        String growthMode = GrowObjects.GrowthModes.EQUIDISTANT_FROM_OBJECTS;
        int connectivity = Integer.parseInt(GrowObjects.Connectivity.TWENTYSIX);
        GrowObjects.process(inputObjects, null, startingObjectMode, growthMode, null, null, true, connectivity, false,
                workspace);
  
                // Masking to original objects
        MaskObjects.maskObjects(inputObjects, finalMask, null, true, false);
      
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);
        process(inputObjects, workspace);

        // // Showing objects
        if (showOutput)
            inputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
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
        return null;
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
