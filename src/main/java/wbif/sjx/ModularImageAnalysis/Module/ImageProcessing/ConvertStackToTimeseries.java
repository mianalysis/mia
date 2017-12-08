//package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;
//
//import fiji.stacks.Hyperstack_rearranger;
//import ij.ImagePlus;
//import ij.ImageStack;
//import ij.plugin.Duplicator;
//import ij.plugin.HyperStackConverter;
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Process.SwitchTAndZ;
//
///**
// * Created by sc13967 on 19/06/2017.
// */
//public class ConvertStackToTimeseries extends HCModule {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String APPLY_TO_INPUT = "Apply to input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//
//    @Override
//    public String getTitle() {
//        return "Convert stack to timeseries";
//    }
//
//    @Override
//    public String getHelp() {
//        return "Checks if there is only 1 frame, but multiple Z-sections.  " +
//                "In this case, the Z and T ordering will be switched";
//    }
//
//    @Override
//    public void run(Workspace workspace, boolean verbose) {
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
//        int nChannels = inputImagePlus.getNChannels();
//        int nFrames = inputImagePlus.getNFrames();
//        int nSlices = inputImagePlus.getNSlices();
//
//        if (inputImagePlus.isComposite()) {
//            ImagePlus processedImagePlus = HyperStackConverter.toHyperStack(inputImagePlus,nChannels,nFrames,nSlices);
//            processedImagePlus = Hyperstack_rearranger.reorderHyperstack(processedImagePlus,"CTZ",true,false);
//
//            inputImagePlus.setStack(processedImagePlus.getStack());
//
//        }
//
//        // If the image is being saved as a new image, adding it to the workspace
//        if (!applyToInput) {
//            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
//            Image outputImage = new Image(outputImageName,inputImagePlus);
//            workspace.addImage(outputImage);
//
//        }
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
//        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
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
