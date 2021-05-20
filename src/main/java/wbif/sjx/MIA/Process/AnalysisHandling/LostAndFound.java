package wbif.sjx.MIA.Process.AnalysisHandling;

import java.util.HashMap;

import wbif.sjx.MIA.Module.Core.InputControl;
import wbif.sjx.MIA.Module.Deprecated.ThresholdImage;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureIntensityDistribution;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.WekaProbabilityMaps;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Threshold.LocalAutoThreshold;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.AffineBlockMatching;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.AffineMOPS;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.AffineManual;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.AffineSIFT;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.UnwarpAutomatic;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.UnwarpManual;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.Registration.Abstract.AbstractAffineRegistration;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.InputOutput.ObjectLoader;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
import wbif.sjx.MIA.Module.Miscellaneous.Macros.RunMacro;
import wbif.sjx.MIA.Module.Miscellaneous.Macros.RunSingleCommand;
import wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous.ReplaceMeasurementValue;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.CalculateNearestNeighbour;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous.CreateDistanceMap;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.ExpandShrinkObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Relationships.RelateManyToOne;
import wbif.sjx.MIA.Module.WorkflowHandling.WorkflowHandling;
import wbif.sjx.MIA.Object.Units.SpatialUnit;

public class LostAndFound {
    private HashMap<String, String> lostModules = new HashMap<>();
    private HashMap<String, HashMap<String, String>> lostParameterNames = new HashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, String>>> lostParameterValues = new HashMap<>();

