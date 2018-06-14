package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.InputOutput.ImageLoader;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.*;

public class ProjectImageTest {
    @Test
    public void testGetTitle() {
        assertNotNull(new ProjectImage().getTitle());
    }

    @Test
    public void testRunMaxZ2D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient2D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMaxZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient3D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // For some reason, 2D images are loaded with pixel depth set to 1
        expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMaxZ4D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMaxZ5D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient5D_ZMaxProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMaxZ3D16bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient3D_ZMaxProj_16bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // For some reason, 2D images are loaded with pixel depth set to 1
        expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMaxZ3D32bit() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient3D_ZMaxProj_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // For some reason, 2D images are loaded with pixel depth set to 1
        expectedImage.getImagePlus().getCalibration().pixelDepth = 0.1;

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MAX);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMinZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZMinProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MIN);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAverageZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZAvProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.AVERAGE);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMedianZ3D() throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZMedProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.MEDIAN);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunStdevZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZStdevProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.STDEV);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunSumZ3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ProjectImage/NoisyGradient4D_ZSumProj_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ProjectImage projectImage = new ProjectImage();
        projectImage.initialiseParameters();
        projectImage.updateParameterValue(ProjectImage.INPUT_IMAGE,"Test_image");
        projectImage.updateParameterValue(ProjectImage.OUTPUT_IMAGE,"Test_output");
        projectImage.updateParameterValue(ProjectImage.PROJECTION_MODE,ProjectImage.ProjectionModes.SUM);
        projectImage.updateParameterValue(ProjectImage.SHOW_IMAGE,false);

        // Running BinaryOperations
        projectImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }
}