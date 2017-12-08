//package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;
//
//import ij.ImagePlus;
//import ij.plugin.Duplicator;
//import ij.plugin.ZProjector;
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;
//
///**
// * Created by sc13967 on 04/05/2017.
// */
//public class ProjectImage extends HCModule {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String PROJECTION_MODE = "Projection mode";
//    public static final String SHOW_IMAGE = "Show image";
//
//    public interface ProjectionModes {
//        String AVERAGE = "Average";
//        String MIN = "Minimum";
//        String MEDIAN = "Median";
//        String MAX = "Maximum";
//        String STDEV = "Standard deviation";
//        String SUM = "Sum";
//
//        String[] ALL = new String[]{AVERAGE, MIN, MEDIAN, MAX, STDEV, SUM};
//
//    }
//
//    public Image projectImageInZ(Image inputImage, String outputImageName, String projectionMode) {
//        ZProjector zProjector = new ZProjector(inputImage.getImagePlus());
//
//        switch (projectionMode) {
//            case ProjectionModes.AVERAGE:
//                zProjector.setMethod(ZProjector.AVG_METHOD);
//                break;
//
//            case ProjectionModes.MIN:
//                zProjector.setMethod(ZProjector.MIN_METHOD);
//                break;
//
//            case ProjectionModes.MEDIAN:
//                zProjector.setMethod(ZProjector.MEDIAN_METHOD);
//                break;
//
//            case ProjectionModes.MAX:
//                zProjector.setMethod(ZProjector.MAX_METHOD);
//                break;
//
//            case ProjectionModes.STDEV:
//                zProjector.setMethod(ZProjector.SD_METHOD);
//                break;
//
//            case ProjectionModes.SUM:
//                zProjector.setMethod(ZProjector.SUM_METHOD);
//                break;
//        }
//
//        zProjector.doProjection();
//        ImagePlus iplOut = zProjector.getProjection();
//
//        return new Image(outputImageName,iplOut);
//
//    }
//
//    @Override
//    public String getTitle() {
//        return "Project image";
//
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    public void run(Workspace workspace, boolean verbose) {
//        // Loading image into workspace
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImages().get(inputImageName);
//
//        // Getting parameters
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        String projectionMode = parameters.getValue(PROJECTION_MODE);
//
//        // Create max projection image
//        Image outputImage = projectImageInZ(inputImage,outputImageName,projectionMode);
//
//        // Adding projected image to workspace
//        workspace.addImage(outputImage);
//
//        // If selected, displaying the image
//        if (parameters.getValue(SHOW_IMAGE)) {
//            new Duplicator().run(outputImage.getImagePlus()).show();
//        }
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(PROJECTION_MODE,Parameter.CHOICE_ARRAY,ProjectionModes.AVERAGE,ProjectionModes.ALL));
//        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        return parameters;
//    }
//
//    @Override
//    public void initialiseImageReferences() {
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetImageReferences() {
//        return null;
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetObjectReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
