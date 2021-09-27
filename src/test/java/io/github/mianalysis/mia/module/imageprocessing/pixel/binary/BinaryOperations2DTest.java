package io.github.mianalysis.mia.module.imageprocessing.pixel.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

/**
 * Created by sc13967 on 13/11/2017.
 */

public class BinaryOperations2DTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new BinaryOperations2D(null).getDescription());
    }

    @Test
    public void testRunWithDilate2DOperation2DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_whiteBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation3DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_whiteBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation4DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_whiteBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation5DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_whiteBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilateOperation2DStackOnInputWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_whiteBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, true);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(1, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperationZeroIters2DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 0);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters2DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_whiteBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters3DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_whiteBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters4DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_whiteBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters5DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_whiteBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters2DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_whiteBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters3DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_whiteBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters4DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_whiteBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters5DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_whiteBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation2DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_whiteBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation3DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_whiteBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation4DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_whiteBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation5DStackWhiteBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_whiteBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation2DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_blackBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation3DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_blackBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation4DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_blackBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation5DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_blackBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilateOperation2DStackOnInputBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_blackBG_dilate1.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, true);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(1, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithDilate2DOperationZeroIters2DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 0);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.DILATE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters2DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_blackBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters3DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_blackBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters4DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_blackBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters5DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_blackBG_erode5.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 5);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters2DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_blackBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters3DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_blackBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters4DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_blackBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have
     * been eroded away
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters5DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_blackBG_erode100.tif").getPath(), "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 100);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.ERODE);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation2DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects2D_8bit_blackBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation3DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects3D_8bit_blackBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation4DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects4D_8bit_blackBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation5DStackBlackBG() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image", ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass()
                .getResource("/images/binaryoperations/BinaryObjects5D_8bit_blackBG_fillHoles2D.tif").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations2D binaryOperations = new BinaryOperations2D(new Modules());
        binaryOperations.updateParameterValue(BinaryOperations2D.INPUT_IMAGE, "Test_image");
        binaryOperations.updateParameterValue(BinaryOperations2D.APPLY_TO_INPUT, false);
        binaryOperations.updateParameterValue(BinaryOperations2D.NUM_ITERATIONS, 1);
        binaryOperations.updateParameterValue(BinaryOperations2D.OUTPUT_IMAGE, "Test_output");
        binaryOperations.updateParameterValue(BinaryOperations2D.OPERATION_MODE,
                BinaryOperations2D.OperationModes.FILL_HOLES);
        binaryOperations.updateParameterValue(BinaryOperations2D.BINARY_LOGIC,
                BinaryOperations2D.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        binaryOperations.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }
}