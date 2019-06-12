// TODO: Could addRef optional argument to getParametersMatchingType for the removal type (i.e. if it matches type 1 addRef
// to the list, but if it matches type 2 remove the same parameter from the list.  Would need to compare Parameters for
// value.

package wbif.sjx.MIA.Object;

import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<Module> implements RefCollection<Module> {
    private InputControl inputControl = new InputControl(this);
    private OutputControl outputControl = new OutputControl(this);

    public ImageMeasurementRefCollection getImageMeasurementRefs(String imageName) {
        return getImageMeasurementRefs(imageName,null);
    }

    public ImageMeasurementRefCollection getImageMeasurementRefs(String imageName, Module cutoffModule) {
        ImageMeasurementRefCollection measurementRefs = new ImageMeasurementRefCollection();

        addImageMeasurementRefs(inputControl, measurementRefs, imageName);
        addImageMeasurementRefs(outputControl, measurementRefs, imageName);

        // Iterating over all modules, collecting any measurements for the current image
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled() |! module.isRunnable()) continue;
            addImageMeasurementRefs(module, measurementRefs, imageName);
        }

        return measurementRefs;

    }

    void addImageMeasurementRefs(Module module, ImageMeasurementRefCollection measurementRefs, String imageName) {
        if (!module.isEnabled()) return;
        ImageMeasurementRefCollection currentMeasurementRefs = module.updateAndGetImageMeasurementRefs();

        if (currentMeasurementRefs == null) return;

        for (ImageMeasurementRef measurementRef:currentMeasurementRefs.values()) {
            if (measurementRef.getImageName() == null) continue;
            if (measurementRef.getImageName().equals(imageName))
                measurementRefs.put(measurementRef.getName(),measurementRef);

        }
    }

    public ObjMeasurementRefCollection getObjectMeasurementRefs(String objectName) {
        return getObjectMeasurementRefs(objectName,null);

    }

    public ObjMeasurementRefCollection getObjectMeasurementRefs(String objectName, Module cutoffModule) {
        ObjMeasurementRefCollection measurementRefs = new ObjMeasurementRefCollection();

        // If this is a distant relative there will be "//" in the name that need to be removed
        if (objectName.contains("//")) objectName = objectName.substring(objectName.indexOf("//")+3);

        addObjectMeasurementRefs(inputControl,measurementRefs,objectName);
        addObjectMeasurementRefs(outputControl,measurementRefs,objectName);

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled() |! module.isRunnable()) continue;
            addObjectMeasurementRefs(module,measurementRefs,objectName);
        }

        return measurementRefs;

    }

    void addObjectMeasurementRefs(Module module, ObjMeasurementRefCollection measurementRefs, String objectName) {
        if (!module.isEnabled()) return;
        ObjMeasurementRefCollection currentMeasurementRefs = module.updateAndGetObjectMeasurementRefs();
        if (currentMeasurementRefs == null) return;

        for (ObjMeasurementRef ref:currentMeasurementRefs.values()) {
            if (ref.getObjectsName() == null) continue;
            if (ref.getObjectsName().equals(objectName)) measurementRefs.put(ref.getName(),ref);

        }
    }

    public MetadataRefCollection getMetadataRefs() {
        return getMetadataRefs(null);

    }

    public MetadataRefCollection getMetadataRefs(Module cutoffModule) {
        MetadataRefCollection metadataRefs = new MetadataRefCollection();

        addMetadataRefs(inputControl, metadataRefs);
        addMetadataRefs(outputControl, metadataRefs);

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            addMetadataRefs(module, metadataRefs);
        }

        return metadataRefs;

    }

    void addMetadataRefs(Module module, MetadataRefCollection metadataRefs) {
        if (!module.isEnabled()) return;

        MetadataRefCollection currentMetadataReferences = module.updateAndGetMetadataReferences();
        if (currentMetadataReferences == null) return;

        metadataRefs.putAll(currentMetadataReferences);

    }

    public RelationshipRefCollection getRelationshipRefs() {
        return getRelationshipRefs(null);

    }

    public RelationshipRefCollection getRelationshipRefs(Module cutoffModule) {
        RelationshipRefCollection relationshipRefs = new RelationshipRefCollection();

        addRelationshipRefs(inputControl, relationshipRefs);
        addRelationshipRefs(outputControl, relationshipRefs);

        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled() |! module.isRunnable()) continue;

            addRelationshipRefs(module,relationshipRefs);
        }

        return relationshipRefs;

    }

    void addRelationshipRefs(Module module, RelationshipRefCollection relationshipRefs) {
        if (!module.isEnabled()) return;

        RelationshipRefCollection currentRelationshipRefs = module.updateAndGetRelationships();
        if (currentRelationshipRefs == null) return;

        relationshipRefs.putAll(currentRelationshipRefs);

    }

    /*
     * Returns an LinkedHashSet of all parameters of a specific type
     */
    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type, Module cutoffModule) {
        LinkedHashSet<T> parameters = new LinkedHashSet<>();

        for (Module module:this) {
            // If the current module is the cutoff the loop terminates.  This prevents the system offering measurements
            // that are created after this module or are currently unavailable.
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            if (!module.isRunnable()) continue;

            // Running through all parameters, adding all images to the list
            ParameterCollection currParameters = module.updateAndGetParameters();
            for (Parameter currParameter : currParameters.values()) {
                if (type.isInstance(currParameter)) {
                    parameters.add((T) currParameter);
                }
            }
        }

        return parameters;

    }

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type) {
        return getParametersMatchingType(type,null);
    }

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(Module cutoffModule, boolean ignoreRemoved) {
        LinkedHashSet<OutputObjectsP> objects = new LinkedHashSet<>();

        for (Module module:this) {
            if (module == cutoffModule) break;

            // Get the added and removed images
            LinkedHashSet<OutputObjectsP> addedObjects = module.getParametersMatchingType(OutputObjectsP.class);
            LinkedHashSet<RemovedObjectsP> removedObjects = module.getParametersMatchingType(RemovedObjectsP.class);

            // Adding new images
            if (addedObjects != null) objects.addAll(addedObjects);

            // Removing images
            if (!ignoreRemoved || removedObjects == null) continue;

            for (Parameter removedImage: removedObjects) {
                String removeImageName = removedImage.getRawStringValue();
                objects.removeIf(outputImageP -> outputImageP.getObjectsName().equals(removeImageName));
            }
        }

        return objects;

    }

    public LinkedHashSet<OutputObjectsP> getAvailableObjects(Module cutoffModule) {
        return getAvailableObjects(cutoffModule,true);
    }

    public LinkedHashSet<OutputImageP> getAvailableImages(Module cutoffModule) {
        return getAvailableImages(cutoffModule,true);
    }

    public LinkedHashSet<OutputImageP> getAvailableImages(Module cutoffModule, boolean ignoreRemoved) {
        LinkedHashSet<OutputImageP> images = new LinkedHashSet<>();

        for (Module module:this) {
            if (module == cutoffModule) break;

            // Get the added and removed images
            LinkedHashSet<OutputImageP> addedImages = module.getParametersMatchingType(OutputImageP.class);
            LinkedHashSet<RemovedImageP> removedImages = module.getParametersMatchingType(RemovedImageP.class);

            // Adding new images
            if (addedImages != null) images.addAll(addedImages);

            // Removing images
            if (!ignoreRemoved || removedImages == null) continue;

            for (Parameter removedImage:removedImages) {
                String removeImageName = removedImage.getRawStringValue();
                images.removeIf(outputImageP -> outputImageP.getImageName().equals(removeImageName));
            }
        }

        return images;

    }

    public boolean hasVisibleParameters() {
        if (inputControl.hasVisibleParameters()) return true;
        if (outputControl.hasVisibleParameters()) return true;

        for (Module module:this) {
            if (module.hasVisibleParameters()) return true;
        }

        return false;

    }

    public InputControl getInputControl() {
        return inputControl;
    }

    public void setInputControl(InputControl inputControl) {
        this.inputControl = inputControl;
    }

    public OutputControl getOutputControl() {
        return outputControl;
    }

    public void setOutputControl(OutputControl outputControl) {
        this.outputControl = outputControl;
    }

    @Override
    public Collection<Module> values() {
        return this;
    }
}
