package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 13/11/2017.
 */
public class BinaryOperationsTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new BinaryOperations().getTitle());
    }

    @Test
    public void testRunWithDilate2DOperation2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation3DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation4DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithDilate2DOperation5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithDilateOperation2DStackOnInput() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,true);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithDilate2DOperationZeroIters2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,0);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters3DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters4DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithErode2DOperationFiveIters5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have been eroded away
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have been eroded away
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters3DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have been eroded away
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters4DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    /**
     * This test is designed to check that nothing goes astray when all objects have been eroded away
     * @throws Exception
     */
    @Test
    public void testRunWithErode2DOperationHundredIters5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(
                BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation2DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation3DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation4DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test
    public void testRunWithFillHoles2DOperation5DStack() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        BinaryOperations binaryOperations = new BinaryOperations();
        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);

        // Running BinaryOperations
        binaryOperations.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertEquals(expectedImage,outputImage);

    }

    @Test @Ignore
    public void testRunWithWatershed3DOperation2DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunWithWatershed3DOperation3DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunWithWatershed3DOperation4DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunWithWatershed3DOperation5DStack() throws Exception {
    }
}