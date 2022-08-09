package io.github.mianalysis.mia.module.images.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;


public class ProjectImageTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ProjectImage(null).getDescription());
    }

    @Test
    public void testRunMaxZ2D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient2D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient2D_ZMaxProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMaxZ3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient3D_ZMaxProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // For some reason, 2D images are loaded with pixel depth set to 1
        expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMaxZ4D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient4D_ZMaxProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMaxZ5D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient5D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient5D_ZMaxProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMaxZ3D16bit() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_16bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient3D_ZMaxProj_16bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // For some reason, 2D images are loaded with pixel depth set to 1
        expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));
        
        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMaxZ3D32bit() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient3D_ZMaxProj_32bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // For some reason, 2D images are loaded with pixel depth set to 1
        expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMinZ3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient4D_ZMinProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MIN);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunAverageZ3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient4D_ZAvProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.AVERAGE);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunMedianZ3D() throws Exception  {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient4D_ZMedProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MEDIAN);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunStdevZ3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient4D_ZStdevProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.STDEV);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunSumZ3D() throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient4D_ZT_8bit_C1.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/projectimage/NoisyGradient4D_ZSumProj_8bit.zip").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage(null);
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.SUM);

        // Running Module
        projectImage.execute(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }
}