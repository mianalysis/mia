package io.github.mianalysis.mia.module.objects.measure.intensity;
// //TODO: Add ability to expand objects by a couple of pixels, to give a background signal estimate.  Probably only makes
// //      sense for normalised distances

// package io.github.mianalysis.MIA.Module.ObjectMeasurements.Intensity;

// import static io.github.mianalysis.MIA.Module.ImageMeasurements.MeasureIntensityDistribution.getDistanceBins;

// import ij.ImagePlus;
// import ij.measure.ResultsTable;
// import io.github.mianalysis.MIA.Module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.module.Module;
// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;
// import io.github.mianalysis.MIA.Module.Category;
// import io.github.mianalysis.MIA.Module.Categories;
// import io.github.mianalysis.MIA.Module.ObjectProcessing.Miscellaneous.CreateDistanceMap;
// import io.github.mianalysis.MIA.Object.Status;
// import io.github.mianalysis.MIA.Object.Image;
// import io.github.mianalysis.MIA.Object.Obj;
// import io.github.mianalysis.MIA.Object.Objs;
// import io.github.mianalysis.MIA.Object.Workspace;
// import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
// import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
// import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
// import io.github.mianalysis.MIA.Object.Parameters.Parameters;
// import io.github.mianalysis.MIA.Object.Parameters.Text.DoubleP;
// import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
// import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefs;
// import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefs;
// import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefs;
// import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefs;
// import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefs;
// import io.github.mianalysis.mia.object.coordinates.quadtree.QuadTree;
// import io.github.mianalysis.mia.object.coordinates.Point;

//public class MeasureRadialIntensityProfile extends Module {
//     public static final String INPUT_OBJECTS = "Input objects";
//     public static final String INPUT_IMAGE = "Input image";
//     public static final String REFERENCE_MODE = "Reference mode";
//     public static final String DISTANCE_MAP_IMAGE = "Distance map image";
//     public static final String MASKING_MODE = "Masking mode";
//     public static final String NUMBER_OF_RADIAL_SAMPLES = "Number of radial samples";
//     public static final String RANGE_MODE = "Range mode";
//     public static final String MIN_DISTANCE = "Minimum distance";
//     public static final String MAX_DISTANCE = "Maximum distance";

//     public MeasureRadialIntensityProfile(Modules modules) {
//         super("Measure radial intensity profile", modules);
//     }
//     // public static final String NORMALISE_DISTANCES = "Normalise distances to
//     // object size";
//     // public static final String CALIBRATED_UNITS = "Calibrated units"; // To be
//     // added

//     public interface ReferenceModes extends CreateDistanceMap.ReferenceModes {
//         String CUSTOM_DISTANCE_MAP = "Custom distance map";

//         String[] ALL = new String[] { CreateDistanceMap.ReferenceModes.DISTANCE_FROM_CENTROID,
//                 CreateDistanceMap.ReferenceModes.DISTANCE_FROM_EDGE, CUSTOM_DISTANCE_MAP };

//     }

//     public interface MaskingModes extends CreateDistanceMap.MaskingModes {
//     }

//     public interface RangeModes {
//         String AUTOMATIC_RANGE = "Automatic range";
//         String MANUAL_RANGE = "Manual range";

//         String[] ALL = new String[] { AUTOMATIC_RANGE, MANUAL_RANGE };

//     }

//     static Image getDistanceMap(Objs inputObjects, Image inputImage, String referenceMode) {
//         switch (referenceMode) {
//             case ReferenceModes.DISTANCE_FROM_CENTROID:
//                 return CreateDistanceMap.getCentroidDistanceMap(inputObjects, "Distance map");

//             case ReferenceModes.DISTANCE_FROM_EDGE:
//                 return CreateDistanceMap.getEdgeDistanceMap(inputObjects, "Distance map", false);
//         }

//         return null;

//     }

//     static CumStat[] processObject(Obj inputObject, Image inputImage, Image distanceMap, double[] distanceBins) {
//         // Setting up CumStats to hold results
//         CumStat[] cumStats = new CumStat[distanceBins.length];
//         for (int i = 0; i < cumStats.length; i++)
//             cumStats[i] = new CumStat();

//         ImagePlus inputIpl = inputImage.getImagePlus();
//         ImagePlus distanceMapIpl = distanceMap.getImagePlus();

//         int t = inputObject.getT();

//         double minDist = distanceBins[0];
//         double maxDist = distanceBins[distanceBins.length - 1];
//         double binWidth = distanceBins[1] - distanceBins[0];

//         for (Point<Integer> point : inputObject.getCoordinateSet()) {
//             int x = point.getX();
//             int y = point.getY();
//             int z = point.getZ();

//             inputIpl.setPosition(1, z + 1, t + 1);
//             distanceMapIpl.setPosition(1, z + 1, t + 1);

//             double distance = distanceMapIpl.getProcessor().getPixelValue(x, y);
//             double intensity = inputIpl.getProcessor().getPixelValue(x, y);
//             double bin = Math.round((distance - minDist) / binWidth) * binWidth + minDist;

//             // Ensuring the bin is within the specified range
//             bin = Math.min(bin, maxDist);
//             bin = Math.max(bin, minDist);

