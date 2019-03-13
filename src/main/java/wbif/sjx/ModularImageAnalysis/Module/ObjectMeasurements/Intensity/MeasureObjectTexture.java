package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

/**
 * Takes a set of objects and measures intensity texture values on a provided image.  Measurements are stored with the
 * objects.
 */
public class MeasureObjectTexture extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String POINT_MEASUREMENT = "Measurements based on centroid point";
    public static final String MEASUREMENT_RADIUS = "Measurement radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";
    public static final String CALIBRATED_OFFSET = "Calibrated offset";

    public interface Measurements {
        String ASM = "ASM";
        String CONTRAST = "CONTRAST";
        String CORRELATION = "CORRELATION";
        String ENTROPY = "ENTROPY";

    }


    public static String getFullName(String imageName, String measurement, double[] offs, boolean calibrated) {
        if (calibrated) {
            return "TEXTURE // " + imageName + "_" + measurement + "_(" + offs[0] + "," + offs[1] + "," + offs[2] + " ${CAL})";
        } else {
            return "TEXTURE // " + imageName + "_" + measurement + "_(" + offs[0] + "," + offs[1] + "," + offs[2] + " PX)";
        }
    }


    static void convertCalibratedOffsets(double[] offs, Obj referenceObject) {
        double dppXY = referenceObject.getDistPerPxXY();
        double dppZ = referenceObject.getDistPerPxZ();

        offs[0] = (int) Math.round(offs[0]/dppXY);
        offs[1] = (int) Math.round(offs[1]/dppXY);
        offs[2] = (int) Math.round(offs[2]/dppZ);

    }

    ObjCollection getLocalObjectRegion(ObjCollection objects, double radius, boolean calibrated, ImagePlus inputImagePlus) throws IntegerOverflowException {
        // Getting local object region
        objects = new GetLocalObjectRegion().getLocalRegions(objects,objects.getName(),inputImagePlus,false,"",radius,calibrated);

        return objects;

    }

    public static void processObject(Obj object, Image image, TextureCalculator textureCalculator, boolean centroidMeasurement, double[] rawOffs, boolean calibratedOffset) {
        ImagePlus ipl = image.getImagePlus();

        int t = object.getT()+1;
        int nSlices = ipl.getNSlices();
        ImageStack timeStack = SubHyperstackMaker.makeSubhyperstack(ipl, "1-1", "1-"+nSlices, t+"-"+t).getStack();
        textureCalculator.calculate(timeStack,object);

        // Acquiring measurements
        String name = getFullName(image.getName(), Measurements.ASM,rawOffs,calibratedOffset);
        Measurement measurement = new Measurement(name,textureCalculator.getASM());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(measurement);
        } else {
            object.addMeasurement(measurement);
        }

        name = getFullName(image.getName(), Measurements.CONTRAST,rawOffs,calibratedOffset);
        measurement = new Measurement(name,textureCalculator.getContrast());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(measurement);
        } else {
            object.addMeasurement(measurement);
        }

        name = getFullName(image.getName(), Measurements.CORRELATION,rawOffs,calibratedOffset);
        measurement = new Measurement(name,textureCalculator.getCorrelation());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(measurement);
        } else {
            object.addMeasurement(measurement);
        }

        name = getFullName(image.getName(), Measurements.ENTROPY,rawOffs,calibratedOffset);
        measurement = new Measurement(name,textureCalculator.getEntropy());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(measurement);
        } else {
            object.addMeasurement(measurement);
        }
    }

    @Override
    public String getTitle() {
        return "Measure object texture";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // If no objects were detected, skipping this module
        if (inputObjects.size() == 0) return true;

        // Getting parameters
        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);
        boolean centroidMeasurement = parameters.getValue(POINT_MEASUREMENT);
        double radius = parameters.getValue(MEASUREMENT_RADIUS);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);

        double[] offs = new double[]{xOffsIn,yOffsIn,zOffsIn};

        // If using calibrated offset values, determining the closest pixel offset
        if (calibratedOffset) convertCalibratedOffsets(offs,inputObjects.getFirst());

        // If a centroid region is being used calculate the local region and reassign that to inputObjects reference
        if (centroidMeasurement) {
            try {
                inputObjects = getLocalObjectRegion(inputObjects,radius,calibrated,inputImagePlus);
            } catch (IntegerOverflowException e) {
                return false;
            }
        }

        // Initialising the texture calculator
        TextureCalculator textureCalculator = new TextureCalculator((int) offs[0], (int) offs[1], (int) offs[2]);

        int nObjects = inputObjects.size();
        int iter = 1;
        for (Obj object:inputObjects.values()) {
            writeMessage("Processed "+(++iter)+" of "+nObjects);
            processObject(object,inputImage,textureCalculator,centroidMeasurement,offs,calibratedOffset);
        }

        if (showOutput) inputObjects.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(POINT_MEASUREMENT, this,false));
        parameters.add(new BooleanP(CALIBRATED_RADIUS, this,false));
        parameters.add(new DoubleP(MEASUREMENT_RADIUS, this,10.0));
        parameters.add(new DoubleP(X_OFFSET, this,1d));
        parameters.add(new DoubleP(Y_OFFSET, this,0d));
        parameters.add(new DoubleP(Z_OFFSET, this,0d));
        parameters.add(new BooleanP(CALIBRATED_OFFSET, this,false));

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
        returnedParameters.add(parameters.getParameter(CALIBRATED_OFFSET));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);
        double[] offs = new double[]{xOffsIn,yOffsIn,zOffsIn};

        String name = getFullName(inputImageName,Measurements.ASM,offs,calibratedOffset);
        MeasurementRef asm = objectMeasurementRefs.getOrPut(name);
        asm.setImageObjName(inputObjectsName);
        asm.setCalculated(true);

        name = getFullName(inputImageName,Measurements.CONTRAST,offs,calibratedOffset);
        MeasurementRef contrast = objectMeasurementRefs.getOrPut(name);
        contrast.setImageObjName(inputObjectsName);
        contrast.setCalculated(true);

        name = getFullName(inputImageName,Measurements.CORRELATION,offs,calibratedOffset);
        MeasurementRef correlation = objectMeasurementRefs.getOrPut(name);
        correlation.setImageObjName(inputObjectsName);
        correlation.setCalculated(true);

        name = getFullName(inputImageName,Measurements.ENTROPY,offs,calibratedOffset);
        MeasurementRef entropy = objectMeasurementRefs.getOrPut(name);
        entropy.setImageObjName(inputObjectsName);
        entropy.setCalculated(true);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
