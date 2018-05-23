package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by Stephen on 09/05/2017.
 */
public class MeasureImageTexture extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";

    public interface Measurements {
        String ASM = "TEXTURE // ASM";
        String CONTRAST = "TEXTURE // CONTRAST";
        String CORRELATION = "TEXTURE // CORRELATION";
        String ENTROPY = "TEXTURE // ENTROPY";

    }


    @Override
    public String getTitle() {
        return "Measure image texture";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET);
        int yOffs = parameters.getValue(Y_OFFSET);
        int zOffs = parameters.getValue(Z_OFFSET);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running texture measurement
        writeMessage("Calculating co-occurance matrix");
        writeMessage("X-offset: "+xOffs);
        writeMessage("Y-offset: "+yOffs);
        writeMessage("Z-offset: "+zOffs);

        TextureCalculator textureCalculator = new TextureCalculator();

        for (int f=0;f<inputImagePlus.getNFrames();f++) {
            textureCalculator.calculate(inputImagePlus, xOffs, yOffs, zOffs,1,f+1);

        }

        // Acquiring measurements
        Measurement ASMMeasurement = new Measurement(Measurements.ASM,textureCalculator.getASM());
        ASMMeasurement.setSource(this);
        inputImage.addMeasurement(ASMMeasurement);
        writeMessage("ASM = "+ASMMeasurement.getValue());

        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST,textureCalculator.getContrast());
        contrastMeasurement.setSource(this);
        inputImage.addMeasurement(contrastMeasurement);
        writeMessage("Contrast = "+contrastMeasurement.getValue());

        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION,textureCalculator.getCorrelation());
        correlationMeasurement.setSource(this);
        inputImage.addMeasurement(correlationMeasurement);
        writeMessage("Correlation = "+correlationMeasurement.getValue());

        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY,textureCalculator.getEntropy());
        entropyMeasurement.setSource(this);
        inputImage.addMeasurement(entropyMeasurement);
        writeMessage("Entropy = "+entropyMeasurement.getValue());

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(X_OFFSET, Parameter.INTEGER,1));
        parameters.add(new Parameter(Y_OFFSET, Parameter.INTEGER,0));
        parameters.add(new Parameter(Z_OFFSET, Parameter.INTEGER,0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        imageMeasurementReferences.setAllCalculated(false);

        String imageName = parameters.getValue(INPUT_IMAGE);

        MeasurementReference asm = imageMeasurementReferences.getOrPut(Measurements.ASM);
        asm.setImageObjName(imageName);
        asm.setCalculated(true);

        MeasurementReference contrast = imageMeasurementReferences.getOrPut(Measurements.CONTRAST);
        contrast.setImageObjName(imageName);
        contrast.setCalculated(true);

        MeasurementReference correlation = imageMeasurementReferences.getOrPut(Measurements.CORRELATION);
        correlation.setImageObjName(imageName);
        correlation.setCalculated(true);

        MeasurementReference entropy = imageMeasurementReferences.getOrPut(Measurements.ENTROPY);
        entropy.setImageObjName(imageName);
        entropy.setCalculated(true);

        return imageMeasurementReferences;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
