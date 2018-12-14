// TODO: Could add optional argument to getParametersMatchingType for the removal type (i.e. if it matches type 1 add
// to the list, but if it matches type 2 remove the same parameter from the list.  Would need to compare Parameters for
// value.

package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<Module> implements Serializable {
    public MeasurementReferenceCollection getImageMeasurementReferences(String imageName) {
        return getImageMeasurementReferences(imageName,null);
    }

    public MeasurementReferenceCollection getImageMeasurementReferences(String imageName, Module cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current image
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MeasurementReferenceCollection currentMeasurementReferences = module.updateAndGetImageMeasurementReferences();

            if (currentMeasurementReferences == null) continue;

            for (MeasurementReference measurementReference:currentMeasurementReferences.values()) {
                if (measurementReference.getImageObjName() == null) continue;
                if (measurementReference.getImageObjName().equals(imageName)
                        & measurementReference.isCalculated())
                    measurementReferences.add(measurementReference);

            }
        }

        return measurementReferences;

    }

    public MeasurementReferenceCollection getObjectMeasurementReferences(String objectName) {
        return getObjectMeasurementReferences(objectName,null);

    }

    public MeasurementReferenceCollection getObjectMeasurementReferences(String objectName, Module cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MeasurementReferenceCollection currentMeasurementReferences =
                    module.updateAndGetObjectMeasurementReferences();
            if (currentMeasurementReferences == null) continue;

            for (MeasurementReference measurementReference:currentMeasurementReferences.values()) {
                if (measurementReference.getImageObjName() == null) continue;
                if (measurementReference.getImageObjName().equals(objectName)
                        & measurementReference.isCalculated())
                    measurementReferences.add(measurementReference);

            }
        }

        return measurementReferences;

    }

    public MetadataReferenceCollection getMetadataReferences(Module cutoffModule) {
        MetadataReferenceCollection metadataReferences = new MetadataReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MetadataReferenceCollection currentMetadataReferences = module.updateAndGetMetadataReferences();
            if (currentMetadataReferences == null) continue;

            metadataReferences.putAll(currentMetadataReferences);

        }

        return metadataReferences;

    }

    /*
     * Returns an ArrayList of all parameters of a specific type
     */
    public LinkedHashSet<Parameter> getParametersMatchingType(int type, Module cutoffModule) {
        LinkedHashSet<Parameter> parameters = new LinkedHashSet<>();

        for (Module module:this) {
            // If the current module is the cutoff the loop terminates.  This prevents the system offering measurements
            // that are created after this module or are currently unavailable.
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            if (!module.isRunnable()) continue;

            // Running through all parameters, adding all images to the list
            ParameterCollection currParameters = module.updateAndGetParameters();
            if (currParameters != null) {
                for (Parameter currParameter : currParameters.values()) {
                    if (currParameter.getType() == type) {
                        parameters.add(currParameter);
                    }
                }
            }
        }

        return parameters;

    }

    public LinkedHashSet<Parameter> getParametersMatchingType(int type) {
        return getParametersMatchingType(type,null);

    }

    public LinkedHashSet<Parameter> getAvailableObjects(Module cutoffModule, boolean ignoreRemoved) {
        // Getting a list of available images
        LinkedHashSet<Parameter> objects = getParametersMatchingType(Parameter.OUTPUT_OBJECTS,cutoffModule);

        if (!ignoreRemoved) return objects;

        // Removing any objects which have since been removed from the workspace
        LinkedHashSet<Parameter> removedObjects = getParametersMatchingType(Parameter.REMOVED_OBJECTS,cutoffModule);
        Iterator<Parameter> iterator = objects.iterator();
        while (iterator.hasNext()) {
            Parameter object = iterator.next();
            for (Parameter removedObject : removedObjects) {
                if (object.getValue().equals(removedObject.getValue())) iterator.remove();
            }
        }

        return objects;

    }

    public LinkedHashSet<Parameter> getAvailableObjects(Module cutoffModule) {
        return getAvailableObjects(cutoffModule,true);
    }

    public LinkedHashSet<Parameter> getAvailableImages(Module cutoffModule) {
        return getAvailableImages(cutoffModule,true);
    }

    public LinkedHashSet<Parameter> getAvailableImages(Module cutoffModule, boolean ignoreRemoved) {
        // Getting a list of available images
        LinkedHashSet<Parameter> images = getParametersMatchingType(Parameter.OUTPUT_IMAGE,cutoffModule);

        if (!ignoreRemoved) return images;

        // Removing any objects which have since been removed from the workspace
        LinkedHashSet<Parameter> removedImages = getParametersMatchingType(Parameter.REMOVED_IMAGE,cutoffModule);
        Iterator<Parameter> iterator = images.iterator();
        while (iterator.hasNext()) {
            Parameter image = iterator.next();
            for (Parameter removedImage : removedImages) {
                if (image.getValue().equals(removedImage.getValue())) iterator.remove();
            }
        }

        return images;

    }

    public RelationshipCollection getRelationships(Module cutoffModule) {
        RelationshipCollection relationships = new RelationshipCollection();

        for (Module module:this) {
            if (module == cutoffModule) {
                break;
            }

            if (module.isEnabled()) module.addRelationships(relationships);

        }

        return relationships;

    }

    public RelationshipCollection getRelationships() {
        return getRelationships(null);

    }

}
