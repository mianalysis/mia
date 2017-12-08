package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by Stephen on 09/05/2017.
 */
public class MeasureImageTexture extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";

    private interface Measurements {
        String ASM = "TEXTURE//ASM";
        String CONTRAST = "TEXTURE//CONTRAST";
        String CORRELATION = "TEXTURE//CORRELATION";
        String ENTROPY = "TEXTURE//ENTROPY";

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
    public void run(Workspace workspace, boolean verbose) {
        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET);
        int yOffs = parameters.getValue(Y_OFFSET);
        int zOffs = parameters.getValue(Z_OFFSET);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running texture measurement
        if (verbose) System.out.println("["+moduleName+"] Calculating co-occurance matrix");
        if (verbose) System.out.println("["+moduleName+"] X-offset: "+xOffs);
        if (verbose) System.out.println("["+moduleName+"] Y-offset: "+yOffs);
        if (verbose) System.out.println("["+moduleName+"] Z-offset: "+zOffs);

        TextureCalculator textureCalculator = new TextureCalculator();

        for (int f=0;f<inputImagePlus.getNFrames();f++) {
            textureCalculator.calculate(inputImagePlus, xOffs, yOffs, zOffs,1,f+1);

        }

        // Acquiring measurements
        Measurement ASMMeasurement = new Measurement(Measurements.ASM,textureCalculator.getASM());
        ASMMeasurement.setSource(this);
        inputImage.addMeasurement(ASMMeasurement);
        if (verbose) System.out.println("["+moduleName+"] ASM = "+ASMMeasurement.getValue());

        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST,textureCalculator.getContrast());
        contrastMeasurement.setSource(this);
        inputImage.addMeasurement(contrastMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Contrast = "+contrastMeasurement.getValue());

        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION,textureCalculator.getCorrelation());
        correlationMeasurement.setSource(this);
        inputImage.addMeasurement(correlationMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Correlation = "+correlationMeasurement.getValue());

        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY,textureCalculator.getEntropy());
        entropyMeasurement.setSource(this);
        inputImage.addMeasurement(entropyMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Entropy = "+entropyMeasurement.getValue());

    }

    @Override
    public ParameterCollection initialiseParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        returnedParameters.addParameter(new Parameter(X_OFFSET, Parameter.INTEGER,1));
        returnedParameters.addParameter(new Parameter(Y_OFFSET, Parameter.INTEGER,0));
        returnedParameters.addParameter(new Parameter(Z_OFFSET, Parameter.INTEGER,0));

        return returnedParameters;

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    protected MeasurementReferenceCollection initialiseImageMeasurementReferences() {
        MeasurementReferenceCollection references = new MeasurementReferenceCollection();

        references.add(new MeasurementReference(Measurements.ASM));
        references.add(new MeasurementReference(Measurements.CONTRAST));
        references.add(new MeasurementReference(Measurements.CORRELATION));
        references.add(new MeasurementReference(Measurements.ENTROPY));

        return references;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        String imageName = parameters.getValue(INPUT_IMAGE);

        MeasurementReference asm = imageMeasurementReferences.get(Measurements.ASM);
        asm.setImageObjName(imageName);

        MeasurementReference contrast = imageMeasurementReferences.get(Measurements.CONTRAST);
        contrast.setImageObjName(imageName);

        MeasurementReference correlation = imageMeasurementReferences.get(Measurements.CORRELATION);
        correlation.setImageObjName(imageName);

        MeasurementReference entropy = imageMeasurementReferences.get(Measurements.ENTROPY);
        entropy.setImageObjName(imageName);

        return imageMeasurementReferences;

    }

    @Override
    protected MeasurementReferenceCollection initialiseObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
