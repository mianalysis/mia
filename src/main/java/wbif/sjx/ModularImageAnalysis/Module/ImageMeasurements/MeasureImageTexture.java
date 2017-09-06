package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;

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

        HashMap<String,CumStat> textureMeasurements = new HashMap<>();
        textureMeasurements.put("ASM",new CumStat());
        textureMeasurements.put("CONTRAST",new CumStat());
        textureMeasurements.put("CORRELATION",new CumStat());
        textureMeasurements.put("ENTROPY",new CumStat());

        TextureCalculator textureCalculator = new TextureCalculator();

        for (int f=0;f<inputImagePlus.getNFrames();f++) {
            textureCalculator.calculate(inputImagePlus, xOffs, yOffs, zOffs,1,f+1);

        }

        // Acquiring measurements
        MIAMeasurement ASMMeasurement = new MIAMeasurement("ASM",textureCalculator.getASM());
        ASMMeasurement.setSource(this);
        inputImage.addMeasurement(ASMMeasurement.getName(),ASMMeasurement);
        if (verbose) System.out.println("["+moduleName+"] ASM = "+ASMMeasurement.getValue());

        MIAMeasurement contrastMeasurement = new MIAMeasurement("CONTRAST",textureCalculator.getContrast());
        contrastMeasurement.setSource(this);
        inputImage.addMeasurement(contrastMeasurement.getName(),contrastMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Contrast = "+contrastMeasurement.getValue());

        MIAMeasurement correlationMeasurement = new MIAMeasurement("CORRELATION",textureCalculator.getCorrelation());
        correlationMeasurement.setSource(this);
        inputImage.addMeasurement(correlationMeasurement.getName(),correlationMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Correlation = "+correlationMeasurement.getValue());

        MIAMeasurement entropyMeasurement = new MIAMeasurement("ENTROPY",textureCalculator.getEntropy());
        entropyMeasurement.setSource(this);
        inputImage.addMeasurement(entropyMeasurement.getName(),entropyMeasurement);
        if (verbose) System.out.println("["+moduleName+"] Entropy = "+entropyMeasurement.getValue());

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(X_OFFSET, Parameter.INTEGER,1));
        parameters.addParameter(new Parameter(Y_OFFSET, Parameter.INTEGER,0));
        parameters.addParameter(new Parameter(Z_OFFSET, Parameter.INTEGER,0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
