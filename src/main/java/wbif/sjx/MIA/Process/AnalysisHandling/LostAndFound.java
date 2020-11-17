package wbif.sjx.MIA.Process.AnalysisHandling;

import java.util.HashMap;

import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.SIFTRegistration;
import wbif.sjx.MIA.Module.InputOutput.ObjectLoader;
import wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous.ReplaceMeasurementValue;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.CalculateNearestNeighbour;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.ExpandShrinkObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Relationships.RelateManyToOne;
import wbif.sjx.MIA.Module.WorkflowHandling.WorkflowHandling;

public class LostAndFound {
    private HashMap<String, String> lostModules = new HashMap<>();
    private HashMap<String, HashMap<String, String>> lostParameters = new HashMap<>();


    public LostAndFound() {
        //// Populating hard-coded module reassignments ////
        lostModules.put("ConditionalAnalysisTermination", new WorkflowHandling(null).getClass().getSimpleName());
        lostModules.put("AutomaticRegistration", new SIFTRegistration(null).getClass().getSimpleName());

        //// Populating hard-coded parameter reassignments ////
        // CalculateNearestNeighbour
        HashMap<String, String> currentParameters = new HashMap<>();
        currentParameters.put("ParentChildRef mode", CalculateNearestNeighbour.RELATIONSHIP_MODE);
        String moduleName = new CalculateNearestNeighbour(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

        // ExpandShrinkObjects
        currentParameters = new HashMap<>();
        currentParameters.put("Radius change (px)", ExpandShrinkObjects.RADIUS_CHANGE);
        moduleName = new ExpandShrinkObjects(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

        // GetObjectLocalRegion
        currentParameters = new HashMap<>();
        currentParameters.put("Local radius", GetLocalObjectRegion.FIXED_VALUE);
        currentParameters.put("Measurement name", GetLocalObjectRegion.RADIUS_MEASUREMENT);
        currentParameters.put("Calibrated radius", GetLocalObjectRegion.CALIBRATED_UNITS);
        moduleName = new GetLocalObjectRegion(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

        // ObjectLoader
        currentParameters = new HashMap<>();
        currentParameters.put("Output parent clusters name", ObjectLoader.PARENT_OBJECTS_NAME);
        currentParameters.put("Output tracks clusters name", ObjectLoader.PARENT_OBJECTS_NAME);
        moduleName = new ObjectLoader(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

        // RelateManyToOne
        currentParameters = new HashMap<>();
        currentParameters.put("Reference point", RelateManyToOne.REFERENCE_MODE);
        moduleName = new RelateManyToOne(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

        // ReplaceMeasurementValue
        currentParameters = new HashMap<>();
        currentParameters.put("Value to replace", ReplaceMeasurementValue.REFERENCE_VALUE);
        moduleName = new ReplaceMeasurementValue(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

        // WorkflowHandling
        currentParameters = new HashMap<>();
        currentParameters.put("Reference image measurement mode", WorkflowHandling.NUMERIC_FILTER_MODE);
        currentParameters.put("Reference value", WorkflowHandling.REFERENCE_NUMERIC_VALUE);
        moduleName = new WorkflowHandling(null).getClass().getSimpleName();
        lostParameters.put(moduleName, currentParameters);

    }

    public String findModule(String oldName) {
        String newName = lostModules.get(oldName);

        // If this module isn't in the lost and found, its new and old names should be
        // the same
        if (newName == null)
            newName = oldName;

        if (!newName.equals(oldName))
            newName = findModule(newName);

        return newName;

    }

    public String findParameter(String moduleName, String oldName) {
        // If no name is found, return the old name
        HashMap<String, String> currentParameters = lostParameters.get(moduleName);
        if (currentParameters == null)
            return oldName;

        String newName = currentParameters.get(oldName);
        if (newName == null)
            newName = oldName;

        return newName;

    }

    public void addLostModuleAssignment(String oldName, String newName) {
        lostModules.put(oldName, newName);

    }

    public void addLostParameterAssignment(String moduleName, String oldName, String newName) {
        HashMap<String, String> currentParameters = lostParameters.putIfAbsent(moduleName, new HashMap<>());
        currentParameters.put(oldName, newName);

    }
}
