package io.github.mianalysis.mia.module.images.process;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.RGBStackConverter;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import sc.fiji.colourDeconvolution.StainMatrix;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ColourDeconvolution extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String ENABLE_IM1_OUTPUT = "Output image 1";
    public static final String OUTPUT_IMAGE_1 = "Output image 1 name";
    public static final String ENABLE_IM2_OUTPUT = "Output image 2";
    public static final String OUTPUT_IMAGE_2 = "Output image 2 name";
    public static final String ENABLE_IM3_OUTPUT = "Output image 3";
    public static final String OUTPUT_IMAGE_3 = "Output image 3 name";

    public static final String DECONVOLUTION_SEPARATOR = "Deconvolution controls";
    public static final String STAIN_MODEL = "Stain model";
    public static final String R1 = "Stain 1 (red)";
    public static final String G1 = "Stain 1 (green)";
    public static final String B1 = "Stain 1 (blue)";
    public static final String R2 = "Stain 2 (red)";
    public static final String G2 = "Stain 2 (green)";
    public static final String B2 = "Stain 2 (blue)";
    public static final String R3 = "Stain 3 (red)";
    public static final String G3 = "Stain 3 (green)";
    public static final String B3 = "Stain 3 (blue)";

    public ColourDeconvolution(Modules modules) {
        super("Colour deconvolution", modules);
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
        String CUSTOM = "Custom (user values)";

        String[] ALL = new String[] { ALCIAN_BLUE_H, AZAN_MALLORY, BRILLIANT_BLUE, CMY, FR_FB_DAB, FLG, GIEMSA, H_AEC,
                H_AND_E, H_AND_E_2, H_AND_E_DAB, H_DAB, H_PAS, MASSON_TRICHROME, METHYL_GREEN_DAB, RGB, CUSTOM };

    }

    public static StainMatrix getCustomStainMatrix(double r1, double g1, double b1, double r2, double g2, double b2,
            double r3, double g3, double b3) {
        StainMatrix sm = new StainMatrix();
        sm.setMODx(new double[] { r1, r2, r3 });
        sm.setMODy(new double[] { g1, g2, g3 });
        sm.setMODz(new double[] { b1, b2, b3 });

        return sm;

    }

    public static ImagePlus[] process(ImagePlus ipl, String[] outputImageNames, StainMatrix stainMatrix) {
        String moduleName = new ColourDeconvolution(null).getName();

        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nChannels = ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();

        // Creating the output image
        ImagePlus deconvolved1 = IJ.createHyperStack(outputImageNames[0], width, height, 1, nSlices, nFrames, 8);
        deconvolved1.setCalibration(ipl.getCalibration());

        ImagePlus deconvolved2 = IJ.createHyperStack(outputImageNames[1], width, height, 1, nSlices, nFrames, 8);
        deconvolved2.setCalibration(ipl.getCalibration());

        ImagePlus deconvolved3 = IJ.createHyperStack(outputImageNames[2], width, height, 1, nSlices, nFrames, 8);
        deconvolved3.setCalibration(ipl.getCalibration());

        // Iterating over all timepoints
        int count = 0;
        for (int t = 1; t <= nFrames; t++) {
            ImagePlus iplSingle = SubHyperstackMaker.makeSubhyperstack(ipl, "1-" + nChannels, "1-" + nSlices,
                    t + "-" + t);

            // If not already an RGB image, convert to one
            if (iplSingle.getBitDepth() != 24)
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

            writeProgressStatus(++count, nFrames, "stacks", moduleName);

        }

        deconvolved1.setPosition(1, 1, 1);
        deconvolved2.setPosition(1, 1, 1);
        deconvolved3.setPosition(1, 1, 1);

        deconvolved1.updateChannelAndDraw();
        deconvolved2.updateChannelAndDraw();
        deconvolved3.updateChannelAndDraw();

        return new ImagePlus[] { deconvolved1, deconvolved2, deconvolved3 };

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Applies the <a href=\"https://imagej.net/plugins/colour-deconvolution\">Colour Deconvolution</a> plugin to unmix an RGB image (stored in the workspace) into up to three separate channels, which are output as separate images.  The input image can be stored as either an RGB or composite image.  This process is only applicable to images created via subtractive mixing (e.g. histological staining), not to additive methods (e.g. fluorescence).";
    }

    @Override
    public Status process(Workspace workspace) {
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
        double r1 = parameters.getValue(R1);
        double g1 = parameters.getValue(G1);
        double b1 = parameters.getValue(B1);
        double r2 = parameters.getValue(R2);
        double g2 = parameters.getValue(G2);
        double b2 = parameters.getValue(B2);
        double r3 = parameters.getValue(R3);
        double g3 = parameters.getValue(G3);
        double b3 = parameters.getValue(B3);

        // Running the deconvolution
        StainMatrix stainMatrix;
        if (stainModel.equals(StainModels.CUSTOM))
            stainMatrix = getCustomStainMatrix(r1, g1, b1, r2, g2, b2, r3, g3, b3);
        else
            stainMatrix = getStainMatrices().get(stainModel);

        String[] outputImageNames = new String[] { outputImageName1, outputImageName2, outputImageName3 };
        ImagePlus[] outputImagePluses = process(inputImagePlus, outputImageNames, stainMatrix);

        // If selected, displaying the image
        if (outputImage1) {
            Image outImage1 = new Image(outputImageName1, outputImagePluses[0]);
            workspace.addImage(outImage1);
            if (showOutput)
                outImage1.showImage();
        }

        if (outputImage2) {
            Image outImage2 = new Image(outputImageName2, outputImagePluses[1]);
            workspace.addImage(outImage2);
            if (showOutput)
                outImage2.showImage();
        }

        if (outputImage3) {
            Image outImage3 = new Image(outputImageName3, outputImagePluses[2]);
            workspace.addImage(outImage3);
            if (showOutput)
                outImage3.showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_IM1_OUTPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE_1, this));
        parameters.add(new BooleanP(ENABLE_IM2_OUTPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE_2, this));
        parameters.add(new BooleanP(ENABLE_IM3_OUTPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE_3, this));

        parameters.add(new SeparatorP(DECONVOLUTION_SEPARATOR, this));
        parameters.add(new ChoiceP(STAIN_MODEL, this, StainModels.H_AND_E, StainModels.ALL));
        parameters.add(new DoubleP(R1, this, 0d));
        parameters.add(new DoubleP(G1, this, 0d));
        parameters.add(new DoubleP(B1, this, 0d));
        parameters.add(new DoubleP(R2, this, 0d));
        parameters.add(new DoubleP(G2, this, 0d));
        parameters.add(new DoubleP(B2, this, 0d));
        parameters.add(new DoubleP(R3, this, 0d));
        parameters.add(new DoubleP(G3, this, 0d));
        parameters.add(new DoubleP(B3, this, 0d));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_IM1_OUTPUT));
        if ((boolean) parameters.getValue(ENABLE_IM1_OUTPUT))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_1));

        returnedParameters.add(parameters.getParameter(ENABLE_IM2_OUTPUT));
        if ((boolean) parameters.getValue(ENABLE_IM2_OUTPUT))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(ENABLE_IM3_OUTPUT));
        if ((boolean) parameters.getValue(ENABLE_IM3_OUTPUT))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_3));

        returnedParameters.add(parameters.get(DECONVOLUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STAIN_MODEL));

        switch ((String) parameters.getValue(STAIN_MODEL)) {
            case StainModels.CUSTOM:
                returnedParameters.add(parameters.getParameter(R1));
                returnedParameters.add(parameters.getParameter(G1));
                returnedParameters.add(parameters.getParameter(B1));
                returnedParameters.add(parameters.getParameter(R2));
                returnedParameters.add(parameters.getParameter(G2));
                returnedParameters.add(parameters.getParameter(B2));
                returnedParameters.add(parameters.getParameter(R3));
                returnedParameters.add(parameters.getParameter(G3));
                returnedParameters.add(parameters.getParameter(B3));

                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace to apply unmixing to.  This can be stored as either an RGB or composite image.");

        parameters.get(ENABLE_IM1_OUTPUT).setDescription(
                "When selected, the first stain in the stain matrix will be output to the workspace with the name specified by \""
                        + OUTPUT_IMAGE_1 + "\"");

        parameters.get(OUTPUT_IMAGE_1)
                .setDescription("Name to assign to first stain image, if output to the workspace.");

        parameters.get(ENABLE_IM2_OUTPUT).setDescription(
                "When selected, the second stain in the stain matrix will be output to the workspace with the name specified by \""
                        + OUTPUT_IMAGE_2 + "\"");

        parameters.get(OUTPUT_IMAGE_2)
                .setDescription("Name to assign to second stain image, if output to the workspace.");

        parameters.get(ENABLE_IM3_OUTPUT).setDescription(
                "When selected, the third stain in the stain matrix will be output to the workspace with the name specified by \""
                        + OUTPUT_IMAGE_3 + "\"");

        parameters.get(OUTPUT_IMAGE_3)
                .setDescription("Name to assign to third stain image, if output to the workspace.");

        parameters.get(STAIN_MODEL)
                .setDescription("Stain models to apply to input image.  If set to \"" + StainModels.CUSTOM
                        + "\" the individual RGB components for each channel can be specified.  Model choices are: "
                        + String.join(",", StainModels.ALL));

        parameters.get(R1).setDescription("Red component of stain 1.  Value specified in range 0-1.");

        parameters.get(G1).setDescription("Green component of stain 1.  Value specified in range 0-1.");

        parameters.get(B1).setDescription("Blue component of stain 1.  Value specified in range 0-1.");

        parameters.get(R2).setDescription("Red component of stain 2.  Value specified in range 0-1.");

        parameters.get(G2).setDescription("Green component of stain 2.  Value specified in range 0-1.");

        parameters.get(B2).setDescription("Blue component of stain 2.  Value specified in range 0-1.");

        parameters.get(R3).setDescription("Red component of stain 3.  Value specified in range 0-1.");

        parameters.get(G3).setDescription("Green component of stain 3.  Value specified in range 0-1.");

        parameters.get(B3).setDescription("Blue component of stain 3.  Value specified in range 0-1.");

    }

    public static LinkedHashMap<String, StainMatrix> getStainMatrices() {
        LinkedHashMap<String, StainMatrix> matrices = new LinkedHashMap<>();

        StainMatrix sm = new StainMatrix();
        sm.setMODx(new double[] { 0.644211, 0.092789, 0 });
        sm.setMODy(new double[] { 0.716556, 0.954111, 0 });
        sm.setMODz(new double[] { 0.266844, 0.283111, 0 });
        matrices.put(StainModels.H_AND_E, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.49015734, 0.04615336, 0 });
        sm.setMODy(new double[] { 0.76897085, 0.8420684, 0 });
        sm.setMODz(new double[] { 0.41040173, 0.5373925, 0 });
        matrices.put(StainModels.H_AND_E_2, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.65, 0.268, 0 });
        sm.setMODy(new double[] { 0.704, 0.57, 0 });
        sm.setMODz(new double[] { 0.286, 0.776, 0 });
        matrices.put(StainModels.H_DAB, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.4642092, 0.94705542, 0 });
        sm.setMODy(new double[] { 0.83008335, 0.25373821, 0 });
        sm.setMODz(new double[] { 0.30827187, 0.19650764, 0 });
        matrices.put(StainModels.FLG, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.834750233, 0.092789, 0 });
        sm.setMODy(new double[] { 0.513556283, 0.954111, 0 });
        sm.setMODz(new double[] { 0.196330403, 0.283111, 0 });
        matrices.put(StainModels.GIEMSA, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.21393921, 0.74890292, 0.268 });
        sm.setMODy(new double[] { 0.85112669, 0.60624161, 0.57 });
        sm.setMODz(new double[] { 0.47794022, 0.26731082, 0.776 });
        matrices.put(StainModels.FR_FB_DAB, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.98, 0.268, 0 });
        sm.setMODy(new double[] { 0.144316, 0.57, 0 });
        sm.setMODz(new double[] { 0.133146, 0.776, 0 });
        matrices.put(StainModels.METHYL_GREEN_DAB, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.65, 0.072, 0.268 });
        sm.setMODy(new double[] { 0.704, 0.99, 0.57 });
        sm.setMODz(new double[] { 0.286, 0.105, 0.776 });
        matrices.put(StainModels.H_AND_E_DAB, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.65, 0.2743, 0 });
        sm.setMODy(new double[] { 0.704, 0.6796, 0 });
        sm.setMODz(new double[] { 0.286, 0.6803, 0 });
        matrices.put(StainModels.H_AEC, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.853033, 0.09289875, 0.10732849 });
        sm.setMODy(new double[] { 0.508733, 0.8662008, 0.36765403 });
        sm.setMODz(new double[] { 0.112656, 0.49098468, 0.9237484 });
        matrices.put(StainModels.AZAN_MALLORY, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.7995107, 0.09997159, 0 });
        sm.setMODy(new double[] { 0.5913521, 0.73738605, 0 });
        sm.setMODz(new double[] { 0.10528667, 0.6680326, 0 });
        matrices.put(StainModels.MASSON_TRICHROME, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.874622, 0.552556, 0 });
        sm.setMODy(new double[] { 0.457711, 0.7544, 0 });
        sm.setMODz(new double[] { 0.158256, 0.353744, 0 });
        matrices.put(StainModels.ALCIAN_BLUE_H, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.644211, 0.175411, 0 });
        sm.setMODy(new double[] { 0.716556, 0.972178, 0 });
        sm.setMODz(new double[] { 0.266844, 0.154589, 0 });
        matrices.put(StainModels.H_PAS, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0.31465548, 0.383573, 0.7433543 });
        sm.setMODy(new double[] { 0.6602395, 0.5271141, 0.51731443 });
        sm.setMODz(new double[] { 0.68196464, 0.7583024, 0.4240403 });
        matrices.put(StainModels.BRILLIANT_BLUE, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 0, 1, 1 });
        sm.setMODy(new double[] { 1, 0, 1 });
        sm.setMODz(new double[] { 1, 1, 0 });
        matrices.put(StainModels.RGB, sm);

        sm = new StainMatrix();
        sm.setMODx(new double[] { 1, 0, 0 });
        sm.setMODy(new double[] { 0, 1, 0 });
        sm.setMODz(new double[] { 0, 0, 1 });
        matrices.put(StainModels.CMY, sm);

        return matrices;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
