// TODO: Normalised distance from centre to edge.  Will need to calculate line between the two and assign points on that line

package io.github.mianalysis.mia.module.objects.convert;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.NotNull;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Creates a distance map for a selected object set. Pixels in the output image
 * are encoded with the distance to the nearest image edge or centroid
 * (depending on setting). A single distance map image is created for all
 * objects in the specified set. Uses the plugin
 * "<a href="https://github.com/ijpb/MorphoLibJ">MorphoLibJ</a>".
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateClassImage extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Objects input / image output";

    public static final String ADD_OBJECTS = "Add objects";

    /**
     * Objects from workspace for which distance map will be created. A single
     * distance map will be created for all objects.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Objects from workspace for which distance map will be created. A single
     * distance map will be created for all objects.
     */
    public static final String CLASS_INDEX = "Class index";

    public static final String ALLOW_MISSING_OBJECTS = "Allow missing objects";

    /**
     * Output distance map image which will be added to the workspace. This will
     * contain the distance map for each object.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    public CreateClassImage(Modules modules) {
        super("Create class image", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_CONVERT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Creates a class image for the specific objects.  For each object, the index of that class is assigned to all pixels.";
    }

    static LinkedHashMap<ObjsI, Integer> getAvailableObjects(WorkspaceI workspace,
            LinkedHashMap<Integer, Parameters> collections) {
        LinkedHashMap<ObjsI, Integer> available = new LinkedHashMap<>();

        for (Parameters collection : collections.values()) {
            ObjsI image = workspace.getObjects(collection.getValue(INPUT_OBJECTS, workspace));
            if (image == null)
                continue;

            int classIndex = collection.getValue(CLASS_INDEX, workspace);
            available.put(image, classIndex);

        }

        return available;

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        // Getting input objects
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_OBJECTS, workspace);
        LinkedHashMap<ObjsI, Integer> inputObjects = getAvailableObjects(workspace, collections);

        // Adding first objects to image
        ImageI outputImage = null;
        for (ObjsI currInputObjects:inputObjects.keySet()) {
            int classIndex = inputObjects.get(currInputObjects);

            if (outputImage == null) {
                ImagePlus ipl = IJ.createHyperStack(outputImageName, currInputObjects.getWidth(), currInputObjects.getHeight(), 1, currInputObjects.getNSlices(), currInputObjects.getNFrames(), 32);
                outputImage = ImageFactory.createImage(outputImageName, ipl);
            }

            for (ObjI inputObject:currInputObjects.values())
                inputObject.addToImage(outputImage, classIndex);
            
        }        
        
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.showAsIs();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new CustomInputObjectsP(INPUT_OBJECTS, this));
        collection.add(new IntegerP(CLASS_INDEX, this, 1));
        parameters.add(new ParameterGroup(ADD_OBJECTS, this, collection, 1, "Add another object set."));
        parameters.add(new BooleanP(ALLOW_MISSING_OBJECTS, this, false,
                "If enabled, the moduule can ignore any objects specified for inclusion that aren't present in the workspace.  This is useful if an object's existence is dependent on optional modules."));

        parameters.add(
                new OutputImageP(OUTPUT_IMAGE, this, "", "The resultant class image to be added to the workspace."));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;

        boolean allowMissingObjects = parameters.getValue(ALLOW_MISSING_OBJECTS, workspace);

        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_OBJECTS, workspace);
        for (Parameters collection : collections.values()) {
            CustomInputObjectsP parameter = collection.getParameter(INPUT_OBJECTS);
            parameter.setAllowMissingObjects(allowMissingObjects);

        }

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

    // Creating a custom class for this module, which always returns true. This way
    // channels can go missing and this will still work.
    class CustomInputObjectsP extends InputObjectsP {
        private boolean allowMissingObjects = false;

        private CustomInputObjectsP(String name, Module module) {
            super(name, module);

        }

        public CustomInputObjectsP(String name, Module module, @NotNull String choice) {
            super(name, module, choice);
        }

        public CustomInputObjectsP(String name, Module module, @NotNull String choice, String description) {
            super(name, module, choice, description);
        }

        @Override
        public boolean verify() {
            if (allowMissingObjects)
                return true;
            else
                return super.verify();
        }

        @Override
        public boolean isValid() {
            if (allowMissingObjects)
                return true;
            else
                return super.isValid();
        }

        @Override
        public <T extends Parameter> T duplicate(Module newModule) {
            CustomInputObjectsP newParameter = new CustomInputObjectsP(name, module, getRawStringValue(),
                    getDescription());

            newParameter.setNickname(getNickname());
            newParameter.setVisible(isVisible());
            newParameter.setExported(isExported());
            newParameter.setAllowMissingObjects(allowMissingObjects);

            return (T) newParameter;

        }

        public boolean isAllowMissingObjects() {
            return allowMissingObjects;
        }

        public void setAllowMissingObjects(boolean allowMissingObjects) {
            this.allowMissingObjects = allowMissingObjects;
        }
    }
}
