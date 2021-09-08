package io.github.mianalysis.mia.process.analysishandling;

import java.util.HashMap;

import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.imageprocessing.pixel.ImageMath;
// import io.github.mianalysis.MIA.Module.ImageMeasurements.MeasureIntensityDistribution;
import io.github.mianalysis.mia.module.imageprocessing.pixel.WekaProbabilityMaps;
import io.github.mianalysis.mia.module.imageprocessing.pixel.binary.BinaryOperations;
import io.github.mianalysis.mia.module.imageprocessing.pixel.binary.DistanceMap;
import io.github.mianalysis.mia.module.imageprocessing.pixel.binary.ExtendedMinima;
import io.github.mianalysis.mia.module.imageprocessing.pixel.binary.FillHolesByVolume;
import io.github.mianalysis.mia.module.imageprocessing.pixel.threshold.LocalAutoThreshold;
import io.github.mianalysis.mia.module.imageprocessing.pixel.threshold.ThresholdImage;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.AffineBlockMatching;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.AffineMOPS;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.AffineManual;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.AffineSIFT;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.UnwarpAutomatic;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.UnwarpManual;
import io.github.mianalysis.mia.module.imageprocessing.stack.registration.abstrakt.AbstractAffineRegistration;
import io.github.mianalysis.mia.module.inputoutput.ImageLoader;
import io.github.mianalysis.mia.module.inputoutput.MetadataExtractor;
import io.github.mianalysis.mia.module.inputoutput.ObjectLoader;
import io.github.mianalysis.mia.module.miscellaneous.GlobalVariables;
import io.github.mianalysis.mia.module.miscellaneous.macros.RunMacro;
import io.github.mianalysis.mia.module.miscellaneous.macros.RunSingleCommand;
import io.github.mianalysis.mia.module.objectmeasurements.intensity.MeasureObjectIntensity;
import io.github.mianalysis.mia.module.objectmeasurements.miscellaneous.ReplaceMeasurementValue;
import io.github.mianalysis.mia.module.objectmeasurements.spatial.CalculateNearestNeighbour;
import io.github.mianalysis.mia.module.objectmeasurements.spatial.FitSpline;
import io.github.mianalysis.mia.module.objectprocessing.identification.CircleHoughDetection;
import io.github.mianalysis.mia.module.objectprocessing.identification.GetLocalObjectRegion;
import io.github.mianalysis.mia.module.objectprocessing.miscellaneous.CreateDistanceMap;
import io.github.mianalysis.mia.module.objectprocessing.refinement.ExpandShrinkObjects;
import io.github.mianalysis.mia.module.objectprocessing.relationships.RelateManyToOne;
import io.github.mianalysis.mia.module.visualisation.PlotMeasurementsScatter;
import io.github.mianalysis.mia.module.workflowhandling.WorkflowHandling;
import io.github.mianalysis.mia.object.units.SpatialUnit;

public class LostAndFound {
    private HashMap<String, String> lostModules = new HashMap<>();
    private HashMap<String, HashMap<String, String>> lostParameterNames = new HashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, String>>> lostParameterValues = new HashMap<>();

