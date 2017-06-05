package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class HCModuleCollection extends ArrayList<HCModule> implements Serializable {
    public HCMeasurementCollection getMeasurements(HCModule cutoffModule) {
        HCMeasurementCollection measurements = new HCMeasurementCollection();

        for (HCModule module:this) {
            if (module == cutoffModule) {
                break;
            }

            module.addMeasurements(measurements);

        }

        return measurements;

    }

    public HCMeasurementCollection getMeasurements() {
        return getMeasurements(null);

    }

    /**
     * Returns an ArrayList of all parameters of a specific type
     * @param type
     * @param cutoffModule
     * @return
     */
    public ArrayList<HCParameter> getParametersMatchingType(int type, HCModule cutoffModule) {
        ArrayList<HCParameter> parameters = new ArrayList<>();

        for (HCModule module:this) {
            // If the current module is the cutoff the loop terminates.  This prevents the system offering measurements
            // that are created after this module
            if (module == cutoffModule) {
                break;
            }

            // Running through all parameters, adding all images to the list
            HCParameterCollection currParameters = module.getActiveParameters();
            if (currParameters != null) {
                for (HCParameter currParameter : currParameters.getParameters().values()) {
                    if (currParameter.getType() == type) {
                        parameters.add(currParameter);
                    }
                }
            }
        }

        return parameters;

    }

    public ArrayList<HCParameter> getParametersMatchingType(int type) {
        return getParametersMatchingType(type,null);

    }

    public HCRelationshipCollection getRelationships(HCModule cutoffModule) {
        HCRelationshipCollection relationships = new HCRelationshipCollection();

        for (HCModule module:this) {
            if (module == cutoffModule) {
                break;
            }

            module.addRelationships(relationships);

        }

        return relationships;

    }

    public HCRelationshipCollection getRelationships() {
        return getRelationships(null);

    }
}
