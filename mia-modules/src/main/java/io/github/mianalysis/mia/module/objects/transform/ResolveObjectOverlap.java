package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.process.threshold.ManualThreshold;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
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

    /**
    * 
    */
    public static final String OUTPUT_OBJECTS = "Output objects";

    public ResolveObjectOverlap(Modules modules) {
        super("Resolve object overlap", modules);
    }

    public static Objs process(Objs inputObjects) {
        // Creating overlap image
        ImagePlus overlap = IJ.createHyperStack("Overlap", inputObjects.getWidth(), inputObjects.getHeight(), 1,
                inputObjects.getNSlices(), inputObjects.getNFrames(), 8);

        ImageStack ist = overlap.getStack();
        for (Obj inputObject : inputObjects.values()) {
            for (Point<Integer> pt : inputObject.getCoordinateSet()) {
                ImageProcessor ipr = ist.getProcessor(overlap.getStackIndex(1, pt.getZ() + 1, inputObject.getT() + 1));
                ipr.putPixel(pt.x, pt.y, ipr.get(pt.x, pt.y) + 1);
            }
        }

        ManualThreshold.applyThreshold(overlap, 1);
        InvertIntensity.process(overlap);

        // Masking input objects
        for (Obj inputObject : inputObjects.values()) {
            Obj maskedObject = MaskObjects.maskObject(inputObject, (Image<?>) ImageFactory.createImage("Mask", overlap),
                    "Masked");
            inputObject.getCoordinateSet().clear();
            inputObject.setCoordinateSet(maskedObject.getCoordinateSet());
            inputObject.clearSurface();
            inputObject.clearCentroid();
            inputObject.clearProjected();
            inputObject.clearROIs();

        }

        return null;

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
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        Objs inputObjects = workspace.getObjects().get(inputObjectsName);
        Objs outputObjects = process(inputObjects);

        // workspace.addObjects(outputObjects);

        // // Showing objects
        // if (showOutput)
        //     outputObjects.convertToImageIDColours().show();

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
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS, workspace),
                parameters.getValue(OUTPUT_OBJECTS, workspace)));

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

    }
}
