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
import io.github.mianalysis.enums.Calibration;
import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.Logic;
import io.github.mianalysis.enums.OutputMode;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageFactoryI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.system.Status;

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
                for (Calibration calibration : Calibration.values())
                    for (OutputMode outputMode : OutputMode.values())
                        for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                            argumentBuilder.add(Arguments.of(threshold, logic, calibration, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> dimLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Logic logic : Logic.values())
                for (Calibration calibration : Calibration.values())
                    for (OutputMode outputMode : OutputMode.values())
                        for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                            argumentBuilder.add(Arguments.of(dimension, logic, calibration, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions, all threshold
     * algorithms and all logics.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("thresholdLogicInputProvider")
    void testAllD3T(Threshold threshold, Logic logic, Calibration calibration, OutputMode outputMode,
            ImageFactoryI imageFactory) throws UnsupportedEncodingException {
        switch (calibration) {
            case CALIBRATED:
                runTest(Dimension.D3T, threshold, logic, 0.48, calibration, outputMode, imageFactory);
                break;
            case UNCALIBRATED:
                runTest(Dimension.D3T, threshold, logic, 24, calibration, outputMode, imageFactory);
                break;
        }
    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions, all threshold
     * algorithms and all logics.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimLogicInputProvider")
    void testAllTPHANSALKAR(Dimension dimension, Logic logic, Calibration calibration, OutputMode outputMode,
            ImageFactoryI imageFactory) throws UnsupportedEncodingException {
        switch (calibration) {
            case CALIBRATED:
                runTest(dimension, Threshold.TPHANSALKAR, logic, 0.48, calibration, outputMode, imageFactory);
                break;
            case UNCALIBRATED:
                runTest(dimension, Threshold.TPHANSALKAR, logic, 24, calibration, outputMode, imageFactory);
                break;
        }
    }

    // /*
    // * Used for testing a single set of parameters
    // */
    // @Test
    // void singleTest() throws UnsupportedEncodingException {
    // runTest(Dimension.D3T, Threshold.TNIBLACK, Logic.LW, 24,
    // OutputMode.CREATE_NEW, ImageFactoryI.IMAGEPLUS);
    // }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, Threshold threshold, Logic logic, double radius,
            Calibration calibration, OutputMode outputMode, ImageFactoryI imageFactory) throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_B8.zip";
        assumeTrue(LocalAutoThresholdMSTest.class.getResource(inputName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(LocalAutoThresholdMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        ImageI image = imageFactory.create("Test_image", ipl);
        workspace.addImage(image);

        String radiusStr = Integer.toString((int) (calibration == Calibration.CALIBRATED ? radius / ipl.getCalibration().pixelWidth : radius));
        String expectedName = "/msimages/localautothreshold/LAThreshold_" + dimension + "_B8_" + threshold + "_"
                + logic + "_R" + radiusStr + ".zip";
        assumeTrue(LocalAutoThresholdMSTest.class.getResource(expectedName) != null);

        String expectedPath = URLDecoder.decode(LocalAutoThresholdMSTest.class.getResource(expectedName).getPath(),
                "UTF-8");
        ImageI expectedImage = imageFactory.create("Expected", IJ.openImage(expectedPath));

        // Initialising module and setting parameters
        LocalAutoThreshold module = new LocalAutoThreshold(new Modules());
        module.updateParameterValue(LocalAutoThreshold.INPUT_IMAGE, "Test_image");
        module.updateParameterValue(LocalAutoThreshold.APPLY_TO_INPUT, applyToInput);
        module.updateParameterValue(LocalAutoThreshold.OUTPUT_IMAGE, "Test_output");
        module.updateParameterValue(LocalAutoThreshold.LOCAL_RADIUS, radius);

        switch (calibration) {
            case CALIBRATED:
                module.updateParameterValue(LocalAutoThreshold.SPATIAL_UNITS_MODE,
                        LocalAutoThreshold.SpatialUnitsModes.CALIBRATED);
                break;
            case UNCALIBRATED:
                module.updateParameterValue(LocalAutoThreshold.SPATIAL_UNITS_MODE,
                        LocalAutoThreshold.SpatialUnitsModes.PIXELS);
                break;
        }

        switch (threshold) {
            case TBERNSEN:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.BERNSEN);
                break;
            case TCONTRAST:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.CONTRAST);
                break;
            case TMEAN:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.MEAN);
                break;
            case TMEDIAN:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.MEDIAN);
                break;
            case TMIDGRAY:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.MIDGREY);
                break;
            case TNIBLACK:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.NIBLACK);
                break;
            case TOTSU:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.OTSU);
                break;
            case TPHANSALKAR:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.PHANSALKAR);
                break;
            case TSAUVOLA:
                module.updateParameterValue(LocalAutoThreshold.ALGORITHM_SLICE,
                        LocalAutoThreshold.AlgorithmsSlice.SAUVOLA);
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
        assertNotNull(new LocalAutoThreshold(null).getDescription());
    }

}