    public LostAndFound() {
        //// Populating hard-coded module reassignments ////
        lostModules.put("AutomaticRegistration", new AffineSIFT(null).getClass().getSimpleName());
        lostModules.put("ConditionalAnalysisTermination", new WorkflowHandling(null).getClass().getSimpleName());
        lostModules.put("RunMacroOnImage", new RunMacro(null).getClass().getSimpleName());
        lostModules.put("RunSingleMacroCommand", new RunSingleCommand(null).getClass().getSimpleName());
        lostModules.put("ManualUnwarp", new UnwarpManual(null).getClass().getSimpleName());
        lostModules.put("UnwarpImages", new UnwarpAutomatic(null).getClass().getSimpleName());
        lostModules.put("BlockMatchingRegistration", new AffineBlockMatching(null).getClass().getSimpleName());
        lostModules.put("ManualRegistration", new AffineManual(null).getClass().getSimpleName());
        lostModules.put("MOPSRegistration", new AffineMOPS(null).getClass().getSimpleName());
        lostModules.put("SIFTRegistration", new AffineSIFT(null).getClass().getSimpleName());

        
        //// Populating hard-coded parameter reassignments ////
        HashMap<String, String> currentParameterNames = null;
        String moduleName = null;

        // BlockMatchingRegistration
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Relative mode", AffineBlockMatching.REFERENCE_MODE);
        moduleName = new AffineBlockMatching(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // CalculateNearestNeighbour
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("ParentChildRef mode", CalculateNearestNeighbour.RELATIONSHIP_MODE);
        moduleName = new CalculateNearestNeighbour(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // CreateDistanceMap
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Spatial units", CreateDistanceMap.SPATIAL_UNITS_MODE);
        currentParameterNames.put("Input image", "");
        moduleName = new CreateDistanceMap(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // DistanceMap
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Spatial units", DistanceMap.SPATIAL_UNITS_MODE);
        moduleName = new DistanceMap(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // DistanceMap
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Block size (simultaneous slices)", WekaProbabilityMaps.SIMULTANEOUS_SLICES);
        moduleName = new WekaProbabilityMaps(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // ExpandShrinkObjects
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Radius change (px)", ExpandShrinkObjects.RADIUS_CHANGE);
        moduleName = new ExpandShrinkObjects(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // GetObjectLocalRegion
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Local radius", GetLocalObjectRegion.FIXED_VALUE);
        currentParameterNames.put("Measurement name", GetLocalObjectRegion.RADIUS_MEASUREMENT);
        currentParameterNames.put("Calibrated radius", GetLocalObjectRegion.CALIBRATED_UNITS);
        moduleName = new GetLocalObjectRegion(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // GlobalVariables
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Control type", GlobalVariables.VARIABLE_TYPE);
        moduleName = new GlobalVariables(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // InputControl
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Spatial units", InputControl.SPATIAL_UNIT);
        moduleName = new InputControl(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // LocalAutoThreshold
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Spatial units", LocalAutoThreshold.SPATIAL_UNITS_MODE);
        moduleName = new LocalAutoThreshold(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // MeasureIntensityDistribution
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Spatial units", MeasureIntensityDistribution.SPATIAL_UNITS_MODE);
        moduleName = new MeasureIntensityDistribution(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // MOPSRegistration
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Relative mode", AffineMOPS.REFERENCE_MODE);
        moduleName = new AffineMOPS(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // ObjectLoader
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Output parent clusters name", ObjectLoader.PARENT_OBJECTS_NAME);
        currentParameterNames.put("Output tracks clusters name", ObjectLoader.PARENT_OBJECTS_NAME);
        currentParameterNames.put("Calibration source", ObjectLoader.PARENT_OBJECTS_NAME);
        currentParameterNames.put("Calibration reference image", ObjectLoader.PARENT_OBJECTS_NAME);
        moduleName = new ObjectLoader(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // RelateManyToOne
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Reference point", RelateManyToOne.REFERENCE_MODE);
        moduleName = new RelateManyToOne(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // ReplaceMeasurementValue
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Value to replace", ReplaceMeasurementValue.REFERENCE_VALUE);
        moduleName = new ReplaceMeasurementValue(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // RunMacroCommand
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Macro title", RunSingleCommand.COMMAND);
        moduleName = new RunSingleCommand(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // SIFTRegistration
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Relative mode", AffineSIFT.REFERENCE_MODE);
        moduleName = new AffineSIFT(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // ThresholdImage
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Spatial units", ThresholdImage.SPATIAL_UNITS_MODE);
        moduleName = new ThresholdImage(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // WorkflowHandling
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Reference image measurement mode", WorkflowHandling.NUMERIC_FILTER_MODE);
        currentParameterNames.put("Reference value", WorkflowHandling.REFERENCE_NUMERIC_VALUE);
        moduleName = new WorkflowHandling(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);


        //// Populating hard-coded parameter value reassignments ////
        HashMap<String, String> currentValues = null;
        HashMap<String, HashMap<String, String>> currentParameterValues = null;

        // AbstractAffineRegistration
        currentValues = new HashMap<>();
        currentValues.put("Affine", AbstractAffineRegistration.TransformationModes.AFFINE);
        currentValues.put("Rigid", AbstractAffineRegistration.TransformationModes.RIGID);
        currentValues.put("Similarity", AbstractAffineRegistration.TransformationModes.SIMILARITY);
        currentValues.put("Translation", AbstractAffineRegistration.TransformationModes.TRANSLATION);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(AbstractAffineRegistration.TRANSFORMATION_MODE, currentValues);
        moduleName = new AffineBlockMatching(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);
        moduleName = new AffineManual(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);
        moduleName = new AffineMOPS(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);
        moduleName = new AffineSIFT(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);

        // InputControl
        currentValues = new HashMap<>();
        currentValues.put("METRE", SpatialUnit.AvailableUnits.METRE);
        currentValues.put("CENTIMETRE", SpatialUnit.AvailableUnits.CENTIMETRE);
        currentValues.put("MILLIMETRE", SpatialUnit.AvailableUnits.MILLIMETRE);
        currentValues.put("MICROMETRE", SpatialUnit.AvailableUnits.MICROMETRE);
        currentValues.put("NANOMETRE", SpatialUnit.AvailableUnits.NANOMETRE);
        currentValues.put("ANGSTROM", SpatialUnit.AvailableUnits.ANGSTROM);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(InputControl.SPATIAL_UNIT, currentValues);
        moduleName = new InputControl(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);

        // ImageLoader
        currentValues = new HashMap<>();
        currentValues.put("Image sequence", ImageLoader.ImportModes.IMAGE_SEQUENCE_ZEROS);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(ImageLoader.IMPORT_MODE, currentValues);
        moduleName = new ImageLoader(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);

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

    public String findParameter(String moduleSimpleName, String oldName) {
        // If no name is found, return the old name
        HashMap<String, String> currentParameters = lostParameterNames.get(moduleSimpleName);
        if (currentParameters == null)
            return oldName;

        String newName = currentParameters.get(oldName);
        if (newName == null)
            return oldName;
        else
            return newName;

    }

    public String findParameterValue(String moduleSimpleName, String parameterName, String oldValue) {
        HashMap<String, HashMap<String, String>> currentParameters = lostParameterValues.get(moduleSimpleName);
        if (currentParameters == null)
            return oldValue;

        HashMap<String, String> currentValues = currentParameters.get(parameterName);
        if (currentValues == null)
            return oldValue;

        String newValue = currentValues.get(oldValue);
        if (newValue == null)
            return oldValue;
        else
            return newValue;
    }

    public void addLostModuleAssignment(String oldName, String newName) {
        lostModules.put(oldName, newName);

    }

    public void addLostParameterAssignment(String moduleName, String oldName, String newName) {
        HashMap<String, String> currentParameters = lostParameterNames.putIfAbsent(moduleName, new HashMap<>());
        currentParameters.put(oldName, newName);

    }
}
