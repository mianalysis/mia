package io.github.mianalysis.mia.module.images.process.threshold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.Logic;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;

/**
 * Created by Gemma and George (and Peggy) on 07/10/2022.
 */
public class LocalAutoThresholdMSTest extends ModuleTest {

    enum Threshold {
        TBERNSEN,
        TCONTRAST,
        TMEAN,
        TMEDIAN,
        TMIDGRAY,
        TNIBLACK,
        TOTSU,
        TPHANSALKAR,
        TSAUVOLA,
    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> thresholdLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
            for (Threshold threshold : Threshold.values())
                for (Logic logic : Logic.values())
                    argumentBuilder.add(Arguments.of(threshold, logic));

        return argumentBuilder.build();

    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> dimLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
            for (Dimension dimension : Dimension.values())
                for (Logic logic : Logic.values())
                    argumentBuilder.add(Arguments.of(dimension, logic));

        return argumentBuilder.build();

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions, all threshold algorithms and all logics. 
     * The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("thresholdLogicInputProvider")
    void testAllD3T(Threshold threshold, Logic logic) throws UnsupportedEncodingException {
        runTest(Dimension.D3T, threshold, logic, 24);

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions, all threshold algorithms and all logics. 
     * The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimLogicInputProvider")
    void testAllTPHANSALKAR(Dimension dimension, Logic logic) throws UnsupportedEncodingException {
        runTest(dimension, Threshold.TPHANSALKAR, logic, 24);

    }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, Threshold threshold, Logic logic, int radius)
            throws UnsupportedEncodingException {
        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_B8.zip";
        assumeTrue(LocalAutoThresholdMSTest.class.getResource(inputName) != null);

        String expectedName = "/msimages/localautothreshold/LAThreshold_" + dimension + "_B8_" + threshold + "_"
                + logic + "_R" + radius + ".zip";
        assumeTrue(LocalAutoThresholdMSTest.class.getResource(expectedName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(LocalAutoThresholdMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        String expectedPath = URLDecoder.decode(LocalAutoThresholdMSTest.class.getResource(expectedName).getPath(),
                "UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(expectedPath));

        // Initialising module and setting parameters
        LocalAutoThreshold module = new LocalAutoThreshold(new Modules());
        module.updateParameterValue(LocalAutoThreshold.INPUT_IMAGE, "Test_image");
        module.updateParameterValue(LocalAutoThreshold.APPLY_TO_INPUT, false);
        module.updateParameterValue(LocalAutoThreshold.OUTPUT_IMAGE, "Test_output");
        module.updateParameterValue(LocalAutoThreshold.LOCAL_RADIUS, radius);

        switch (threshold) {
            case TBERNSEN:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.BERNSEN);
                break;
            case TCONTRAST:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.CONTRAST);
                break;
            case TMEAN:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.MEAN);
                break;
            case TMEDIAN:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.MEDIAN);
                break;
            case TMIDGRAY:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.MIDGREY);
                break;
            case TNIBLACK:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.NIBLACK);
                break;
            case TOTSU:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.OTSU);
                break;
            case TPHANSALKAR:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.PHANSALKAR);
                break;
            case TSAUVOLA:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE, LocalAutoThreshold.AlgorithmsSlice.SAUVOLA);
                break;
        }

        switch (logic) {
            case LB:
                module.updateParameterValue(LocalAutoThreshold.BINARY_LOGIC,
                        LocalAutoThreshold.BinaryLogic.BLACK_BACKGROUND);
                break;
            case LW:
                module.updateParameterValue(LocalAutoThreshold.BINARY_LOGIC,
                        LocalAutoThreshold.BinaryLogic.WHITE_BACKGROUND);
                break;
        }

        // Running Module
        module.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * Test to check this module has an assigned description
     */
    @Override
    public void testGetHelp() {
        assertNotNull(new LocalAutoThreshold(null).getDescription());
    }

}
