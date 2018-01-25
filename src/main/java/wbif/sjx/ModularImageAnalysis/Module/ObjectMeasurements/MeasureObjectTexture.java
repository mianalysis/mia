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

    public interface Measurements {
        String ASM = "ASM";
        String CONTRAST = "CONTRAST";
        String CORRELATION = "CORRELATION";
        String ENTROPY = "ENTROPY";

    }


    private String getFullName(String imageName, String measurement) {
        return "TEXTURE//"+imageName+"_"+measurement;
    }

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
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

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

            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();
            int c = 1;
            int t = object.getT()+1;

            for (int i=0;i<x.size();i++) {
                coords.add(new int[]{x.get(i),y.get(i),z.get(i)});
            }

            textureCalculator.calculate(inputImagePlus,xOffs,yOffs,zOffs,c,t,coords);

            // Acquiring measurements
            Measurement ASMMeasurement = new Measurement(getFullName(inputImageName, Measurements.ASM),textureCalculator.getASM());
            ASMMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(ASMMeasurement);
            } else {
                object.addMeasurement(ASMMeasurement);
            }

            Measurement contrastMeasurement = new Measurement(getFullName(inputImageName, Measurements.CONTRAST),textureCalculator.getContrast());
            contrastMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(contrastMeasurement);
            } else {
                object.addMeasurement(contrastMeasurement);
            }

            Measurement correlationMeasurement = new Measurement(getFullName(inputImageName, Measurements.CORRELATION),textureCalculator.getCorrelation());
            correlationMeasurement.setSource(this);
            if (centroidMeasurement) {
                object.getParent(inputObjectsName).addMeasurement(correlationMeasurement);
            } else {
                object.addMeasurement(correlationMeasurement);
            }

            Measurement entropyMeasurement = new Measurement(getFullName(inputImageName, Measurements.ENTROPY),textureCalculator.getEntropy());
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
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(POINT_MEASUREMENT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MEASUREMENT_RADIUS, Parameter.DOUBLE,10.0));
        parameters.add(new Parameter(X_OFFSET, Parameter.INTEGER,1));
        parameters.add(new Parameter(Y_OFFSET, Parameter.INTEGER,0));
        parameters.add(new Parameter(Z_OFFSET, Parameter.INTEGER,0));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.ASM));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.CONTRAST));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.CORRELATION));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.ENTROPY));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(POINT_MEASUREMENT));

        if (parameters.getValue(POINT_MEASUREMENT)) {
            returnedParameters.add(parameters.getParameter(CALIBRATED_RADIUS));
            returnedParameters.add(parameters.getParameter(MEASUREMENT_RADIUS));
        }

        returnedParameters.add(parameters.getParameter(X_OFFSET));
        returnedParameters.add(parameters.getParameter(Y_OFFSET));
        returnedParameters.add(parameters.getParameter(Z_OFFSET));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        MeasurementReference asm = objectMeasurementReferences.get(Measurements.ASM);
        asm.setImageObjName(inputObjectsName);
        asm.setNickName(getFullName(inputImageName,Measurements.ASM));

        MeasurementReference contrast = objectMeasurementReferences.get(Measurements.CONTRAST);
        contrast.setImageObjName(inputObjectsName);
        contrast.setNickName(getFullName(inputImageName,Measurements.CONTRAST));

        MeasurementReference correlation = objectMeasurementReferences.get(Measurements.CORRELATION);
        correlation.setImageObjName(inputObjectsName);
        correlation.setNickName(getFullName(inputImageName,Measurements.CORRELATION));

        MeasurementReference entropy = objectMeasurementReferences.get(Measurements.ENTROPY);
        entropy.setImageObjName(inputObjectsName);
        entropy.setNickName(getFullName(inputImageName,Measurements.ENTROPY));

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
