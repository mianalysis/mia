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
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.system.Status;

public class ProjectImageMSTest extends ModuleTest {
    enum Axis {
        // AX,
        // AY,
        AC,
        AZ,
        AT
    }

    enum Mode {
        MAVERAGE,
        MMIN,
        MMEDIAN,
        MMAX,
        MSTDEV,
        MSUM
    }

    /**
     * Generates dimension, mode and axis permutations
     */
    public static Stream<Arguments> dimFilterInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Axis axis : Axis.values())
                for (Mode mode : Mode.values())
                    for (ImageType imageType : ImageType.values())
                        argumentBuilder.add(Arguments.of(dimension, axis, mode, imageType));

        return argumentBuilder.build();

    }

    /**
     * Generates bit depth permutations
     */
    public static Stream<Arguments> bitdepthInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (BitDepth bitDepth : BitDepth.values())
            for (ImageType imageType : ImageType.values())
                argumentBuilder.add(Arguments.of(bitDepth, imageType));

        return argumentBuilder.build();

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions, axes and
     * modes.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimFilterInputProvider")
    void test8Bit(Dimension dimension, Axis axis, Mode mode, ImageType imageType)
            throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, axis, mode, imageType);
    }

    /**
     * Parameterized test run with all bit depths for average Z-projected D4ZT
     * stack. 
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("bitdepthInputProvider")
    void testAllBitDepths_D4ZT_MAVERAGE_AZ(BitDepth bitDepth, ImageType imageType)
            throws UnsupportedEncodingException {
        runTest(Dimension.D4ZT, bitDepth, Axis.AZ, Mode.MAVERAGE, imageType);
    }

    /**
     * Parameterized test run with all bit depths for maximum Z-projected D4ZT
     * stack. 
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("bitdepthInputProvider")
    void testAllBitDepths_D4ZT_MMAX_AZ(BitDepth bitDepth, ImageType imageType)
            throws UnsupportedEncodingException {
        runTest(Dimension.D4ZT, bitDepth, Axis.AZ, Mode.MMAX, imageType);
    }

    // /*
    //  * Used for testing a single set of parameters
    //  */
    // @Test
    // void singleTest() throws UnsupportedEncodingException {
    //     runTest(Dimension.D3T, BitDepth.B8, Axis.AT, Mode.MMAX, ImageType.IMGLIB2);
    // }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, BitDepth bitDepth, Axis axis, Mode mode, ImageType imageType)
            throws UnsupportedEncodingException {
        long t1 = System.currentTimeMillis();

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(ProjectImageMSTest.class.getResource(inputName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(ProjectImageMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        Image image = ImageFactory.createImage("Test_image", ipl, imageType);
        workspace.addImage(image);

        // Loading the expected image
        String expectedName = "/msimages/projectimage/ProjectImage_" + dimension + "_" + bitDepth + "_" + axis + "_"
                + mode + ".zip";
        assumeTrue(ProjectImageMSTest.class.getResource(expectedName) != null);

        String expectedPath = URLDecoder.decode(ProjectImageMSTest.class.getResource(expectedName).getPath(), "UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(expectedPath), imageType);

        // When loading a 2D image (but not a single-slice hyperstack), ImageJ will
        // disregard any Z-axis spatial calibration. Since this calibration will be
        // present in the projected image after the projection operation, the Z-axis
        // calibration needs to be manually added to the loaded expected image. This
        // isn't done for 2D images, as the input for these will have already had the
        // calibration scrubbed.
        if (dimension != Dimension.D2)
            expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising module and setting parameters
        ProjectImage projectImage = new ProjectImage(new Modules());
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE, "Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE, "Test_output");

        switch (mode) {
            case MAVERAGE:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE, ProjectImage.ProjectionModes.AVERAGE);
                break;
            case MMAX:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE, ProjectImage.ProjectionModes.MAX);
                break;
            case MMEDIAN:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE, ProjectImage.ProjectionModes.MEDIAN);
                break;
            case MMIN:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE, ProjectImage.ProjectionModes.MIN);
                break;
            case MSTDEV:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE, ProjectImage.ProjectionModes.STDEV);
                break;
            case MSUM:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE, ProjectImage.ProjectionModes.SUM);
                break;
        }

        switch (axis) {
            case AC:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_AXIS, ProjectImage.AxisModes.CHANNEL);
                break;
            case AT:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_AXIS, ProjectImage.AxisModes.TIME);
                break;
            case AZ:
                projectImage.updateParameterValue(ProjectImage.PROJECTION_AXIS, ProjectImage.AxisModes.Z);
                break;
        }

        // Running Module
        Status status = projectImage.execute(workspace);
        assertEquals(Status.PASS, status);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        Image outputImage = workspace.getImage("Test_output");

        // new ij.ImageJ();
        // expectedImage.showImage();
        // outputImage.showImage();
        // IJ.runMacro("waitForUser");

        assertEquals(expectedImage, outputImage);

        long t2 = System.currentTimeMillis();
        // System.out.println(t2 - t1);

    }

    /**
     * Test to check this module has an assigned description
     */
    @Override
    public void testGetHelp() {
        assertNotNull(new ProjectImage(null).getDescription());
    }
}
