//package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;
//
//import ij.ImagePlus;
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.common.Analysis.TextureCalculator;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//
///**
// * Created by Stephen on 09/05/2017.
// */
//public class MeasureImageTexture extends HCModule {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String X_OFFSET = "X-offset";
//    public static final String Y_OFFSET = "Y-offset";
//    public static final String Z_OFFSET = "Z-offset";
//
//    private ImageObjReference inputImage;
//
//    private MeasurementReference asmMeasurement;
//    private MeasurementReference contrastMeasurement;
//    private MeasurementReference correlationMeasurement;
//    private MeasurementReference entropyMeasurement;
//
//    private interface Measurements {
//        String ASM = "TEXTURE//ASM";
//        String CONTRAST = "TEXTURE//CONTRAST";
//        String CORRELATION = "TEXTURE//CORRELATION";
//        String ENTROPY = "TEXTURE//ENTROPY";
//
//    }
//
//
//    @Override
//    public String getTitle() {
//        return "Measure image texture";
//
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    public void run(Workspace workspace, boolean verbose) {
//        // Getting parameters
//        int xOffs = parameters.getValue(X_OFFSET);
//        int yOffs = parameters.getValue(Y_OFFSET);
//        int zOffs = parameters.getValue(Z_OFFSET);
//
//        // Getting input image
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImages().get(inputImageName);
//        ImagePlus inputImagePlus = inputImage.getImagePlus();
//
//        // Running texture measurement
//        if (verbose) System.out.println("["+moduleName+"] Calculating co-occurance matrix");
//        if (verbose) System.out.println("["+moduleName+"] X-offset: "+xOffs);
//        if (verbose) System.out.println("["+moduleName+"] Y-offset: "+yOffs);
//        if (verbose) System.out.println("["+moduleName+"] Z-offset: "+zOffs);
//
//        TextureCalculator textureCalculator = new TextureCalculator();
//
//        for (int f=0;f<inputImagePlus.getNFrames();f++) {
//            textureCalculator.calculate(inputImagePlus, xOffs, yOffs, zOffs,1,f+1);
//
//        }
//
//        // Acquiring measurements
//        Measurement ASMMeasurement = new Measurement(Measurements.ASM,textureCalculator.getASM());
//        ASMMeasurement.setSource(this);
//        inputImage.addMeasurement(ASMMeasurement);
//        if (verbose) System.out.println("["+moduleName+"] ASM = "+ASMMeasurement.getValue());
//
//        Measurement contrastMeasurement = new Measurement(Measurements.CONTRAST,textureCalculator.getContrast());
//        contrastMeasurement.setSource(this);
//        inputImage.addMeasurement(contrastMeasurement);
//        if (verbose) System.out.println("["+moduleName+"] Contrast = "+contrastMeasurement.getValue());
//
//        Measurement correlationMeasurement = new Measurement(Measurements.CORRELATION,textureCalculator.getCorrelation());
//        correlationMeasurement.setSource(this);
//        inputImage.addMeasurement(correlationMeasurement);
//        if (verbose) System.out.println("["+moduleName+"] Correlation = "+correlationMeasurement.getValue());
//
//        Measurement entropyMeasurement = new Measurement(Measurements.ENTROPY,textureCalculator.getEntropy());
//        entropyMeasurement.setSource(this);
//        inputImage.addMeasurement(entropyMeasurement);
//        if (verbose) System.out.println("["+moduleName+"] Entropy = "+entropyMeasurement.getValue());
//
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(X_OFFSET, Parameter.INTEGER,1));
//        parameters.addParameter(new Parameter(Y_OFFSET, Parameter.INTEGER,0));
//        parameters.addParameter(new Parameter(Z_OFFSET, Parameter.INTEGER,0));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        return parameters;
//    }
//
//    @Override
//    public void initialiseImageReferences() {
//        inputImage = new ImageObjReference();
//        imageReferences.add(inputImage);
//
//        asmMeasurement = new MeasurementReference(Measurements.ASM);
//        contrastMeasurement = new MeasurementReference(Measurements.CONTRAST);
//        correlationMeasurement = new MeasurementReference(Measurements.CORRELATION);
//        entropyMeasurement = new MeasurementReference(Measurements.ENTROPY);
//        inputImage.addMeasurementReference(asmMeasurement);
//        inputImage.addMeasurementReference(contrastMeasurement);
//        inputImage.addMeasurementReference(correlationMeasurement);
//        inputImage.addMeasurementReference(entropyMeasurement);
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetImageReferences() {
//        // Updating image name
//        inputImage.setName(parameters.getValue(INPUT_IMAGE));
//
//        return imageReferences;
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetObjectReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
