package io.github.mianalysis.mia.module.images.process.binary;

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
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Gemma and George on 05/10/2022.
 */
public class BinaryOperations2DMSTest extends ModuleTest {

    enum Filter {
        FDILATE,
        FDISTANCE_MAP,
        FERODE,
        FFILL_HOLES,
        FOUTLINE,
        FSKELETONISE,
        FULTIMATE_POINTS,
        FVORONOI,
        FWATERSHED;
    }

    /**
     * Generates all permutations
     */
    public static Stream<Arguments> dimThresholdLogicInputProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Dimension dimension : Dimension.values())
            for (Filter filter : Filter.values())
                for (Logic logic : Logic.values())
                    for (OutputMode outputMode : OutputMode.values())
                        for (ImageType imageType : ImageType.values())
                            argumentBuilder.add(Arguments.of(dimension, filter, logic, outputMode, imageType));

        return argumentBuilder.build();

    }

    /**
     * Parameterized test run with 8-bit bit depth and all dimensions, all filter
     * algorithms and all logics.
     * 
     * @throws UnsupportedEncodingException
     */
    @ParameterizedTest
    @MethodSource("dimThresholdLogicInputProvider")
    void testAll(Dimension dimension, Filter filter, Logic logic, OutputMode outputMode, ImageType imageType)
            throws UnsupportedEncodingException {
        runTest(dimension, filter, logic, 1, 1, outputMode, imageType);
    }

    // /*
    //  * Used for testing a single set of parameters
    //  */
    // @Test
    // void singleTest() throws UnsupportedEncodingException {
    //     runTest(Dimension.D3T, Filter.FOUTLINE, Logic.LW, 1, 1, OutputMode.CREATE_NEW,
    //             ImageType.IMAGEPLUS);
    // }

    /**
     * Performs the test
     * 
     * @throws UnsupportedEncodingException
     */
    public static void runTest(Dimension dimension, Filter filter, Logic logic, int nIterations, int count,
            OutputMode outputMode,
            ImageType imageType)
            throws UnsupportedEncodingException {
        boolean applyToInput = outputMode.equals(OutputMode.APPLY_TO_INPUT);

        // Checks input image and expected images are available. If not found, the test
        // skips
        String inputName = "/msimages/binaryobjects/BinaryObjects_" + dimension + "_B8_" + logic + ".zip";
        assumeTrue(BinaryOperations2DMSTest.class.getResource(inputName) != null);

        String expectedName = "/msimages/binaryoperations2D/BinaryOps2D_" + dimension + "_B8_" + filter + "_" + logic
                + "_C" + count + "_I" + nIterations + ".zip";
        assumeTrue(BinaryOperations2DMSTest.class.getResource(expectedName) != null);

        // Doing the main part of the test
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String inputPath = URLDecoder.decode(BinaryOperations2DMSTest.class.getResource(inputName).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(inputPath);
        ImageI image = ImageFactory.createImage("Test_image", ipl, imageType);
        workspace.addImage(image);

        String expectedPath = URLDecoder.decode(BinaryOperations2DMSTest.class.getResource(expectedName).getPath(),
                "UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(expectedPath), imageType);

        // Initialising module and setting parameters
        BinaryOperations2D module = new BinaryOperations2D(new Modules());
        module.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        module.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, applyToInput);
        module.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        module.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, nIterations);
        module.updateParameterValue(BinaryOperations2D.COUNT, count);

        switch (filter) {
            case FDILATE:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.DILATE);
                break;
            case FDISTANCE_MAP:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.DISTANCE_MAP);
                break;
            case FERODE:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE, BinaryOperations2D.OperationModes.ERODE);
                break;
            case FFILL_HOLES:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.FILL_HOLES);
                break;
            case FOUTLINE:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.OUTLINE);
                break;
            case FSKELETONISE:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.SKELETONISE);
                break;
            case FULTIMATE_POINTS:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.ULTIMATE_POINTS);
                break;
            case FVORONOI:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.VORONOI);
                break;
            case FWATERSHED:
                module.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                        BinaryOperations2D.OperationModes.WATERSHED);
                break;
        }

        switch (logic) {
            case LB:
                module.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                        BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);
                break;
            case LW:
                module.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                        BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);
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
        assertNotNull(new BinaryOperations2D(null).getDescription());
    }

}
