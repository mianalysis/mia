package io.github.mianalysis.mia.module.images.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;


public class NormaliseIntensityTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new NormaliseIntensity(null).getDescription());
    }

    @Test
    public void testNormaliseIntensity8bit2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised2D_8bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised3D_8bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit4D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient5D_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised5D_8bit_C1.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised5D_8bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity16bit3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient3D_16bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised3D_16bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity32bitOverOne3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/LightNoisyGradient3D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/LightNoisyGradientNormalised3D_32bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity32bitUnderOne3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient3D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised3D_32bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunDoApply8bit3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalised3D_8bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,true);

        // Running NormaliseIntensity
        normaliseIntensity.execute(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        ImageI outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testNormaliseIntensity8bit2DClipPrecise() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientClip2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/normaliseintensity/DarkNoisyGradientNormalisedClip2D_8bit.zip").getPath(),"UTF-8");
        ImageI expectedImage = ImageFactory.createImage("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity(new Modules());
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
        ImageI outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}