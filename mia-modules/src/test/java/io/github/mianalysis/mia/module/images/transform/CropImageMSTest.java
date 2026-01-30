package io.github.mianalysis.mia.module.images.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
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
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImagePlusImageFactory;
import io.github.mianalysis.mia.object.image.ImageFactoryI;
import io.github.mianalysis.mia.object.system.Status;

public class CropImageMSTest extends ModuleTest {
    enum LimitsMode {
        LFIXED,
        LOBJECTS
    }

    /**
     * Generates dimension permutations
     */
    public static Stream<Arguments> dimProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (OutputMode outputMode : OutputMode.values())
                for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                    argumentBuilder.add(Arguments.of(dimension, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /**
     * Generates dimension permutations
     */
    public static Stream<Arguments> dimInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (LimitsMode limitsMode : LimitsMode.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        argumentBuilder.add(Arguments.of(dimension, limitsMode, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /**
     * Generates bit depth permutations
     */
    public static Stream<Arguments> bitdepthInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (BitDepth bitDepth : BitDepth.values())
            for (LimitsMode limitsMode : LimitsMode.values())
                for (OutputMode outputMode : OutputMode.values())
                    for (ImageFactoryI imageFactory : ImageFactories.getFactories().values())
                        argumentBuilder.add(Arguments.of(bitDepth, limitsMode, outputMode, imageFactory));

        return argumentBuilder.build();

    }

    /*
     * Normal crop within limits (all dimensions, 8-bit)
     */
    @ParameterizedTest
    @MethodSource("dimInputProvider")
    void test8Bit_X12Y15W23H6(Dimension dimension, LimitsMode limitsMode, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, limitsMode, 12, 15, 23, 6, outputMode, imageFactory);
    }

    /*
     * All within limits (D4ZT dimension, all bit depths)
     */
    @ParameterizedTest
    @MethodSource("bitdepthInputProvider")
    void testAllBitDepths_D4ZT_X12Y15W23H6(BitDepth bitDepth, LimitsMode limitsMode, OutputMode outputMode,
            ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(Dimension.D4ZT, bitDepth, limitsMode, 12, 15, 23, 6, outputMode, imageFactory);
    }

    /*
     * Negative value for one input (all dimensions, 8-bit)
     */
    @ParameterizedTest
    @MethodSource("dimProvider")
    void test8Bit_Xm12Y15W23H6(Dimension dimension, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, LimitsMode.LFIXED, -12, 15, 23, 6, outputMode, imageFactory);
    }

    /*
     * Height beyond limit (all dimensions, 8-bit)
     */
    @ParameterizedTest
    @MethodSource("dimProvider")
    void test8Bit_X12Y15W23H90(Dimension dimension, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, LimitsMode.LFIXED, 12, 15, 23, 90, outputMode, imageFactory);
    }

    /*
     * Negative height
     */
    @Test
    void test8Bit_X12Y15W23Hm6()
            throws UnsupportedEncodingException {
        runTest(Dimension.D2, BitDepth.B8, LimitsMode.LFIXED, 12, 15, 23, -6, OutputMode.CREATE_NEW,
                new ImagePlusImageFactory());
    }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, BitDepth bitDepth, LimitsMode limitsMode, int x, int y, int w,
            int h, OutputMode outputMode, ImageFactoryI imageFactory)
            throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(CropImageMSTest.class.getResource(inputName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(CropImageMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        ImageI image = imageFactory.create("Test_image", ipl);
        workspace.addImage(image);

        // Loading the expected image (if we're expecting one)
        ImageI expectedImage = null;
        if (w >= 0 && h >= 0) {
            String expectedName = "/msimages/cropimage/CropImage_" + dimension + "_" + bitDepth + "_X" + x + "_Y" + y
                    + "_W" + w + "_H" + h + ".zip";
            assumeTrue(CropImageMSTest.class.getResource(expectedName) != null);
            String expectedPath = URLDecoder.decode(CropImageMSTest.class.getResource(expectedName).getPath(), "UTF-8");
            expectedImage = imageFactory.create("Expected", IJ.openImage(expectedPath));
        }

        // Initialising module and setting parameters
        CropImage cropImage = new CropImage(new Modules());
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE, "Test_image");
        cropImage.updateParameterValue(CropImage.APPLY_TO_INPUT, applyToInput);
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE, "Test_output");
        switch (limitsMode) {
            case LFIXED:
                cropImage.updateParameterValue(CropImage.LIMITS_MODE, CropImage.LimitsModes.FIXED_VALUES);
                cropImage.updateParameterValue(CropImage.LEFT, x);
                cropImage.updateParameterValue(CropImage.TOP, y);
                cropImage.updateParameterValue(CropImage.WIDTH, w);
                cropImage.updateParameterValue(CropImage.HEIGHT, h);
                break;
            case LOBJECTS:
                cropImage.updateParameterValue(CropImage.LIMITS_MODE,
                        CropImage.LimitsModes.FROM_OBJECTS);
                cropImage.updateParameterValue(CropImage.LEFT, 0);
                cropImage.updateParameterValue(CropImage.TOP, 0);
                cropImage.updateParameterValue(CropImage.WIDTH, 0);
                cropImage.updateParameterValue(CropImage.HEIGHT, 0);
                cropImage.updateParameterValue(CropImage.INPUT_OBJECTS, "LimitsObjects");

                ObjsI limitsObjects = ObjsFactories.getDefaultFactory().createFromImage("LimitsObjects", image);
                limitsObjects.setNFrames(1);
                limitsObjects.setFrameInterval(1);
                limitsObjects.setTemporalUnit(null);
                
                ObjI limitObject = limitsObjects.createAndAddNewObject(new PointListFactory());
                try {
                    limitObject.addCoord(x, y, 0);
                    limitObject.addCoord(x + w - 1, y + h - 1, 0);
                } catch (PointOutOfRangeException e) {
                    fail("Can't add object points");
                }
                workspace.addObjects(limitsObjects);

                break;
        }

        // Running Module
        Status status = cropImage.execute(workspace);

        if (w < 0 || h < 0) {
            assertEquals(Status.FAIL, status);
            return;
        }

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
        assertNotNull(new CropImage(null).getDescription());
    }
}
