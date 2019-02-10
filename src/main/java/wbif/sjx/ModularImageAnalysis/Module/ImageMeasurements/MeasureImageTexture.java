package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.DoubleP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.IntegerP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
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
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET);
        int yOffs = parameters.getValue(Y_OFFSET);
        int zOffs = parameters.getValue(Z_OFFSET);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running texture measurement
        TextureCalculator textureCalculator = new TextureCalculator(xOffs,yOffs,zOffs);
        textureCalculator.calculate(inputImagePlus.getStack());

        // Acquiring measurements
        Measurement ASMMeasurement = new Measurement(Measurements.ASM, textureCalculator.getASM());
        ASMMeasurement.setSource(this);
        inputImage.addMeasurement(ASMMeasurement);
        writeMessage("ASM = " + ASMMeasurement.getValue());

        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST, textureCalculator.getContrast());
        contrastMeasurement.setSource(this);
        inputImage.addMeasurement(contrastMeasurement);
        writeMessage("Contrast = " + contrastMeasurement.getValue());

        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION, textureCalculator.getCorrelation());
        correlationMeasurement.setSource(this);
        inputImage.addMeasurement(correlationMeasurement);
        writeMessage("Correlation = " + correlationMeasurement.getValue());

        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY, textureCalculator.getEntropy());
        entropyMeasurement.setSource(this);
        inputImage.addMeasurement(entropyMeasurement);
        writeMessage("Entropy = " + entropyMeasurement.getValue());

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new DoubleP(X_OFFSET, this,1));
        parameters.add(new DoubleP(Y_OFFSET, this,0));
        parameters.add(new IntegerP(Z_OFFSET, this,0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        imageMeasurementRefs.setAllCalculated(false);

        String imageName = parameters.getValue(INPUT_IMAGE);

        MeasurementRef asm = imageMeasurementRefs.getOrPut(Measurements.ASM);
        asm.setImageObjName(imageName);
        asm.setCalculated(true);

        MeasurementRef contrast = imageMeasurementRefs.getOrPut(Measurements.CONTRAST);
        contrast.setImageObjName(imageName);
        contrast.setCalculated(true);

        MeasurementRef correlation = imageMeasurementRefs.getOrPut(Measurements.CORRELATION);
        correlation.setImageObjName(imageName);
        correlation.setCalculated(true);

        MeasurementRef entropy = imageMeasurementRefs.getOrPut(Measurements.ENTROPY);
        entropy.setImageObjName(imageName);
        entropy.setCalculated(true);

        return imageMeasurementRefs;

    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
