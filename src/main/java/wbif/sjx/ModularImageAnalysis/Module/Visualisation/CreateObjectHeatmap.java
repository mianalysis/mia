//package wbif.sjx.ModularImageAnalysis.Module.Visualisation;
//
//import ij.IJ;
//import ij.ImagePlus;
//import ij.measure.Calibration;
//import ij.process.ImageProcessor;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.MathFunc.CumStat;
//import wbif.sjx.common.MathFunc.Indexer;
//import wbif.sjx.common.MathFunc.MidpointCircle;
//import wbif.sjx.common.Object.Point;
//
//public class CreateObjectHeatmap extends Module {
//    public static final String INPUT_OBJECTS = "Input objects";
//    public static final String TEMPLATE_IMAGE = "Template image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String DOWNSAMPLE_RANGE = "Downsample range";
//    public static final String AVERAGE_SLICES = "Average slices";
//    public static final String AVERAGE_TIME = "Average time";
//
//
//    public Image process(ObjCollection objects, Image image, String outputImageName, boolean averageZ, boolean averageT, int downsampleRange) {
//        ImagePlus ipl = image.getImagePlus();
//
//        // Get final CumStat[] dimensions
//        int width = ipl.getWidth();
//        int height = ipl.getHeight();
//        int nSlices = averageZ ? 1 : ipl.getNSlices();
//        int nFrames = averageT ? 1 : ipl.getNFrames();
//
//        // Creating ImagePlus
//        ImagePlus outputIpl = IJ.createHyperStack(outputImageName,width,height,1,nSlices,nFrames,16);
//        outputIpl.setCalibration(ipl.getCalibration());
//
//        // Getting coordinates of reference points
//        MidpointCircle midpointCircle = new MidpointCircle(downsampleRange);
//        int[] xSamp = midpointCircle.getXCircleFill();
//        int[] ySamp = midpointCircle.getYCircleFill();
//
//        // Adding objects
//        for (Obj object:objects.values()) {
//            // Getting all object points
//            for (Point<Integer> point:object.getPoints()) {
//                // Getting index for this point
//                int x = point.getX();
//                int y = point.getY();
//                int z = averageZ ? 0 : point.getZ();
//                int t = averageT ? 0 : object.getT();
//
//                // Incrementing object count
//                for (int i = 0; i < xSamp.length; i++) {
//                    int xx = x + xSamp[i];
//                    int yy = y + ySamp[i];
//
//                    if (xx < 0 || xx >= width || yy < 0 || yy >= height) continue;
//                    outputIpl.setPosition(1, z + 1, t + 1);
//                    ImageProcessor ipr = outputIpl.getProcessor();
//                    ipr.set(xx, yy, ipr.get(xx, yy) + 1);
//                }
//            }
//        }
//
//        return new Image(outputImageName,outputIpl);
//
//    }
//
//
//    @Override
//    public String getTitle() {
//        return "Create object heatmap";
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.VISUALISATION;
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    protected boolean run(Workspace workspace) {
//        // Getting input objects
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
//
//        // Getting template image
//        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
//        Image templateImage = workspace.getImage(templateImageName);
//
//        // Getting parameters
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        int downsampleRange = parameters.getValue(DOWNSAMPLE_RANGE);
//        boolean averageZ = parameters.getValue(AVERAGE_SLICES);
//        boolean averageT = parameters.getValue(AVERAGE_TIME);
//
//        // Create statistic image
//        Calibration calibration = templateImage.getImagePlus().getCalibration();
//        Image outputImage = process(inputObjects,templateImage,outputImageName,averageZ,averageT,downsampleRange);
//
//        workspace.addImage(outputImage);
//        if (showOutput) showImage(outputImage);
//
//        return true;
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
//        parameters.add(new Parameter(TEMPLATE_IMAGE,Parameter.INPUT_IMAGE,null));
//        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
//        parameters.add(new Parameter(DOWNSAMPLE_RANGE,Parameter.INTEGER,3));
//        parameters.add(new Parameter(AVERAGE_SLICES,Parameter.BOOLEAN,true));
//        parameters.add(new Parameter(AVERAGE_TIME,Parameter.BOOLEAN,true));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        return parameters;
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
//    public MetadataReferenceCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
