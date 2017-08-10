package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

//import de.biomedical_imaging.ij.steger.Line;
//import de.biomedical_imaging.ij.steger.LineDetector;
//import de.biomedical_imaging.ij.steger.Lines;
import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class RidgeDetection extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String LOWER_THRESHOLD = "Lower threshold";
    public static final String UPPER_THRESHOLD = "Upper threshold";
    public static final String SIGMA = "Sigma (px)";


    @Override
    public String getTitle() {
        return "Ridge detection";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE";

    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output image name
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting parameters (RidgeDetection plugin wants to use pixel units only)
        double lowerThreshold = parameters.getValue(LOWER_THRESHOLD);
        double upperThreshold = parameters.getValue(UPPER_THRESHOLD);
        double sigma = parameters.getValue(SIGMA);

//        // Running ridge detection
//        if (verbose) System.out.println("["+moduleName+"] Running RidgeDetection plugin (plugin by Thorsten Wagner)");
//        Lines lines = new LineDetector().detectLines(inputImagePlus.getProcessor(),sigma,upperThreshold,lowerThreshold,false,true,false,false);
//
//        // Creating binary image
//        ImagePlus outputImagePlus = IJ.createHyperStack("Lines",inputImagePlus.getWidth(),inputImagePlus.getHeight(),
//                1,1,inputImagePlus.getNFrames(),8);
//
//        // Going through each object, adding its pixel coordinates to the binary image
//        if (verbose) System.out.println("["+moduleName+"] Creating binary ridge image");
//        for (Line line:lines) {
//            float[] x = line.getXCoordinates();
//            float[] y = line.getYCoordinates();
//            int frame = line.getFrame();
//
//            outputImagePlus.setPosition(1,1,frame);
//
//            for (int i=0;i<x.length;i++) {
//                outputImagePlus.getProcessor().set(Math.round(x[i]),Math.round(y[i]),1);
//
//            }
//        }
//
//        // Adding image to workspace
//        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
//        HCImage outputImage = new HCImage(outputImageName,outputImagePlus);
//        workspace.addImage(outputImage);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(LOWER_THRESHOLD, Parameter.DOUBLE,null));
        parameters.addParameter(new Parameter(UPPER_THRESHOLD, Parameter.DOUBLE,null));
        parameters.addParameter(new Parameter(SIGMA, Parameter.DOUBLE,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
