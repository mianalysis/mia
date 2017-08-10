package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.GetLocalObjectRegion;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Takes a set of objects and measures intensity texture values on a provided image.  Measurements are stored with the
 * objects.
 */
public class MeasureObjectTexture extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String POINT_MEASUREMENT = "Measurements based on centroid point";
    public static final String MEASUREMENT_RADIUS = "Measurement radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";


    @Override
    public String getTitle() {
        return "Measure object texture";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET);
        int yOffs = parameters.getValue(Y_OFFSET);
        int zOffs = parameters.getValue(Z_OFFSET);
        boolean centroidMeasurement = parameters.getValue(POINT_MEASUREMENT);

        // If a centroid region is being used calculate the local region and reassign that to inputObjects reference
        if (centroidMeasurement) {
            double radius = parameters.getValue(MEASUREMENT_RADIUS);
            boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);

            // Getting local object region
            inputObjects = GetLocalObjectRegion.getLocalRegions(inputObjects, inputObjectsName, radius, calibrated);

        }

        // Running texture measurement
        if (verbose) System.out.println("["+moduleName+"] Calculating co-occurance matrix");
        if (verbose) System.out.println("["+moduleName+"] X-offset: "+xOffs);
        if (verbose) System.out.println("["+moduleName+"] Y-offset: "+yOffs);
        if (verbose) System.out.println("["+moduleName+"] Z-offset: "+zOffs);

        TextureCalculator textureCalculator = new TextureCalculator();

        int nObjects = inputObjects.size();
        int iter = 1;
        if (verbose) System.out.println("["+moduleName+"] Initialising measurements");
        for (Obj object:inputObjects.values()) {
            if (verbose) System.out.println("["+moduleName+"] Processing object "+(iter++)+" of "+nObjects);
            ArrayList<int[]> coords = new ArrayList<>();

            ArrayList<Integer> x = object.getCoordinates(Obj.X);
            ArrayList<Integer> y = object.getCoordinates(Obj.Y);
            ArrayList<Integer> z = object.getCoordinates(Obj.Z);

            for (int i=0;i<x.size();i++) {
                coords.add(new int[]{x.get(i),y.get(i),z.get(i)});

            }

            textureCalculator.calculate(inputImagePlus,xOffs,yOffs,zOffs,coords);

            // Acquiring measurements
            MIAMeasurement ASMMeasurement = new MIAMeasurement(inputImageName+"_ASM",textureCalculator.getASM());
            ASMMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(ASMMeasurement);
            } else {
                object.addMeasurement(ASMMeasurement);
            }

            MIAMeasurement contrastMeasurement = new MIAMeasurement(inputImageName+"_CONTRAST",textureCalculator.getContrast());
            contrastMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(contrastMeasurement);
            } else {
                object.addMeasurement(contrastMeasurement);
            }

            MIAMeasurement correlationMeasurement = new MIAMeasurement(inputImageName+"_CORRELATION",textureCalculator.getCorrelation());
            correlationMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(correlationMeasurement);
            } else {
                object.addMeasurement(correlationMeasurement);
            }

            MIAMeasurement entropyMeasurement = new MIAMeasurement(inputImageName+"_ENTROPY",textureCalculator.getEntropy());
            entropyMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(entropyMeasurement);
            } else {
                object.addMeasurement(entropyMeasurement);
            }

        }

        if (verbose) System.out.println("["+moduleName+"] Measurements complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(POINT_MEASUREMENT, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(MEASUREMENT_RADIUS, Parameter.DOUBLE,10.0));
        parameters.addParameter(new Parameter(X_OFFSET, Parameter.INTEGER,1));
        parameters.addParameter(new Parameter(Y_OFFSET, Parameter.INTEGER,0));
        parameters.addParameter(new Parameter(Z_OFFSET, Parameter.INTEGER,0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(POINT_MEASUREMENT));

        if (parameters.getValue(POINT_MEASUREMENT)) {
            returnedParameters.addParameter(parameters.getParameter(CALIBRATED_RADIUS));
            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT_RADIUS));
        }

        returnedParameters.addParameter(parameters.getParameter(X_OFFSET));
        returnedParameters.addParameter(parameters.getParameter(Y_OFFSET));
        returnedParameters.addParameter(parameters.getParameter(Z_OFFSET));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        if (parameters.getValue(INPUT_OBJECTS) != null) {
            measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS), (parameters.getValue(INPUT_IMAGE)) + "_ASM");
            measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS), (parameters.getValue(INPUT_IMAGE)) + "_CONTRAST");
            measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS), (parameters.getValue(INPUT_IMAGE)) + "_CORRELATION");
            measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS), (parameters.getValue(INPUT_IMAGE)) + "_ENTROPY");
        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
