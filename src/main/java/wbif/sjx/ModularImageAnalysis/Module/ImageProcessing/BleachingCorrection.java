//package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;
//
//import emblcmci.BleachCorrection;
//import emblcmci.BleachCorrection_MH;
//import ij.ImagePlus;
//import ij.plugin.Duplicator;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Process.IntensityMinMax;
//
///**
// * Created by sc13967 on 30/11/2017.
// */
//public class BleachingCorrection extends HCModule {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String APPLY_TO_INPUT = "Apply to input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String SHOW_IMAGE = "Show image";
//
//    @Override
//    public String getTitle() {
//        return "Bleaching correction";
//    }
//
//    @Override
//    public String getHelp() {
//        return "Uses the Fiji bleaching correction plugin (by Kota Miura)";
//    }
//
//    @Override
//    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
//        // Getting input image
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImages().get(inputImageName);
//        ImagePlus inputImagePlus = inputImage.getImagePlus();
//
//        // Getting parameters
//        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//
//        // If applying to a new image, the input image is duplicated
//        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}
//
//        if (verbose) System.out.println("["+moduleName+"] Running bleach correction (may take several minutes)");
//
//        new BleachCorrection_MH(inputImagePlus).doCorrection();
//
//        // If the image is being saved as a new image, adding it to the workspace
//        if (!applyToInput) {
//            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
//            Image outputImage = new Image(outputImageName,inputImagePlus);
//            workspace.addImage(outputImage);
//
//            // If selected, displaying the image
//            if (parameters.getValue(SHOW_IMAGE)) {
//                ImagePlus dispIpl = new Duplicator().run(outputImage.getImagePlus());
//                IntensityMinMax.run(dispIpl,true);
//                dispIpl.show();
//            }
//
//        } else {
//            // If selected, displaying the image
//            if (parameters.getValue(SHOW_IMAGE)) {
//                ImagePlus dispIpl = new Duplicator().run(inputImagePlus);
//                IntensityMinMax.run(dispIpl,true);
//                dispIpl.show();
//            }
//        }
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
//        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
//        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));
//
//        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
//            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
//        }
//
//        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));
//
//        return returnedParameters;
//
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
