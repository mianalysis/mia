package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imagej.ops.Ops;
import net.imagej.ops.convert.normalizeScale.NormalizeScaleRealTypes;
import net.imglib2.converter.Converter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import sc.fiji.coloc.algorithms.MissingPreconditionException;
import sc.fiji.coloc.algorithms.PearsonsCorrelation;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Analysis.ColocalisationCalculator;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;


public class MeasureImageColocalisation extends Module {
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";
    public static final String MASKING_MODE = "Masking mode";
    public static final String MASK_IMAGE = "Mask image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASURE_PCC = "Measure PCC";
    public static final String PCC_IMPLEMENTATION = "PCC implementation";
//    public static final String MEASURE_MANDERS = "Measure Manders";
//    public static final String THRESHOLDING_MODE = "Thresholding mode";

    public MeasureImageColocalisation(ModuleCollection modules) {
        super("Measure image colocalisation",modules);
    }


    public interface MaskingModes {
        String NONE = "None";
        String MASK_IMAGE = "Mask using image";
        String MEASURE_INSIDE_OBJECTS = "Measure inside objects";
        String MEASURE_OUTSIDE_OBJECTS = "Measure outside objects";

        String[] ALL = new String[]{NONE,MASK_IMAGE,MEASURE_INSIDE_OBJECTS,MEASURE_OUTSIDE_OBJECTS};

    }

    public interface PCCImplementations {
        String CLASSIC = "Classic";
        String FAST = "Fast";

        String[] ALL = new String[]{CLASSIC,FAST};

    }

    public interface ThresholdingModes {
        String BISECTION = "Bisection (correlation)";
        String COSTES = "Costes (correlation)";

        String[] ALL = new String[]{BISECTION,COSTES};

    }

    public interface Measurements {
        String MEAN_PCC = "MEAN_PCC";
    }


