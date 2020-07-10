package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.Analysis.TextureCalculator;

/**
 * Created by Stephen on 09/05/2017.
 */
public class MeasureImageTexture extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";

    public MeasureImageTexture(ModuleCollection modules) {
        super("Measure image texture",modules);
    }

    public interface Measurements {
        String ASM = "TEXTURE // ASM";
        String CONTRAST = "TEXTURE // CONTRAST";
        String CORRELATION = "TEXTURE // CORRELATION";
        String ENTROPY = "TEXTURE // ENTROPY";

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
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
        inputImage.addMeasurement(ASMMeasurement);
        writeStatus("ASM = " + ASMMeasurement.getValue());

        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST, textureCalculator.getContrast());
        inputImage.addMeasurement(contrastMeasurement);
        writeStatus("Contrast = " + contrastMeasurement.getValue());

        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION, textureCalculator.getCorrelation());
        inputImage.addMeasurement(correlationMeasurement);
        writeStatus("Correlation = " + correlationMeasurement.getValue());

        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY, textureCalculator.getEntropy());
        inputImage.addMeasurement(entropyMeasurement);
        writeStatus("Entropy = " + entropyMeasurement.getValue());

        if (showOutput) inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new IntegerP(X_OFFSET, this,1));
        parameters.add(new IntegerP(Y_OFFSET, this,0));
        parameters.add(new IntegerP(Z_OFFSET, this,0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String imageName = parameters.getValue(INPUT_IMAGE);

        ImageMeasurementRef asm = imageMeasurementRefs.getOrPut(Measurements.ASM);
        asm.setImageName(imageName);
        returnedRefs.add(asm);

        ImageMeasurementRef contrast = imageMeasurementRefs.getOrPut(Measurements.CONTRAST);
        contrast.setImageName(imageName);
        returnedRefs.add(contrast);

        ImageMeasurementRef correlation = imageMeasurementRefs.getOrPut(Measurements.CORRELATION);
        correlation.setImageName(imageName);
        returnedRefs.add(correlation);

        ImageMeasurementRef entropy = imageMeasurementRefs.getOrPut(Measurements.ENTROPY);
        entropy.setImageName(imageName);
        returnedRefs.add(entropy);

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
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
