package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.RGBStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetDisplayRange;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
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
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.thirdparty.Colour_Deconvolution2;

/**
 * Applies the <a href="https://imagej.net/plugins/colour-deconvolution">Colour
 * Deconvolution</a> plugin to unmix an RGB image (stored in the workspace) into
 * up to three separate channels, which are output as separate images. The input
 * image can be stored as either an RGB or composite image. This process is only
 * applicable to images created via subtractive mixing (e.g. histological
 * staining), not to additive methods (e.g. fluorescence).
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ColourDeconvolution extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input";

    /**
     * Image from the workspace to apply unmixing to. This can be stored as either
     * an RGB or composite image.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Image output";

    /**
    * 
    */
    public static final String OUTPUT_TYPE = "Output type";

    /**
     * When selected, the first stain in the stain matrix will be output to the
     * workspace with the name specified by "Output image 1 name"
     */
    public static final String ENABLE_IM1_OUTPUT = "Output image 1";

    /**
     * Name to assign to first stain image, if output to the workspace.
     */
    public static final String OUTPUT_IMAGE_1 = "Output image 1 name";

    /**
     * When selected, the second stain in the stain matrix will be output to the
     * workspace with the name specified by "Output image 2 name"
     */
    public static final String ENABLE_IM2_OUTPUT = "Output image 2";

    /**
     * Name to assign to second stain image, if output to the workspace.
     */
    public static final String OUTPUT_IMAGE_2 = "Output image 2 name";

    /**
     * When selected, the third stain in the stain matrix will be output to the
     * workspace with the name specified by "Output image 3 name"
     */
    public static final String ENABLE_IM3_OUTPUT = "Output image 3";

    /**
     * Name to assign to third stain image, if output to the workspace.
     */
    public static final String OUTPUT_IMAGE_3 = "Output image 3 name";

    /**
    * 
    */
    public static final String DECONVOLUTION_SEPARATOR = "Deconvolution controls";

    /**
     * Stain models to apply to input image. If set to "Custom (user values)" the
     * individual RGB components for each channel can be specified. Model choices
     * are: Alcian blue and H,Azan-Mallory,Brilliant_Blue,CMY,FastRed FastBlue
     * DAB,Feulgen Light Green,Giemsa,H AEC,HandE,HandE 2,HandE DAB,H DAB,H
     * PAS,Masson Trichrome,Methyl Green DAB,RGB,Custom (user values)
     */
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

    public static final String CROSS_PRODUCT_FOR_COLOUR_3 = "Cross product for colour 3";

    public ColourDeconvolution(Modules modules) {
        super("Colour deconvolution", modules);
    }

    public interface OutputTypes {
        String ABSORBANCE_32 = "32-bit absorbance";
        String TRANSMITTANCE_8 = "8-bit transmittance";
        String TRANSMITTANCE_32 = "32-bit transmittance";

        String[] ALL = new String[] { ABSORBANCE_32, TRANSMITTANCE_8, TRANSMITTANCE_32 };

    }

    public interface StainModels {
        String ALCIAN_BLUE_H = "Alcian Blue & H";
        String ASTRABLUE_FUCHSIN = "AstraBlue Fuchsin";
        String AZAN_MALLORY = "Azan-Mallory";
        String BRILLIANT_BLUE = "Brilliant Blue";
        String CMY = "CMY";
        String FR_FB_DAB = "FastRed FastBlue DAB";
        String FLG = "Feulgen LightGreen";
        String GIEMSA = "Giemsa";
        String H_AEC = "H AEC";
        String H_AND_E = "H&E";
        String H_AND_E_2 = "H&E 2";
        String H_DAB = "H DAB";
        String H_AND_E_DAB = "H&E DAB";
        String H_DAB_FUCHSIN = "H DAB NewFuchsin";
        String H_PAS = "H PAS";
        String H_HRP_GREEN_FUCHSIN = "H HRP-Green NewFuchsin";
        String MASSON_TRICHROME = "Masson Trichrome";
        String METHYL_GREEN_DAB = "Methyl Green DAB";
        String NBT = "NBT/BCIP Red Counterstain II";
        String RGB = "RGB";
        String CUSTOM = "Custom (user values)";

        String[] ALL = new String[] { ALCIAN_BLUE_H, ASTRABLUE_FUCHSIN, AZAN_MALLORY, BRILLIANT_BLUE, CMY, FR_FB_DAB,
                FLG, GIEMSA, H_AEC, H_AND_E, H_AND_E_2, H_DAB, H_AND_E_DAB, H_DAB_FUCHSIN, H_PAS, H_HRP_GREEN_FUCHSIN,
                MASSON_TRICHROME, METHYL_GREEN_DAB, NBT, RGB, CUSTOM };

    }

    public static double[][] getStainVectors(String myStain) {
        double[] MODx = new double[3];
        double[] MODy = new double[3];
        double[] MODz = new double[3];

        // Stains are defined after this line ------------------------
        if (myStain.equals(StainModels.H_AND_E)) {
            // GL Haem matrix
            MODx[0] = 0.644211;
            MODy[0] = 0.716556;
            MODz[0] = 0.266844;
            // GL Eos matrix
            MODx[1] = 0.092789;
            MODy[1] = 0.954111;
            MODz[1] = 0.283111;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.H_AND_E_2)) {
            // GL Haem matrix
            MODx[0] = 0.49015734;
            MODy[0] = 0.76897085;
            MODz[0] = 0.41040173;
            // GL Eos matrix
            MODx[1] = 0.04615336;
            MODy[1] = 0.8420684;
            MODz[1] = 0.5373925;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.H_DAB)) {
            // Haem matrix
            MODx[0] = 0.650;
            MODy[0] = 0.704;
            MODz[0] = 0.286;
            // DAB matrix 3,3-diamino-benzidine tetrahydrochloride
            MODx[1] = 0.268;
            MODy[1] = 0.570;
            MODz[1] = 0.776;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.NBT)) {
            // Used in HR-HPV ISH (INFORM HPV III, Roche Ventana)
            // NBT.BCIP
            MODx[0] = 0.62302786;
            MODy[0] = 0.697869;
            MODz[0] = 0.3532918;
            // Red Counterstain II
            MODx[1] = 0.073615186;
            MODy[1] = 0.79345673;
            MODz[1] = 0.6041582;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.H_DAB_FUCHSIN)) {
            // mutilple immunostains labelling from J Isola's lab
            // Haematoxylin
            MODx[0] = 0.5625407925;
            MODy[0] = 0.70450559;
            MODz[0] = 0.4308375625;
            // DAB
            MODx[1] = 0.26503363;
            MODy[1] = 0.68898016;
            MODz[1] = 0.674584;
            // NewFuchsin
            MODx[2] = 0.0777851125;
            MODy[2] = 0.804293475;
            MODz[2] = 0.5886050475;
        }

        if (myStain.equals(StainModels.H_HRP_GREEN_FUCHSIN)) {
            // mutilple immunostains labelling from J Isola's lab
            // HRP-Green
            MODx[0] = 0.8098939567;
            MODy[0] = 0.4488181033;
            MODz[0] = 0.3714423567;
            // NewFuchsin
            MODx[1] = 0.0777851125;
            MODy[1] = 0.804293475;
            MODz[1] = 0.5886050475;
            // Zero matrix
            MODx[2] = 0;
            MODy[2] = 0;
            MODz[2] = 0;
        }

        if (myStain.equals(StainModels.FLG)) {
            // Feulgen
            MODx[0] = 0.46420921;
            MODy[0] = 0.83008335;
            MODz[0] = 0.30827187;
            // Light Green
            MODx[1] = 0.94705542;
            MODy[1] = 0.25373821;
            MODz[1] = 0.19650764;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.GIEMSA)) {
            // GL Methylene Blue
            MODx[0] = 0.834750233;
            MODy[0] = 0.513556283;
            MODz[0] = 0.196330403;
            // GL Eosin matrix
            MODx[1] = 0.092789;
            MODy[1] = 0.954111;
            MODz[1] = 0.283111;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.FR_FB_DAB)) {
            // GL Fast red
            MODx[0] = 0.21393921;
            MODy[0] = 0.85112669;
            MODz[0] = 0.47794022;
            // GL Fast blue
            MODx[1] = 0.74890292;
            MODy[1] = 0.60624161;
            MODz[1] = 0.26731082;
            // DAB
            MODx[2] = 0.268;
            MODy[2] = 0.570;
            MODz[2] = 0.776;
        }

        if (myStain.equals(StainModels.METHYL_GREEN_DAB)) {
            // GL Methyl Green matrix
            MODx[0] = 0.98003;
            MODy[0] = 0.144316;
            MODz[0] = 0.133146;
            // DAB matrix
            MODx[1] = 0.268;
            MODy[1] = 0.570;
            MODz[1] = 0.776;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.H_AND_E_DAB)) {
            // Haematoxylin matrix
            MODx[0] = 0.650;
            MODy[0] = 0.704;
            MODz[0] = 0.286;
            // Eosin matrix
            MODx[1] = 0.072;
            MODy[1] = 0.990;
            MODz[1] = 0.105;
            // DAB matrix
            MODx[2] = 0.268;
            MODy[2] = 0.570;
            MODz[2] = 0.776;
        }

        if (myStain.equals(StainModels.H_AEC)) {
            // Haematoxylin matrix
            MODx[0] = 0.650;
            MODy[0] = 0.704;
            MODz[0] = 0.286;
            // AEC 3-amino-9-ethylcarbazole
            MODx[1] = 0.2743;
            MODy[1] = 0.6796;
            MODz[1] = 0.6803;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.AZAN_MALLORY)) {
            // Azocarmine and Aniline Blue (AZAN)
            // GL Aniline Blue
            MODx[0] = 0.853033;
            MODy[0] = 0.508733;
            MODz[0] = 0.112656;
            // GL Azocarmine
            MODx[1] = 0.09289875;
            MODy[1] = 0.8662008;
            MODz[1] = 0.49098468;
            // GL Orange-G
            MODx[2] = 0.10732849;
            MODy[2] = 0.36765403;
            MODz[2] = 0.9237484;
        }

        if (myStain.equals(StainModels.MASSON_TRICHROME)) {
            // GL Methyl blue
            MODx[0] = 0.7995107;
            MODy[0] = 0.5913521;
            MODz[0] = 0.10528667;
            // GL Ponceau-Fuchsin has 2 hues, really this is only approximate
            MODx[1] = 0.09997159;
            MODy[1] = 0.73738605;
            MODz[1] = 0.6680326;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
            // GL Iron Haematoxylin, but this does not seem to work well because it gets
            // confused with the other 2 components
            // MODx[2]=0.6588232; MODy[2]=0.66414213; MODz[2]=0.3533655;
        }

        if (myStain.equals(StainModels.ALCIAN_BLUE_H)) {
            // GL Alcian Blue matrix
            MODx[0] = 0.874622;
            MODy[0] = 0.457711;
            MODz[0] = 0.158256;
            // GL Haematox after PAS matrix
            MODx[1] = 0.552556;
            MODy[1] = 0.7544;
            MODz[1] = 0.353744;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.H_PAS)) {
            // GL Haem matrix
            MODx[0] = 0.644211;
            MODy[0] = 0.716556;
            MODz[0] = 0.266844;
            // GL PAS matrix
            MODx[1] = 0.175411;
            MODy[1] = 0.972178;
            MODz[1] = 0.154589;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.BRILLIANT_BLUE)) {
            MODx[0] = 0.31465548;
            MODy[0] = 0.6602395;
            MODz[0] = 0.68196464;

            MODx[1] = 0.383573;
            MODy[1] = 0.5271141;
            MODz[1] = 0.7583024;

            MODx[2] = 0.7433543;
            MODy[2] = 0.51731443;
            MODz[2] = 0.4240403;
        }

        if (myStain.equals(StainModels.ASTRABLUE_FUCHSIN)) {
            // GL AstraBlue
            MODx[0] = 0.92045766;
            MODy[0] = 0.35425216;
            MODz[0] = 0.16511545;
            // GL Basic Fuchsin
            MODx[1] = 0.13336428;
            MODy[1] = 0.8301452;
            MODz[1] = 0.5413621;
            // Zero matrix
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 0.0;
        }

        if (myStain.equals(StainModels.RGB)) {
            // R
            MODx[0] = 0.001;
            MODy[0] = 1.0;
            MODz[0] = 1.0;
            // G
            MODx[1] = 1.0;
            MODy[1] = 0.001;
            MODz[1] = 1.0;
            // B
            MODx[2] = 1.0;
            MODy[2] = 1.0;
            MODz[2] = 0.001;
        }

        if (myStain.equals(StainModels.CMY)) {
            // C
            MODx[0] = 1.0;
            MODy[0] = 0.0;
            MODz[0] = 0.0;
            // M
            MODx[1] = 0.0;
            MODy[1] = 1.0;
            MODz[1] = 0.0;
            // Y
            MODx[2] = 0.0;
            MODy[2] = 0.0;
            MODz[2] = 1.0;
        }

        return new double[][] { MODx, MODy, MODz };

    }

    public static double[][] getCustomStainVectors(double r1, double g1, double b1, double r2, double g2, double b2,
            double r3, double g3, double b3) {
        double[] MODx = new double[] { r1, r2, r3 };
        double[] MODy = new double[] { g1, g2, g3 };
        double[] MODz = new double[] { b1, b2, b3 };

        return new double[][] { MODx, MODy, MODz };

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Applies the <a href=\"https://imagej.net/plugins/colour-deconvolution\">Colour Deconvolution</a> plugin to unmix an RGB image (stored in the workspace) into up to three separate channels, which are output as separate images.  The input image can be stored as either an RGB or composite image.  This process is only applicable to images created via subtractive mixing (e.g. histological staining), not to additive methods (e.g. fluorescence).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputType = parameters.getValue(OUTPUT_TYPE, workspace);
        boolean outputImage1 = parameters.getValue(ENABLE_IM1_OUTPUT, workspace);
        String outputImageName1 = parameters.getValue(OUTPUT_IMAGE_1, workspace);
        boolean outputImage2 = parameters.getValue(ENABLE_IM2_OUTPUT, workspace);
        String outputImageName2 = parameters.getValue(OUTPUT_IMAGE_2, workspace);
        boolean outputImage3 = parameters.getValue(ENABLE_IM3_OUTPUT, workspace);
        String outputImageName3 = parameters.getValue(OUTPUT_IMAGE_3, workspace);
        String stainModel = parameters.getValue(STAIN_MODEL, workspace);
        double r1 = parameters.getValue(R1, workspace);
        double g1 = parameters.getValue(G1, workspace);
        double b1 = parameters.getValue(B1, workspace);
        double r2 = parameters.getValue(R2, workspace);
        double g2 = parameters.getValue(G2, workspace);
        double b2 = parameters.getValue(B2, workspace);
        double r3 = parameters.getValue(R3, workspace);
        double g3 = parameters.getValue(G3, workspace);
        double b3 = parameters.getValue(B3, workspace);
        boolean doIcross = parameters.getValue(CROSS_PRODUCT_FOR_COLOUR_3, workspace);

        // Getting input image
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        String outputTypeConverted = "";
        boolean doILUTs = true;
        switch (outputType) {
            case OutputTypes.ABSORBANCE_32:
                outputTypeConverted = "32bit_Absorbance";
                doILUTs = false;
                break;
            case OutputTypes.TRANSMITTANCE_8:
                outputTypeConverted = "8bit_Transmittance";
                break;
            case OutputTypes.TRANSMITTANCE_32:
                outputTypeConverted = "32bit_Transmittance";
                break;
        }

        switch (stainModel) {
            case StainModels.H_DAB_FUCHSIN:
            case StainModels.FR_FB_DAB:
            case StainModels.H_AND_E_DAB:
            case StainModels.AZAN_MALLORY:
            case StainModels.BRILLIANT_BLUE:
            case StainModels.RGB:
            case StainModels.CMY:
                doIcross = false;
                break;
        }

        // Running the deconvolution
        double[][] stainMatrix;
        if (stainModel.equals(StainModels.CUSTOM))
            stainMatrix = getCustomStainVectors(r1, g1, b1, r2, g2, b2, r3, g3, b3);
        else
            stainMatrix = getStainVectors(stainModel);

        if (inputIpl.getBitDepth() != 24) {
            inputIpl = inputIpl.duplicate();
            SetDisplayRange.setDisplayRangeManual(inputIpl,
                    new double[] { 0, Math.pow(2, inputIpl.getBitDepth()) - 1 });
            RGBStackConverter.convertToRGB(inputIpl);
        }

        Object[] result = new Colour_Deconvolution2().exec(inputIpl, inputIpl.getTitle(), stainModel,
                outputTypeConverted, doILUTs, doIcross, false, true, stainMatrix[0], stainMatrix[1], stainMatrix[2]);

        // If selected, displaying the image
        if (outputImage1) {
            Image outImage1 = ImageFactory.createImage(outputImageName1, (ImagePlus) result[1]);
            workspace.addImage(outImage1);
            if (showOutput)
                outImage1.show(false);
        }

        if (outputImage2) {
            Image outImage2 = ImageFactory.createImage(outputImageName2, (ImagePlus) result[2]);
            workspace.addImage(outImage2);
            if (showOutput)
                outImage2.show(false);
        }

        if (outputImage3) {
            Image outImage3 = ImageFactory.createImage(outputImageName3, (ImagePlus) result[3]);
            workspace.addImage(outImage3);
            if (showOutput)
                outImage3.show(false);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.TRANSMITTANCE_8, OutputTypes.ALL));
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

        // The following is enabled by default in the main Colour_Deconvolution2 plugin,
        // but disabled here as this gives closer results to the original plugin.
        parameters.add(new BooleanP(CROSS_PRODUCT_FOR_COLOUR_3, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_TYPE));
        returnedParameters.add(parameters.getParameter(ENABLE_IM1_OUTPUT));
        if ((boolean) parameters.getValue(ENABLE_IM1_OUTPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_1));

        returnedParameters.add(parameters.getParameter(ENABLE_IM2_OUTPUT));
        if ((boolean) parameters.getValue(ENABLE_IM2_OUTPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(ENABLE_IM3_OUTPUT));
        if ((boolean) parameters.getValue(ENABLE_IM3_OUTPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE_3));

        returnedParameters.add(parameters.get(DECONVOLUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STAIN_MODEL));

        switch ((String) parameters.getValue(STAIN_MODEL, workspace)) {
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

                if ((double) parameters.getValue(R3, workspace) == 0 && (double) parameters.getValue(G3, workspace) == 0
                        && (double) parameters.getValue(B3, workspace) == 0)
                    returnedParameters.add(parameters.getParameter(CROSS_PRODUCT_FOR_COLOUR_3));
                break;
            case StainModels.H_AND_E:
            case StainModels.H_AND_E_2:
            case StainModels.H_DAB:
            case StainModels.NBT:
            case StainModels.H_HRP_GREEN_FUCHSIN:
            case StainModels.FLG:
            case StainModels.GIEMSA:
            case StainModels.METHYL_GREEN_DAB:
            case StainModels.H_AEC:
            case StainModels.MASSON_TRICHROME:
            case StainModels.ALCIAN_BLUE_H:
            case StainModels.H_PAS:
            case StainModels.ASTRABLUE_FUCHSIN:
                returnedParameters.add(parameters.getParameter(CROSS_PRODUCT_FOR_COLOUR_3));
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
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

    @Override
    public boolean verify() {
        return true;
    }
}
