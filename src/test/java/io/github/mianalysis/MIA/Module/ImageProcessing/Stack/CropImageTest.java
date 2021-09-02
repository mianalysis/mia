package io.github.mianalysis.mia.module.imageprocessing.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.Object.Image;
import io.github.mianalysis.mia.Object.Workspace;
import io.github.mianalysis.mia.Object.Workspaces;

public class CropImageTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CropImage<>(null).getDescription());
    }

    @Test
    public void testRun8bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient2D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising CropImage
        CropImage cropImage = new CropImage(new Modules());
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Test_output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,49);
        cropImage.updateParameterValue(CropImage.HEIGHT,37);

        // Running CropImage
        cropImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun8bit3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient3D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising CropImage
        CropImage cropImage = new CropImage(new Modules());
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Test_output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,49);
        cropImage.updateParameterValue(CropImage.HEIGHT,37);

        // Running CropImage
        cropImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun16bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient3D_16bit_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising CropImage
        CropImage cropImage = new CropImage(new Modules());
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Test_output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,49);
        cropImage.updateParameterValue(CropImage.HEIGHT,37);

        // Running CropImage
        cropImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun32bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient3D_32bit_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising CropImage
        CropImage cropImage = new CropImage(new Modules());
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Test_output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,49);
        cropImage.updateParameterValue(CropImage.HEIGHT,37);

        // Running CropImage
        cropImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRun8bit4D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient4D_ZT_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient5D_8bit_C1_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising CropImage
        CropImage cropImage = new CropImage(new Modules());
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Test_output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,49);
        cropImage.updateParameterValue(CropImage.HEIGHT,37);

        // Running CropImage
        cropImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRun8bit5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient5D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising CropImage
        CropImage cropImage = new CropImage(new Modules());
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Test_output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,49);
        cropImage.updateParameterValue(CropImage.HEIGHT,37);

        // Running CropImage
        cropImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}