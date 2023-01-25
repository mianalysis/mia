package io.github.mianalysis.mia.module.objects.convert;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.detect.IdentifyObjects;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureObjectIntensity;
import io.github.mianalysis.mia.module.objects.relate.TrackObjects;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.exceptions.IntegerOverflowException;
import io.github.sjcross.sjcommon.mathfunc.CumStat;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

/**
 * Created by sc13967 on 04/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ConvertImageToObjects extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String VOLUME_TYPE = "Volume type";
    public static final String CREATE_TRACKS = "Create track objects";
    public static final String TRACK_OBJECTS_NAME = "Output track objects name";

    public ConvertImageToObjects(Modules modules) {
        super("Convert image to objects", modules);
    }

    public interface VolumeTypes extends VolumeTypesInterface {
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_CONVERT;
    }

    @Override
    public String getDescription() {
        return "Converts objects encoded in a labelled image stack back into objects.  Each output object is comprised of all pixels in a single timepoint with the same pixel intensity.  As such, pixels need not be in direct contact to be assigned the same object.  For objects tracked through time, the image intensity can be interpreted as the ID of a parent track object."

                + "<br><br>Note: This module has different behaviour to the \"" + new IdentifyObjects(null).getName()
                + "\" module, which takes a binary image, identifies contiguous foreground regions and assigns new object IDs.";
    }

    public static Objs createParents(Objs inputObjects, Image image, String parentsName) {
        Objs parentObjects = new Objs(parentsName, inputObjects);

        for (Obj inputObject : inputObjects.values()) {
            // Taking parent ID as the largest intensity within the object
            CumStat cs = MeasureObjectIntensity.measureIntensity(inputObject, image, false);
            int ID = (int) Math.round(cs.getMax());

            // Getting corresponding parent object
            parentObjects.putIfAbsent(ID, new Obj(parentObjects, VolumeType.POINTLIST, ID));
            Obj parentObject = parentObjects.get(ID);

            // Assigning relationships
            inputObject.addParent(parentObject);
            parentObject.addChild(inputObject);

        }

        return parentObjects;

    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);

        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
        String volumeType = parameters.getValue(VOLUME_TYPE,workspace);
        boolean createParents = parameters.getValue(CREATE_TRACKS,workspace);
        String parentObjectsName = parameters.getValue(TRACK_OBJECTS_NAME,workspace);

        Objs objects = null;
        try {
            objects = inputImage.convertImageToObjects(VolumeTypesInterface.getVolumeType(volumeType), outputObjectsName);
        } catch (IntegerOverflowException e) {
            return Status.FAIL;
        }

        if (createParents)
            workspace.addObjects(createParents(objects, inputImage, parentObjectsName));

        if (showOutput)
            if (createParents)
                TrackObjects.showObjects(objects, parentObjectsName);
            else
                objects.convertToImageIDColours().showImage();

        workspace.addObjects(objects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.POINTLIST, VolumeTypes.ALL));
        parameters.add(new BooleanP(CREATE_TRACKS, this, false));
        parameters.add(new OutputTrackObjectsP(TRACK_OBJECTS_NAME, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_IMAGE));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.get(VOLUME_TYPE));

        returnedParameters.add(parameters.get(CREATE_TRACKS));
        if ((boolean) parameters.getValue(CREATE_TRACKS,workspace))
            returnedParameters.add(parameters.get(TRACK_OBJECTS_NAME));

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

        if ((boolean) parameters.getValue(CREATE_TRACKS,workspace)) {
            String childObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);
            String parentObjectsName = parameters.getValue(TRACK_OBJECTS_NAME,workspace);

            returnedRelationships.add(parentChildRefs.getOrPut(parentObjectsName, childObjectsName));
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
        parameters.get(INPUT_IMAGE).setDescription(
                "Labelled image to convert to objects.  The background (non-object region) of this image should be black (0 intensity) and all pixels corresponding to the same object should have the same value.  Objects can only exist in a single timepoint, so pixels of the same intensity but in different timepoints will be assigned to different objects.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output objects created by the conversion process.  These will be stored in the workspace and be accessible via this name.");

        parameters.get(VOLUME_TYPE).setDescription(
                "The method used to store pixel coordinates.  This only affects performance and memory usage, there is no difference in results obtained using difference storage methods.<br><ul>"
                        + "<li>\"" + VolumeTypes.POINTLIST
                        + "\" (default) stores object coordinates as a list of XYZ coordinates.  This is most efficient for small objects, very thin objects or objects with lots of holes.</li>"
                        + "<li>\"" + VolumeTypes.OCTREE
                        + "\" stores objects in an octree format.  Here, the coordinate space is broken down into cubes of different sizes, each of which is marked as foreground (i.e. an object) or background.  Octrees are most efficient when there are lots of large cubic regions of the same label, as the space can be represented by larger (and thus fewer) cubes.  This is best used when there are large, completely solid objects.  If z-axis sampling is much larger than xy-axis sampling, it's typically best to opt for the quadtree method.</li>"
                        + "<li>\"" + VolumeTypes.QUADTREE
                        + "\" stores objects in a quadtree format.  Here, each Z-plane of the object is broken down into squares of different sizes, each of which is marked as foreground (i.e. an object) or background.  Quadtrees are most efficient when there are lots of large square regions of the same label, as the space can be represented by larger (and thus fewer) squares.  This is best used when there are large, completely solid objects.</li></ul>");

        parameters.get(CREATE_TRACKS).setDescription(
                "When selected, the intensity of the image for each object will be assumed as corresponding to the ID of a parent track object.  This allows track relationships for objects present in multiple timepoints to be loaded back into MIA.");

        parameters.get(TRACK_OBJECTS_NAME).setDescription("If creating track objects (\"" + CREATE_TRACKS
                + "\" is selected), this is the name of the output tracks.  These will be parents of the output objects.");
    }
}
