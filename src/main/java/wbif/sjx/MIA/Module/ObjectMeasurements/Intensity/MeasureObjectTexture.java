package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import java.util.concurrent.atomic.AtomicInteger;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Analysis.TextureCalculator;

/**
 * Takes a set of objects and measures intensity texture values on a provided image.  Measurements are stored with the
 * objects.
 */
public class MeasureObjectTexture extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    
    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";
    public static final String CALIBRATED_OFFSET = "Calibrated offset";

    public MeasureObjectTexture(ModuleCollection modules) {
        super("Measure object texture",modules);
    }

    public interface Measurements {
        String ASM = "ASM";
        String CONTRAST = "CONTRAST";
        String CORRELATION = "CORRELATION";
        String ENTROPY = "ENTROPY";

    }


    public static String getFullName(String imageName, String measurement, double[] offs, boolean calibrated) {
        if (calibrated) {
            return "TEXTURE // " + imageName + "_" + measurement + "_(" + offs[0] + "," + offs[1] + "," + offs[2] + " ${SCAL})";
        } else {
            return "TEXTURE // " + imageName + "_" + measurement + "_(" + offs[0] + "," + offs[1] + "," + offs[2] + " PX)";
        }
    }


    static void convertCalibratedOffsets(double[] offs, Obj referenceObject) {
        double dppXY = referenceObject.getDppXY();
        double dppZ = referenceObject.getDppZ();

        offs[0] = (int) Math.round(offs[0]/dppXY);
        offs[1] = (int) Math.round(offs[1]/dppXY);
        offs[2] = (int) Math.round(offs[2]/dppZ);

    }

    public static void processObject(Obj object, Image image, TextureCalculator textureCalculator, double[] offs, boolean calibratedOffset) {
        ImagePlus ipl = image.getImagePlus();

        // If the input stack is a single timepoint and channel, there's no need to create a new ImageStack
        ImageStack timeStack = null;
        if (ipl.getNChannels() == 1 && ipl.getNFrames() == 1) {
            timeStack = ipl.getStack();
        } else {
            int t = object.getT() + 1;
            int nSlices = ipl.getNSlices();
            timeStack = SubHyperstackMaker.makeSubhyperstack(ipl, "1-1", "1-" + nSlices, t + "-" + t).getStack();
        }

        textureCalculator.calculate(timeStack,object,(int) offs[0], (int) offs[1], (int) offs[2]);

        // Acquiring measurements
        String name = getFullName(image.getName(), Measurements.ASM,offs,calibratedOffset);
        object.addMeasurement(new Measurement(name,textureCalculator.getASM()));

        name = getFullName(image.getName(), Measurements.CONTRAST,offs,calibratedOffset);
        object.addMeasurement(new Measurement(name,textureCalculator.getContrast()));

        name = getFullName(image.getName(), Measurements.CORRELATION,offs,calibratedOffset);
        object.addMeasurement(new Measurement(name,textureCalculator.getCorrelation()));

        name = getFullName(image.getName(), Measurements.ENTROPY,offs,calibratedOffset);
        object.addMeasurement(new Measurement(name,textureCalculator.getEntropy()));

    }



    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Texture measures, largely from  Robert M. Haralick, K. Shanmugam, and Its'hak Dinstein, " +
                "\"Textural Features for Image Classification\", IEEE Transactions on Systems, Man, and Cybernetics, 1973, SMC-3 (6): 610–621";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // This requires an 8-bit image.  If the provided image isn't 8-bit, convert it
        if (inputImagePlus.getBitDepth() != 8) {
            MIA.log.writeWarning("Texture analysis requires an 8-bit image.  Converting to 8-bit with scaling enabled.");
            inputImagePlus = inputImagePlus.duplicate();
            ImageTypeConverter.process(inputImagePlus,8,ImageTypeConverter.ScalingModes.SCALE);
            inputImage = new Image(inputImage.getName(),inputImagePlus);
        }

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // If no objects were detected, skipping this module
        if (inputObjects.size() == 0) return Status.PASS;

        // Getting parameters
        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);

        double[] offs = new double[]{xOffsIn,yOffsIn,zOffsIn};

        // If using calibrated offset values, determining the closest pixel offset
        if (calibratedOffset) convertCalibratedOffsets(offs,inputObjects.getFirst());

        // Initialising the texture calculator
        TextureCalculator textureCalculator = new TextureCalculator();

        int nObjects = inputObjects.size();
        AtomicInteger iter = new AtomicInteger(0);
        for (Obj object:inputObjects.values()) {
                processObject(object, inputImage, textureCalculator, offs, calibratedOffset);
                writeProgressStatus(iter.incrementAndGet(), nObjects, "objects");
            }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));        

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new DoubleP(X_OFFSET, this,1d));
        parameters.add(new DoubleP(Y_OFFSET, this,0d));
        parameters.add(new DoubleP(Z_OFFSET, this,0d));
        parameters.add(new BooleanP(CALIBRATED_OFFSET, this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);
        double[] offs = new double[]{xOffsIn,yOffsIn,zOffsIn};

        String name = getFullName(inputImageName,Measurements.ASM,offs,calibratedOffset);
        ObjMeasurementRef asm = objectMeasurementRefs.getOrPut(name);
        asm.setObjectsName(inputObjectsName);
        returnedRefs.add(asm);

        name = getFullName(inputImageName,Measurements.CONTRAST,offs,calibratedOffset);
        ObjMeasurementRef contrast = objectMeasurementRefs.getOrPut(name);
        contrast.setObjectsName(inputObjectsName);
        returnedRefs.add(contrast);

        name = getFullName(inputImageName,Measurements.CORRELATION,offs,calibratedOffset);
        ObjMeasurementRef correlation = objectMeasurementRefs.getOrPut(name);
        correlation.setObjectsName(inputObjectsName);
        returnedRefs.add(correlation);

        name = getFullName(inputImageName,Measurements.ENTROPY,offs,calibratedOffset);
        ObjMeasurementRef entropy = objectMeasurementRefs.getOrPut(name);
        entropy.setObjectsName(inputObjectsName);
        returnedRefs.add(entropy);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}