    public static Image getMaskImage(ObjCollection objects, Image templateImage, String maskingMode) {
        switch (maskingMode) {
            case MaskingModes.MEASURE_INSIDE_OBJECTS:
                // Creating the new Obj
                HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objects,ColourFactory.SingleColours.WHITE);
                Image image = objects.convertToImage("Mask",templateImage,hues,8,false);
                InvertIntensity.process(image.getImagePlus());
                return image;

            case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
                // Creating the new Obj
                hues = ColourFactory.getSingleColourHues(objects,ColourFactory.SingleColours.WHITE);
                return objects.convertToImage("Mask",templateImage,hues,8,false);

            case MaskingModes.NONE:
                return null;

            default:
                return null;
        }
    }

    public static String getFullName(String imageName2, String measurement) {
        return "COLOCALISATION // "+imageName2+"_"+measurement;
    }

    public static void measurePCCOld(Image image1, Image image2, Image mask) {
        CumStat cs = new CumStat();
        ImagePlus ipl1 = image1.getImagePlus();
        ImagePlus ipl2 = image2.getImagePlus();
        ImagePlus maskIpl = mask == null ? null : mask.getImagePlus();

        // Iterating over all stacks
        for (int c=0;c<ipl1.getNChannels();c++) {
            for (int t=0;t<ipl1.getNFrames();t++) {
                ipl1.setPosition(c+1,1,t+1);
                ipl2.setPosition(c+1,1,t+1);

                if (mask == null) {
                    cs.addMeasure(ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack()));
                } else {
                    maskIpl.setPosition(c+1,1,t+1);
                    cs.addMeasure(ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack(),maskIpl.getStack()));
                }
            }
        }

        MIA.log.writeDebug("Old PCC method = "+cs.getMean());

        // Adding the measurement to the image
        String name = getFullName(image2.getName(),Measurements.MEAN_PCC);
        image1.addMeasurement(new Measurement(name,cs.getMean()));

    }

    public static PearsonsCorrelation.Implementation getPCCImplementation(String pccImplementationName) {
        switch (pccImplementationName) {
            case PCCImplementations.CLASSIC:
            default:
                return PearsonsCorrelation.Implementation.Classic;
            case PCCImplementations.FAST:
                return PearsonsCorrelation.Implementation.Fast;
        }
    }

    public static void measurePCC(Image image1, Image image2, Image maskImage, String pccImplementationName) {
        Img img1 = image1.getImgPlus();
        Img img2 = image2.getImgPlus();
        Img maskImg = maskImage == null ? null : maskImage.getImgPlus();

        // Converting mask to BitType
//        Img<BitType> maskImgBitType = ArrayImgs.bits(maskImg);
        ImageJ ij = new ImageJ();

//        NormalizeScaleRealTypes converter = new NormalizeScaleRealTypes();
//        converter.setEnvironment(ij.op());
//        converter.initialize();
//        converter.checkInput(maskImg);
//
//        Img<BitType> maskImgBitType = ij.op().create().img(maskImg,new BitType());
//        Img<BitType> maskImgBitTypeInv = ij.op().create().img(maskImgBitType,new BitType());
//        ij.op().run(Ops.Convert.ImageType.class, maskImgBitType, maskImg);
        Img<BitType> maskImgBitType = ij.op().convert().bit(maskImg);
        ImageJFunctions.show(maskImgBitType);
        IJ.runMacro("waitForUser");
        ij.op().image().invert(maskImgBitType,maskImgBitType);

        PearsonsCorrelation.Implementation implementation = getPCCImplementation(pccImplementationName);
        PearsonsCorrelation pcc = new PearsonsCorrelation(implementation);
        double pccValue = 0;
        try {
            if (maskImg == null) {
                pccValue = pcc.calculatePearsons(img1,img2);
            } else {
                pccValue = pcc.calculatePearsons(img1,img2,maskImgBitType);
            }
        } catch (MissingPreconditionException e) {
            e.printStackTrace();
        }

        MIA.log.writeDebug("Coloc2 PCC method = "+pccValue);

        // Adding the measurement to the image
        String name = getFullName(image2.getName(),Measurements.MEAN_PCC);
        image1.addMeasurement(new Measurement(name,pccValue));

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "Calculates PCC, averaged across all timepoints and channels.";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1);
        Image image1 = workspace.getImages().get(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2);
        Image image2 = workspace.getImages().get(imageName2);

        // Getting parameters
        String maskingMode = parameters.getValue(MASKING_MODE);
        String maskImageName = parameters.getValue(MASK_IMAGE);
        Image maskImage = workspace.getImage(maskImageName);
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);
        String pccImplementationName = parameters.getValue(PCC_IMPLEMENTATION);

        // If objects are to be used as a mask a binary image is created.  Otherwise, null is returned
        switch (maskingMode) {
            case MaskingModes.MEASURE_INSIDE_OBJECTS:
            case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
                maskImage = getMaskImage(objects,image1,maskingMode);
                break;
        }

        // Running measurements
        measurePCCOld(image1,image2,maskImage);
        measurePCC(image1,image2,maskImage,pccImplementationName);

        if (showOutput) image1.showMeasurements(this);
        if (showOutput) image2.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE_1,this));
        parameters.add(new InputImageP(INPUT_IMAGE_2,this));
        parameters.add(new ChoiceP(MASKING_MODE,this,MaskingModes.NONE,MaskingModes.ALL));
        parameters.add(new InputImageP(MASK_IMAGE,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new BooleanP(MEASURE_PCC,this,true));
        parameters.add(new ChoiceP(PCC_IMPLEMENTATION,this,PCCImplementations.CLASSIC,PCCImplementations.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(MASKING_MODE));
        switch ((String) parameters.getValue(MASKING_MODE)) {
            case MaskingModes.MASK_IMAGE:
                returnedParameters.add(parameters.getParameter(MASK_IMAGE));
                break;
            case MaskingModes.MEASURE_INSIDE_OBJECTS:
            case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASURE_PCC));
        if ((boolean) parameters.getValue(MEASURE_PCC)) {
            returnedParameters.add(parameters.getParameter(PCC_IMPLEMENTATION));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputImage1Name = parameters.getValue(INPUT_IMAGE_1);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE_2);

        if ((boolean) parameters.getValue(MEASURE_PCC)) {
            String name = getFullName(inputImage2Name, Measurements.MEAN_PCC);
            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
            reference.setImageName(inputImage1Name);
            returnedRefs.add(reference);
        }

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