//             // Adding the measurement to the relevant bin
//             for (int i = 0; i < distanceBins.length; i++) {
//                 if (Math.abs(bin - distanceBins[i]) < binWidth / 2)
//                     cumStats[i].addMeasure(intensity);
//             }
//         }

//         return cumStats;

//     }


//     @Override
//     public Category getCategory() {
//         return Categories.OBJECTS_MEASURE_INTENSITY;
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting input objects
//         String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
//         Objs inputObjects = workspace.getObjects(inputObjectsName);

//         // Getting input image
//         String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
//         Image inputImage = workspace.getImage(inputImageName);

//         // Getting other parameters
//         String referenceMode = parameters.getValue(REFERENCE_MODE,workspace);
//         String distanceMapImageName = parameters.getValue(DISTANCE_MAP_IMAGE,workspace);
//         String maskingMode = parameters.getValue(MASKING_MODE,workspace);
//         int nRadialSample = parameters.getValue(NUMBER_OF_RADIAL_SAMPLES,workspace);
//         String rangeMode = parameters.getValue(RANGE_MODE,workspace);
//         double minDistance = parameters.getValue(MIN_DISTANCE,workspace);
//         double maxDistance = parameters.getValue(MAX_DISTANCE,workspace);

//         // Getting the distance map for all objects
//         Image distanceMap = null;
//         switch (referenceMode) {
//             case ReferenceModes.CUSTOM_DISTANCE_MAP:
//                 distanceMap = workspace.getImage(distanceMapImageName);
//                 break;

//             case ReferenceModes.DISTANCE_FROM_EDGE:
//             case ReferenceModes.DISTANCE_FROM_CENTROID:
//                 distanceMap = getDistanceMap(inputObjects, inputImage, referenceMode);
//                 break;
//         }

//         // Applying the relevant masking
//         CreateDistanceMap.applyMasking(distanceMap, inputObjects, maskingMode);

//         // Getting the distance bin centroids
//         double[] distanceBins = null;
//         switch (rangeMode) {
//             case RangeModes.AUTOMATIC_RANGE:
//                 distanceBins = getDistanceBins(distanceMap, nRadialSample);
//                 break;

//             case RangeModes.MANUAL_RANGE:
//                 distanceBins = getDistanceBins(nRadialSample, minDistance, maxDistance);
//                 break;
//         }

//         // Creating a new ResultsTable
//         ResultsTable resultsTable = new ResultsTable();

//         // Adding distance bin values to ResultsTable
//         for (int i = 0; i < distanceBins.length; i++) {
//             resultsTable.setValue("Distance", i, distanceBins[i]);
//         }

//         // Processing each object
//         int count = 0;
//         int total = inputObjects.size();
//         for (Obj inputObject : inputObjects.values()) {
//             CumStat[] cumStats = processObject(inputObject, inputImage, distanceMap, distanceBins);

//             for (int i = 0; i < distanceBins.length; i++) {
//                 resultsTable.setValue(("Object " + count + " mean"), i, cumStats[i].getMean());
//                 resultsTable.setValue(("Object " + count + " N"), i, cumStats[i].getN());
//             }

//             writeProgressStatus(++count, total, "objects");
            
//         }

//         resultsTable.show("Radial intensity profile");

//         if (showOutput)
//             inputObjects.showMeasurements(this, modules);

//         return Status.PASS;

//     }

//     @Override
//     public void initialiseParameters() {
//         parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.DISTANCE_FROM_CENTROID, ReferenceModes.ALL));
//         parameters.add(new InputImageP(DISTANCE_MAP_IMAGE, this));
//         parameters.add(new ChoiceP(MASKING_MODE, this, MaskingModes.INSIDE_ONLY, MaskingModes.ALL));
//         parameters.add(new IntegerP(NUMBER_OF_RADIAL_SAMPLES, this, 10));
//         parameters.add(new ChoiceP(RANGE_MODE, this, RangeModes.AUTOMATIC_RANGE, RangeModes.ALL));
//         parameters.add(new DoubleP(MIN_DISTANCE, this, 0d));
//         parameters.add(new DoubleP(MAX_DISTANCE, this, 1d));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         Parameters returnedParameters = new Parameters();

//         returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
//         returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

//         returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
//         switch ((String) parameters.getValue(REFERENCE_MODE,workspace)) {
//             case ReferenceModes.CUSTOM_DISTANCE_MAP:
//                 returnedParameters.add(parameters.getParameter(DISTANCE_MAP_IMAGE));
//                 break;
//         }

//         returnedParameters.add(parameters.getParameter(MASKING_MODE));
//         returnedParameters.add(parameters.getParameter(NUMBER_OF_RADIAL_SAMPLES));

//         returnedParameters.add(parameters.getParameter(RANGE_MODE));
//         switch ((String) parameters.getValue(RANGE_MODE,workspace)) {
//             case RangeModes.MANUAL_RANGE:
//                 returnedParameters.add(parameters.getParameter(MIN_DISTANCE));
//                 returnedParameters.add(parameters.getParameter(MAX_DISTANCE));
//                 break;
//         }

//         return returnedParameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
// public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
// public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
