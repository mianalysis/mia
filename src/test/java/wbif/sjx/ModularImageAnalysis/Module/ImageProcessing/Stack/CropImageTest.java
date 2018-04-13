package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class CropImageTest {
    @Test
    public void getTitle() {
        assertNotNull(new CropImage<>().getTitle());
    }

    @Test
    public void testRun8bit2D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient2D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising CropImage
        CropImage cropImage = new CropImage();
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,52);
        cropImage.updateParameterValue(CropImage.HEIGHT,49);
        cropImage.updateParameterValue(CropImage.SHOW_IMAGE,false);

        // Running CropImage
        cropImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(1,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRun8bit3D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient3D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising CropImage
        CropImage cropImage = new CropImage();
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,52);
        cropImage.updateParameterValue(CropImage.HEIGHT,49);
        cropImage.updateParameterValue(CropImage.SHOW_IMAGE,false);

        // Running CropImage
        cropImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRun16bit2D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient3D_16bit_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising CropImage
        CropImage cropImage = new CropImage();
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,52);
        cropImage.updateParameterValue(CropImage.HEIGHT,49);
        cropImage.updateParameterValue(CropImage.SHOW_IMAGE,false);

        // Running CropImage
        cropImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(16,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRun32bit2D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient3D_32bit_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising CropImage
        CropImage cropImage = new CropImage();
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,52);
        cropImage.updateParameterValue(CropImage.HEIGHT,49);
        cropImage.updateParameterValue(CropImage.SHOW_IMAGE,false);

        // Running CropImage
        cropImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(32,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(1,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRun8bit4D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient5D_8bit_C1_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising CropImage
        CropImage cropImage = new CropImage();
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,52);
        cropImage.updateParameterValue(CropImage.HEIGHT,49);
        cropImage.updateParameterValue(CropImage.SHOW_IMAGE,false);

        // Running CropImage
        cropImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(4,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }

    @Test
    public void testRun8bit5D() throws Exception {
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

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/CropImage/NoisyGradient5D_8bit_3-12-52-49.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising CropImage
        CropImage cropImage = new CropImage();
        cropImage.initialiseParameters();
        cropImage.updateParameterValue(CropImage.INPUT_IMAGE,"Test_image");
        cropImage.updateParameterValue(CropImage.OUTPUT_IMAGE,"Output");
        cropImage.updateParameterValue(CropImage.LEFT,3);
        cropImage.updateParameterValue(CropImage.TOP,12);
        cropImage.updateParameterValue(CropImage.WIDTH,52);
        cropImage.updateParameterValue(CropImage.HEIGHT,49);
        cropImage.updateParameterValue(CropImage.SHOW_IMAGE,false);

        // Running CropImage
        cropImage.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(49,outputImage.getWidth());
        assertEquals(37,outputImage.getHeight());
        assertEquals(2,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(4,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<outputImage.getNChannels();c++) {
            for (int z = 0; z < outputImage.getNSlices(); z++) {
                for (int t = 0; t < outputImage.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    outputImage.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = outputImage.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }
}