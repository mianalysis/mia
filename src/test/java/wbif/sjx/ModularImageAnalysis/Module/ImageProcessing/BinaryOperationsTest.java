//package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;
//
//import ij.IJ;
//import ij.ImagePlus;
//import org.junit.Ignore;
//import org.junit.Test;
//import wbif.sjx.ModularImageAnalysis.Object.Image;
//import wbif.sjx.ModularImageAnalysis.Object.Workspace;
//
//import java.net.URLDecoder;
//
//import static org.junit.Assert.*;
//
///**
// * Created by sc13967 on 13/11/2017.
// */
//public class BinaryOperationsTest {
//    @Test
//    public void testGetTitle() throws Exception {
//        assertNotNull(new BinaryOperations().getTitle());
//    }
//
//    @Test
//    public void testRunWithDilate2DOperation2DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(1,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithDilate2DOperation3DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithDilate2DOperation4DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithDilate2DOperation5DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(2,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithDilateOperation2DStackOnInput() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_dilate1.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,true);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(1,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_image").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(1,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithDilate2DOperationZeroIters2DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,0);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.DILATE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(1,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithErode2DOperationFiveIters2DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(1,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithErode2DOperationFiveIters3DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithErode2DOperationFiveIters4DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithErode2DOperationFiveIters5DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_erode5.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,5);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(2,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    /**
//     * This test is designed to check that nothing goes astray when all objects have been eroded away
//     * @throws Exception
//     */
//    @Test
//    public void testRunWithErode2DOperationHundredIters2DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(1,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    /**
//     * This test is designed to check that nothing goes astray when all objects have been eroded away
//     * @throws Exception
//     */
//    @Test
//    public void testRunWithErode2DOperationHundredIters3DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    /**
//     * This test is designed to check that nothing goes astray when all objects have been eroded away
//     * @throws Exception
//     */
//    @Test
//    public void testRunWithErode2DOperationHundredIters4DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    /**
//     * This test is designed to check that nothing goes astray when all objects have been eroded away
//     * @throws Exception
//     */
//    @Test
//    public void testRunWithErode2DOperationHundredIters5DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_erode100.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,100);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.ERODE);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(2,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithFillHoles2DOperation2DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects2D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects2D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(1,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithFillHoles2DOperation3DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects3D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(1,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testRunWithFillHoles2DOperation4DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects4D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects4D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(1,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test @Ignore
//    public void testRunWithFillHoles2DOperation5DStack() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting calibration parameters
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Loading the test image and adding to workspace
//        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects5D_8bit_whiteBG.tif").getPath(),"UTF-8");
//        ImagePlus ipl = IJ.openImage(pathToImage);
//        Image image = new Image("Test_image",ipl);
//        workspace.addImage(image);
//
//        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryOperations/BinaryObjects5D_8bit_whiteBG_fillHoles2D.tif").getPath(),"UTF-8");
//        ImagePlus expectedImage = IJ.openImage(pathToImage);
//
//        // Initialising BinaryOperations
//        BinaryOperations binaryOperations = new BinaryOperations();
//        binaryOperations.updateParameterValue(BinaryOperations.INPUT_IMAGE,"Test_image");
//        binaryOperations.updateParameterValue(BinaryOperations.APPLY_TO_INPUT,false);
//        binaryOperations.updateParameterValue(BinaryOperations.NUM_ITERATIONS,1);
//        binaryOperations.updateParameterValue(BinaryOperations.OUTPUT_IMAGE,"Test_output");
//        binaryOperations.updateParameterValue(BinaryOperations.OPERATION_MODE,BinaryOperations.OperationModes.FILL_HOLES_2D);
//
//        // Running BinaryOperations
//        binaryOperations.run(workspace,false);
//
//        // Checking the images in the workspace
//        assertEquals(2,workspace.getImages().size());
//        assertNotNull(workspace.getImage("Test_image"));
//        assertNotNull(workspace.getImage("Test_output"));
//
//        // Checking the output image has the expected calibration
//        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
//        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
//        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
//        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
//        assertEquals(8,outputImage.getBitDepth());
//
//        // Checking the size of the output image
//        assertEquals(64,outputImage.getWidth());
//        assertEquals(76,outputImage.getHeight());
//        assertEquals(2,outputImage.getNChannels());
//        assertEquals(12,outputImage.getNSlices());
//        assertEquals(4,outputImage.getNFrames());
//
//        // Checking the individual image pixel values
//        for (int c=0;c<outputImage.getNChannels();c++) {
//            for (int z = 0; z < outputImage.getNSlices(); z++) {
//                for (int t = 0; t < outputImage.getNFrames(); t++) {
//                    expectedImage.setPosition(c+1, z + 1, t + 1);
//                    outputImage.setPosition(c+1, z + 1, t + 1);
//
//                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
//                    float[][] actualValues = outputImage.getProcessor().getFloatArray();
//
//                    assertArrayEquals(expectedValues, actualValues);
//
//                }
//            }
//        }
//    }
//
//    @Test @Ignore
//    public void testRunWithWatershed3DOperation2DStack() throws Exception {
//    }
//
//    @Test @Ignore
//    public void testRunWithWatershed3DOperation3DStack() throws Exception {
//    }
//
//    @Test @Ignore
//    public void testRunWithWatershed3DOperation4DStack() throws Exception {
//    }
//
//    @Test @Ignore
//    public void testRunWithWatershed3DOperation5DStack() throws Exception {
//    }
//}