package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 09/09/2017.
 */
public class ChannelExtractorTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ChannelExtractor().getTitle());

    }

    @Test
    public void testRun8bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C1.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(6,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int z=0;z<outputImage.getNSlices();z++) {
            for (int t=0;t<outputImage.getNSlices();t++) {
                expectedImage.setPosition(1,z+1,t+1);
                outputImage.setPosition(1,z+1,t+1);

                int[][] expectedValues = expectedImage.getProcessor().getIntArray();
                int[][] actualValues = outputImage.getProcessor().getIntArray();

                assertArrayEquals(expectedValues,actualValues);

            }
        }
    }

    @Test
    public void testRun8bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_8bit_C2.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(8,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(6,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int z=0;z<outputImage.getNSlices();z++) {
            for (int t=0;t<outputImage.getNSlices();t++) {
                expectedImage.setPosition(1,z+1,t+1);
                outputImage.setPosition(1,z+1,t+1);

                int[][] expectedValues = expectedImage.getProcessor().getIntArray();
                int[][] actualValues = outputImage.getProcessor().getIntArray();

                assertArrayEquals(expectedValues,actualValues);

            }
        }
    }

    @Test
    public void testRun16bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_16bit_C1.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(16,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(6,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int z=0;z<outputImage.getNSlices();z++) {
            for (int t=0;t<outputImage.getNSlices();t++) {
                expectedImage.setPosition(1,z+1,t+1);
                outputImage.setPosition(1,z+1,t+1);

                int[][] expectedValues = expectedImage.getProcessor().getIntArray();
                int[][] actualValues = outputImage.getProcessor().getIntArray();

                assertArrayEquals(expectedValues,actualValues);

            }
        }
    }

    @Test
    public void testRun16bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_16bit_C2.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(16,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(6,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int z=0;z<outputImage.getNSlices();z++) {
            for (int t=0;t<outputImage.getNSlices();t++) {
                expectedImage.setPosition(1,z+1,t+1);
                outputImage.setPosition(1,z+1,t+1);

                int[][] expectedValues = expectedImage.getProcessor().getIntArray();
                int[][] actualValues = outputImage.getProcessor().getIntArray();

                assertArrayEquals(expectedValues,actualValues);

            }
        }
    }

    @Test
    public void testRun32bit5DChannel1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_32bit_C1.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,1);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(32,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(6,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int z=0;z<outputImage.getNSlices();z++) {
            for (int t=0;t<outputImage.getNSlices();t++) {
                expectedImage.setPosition(1,z+1,t+1);
                outputImage.setPosition(1,z+1,t+1);

                float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                float[][] actualValues = outputImage.getProcessor().getFloatArray();

                assertArrayEquals(expectedValues,actualValues);

            }
        }
    }

    @Test
    public void testRun32bit5DChannel2() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image_5D",ipl);
        workspace.addImage(image);

        pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient5D_32bit_C2.tif").getPath(),"UTF-8");
        ImagePlus expectedImage = IJ.openImage(pathToImage);

        // Initialising ChannelExtractor
        ChannelExtractor channelExtractor = new ChannelExtractor();
        channelExtractor.initialiseParameters();
        channelExtractor.updateParameterValue(ChannelExtractor.INPUT_IMAGE,"Test_image_5D");
        channelExtractor.updateParameterValue(ChannelExtractor.OUTPUT_IMAGE,"Test_output");
        channelExtractor.updateParameterValue(ChannelExtractor.CHANNEL_TO_EXTRACT,2);
        channelExtractor.updateParameterValue(ChannelExtractor.SHOW_IMAGE,false);

        // Running ChannelExtractor
        channelExtractor.run(workspace,false);

        // Checking the images in the workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test_image_5D"));
        assertNotNull(workspace.getImage("Test_output"));

        // Checking the output image has the expected calibration
        ImagePlus outputImage = workspace.getImage("Test_output").getImagePlus();
        assertEquals(dppXY,outputImage.getCalibration().pixelWidth,1E-2);
        assertEquals(dppZ,outputImage.getCalibration().pixelDepth,1E-2);
        assertEquals(calibratedUnits,outputImage.getCalibration().getXUnit());
        assertEquals(32,outputImage.getBitDepth());

        // Checking the size of the output image
        assertEquals(64,outputImage.getWidth());
        assertEquals(76,outputImage.getHeight());
        assertEquals(1,outputImage.getNChannels());
        assertEquals(12,outputImage.getNSlices());
        assertEquals(6,outputImage.getNFrames());

        // Checking the individual image pixel values
        for (int z=0;z<outputImage.getNSlices();z++) {
            for (int t=0;t<outputImage.getNSlices();t++) {
                expectedImage.setPosition(1,z+1,t+1);
                outputImage.setPosition(1,z+1,t+1);

                float[][] expectedValues = expectedImage.getProcessor().getFloatArray();
                float[][] actualValues = outputImage.getProcessor().getFloatArray();

                assertArrayEquals(expectedValues,actualValues);

            }
        }
    }
}