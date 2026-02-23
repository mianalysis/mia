
package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.images.configure.SetLookupTable;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.imagej.ImageTiler;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TileStack<T extends RealType<T> & NativeType<T>> extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
     * .
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Name of the output image created during the cropping process if storing the
     * cropped image as a new image in the workspace ("Apply to input image"
     * parameter).
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String TILE_SEPARATOR = "Tile selection";

    /**
    * 
    */
    public static final String X_NUM_TILES = "x-axis number of tiles";

    /**
    * 
    */
    public static final String Y_NUM_TILES = "y-axis number of tiles";

    /**
    * 
    */
    public static final String X_OVERLAP_PX = "x-axis overlap";

    /**
    * 
    */
    public static final String Y_OVERLAP_PX = "y-axis overlap";

    /**
    * 
    */
    public static final String TILE_AXIS = "Tile axis";

    public interface TileAxes extends ImageTiler.TileAxes {
    };

    public TileStack(ModulesI modules) {
        super("Tile stack", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
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
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        int xNumTiles = parameters.getValue(X_NUM_TILES, workspace);
        int yNumTiles = parameters.getValue(Y_NUM_TILES, workspace);
        int xOverlapPx = parameters.getValue(X_OVERLAP_PX, workspace);
        int yOverlapPx = parameters.getValue(Y_OVERLAP_PX, workspace);
        String tileAxis = parameters.getValue(TILE_AXIS, workspace);

        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        ImagePlus outputIpl = ImageTiler.tile(inputIpl, xNumTiles, yNumTiles, xOverlapPx, yOverlapPx, tileAxis);
        outputIpl.setTitle(outputImageName);
        outputIpl = outputIpl.duplicate();
        
        ImageI outputImage = ImageFactories.getDefaultFactory().create(outputImageName, outputIpl);
        SetLookupTable.copyLUTFromImage(outputImage, inputImage);

        workspace.addImage(outputImage);
        if (showOutput)
            outputImage.showAsIs();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(TILE_SEPARATOR, this));
        parameters.add(new IntegerP(X_NUM_TILES, this, 4));
        parameters.add(new IntegerP(Y_NUM_TILES, this, 4));
        parameters.add(new IntegerP(X_OVERLAP_PX, this, 32));
        parameters.add(new IntegerP(Y_OVERLAP_PX, this, 32));
        parameters.add(new ChoiceP(TILE_AXIS, this, TileAxes.Z, TileAxes.ALL));

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
