package io.github.mianalysis.mia.module.images.process.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mianalysis.TestUtils;
import io.github.mianalysis.enums.Dimension;
import io.github.mianalysis.enums.Logic;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Workspace;


public class WatershedTest extends ModuleTest {
    static final String inputMaskImageName = "Mask_image";
    static final String inputMarkerImageName = "Marker_image";
    static final String inputIntensityImageName = "Intensity_image";
    static final String outputImageName = "Test_output";
    static final String expectedImageName = "Expected";
    

    @Override
    public void testGetHelp() {
        assertNotNull(new Watershed(null).getDescription());
    }
  
    // @ParameterizedTest
    // @MethodSource("dimensionLogicInputProvider")
    // void testTest(Dimension dimension,Logic logic) {
        
    // }

    void testImageMaskOnly(Workspace workspace, String expectedImagePath) throws UnsupportedEncodingException {
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage(inputMaskImageName));
        assertNotNull(workspace.getImage(outputImageName));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage(outputImageName);
        Image expectedImage = TestUtils.loadImage(expectedImagePath, expectedImageName);

        assertEquals(expectedImage, outputImage);

    }

    void testImageMaskAndMarker(Workspace workspace, String expectedImagePath) throws UnsupportedEncodingException {
        assertEquals(3, workspace.getImages().size());
        assertNotNull(workspace.getImage(inputMaskImageName));
        assertNotNull(workspace.getImage(inputMarkerImageName));
        assertNotNull(workspace.getImage(outputImageName));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage(outputImageName);
        Image expectedImage = TestUtils.loadImage(expectedImagePath, expectedImageName);

        assertEquals(expectedImage, outputImage);

    }

    void testImageMaskAndIntensity(Workspace workspace, String expectedImagePath) throws UnsupportedEncodingException {
        assertEquals(3, workspace.getImages().size());
        assertNotNull(workspace.getImage(inputMaskImageName));
        assertNotNull(workspace.getImage(inputIntensityImageName));
        assertNotNull(workspace.getImage(outputImageName));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage(outputImageName);
        Image expectedImage = TestUtils.loadImage(expectedImagePath, expectedImageName);

        assertEquals(expectedImage, outputImage);

    }

    void testImageMaskMarkerAndIntensity(Workspace workspace, String expectedImagePath)
            throws UnsupportedEncodingException {
        assertEquals(4, workspace.getImages().size());
        assertNotNull(workspace.getImage(inputMaskImageName));
        assertNotNull(workspace.getImage(inputMarkerImageName));
        assertNotNull(workspace.getImage(inputIntensityImageName));
        assertNotNull(workspace.getImage(outputImageName));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage(outputImageName);
        Image expectedImage = TestUtils.loadImage(expectedImagePath, expectedImageName);

        assertEquals(expectedImage, outputImage);

    }
    
    /*  
        #################
        ### 2D IMAGES ###
        #################
    */

    // @ParameterizedTest
    // @MethodSource("inputProvider")
    // public void pTestTest(Dimension dimension, Logic logic) {
    //     System.err.println(dimension+"____"+logic);

    // }

    @Test @Disabled
    public void testRunWithWhiteObjects2DBlackBackgroundSvensonMatchPixels() throws Exception {
        // // Creating a new workspace
        // Workspaces workspaces = new Workspaces();
        // Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // // Loading the test image and adding to workspace
        // TestUtils.addImageToWorkspace(workspace, "/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip", c;
                
        // // Initialising BinaryOperations
        // DistanceMap distanceMap = new DistanceMap(new Modules());
        // distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, inputMaskImageName);
        // distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, outputImageName);
        // distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        // distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        // distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        // distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // // Running Module
        // distanceMap.execute(workspace);

        // // Checking the images in the workspace
        // String expectedImagePath = "/images/distancemap3D/DistanceMap3D_insideObjects_svensson_match_pixels_2D.tif.zip";
        // testImage(workspace, expectedImagePath);

    }
    
}