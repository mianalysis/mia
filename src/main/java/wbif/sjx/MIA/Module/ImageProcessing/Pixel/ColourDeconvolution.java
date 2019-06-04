package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.RGBStackConverter;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import sc.fiji.colourDeconvolution.StainMatrix;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import java.util.LinkedHashMap;

public class ColourDeconvolution extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String ENABLE_IM1_OUTPUT = "Output image 1";
    public static final String OUTPUT_IMAGE_1 = "Output image 1 name";
    public static final String ENABLE_IM2_OUTPUT = "Output image 2";
    public static final String OUTPUT_IMAGE_2 = "Output image 2 name";
    public static final String ENABLE_IM3_OUTPUT = "Output image 3";
    public static final String OUTPUT_IMAGE_3 = "Output image 3 name";
    public static final String STAIN_MODEL = "Stain model";

    public ColourDeconvolution(ModuleCollection modules) {
        super("Colour deconvolution",modules);
    }


    public interface StainModels {
        String ALCIAN_BLUE_H = "Alcian blue & H";
        String AZAN_MALLORY = "Azan-Mallory";
        String BRILLIANT_BLUE = "Brilliant_Blue";
        String CMY = "CMY";
        String FLG = "Feulgen Light Green";
        String FR_FB_DAB = "FastRed FastBlue DAB";
        String GIEMSA = "Giemsa";
        String H_AEC = "H AEC";
        String H_AND_E = "H&E";
        String H_AND_E_2 = "H&E 2";
        String H_AND_E_DAB = "H&E DAB";
        String H_DAB = "H DAB";
        String H_PAS = "H PAS";
        String MASSON_TRICHROME = "Masson Trichrome";
        String METHYL_GREEN_DAB = "Methyl Green DAB";
        String RGB = "RGB";

        String[] ALL = new String[]{ALCIAN_BLUE_H,AZAN_MALLORY,BRILLIANT_BLUE,CMY,FR_FB_DAB,FLG,GIEMSA,H_AEC,H_AND_E,
                H_AND_E_2,H_AND_E_DAB,H_DAB,H_PAS,MASSON_TRICHROME,METHYL_GREEN_DAB,RGB};

    }


    public ImagePlus[] process(ImagePlus ipl, String[] outputImageNames, StainMatrix stainMatrix) {
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        // Creating the output image
        ImagePlus deconvolved1 = IJ.createHyperStack(outputImageNames[0],width,height,1,nSlices,nFrames,8);
        deconvolved1.setCalibration(ipl.getCalibration());

        ImagePlus deconvolved2 = IJ.createHyperStack(outputImageNames[1],width,height,1,nSlices,nFrames,8);
        deconvolved2.setCalibration(ipl.getCalibration());

        ImagePlus deconvolved3 = IJ.createHyperStack(outputImageNames[2],width,height,1,nSlices,nFrames,8);
        deconvolved3.setCalibration(ipl.getCalibration());

        // Iterating over all timepoints
        int count = 0;
        for (int t = 1; t <= nFrames; t++) {
            ImagePlus iplSingle = SubHyperstackMaker.makeSubhyperstack(ipl, "1-" + nChannels, "1-" + nSlices, t + "-" + t);
            RGBStackConverter.convertToRGB(iplSingle);

            ImageStack[] iplOut = stainMatrix.compute(false, true, iplSingle);

            for (int z = 1; z <= nSlices; z++) {
                deconvolved1.setPosition(1, z, t);
                deconvolved2.setPosition(1, z, t);
                deconvolved3.setPosition(1, z, t);

                ImageProcessor iprDeconvolved1 = deconvolved1.getProcessor();
                ImageProcessor iprDeconvolved2 = deconvolved2.getProcessor();
                ImageProcessor iprDeconvolved3 = deconvolved3.getProcessor();

                ImageProcessor iprOut1 = iplOut[0].getProcessor(z);
                ImageProcessor iprOut2 = iplOut[1].getProcessor(z);
                ImageProcessor iprOut3 = iplOut[2].getProcessor(z);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        iprDeconvolved1.setf(x, y, iprOut1.getf(x, y));
                        iprDeconvolved2.setf(x, y, iprOut2.getf(x, y));
                        iprDeconvolved3.setf(x, y, iprOut3.getf(x, y));
                    }
                }
            }

            writeMessage("Processed "+(++count)+" of "+nFrames+" stacks");

        }

        deconvolved1.setPosition(1,1,1);
        deconvolved2.setPosition(1,1,1);
        deconvolved3.setPosition(1,1,1);

        deconvolved1.updateChannelAndDraw();
        deconvolved2.updateChannelAndDraw();
        deconvolved3.updateChannelAndDraw();

        return new ImagePlus[]{deconvolved1,deconvolved2,deconvolved3};

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean outputImage1 = parameters.getValue(ENABLE_IM1_OUTPUT);
        String outputImageName1 = parameters.getValue(OUTPUT_IMAGE_1);
        boolean outputImage2 = parameters.getValue(ENABLE_IM2_OUTPUT);
        String outputImageName2 = parameters.getValue(OUTPUT_IMAGE_2);
        boolean outputImage3 = parameters.getValue(ENABLE_IM3_OUTPUT);
        String outputImageName3 = parameters.getValue(OUTPUT_IMAGE_3);
        String stainModel = parameters.getValue(STAIN_MODEL);

        // Running the deconvolution
        StainMatrix stainMatrix = getStainMatrices().get(stainModel);
        String[] outputImageNames = new String[]{outputImageName1,outputImageName2,outputImageName3};
        ImagePlus[] outputImagePluses = process(inputImagePlus,outputImageNames,stainMatrix);

        // If selected, displaying the image
        if (outputImage1) {
            Image outImage1 = new Image(outputImageName1,outputImagePluses[0]);
            workspace.addImage(outImage1);
            if (showOutput) outImage1.showImage();
        }

        if (outputImage2) {
            Image outImage2 = new Image(outputImageName2,outputImagePluses[1]);
            workspace.addImage(outImage2);
            if (showOutput) outImage2.showImage();
        }

        if (outputImage3) {
            Image outImage3 = new Image(outputImageName3,outputImagePluses[2]);
            workspace.addImage(outImage3);
            if (showOutput) outImage3.showImage();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(ENABLE_IM1_OUTPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE_1, this));
        parameters.add(new BooleanP(ENABLE_IM2_OUTPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE_2, this));
        parameters.add(new BooleanP(ENABLE_IM3_OUTPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE_3, this));
        parameters.add(new ChoiceP(STAIN_MODEL, this,StainModels.H_AND_E,StainModels.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(ENABLE_IM1_OUTPUT));
        if (parameters.getValue(ENABLE_IM1_OUTPUT)) returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_1));

        returnedParameters.add(parameters.getParameter(ENABLE_IM2_OUTPUT));
        if (parameters.getValue(ENABLE_IM2_OUTPUT)) returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(ENABLE_IM3_OUTPUT));
        if (parameters.getValue(ENABLE_IM3_OUTPUT)) returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_3));

        returnedParameters.add(parameters.getParameter(STAIN_MODEL));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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

    public static LinkedHashMap<String,StainMatrix> getStainMatrices() {
        LinkedHashMap<String,StainMatrix> matrices = new LinkedHashMap<>();

        StainMatrix sm = new StainMatrix();
        sm.setMODx(new double[]{0.644211,0.092789,0});
        sm.setMODy(new double[]{0.716556,0.954111,0});
        sm.setMODz(new double[]{0.266844,0.283111,0});
        matrices.put(StainModels.H_AND_E,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.49015734,0.04615336,0});
        sm.setMODy(new double[]{0.76897085,0.8420684,0});
        sm.setMODz(new double[]{0.41040173,0.5373925,0});
        matrices.put(StainModels.H_AND_E_2,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.65,0.268,0});
        sm.setMODy(new double[]{0.704,0.57,0});
        sm.setMODz(new double[]{0.286,0.776,0});
        matrices.put(StainModels.H_DAB,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.4642092,0.94705542,0});
        sm.setMODy(new double[]{0.83008335,0.25373821,0});
        sm.setMODz(new double[]{0.30827187,0.19650764,0});
        matrices.put(StainModels.FLG,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.834750233,0.092789,0});
        sm.setMODy(new double[]{0.513556283,0.954111,0});
        sm.setMODz(new double[]{0.196330403,0.283111,0});
        matrices.put(StainModels.GIEMSA,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.21393921,0.74890292,0.268});
        sm.setMODy(new double[]{0.85112669,0.60624161,0.57});
        sm.setMODz(new double[]{0.47794022,0.26731082,0.776});
        matrices.put(StainModels.FR_FB_DAB,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.98,0.268,0});
        sm.setMODy(new double[]{0.144316,0.57,0});
        sm.setMODz(new double[]{0.133146,0.776,0});
        matrices.put(StainModels.METHYL_GREEN_DAB,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.65,0.072,0.268});
        sm.setMODy(new double[]{0.704,0.99,0.57});
        sm.setMODz(new double[]{0.286,0.105,0.776});
        matrices.put(StainModels.H_AND_E_DAB,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.65,0.2743,0});
        sm.setMODy(new double[]{0.704,0.6796,0});
        sm.setMODz(new double[]{0.286,0.6803,0});
        matrices.put(StainModels.H_AEC,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.853033,0.09289875,0.10732849});
        sm.setMODy(new double[]{0.508733,0.8662008,0.36765403});
        sm.setMODz(new double[]{0.112656,0.49098468,0.9237484});
        matrices.put(StainModels.AZAN_MALLORY,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.7995107,0.09997159,0});
        sm.setMODy(new double[]{0.5913521,0.73738605,0});
        sm.setMODz(new double[]{0.10528667,0.6680326,0});
        matrices.put(StainModels.MASSON_TRICHROME,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.874622,0.552556,0});
        sm.setMODy(new double[]{0.457711,0.7544,0});
        sm.setMODz(new double[]{0.158256,0.353744,0});
        matrices.put(StainModels.ALCIAN_BLUE_H,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.644211,0.175411,0});
        sm.setMODy(new double[]{0.716556,0.972178,0});
        sm.setMODz(new double[]{0.266844,0.154589,0});
        matrices.put(StainModels.H_PAS,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0.31465548,0.383573,0.7433543});
        sm.setMODy(new double[]{0.6602395,0.5271141,0.51731443});
        sm.setMODz(new double[]{0.68196464,0.7583024,0.4240403});
        matrices.put(StainModels.BRILLIANT_BLUE,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{0,1,1});
        sm.setMODy(new double[]{1,0,1});
        sm.setMODz(new double[]{1,1,0});
        matrices.put(StainModels.RGB,sm);

        sm = new StainMatrix();
        sm.setMODx(new double[]{1,0,0});
        sm.setMODy(new double[]{0,1,0});
        sm.setMODz(new double[]{0,0,1});
        matrices.put(StainModels.CMY,sm);

        return matrices;

    }

}
