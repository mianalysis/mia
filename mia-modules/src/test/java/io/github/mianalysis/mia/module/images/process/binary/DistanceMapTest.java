package io.github.mianalysis.mia.module.images.process.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.mianalysis.mia.TestUtils;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;

public class DistanceMapTest extends ModuleTest {

    static final String inputImageName = "Test_image";
    static final String outputImageName = "Test_output";
    static final String expectedImageName = "Expected";


    void testImage(WorkspaceI workspace, String expectedImagePath) throws UnsupportedEncodingException {
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage(inputImageName));
        assertNotNull(workspace.getImage(outputImageName));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage(outputImageName);
        Image expectedImage = TestUtils.loadImage(expectedImagePath, expectedImageName);
        
        assertEquals(expectedImage, outputImage);

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new DistanceMap(null).getDescription());
    }


    /*  
        #################
        ### 2D IMAGES ###
        #################
    */

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundChessboardMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CHESSBOARD);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_chessboard_match_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundCityblockMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CITY_BLOCK);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_cityblock_match_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundBorgeforsMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.BORGEFORS);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_borgefors_match_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_outsideObjects_svensson_match_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithBlackObjects2DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_whiteBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundSvensonMatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_cal_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundSvensonUnmatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_cal_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects2DBlackBackgroundSvensonUnmatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_pixels_2D.tif.zip";
        testImage(workspace, expectedImagePath);

    }


    /*  
        #################
        ### 3D IMAGES ###
        #################
    */

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundChessboardMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CHESSBOARD);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_chessboard_match_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundCityblockMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CITY_BLOCK);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_cityblock_match_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundBorgeforsMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.BORGEFORS);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_borgefors_match_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_outsideObjects_svensson_match_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithBlackObjects3DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundSvensonMatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_cal_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundSvensonUnmatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_cal_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects3DBlackBackgroundSvensonUnmatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_pixels_3D.tif.zip";
        testImage(workspace, expectedImagePath);

    }
    
    
    /*  
        #################
        ### 4D IMAGES ###
        #################
    */

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundChessboardMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CHESSBOARD);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_chessboard_match_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundCityblockMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CITY_BLOCK);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_cityblock_match_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundBorgeforsMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.BORGEFORS);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_borgefors_match_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_whiteBG.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_outsideObjects_svensson_match_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithBlackObjects4DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_whiteBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundSvensonMatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_cal_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundSvensonUnmatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_cal_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects4DBlackBackgroundSvensonUnmatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_pixels_4D.tif.zip";
        testImage(workspace, expectedImagePath);

    }


    /*  
        #################
        ### 5D IMAGES ###
        #################
    */

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundChessboardMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CHESSBOARD);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_chessboard_match_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundCityblockMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.CITY_BLOCK);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_cityblock_match_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundBorgeforsMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.BORGEFORS);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_borgefors_match_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_whiteBG_diffC.zip", inputImageName);
                
        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_outsideObjects_svensson_match_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithBlackObjects5DWhiteBackgroundSvensonMatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_whiteBG_diffC.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundSvensonMatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_cal_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundSvensonUnmatchCalibrated() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip", inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.CALIBRATED);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_cal_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

    @Test
    public void testRunWithWhiteObjects5DBlackBackgroundSvensonUnmatchPixels() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects5D_8bit_blackBG_diffC.zip",
                inputImageName);

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputImageName);
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, false);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_unmatch_pixels_5D.tif.zip";
        testImage(workspace, expectedImagePath);

    }

}