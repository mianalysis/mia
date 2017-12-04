package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<HCModule> implements Serializable {
    public HashMap<String,Reference> getImageReferences() {
        return getImageReferences(null);
    }

    public HashMap<String,Reference> getImageReferences(HCModule cutoffModule) {
        HashMap<String,Reference> superReferences = new HashMap<>();

        for (HCModule module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;

            // Getting references from the current module, then adding them to the super references
            ReferenceCollection currentReferences = module.updateAndGetImageReferences();
            if (currentReferences == null) continue;

            for (Reference currentReference:currentReferences) {
                superReferences.put(currentReference.getName(),new Reference());

                superReferences.get(currentReference.getName()).addMeasurementReferences(currentReference);

            }
        }

        System.out.println(superReferences.size());

        return superReferences;

    }

    public HashMap<String,Reference> getObjectReferences() {
        return getObjectReferences(null);
    }

    public HashMap<String,Reference> getObjectReferences(HCModule cutoffModule) {
        HashMap<String,Reference> superReferences = new HashMap<>();

        for (HCModule module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;

            // Getting references from the current module, then adding them to the super references
            ReferenceCollection currentReferences = module.updateAndGetObjectReferences();
            for (Reference currentReference:currentReferences) {
                superReferences.put(currentReference.getName(),new Reference());

                superReferences.get(currentReference.getName()).addMeasurementReferences(currentReference);

            }
        }

        return superReferences;

    }

    public MeasurementCollection getMeasurements(HCModule cutoffModule) {
        MeasurementCollection measurements = new MeasurementCollection();

        for (HCModule module:this) {
            if (module == cutoffModule) {
                break;
            }

            if (module.isEnabled()) module.addMeasurements(measurements);

        }

        return measurements;

    }

    public MeasurementCollection getMeasurements() {
        return getMeasurements(null);

    }

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
            ParameterCollection currParameters = module.getActiveParameters();
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
