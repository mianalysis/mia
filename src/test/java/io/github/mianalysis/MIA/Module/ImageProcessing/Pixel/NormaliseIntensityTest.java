package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.WorkspaceCollection;

public class NormaliseIntensityTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new NormaliseIntensity(null).getDescription());
    }

    @Test
    public void testNormaliseIntensity8bit2D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised2D_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit3D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit4D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised5D_8bit_C1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit5D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised5D_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity16bit3D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_16bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity32bitOverOne3D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/LightNoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/LightNoisyGradientNormalised3D_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity32bitUnderOne3D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunDoApply8bit3D() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,true);

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit2DClipPrecise() throws Exception {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientClip2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalisedClip2D_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new ModuleCollection());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.CALCULATION_MODE,NormaliseIntensity.CalculationModes.PRECISE);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.CLIP_FRACTION_MIN,0.01);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.CLIP_FRACTION_MAX,0.01);

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}