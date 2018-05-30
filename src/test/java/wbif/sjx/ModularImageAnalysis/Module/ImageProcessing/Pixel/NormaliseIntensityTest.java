package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class NormaliseIntensityTest {
    private float tolerance = 1E-6f;

    @Test
    public void testGetTitle() {
        assertNotNull(new NormaliseIntensity().getTitle());
    }

    @Test
    public void testNormaliseIntensity8bit2D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised2D_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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
    public void testNormaliseIntensity8bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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
    public void testNormaliseIntensity8bit4D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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
    public void testNormaliseIntensity8bit5D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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

    @Test
    public void testNormaliseIntensity16bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(16,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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
    public void testNormaliseIntensity32bitOverOne3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/LightNoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/LightNoisyGradientNormalised3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(32,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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

                    for (int i = 0;i<expectedValues.length;i++) {
                        assertArrayEquals(expectedValues[i], actualValues[i], (float) tolerance);
                    }

                }
            }
        }
    }

    @Test
    public void testNormaliseIntensity32bitUnderOne3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,false);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.OUTPUT_IMAGE,"Test_output");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(32,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
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

                    for (int i = 0;i<expectedValues.length;i++) {
                        assertArrayEquals(expectedValues[i], actualValues[i], (float) tolerance);
                    }
                }
            }
        }
    }

    @Test
    public void testRunDoApply8bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NormaliseIntensity/DarkNoisyGradientNormalised3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising BinaryOperations
        NormaliseIntensity normaliseIntensity = new NormaliseIntensity();
        normaliseIntensity.updateParameterValue(NormaliseIntensity.INPUT_IMAGE,"Test_image");
        normaliseIntensity.updateParameterValue(NormaliseIntensity.APPLY_TO_INPUT,true);
        normaliseIntensity.updateParameterValue(NormaliseIntensity.SHOW_IMAGE,false);

        // Running NormaliseIntensity
        normaliseIntensity.run(workspace);

        // Checking the images in the workspace
        assertEquals(1,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image"));

        // Checking the output image has the expected calibration
        assertEquals(dppXY,ipl.getCalibration().pixelWidth,1E-2);
        assertEquals(calibratedUnits,ipl.getCalibration().getXUnit());
        assertEquals(8,ipl.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,ipl.getWidth());
        assertEquals(76,ipl.getHeight());
        assertEquals(1,ipl.getNChannels());
        assertEquals(12,ipl.getNSlices());
        assertEquals(1,ipl.getNFrames());

        // Checking the individual image pixel values
        for (int c=0;c<ipl.getNChannels();c++) {
            for (int z = 0; z < ipl.getNSlices(); z++) {
                for (int t = 0; t < ipl.getNFrames(); t++) {
                    expectedImage.setPosition(c+1, z + 1, t + 1);
                    ipl.setPosition(c+1, z + 1, t + 1);

                    float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                    float[][] actualValues = ipl.getProcessor().getFloatArray();

                    assertArrayEquals(expectedValues, actualValues);

                }
            }
        }
    }
}