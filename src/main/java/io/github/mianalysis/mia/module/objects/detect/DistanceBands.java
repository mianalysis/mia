// package io.github.mianalysis.mia.module.objects.detect;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ij.ImagePlus;
// import ij.process.ImageProcessor;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.module.images.process.ImageMath;
// import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
// import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
// import io.github.mianalysis.mia.module.objects.convert.ConvertImageToObjects;
// import io.github.mianalysis.mia.object.Objs;
// import io.github.mianalysis.mia.object.VolumeTypesInterface;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.parameters.BooleanP;
// import io.github.mianalysis.mia.object.parameters.ChoiceP;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
// import io.github.mianalysis.mia.object.parameters.choiceinterfaces.SpatialUnitsInterface;
// import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
// import io.github.mianalysis.mia.object.parameters.text.DoubleP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import net.imagej.ImgPlus;
// import net.imglib2.loops.LoopBuilder;
// import net.imglib2.type.NativeType;
// import net.imglib2.type.numeric.RealType;

// /**
//  * Created by sc13967 on 06/06/2017.
//  */
// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class DistanceBands<T extends RealType<T> & NativeType<T>> extends Module {
//     public static final String INPUT_SEPARATOR = "Image input";
//     public static final String INPUT_IMAGE = "Input image";

//     public static final String OUTPUT_SEPARATOR = "Object output";
//     public static final String OUTPUT_OBJECTS = "Output objects";
//     public static final String BAND_MODE = "Band mode";
//     public static final String VOLUME_TYPE = "Volume type";

//     public static final String BAND_SEPARATOR = "Band controls";
//     public static final String BINARY_LOGIC = "Binary logic";
//     public static final String MATCH_Z_TO_X = "Match Z to XY";
//     public static final String WEIGHT_MODE = "Weight mode";
//     public static final String BAND_WIDTH = "Band width";
//     public static final String SPATIAL_UNITS_MODE = "Spatial units mode";

//     public DistanceBands(Modules modules) {
//         super("Distance bands", modules);
//     }

//     public interface BinaryLogic extends BinaryLogicInterface {
//     }

//     public interface BandModes {
//         String INSIDE_AND_OUTSIDE = "Inside and outside";
//         String INSIDE_OBJECTS = "Inside objects";
//         String OUTSIDE_OBJECTS = "Outside objects";

//         String[] ALL = new String[] { INSIDE_AND_OUTSIDE, INSIDE_OBJECTS, OUTSIDE_OBJECTS };

//     }

//     public interface VolumeTypes extends VolumeTypesInterface {
//     }

//     public interface WeightModes extends DistanceMap.WeightModes {

//     }

//     public interface SpatialUnitsModes extends SpatialUnitsInterface {
//     }

//     public interface DetectionModes extends IdentifyObjects.DetectionModes {
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.OBJECTS_DETECT;
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     public static <T extends RealType<T> & NativeType<T>> Objs getInternalBands(Image<T> inputImage, String outputObjectsName, boolean blackBackground,
//             String weightMode, boolean matchZToXY, double bandWidthPx, String type) {
//         Image<T> distPx = DistanceMap.process(inputImage, "Distance", blackBackground, weightMode, matchZToXY, false);

//         // Generating integer internal bands image
//         ImageMath.process(distPx, ImageMath.CalculationModes.DIVIDE, bandWidthPx);

//         ImagePlus distPxIpl = distPx.getImagePlus();
//         int width = distPxIpl.getWidth();
//         int height = distPxIpl.getHeight();
//         int nChannels = distPxIpl.getNChannels();
//         int nSlices = distPxIpl.getNSlices();
//         int nFrames = distPxIpl.getNFrames();

//         for (int c=1;c<=nChannels;c++) {
//             for (int t = 1; t <= nFrames; t++) {
//                 for (int z = 1; z <= nSlices; z++) {
//                     distPxIpl.setPosition(c, z, t);
//                     ImageProcessor ipr = distPxIpl.getProcessor();
//                     for (int x = 0; x < width; x++) {
//                         for (int y = 0; y < height; y++) {
//                             ipr.setf(x, y, (float) Math.ceil(ipr.getf(x, y)));
//                         }
//                     }
//                 }
//             }
//         }

//         ImageTypeConverter.process(distPx, 8, ImageTypeConverter.ScalingModes.CLIP);

//         // ImgPlus<T> distPxImg = distPx.getImgPlus();
//         // LoopBuilder.setImages(distPxImg).forEachPixel((s) -> s.setReal(Math.ceil(s.getRealDouble())));
//         // ImageTypeConverter.process(inputImage, 8, ImageTypeConverter.ScalingModes.CLIP);
//         // distPx.setImgPlus(distPxImg);

//         // Creating bands objects
//         Objs internalBands = distPx.convertImageToObjects(type, outputObjectsName, false);

//         return internalBands;
//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting parameters
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
//         String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
//         String bandMode = parameters.getValue(BAND_MODE, workspace);
//         boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
//         String type = parameters.getValue(VOLUME_TYPE, workspace);
//         boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X, workspace);
//         String weightMode = parameters.getValue(WEIGHT_MODE, workspace);
//         double bandWidth = parameters.getValue(BAND_WIDTH, workspace);
//         String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE, workspace);

//         // Getting input image
//         Image inputImage = workspace.getImage(inputImageName);

//         // Getting internal bands
//         Objs internalBands = getInternalBands(inputImage, outputObjectsName, blackBackground, weightMode, matchZToXY, bandWidth, type);
//         workspace.addObjects(internalBands);

//         // // Adding objects to workspace
//         // workspace.addObjects(outputObjects);

//         // Showing objects
//         if (showOutput)
//             internalBands.convertToImageIDColours().showImage();

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));

//         parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
//         parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
//         parameters.add(new ChoiceP(BAND_MODE, this, BandModes.INSIDE_AND_OUTSIDE, BandModes.ALL));
//         parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.QUADTREE, VolumeTypes.ALL));

//         parameters.add(new SeparatorP(BAND_SEPARATOR, this));
//         parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
//         parameters.add(new BooleanP(MATCH_Z_TO_X, this, true));
//         parameters.add(new ChoiceP(WEIGHT_MODE, this, WeightModes.WEIGHTS_3_4_5_7, WeightModes.ALL));
//         parameters.add(new DoubleP(BAND_WIDTH, this, 1));
//         parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitsModes.PIXELS, SpatialUnitsModes.ALL));

//         addParameterDescriptions();

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         Parameters returnedParameters = new Parameters();

//         returnedParameters.add(parameters.get(INPUT_SEPARATOR));
//         returnedParameters.add(parameters.get(INPUT_IMAGE));

//         returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
//         returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
//         returnedParameters.add(parameters.get(BAND_MODE));
//         returnedParameters.add(parameters.get(VOLUME_TYPE));

//         returnedParameters.add(parameters.get(BAND_SEPARATOR));
//         returnedParameters.add(parameters.get(BINARY_LOGIC));
//         returnedParameters.add(parameters.get(MATCH_Z_TO_X));
//         returnedParameters.add(parameters.get(WEIGHT_MODE));
//         returnedParameters.add(parameters.get(BAND_WIDTH));
//         returnedParameters.add(parameters.get(SPATIAL_UNITS_MODE));

//         return returnedParameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         MIA.log.writeWarning("NEED TO ADD DISTANCE MEASUREMENTS - THESE COULD BE BAND MIN, MEAN AND MAX");
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
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

//     void addParameterDescriptions() {

//     }
// }
