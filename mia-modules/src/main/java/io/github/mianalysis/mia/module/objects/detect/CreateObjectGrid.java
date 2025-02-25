package io.github.mianalysis.mia.module.objects.detect;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 31/10/2024.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateObjectGrid extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/object output";

    /**
     * 
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * 
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String GRID_SEPARATOR = "Grid settings";

    /**
     * 
     */
    public static final String CELL_WIDTH = "Cell width (px)";

    /**
     * 
     */
    public static final String CELL_HEIGHT = "Cell height (px)";

    public CreateObjectGrid(Modules modules) {
        super("Create object grid", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        int cellW = parameters.getValue(CELL_WIDTH, workspace);
        int cellH = parameters.getValue(CELL_HEIGHT, workspace);

        // Creating output objects
        Image inputImage = workspace.getImages().get(inputImageName);
        Objs outputObjects = new Objs(outputObjectsName, inputImage.getImagePlus());

        double imageW = inputImage.getWidth();
        double imageH = inputImage.getHeight();
        int nCols = (int) Math.ceil(imageW / (double) cellW);
        int nRows = (int) Math.ceil(imageH / (double) cellH);

        for (int idxX = 0; idxX < nCols; idxX++) {
            for (int idxY = 0; idxY < nRows; idxY++) {
                Obj outputObj = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
                for (int x = 0; x < cellW; x++) {
                    for (int y = 0; y < cellH; y++) {
                        try {
                            outputObj.add(x + idxX * cellW, y + idxY * cellH, 0);
                        } catch (PointOutOfRangeException e) {
                            // Some may be out of range at the edge
                        }
                    }
                }
            }
        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(GRID_SEPARATOR, this));
        parameters.add(new IntegerP(CELL_WIDTH, this, 10));
        parameters.add(new IntegerP(CELL_HEIGHT, this, 10));

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
}
