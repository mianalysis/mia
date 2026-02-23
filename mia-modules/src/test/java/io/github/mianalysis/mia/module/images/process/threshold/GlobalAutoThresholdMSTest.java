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
import io.github.mianalysis.enums.OutputMode;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageFactoryI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Gemma and BIG George on 05/10/2022.
 */
public class GlobalAutoThresholdMSTest extends ModuleTest {

    enum Threshold {
        THUANG,
        TINTERMODES,
        TISO_DATA,
        TLI,
        TMAX_ENTROPY,
        TMEAN,
        TMIN_ERROR,
        TMINIMUM,
        TMOMENTS,
        TOTSU,
        TPERCENTILE,
        TRENYI_ENTROPY,
        TSHANBHAG,
        TTRIANGLE,
        TYEN
    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> thresholdLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Threshold threshold : Threshold.values())
            for (Logic logic : Logic.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        argumentBuilder.add(Arguments.of(threshold, logic, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> dimLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Logic logic : Logic.values())

                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        argumentBuilder.add(Arguments.of(dimension, logic, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /**
     * Parameterized test run with 8-bit bit depth only D3T dimension only with all
     * threshold algorithms and all logics.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("thresholdLogicInputProvider")
    void testAll(Threshold threshold, Logic logic, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(Dimension.D3T, threshold, logic, outputMode, imageFactory);

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions and all logics
     * for Huang algorithm only.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimLogicInputProvider")
    void testAllTHUANG(Dimension dimension, Logic logic, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(dimension, Threshold.THUANG, logic, outputMode, imageFactory);

    }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, Threshold threshold, Logic logic, OutputMode outputMode,
            ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_B8.zip";
        assumeTrue(GlobalAutoThresholdMSTest.class.getResource(inputName) != null);

        String expectedName = "/msimages/globalautothreshold/GAThreshold_" + dimension + "_B8_" + threshold + "_"
                + logic + ".zip";
        assumeTrue(GlobalAutoThresholdMSTest.class.getResource(expectedName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(GlobalAutoThresholdMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        ImageI image = imageFactory.create("Test_image", ipl);
        workspace.addImage(image);

        String expectedPath = URLDecoder.decode(GlobalAutoThresholdMSTest.class.getResource(expectedName).getPath(),
                "UTF-8");
        ImageI expectedImage = imageFactory.create("Expected", IJ.openImage(expectedPath));

        // Initialising module and setting parameters
        GlobalAutoThreshold module = new GlobalAutoThreshold(new Modules());
        module.updateParameterValue(GlobalAutoThreshold.INPUT_IMAGE, "Test_image");
        module.updateParameterValue(GlobalAutoThreshold.APPLY_TO_INPUT, applyToInput);
        module.updateParameterValue(GlobalAutoThreshold.OUTPUT_IMAGE, "Test_output");

        switch (threshold) {
            case THUANG:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.HUANG);
                break;
            case TINTERMODES:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.INTERMODES);
                break;
            case TISO_DATA:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.ISO_DATA);
                break;
            case TLI:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.LI);
                break;
            case TMAX_ENTROPY:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.MAX_ENTROPY);
                break;
            case TMEAN:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.MEAN);
                break;
            case TMINIMUM:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.MINIMUM);
                break;
            case TMIN_ERROR:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.MIN_ERROR);
                break;
            case TMOMENTS:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.MOMENTS);
                break;
            case TOTSU:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.OTSU);
                break;
            case TPERCENTILE:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.PERCENTILE);
                break;
            case TRENYI_ENTROPY:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM,
                        GlobalAutoThreshold.Algorithms.RENYI_ENTROPY);
                break;
            case TSHANBHAG:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.SHANBHAG);
                break;
            case TTRIANGLE:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.TRIANGLE);
                break;
            case TYEN:
                module.updateParameterValue(GlobalAutoThreshold.ALGORITHM, GlobalAutoThreshold.Algorithms.YEN);
                break;
        }

        switch (logic) {
            case LB:
                module.updateParameterValue(GlobalAutoThreshold.BINARY_LOGIC,
                        GlobalAutoThreshold.BinaryLogic.BLACK_BACKGROUND);
                break;
            case LW:
                module.updateParameterValue(GlobalAutoThreshold.BINARY_LOGIC,
                        GlobalAutoThreshold.BinaryLogic.WHITE_BACKGROUND);
                break;
        }

        // Running Module
        Status status = module.execute(workspace);
        assertEquals(Status.PASS, status);

        // Checking the images in the workspace
        if (applyToInput) {
            assertEquals(1, workspace.getImages().size());
            assertNotNull(workspace.getImage("Test_image"));

            ImageI outputImage = workspace.getImage("Test_image");
            assertEquals(expectedImage, outputImage);

        } else {
            assertEquals(2, workspace.getImages().size());
            assertNotNull(workspace.getImage("Test_image"));
            assertNotNull(workspace.getImage("Test_output"));

            ImageI outputImage = workspace.getImage("Test_output");
            assertEquals(expectedImage, outputImage);

        }
    }

    /**
     * Test to check this module has an assigned description
     */
    @Override
    public void testGetHelp() {
        assertNotNull(new GlobalAutoThreshold(null).getDescription());
    }

}
