//// TODO: Add option to mask objects
//// TODO: Options to set inside objects to negative
//// TODO: Option to normalise inside-only edge to maximum distance to object edge
//
//// - Mask inside
//// - Mask outside
//// - Negative inside
//
//package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous;
//
//import ij.IJ;
//import ij.ImagePlus;
//import ij.plugin.Duplicator;
//import ij.plugin.Resizer;
//import inra.ijpb.binary.ChamferWeights3D;
//import inra.ijpb.plugins.GeodesicDistanceMap3D;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
//import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ImageCalculator;
//import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ImageMath;
//import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
//import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack.InterpolateZAxis;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Process.IntensityMinMax;
//
//import java.util.HashMap;
//
//public class CreateDistanceMap extends Module {
//    public static final String INPUT_OBJECTS = "Input objects";
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String REFERENCE_MODE = "Reference mode";
//    public static final String INVERT_MAP_WITHIN_OBJECTS = "Invert map within objects";
//    public static final String MASKING_MODE = "Masking mode";
//
//
//    public interface ReferenceModes {
//        String ABSOLUTE_CENTROID_DISTANCE = "Absolute distance from object centroid";
//        String DISTANCE_FROM_EDGE = "Distance from object edge";
//
//        String[] ALL = new String[]{ABSOLUTE_CENTROID_DISTANCE, DISTANCE_FROM_EDGE};
//
//    }
//
//    public interface MaskingModes {
//        String INSIDE_AND_OUTSIDE = "Inside and outside";
//        String INSIDE_ONLY = "Inside only";
//        String OUTSIDE_ONLY = "Outside only";
//
//        String[] ALL = new String[]{INSIDE_AND_OUTSIDE,INSIDE_ONLY,OUTSIDE_ONLY};
//
//    }
//
//
//    public static Image getCentroidDistanceMap(Image inputImage, ObjCollection inputObjects, String outputImageName) {
//        // Getting image parameters
//        int width = inputImage.getImagePlus().getWidth();
//        int height = inputImage.getImagePlus().getHeight();
//        int nZ = inputImage.getImagePlus().getNSlices();
//        int nT = inputImage.getImagePlus().getNFrames();
//
//        // Creating a blank image (8-bit, so binary operations work)
//        ImagePlus distanceMap = IJ.createHyperStack(outputImageName, width, height,1,nZ,nT,8);
//        distanceMap.setCalibration(inputImage.getImagePlus().getCalibration());
//
//        // Adding a spot to the centre of each object
//        for (Obj inputObj:inputObjects.values()) {
//            int x = (int) Math.round(inputObj.getXMean(true));
//            int y = (int) Math.round(inputObj.getYMean(true));
//            int z = (int) Math.round(inputObj.getZMean(true,false));
//            int t = inputObj.getT();
//
//            distanceMap.setPosition(1,z+1,t+1);
//            distanceMap.getProcessor().set(x,y,255);
//        }
//
//        // Calculating the distance map
//        distanceMap = BinaryOperations.getDistanceMap3D(distanceMap,true);
//
//        return new Image(outputImageName,distanceMap);
//
//    }
//
//    public static Image getEdgeDistanceMap(Image inputImage, ObjCollection inputObjects, String outputImageName, boolean invertInside) {
//        // Creating an objects image
//        String colourMode = ObjCollection.ColourModes.SINGLE_COLOUR;
//        String colourSource = ObjCollection.SingleColours.WHITE;
//        HashMap<Integer, Float> hues = inputObjects.getHues(colourMode, colourSource, false);
//        ImagePlus objIpl = inputObjects.convertObjectsToImage(outputImageName,inputImage,colourMode,hues).getImagePlus();
//
//        // Calculating the distance maps.  The inside map is set to negative
//        ImagePlus outsideDistIpl = BinaryOperations.getDistanceMap3D(objIpl,true);
//        InvertIntensity.process(objIpl);
//        BinaryOperations.applyStockBinaryTransform(objIpl,BinaryOperations.OperationModes.ERODE_2D,1);
//        ImagePlus insideDistIpl = BinaryOperations.getDistanceMap3D(objIpl,true);
//
//        // If selected, inverting the inside of the object, so values here are negative
//        if (invertInside) ImageMath.process(insideDistIpl,ImageMath.CalculationTypes.MULTIPLY,-1.0);
//
//        // Compiling the distance map
//        ImagePlus distanceMap = new ImageCalculator().process(insideDistIpl,outsideDistIpl,
//                ImageCalculator.CalculationMethods.ADD,ImageCalculator.OverwriteModes.CREATE_NEW,true,true);
//
//        return new Image(outputImageName,distanceMap);
//
//    }
//
//
//    @Override
//    public String getTitle() {
//        return "Create distance map";
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.OBJECT_PROCESSING_MISCELLANEOUS;
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    protected void run(Workspace workspace) throws GenericMIAException {
//        // Getting input objects
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
//
//        // Getting input image
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImage(inputImageName);
//
//        // Getting other parameters
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        String referenceMode = parameters.getValue(REFERENCE_MODE);
//        boolean invertInside = parameters.getValue(INVERT_MAP_WITHIN_OBJECTS);
//        String maskingMode = parameters.getValue(MASKING_MODE);
//
//        // Initialising the distance map
//        Image distanceMap = null;
//        switch (referenceMode) {
//            case ReferenceModes.ABSOLUTE_CENTROID_DISTANCE:
//                distanceMap = getCentroidDistanceMap(inputImage,inputObjects,outputImageName);
//                break;
//
//            case ReferenceModes.DISTANCE_FROM_EDGE:
//                distanceMap = getEdgeDistanceMap(inputImage,inputObjects,outputImageName,invertInside);
//                break;
//        }
//
//        if (distanceMap == null) return;
//
//        workspace.addImage(distanceMap);
//
//        // If necessary, displaying the distance map
//        if (showOutput) {
//            ImagePlus dispIpl = new Duplicator().run(distanceMap.getImagePlus());
//            dispIpl.setTitle(distanceMap.getName());
//            IntensityMinMax.run(dispIpl,true);
//            dispIpl.show();
//        }
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
//        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
//        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
//        parameters.add(new Parameter(REFERENCE_MODE,Parameter.CHOICE_ARRAY,ReferenceModes.ABSOLUTE_CENTROID_DISTANCE,ReferenceModes.ALL));
//        parameters.add(new Parameter(INVERT_MAP_WITHIN_OBJECTS,Parameter.BOOLEAN,true));
//        parameters.add(new Parameter(MASKING_MODE,Parameter.CHOICE_ARRAY,MaskingModes.INSIDE_AND_OUTSIDE,MaskingModes.ALL));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//
//        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
//        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
//        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
//
//        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
//        switch ((String) parameters.getValue(REFERENCE_MODE)) {
//            case ReferenceModes.DISTANCE_FROM_EDGE:
//                returnedParameters.add(parameters.getParameter(INVERT_MAP_WITHIN_OBJECTS));
//                break;
//        }
//
//        returnedParameters.add(parameters.getParameter(MASKING_MODE));
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