    public LostAndFound() {
        /// Populating hard-coded module reassignments ///
        lostModules.put("Fit spline", new FitSpline(null).getClass().getSimpleName());
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
        lostModules.put("UnwarpImages", new UnwarpAutomatic(null).getClass().getSimpleName());
        lostModules.put("HoughObjectDetection", new CircleHoughDetection(null).getClass().getSimpleName());

        /// Populating hard-coded parameter reassignments ///
        HashMap<String, String> currentParameterNames = null;
        String moduleName = null;

        // BinaryOperations
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Connectivity (3D)", BinaryOperations.CONNECTIVITY);
        moduleName = new BinaryOperations(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

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

        // ExtendedMinima
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Connectivity (3D)", ExtendedMinima.CONNECTIVITY);
        moduleName = new ExtendedMinima(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // FillHolesByVolume
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Use minimum volume", FillHolesByVolume.SET_MINIMUM_VOLUME);
        currentParameterNames.put("Minimum size", FillHolesByVolume.MINIMUM_VOLUME);
        currentParameterNames.put("Use maximum volume", FillHolesByVolume.SET_MAXIMUM_VOLUME);
        currentParameterNames.put("Maximum size", FillHolesByVolume.MAXIMUM_VOLUME);
        moduleName = new FillHolesByVolume(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // GetObjectLocalRegion
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Local radius", GetLocalObjectRegion.FIXED_VALUE_FOR_RADIUS);
        currentParameterNames.put("Fixed value", GetLocalObjectRegion.FIXED_VALUE_FOR_RADIUS);
        currentParameterNames.put("Measurement name", GetLocalObjectRegion.RADIUS_MEASUREMENT);
        currentParameterNames.put("Parent object", GetLocalObjectRegion.PARENT_OBJECT_FOR_RADIUS);

        moduleName = new GetLocalObjectRegion(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // GlobalVariables
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Control type", GlobalVariables.VARIABLE_TYPE);
        currentParameterNames.put("Variable choice", GlobalVariables.VARIABLE_CHOICE);
        moduleName = new GlobalVariables(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // HoughObjectDetection
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Sampling rate", CircleHoughDetection.DOWNSAMPLE_FACTOR);
        moduleName = new CircleHoughDetection(null).getClass().getSimpleName();
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

        // MeasureObjectIntensity
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Measure weighted distance to edge", "");
        currentParameterNames.put("Edge distance mode", "");
        currentParameterNames.put("Measure intensity profile from edge", "");
        currentParameterNames.put("Minimum distance", "");
        currentParameterNames.put("Maximum distance", "");
        currentParameterNames.put("Calibrated distances", "");
        currentParameterNames.put("Number of measurements", "");
        currentParameterNames.put("Only measure on masked regions", "");
        currentParameterNames.put("Mask image", "");
        moduleName = new MeasureObjectIntensity(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        // MetadataExtractor
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Keyword list", "");
        currentParameterNames.put("Keyword source", "");
        moduleName = new MetadataExtractor(null).getClass().getSimpleName();
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

        // PlotMeasurementsScatter
        currentParameterNames = new HashMap<>();
        currentParameterNames.put("Exclude NaN measurements", "");
        moduleName = new PlotMeasurementsScatter(null).getClass().getSimpleName();
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
        currentParameterNames.put("Reference metadata value", WorkflowHandling.METADATA_VALUE);
        currentParameterNames.put("Reference image measurement", WorkflowHandling.IMAGE_MEASUREMENT);
        moduleName = new WorkflowHandling(null).getClass().getSimpleName();
        lostParameterNames.put(moduleName, currentParameterNames);

        /// Populating hard-coded parameter value reassignments ///
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

        // CalculateNearestNeighbour
        currentValues = new HashMap<>();
        currentValues.put("Centroid", CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        currentValues.put("Surface", CalculateNearestNeighbour.ReferenceModes.SURFACE_3D);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(CalculateNearestNeighbour.REFERENCE_MODE, currentValues);
        moduleName = new CalculateNearestNeighbour(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);

        // ImageLoader
        currentValues = new HashMap<>();
        currentValues.put("Image sequence", ImageLoader.ImportModes.IMAGE_SEQUENCE_ZEROS);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(ImageLoader.IMPORT_MODE, currentValues);
        moduleName = new ImageLoader(null).getClass().getSimpleName();
        lostParameterValues.put(moduleName, currentParameterValues);

        // ImageMath
        currentValues = new HashMap<>();
        currentValues.put("Measurement value", ImageMath.ValueSources.MEASUREMENT);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(ImageMath.VALUE_SOURCE, currentValues);
        moduleName = new ImageMath(null).getClass().getSimpleName();
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

        // MetadataExtractor
        currentValues = new HashMap<>();
        currentValues.put("Cell Voyager filename", MetadataExtractor.FilenameExtractors.CV1000_FILENAME_EXTRACTOR);
        currentValues.put("Yokogawa filename", MetadataExtractor.FilenameExtractors.CV1000_FILENAME_EXTRACTOR);
        currentParameterValues = new HashMap<>();
        currentParameterValues.put(MetadataExtractor.FILENAME_EXTRACTOR, currentValues);

        currentValues = new HashMap<>();
        currentValues.put("Cell Voyager foldername",
                MetadataExtractor.FoldernameExtractors.CV1000_FOLDERNAME_EXTRACTOR);
        currentParameterValues.put(MetadataExtractor.FILENAME_EXTRACTOR, currentValues);

        moduleName = new MetadataExtractor(null).getClass().getSimpleName();
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
        String finalModuleName = findModule(moduleSimpleName);

        // If no name is found, return the old name
        HashMap<String, String> currentParameters = lostParameterNames.get(finalModuleName);
        if (currentParameters == null)
            return oldName;

        String newName = currentParameters.get(oldName);
        if (newName == null)
            return oldName;
        else
            return newName;

    }

    public String findParameterValue(String moduleSimpleName, String parameterName, String oldValue) {
        String finalModuleName = findModule(moduleSimpleName);
        String finalParameterName = findParameter(finalModuleName, parameterName);

        HashMap<String, HashMap<String, String>> currentParameters = lostParameterValues.get(finalModuleName);
        if (currentParameters == null)
            return oldValue;

        HashMap<String, String> currentValues = currentParameters.get(finalParameterName);
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