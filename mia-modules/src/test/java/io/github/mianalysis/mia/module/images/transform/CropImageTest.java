package io.github.mianalysis.mia.module.images.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageI;


public class CropImageTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CropImage<>(null).getDescription());
    }

    @Test
    public void testRun8bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/cropimage/NoisyGradient2D_8bit_3-12-52-49.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactories.getDefaultFactory().create("Expected", IJ.openImage(pathToImage));

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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun8bit3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/cropimage/NoisyGradient3D_8bit_3-12-52-49.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactories.getDefaultFactory().create("Expected", IJ.openImage(pathToImage));

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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun16bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_16bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/cropimage/NoisyGradient3D_16bit_3-12-52-49.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactories.getDefaultFactory().create("Expected", IJ.openImage(pathToImage));

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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRun32bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/cropimage/NoisyGradient3D_32bit_3-12-52-49.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactories.getDefaultFactory().create("Expected", IJ.openImage(pathToImage));

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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRun8bit4D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/cropimage/NoisyGradient5D_8bit_C1_3-12-52-49.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactories.getDefaultFactory().create("Expected", IJ.openImage(pathToImage));

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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);
    }

    @Test
    public void testRun8bit5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/cropimage/NoisyGradient5D_8bit_3-12-52-49.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactories.getDefaultFactory().create("Expected", IJ.openImage(pathToImage));

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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}