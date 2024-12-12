package io.github.mianalysis.mia.module.images.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import io.github.mianalysis.enums.BitDepth;
import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.OutputMode;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.system.Status;

public class InvertIntensityMSTest extends ModuleTest {
    /**
     * Generates dimension permutations
     */
    public static Stream<Arguments> dimFilterInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (OutputMode outputMode : OutputMode.values())
                for (ImageType imageType : ImageType.values())
                    argumentBuilder.add(Arguments.of(dimension, outputMode, imageType));

        return argumentBuilder.build();

    }

    /**
     * Generates bit depth permutations
     */
    public static Stream<Arguments> bitdepthInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (BitDepth bitDepth : BitDepth.values())
            for (OutputMode outputMode : OutputMode.values())
                for (ImageType imageType : ImageType.values())
                    argumentBuilder.add(Arguments.of(bitDepth, outputMode, imageType));

        return argumentBuilder.build();

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions.
     * The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimFilterInputProvider")
    void test8Bit(Dimension dimension, OutputMode outputMode, ImageType imageType)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, outputMode, imageType);
    }

    /**
     * Parameterized test run with all bit depths for maximum Z-projected D4ZT
     * stack. The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("bitdepthInputProvider")
    void testAllBitDepths_D4ZT(BitDepth bitDepth, OutputMode outputMode, ImageType imageType)
            throws UnsupportedEncodingException {
        assumeFalse(bitDepth == BitDepth.B32);
        runTest(Dimension.D4ZT, bitDepth, outputMode, imageType);
    }

    // /*
    // * Used for testing a single set of parameters
    // */
    // @Test
    // void singleTest() throws UnsupportedEncodingException {
    // runTest(Dimension.D4ZT, BitDepth.B32, OutputMode.APPLY_TO_INPUT,
    // ImageType.IMGLIB2);
    // }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, BitDepth bitDepth, OutputMode outputMode, ImageType imageType)
            throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(InvertIntensityMSTest.class.getResource(inputName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(InvertIntensityMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        ImageI image = ImageFactory.createImage("Test_image", ipl, imageType);
        workspace.addImage(image);

        // Loading the expected image
        String expectedName = "/msimages/invertintensity/InvertIntensity_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(InvertIntensityMSTest.class.getResource(expectedName) != null);

        String expectedPath = URLDecoder.decode(InvertIntensityMSTest.class.getResource(expectedName).getPath(),
                "UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(expectedPath), imageType);

        // Initialising module and setting parameters
        InvertIntensity invertIntensity = new InvertIntensity(new Modules());
        invertIntensity.updateParameterValue(InvertIntensity.INPUT_IMAGE, "Test_image");
        invertIntensity.updateParameterValue(InvertIntensity.APPLY_TO_INPUT, applyToInput);
        invertIntensity.updateParameterValue(InvertIntensity.OUTPUT_IMAGE, "Test_output");

        // Running Module
        Status status = invertIntensity.execute(workspace);
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
        assertNotNull(new InvertIntensity(null).getDescription());
    }
}
