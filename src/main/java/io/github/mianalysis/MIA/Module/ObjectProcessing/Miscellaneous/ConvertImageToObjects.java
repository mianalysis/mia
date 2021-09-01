// TODO: Add option to create parent objects based on intensity (i.e. follow objects through time)

package io.github.mianalysis.MIA.Module.ObjectProcessing.Miscellaneous;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.VolumeTypesInterface;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;
import io.github.sjcross.common.Exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ConvertImageToObjects extends Module {
    public static final String INPUT_SEPARATOR = "Image input/object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String VOLUME_TYPE = "Volume type";

    public ConvertImageToObjects(ModuleCollection modules) {
        super("Convert image to objects", modules);
    }

    public interface VolumeTypes extends VolumeTypesInterface {
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Converts objects encoded in a labelled image stack back into objects.  Each output object is comprised of all pixels in a single timepoint with the same pixel intensity.  As such, pixels need not be in direct contact to be assigned the same object.<br><br>Note: This is different behaviour to the \""+new IdentifyObjects(null).getName()+"\" module, which takes a binary image, identifies contiguous foreground regions and assigns new object IDs.";
    }

    @Override
    public Status process(Workspace workspace) {
            String inputImageName = parameters.getValue(INPUT_IMAGE);
            Image inputImage = workspace.getImages().get(inputImageName);

            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
            String volumeType = parameters.getValue(VOLUME_TYPE);

            ObjCollection objects = null;
            try {
                objects = inputImage.convertImageToObjects(volumeType, outputObjectsName);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }

            if (showOutput)
                objects.convertToImageRandomColours().showImage();

            workspace.addObjects(objects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.POINTLIST, VolumeTypes.ALL));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

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
        return null;
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
      parameters.get(INPUT_IMAGE).setDescription("Labelled image to convert to objects.  The background (non-object region) of this image should be black (0 intensity) and all pixels corresponding to the same object should have the same value.  Objects can only exist in a single timepoint, so pixels of the same intensity but in different timepoints will be assigned to different objects.");

      parameters.get(OUTPUT_OBJECTS).setDescription("Output objects created by the conversion process.  These will be stored in the workspace and be accessible via this name.");

      parameters.get(VOLUME_TYPE).setDescription("The method used to store pixel coordinates.  This only affects performance and memory usage, there is no difference in results obtained using difference storage methods.<br><ul>"
                      + "<li>\"" + VolumeTypes.POINTLIST
                      + "\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.</li>"
                      + "<li>\"" + VolumeTypes.OCTREE
                      + "\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.</li>"
                      + "<li>\"" + VolumeTypes.QUADTREE
                      + "\" stores objects in a quadtree format.  Here, each Z-plane of the object is broken down into squares of different sizes, each of which is marked as foreground (i.e. an object) or background.  Quadtrees are most efficient when there are lots of large square regions of the same label, as the space can be represented by larger (and thus fewer) squares.  This is best used when there are large, completely solid objects.</li></ul>");

    }
}
