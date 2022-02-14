package io.github.mianalysis.mia.module.images.process.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

public class DistanceMapTest extends ModuleTest {

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new DistanceMap(null).getDescription());
    }

    @Test
    public void testRunWithObjects3DBlackBackgroundSvensonWeights() throws Exception {
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
                .getResource("/images/distancemap3D/DistanceMap3D_insideObjects_svensson_3D.tif.zip").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, "Test_image");
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, "Test_output");
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.BLACK_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

    @Test
    public void testRunWithObjects3DWhiteBackgroundSvensonWeights() throws Exception {
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
                .getResource("/images/distancemap3D/DistanceMap3D_insideObjects_svensson_3D.tif.zip").getPath(),
                "UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        DistanceMap distanceMap = new DistanceMap(new Modules());
        distanceMap.updateParameterValue(DistanceMap.INPUT_IMAGE, "Test_image");
        distanceMap.updateParameterValue(DistanceMap.OUTPUT_IMAGE, "Test_output");
        distanceMap.updateParameterValue(DistanceMap.WEIGHT_MODE, DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        distanceMap.updateParameterValue(DistanceMap.MATCH_Z_TO_X, true);
        distanceMap.updateParameterValue(DistanceMap.SPATIAL_UNITS_MODE, DistanceMap.SpatialUnitsModes.PIXELS);
        distanceMap.updateParameterValue(DistanceMap.BINARY_LOGIC, DistanceMap.BinaryLogic.WHITE_BACKGROUND);

        // Running Module
        distanceMap.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2, workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage, outputImage);

    }

}