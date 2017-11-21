// TODO: Check "frame" from RidgeDetection is 0-indexed

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class RidgeDetection extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LOWER_THRESHOLD = "Lower threshold";
    public static final String UPPER_THRESHOLD = "Upper threshold";
    public static final String SIGMA = "Sigma (px)";
    public static final String MIN_LENGTH = "Minimum length (px)";
    public static final String MAX_LENGTH = "Maximum length (px)";

    private interface Measurements {
        String LENGTH_PX = "RIDGE_DETECT//LENGTH_(PX)";

    }


    @Override
    public String getTitle() {
        return "Ridge detection";
    }

    @Override
    public String getHelp() {
        return "Uses the RidgeDetection Fiji plugin by Thorsten Wagner, which implements Carsten Steger's " +
                "\npaper \"An Unbiased Detector of Curvilinear Structures\"" +
                "\nINCOMPLETE";

    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting parameters (RidgeDetection plugin wants to use pixel units only)
        double lowerThreshold = parameters.getValue(LOWER_THRESHOLD);
        double upperThreshold = parameters.getValue(UPPER_THRESHOLD);
        double sigma = parameters.getValue(SIGMA);
        double minLength = parameters.getValue(MIN_LENGTH);
        double maxLength = parameters.getValue(MAX_LENGTH);

        // Storing the image calibration
        Calibration calibration = inputImagePlus.getCalibration();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

//        // Converting image to 8-bit
//        inputImagePlus = new Duplicator().run(inputImagePlus);
//        IntensityMinMax.run(inputImagePlus,true);
//        IJ.run(inputImagePlus,"8-bit",null);

        // Running ridge detection
        if (verbose) System.out.println("["+moduleName+"] Running RidgeDetection plugin (plugin by Thorsten Wagner)");
        Lines lines = new LineDetector().detectLines(inputImagePlus.getProcessor(),sigma,upperThreshold,lowerThreshold,minLength,maxLength,false,true,false,false);

        // Iterating over each object, adding it to the nascent ObjSet
        ObjSet outputObjects = new ObjSet(outputObjectsName);

        for (Line line:lines) {
            float[] x = line.getXCoordinates();
            float[] y = line.getYCoordinates();
            int frame = line.getFrame();

            Obj outputObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibrationUnits);
            outputObject.setT(frame);

            for (int i=0;i<x.length;i++) {
                outputObject.addCoord(Math.round(x[i]),Math.round(y[i]),0);
            }

            double estLength = line.estimateLength();
            outputObject.addMeasurement(new MIAMeasurement(Measurements.LENGTH_PX,estLength));

            outputObjects.add(outputObject);

        }

        workspace.addObjects(outputObjects);

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName+") to workspace");


    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(LOWER_THRESHOLD, Parameter.DOUBLE,0.5));
        parameters.addParameter(new Parameter(UPPER_THRESHOLD, Parameter.DOUBLE,0.85));
        parameters.addParameter(new Parameter(SIGMA, Parameter.DOUBLE,3d));
        parameters.addParameter(new Parameter(MIN_LENGTH, Parameter.DOUBLE,0d));
        parameters.addParameter(new Parameter(MAX_LENGTH, Parameter.DOUBLE,0d));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        measurements.addMeasurement(outputObjectsName,Measurements.LENGTH_PX);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}