// TODO: Could add optional argument to getParametersMatchingType for the removal type (i.e. if it matches type 1 add
// to the list, but if it matches type 2 remove the same parameter from the list.  Would need to compare Parameters for
// value.

package wbif.sjx.MIA.Object;

import wbif.sjx.MIA.GUI.InputOutput.InputControl;
import wbif.sjx.MIA.GUI.InputOutput.OutputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<Module> implements Serializable {
    private InputControl inputControl = new InputControl();
    private OutputControl outputControl = new OutputControl();

    public MeasurementRefCollection getImageMeasurementRefs(String imageName) {
        return getImageMeasurementRefs(imageName,null);
    }

    public MeasurementRefCollection getImageMeasurementRefs(String imageName, Module cutoffModule) {
        MeasurementRefCollection measurementRefs = new MeasurementRefCollection();

        addImageMeasurementRefs(inputControl, measurementRefs, imageName);
        addImageMeasurementRefs(outputControl, measurementRefs, imageName);

        // Iterating over all modules, collecting any measurements for the current image
        for (Module module:this) {
            if (module == cutoffModule) break;
            addImageMeasurementRefs(module, measurementRefs, imageName);
        }

        return measurementRefs;

    }

    void addImageMeasurementRefs(Module module, MeasurementRefCollection measurementRefs, String imageName) {
        if (!module.isEnabled()) return;
        MeasurementRefCollection currentMeasurementRefs = module.updateAndGetImageMeasurementRefs();

        if (currentMeasurementRefs == null) return;

        for (MeasurementRef measurementRef:currentMeasurementRefs.values()) {
            if (measurementRef.getImageObjName() == null) continue;
            if (measurementRef.getImageObjName().equals(imageName)
                    & measurementRef.isAvailable())
                measurementRefs.add(measurementRef);

        }
    }

    public MeasurementRefCollection getObjectMeasurementRefs(String objectName) {
        return getObjectMeasurementRefs(objectName,null);

    }

    public MeasurementRefCollection getObjectMeasurementRefs(String objectName, Module cutoffModule) {
        MeasurementRefCollection measurementRefs = new MeasurementRefCollection();

        // If this is a distant relative there will be "//" in the name that need to be removed
        if (objectName.contains("//")) objectName = objectName.substring(objectName.indexOf("//")+3);

        addObjectMeasurementRefs(inputControl,measurementRefs,objectName);
        addObjectMeasurementRefs(outputControl,measurementRefs,objectName);

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            addObjectMeasurementRefs(module,measurementRefs,objectName);
        }

        return measurementRefs;

    }

    void addObjectMeasurementRefs(Module module, MeasurementRefCollection measurementRefs, String objectName) {
        if (!module.isEnabled()) return;
        MeasurementRefCollection currentMeasurementRefs = module.updateAndGetObjectMeasurementRefs(this);
        if (currentMeasurementRefs == null) return;

        for (MeasurementRef measurementReference:currentMeasurementRefs.values()) {
            if (measurementReference.getImageObjName() == null) continue;
            if (measurementReference.getImageObjName().equals(objectName)
                    & measurementReference.isAvailable())
                measurementRefs.add(measurementReference);

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

    public RelationshipRefCollection getRelationshipRefs(Module cutoffModule) {
        RelationshipRefCollection relationships = new RelationshipRefCollection();

        for (Module module:this) {
            if (module == cutoffModule) {
                break;
            }

            if (module.isEnabled()) {
                RelationshipRefCollection currRelationships = module.updateAndGetRelationships();
                if (currRelationships == null) continue;
                relationships.addAll(currRelationships);
            }
        }

        return relationships;

    }

    public RelationshipRefCollection getRelationshipRefs() {
        return getRelationshipRefs(null);

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
            for (Parameter currParameter : currParameters) {
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
        // Getting a list of available images
        LinkedHashSet<OutputObjectsP> objects = getParametersMatchingType(OutputObjectsP.class,cutoffModule);

        // Remove any images without a name
        objects.removeIf(outputObjectsP -> outputObjectsP.getObjectsName().equals(""));

        if (!ignoreRemoved) return objects;

        // Removing any objects which have since been removed from the workspace
        LinkedHashSet<RemovedObjectsP> removedObjectParams = getParametersMatchingType(RemovedObjectsP.class,cutoffModule);
        for (Parameter removedObject: removedObjectParams) {
            String removeObjectName = removedObject.getValueAsString();
            objects.removeIf(outputImageP -> outputImageP.getObjectsName().equals(removeObjectName));
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
        // Getting a list of available images
        LinkedHashSet<OutputImageP> images = getParametersMatchingType(OutputImageP.class,cutoffModule);

        // Remove any images without a name
        images.removeIf(outputImageP -> outputImageP.getImageName().equals(""));

        if (!ignoreRemoved) return images;

        // Removing any objects which have since been removed from the workspace
        LinkedHashSet<RemovedImageP> removedImagePS = getParametersMatchingType(RemovedImageP.class,cutoffModule);
        for (Parameter removedImage: removedImagePS) {
            String removeImageName = removedImage.getValueAsString();
            images.removeIf(outputImageP -> outputImageP.getImageName().equals(removeImageName));
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
}
