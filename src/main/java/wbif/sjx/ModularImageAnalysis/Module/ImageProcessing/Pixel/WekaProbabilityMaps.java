//package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;
//
//import ij.IJ;
//import ij.ImagePlus;
//import ij.Prefs;
//import ij.plugin.Duplicator;
//import trainableSegmentation.WekaSegmentation;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Process.IntensityMinMax;
//
///**
// * Created by sc13967 on 22/03/2018.
// */
//public class WekaProbabilityMaps extends Module {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String CLASSIFIER_FILE = "Classifier file path";
//    public static final String SHOW_IMAGE = "Show probability maps";
//
//    @Override
//    public String getTitle() {
//        return "Weka probability maps";
//    }
//
//    @Override
//    public String getHelp() {
//        return "Loads a saved WEKA classifier model and applies it to the input image.  Returns the " +
//                "\nmulti-channel probability map";
//    }
//
//    @Override
//    protected void run(Workspace workspace) throws GenericMIAException {
//        // Getting input image
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImages().get(inputImageName);
//        ImagePlus inputImagePlus = inputImage.getImagePlus();
//
//        ImagePlus probabilityMaps = new Duplicator().run(inputImagePlus);
//
//        // Getting parameters
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        String classifierFilePath = parameters.getValue(CLASSIFIER_FILE);
//        boolean showImage = parameters.getValue(SHOW_IMAGE);
//
//        // Initialising the training system
//        writeMessage("Loading classifier");
//        WekaSegmentation wekaSegmentation = new WekaSegmentation();
//        wekaSegmentation.loadClassifier(classifierFilePath);
//
//        // Running the classifier
//        writeMessage("Calculating probabilities");
//        int nThreads = Prefs.getThreads();
//        probabilityMaps = wekaSegmentation.applyClassifier(probabilityMaps,nThreads,true);
//
//        // Adding the probability maps to the Workspace
//        workspace.addImage(new Image(outputImageName,probabilityMaps));
//
//        if (showImage) {
//            ImagePlus dispIpl = new Duplicator().run(probabilityMaps);
//            IntensityMinMax.run(dispIpl,true);
//            dispIpl.show();
//        }
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
//        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
//        parameters.add(new Parameter(CLASSIFIER_FILE,Parameter.FILE_PATH,null));
//        parameters.add(new Parameter(SHOW_IMAGE,Parameter.BOOLEAN,false));
//
//    }
//
//    @Override
//    protected void initialiseMeasurementReferences() {
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        return parameters;
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
//
//}
