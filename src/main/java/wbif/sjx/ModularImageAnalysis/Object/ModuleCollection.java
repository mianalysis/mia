package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<HCModule> implements Serializable {
    public MeasurementReferenceCollection getImageReferences(String imageName) {
        return getImageReferences(imageName,null);
    }

    public MeasurementReferenceCollection getImageReferences(String imageName, HCModule cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current image
        for (HCModule module:this) {
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

    public MeasurementReferenceCollection getObjectReferences(String objectName, HCModule cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current objects
        for (HCModule module:this) {
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
//    public LinkedHashMap<String,ImageObjReference> getImageReferences(HCModule cutoffModule) {
//        LinkedHashMap<String,ImageObjReference> superReferences = new LinkedHashMap<>();
//
//        for (HCModule module:this) {
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
//    public LinkedHashMap<String,ImageObjReference> getObjectReferences(HCModule cutoffModule) {
//        LinkedHashMap<String,ImageObjReference> superReferences = new LinkedHashMap<>();
//
//        for (HCModule module:this) {
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
    public LinkedHashSet<Parameter> getParametersMatchingType(int type, HCModule cutoffModule) {
        LinkedHashSet<Parameter> parameters = new LinkedHashSet<>();

        for (HCModule module:this) {
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

    public RelationshipCollection getRelationships(HCModule cutoffModule) {
        RelationshipCollection relationships = new RelationshipCollection();

        for (HCModule module:this) {
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
