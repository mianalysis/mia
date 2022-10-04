package io.github.mianalysis.mia.module.images.process;

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

public class FilterImageMSTest extends ModuleTest {

    enum Filter {
        FDOG2D, // 2D difference of Gaussian
        FGAUSS2D, // 2D Gaussian
        FGAUSS3D, // 3D Gaussian
        FGRAD2D, // 2D gradient
        FMAX2D, // 2D maximum
        FMAX3D, // 3D maximum
        FMEAN2D, // 2D mean
        FMEAN3D, // 3D mean
        FMEDIAN2D, // 2D median
        FMEDIAN3D, // 3D median
        FMIN2D, // 2D minimum
        FMIN3D, // 3D minimum
        // FRIDGE2D, // 2D ridge enhancement
        FVAR2D, // 2D variance
        FVAR3D, // 3D variance

    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> dimBitdepthFilterInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (BitDepth bitDepth : BitDepth.values())
                for (Filter filter : Filter.values())
                    argumentBuilder.add(Arguments.of(dimension, bitDepth, filter));

        return argumentBuilder.build();

    }

    /**
     * Generates dimension and filter permutations
     */
    public static Stream<Arguments> dimFilterInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Filter filter : Filter.values())
                argumentBuilder.add(Arguments.of(dimension, filter));

        return argumentBuilder.build();

    }

    /**
     * Generates bit depth permutations
     */
    public static Stream<Arguments> bitdepthInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (BitDepth bitDepth : BitDepth.values())
            argumentBuilder.add(Arguments.of(bitDepth));

        return argumentBuilder.build();

    }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, BitDepth bitDepth, Filter filter, int radius)
            throws UnsupportedEncodingException {
        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/noisygradient/NoisyGradient_" + dimension + "_" + bitDepth + ".zip";
        assumeTrue(FilterImageMSTest.class.getResource(inputName) != null);

        String expectedName = "/msimages/filterimage/FilterImage_" + dimension + "_" + bitDepth + "_" + filter + "_R"
                + radius + ".zip";
        assumeTrue(FilterImageMSTest.class.getResource(expectedName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(FilterImageMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        String expectedPath = URLDecoder.decode(FilterImageMSTest.class.getResource(expectedName).getPath(), "UTF-8");
        Image expectedImage = ImageFactory.createImage("Expected", IJ.openImage(expectedPath));

        // Initialising module and setting parameters
        FilterImage filterImage = new FilterImage(new Modules());
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE, "Test_image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT, false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE, "Test_output");
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS, false);

        switch (filter) {
            case FDOG2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.DOG2D);
                break;
            case FGAUSS2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.GAUSSIAN2D);
                break;
            case FGAUSS3D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.GAUSSIAN3D);
                break;
            case FGRAD2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.GRADIENT2D);
                break;
            case FMAX2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MAXIMUM2D);
                break;
            case FMAX3D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MAXIMUM3D);
                break;
            case FMEAN2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MEAN2D);
                break;
            case FMEAN3D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MEAN3D);
                break;
            case FMEDIAN2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MEDIAN2D);
                break;
            case FMEDIAN3D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MEDIAN3D);
                break;
            case FMIN2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MINIMUM2D);
                break;
            case FMIN3D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.MINIMUM3D);
                break;
            // case FRIDGE2D:
            // filterImage.updateParameterValue(FilterImage.FILTER_MODE,
            // FilterImage.FilterModes.RIDGE_ENHANCEMENT);
            // break;
            case FVAR2D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.VARIANCE2D);
                break;
            case FVAR3D:
                filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.VARIANCE3D);
                break;
        }

        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS, radius);

        // Running Module
        filterImage.execute(workspace);

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
        assertNotNull(new FilterImage(null).getDescription());
    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions and filters.
     * The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimFilterInputProvider")
    void test8Bit(Dimension dimension, Filter filter) throws UnsupportedEncodingException {
        runTest(dimension, BitDepth.B8, filter, 3);

    }

    /**
     * Parameterized test run with all bit depths for D4ZT dimension and 2D mean
     * filter only. The reduced testing here is to keep storage requirements down.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("bitdepthInputProvider")
    void testAllBitDepths_D4ZT_FMEAN(BitDepth bitDepth) throws UnsupportedEncodingException {
        runTest(Dimension.D4ZT, bitDepth, Filter.FMEAN2D, 3);

    }
}
