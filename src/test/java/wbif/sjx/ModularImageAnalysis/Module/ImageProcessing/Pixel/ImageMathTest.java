package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Measurement;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class ImageMathTest {
    @Test
    public void getTitle() {
        assertNotNull(new ImageMath().getTitle());
    }

    @Test
    public void testRunAddPositive2D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient2D_Add50_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddPositive3D8bit() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Add50_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddPositive3D16bit() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Add50_16bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddPositive3D32bit() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Add50_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddPositive4D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient5D_Add50_8bit_C1.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddPositive5D() throws Exception {
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient5D_Add50_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddPositiveToInput5D() throws Exception {
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient5D_Add50_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,true);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,50d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_image");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddMeasurement5D() throws Exception {
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Adding a measurement to the input image
        image.addMeasurement(new Measurement("Test meas",12.4));

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient5D_AddMeas_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.MEASUREMENT);
        imageMath.updateParameterValue(ImageMath.MEASUREMENT,"Test meas");

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunAddNegative3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Add-5_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.ADD);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,-5d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunSubtractPositive3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Subtract12_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.SUBTRACT);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,12d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunSubtractNegative3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Subtract-12_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.SUBTRACT);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,-12d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMultiplyPositive3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Multiply2p3_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.MULTIPLY);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,2.3d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMultiplyNegative3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Multiply-2p3_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.MULTIPLY);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,-2.3d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunMultiplyNegative3D32bit() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Multiply-2p3_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.MULTIPLY);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,-2.3d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRunDividePositive3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Divide0p4_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.DIVIDE);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,0.4d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRuDivideNegative3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Divide-0p6_8bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.DIVIDE);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,-0.6d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }

    @Test
    public void testRuDivideNegative3D32bit() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/ImageMath/NoisyGradient3D_Divide-0p6_32bit.tif").getPath(),"UTF-8");
        Image expectedImage = new Image("Expected", IJ.openImage(pathToImage));

        // Initialising BinaryOperations
        ImageMath imageMath = new ImageMath();
        imageMath.initialiseParameters();
        imageMath.updateParameterValue(ImageMath.INPUT_IMAGE,"Test_image");
        imageMath.updateParameterValue(ImageMath.OUTPUT_IMAGE,"Test_output");
        imageMath.updateParameterValue(ImageMath.APPLY_TO_INPUT,false);
        imageMath.updateParameterValue(ImageMath.CALCULATION_TYPE,ImageMath.CalculationTypes.DIVIDE);
        imageMath.updateParameterValue(ImageMath.VALUE_SOURCE,ImageMath.ValueSources.FIXED);
        imageMath.updateParameterValue(ImageMath.MATH_VALUE,-0.6d);

        // Running BinaryOperations
        imageMath.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        Image outputImage = workspace.getImage("Test_output");
        assertTrue(outputImage.equals(expectedImage));

    }
}