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

    @Override
    public String getTitle() {
        return "Measure image texture";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting parameters
        int xOffs = parameters.getValue(X_OFFSET);
        int yOffs = parameters.getValue(Y_OFFSET);
        int zOffs = parameters.getValue(Z_OFFSET);

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Running texture measurement
        if (verbose) System.out.println("["+moduleName+"] Calculating co-occurance matrix");
        if (verbose) System.out.println("["+moduleName+"] X-offset: "+xOffs);
        if (verbose) System.out.println("["+moduleName+"] Y-offset: "+yOffs);
        if (verbose) System.out.println("["+moduleName+"] Z-offset: "+zOffs);

        TextureCalculator textureCalculator = new TextureCalculator();
        textureCalculator.calculate(inputImagePlus,xOffs,yOffs,zOffs);

        // Acquiring measurements
        HCMeasurement ASMMeasurement = new HCMeasurement("ASM",textureCalculator.getASM());
        ASMMeasurement.setSource(this);
        inputImage.addMeasurement(ASMMeasurement.getName(),ASMMeasurement);
        if (verbose) System.out.println("["+moduleName+"] ASM = "+ASMMeasurement.getValue());

        HCMeasurement contrastMeasurement = new HCMeasurement("CONTRAST",textureCalculator.getContrast());
        contrastMeasurement.setSource(this);
        inputImage.addMeasurement(contrastMeasurement.getName(),contrastMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Contrast = "+contrastMeasurement.getValue());

        HCMeasurement correlationMeasurement = new HCMeasurement("CORRELATION",textureCalculator.getCorrelation());
        correlationMeasurement.setSource(this);
        inputImage.addMeasurement(correlationMeasurement.getName(),correlationMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Correlation = "+correlationMeasurement.getValue());

        HCMeasurement entropyMeasurement = new HCMeasurement("ENTROPY",textureCalculator.getEntropy());
        entropyMeasurement.setSource(this);
        inputImage.addMeasurement(entropyMeasurement.getName(),entropyMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Entropy = "+entropyMeasurement.getValue());

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE, HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(X_OFFSET, HCParameter.INTEGER,1));
        parameters.addParameter(new HCParameter(Y_OFFSET, HCParameter.INTEGER,0));
        parameters.addParameter(new HCParameter(Z_OFFSET, HCParameter.INTEGER,0));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
