package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<Module> implements Serializable {
    public MeasurementReferenceCollection getImageReferences(String imageName) {
        return getImageReferences(imageName,null);
    }

    public MeasurementReferenceCollection getImageReferences(String imageName, Module cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current image
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MeasurementReferenceCollection currentMeasurementReferences = module.updateAndGetImageMeasurementReferences();

            if (currentMeasurementReferences == null) continue;

            for (MeasurementReference measurementReference:currentMeasurementReferences) {
                if (measurementReference.getImageObjName().equals(imageName)
                        & measurementReference.isCalculated())
                    measurementReferences.add(measurementReference);

            }
        }

        return measurementReferences;

    }

    public MeasurementReferenceCollection getObjectReferences(String objectName) {
        return getObjectReferences(objectName,null);

    }

    public MeasurementReferenceCollection getObjectReferences(String objectName, Module cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MeasurementReferenceCollection currentMeasurementReferences =
                    module.updateAndGetObjectMeasurementReferences();
            if (currentMeasurementReferences == null) continue;

            for (MeasurementReference measurementReference:currentMeasurementReferences) {
                if (measurementReference.getImageObjName().equals(objectName)
                        & measurementReference.isCalculated())
                    measurementReferences.add(measurementReference);

            }
        }

        return measurementReferences;

    }

//    public LinkedHashMap<String,ImageObjReference> getImageReferences() {
//        return getImageReferences(null);
//    }
//
//    public LinkedHashMap<String,ImageObjReference> getImageReferences(Module cutoffModule) {
//        LinkedHashMap<String,ImageObjReference> superReferences = new LinkedHashMap<>();
//
//        for (Module module:this) {
//            if (module == cutoffModule) break;
//            if (!module.isEnabled()) continue;
//
//            // Getting references from the current module, then adding them to the super references
//            ReferenceCollection currentReferences = module.updateAndGetImageReferences();
//            if (currentReferences == null) continue;
//
//            for (ImageObjReference currentImageObjReference :currentReferences) {
//                superReferences.putIfAbsent(currentImageObjReference.getName(),new ImageObjReference());
//
//                superReferences.get(currentImageObjReference.getName()).addMeasurementReferences(currentImageObjReference);
//
//            }
//        }
//
//        return superReferences;
//
//    }
//
//    public LinkedHashMap<String,ImageObjReference> getObjectReferences() {
//        return getObjectReferences(null);
//    }
//
//    public LinkedHashMap<String,ImageObjReference> getObjectReferences(Module cutoffModule) {
//        LinkedHashMap<String,ImageObjReference> superReferences = new LinkedHashMap<>();
//
//        for (Module module:this) {
//            if (module == cutoffModule) break;
//            if (!module.isEnabled()) continue;
//
//            // Getting references from the current module, then adding them to the super references
//            ReferenceCollection currentReferences = module.updateAndGetObjectReferences();
//            if (currentReferences == null) continue;
//
//            for (ImageObjReference currentImageObjReference :currentReferences) {
//                superReferences.putIfAbsent(currentImageObjReference.getName(),new ImageObjReference());
//
//                superReferences.get(currentImageObjReference.getName()).addMeasurementReferences(currentImageObjReference);
//
//            }
//        }
//
//        return superReferences;
//
//    }

    /**
     * Returns an ArrayList of all parameters of a specific type
     * @param type
     * @param cutoffModule
     * @return
     */
    public LinkedHashSet<Parameter> getParametersMatchingType(int type, Module cutoffModule) {
        LinkedHashSet<Parameter> parameters = new LinkedHashSet<>();

        for (Module module:this) {
            // If this module isn't enabled, skip it
            if (!module.isEnabled()) continue;

            // If the current module is the cutoff the loop terminates.  This prevents the system offering measurements
            // that are created after this module
            if (module == cutoffModule) {
                break;
            }

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
