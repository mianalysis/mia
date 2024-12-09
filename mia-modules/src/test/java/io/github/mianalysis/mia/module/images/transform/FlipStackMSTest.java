package io.github.mianalysis.mia.module.images.transform;

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
import io.github.mianalysis.enums.BitDepth;
import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.OutputMode;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.system.Status;

public class FlipStackMSTest extends ModuleTest {
    enum Axis {
        AX,
        AY,
        AC,
        AZ,
        AT
    }

    /**
     * Generates dimension and axis permutations
     */
    public static Stream<Arguments> dimFilterInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Axis axis : Axis.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageType imageType : ImageType.values())
                        argumentBuilder.add(Arguments.of(dimension, axis, outputMode, imageType));

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
     * Parameterized test run with 8-bit bit depth and all dimensions amd axes.
     * The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimFilterInputProvider")
    void test8Bit(Dimension dimension, Axis axis, OutputMode outputMode, ImageType imageType)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, axis, outputMode, imageType);
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
        runTest(Dimension.D4ZT, bitDepth, Axis.AZ, outputMode, imageType);
    }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, BitDepth bitDepth, Axis axis, OutputMode outputMode, ImageType imageType)
            throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(FlipStackMSTest.class.getResource(inputName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(FlipStackMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        Image image = ImageFactory.createImage("Test_image", ipl, imageType);
        workspace.addImage(image);

        // Loading the expected image
        String expectedName = "/msimages/flipstack/FlipStack_" + dimension + "_" + bitDepth + "_" + axis + ".zip";
        assumeTrue(FlipStackMSTest.class.getResource(expectedName) != null);

        String expectedPath = URLDecoder.decode(FlipStackMSTest.class.getResource(expectedName).getPath(), "UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(expectedPath), imageType);

        // Initialising module and setting parameters
        FlipStack flipStack = new FlipStack(new Modules());
        flipStack.updateParameterValue(FlipStack.INPUT_IMAGE, "Test_image");
        flipStack.updateParameterValue(FlipStack.APPLY_TO_INPUT, applyToInput);
        flipStack.updateParameterValue(FlipStack.OUTPUT_IMAGE, "Test_output");

        switch (axis) {
            case AX:
                flipStack.updateParameterValue(FlipStack.AXIS_MODE, FlipStack.AxisModes.X);
                break;
            case AY:
                flipStack.updateParameterValue(FlipStack.AXIS_MODE, FlipStack.AxisModes.Y);
                break;
            case AC:
                flipStack.updateParameterValue(FlipStack.AXIS_MODE, FlipStack.AxisModes.CHANNEL);
                break;
            case AZ:
                flipStack.updateParameterValue(FlipStack.AXIS_MODE, FlipStack.AxisModes.Z);
                break;
            case AT:
                flipStack.updateParameterValue(FlipStack.AXIS_MODE, FlipStack.AxisModes.TIME);
                break;
        }

        // Running Module
        Status status = flipStack.execute(workspace);
        assertEquals(Status.PASS, status);

        // Checking the images in the workspace
        if (applyToInput) {
            assertEquals(1, workspace.getImages().size());
            assertNotNull(workspace.getImage("Test_image"));

            Image outputImage = workspace.getImage("Test_image");
            assertEquals(expectedImage, outputImage);

        } else {
            assertEquals(2, workspace.getImages().size());
            assertNotNull(workspace.getImage("Test_image"));
            assertNotNull(workspace.getImage("Test_output"));

            Image outputImage = workspace.getImage("Test_output");
            assertEquals(expectedImage, outputImage);

        }

    }

    /**
     * Test to check this module has an assigned description
     */
    @Override
    public void testGetHelp() {
        assertNotNull(new FlipStack(null).getDescription());
    }
}
